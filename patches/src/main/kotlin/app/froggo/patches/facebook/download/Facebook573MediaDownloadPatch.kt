package app.froggo.patches.facebook.download

import app.froggo.patches.shared.Constants.COMPATIBILITY_FACEBOOK_573
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable as toMutableField
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

private val menuCallback = Fingerprint(
    returnType = "V",
    parameters = listOf("LX/VyQ;"),
    custom = { method, classDef ->
        classDef.type == "LX/WKI;" && method.name == "Dtf"
    },
)

private val videoSaveCallback = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    custom = { method, classDef ->
        classDef.type == "LX/bq4;" && method.name == "onClick"
    },
)

private val storyHeader = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/3QZ;"),
    custom = { method, classDef ->
        classDef.type == "LX/9Uw;" && method.name == "A1K"
    },
)

private val storyHeaderCallback = Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("LX/X6V;", "Ljava/lang/Object;"),
    custom = { method, classDef ->
        classDef.type == "LX/9Uw;" && method.name == "A1O"
    },
)

private val storyAlternateHeader = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/24H;"),
    custom = { method, classDef ->
        classDef.type == "LX/P0G;" && method.name == "render"
    },
)

private val fullscreenStoryTopbar = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/24H;"),
    custom = { method, classDef ->
        classDef.type == "LX/9W5;" && method.name == "render"
    },
)

private val reelSidebar = Fingerprint(
    returnType = "LX/3Pu;",
    parameters = listOf("LX/3QZ;"),
    custom = { method, classDef ->
        classDef.type == "LX/9vm;" && method.name == "A1K"
    },
)

@Suppress("unused")
val downloadFacebookMedia573Patch = bytecodePatch(
    name = "Download Facebook Media (573)",
    description = "Adds direct downloads for the visible Story, Reel, and video media through MediaStore and DownloadManager.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FACEBOOK_573)

    val imageFolderOption = stringOption(
        key = "facebookImageFolder",
        default = "Pictures/FroggoPatches/Facebook/@",
        values = mapOf("Pictures/FroggoPatches/Facebook/@" to "Pictures/FroggoPatches/Facebook/@"),
        title = "Facebook image folder",
        description = "Relative MediaStore folder prefix. The creator name is appended after @.",
        required = true,
    ) { it != null && it.startsWith("Pictures/") && it.endsWith("@") && ".." !in it }

    val videoFolderOption = stringOption(
        key = "facebookVideoFolder",
        default = "FroggoPatches/Facebook/@",
        values = mapOf("Movies/FroggoPatches/Facebook/@" to "FroggoPatches/Facebook/@"),
        title = "Facebook video folder",
        description = "Folder below Movies. The creator name is appended after @.",
        required = true,
    ) { it != null && !it.startsWith("/") && it.endsWith("@") && ".." !in it }

    execute {
        val imagePathPrefix = imageFolderOption.value!!
        val videoPathPrefix = "Movies/${videoFolderOption.value!!}"
        val storyDirectActionHash = -2013570421

        val storyDirectButtonHelper = ImmutableMethod(
            storyHeader.classDef.type,
            "froggoCreateStoryDownloadButton",
            listOf(
                ImmutableMethodParameter("LX/3QZ;", null, null),
                ImmutableMethodParameter("Z", null, null),
            ),
            "LX/4hG;",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    invoke-static {p0}, LX/4hG;->A00(LX/3QZ;)LX/4hH;
                    move-result-object v0
                    const-string v6, "FroggoPatches"
                    const-string v7, "story-button-built"
                    invoke-static {v6, v7}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                    const/high16 v1, 0x41c00000
                    invoke-virtual {v0, v1}, LX/4hH;->A1j(F)V
                    const/high16 v1, 0x41c00000
                    invoke-virtual {v0, v1}, LX/4hH;->A1X(F)V
                    const/4 v1, 0x0
                    invoke-virtual {v0, v1}, LX/4hH;->A1W(F)V
                    invoke-static {v0}, LX/9Di;->A1X(LX/Nqn;)V
                    sget-object v1, Lcom/facebook/fds/core/theme/component/FDSColors;->A00:Lcom/facebook/fds/core/theme/component/FDSColors;
                    if-eqz p1, :froggo_story_download_button_light
                    sget-object v2, LX/1y5;->A09:LX/1y5;
                    goto :froggo_story_download_button_color_ready
                    :froggo_story_download_button_light
                    sget-object v2, LX/1y5;->A4C:LX/1y5;
                    :froggo_story_download_button_color_ready
                    invoke-virtual {v1, v2, p0}, Lcom/facebook/fds/core/theme/component/FDSColors;->A02(LX/1y5;LX/3QZ;)I
                    move-result v1
                    invoke-virtual {v0, v1}, LX/4hH;->A32(I)V
                    sget v1, Lcom/facebook/katana/R${'$'}drawable${'$'}3;->fb_ic_download_24:I
                    invoke-virtual {v0, v1}, LX/4hH;->A35(I)V
                    move-object v6, v0
                    move-object v0, p0
                    sget-object v1, LX/1K7;->A03:LX/1K7;
                    const-class v2, ${storyHeader.classDef.type}
                    const-string v3, "ExpandedMediaFeedHeaderComponent"
                    const/4 v4, 0x0
                    const v5, $storyDirectActionHash
                    invoke-virtual/range {v0 .. v5}, LX/3QZ;->A07(LX/1K7;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Object;I)LX/X6V;
                    move-result-object v1
                    move-object v0, v6
                    invoke-virtual {v0, v1}, LX/4hH;->A2C(LX/X6V;)V
                    sget-object v1, LX/2PU;->A04:LX/2PU;
                    invoke-virtual {v0, v1}, LX/Nqn;->A2T(LX/2PU;)V
                    invoke-virtual {v0}, LX/Nqn;->A1O()V
                    iget-object v0, v0, LX/4hH;->A00:LX/4hG;
                    return-object v0
                """.trimIndent(),
            )
        }
        storyHeader.classDef.methods.add(storyDirectButtonHelper)

        storyHeaderCallback.method.addInstructions(
            0,
            """
                move-object/from16 v4, p1
                move-object/from16 v5, p2
                iget v0, v4, LX/X6V;->A01:I
                const v1, $storyDirectActionHash
                if-ne v0, v1, :froggo_story_header_stock_callback
                instance-of v1, v5, LX/1MP;
                if-eqz v1, :froggo_story_header_touch_done
                check-cast v5, LX/1MP;
                invoke-virtual {v5}, LX/1MP;->A00()Landroid/view/View;
                move-result-object v5
                :froggo_story_header_start
                const-string v2, "FroggoPatches"
                const-string v3, "story-action-callback"
                invoke-static {v2, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                iget-object v0, v4, LX/X6V;->A00:LX/1K9;
                iget-object v0, v0, LX/1K9;->A00:LX/3QZ;
                invoke-static {v0}, LX/41t;->A0O(LX/3QZ;)Ljava/lang/Object;
                move-result-object v0
                check-cast v0, LX/9VC;
                iget-object v0, v0, LX/9VC;->A00:Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;
                invoke-static {v0, v5}, LX/WKI;->froggoChooseStoryDownload(Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;Landroid/view/View;)V
                const/4 v0, 0x0
                return-object v0
                :froggo_story_header_touch_done
                sget-object v0, LX/0FI;->A00:LX/0FI;
                return-object v0
                :froggo_story_header_stock_callback
            """.trimIndent(),
        )

        val storyHeaderInstructions = storyHeader.method.implementation!!.instructions
        val storyDotsCalls = storyHeaderInstructions.withIndex().mapNotNull { (index, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            if (
                reference?.definingClass == storyHeader.classDef.type &&
                    reference.name == "A00" &&
                    reference.parameterTypes == listOf("LX/3QZ;", "Z")
            ) {
                index
            } else {
                null
            }
        }
        val storyDotsCallSites = storyDotsCalls.mapNotNull { callIndex ->
            val dotsResultRegister =
                (storyHeaderInstructions.getOrNull(callIndex + 1) as? OneRegisterInstruction)?.registerA
                    ?: return@mapNotNull null
            fun isDotsCollectionAdd(index: Int): Boolean {
                val instruction = storyHeaderInstructions[index]
                val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                return reference?.definingClass == "Ljava/util/AbstractCollection;" &&
                    reference.name == "add" &&
                    reference.parameterTypes == listOf("Ljava/lang/Object;") &&
                    (instruction as? FiveRegisterInstruction)?.registerCount == 2 &&
                    (instruction as FiveRegisterInstruction).registerD == dotsResultRegister
            }
            val addIndex =
                (callIndex + 1 until minOf(callIndex + 96, storyHeaderInstructions.size))
                    .firstOrNull(::isDotsCollectionAdd)
                    ?: (0 until callIndex).reversed().firstOrNull(::isDotsCollectionAdd)
            if (addIndex == null) null else callIndex to addIndex
        }
        require(storyDotsCallSites.size == 2) {
            "Expected both Story header dots layouts to add their button to a collection"
        }
        val directButtonReference = ImmutableMethodReference(
            storyHeader.classDef.type,
            "froggoCreateStoryDownloadButton",
            listOf("LX/3QZ;", "Z"),
            "LX/4hG;",
        )
        storyDotsCallSites.asReversed().forEach { (storyDotsCall, dotsAddIndex) ->
            val dotsCallRegisters = storyHeaderInstructions[storyDotsCall] as FiveRegisterInstruction
            val dotsResultRegister =
                (storyHeaderInstructions[storyDotsCall + 1] as OneRegisterInstruction).registerA
            val dotsAddRegisters = storyHeaderInstructions[dotsAddIndex] as FiveRegisterInstruction
            val storyHeaderAddReference =
                (storyHeaderInstructions[dotsAddIndex] as ReferenceInstruction).reference
            storyHeader.method.addInstructions(
                dotsAddIndex + 1,
                listOf(
                    BuilderInstruction35c(
                        Opcode.INVOKE_STATIC,
                        dotsCallRegisters.registerCount,
                        dotsCallRegisters.registerC,
                        dotsCallRegisters.registerD,
                        0,
                        0,
                        0,
                        directButtonReference,
                    ),
                    BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, dotsResultRegister),
                    BuilderInstruction35c(
                        Opcode.INVOKE_VIRTUAL,
                        2,
                        dotsAddRegisters.registerC,
                        dotsResultRegister,
                        0,
                        0,
                        0,
                        storyHeaderAddReference,
                    ),
                ),
            )
        }

        val storyAlternateHeaderInstructions = storyAlternateHeader.method.implementation!!.instructions
        val storyAlternateHeaderAnchors = storyAlternateHeaderInstructions.withIndex().mapNotNull { (index, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            if (
                reference?.definingClass == "LX/Nqo;" &&
                    reference.name == "A0x"
            ) {
                index
            } else {
                null
            }
        }
        require(storyAlternateHeaderAnchors.size == 1) {
            "Expected one alternate Story header child collection anchor"
        }
        storyAlternateHeader.method.addInstructions(
            storyAlternateHeaderAnchors.single(),
            """
                invoke-static {v6}, LX/4hG;->A00(LX/3QZ;)LX/4hH;
                move-result-object v0
                const/high16 v1, 0x41c00000
                invoke-virtual {v0, v1}, LX/4hH;->A1j(F)V
                const/high16 v1, 0x41c00000
                invoke-virtual {v0, v1}, LX/4hH;->A1X(F)V
                const/4 v1, 0x0
                invoke-virtual {v0, v1}, LX/4hH;->A1W(F)V
                invoke-static {v0}, LX/9Di;->A1X(LX/Nqn;)V
                sget-object v1, LX/1y5;->A4C:LX/1y5;
                sget-object v2, Lcom/facebook/fds/core/theme/component/FDSColors;->A00:Lcom/facebook/fds/core/theme/component/FDSColors;
                invoke-static {v0, v1, v2, v6}, LX/HrH;->A1D(LX/4hH;LX/1y5;Lcom/facebook/fds/core/theme/component/FDSColors;LX/3QZ;)V
                sget v1, Lcom/facebook/katana/R${'$'}drawable${'$'}3;->fb_ic_download_24:I
                invoke-virtual {v0, v1}, LX/4hH;->A35(I)V
                invoke-static {v7}, LX/CQu;->A10(Ljava/lang/Object;)Ljava/lang/Object;
                move-result-object v1
                new-instance v2, LX/WKI;
                const/16 v3, 0x7f
                move-object v9, v1
                move-object v10, v1
                move-object v11, v1
                invoke-direct {v2, v3, v9, v10, v11}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                new-instance v1, LX/3S5;
                const/4 v3, 0x0
                invoke-direct {v1, v3, v2}, LX/3S5;-><init>(LX/3QZ;Lkotlin/jvm/functions/Function1;)V
                invoke-virtual {v0, v1}, LX/4hH;->A2C(LX/X6V;)V
                invoke-virtual {v0}, LX/Nqn;->A1O()V
                iget-object v0, v0, LX/4hH;->A00:LX/4hG;
                invoke-virtual {v4, v0}, Ljava/util/AbstractCollection;->add(Ljava/lang/Object;)Z
            """.trimIndent(),
        )

        val fullscreenStoryDownloadHelper = ImmutableMethod(
            fullscreenStoryTopbar.classDef.type,
            "froggoCreateFullscreenStoryDownloadButton",
            listOf(
                ImmutableMethodParameter(fullscreenStoryTopbar.classDef.type, null, null),
                ImmutableMethodParameter("LX/3QZ;", null, null),
            ),
            "LX/AnR;",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    invoke-static {p1}, LX/9Di;->A0o(LX/3QZ;)Ljava/lang/Object;
                    move-result-object v0
                    check-cast v0, LX/BsZ;
                    invoke-static {v0}, LX/BsZ;->A00(LX/BsZ;)Lcom/facebook/auth/usersession/FbUserSession;
                    move-result-object v1
                    invoke-virtual {p1}, LX/3QZ;->A01()Landroid/content/Context;
                    move-result-object v2
                    move-object/from16 v3, p0
                    iget-object v3, v3, LX/9W5;->A04:Lcom/facebook/stories/model/StoryCard;

                    new-instance v4, LX/WKI;
                    const/16 v5, 0x83
                    move-object v6, v2
                    move-object v7, v3
                    const/4 v8, 0x0
                    invoke-direct {v4, v5, v6, v7, v8}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V

                    invoke-static {v4}, LX/3S5;->A00(Lkotlin/jvm/functions/Function1;)LX/3S5;
                    move-result-object v5

                    sget-object v2, LX/1Vq;->A80:LX/1Vq;
                    sget-object v3, LX/1c6;->A02:LX/1c6;
                    move-object v4, v5
                    const-string v5, "Descargar"
                    const-string v6, "storyviewer_download_button"
                    new-instance v0, LX/AnR;
                    invoke-direct/range {v0 .. v6}, LX/AnR;-><init>(Lcom/facebook/auth/usersession/FbUserSession;LX/1Vq;LX/1c6;LX/X6V;Ljava/lang/String;Ljava/lang/String;)V
                    return-object v0
                """.trimIndent(),
            )
        }
        fullscreenStoryTopbar.classDef.methods.add(fullscreenStoryDownloadHelper)

        val fullscreenStoryTopbarInstructions = fullscreenStoryTopbar.method.implementation!!.instructions
        val storyTrayConstructorCalls = fullscreenStoryTopbarInstructions.withIndex().mapNotNull { (index, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            if (
                reference?.definingClass == "LX/An8;" &&
                    reference.name == "<init>" &&
                    reference.parameterTypes == listOf(
                        "Lcom/facebook/auth/usersession/FbUserSession;",
                        "LX/FlR;",
                        "Lcom/facebook/stories/model/StoryBucket;",
                        "LX/BsZ;",
                    )
            ) {
                index
            } else {
                null
            }
        }
        require(storyTrayConstructorCalls.size == 1) {
            "Expected one fullscreen Story tray entrypoint component"
        }
        val storyTrayConstructorIndex = storyTrayConstructorCalls.single()
        val storyTrayConstructorRegisters = fullscreenStoryTopbarInstructions[storyTrayConstructorIndex] as FiveRegisterInstruction
        val storyTrayRegister = storyTrayConstructorRegisters.registerC
        val storyTrayAddIndex =
            (storyTrayConstructorIndex + 1 until minOf(storyTrayConstructorIndex + 5, fullscreenStoryTopbarInstructions.size))
                .firstOrNull { index ->
                    val instruction = fullscreenStoryTopbarInstructions[index]
                    val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                    reference?.definingClass == "Ljava/util/AbstractCollection;" &&
                        reference.name == "add" &&
                        reference.parameterTypes == listOf("Ljava/lang/Object;") &&
                        (instruction as? FiveRegisterInstruction)?.registerCount == 2 &&
                        (instruction as FiveRegisterInstruction).registerD == storyTrayRegister
                }
        requireNotNull(storyTrayAddIndex) {
            "Expected the fullscreen Story tray entrypoint to be added to its topbar collection"
        }
        fullscreenStoryTopbar.method.addInstructions(
            storyTrayAddIndex + 1,
            """
                move-object/from16 v66, p0
                move-object/from16 v67, v44
                invoke-static/range {v66 .. v67}, LX/9W5;->froggoCreateFullscreenStoryDownloadButton(LX/9W5;LX/3QZ;)LX/AnR;
                move-result-object v8
                invoke-virtual {v9, v8}, Ljava/util/AbstractCollection;->add(Ljava/lang/Object;)Z
            """.trimIndent(),
        )

        menuCallback.method.addInstructions(
            0,
            """
                iget v0, p0, LX/WKI;->${'$'}t:I
                const/16 v1, 0x7f
                if-ne v0, v1, :froggo_story_download_stock_callback
                new-instance v0, Ljava/lang/Thread;
                invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                return-void
                :froggo_story_download_stock_callback
            """.trimIndent(),
        )

        val callbackClass = menuCallback.classDef
        callbackClass.interfaces.removeAll { it == "Ljava/lang/Runnable;" }
        callbackClass.interfaces.add("Ljava/lang/Runnable;")
        callbackClass.interfaces.removeAll { it == "Lkotlin/jvm/functions/Function1;" }
        callbackClass.interfaces.add("Lkotlin/jvm/functions/Function1;")
        callbackClass.interfaces.removeAll { it == "Landroid/content/DialogInterface${'$'}OnClickListener;" }
        callbackClass.interfaces.add("Landroid/content/DialogInterface${'$'}OnClickListener;")

        callbackClass.fields.add(
            ImmutableField(
                callbackClass.type,
                "froggoDownloadButton",
                "Landroid/view/View;",
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                null,
                emptySet(),
                emptySet(),
            ).toMutableField(),
        )
        callbackClass.fields.add(
            ImmutableField(
                callbackClass.type,
                "froggoDownloadButtonTint",
                "Landroid/content/res/ColorStateList;",
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                null,
                emptySet(),
                emptySet(),
            ).toMutableField(),
        )
        callbackClass.fields.add(
            ImmutableField(
                callbackClass.type,
                "froggoDownloadSpinner",
                "Landroid/widget/ProgressBar;",
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                null,
                emptySet(),
                emptySet(),
            ).toMutableField(),
        )

        val toastHelper = ImmutableMethod(
            callbackClass.type,
            "froggoPostToast",
            listOf(
                ImmutableMethodParameter("Landroid/content/Context;", null, null),
                ImmutableMethodParameter("Ljava/lang/CharSequence;", null, null),
            ),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(8),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    new-instance v0, Landroid/os/Handler;
                    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
                    move-result-object v1
                    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
                    new-instance v1, LX/WKI;
                    const/16 v2, 0x82
                    move-object v3, p0
                    move-object v4, p1
                    const/4 v5, 0x0
                    invoke-direct {v1, v2, v3, v4, v5}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(toastHelper)

        val localizedSuccessToastHelper = ImmutableMethod(
            callbackClass.type,
            "froggoPostLocalizedDownloadSuccess",
            listOf(
                ImmutableMethodParameter("Landroid/content/Context;", null, null),
                ImmutableMethodParameter("Z", null, null),
            ),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(6),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    invoke-static {}, Ljava/util/Locale;->getDefault()Ljava/util/Locale;
                    move-result-object v0
                    invoke-virtual {v0}, Ljava/util/Locale;->getLanguage()Ljava/lang/String;
                    move-result-object v0
                    const-string v1, "es"
                    invoke-virtual {v1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :froggo_success_english
                    if-eqz p1, :froggo_success_spanish_story
                    const-string v1, "Reel descargado"
                    goto :froggo_success_ready
                    :froggo_success_spanish_story
                    const-string v1, "Historia descargada"
                    goto :froggo_success_ready
                    :froggo_success_english
                    if-eqz p1, :froggo_success_english_story
                    const-string v1, "Reel downloaded"
                    goto :froggo_success_ready
                    :froggo_success_english_story
                    const-string v1, "Story downloaded"
                    :froggo_success_ready
                    invoke-static {p0, v1}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(localizedSuccessToastHelper)

        val captureDownloadButtonHelper = ImmutableMethod(
            callbackClass.type,
            "froggoCaptureDownloadButton",
            listOf(ImmutableMethodParameter("Ljava/lang/Object;", null, null)),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(8),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    :froggo_capture_button_try
                    move-object v0, p0
                    instance-of v1, v0, Landroid/view/View;
                    if-nez v1, :froggo_capture_button_direct
                    instance-of v1, v0, LX/1MP;
                    if-nez v1, :froggo_capture_button_click
                    instance-of v1, v0, LX/43B;
                    if-nez v1, :froggo_capture_button_pressed
                    instance-of v1, v0, LX/1LV;
                    if-nez v1, :froggo_capture_button_touch
                    goto :froggo_capture_button_done

                    :froggo_capture_button_direct
                    check-cast v0, Landroid/view/View;
                    goto :froggo_capture_button_store

                    :froggo_capture_button_click
                    check-cast v0, LX/1MP;
                    invoke-virtual {v0}, LX/1MP;->A00()Landroid/view/View;
                    move-result-object v0
                    goto :froggo_capture_button_store

                    :froggo_capture_button_pressed
                    check-cast v0, LX/43B;
                    invoke-virtual {v0}, LX/43B;->A00()Landroid/view/View;
                    move-result-object v0
                    goto :froggo_capture_button_store

                    :froggo_capture_button_touch
                    check-cast v0, LX/1LV;
                    invoke-virtual {v0}, LX/1LV;->A01()Landroid/view/View;
                    move-result-object v0

                    :froggo_capture_button_store
                    if-eqz v0, :froggo_capture_button_done
                    sput-object v0, LX/WKI;->froggoDownloadButton:Landroid/view/View;
                    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                    move-result-object v1
                    invoke-virtual {v1}, Ljava/lang/Class;->getName()Ljava/lang/String;
                    move-result-object v2
                    const-string v1, "FroggoButtonView"
                    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                    const-string v1, "FroggoPatches"
                    const-string v2, "feedback-button-captured"
                    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

                    :froggo_capture_button_done
                    return-void
                    .catch Ljava/lang/Throwable; {:froggo_capture_button_try .. :froggo_capture_button_done} :froggo_capture_button_catch
                    :froggo_capture_button_catch
                    move-exception v0
                    const-string v1, "FroggoPatches"
                    const-string v2, "feedback-capture-exception"
                    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(captureDownloadButtonHelper)

        val animationStartHelper = ImmutableMethod(
            callbackClass.type,
            "froggoShowDownloadFeedbackStart",
            listOf(ImmutableMethodParameter("Ljava/lang/Object;", null, null)),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(10),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    :froggo_feedback_start_try
                    invoke-static {p0}, LX/WKI;->froggoCaptureDownloadButton(Ljava/lang/Object;)V
                    sget-object v0, LX/WKI;->froggoDownloadButton:Landroid/view/View;
                    if-eqz v0, :froggo_feedback_start_no_view
                    sget-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    if-eqz v1, :froggo_feedback_start_spinner_cleared
                    instance-of v2, v0, Landroid/view/ViewGroup;
                    if-eqz v2, :froggo_feedback_start_spinner_cleared
                    check-cast v0, Landroid/view/ViewGroup;
                    invoke-virtual {v0}, Landroid/view/ViewGroup;->getOverlay()Landroid/view/ViewGroupOverlay;
                    move-result-object v2
                    invoke-virtual {v2, v1}, Landroid/view/ViewGroupOverlay;->remove(Landroid/view/View;)V
                    :froggo_feedback_start_spinner_cleared
                    const/4 v1, 0x0
                    sput-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    invoke-virtual {v0}, Landroid/view/View;->getBackgroundTintList()Landroid/content/res/ColorStateList;
                    move-result-object v1
                    sput-object v1, LX/WKI;->froggoDownloadButtonTint:Landroid/content/res/ColorStateList;
                    const/4 v1, 0x0
                    invoke-virtual {v0, v1}, Landroid/view/View;->setEnabled(Z)V
                    const v1, 0x3e99999a
                    invoke-virtual {v0, v1}, Landroid/view/View;->setAlpha(F)V
                    instance-of v1, v0, Landroid/view/ViewGroup;
                    if-eqz v1, :froggo_feedback_start_no_spinner_host
                    check-cast v0, Landroid/view/ViewGroup;
                    new-instance v1, Landroid/widget/ProgressBar;
                    invoke-virtual {v0}, Landroid/view/View;->getContext()Landroid/content/Context;
                    move-result-object v2
                    invoke-direct {v1, v2}, Landroid/widget/ProgressBar;-><init>(Landroid/content/Context;)V
                    const/4 v2, 0x1
                    invoke-virtual {v1, v2}, Landroid/widget/ProgressBar;->setIndeterminate(Z)V
                    const v2, 0x3f19999a
                    invoke-virtual {v1, v2}, Landroid/view/View;->setScaleX(F)V
                    invoke-virtual {v1, v2}, Landroid/view/View;->setScaleY(F)V
                    invoke-virtual {v0}, Landroid/view/ViewGroup;->getOverlay()Landroid/view/ViewGroupOverlay;
                    move-result-object v2
                    invoke-virtual {v2, v1}, Landroid/view/ViewGroupOverlay;->add(Landroid/view/View;)V
                    sput-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    :froggo_feedback_start_no_spinner_host
                    const-string v1, "FroggoPatches"
                    const-string v2, "feedback-spinner-start"
                    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                    goto :froggo_feedback_start_done

                    :froggo_feedback_start_no_view
                    const-string v1, "FroggoPatches"
                    const-string v2, "feedback-start-no-view"
                    invoke-static {v1, v2}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

                    :froggo_feedback_start_done
                    return-void
                    .catch Ljava/lang/Throwable; {:froggo_feedback_start_try .. :froggo_feedback_start_done} :froggo_feedback_start_catch
                    :froggo_feedback_start_catch
                    move-exception v0
                    const-string v1, "FroggoPatches"
                    const-string v2, "feedback-start-exception"
                    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(animationStartHelper)

        val downloadResultHelper = ImmutableMethod(
            callbackClass.type,
            "froggoShowDownloadFeedbackResult",
            listOf(ImmutableMethodParameter("Z", null, null)),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(8),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    new-instance v0, Landroid/os/Handler;
                    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
                    move-result-object v1
                    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
                    new-instance v1, LX/WKI;
                    const/16 v2, 0x84
                    invoke-static {p0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                    move-result-object v3
                    const/4 v4, 0x0
                    const/4 v5, 0x0
                    invoke-direct {v1, v2, v3, v4, v5}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(downloadResultHelper)

        val storyChoiceHelper = ImmutableMethod(
            callbackClass.type,
            "froggoChooseStoryDownload",
            listOf(
                ImmutableMethodParameter("Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;", null, null),
                ImmutableMethodParameter("Landroid/view/View;", null, null),
            ),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(12),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    iget-object v0, p0, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A02:Lcom/facebook/stories/model/StoryCard;
                    invoke-virtual {v0}, Lcom/facebook/stories/model/StoryCard;->A0l()LX/8OX;
                    move-result-object v0
                    sget-object v1, LX/8OX;->A0D:LX/8OX;
                    if-eq v0, v1, :froggo_story_choice_video
                    invoke-static {p1}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    new-instance v0, LX/WKI;
                    const/16 v1, 0x7f
                    invoke-direct {v0, v1, p0, p0, p0}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    new-instance v1, Ljava/lang/Thread;
                    invoke-direct {v1, v0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v1}, Ljava/lang/Thread;->start()V
                    return-void

                    :froggo_story_choice_video
                    iget-object v0, p0, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A09:Landroid/content/Context;
                    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;
                    move-result-object v1
                    invoke-virtual {v1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;
                    move-result-object v1
                    invoke-virtual {v1}, Landroid/content/res/Configuration;->getLocales()Landroid/os/LocaleList;
                    move-result-object v1
                    const/4 v2, 0x0
                    invoke-virtual {v1, v2}, Landroid/os/LocaleList;->get(I)Ljava/util/Locale;
                    move-result-object v1
                    invoke-virtual {v1}, Ljava/util/Locale;->getLanguage()Ljava/lang/String;
                    move-result-object v1
                    const-string v3, "es"
                    invoke-virtual {v3, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v1
                    if-eqz v1, :froggo_story_choice_english
                    const-string v3, "Descargar historia"
                    const-string v4, "Video completo"
                    const-string v5, "Primer frame"
                    goto :froggo_story_choice_text_ready
                    :froggo_story_choice_english
                    const-string v3, "Download story"
                    const-string v4, "Full video"
                    const-string v5, "First frame"
                    :froggo_story_choice_text_ready
                    const/4 v1, 0x2
                    new-array v1, v1, [Ljava/lang/CharSequence;
                    const/4 v2, 0x0
                    aput-object v4, v1, v2
                    const/4 v2, 0x1
                    aput-object v5, v1, v2
                    new-instance v2, LX/WKI;
                    const/16 v4, 0x87
                    invoke-direct {v2, v4, p0, p1, p0}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    new-instance v4, Landroid/app/AlertDialog${'$'}Builder;
                    invoke-direct {v4, v0}, Landroid/app/AlertDialog${'$'}Builder;-><init>(Landroid/content/Context;)V
                    invoke-virtual {v4, v3}, Landroid/app/AlertDialog${'$'}Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog${'$'}Builder;
                    invoke-virtual {v4, v1, v2}, Landroid/app/AlertDialog${'$'}Builder;->setItems([Ljava/lang/CharSequence;Landroid/content/DialogInterface${'$'}OnClickListener;)Landroid/app/AlertDialog${'$'}Builder;
                    invoke-virtual {v4}, Landroid/app/AlertDialog${'$'}Builder;->show()Landroid/app/AlertDialog;
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(storyChoiceHelper)

        val fullscreenStoryChoiceHelper = ImmutableMethod(
            callbackClass.type,
            "froggoChooseFullscreenStoryDownload",
            listOf(
                ImmutableMethodParameter(callbackClass.type, null, null),
                ImmutableMethodParameter("Ljava/lang/Object;", null, null),
            ),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(12),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    iget-object v0, p0, LX/WKI;->A01:Ljava/lang/Object;
                    check-cast v0, Lcom/facebook/stories/model/StoryCard;
                    invoke-virtual {v0}, Lcom/facebook/stories/model/StoryCard;->A0l()LX/8OX;
                    move-result-object v0
                    sget-object v1, LX/8OX;->A0D:LX/8OX;
                    if-eq v0, v1, :froggo_fullscreen_story_choice_video
                    invoke-static {p1}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    return-void
                    :froggo_fullscreen_story_choice_video
                    invoke-static {p1}, LX/WKI;->froggoCaptureDownloadButton(Ljava/lang/Object;)V
                    sget-object v6, LX/WKI;->froggoDownloadButton:Landroid/view/View;
                    if-eqz v6, :froggo_fullscreen_story_choice_no_view
                    invoke-virtual {v6}, Landroid/view/View;->getContext()Landroid/content/Context;
                    move-result-object v0
                    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;
                    move-result-object v1
                    invoke-virtual {v1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;
                    move-result-object v1
                    invoke-virtual {v1}, Landroid/content/res/Configuration;->getLocales()Landroid/os/LocaleList;
                    move-result-object v1
                    const/4 v2, 0x0
                    invoke-virtual {v1, v2}, Landroid/os/LocaleList;->get(I)Ljava/util/Locale;
                    move-result-object v1
                    invoke-virtual {v1}, Ljava/util/Locale;->getLanguage()Ljava/lang/String;
                    move-result-object v1
                    const-string v3, "es"
                    invoke-virtual {v3, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v1
                    if-eqz v1, :froggo_fullscreen_story_choice_english
                    const-string v3, "Descargar historia"
                    const-string v4, "Video completo"
                    const-string v5, "Primer frame"
                    goto :froggo_fullscreen_story_choice_text_ready
                    :froggo_fullscreen_story_choice_english
                    const-string v3, "Download story"
                    const-string v4, "Full video"
                    const-string v5, "First frame"
                    :froggo_fullscreen_story_choice_text_ready
                    const/4 v1, 0x2
                    new-array v1, v1, [Ljava/lang/CharSequence;
                    const/4 v2, 0x0
                    aput-object v4, v1, v2
                    const/4 v2, 0x1
                    aput-object v5, v1, v2
                    new-instance v2, LX/WKI;
                    const/16 v4, 0x8a
                    invoke-direct {v2, v4, p0, p1, p0}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    new-instance v4, Landroid/app/AlertDialog${'$'}Builder;
                    invoke-direct {v4, v0}, Landroid/app/AlertDialog${'$'}Builder;-><init>(Landroid/content/Context;)V
                    invoke-virtual {v4, v3}, Landroid/app/AlertDialog${'$'}Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog${'$'}Builder;
                    invoke-virtual {v4, v1, v2}, Landroid/app/AlertDialog${'$'}Builder;->setItems([Ljava/lang/CharSequence;Landroid/content/DialogInterface${'$'}OnClickListener;)Landroid/app/AlertDialog${'$'}Builder;
                    invoke-virtual {v4}, Landroid/app/AlertDialog${'$'}Builder;->show()Landroid/app/AlertDialog;
                    return-void
                    :froggo_fullscreen_story_choice_no_view
                    invoke-static {p1}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(fullscreenStoryChoiceHelper)

        val storyChoiceClickMethod = ImmutableMethod(
            callbackClass.type,
            "onClick",
            listOf(
                ImmutableMethodParameter("Landroid/content/DialogInterface;", null, null),
                ImmutableMethodParameter("I", null, null),
            ),
            "V",
            AccessFlags.PUBLIC.value,
            null,
            null,
            MutableMethodImplementation(8),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    iget v0, p0, LX/WKI;->${'$'}t:I
                    const/16 v1, 0x8a
                    if-ne v0, v1, :froggo_story_choice_click_header
                    iget-object v2, p0, LX/WKI;->A01:Ljava/lang/Object;
                    invoke-static {v2}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    iget-object v2, p0, LX/WKI;->A00:Ljava/lang/Object;
                    check-cast v2, LX/WKI;
                    const/4 v1, 0x0
                    if-ne p2, v1, :froggo_fullscreen_story_choice_first_frame
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, v2}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    return-void
                    :froggo_fullscreen_story_choice_first_frame
                    iget-object v3, v2, LX/WKI;->A00:Ljava/lang/Object;
                    iget-object v4, v2, LX/WKI;->A01:Ljava/lang/Object;
                    new-instance v2, LX/WKI;
                    const/16 v1, 0x89
                    const/4 v5, 0x0
                    invoke-direct {v2, v1, v3, v4, v5}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, v2}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    return-void
                    :froggo_story_choice_click_header
                    const/16 v1, 0x87
                    if-ne v0, v1, :froggo_story_choice_click_done
                    iget-object v0, p0, LX/WKI;->A01:Ljava/lang/Object;
                    invoke-static {v0}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    iget-object v0, p0, LX/WKI;->A00:Ljava/lang/Object;
                    const/4 v1, 0x0
                    if-ne p2, v1, :froggo_story_choice_first_frame
                    const/16 v1, 0x7f
                    goto :froggo_story_choice_worker_ready
                    :froggo_story_choice_first_frame
                    const/16 v1, 0x88
                    :froggo_story_choice_worker_ready
                    new-instance v2, LX/WKI;
                    invoke-direct {v2, v1, v0, v0, v0}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, v2}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    :froggo_story_choice_click_done
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(storyChoiceClickMethod)

        val storyWorkerMethod = ImmutableMethod(
            callbackClass.type,
            "froggoRunStoryDownload",
            emptyList<ImmutableMethodParameter>(),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.SYNTHETIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(0, compactStoryDownloadWorkerInstructions(imagePathPrefix, videoPathPrefix))
        }
        callbackClass.methods.add(storyWorkerMethod)

        val storyFirstFrameWorkerMethod = ImmutableMethod(
            callbackClass.type,
            "froggoRunStoryFirstFrameDownload",
            emptyList<ImmutableMethodParameter>(),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.SYNTHETIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(0, storyFirstFrameWorkerInstructions(imagePathPrefix))
        }
        callbackClass.methods.add(storyFirstFrameWorkerMethod)

        val fullscreenStoryWorkerMethod = ImmutableMethod(
            callbackClass.type,
            "froggoRunFullscreenStoryDownload",
            emptyList<ImmutableMethodParameter>(),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.SYNTHETIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(0, fullscreenStoryDownloadWorkerInstructions(imagePathPrefix, videoPathPrefix))
        }
        callbackClass.methods.add(fullscreenStoryWorkerMethod)

        val reelWorkerMethod = ImmutableMethod(
            callbackClass.type,
            "froggoRunReelDownload",
            emptyList<ImmutableMethodParameter>(),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.SYNTHETIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(0, compactReelDownloadWorkerInstructions(videoFolderOption.value!!))
        }
        callbackClass.methods.add(reelWorkerMethod)

        val workerMethod = ImmutableMethod(
            callbackClass.type,
            "run",
            emptyList(),
            "V",
            AccessFlags.PUBLIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    :froggo_download_dispatch_try_start
                    iget v0, p0, LX/WKI;->${'$'}t:I
                    const/16 v1, 0x84
                    if-eq v0, v1, :froggo_download_result_worker
                    const/16 v1, 0x86
                    if-eq v0, v1, :froggo_download_button_cleanup
                    const/16 v1, 0x82
                    if-eq v0, v1, :froggo_toast_worker
                    const/16 v1, 0x83
                    if-eq v0, v1, :froggo_fullscreen_story_worker
                    const/16 v1, 0x7f
                    if-eq v0, v1, :froggo_story_download_worker
                    const/16 v1, 0x88
                    if-eq v0, v1, :froggo_story_first_frame_worker
                    const/16 v1, 0x89
                    if-eq v0, v1, :froggo_story_first_frame_worker
                    invoke-virtual {p0}, LX/WKI;->froggoRunReelDownload()V
                    goto :froggo_download_dispatch_end
                    :froggo_fullscreen_story_worker
                    invoke-virtual {p0}, LX/WKI;->froggoRunFullscreenStoryDownload()V
                    goto :froggo_download_dispatch_end
                    :froggo_story_download_worker
                    invoke-virtual {p0}, LX/WKI;->froggoRunStoryDownload()V
                    goto :froggo_download_dispatch_end
                    :froggo_story_first_frame_worker
                    invoke-virtual {p0}, LX/WKI;->froggoRunStoryFirstFrameDownload()V
                    goto :froggo_download_dispatch_end
                    :froggo_download_result_worker
                    sget-object v0, LX/WKI;->froggoDownloadButton:Landroid/view/View;
                    if-eqz v0, :froggo_download_dispatch_end
                    sget-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    if-eqz v1, :froggo_download_result_spinner_cleared
                    instance-of v2, v0, Landroid/view/ViewGroup;
                    if-eqz v2, :froggo_download_result_spinner_cleared
                    check-cast v0, Landroid/view/ViewGroup;
                    invoke-virtual {v0}, Landroid/view/ViewGroup;->getOverlay()Landroid/view/ViewGroupOverlay;
                    move-result-object v2
                    invoke-virtual {v2, v1}, Landroid/view/ViewGroupOverlay;->remove(Landroid/view/View;)V
                    :froggo_download_result_spinner_cleared
                    const/4 v1, 0x0
                    sput-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    invoke-virtual {v0}, Landroid/view/View;->clearAnimation()V
                    const/high16 v1, 0x3f800000
                    invoke-virtual {v0, v1}, Landroid/view/View;->setAlpha(F)V
                    const/4 v1, 0x1
                    invoke-virtual {v0, v1}, Landroid/view/View;->setEnabled(Z)V

                    iget-object v1, p0, LX/WKI;->A00:Ljava/lang/Object;
                    check-cast v1, Ljava/lang/Boolean;
                    invoke-virtual {v1}, Ljava/lang/Boolean;->booleanValue()Z
                    move-result v1
                    if-eqz v1, :froggo_download_result_failure
                    const/16 v2, 0x10
                    invoke-virtual {v0, v2}, Landroid/view/View;->performHapticFeedback(I)Z
                    const v2, 0xff4caf50
                    const-string v3, "feedback-button-success"
                    goto :froggo_download_result_style_ready
                    :froggo_download_result_failure
                    const v2, 0xfff44336
                    const-string v3, "feedback-button-failure"
                    :froggo_download_result_style_ready
                    invoke-static {v2}, Landroid/content/res/ColorStateList;->valueOf(I)Landroid/content/res/ColorStateList;
                    move-result-object v2
                    invoke-virtual {v0, v2}, Landroid/view/View;->setBackgroundTintList(Landroid/content/res/ColorStateList;)V
                    const-string v2, "FroggoPatches"
                    invoke-static {v2, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

                    new-instance v2, Landroid/view/animation/AlphaAnimation;
                    const v3, 0x3e99999a
                    const/high16 v4, 0x3f800000
                    invoke-direct {v2, v3, v4}, Landroid/view/animation/AlphaAnimation;-><init>(FF)V
                    const-wide/16 v3, 0xdc
                    invoke-virtual {v2, v3, v4}, Landroid/view/animation/Animation;->setDuration(J)V
                    invoke-virtual {v0, v2}, Landroid/view/View;->startAnimation(Landroid/view/animation/Animation;)V

                    new-instance v2, Landroid/os/Handler;
                    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
                    move-result-object v3
                    invoke-direct {v2, v3}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
                    new-instance v3, LX/WKI;
                    const/16 v4, 0x86
                    move-object v5, v0
                    const/4 v6, 0x0
                    const/4 v7, 0x0
                    invoke-direct {v3, v4, v5, v6, v7}, LX/WKI;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
                    const-wide/16 v4, 0x384
                    invoke-virtual {v2, v3, v4, v5}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z
                    goto :froggo_download_dispatch_end

                    :froggo_download_button_cleanup
                    iget-object v0, p0, LX/WKI;->A00:Ljava/lang/Object;
                    instance-of v1, v0, Landroid/view/View;
                    if-eqz v1, :froggo_download_dispatch_end
                    check-cast v0, Landroid/view/View;
                    sget-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    if-eqz v1, :froggo_download_cleanup_spinner_cleared
                    instance-of v2, v0, Landroid/view/ViewGroup;
                    if-eqz v2, :froggo_download_cleanup_spinner_cleared
                    check-cast v0, Landroid/view/ViewGroup;
                    invoke-virtual {v0}, Landroid/view/ViewGroup;->getOverlay()Landroid/view/ViewGroupOverlay;
                    move-result-object v2
                    invoke-virtual {v2, v1}, Landroid/view/ViewGroupOverlay;->remove(Landroid/view/View;)V
                    :froggo_download_cleanup_spinner_cleared
                    const/4 v1, 0x0
                    sput-object v1, LX/WKI;->froggoDownloadSpinner:Landroid/widget/ProgressBar;
                    invoke-virtual {v0}, Landroid/view/View;->clearAnimation()V
                    const/high16 v1, 0x3f800000
                    invoke-virtual {v0, v1}, Landroid/view/View;->setAlpha(F)V
                    const/4 v1, 0x1
                    invoke-virtual {v0, v1}, Landroid/view/View;->setEnabled(Z)V
                    sget-object v1, LX/WKI;->froggoDownloadButton:Landroid/view/View;
                    if-ne v1, v0, :froggo_download_dispatch_end
                    sget-object v1, LX/WKI;->froggoDownloadButtonTint:Landroid/content/res/ColorStateList;
                    invoke-virtual {v0, v1}, Landroid/view/View;->setBackgroundTintList(Landroid/content/res/ColorStateList;)V
                    const/4 v1, 0x0
                    sput-object v1, LX/WKI;->froggoDownloadButton:Landroid/view/View;
                    sput-object v1, LX/WKI;->froggoDownloadButtonTint:Landroid/content/res/ColorStateList;
                    const-string v1, "FroggoPatches"
                    const-string v2, "feedback-button-restored"
                    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                    goto :froggo_download_dispatch_end
                    :froggo_toast_worker
                    iget-object v0, p0, LX/WKI;->A00:Ljava/lang/Object;
                    check-cast v0, Landroid/content/Context;
                    iget-object v1, p0, LX/WKI;->A01:Ljava/lang/Object;
                    check-cast v1, Ljava/lang/CharSequence;
                    invoke-static {}, Landroid/os/Looper;->myLooper()Landroid/os/Looper;
                    move-result-object v2
                    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
                    move-result-object v3
                    if-ne v2, v3, :froggo_toast_repost
                    const/4 v2, 0x1
                    invoke-static {v0, v1, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
                    move-result-object v0
                    invoke-virtual {v0}, Landroid/widget/Toast;->show()V
                    goto :froggo_download_dispatch_end
                    :froggo_toast_repost
                    new-instance v2, Landroid/os/Handler;
                    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;
                    move-result-object v3
                    invoke-direct {v2, v3}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V
                    invoke-virtual {v2, p0}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
                    :froggo_download_dispatch_end
                    return-void
                    .catch Ljava/lang/Throwable; {:froggo_download_dispatch_try_start .. :froggo_download_dispatch_end} :froggo_download_dispatch_catch
                    :froggo_download_dispatch_catch
                    move-exception v0
                    const-string v1, "FroggoPatches"
                    const-string v2, "dispatch exception"
                    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
                    return-void
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(workerMethod)

        val callbackInvokeMethod = ImmutableMethod(
            callbackClass.type,
            "invoke",
            listOf(ImmutableMethodParameter("Ljava/lang/Object;", null, null)),
            "Ljava/lang/Object;",
            AccessFlags.PUBLIC.value,
            null,
            null,
            MutableMethodImplementation(8),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    iget v0, p0, LX/WKI;->${'$'}t:I
                    const/16 v1, 0x80
                    if-eq v0, v1, :froggo_download_invoke_capture_button
                    const/16 v1, 0x81
                    if-ne v0, v1, :froggo_download_invoke_check_fullscreen
                    const-string v1, "FroggoPatches"
                    const-string v2, "invoke-start"
                    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                    invoke-static {p1}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    goto :froggo_download_invoke_done
                    :froggo_download_invoke_check_fullscreen
                    const/16 v1, 0x83
                    if-ne v0, v1, :froggo_download_invoke_check_story
                    invoke-static {p0, p1}, LX/WKI;->froggoChooseFullscreenStoryDownload(LX/WKI;Ljava/lang/Object;)V
                    goto :froggo_download_invoke_done
                    :froggo_download_invoke_check_story
                    const/16 v1, 0x7f
                    if-ne v0, v1, :froggo_download_invoke_done
                    invoke-static {p1}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                    new-instance v0, Ljava/lang/Thread;
                    invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                    invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                    goto :froggo_download_invoke_done
                    :froggo_download_invoke_capture_button
                    invoke-static {p1}, LX/WKI;->froggoCaptureDownloadButton(Ljava/lang/Object;)V
                    :froggo_download_invoke_done
                    sget-object v0, LX/0FI;->A00:LX/0FI;
                    return-object v0
                """.trimIndent(),
            )
        }
        callbackClass.methods.add(callbackInvokeMethod)

        val videoCallbackClass = videoSaveCallback.classDef
        videoCallbackClass.interfaces.removeAll { it == "Ljava/lang/Runnable;" }
        videoCallbackClass.interfaces.add("Ljava/lang/Runnable;")
        val videoWorkerMethod = ImmutableMethod(
            videoCallbackClass.type,
            "run",
            emptyList(),
            "V",
            AccessFlags.PUBLIC.value,
            null,
            null,
            MutableMethodImplementation(16),
        ).toMutable().apply {
            addInstructions(0, compactVideoDownloadWorkerInstructions(videoFolderOption.value!!))
        }
        videoCallbackClass.methods.add(videoWorkerMethod)
        videoSaveCallback.method.addInstructions(
            0,
            """
                move-object/from16 v1, p1
                invoke-static {v1}, LX/WKI;->froggoShowDownloadFeedbackStart(Ljava/lang/Object;)V
                new-instance v0, Ljava/lang/Thread;
                invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V
                invoke-virtual {v0}, Ljava/lang/Thread;->start()V
                return-void
            """.trimIndent(),
        )
    }
}
