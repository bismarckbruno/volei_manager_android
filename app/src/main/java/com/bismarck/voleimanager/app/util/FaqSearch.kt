package com.bismarck.voleimanager.app.util

import java.text.Normalizer

/**
 * Lightweight, fully local "semantic-ish" search used to filter/rank FAQ entries.
 *
 * There's no ML/embeddings involved: it normalizes text (removes accents, lowercases),
 * strips common stopwords in PT/EN/ES (the app's supported locales) and scores each
 * FAQ entry by how many of the query's remaining tokens appear (as a substring, so
 * plurals/variations still match) inside the entry's searchable text. Matches against
 * an entry's curated `keywords` are weighted higher than matches in the plain
 * question/answer/table text, so a well-chosen synonym list acts as the "semantic" layer.
 */
object FaqSearch {

    /** Extra weight given to a query token match found inside an entry's keyword list. */
    private const val KEYWORD_MATCH_WEIGHT = 3
    private const val TEXT_MATCH_WEIGHT = 1
    private const val MIN_TOKEN_LENGTH = 2
    /** Bonus for a verbatim (stopwords included) match of the whole query against the question. */
    private const val EXACT_QUESTION_BONUS = 100
    /** Bonus for a verbatim match found elsewhere in the entry (answer/table/keywords). */
    private const val EXACT_CORPUS_BONUS = 40

    private val STOPWORDS: Set<String> = setOf(
        // Português
        "a", "ao", "aos", "as", "com", "como", "da", "das", "de", "do", "dos", "e", "é",
        "em", "essa", "essas", "esse", "esses", "esta", "estas", "este", "estes", "eu",
        "faz", "fazer", "isso", "isto", "já", "la", "lhe", "lhes", "mais", "mas", "me",
        "mesmo", "meu", "meus", "minha", "minhas", "muito", "na", "nas", "no", "nos",
        "não", "nós", "o", "os", "ou", "para", "pelo", "pela", "pelos", "pelas", "por",
        "qual", "quais", "quando", "quanto", "quantos", "que", "quem", "se", "sem",
        "seu", "seus", "sua", "suas", "são", "tem", "têm", "um", "uma", "uns", "umas",
        "você", "voces", "vocês",
        // English
        "a", "am", "an", "and", "are", "as", "at", "be", "but", "by", "can", "do", "does",
        "for", "from", "how", "i", "in", "is", "it", "its", "of", "on", "or", "should",
        "so", "that", "the", "there", "these", "this", "those", "to", "was", "what",
        "when", "where", "which", "who", "why", "will", "with", "you", "your",
        // Español
        "al", "algo", "algún", "alguna", "cada", "como", "con", "cual", "cuales",
        "cuando", "cuanto", "de", "del", "el", "ella", "ellas", "ellos", "en", "es",
        "esa", "esas", "ese", "esos", "esta", "estas", "este", "estos", "hace", "hacer",
        "la", "las", "lo", "los", "más", "mi", "mis", "mucho", "muy", "no", "nos",
        "para", "pero", "por", "porque", "qué", "quien", "quienes", "se", "si", "sin",
        "su", "sus", "también", "tu", "tus", "un", "una", "uno", "unos", "y", "ya"
    )

    /** Removes accents/diacritics and lowercases, so "é"/"e" and "Á"/"a" match the same. */
    fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{Mn}+"), "")
    }

    /** Splits into meaningful tokens: normalized, non-stopword, at least [MIN_TOKEN_LENGTH] chars. */
    fun tokenize(text: String): List<String> =
        normalize(text)
            .split(Regex("[^\\p{L}\\p{Nd}]+"))
            .filter { it.length >= MIN_TOKEN_LENGTH && it !in STOPWORDS }

    /**
     * Scores how relevant [questionText] (plus [corpusText]/[keywordsText]) is for [query].
     * Returns 0 when the query is blank/has no meaningful (non-stopword) tokens, or when
     * nothing matches — callers should treat a purely-stopword query (e.g. "o que") as
     * "no active search" and skip filtering entirely, rather than call this (see [tokenize]).
     *
     * On top of the token-overlap score, a verbatim match of the *whole* query (kept as
     * typed, stopwords included) against [questionText] gets a strong bonus, so a search
     * like "como funciona" ranks a question that literally starts with it first; a verbatim
     * match found elsewhere in the entry gets a smaller bonus. The token-based matching below
     * (which uses `contains`, not equality) still covers close variations on its own, e.g. a
     * query token "funciona" also matches a question containing "funcionam".
     */
    fun score(query: String, questionText: String, corpusText: String, keywordsText: String): Int {
        val queryTokens = tokenize(query).distinct()
        if (queryTokens.isEmpty()) return 0

        val normalizedQuery = normalize(query).trim()
        val normalizedQuestion = normalize(questionText)
        val normalizedCorpus = normalize(corpusText)
        val normalizedKeywords = normalize(keywordsText)

        var total = 0
        if (normalizedQuery.isNotEmpty()) {
            if (normalizedQuestion.contains(normalizedQuery)) {
                total += EXACT_QUESTION_BONUS
            } else if (normalizedCorpus.contains(normalizedQuery)) {
                total += EXACT_CORPUS_BONUS
            }
        }

        for (token in queryTokens) {
            if (normalizedKeywords.contains(token)) total += KEYWORD_MATCH_WEIGHT
            else if (normalizedCorpus.contains(token)) total += TEXT_MATCH_WEIGHT
        }
        return total
    }
}
