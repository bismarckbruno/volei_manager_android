package com.bismarck.voleimanager.app.util

/** Limites e regras de validação para os campos de cadastro/login (Firebase Auth), evitando
 *  textos absurdamente longos e ajudando a barrar tentativas simples de injeção — o Firebase Auth
 *  já trata os valores como texto opaco (não há risco de SQL injection), mas limitar o tamanho
 *  ainda protege o Firestore (documento de perfil) de payloads enormes. */
const val MAX_EMAIL_LENGTH = 254
const val MIN_PASSWORD_LENGTH = 8
const val MAX_PASSWORD_LENGTH = 16
const val MAX_FULL_NAME_LENGTH = 60

private val EMAIL_REGEX = Regex(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
)

/** Exige ao menos uma maiúscula, uma minúscula, um dígito e um caractere especial, dentro do
 *  intervalo de tamanho [MIN_PASSWORD_LENGTH]..[MAX_PASSWORD_LENGTH] (checado à parte, já que o
 *  campo de texto trunca em [MAX_PASSWORD_LENGTH] antes mesmo de chegar aqui). */
private val PASSWORD_UPPERCASE_REGEX = Regex(".*[A-Z].*")
private val PASSWORD_LOWERCASE_REGEX = Regex(".*[a-z].*")
private val PASSWORD_DIGIT_REGEX = Regex(".*[0-9].*")
private val PASSWORD_SPECIAL_REGEX = Regex(".*[^A-Za-z0-9].*")

fun isValidEmail(email: String): Boolean =
    email.length <= MAX_EMAIL_LENGTH && EMAIL_REGEX.matches(email.trim())

fun isValidPassword(password: String): Boolean =
    password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH &&
        PASSWORD_UPPERCASE_REGEX.matches(password) &&
        PASSWORD_LOWERCASE_REGEX.matches(password) &&
        PASSWORD_DIGIT_REGEX.matches(password) &&
        PASSWORD_SPECIAL_REGEX.matches(password)
