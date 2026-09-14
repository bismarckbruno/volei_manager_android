package com.bismarck.voleimanager.app.util

import android.os.Build

/**
 * Detecta se o app está rodando dentro de um teste local (Robolectric), que simula o Android em
 * uma JVM comum sem acesso à rede real. Robolectric define `Build.FINGERPRINT == "robolectric"`
 * por padrão (nenhum dispositivo/emulador real produz esse valor).
 *
 * Usado por [AuthManager]/[CloudFunctionsManager] para nunca tentar uma chamada de rede real
 * (Firebase Auth/Firestore/Functions) em testes: mesmo com um `google-services.json` real
 * configurado (necessário para a telemetria), o SDK do Firebase não expira essas chamadas sozinho
 * sem um timeout explícito, e a JVM de teste não tem acesso à internet — o resultado seria travar
 * threads de `Dispatchers.IO`/`Dispatchers.Default` por minutos (ou para sempre) em vez de falhar
 * rápido como acontece quando o Firebase realmente não está configurado.
 */
internal val isRunningInUnitTest: Boolean
    get() = Build.FINGERPRINT == "robolectric"
