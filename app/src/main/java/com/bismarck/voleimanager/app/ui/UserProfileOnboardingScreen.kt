package com.bismarck.voleimanager.app.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.ui.viewmodel.UserProfileType

/**
 * Primeira pergunta do onboarding, perguntada uma única vez (antes de qualquer etapa do
 * onboarding de grupo): qual é o perfil do usuário no app. Organizador e Auxiliar devem, na
 * sequência, ser direcionados ao cadastro/login gratuito (tela de Nuvem); Espectador segue sem
 * essa exigência.
 */
@Composable
fun UserProfileOnboardingScreen(onProfileSelected: (UserProfileType) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<UserProfileType?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.onboarding_profile_question),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.onboarding_profile_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val options = listOf(
                    UserProfileType.ORGANIZADOR,
                    UserProfileType.AUXILIAR,
                    UserProfileType.ESPECTADOR
                )
                items(options) { profile ->
                    UserProfileOptionCard(
                        profile = profile,
                        selected = selected == profile,
                        onSelect = { selected = profile }
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { selected?.let(onProfileSelected) },
                            enabled = selected != null
                        ) {
                            Text(stringResource(R.string.continue_word))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserProfileOptionCard(
    profile: UserProfileType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val (titleRes, descriptionRes, icon) = when (profile) {
        UserProfileType.ORGANIZADOR -> Triple(
            R.string.user_profile_organizer,
            R.string.user_profile_organizer_desc,
            Icons.Filled.SupervisorAccount
        )
        UserProfileType.AUXILIAR -> Triple(
            R.string.user_profile_assistant,
            R.string.user_profile_assistant_desc,
            Icons.Filled.Groups
        )
        UserProfileType.ESPECTADOR -> Triple(
            R.string.user_profile_viewer,
            R.string.user_profile_viewer_desc,
            Icons.Filled.Visibility
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = stringResource(titleRes), fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Passo obrigatório logo após escolher o perfil Organizador/Auxiliar: sem botão de pular, já que
 * uma conta gratuita é exigida antes de prosseguir para o onboarding de grupo (mas a confirmação
 * do e-mail em si não é obrigatória — ver [com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel.onAuthGatePassed]).
 */
@Composable
fun MandatoryAccountGateScreen(onLoginClick: () -> Unit, onSignUpClick: () -> Unit, onBackClick: () -> Unit) {
    ProfileRoutingScreenScaffold(
        titleRes = R.string.onboarding_auth_required_title,
        hintRes = R.string.onboarding_auth_required_hint,
        onBackClick = onBackClick
    ) {
        Button(onClick = onSignUpClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.signup_title))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.login_title))
        }
    }
}

/** Sugestão pulável de login/cadastro para quem escolheu o perfil Espectador. */
@Composable
fun SpectatorAuthSuggestionScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSkip: () -> Unit,
    onBackClick: () -> Unit
) {
    ProfileRoutingScreenScaffold(
        titleRes = R.string.onboarding_spectator_auth_title,
        hintRes = R.string.onboarding_spectator_auth_hint,
        onBackClick = onBackClick
    ) {
        Button(onClick = onSignUpClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.signup_title))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.login_title))
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}

/** Sugestão pulável de código de convite de grupo, exibida após [SpectatorAuthSuggestionScreen]. */
@Composable
fun SpectatorJoinSuggestionScreen(onJoinClick: () -> Unit, onSkip: () -> Unit, onBackClick: () -> Unit) {
    ProfileRoutingScreenScaffold(
        titleRes = R.string.onboarding_spectator_join_title,
        hintRes = R.string.onboarding_spectator_join_hint,
        onBackClick = onBackClick
    ) {
        Button(onClick = onJoinClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.join_existing_group))
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_explore_without_group))
        }
    }
}

/** Estrutura comum das telas de roteamento por perfil (botão de voltar, título, texto de apoio e
 *  botões) — [onBackClick] permite reconsiderar a escolha de perfil feita na primeira tela,
 *  também acionável pelo botão/gesto de voltar do sistema (ver [androidx.activity.compose.BackHandler]
 *  no call site em `VoleiManagerApp`). */
@Composable
private fun ProfileRoutingScreenScaffold(
    titleRes: Int,
    hintRes: Int,
    onBackClick: () -> Unit,
    buttons: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = onBackClick, modifier = Modifier.padding(4.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(hintRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(32.dp))
                    buttons()
                }
            }
        }
    }
}
