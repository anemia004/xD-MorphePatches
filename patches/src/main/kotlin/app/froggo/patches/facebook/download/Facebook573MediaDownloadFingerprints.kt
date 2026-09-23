package app.froggo.patches.facebook.download

import app.morphe.patcher.Fingerprint

internal val menuCallback = Fingerprint(
    returnType = "V",
    parameters = listOf("LX/VyQ;"),
    custom = { method, classDef ->
        classDef.type == "LX/WKI;" && method.name == "Dtf"
    },
)

internal val videoSaveCallback = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    custom = { method, classDef ->
        classDef.type == "LX/bq4;" && method.name == "onClick"
    },
)

internal val storyHeader = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/3QZ;"),
    custom = { method, classDef ->
        classDef.type == "LX/9Uw;" && method.name == "A1K"
    },
)

internal val storyHeaderCallback = Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("LX/X6V;", "Ljava/lang/Object;"),
    custom = { method, classDef ->
        classDef.type == "LX/9Uw;" && method.name == "A1O"
    },
)

internal val storyAlternateHeader = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/24H;"),
    custom = { method, classDef ->
        classDef.type == "LX/P0G;" && method.name == "render"
    },
)

internal val fullscreenStoryTopbar = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/24H;"),
    custom = { method, classDef ->
        classDef.type == "LX/9W5;" && method.name == "render"
    },
)

internal val reelSidebar = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/3QZ;"),
    custom = { method, classDef ->
        classDef.type == "LX/9vm;" && method.name == "A1K"
    },
)
