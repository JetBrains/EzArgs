package com.jetbrains.rider.ezargs.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.ui.TextFieldWithAutoCompletion

val UnrealEngineArgs = mutableListOf(
    // General Options
    "-game",
    "-name",
    // Server Options
    "-listen",
    "-bIsLanMatch",
    "-bIsFromInvite",
    "-spectatoronly",
    // Switches
    // Developer
    "-ABSLOG",
    "-ALLUSERS",
    "-AUTO",
    "-AUTOCHECKOUTPACKAGES",
    "-AutomatedMapBuild",
    "-BIASCOMPRESSIONFORSIZE",
    "-BUILDMACHINE",
    "-BULKIMPORTINGSOUNDS",
    "-CHECK_NATIVE_CLASS_SIZES",
    "-CODERMODE",
    "-COMPATSCALE",
    "-CONFORMDIR",
    "-COOKFORDEMO",
    "-COOKPACKAGES",
    "-CRASHREPORTS",
    "-D3DDEBUG",
    "-DEVCON",
    "-DUMPFILEIOSTATS",
    "-FIXEDSEED",
    "-FIXUPTANGENTS",
    "-FORCELOGFLUSH",
    "-FORCEPVRTC",
    "-FORCESOUNDRECOOK",
    "-GENERICBROWSER",
    "-INSTALLED",
    "-INSTALLFW",
    "-INSTALLGE",
    "-CULTUREFORCOOKING",
    "-LIGHTMASSDEBUG",
    "-LIGHTMASSSTATS",
    "-LOG",
    "-LOGTIMES",
    "-NOCONFORM",
    "-NOCONTENTBROWSER",
    "-NOINNEREXCEPTION",
    "-NOLOADSTARTUPPACKAGES",
    "-NOLOGTIMES",
    "-NOPAUSE",
    "-NOPAUSEONSUCCESS",
    "-NORC",
    "-NOVERIFYGC",
    "-NOWRITE",
    "-SEEKFREELOADING",
    "-SEEKFREEPACKAGEMAP",
    "-SEEKFREELOADINGPCCONSOLE",
    "-SEEKFREELOADINGSERVER",
    "-SETTHREADNAMES",
    "-SHOWMISSINGLOC",
    "-SILENT",
    "-TRACEANIMUSAGE",
    "-TREATLOADWARNINGSASERRORS",
    "-UNATTENDED",
    "-UNINSTALLGE",
    "-USEUNPUBLISHED",
    "-VADEBUG",
    "-VERBOSE",
    "-VERIFYGC",
    "-WARNINGSASERRORS",
    // Rendering
    "-ConsoleX",
    "-ConsoleY",
    "-WinX",
    "-WinY",
    "-ResX",
    "-ResY",
    "-VSync",
    "-NoVSync",
    "-BENCHMARK",
    "-DUMPMOVIE",
    "-EXEC",
    "-FPS",
    "-FULLSCREEN",
    "-SECONDS",
    "-WINDOWED",
    // Network
    "-LANPLAY",
    "-Limitclientticks",
    "-MULTIHOME",
    "-NETWORKPROFILER",
    "-NOSTEAM",
    "-PORT",
    "-PRIMARYNET",
    // User
    "-NOHOMEDIR",
    "-NOFORCEFEEDBACK",
    "-NOSOUND",
    "-NOSPLASH",
    "-NOTEXTURESTREAMING",
    "-ONETHREAD",
    "-PATHS",
    "-PREFERREDPROCESSOR",
    "-USEALLAVAILABLECORES",
    // Debugging
    "-BugLoc",
    "-BugRot",
    // Misc
    "-timelimit",
    "-goalscore",
    "-numbots"
)

object CmdArgsCompletionProvider : TextFieldWithAutoCompletion.StringsCompletionProvider(UnrealEngineArgs, null) {
    override fun createPrefixMatcher(prefix: String): PrefixMatcher = CamelHumpMatcher(prefix)
}