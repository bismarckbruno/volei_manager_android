package com.bismarck.voleimanager.app.util

import android.util.Xml
import com.bismarck.voleimanager.app.platform.SpreadsheetCodec
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.xmlpull.v1.XmlPullParser

/** Leitura/escrita de .xlsx no Android (java.util.zip + XmlPullParser). */
class AndroidSpreadsheetCodec : SpreadsheetCodec {
    /**
     * Gera os bytes de um arquivo .xlsx mínimo e válido (zip + XML escrito manualmente, sem
     * biblioteca externa), com uma única planilha contendo [headers] na primeira linha seguida de
     * [rows]. Usa células do tipo `inlineStr` para texto, evitando a necessidade de um
     * `sharedStrings.xml` separado. Espelha o parsing já existente em [readXlsxRows]/[parseSheetRows].
     */
    override fun buildXlsx(headers: List<String>, rows: List<List<String>>): ByteArray {
        fun xmlEscape(s: String) = s.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;")

        fun columnName(index: Int): String {
            var i = index
            val sb = StringBuilder()
            do {
                sb.insert(0, ('A' + (i % 26)))
                i = i / 26 - 1
            } while (i >= 0)
            return sb.toString()
        }

        val allRows = listOf(headers) + rows
        val sheetXml = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
            append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
            append("<sheetData>")
            allRows.forEachIndexed { rowIndex, row ->
                append("<row r=\"${rowIndex + 1}\">")
                row.forEachIndexed { colIndex, cell ->
                    val ref = "${columnName(colIndex)}${rowIndex + 1}"
                    val numeric = cell.toDoubleOrNull()
                    if (numeric != null && cell.isNotBlank()) {
                        append("<c r=\"$ref\"><v>${xmlEscape(cell)}</v></c>")
                    } else {
                        append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xmlEscape(cell)}</t></is></c>")
                    }
                }
                append("</row>")
            }
            append("</sheetData>")
            append("</worksheet>")
        }

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            </Types>""".trimIndent()

        val rootRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
            </Relationships>""".trimIndent()

        val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
            <sheets><sheet name="Jogadores" sheetId="1" r:id="rId1"/></sheets>
            </workbook>""".trimIndent()

        val workbookRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
            </Relationships>""".trimIndent()

        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zip ->
            fun writeEntry(name: String, data: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            writeEntry("[Content_Types].xml", contentTypesXml)
            writeEntry("_rels/.rels", rootRelsXml)
            writeEntry("xl/workbook.xml", workbookXml)
            writeEntry("xl/_rels/workbook.xml.rels", workbookRelsXml)
            writeEntry("xl/worksheets/sheet1.xml", sheetXml)
        }
        return outputStream.toByteArray()
    }

    /** Extrai as linhas/colunas da primeira planilha de um arquivo .xlsx (formato zip + XML). */
    override fun readXlsxRows(bytes: ByteArray): List<List<String>> {
        var sharedStrings: List<String> = emptyList()
        val sheetEntries = mutableMapOf<String, ByteArray>()

        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "xl/sharedStrings.xml" -> sharedStrings = parseSharedStrings(zis.readBytes())
                    entry.name.matches(Regex("xl/worksheets/sheet\\d+\\.xml")) ->
                        sheetEntries[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val firstSheetName = sheetEntries.keys.minByOrNull {
            Regex("\\d+").find(it)?.value?.toIntOrNull() ?: Int.MAX_VALUE
        } ?: return emptyList()

        return parseSheetRows(sheetEntries.getValue(firstSheetName), sharedStrings)
    }

    private fun parseSharedStrings(data: ByteArray): List<String> {
        val strings = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        var eventType = parser.eventType
        var currentSi: StringBuilder? = null
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> currentSi = StringBuilder()
                    "t" -> currentSi?.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> if (parser.name == "si") {
                    strings.add(currentSi?.toString() ?: "")
                    currentSi = null
                }
            }
            eventType = try { parser.next() } catch (e: Exception) { XmlPullParser.END_DOCUMENT }
        }
        return strings
    }

    private fun parseSheetRows(data: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        var eventType = parser.eventType
        var currentRow: MutableList<String>? = null
        var currentColIndex = -1
        var currentCellType: String? = null
        var currentValue: StringBuilder? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        currentCellType = parser.getAttributeValue(null, "t")
                        val ref = parser.getAttributeValue(null, "r")
                        currentColIndex = ref?.let { columnLetterToIndex(it) } ?: (currentColIndex + 1)
                        currentValue = StringBuilder()
                    }
                    "v" -> currentValue?.append(parser.nextText())
                    "t" -> if (currentCellType == "inlineStr") currentValue?.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "c" -> {
                        val row = currentRow
                        if (row != null && currentColIndex >= 0) {
                            while (row.size <= currentColIndex) row.add("")
                            val raw = currentValue?.toString() ?: ""
                            row[currentColIndex] = when (currentCellType) {
                                "s" -> raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
                                "b" -> if (raw == "1") "true" else "false"
                                else -> raw
                            }
                        }
                        currentCellType = null
                        currentValue = null
                    }
                    "row" -> {
                        currentRow?.let { rows.add(it) }
                        currentRow = null
                        currentColIndex = -1
                    }
                }
            }
            eventType = try { parser.next() } catch (e: Exception) { XmlPullParser.END_DOCUMENT }
        }
        return rows
    }

    /** Converte uma referência de célula do Excel (ex.: "C7") no índice de coluna 0-based (2). */
    private fun columnLetterToIndex(cellRef: String): Int {
        var idx = 0
        for (c in cellRef) {
            if (c.isLetter()) {
                idx = idx * 26 + (c.uppercaseChar() - 'A' + 1)
            } else break
        }
        return idx - 1
    }

}
