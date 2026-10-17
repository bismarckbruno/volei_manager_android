package com.bismarck.voleimanager.app.platform

/** Referência opaca a um arquivo escolhido pelo usuário (no Android, o texto de uma `content://` Uri). */
data class FileRef(val uri: String)

/** Leitura e compartilhamento de arquivos (backup `.vlz`, CSV/XLSX, imagem do histórico). */
interface FileService {
    /** Lê todo o conteúdo de [file]; `null` se não for possível abrir. */
    fun readBytes(file: FileRef): ByteArray?

    /**
     * Abre o seletor de compartilhamento do sistema com [bytes] salvos como [fileName].
     * Lança exceção se não for possível preparar/compartilhar o arquivo.
     */
    fun shareFile(fileName: String, bytes: ByteArray, mimeType: String, chooserTitle: String)
}
