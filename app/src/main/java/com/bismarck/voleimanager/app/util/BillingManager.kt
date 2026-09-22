package com.bismarck.voleimanager.app.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * IDs de produto/plano base das assinaturas premium — precisam bater exatamente com o que for
 * cadastrado no Play Console (Monetizar > Produtos > Assinaturas). Os preços em si (incluindo o
 * ajuste de preço por país, ex.: R$ 9,99/R$ 19,99 por mês no Brasil x ~US$1,89/US$3,89 fora dele)
 * ficam inteiramente configurados lá — o app nunca hardcoda valores monetários usados de verdade, só um
 * texto de fallback (ver `cloud_sync_plan_*_price` em strings.xml) para quando a Play Store ainda
 * não respondeu com o preço real (offline, sem os produtos cadastrados, etc.).
 */
object BillingProductIds {
    const val SINGLE_GROUP = "premium_single_group"
    const val MULTI_GROUP = "premium_multi_group"
    const val BASE_PLAN_MONTHLY = "mensal"
    const val BASE_PLAN_ANNUAL = "anual"
}

/** Uma oferta de assinatura comprável: um par (produto, plano base) já com o preço formatado e
 *  localizado devolvido pela própria Play Store (respeita moeda/preço regional configurados no
 *  Play Console, sem o app precisar saber a conversão). */
data class SubscriptionOffer(
    val productId: String,
    val basePlanId: String,
    val offerToken: String,
    val formattedPrice: String,
    /** Período de faturamento ISO-8601 (ex.: "P1M" mensal, "P1Y" anual). */
    val billingPeriodIso: String,
    /** Preço bruto (micros da unidade monetária, ex.: R$ 1,00 = 1_000_000) e código ISO-4217 da
     *  moeda — usados só para calcular localmente o "equivalente por mês" do plano anual (preço
     *  anual / 12) e mostrar a economia real na moeda do próprio usuário, sem o app precisar saber
     *  taxas de câmbio. [formattedPrice] continua sendo o texto oficial mostrado como preço. */
    val priceAmountMicros: Long,
    val priceCurrencyCode: String
)

/** Compra vista pelo Play Billing neste aparelho (nova ou já conhecida de uma sessão anterior),
 *  emitida para o [VoleiViewModel][com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel]
 *  repassar ao backend (`linkPurchaseToken`) e obter um entitlement validado de verdade — ver
 *  `purchase-validation-function`. */
data class AcknowledgedPurchase(val purchaseToken: String, val productId: String)

/**
 * Fachada sobre o Google Play Billing Library — busca as ofertas de assinatura cadastradas no
 * Play Console, lança o fluxo de compra nativo da Play Store e reconhece (`acknowledge`) compras
 * concluídas (obrigatório em até 3 dias, senão a Play Store estorna automaticamente).
 *
 * O que é refletido aqui ([activeProductIds]) é só o conhecimento **local e otimista** da última
 * compra vista no aparelho (via `queryPurchasesAsync`/`PurchasesUpdatedListener`) — a fonte de
 * verdade definitiva da assinatura (renovação, cancelamento, período de graça, reembolso) precisa
 * ser validada no backend por Real-time Developer Notifications (`purchase-validation-function`,
 * ainda não implementada em `volei_manager_backend`) e refletida em `users/{uid}.activeEntitlement`
 * no Firestore. Até essa fase existir, [VoleiViewModel] usa este estado local como um substituto
 * best-effort, do mesmo jeito que [CloudFunctionsManager.switchPremiumGroup] já é "melhor esforço"
 * enquanto não há assinatura validada no servidor.
 */
object BillingManager {
    private const val TAG = "BillingManager"

    private var billingClient: BillingClient? = null
    private var productDetailsCache: Map<String, ProductDetails> = emptyMap()

    private val _offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    val offers: StateFlow<List<SubscriptionOffer>> = _offers.asStateFlow()

    /** IDs de produto (ex.: [BillingProductIds.SINGLE_GROUP]) com uma compra ativa reconhecida
     *  localmente. Vazio enquanto não há nenhuma assinatura ativa conhecida. */
    private val _activeProductIds = MutableStateFlow<Set<String>>(emptySet())
    val activeProductIds: StateFlow<Set<String>> = _activeProductIds.asStateFlow()

    /** Ver [AcknowledgedPurchase]. Buffer pequeno só para não perder um evento emitido antes do
     *  ViewModel terminar de se inscrever (init roda [init] e a coleta quase na mesma linha). */
    private val _purchaseEvents = MutableSharedFlow<AcknowledgedPurchase>(extraBufferCapacity = 4)
    val purchaseEvents: SharedFlow<AcknowledgedPurchase> = _purchaseEvents.asSharedFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.forEach(::handlePurchase)
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Log.d(TAG, "Usuário cancelou o fluxo de compra.")
            else ->
                Log.d(TAG, "Compra não concluída: ${billingResult.debugMessage}")
        }
    }

    /** Deve ser chamado uma vez na inicialização do app (feito em `VoleiViewModel`, mesmo padrão de
     *  [AuthManager.init]/[TelemetryManager.init]). Nunca conecta de verdade em testes unitários. */
    fun init(context: Context) {
        if (isRunningInUnitTest || billingClient != null) return
        val client = BillingClient.newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            // PBL 8.0.0+: deixa a própria biblioteca reconectar ao serviço da Play Store quando
            // uma chamada é feita com a conexão caída, em vez de exigir startConnection() manual
            // a cada onBillingServiceDisconnected.
            .enableAutoServiceReconnection()
            .build()
        billingClient = client
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: com.android.billingclient.api.BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryOffers()
                    refreshPurchases()
                } else {
                    Log.d(TAG, "Falha ao conectar ao Play Billing: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Play Billing desconectado — tentará reconectar na próxima operação.")
            }
        })
    }

    private fun queryOffers() {
        val client = billingClient ?: return
        val products = listOf(BillingProductIds.SINGLE_GROUP, BillingProductIds.MULTI_GROUP).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        // PBL 8.0.0 mudou a assinatura do listener: o segundo parâmetro agora é um
        // QueryProductDetailsResult (com productDetailsList + unfetchedProductList), não mais a
        // List<ProductDetails> direta.
        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                // Esperado até os produtos existirem no Play Console (ver plano de implementação).
                Log.d(TAG, "queryProductDetails falhou: ${billingResult.debugMessage}")
                return@queryProductDetailsAsync
            }
            val detailsList = queryProductDetailsResult.productDetailsList
            if (queryProductDetailsResult.unfetchedProductList.isNotEmpty()) {
                // Esperado até todos os produtos/planos existirem no Play Console.
                Log.d(TAG, "Produtos não encontrados: ${queryProductDetailsResult.unfetchedProductList}")
            }
            productDetailsCache = detailsList.associateBy { it.productId }
            _offers.value = detailsList.flatMap { details ->
                details.subscriptionOfferDetails.orEmpty().mapNotNull { offer ->
                    val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()
                        ?: return@mapNotNull null
                    SubscriptionOffer(
                        productId = details.productId,
                        basePlanId = offer.basePlanId,
                        offerToken = offer.offerToken,
                        formattedPrice = pricingPhase.formattedPrice,
                        billingPeriodIso = pricingPhase.billingPeriod,
                        priceAmountMicros = pricingPhase.priceAmountMicros,
                        priceCurrencyCode = pricingPhase.priceCurrencyCode
                    )
                }
            }
        }
    }

    /** Lança o fluxo de compra nativo da Play Store para [offer]. Retorna `false` se o billing
     *  ainda não está pronto (ex.: sem conexão, produtos não carregados) — quem chama deve tratar
     *  isso mostrando uma mensagem amigável em vez de travar. */
    fun launchPurchaseFlow(activity: Activity, offer: SubscriptionOffer): Boolean {
        val client = billingClient ?: return false
        val productDetails = productDetailsCache[offer.productId] ?: return false
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offer.offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val result = client.launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    /** Reconsulta as assinaturas ativas conhecidas pela Play Store neste aparelho/conta (ex.: ao
     *  reabrir o app), atualizando [activeProductIds] e reconhecendo qualquer compra pendente. */
    fun refreshPurchases() {
        val client = billingClient ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "queryPurchasesAsync falhou: ${billingResult.debugMessage}")
                return@queryPurchasesAsync
            }
            val active = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            _activeProductIds.value = active.flatMap { it.products }.toSet()
            active.forEach(::handlePurchase)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        _activeProductIds.value = _activeProductIds.value + purchase.products
        purchase.products.firstOrNull()?.let { productId ->
            _purchaseEvents.tryEmit(AcknowledgedPurchase(purchase.purchaseToken, productId))
        }
        if (purchase.isAcknowledged) return
        val client = billingClient ?: return
        val ackParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(ackParams) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "acknowledgePurchase falhou: ${billingResult.debugMessage}")
            }
        }
    }
}
