package com.example.jetskeyboard

enum class KeyboardMode {
    ARABIC,
    ENGLISH_SMALL,
    ENGLISH_CAPS,
    SYMBOLS
}

enum class KeyAction {
    CHARACTER,
    SPACE,
    BACKSPACE,
    ENTER,
    SHIFT,
    SYMBOLS,
    LETTERS,
    LANGUAGE,
    PERIOD,
    COMMA
}

data class KeySpec(
    val label: String,
    val action: KeyAction = KeyAction.CHARACTER,
    val alternates: List<String> = emptyList(),
    val subLabel: String? = null,
    val weight: Float = 1f
)

object LayoutData {
    private fun key(
        label: String,
        alternates: List<String> = emptyList(),
        subLabel: String? = null
    ) = KeySpec(label, KeyAction.CHARACTER, alternates, subLabel)

    private fun row(vararg keys: KeySpec) = keys.toList()

    fun rows(mode: KeyboardMode): List<List<KeySpec>> = when (mode) {
        KeyboardMode.ARABIC -> listOf(
            row(key("ض"), key("ص"), key("ث"), key("ق"), key("ف"), key("غ"), key("ع"), key("ه"), key("خ"), key("ح"), key("ج")),
            row(key("ش"), key("س"), key("ي"), key("ب"), key("ل"), key("ا", listOf("أ", "إ", "آ", "ٱ")), key("ت"), key("ن"), key("م"), key("ك"), key("ط")),
            row(KeySpec("⇧", KeyAction.SHIFT, weight = 1.35f), key("ئ"), key("ء"), key("ؤ"), key("ر"), key("لا"), key("ى"), key("ة"), key("و"), key("ز"), key("ظ"), KeySpec("⌫", KeyAction.BACKSPACE, weight = 1.35f)),
            bottomRow()
        )
        KeyboardMode.ENGLISH_SMALL -> listOf(
            row(key("q", listOf("1", "@")), key("w", listOf("2")), key("e", listOf("3", "é", "è", "ê")), key("r", listOf("4")), key("t", listOf("5")), key("y", listOf("6")), key("u", listOf("7", "ü")), key("i", listOf("8", "í")), key("o", listOf("9", "ó")), key("p", listOf("0"))),
            row(key("a"), key("s"), key("d"), key("f"), key("g"), key("h"), key("j"), key("k"), key("l")),
            row(KeySpec("⇧", KeyAction.SHIFT, weight = 1.35f), key("z"), key("x"), key("c"), key("v"), key("b"), key("n"), key("m"), KeySpec("⌫", KeyAction.BACKSPACE, weight = 1.35f)),
            bottomRow()
        )
        KeyboardMode.ENGLISH_CAPS -> listOf(
            row(key("Q"), key("W"), key("E"), key("R"), key("T"), key("Y"), key("U"), key("I"), key("O"), key("P")),
            row(key("A"), key("S"), key("D"), key("F"), key("G"), key("H"), key("J"), key("K"), key("L")),
            row(KeySpec("⇧", KeyAction.SHIFT, weight = 1.35f), key("Z"), key("X"), key("C"), key("V"), key("B"), key("N"), key("M"), KeySpec("⌫", KeyAction.BACKSPACE, weight = 1.35f)),
            bottomRow()
        )
        KeyboardMode.SYMBOLS -> listOf(
            row(key("1"), key("2"), key("3"), key("4"), key("5"), key("6"), key("7"), key("8"), key("9"), key("0")),
            row(key("@"), key("#"), key("$"), key("%"), key("&"), key("*"), key("-"), key("+"), key("("), key(")")),
            row(key("١"), key("٢"), key("٣"), key("٤"), key("٥"), key("٦"), key("٧"), key("٨"), key("٩"), key("٠")),
            row(KeySpec("=\\<", KeyAction.LETTERS, weight = 1.3f), key("،"), key("؟", listOf("!", "؛", ":")), key("."), key(","), key("/"), KeySpec("⌫", KeyAction.BACKSPACE, weight = 1.3f)),
            bottomRow()
        )
    }

    private fun bottomRow(): List<KeySpec> = row(
        KeySpec("?123", KeyAction.SYMBOLS, weight = 1.35f),
        KeySpec("🌐", KeyAction.LANGUAGE, weight = 1.1f),
        KeySpec(",", KeyAction.COMMA, weight = 0.9f),
        KeySpec("English", KeyAction.SPACE, subLabel = "اسحب لتحريك المؤشر", weight = 4.8f),
        KeySpec(".", KeyAction.PERIOD, weight = 0.9f),
        KeySpec("↵", KeyAction.ENTER, weight = 1.35f)
    )
}
