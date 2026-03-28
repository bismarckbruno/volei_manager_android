import java.io.File

tasks.register("renamePackage") {
    doLast {
        val oldPackage = "com.bismarck.voleimanager"
        val newPackage = "com.bismarck.voleimanager"
        
        // 1. Text replacement
        file(".").walkTopDown().forEach { file ->
            val path = file.absolutePath.replace("\\\\", "/")
            if (file.isFile && !path.contains("/build/") && !path.contains("/.gradle/") && !path.contains("/.idea/") && !path.contains("/.git/")) {
                if (file.extension in listOf("kt", "kts", "xml", "pro")) {
                    val text = file.readText()
                    if (text.contains(oldPackage)) {
                        println("Renaming inside: ${file.name}")
                        file.writeText(text.replace(oldPackage, newPackage))
                    }
                }
            }
        }
        
        // 2. Move directories
        val srcDirs = listOf(
            "app/src/main/java/com/example/voleimanager",
            "app/src/androidTest/java/com/example/voleimanager",
            "app/src/test/java/com/example/voleimanager"
        )
        
        srcDirs.forEach { dirPath ->
            val dir = file(dirPath)
            if (dir.exists()) {
                val newDir = file(dirPath.replace("example", "bruno"))
                newDir.parentFile.mkdirs() // Creates com/bruno
                
                // Since renameTo can be tricky with non-empty dirs across file systems,
                // but here it's on the same drive and same parent logic.
                val success = dir.renameTo(newDir)
                println("Moved $dirPath to $newDir: $success")
                
                // Try to delete the old 'example' folder if empty
                val oldExample = file(dirPath.substringBeforeLast("/voleimanager"))
                if (oldExample.exists() && oldExample.listFiles()?.isEmpty() == true) {
                    oldExample.delete()
                }
            }
        }
    }
}
