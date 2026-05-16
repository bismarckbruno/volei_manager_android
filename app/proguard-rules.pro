# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preservar anotações e nomes necessários para Gson e Room
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod

# Manter as classes de dados da ViewModel (para não quebrar Snapshot e Backup do Gson)
-keep class com.bismarck.voleimanager.app.ui.viewmodel.GameStateSnapshot { *; }
-keep class com.bismarck.voleimanager.app.ui.viewmodel.BackupData { *; }

# Manter todas as entidades do banco de dados (Room + Gson)
-keep class com.bismarck.voleimanager.app.data.model.** { *; }

# (Opcional, mas recomendado) Manter enumerações intactas se usadas no Gson
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}