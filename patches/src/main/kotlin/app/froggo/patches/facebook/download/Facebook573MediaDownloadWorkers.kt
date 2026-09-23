package app.froggo.patches.facebook.download

/**
 * Story worker — DownloadManager-based.
 * Extracts URL + author from StoryCard, enqueues a DownloadManager request.
 * No MediaStore, no URLConnection, no byte copy.
 */
internal fun compactStoryDownloadWorkerInstructions(
    imagePathPrefix: String,
    videoPathPrefix: String,
) = """
    move-object v10, p0
    iget-object v10, v10, LX/WKI;->A02:Ljava/lang/Object;
    check-cast v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;
    iget-object v0, v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A09:Landroid/content/Context;
    iget-object v1, v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A02:Lcom/facebook/stories/model/StoryCard;

    :froggo_story_download_try_start
    invoke-virtual {v1}, Lcom/facebook/stories/model/StoryCard;->getMedia()LX/9Uo;
    move-result-object v9
    if-eqz v9, :froggo_story_download_fail
    invoke-virtual {v1}, Lcom/facebook/stories/model/StoryCard;->A0l()LX/8OX;
    move-result-object v10
    sget-object v5, LX/8OX;->A0D:LX/8OX;
    if-ne v10, v5, :froggo_story_download_photo
    iget-object v11, v9, LX/9Uo;->A05:Ljava/lang/String;
    invoke-virtual {v1}, Lcom/facebook/stories/model/StoryCard;->A0S()LX/CP6;
    move-result-object v12
    if-eqz v12, :froggo_story_download_video_url_ready
    check-cast v12, LX/a7W;
    const v13, -0x7cc94363
    invoke-virtual {v12, v13}, LX/a7W;->getCachedNullableString(I)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_story_download_video_url_ready
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_story_download_video_url_ready
    move-object v11, v12
    :froggo_story_download_video_url_ready
    const/4 v8, 0x1
    goto :froggo_story_download_type_ready

    :froggo_story_download_photo
    sget-object v5, LX/8OX;->A09:LX/8OX;
    if-ne v10, v5, :froggo_story_download_fail
    iget-object v11, v9, LX/9Uo;->A03:Ljava/lang/String;
    const/4 v8, 0x0

    :froggo_story_download_type_ready
    if-eqz v11, :froggo_story_download_fail
    invoke-virtual {v11}, Ljava/lang/String;->length()I
    move-result v5
    if-lez v5, :froggo_story_download_fail

    invoke-virtual {v1}, Lcom/facebook/stories/model/StoryCard;->A0U()LX/CPA;
    move-result-object v10
    if-eqz v10, :froggo_story_download_card_id
    invoke-interface {v10}, LX/CPA;->C2z()Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_story_download_owner_name
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v5
    if-lez v5, :froggo_story_download_owner_name
    goto :froggo_story_download_owner_ready

    :froggo_story_download_owner_name
    invoke-interface {v10}, LX/CPA;->getName()Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_story_download_card_id
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v5
    if-lez v5, :froggo_story_download_card_id
    goto :froggo_story_download_owner_ready

    :froggo_story_download_card_id
    invoke-virtual {v1}, Lcom/facebook/stories/model/StoryCard;->getId()Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_story_download_unknown_owner
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v5
    if-lez v5, :froggo_story_download_unknown_owner
    goto :froggo_story_download_owner_ready

    :froggo_story_download_unknown_owner
    const-string v12, "unknown"

    :froggo_story_download_owner_ready
    const-string v10, "@"
    invoke-virtual {v12, v10}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v5
    if-eqz v5, :froggo_story_download_owner_no_at
    const/4 v10, 0x1
    invoke-virtual {v12, v10}, Ljava/lang/String;->substring(I)Ljava/lang/String;
    move-result-object v12

    :froggo_story_download_owner_no_at
    const-string v10, "[^A-Za-z0-9._-]"
    const-string v5, "_"
    invoke-virtual {v12, v10, v5}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_story_download_unknown_owner_after_sanitize
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v5
    if-lez v5, :froggo_story_download_unknown_owner_after_sanitize
    goto :froggo_story_download_owner_sanitized

    :froggo_story_download_unknown_owner_after_sanitize
    const-string v12, "unknown"

    :froggo_story_download_owner_sanitized
    invoke-static {v11}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
    move-result-object v10

    const-string v5, "download"
    invoke-virtual {v0, v5}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, Landroid/app/DownloadManager;

    new-instance v3, Ljava/lang/StringBuilder;
    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V
    const-string v5, "FB_story_"
    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v3, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v5, "_"
    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v5
    invoke-virtual {v3, v5, v6}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;
    if-eqz v8, :froggo_story_download_ext_jpg
    const-string v5, ".mp4"
    goto :froggo_story_download_ext_ready
    :froggo_story_download_ext_jpg
    const-string v5, ".jpg"
    :froggo_story_download_ext_ready
    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v3

    new-instance v6, Ljava/lang/StringBuilder;
    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V
    const-string v5, "FroggoPatches/Facebook/"
    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v6, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v6

    new-instance v5, Landroid/app/DownloadManager${'$'}Request;
    invoke-direct {v5, v10}, Landroid/app/DownloadManager${'$'}Request;-><init>(Landroid/net/Uri;)V
    invoke-virtual {v5, v3}, Landroid/app/DownloadManager${'$'}Request;->setTitle(Ljava/lang/CharSequence;)Landroid/app/DownloadManager${'$'}Request;
    if-eqz v8, :froggo_story_download_mime_jpg
    const-string v9, "video/mp4"
    goto :froggo_story_download_mime_ready
    :froggo_story_download_mime_jpg
    const-string v9, "image/jpeg"
    :froggo_story_download_mime_ready
    invoke-virtual {v5, v9}, Landroid/app/DownloadManager${'$'}Request;->setMimeType(Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const/4 v9, 0x1
    invoke-virtual {v5, v9}, Landroid/app/DownloadManager${'$'}Request;->setNotificationVisibility(I)Landroid/app/DownloadManager${'$'}Request;
    const-string v9, "User-Agent"
    const-string v10, "Mozilla/5.0"
    invoke-virtual {v5, v9, v10}, Landroid/app/DownloadManager${'$'}Request;->addRequestHeader(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    if-eqz v8, :froggo_story_download_dir_pictures
    sget-object v9, Landroid/os/Environment;->DIRECTORY_MOVIES:Ljava/lang/String;
    goto :froggo_story_download_dir_ready
    :froggo_story_download_dir_pictures
    sget-object v9, Landroid/os/Environment;->DIRECTORY_PICTURES:Ljava/lang/String;
    :froggo_story_download_dir_ready
    invoke-virtual {v5, v9, v6}, Landroid/app/DownloadManager${'$'}Request;->setDestinationInExternalPublicDir(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    invoke-virtual {v4, v5}, Landroid/app/DownloadManager;->enqueue(Landroid/app/DownloadManager${'$'}Request;)J
    move-result-wide v9

    const/4 v9, 0x1
    invoke-static {v9}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const/4 v9, 0x0
    invoke-static {v0, v9}, LX/WKI;->froggoPostLocalizedDownloadSuccess(Landroid/content/Context;Z)V
    goto :froggo_story_download_finish

    :froggo_story_download_finish
    return-void
    .catch Ljava/lang/Throwable; {:froggo_story_download_try_start .. :froggo_story_download_finish} :froggo_story_download_catch

    :froggo_story_download_fail
    const-string v5, "FroggoPatches"
    const-string v6, "story download failed"
    invoke-static {v5, v6}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    const/4 v5, 0x0
    invoke-static {v5}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v5, "🐸 No se pudo descargar la Historia"
    invoke-static {v0, v5}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    goto :froggo_story_download_finish

    :froggo_story_download_catch
    move-exception v5
    const-string v6, "FroggoPatches"
    const-string v7, "story download exception"
    invoke-static {v6, v7, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    goto :froggo_story_download_fail
""".trimIndent()

internal fun fullscreenStoryDownloadWorkerInstructions(
    imagePathPrefix: String,
    videoPathPrefix: String,
) = run {
    val callbackHeader = """
        move-object v10, p0
        iget-object v10, v10, LX/WKI;->A02:Ljava/lang/Object;
        check-cast v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;
        iget-object v0, v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A09:Landroid/content/Context;
        iget-object v1, v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A02:Lcom/facebook/stories/model/StoryCard;
    """.trimIndent()
    compactStoryDownloadWorkerInstructions(imagePathPrefix, videoPathPrefix)
        .replace(
            callbackHeader,
            """
                move-object v0, p0
                iget-object v0, v0, LX/WKI;->A00:Ljava/lang/Object;
                check-cast v0, Landroid/content/Context;
                move-object v1, p0
                iget-object v1, v1, LX/WKI;->A01:Ljava/lang/Object;
                check-cast v1, Lcom/facebook/stories/model/StoryCard;
            """.trimIndent(),
        )
        .replace("froggo_story_download_", "froggo_fullscreen_story_download_")
}

internal fun storyFirstFrameWorkerInstructions(imagePathPrefix: String) = """
    iget v9, p0, LX/WKI;->${'$'}t:I
    const/16 v10, 0x89
    if-eq v9, v10, :froggo_story_frame_fullscreen
    iget-object v0, p0, LX/WKI;->A00:Ljava/lang/Object;
    check-cast v0, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;
    iget-object v1, v0, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A09:Landroid/content/Context;
    iget-object v2, v0, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A02:Lcom/facebook/stories/model/StoryCard;
    goto :froggo_story_frame_payload_ready
    :froggo_story_frame_fullscreen
    iget-object v1, p0, LX/WKI;->A00:Ljava/lang/Object;
    check-cast v1, Landroid/content/Context;
    iget-object v2, p0, LX/WKI;->A01:Ljava/lang/Object;
    check-cast v2, Lcom/facebook/stories/model/StoryCard;
    :froggo_story_frame_payload_ready
    const/4 v3, 0x0
    :froggo_story_frame_try
    invoke-virtual {v2}, Lcom/facebook/stories/model/StoryCard;->getMedia()LX/9Uo;
    move-result-object v4
    if-eqz v4, :froggo_story_frame_fail
    iget-object v4, v4, LX/9Uo;->A05:Ljava/lang/String;
    if-eqz v4, :froggo_story_frame_fail
    new-instance v5, Landroid/media/MediaMetadataRetriever;
    invoke-direct {v5}, Landroid/media/MediaMetadataRetriever;-><init>()V
    new-instance v6, Ljava/util/HashMap;
    invoke-direct {v6}, Ljava/util/HashMap;-><init>()V
    const-string v7, "User-Agent"
    const-string v8, "Mozilla/5.0"
    invoke-virtual {v6, v7, v8}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v5, v4, v6}, Landroid/media/MediaMetadataRetriever;->setDataSource(Ljava/lang/String;Ljava/util/Map;)V
    const-wide/16 v6, 0x0
    invoke-virtual {v5, v6, v7}, Landroid/media/MediaMetadataRetriever;->getFrameAtTime(J)Landroid/graphics/Bitmap;
    move-result-object v4
    invoke-virtual {v5}, Landroid/media/MediaMetadataRetriever;->release()V
    if-eqz v4, :froggo_story_frame_fail

    invoke-virtual {v2}, Lcom/facebook/stories/model/StoryCard;->A0U()LX/CPA;
    move-result-object v5
    if-eqz v5, :froggo_story_frame_unknown
    invoke-interface {v5}, LX/CPA;->C2z()Ljava/lang/String;
    move-result-object v5
    if-nez v5, :froggo_story_frame_owner_ready
    :froggo_story_frame_unknown
    const-string v5, "unknown"
    :froggo_story_frame_owner_ready
    const-string v6, "[^A-Za-z0-9._-]"
    const-string v7, "_"
    invoke-virtual {v5, v6, v7}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v5
    new-instance v6, Ljava/lang/StringBuilder;
    const-string v7, "$imagePathPrefix"
    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v5, "/"
    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v5
    new-instance v6, Ljava/text/SimpleDateFormat;
    const-string v7, "yyyyMMdd_HHmmss'_story-01.jpg'"
    sget-object v8, Ljava/util/Locale;->US:Ljava/util/Locale;
    invoke-direct {v6, v7, v8}, Ljava/text/SimpleDateFormat;-><init>(Ljava/lang/String;Ljava/util/Locale;)V
    new-instance v7, Ljava/util/Date;
    invoke-direct {v7}, Ljava/util/Date;-><init>()V
    invoke-virtual {v6, v7}, Ljava/text/SimpleDateFormat;->format(Ljava/util/Date;)Ljava/lang/String;
    move-result-object v6
    new-instance v7, Landroid/content/ContentValues;
    invoke-direct {v7}, Landroid/content/ContentValues;-><init>()V
    const-string v8, "_display_name"
    invoke-virtual {v7, v8, v6}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V
    const-string v6, "mime_type"
    const-string v8, "image/jpeg"
    invoke-virtual {v7, v6, v8}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V
    const-string v6, "relative_path"
    invoke-virtual {v7, v6, v5}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V
    const-string v6, "is_pending"
    const/4 v8, 0x1
    invoke-static {v8}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v8
    invoke-virtual {v7, v6, v8}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
    invoke-virtual {v1}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;
    move-result-object v5
    sget-object v6, Landroid/provider/MediaStore${'$'}Images${'$'}Media;->EXTERNAL_CONTENT_URI:Landroid/net/Uri;
    invoke-virtual {v5, v6, v7}, Landroid/content/ContentResolver;->insert(Landroid/net/Uri;Landroid/content/ContentValues;)Landroid/net/Uri;
    move-result-object v3
    if-eqz v3, :froggo_story_frame_fail
    invoke-virtual {v5, v3}, Landroid/content/ContentResolver;->openOutputStream(Landroid/net/Uri;)Ljava/io/OutputStream;
    move-result-object v6
    if-eqz v6, :froggo_story_frame_fail
    sget-object v7, Landroid/graphics/Bitmap${'$'}CompressFormat;->JPEG:Landroid/graphics/Bitmap${'$'}CompressFormat;
    const/16 v8, 0x63
    invoke-virtual {v4, v7, v8, v6}, Landroid/graphics/Bitmap;->compress(Landroid/graphics/Bitmap${'$'}CompressFormat;ILjava/io/OutputStream;)Z
    move-result v7
    invoke-virtual {v6}, Ljava/io/OutputStream;->close()V
    invoke-virtual {v4}, Landroid/graphics/Bitmap;->recycle()V
    if-eqz v7, :froggo_story_frame_fail
    new-instance v7, Landroid/content/ContentValues;
    invoke-direct {v7}, Landroid/content/ContentValues;-><init>()V
    const-string v6, "is_pending"
    const/4 v8, 0x0
    invoke-static {v8}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v8
    invoke-virtual {v7, v6, v8}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
    const/4 v6, 0x0
    invoke-virtual {v5, v3, v7, v6, v6}, Landroid/content/ContentResolver;->update(Landroid/net/Uri;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I
    const/4 v6, 0x1
    invoke-static {v6}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const/4 v6, 0x0
    invoke-static {v1, v6}, LX/WKI;->froggoPostLocalizedDownloadSuccess(Landroid/content/Context;Z)V
    return-void

    :froggo_story_frame_fail
    if-eqz v3, :froggo_story_frame_fail_feedback
    invoke-virtual {v1}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;
    move-result-object v5
    const/4 v6, 0x0
    invoke-virtual {v5, v3, v6, v6}, Landroid/content/ContentResolver;->delete(Landroid/net/Uri;Ljava/lang/String;[Ljava/lang/String;)I
    :froggo_story_frame_fail_feedback
    const/4 v6, 0x0
    invoke-static {v6}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v6, "🐸 No se pudo guardar el primer frame"
    invoke-static {v1, v6}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void
    .catch Ljava/lang/Throwable; {:froggo_story_frame_try .. :froggo_story_frame_fail_feedback} :froggo_story_frame_catch
    :froggo_story_frame_catch
    move-exception v4
    const-string v5, "FroggoPatches"
    const-string v6, "story first-frame exception"
    invoke-static {v5, v6, v4}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    goto :froggo_story_frame_fail
""".trimIndent()

/**
 * Feed-video worker — DownloadManager-based.
 */
internal val compactVideoDownloadWorkerInstructions = """
    move-object v1, p0
    iget-object v1, v1, LX/bq4;->A01:LX/b1P;
    invoke-virtual {v1}, Landroid/view/View;->getContext()Landroid/content/Context;
    move-result-object v0

    :froggo_video_download_try_start
    iget-object v2, v1, LX/a8s;->A0B:Lcom/facebook/video/engine/api/VideoPlayerParams;
    if-eqz v2, :froggo_video_download_fail
    iget-object v3, v2, Lcom/facebook/video/engine/api/VideoPlayerParams;->A0b:Lcom/facebook/video/engine/api/VideoDataSource;
    if-eqz v3, :froggo_video_download_fail
    iget-object v4, v3, Lcom/facebook/video/engine/api/VideoDataSource;->A08:Landroid/net/Uri;
    if-eqz v4, :froggo_video_download_fail
    invoke-virtual {v4}, Landroid/net/Uri;->getScheme()Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_video_download_fail
    const-string v6, "http"
    invoke-virtual {v5, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v7
    if-nez v7, :froggo_video_download_url_ok
    const-string v6, "https"
    invoke-virtual {v5, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v7
    if-eqz v7, :froggo_video_download_fail

    :froggo_video_download_url_ok
    const-string v12, "unknown"
    iget-object v10, v1, LX/a8S;->A04:LX/4ta;
    invoke-static {v10}, LX/2lw;->A05(LX/4ta;)Lcom/facebook/graphql/model/GraphQLMedia;
    move-result-object v10
    if-eqz v10, :froggo_video_download_author_from_param
    invoke-virtual {v10}, Lcom/facebook/graphql/model/GraphQLMedia;->A0O()LX/41Q;
    move-result-object v11
    if-eqz v11, :froggo_video_download_author_media_id
    const v13, 0xf02988d6
    invoke-virtual {v11, v13}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_video_download_author_name
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_video_download_author_name
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_name
    const v13, 0x337a8b
    invoke-virtual {v11, v13}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_video_download_author_owner_id
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_video_download_author_owner_id
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_owner_id
    const v13, 0xd1b
    invoke-virtual {v11, v13}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_video_download_author_media_id
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_video_download_author_media_id
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_media_id
    const v13, 0xd1b
    invoke-virtual {v10, v13}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_video_download_author_from_param
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_video_download_author_from_param
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_from_param
    iget-object v12, v2, Lcom/facebook/video/engine/api/VideoPlayerParams;->A0v:Ljava/lang/String;
    if-eqz v12, :froggo_video_download_author_unknown
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_video_download_author_unknown
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_unknown
    const-string v12, "unknown"

    :froggo_video_download_author_ready
    const-string v10, "@"
    invoke-virtual {v12, v10}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v13
    if-eqz v13, :froggo_video_download_author_no_at
    const/4 v10, 0x1
    invoke-virtual {v12, v10}, Ljava/lang/String;->substring(I)Ljava/lang/String;
    move-result-object v12

    :froggo_video_download_author_no_at
    const-string v10, "[^A-Za-z0-9._-]"
    const-string v11, "_"
    invoke-virtual {v12, v10, v11}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v12
    if-eqz v12, :froggo_video_download_author_sanitized_unknown
    invoke-virtual {v12}, Ljava/lang/String;->length()I
    move-result v13
    if-lez v13, :froggo_video_download_author_sanitized_unknown
    goto :froggo_video_download_author_sanitized

    :froggo_video_download_author_sanitized_unknown
    const-string v12, "unknown"

    :froggo_video_download_author_sanitized
    new-instance v10, Ljava/lang/StringBuilder;
    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V
    const-string v13, "FB_video_"
    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v10, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v13, "_"
    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v13
    invoke-virtual {v10, v13, v14}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;
    const-string v13, ".mp4"
    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v10

    new-instance v5, Ljava/lang/StringBuilder;
    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V
    const-string v13, "FroggoPatches/Facebook/"
    invoke-virtual {v5, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v5, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v5

    const-string v13, "download"
    invoke-virtual {v0, v13}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v6
    check-cast v6, Landroid/app/DownloadManager;

    new-instance v7, Landroid/app/DownloadManager${'$'}Request;
    invoke-direct {v7, v4}, Landroid/app/DownloadManager${'$'}Request;-><init>(Landroid/net/Uri;)V
    invoke-virtual {v7, v10}, Landroid/app/DownloadManager${'$'}Request;->setTitle(Ljava/lang/CharSequence;)Landroid/app/DownloadManager${'$'}Request;
    const-string v13, "video/mp4"
    invoke-virtual {v7, v13}, Landroid/app/DownloadManager${'$'}Request;->setMimeType(Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const/4 v13, 0x1
    invoke-virtual {v7, v13}, Landroid/app/DownloadManager${'$'}Request;->setNotificationVisibility(I)Landroid/app/DownloadManager${'$'}Request;
    const-string v13, "User-Agent"
    const-string v14, "Mozilla/5.0"
    invoke-virtual {v7, v13, v14}, Landroid/app/DownloadManager${'$'}Request;->addRequestHeader(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    sget-object v13, Landroid/os/Environment;->DIRECTORY_MOVIES:Ljava/lang/String;
    invoke-virtual {v7, v13, v5}, Landroid/app/DownloadManager${'$'}Request;->setDestinationInExternalPublicDir(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    invoke-virtual {v6, v7}, Landroid/app/DownloadManager;->enqueue(Landroid/app/DownloadManager${'$'}Request;)J
    move-result-wide v13

    const/4 v13, 0x1
    invoke-static {v13}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    goto :froggo_video_download_finish

    :froggo_video_download_finish
    return-void
    .catch Ljava/lang/Throwable; {:froggo_video_download_try_start .. :froggo_video_download_finish} :froggo_video_download_catch

    :froggo_video_download_fail
    const-string v13, "FroggoPatches"
    const-string v14, "video download failed"
    invoke-static {v13, v14}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    const/4 v13, 0x0
    invoke-static {v13}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    goto :froggo_video_download_finish

    :froggo_video_download_catch
    move-exception v13
    const-string v14, "FroggoPatches"
    const-string v15, "video download exception"
    invoke-static {v14, v15, v13}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    goto :froggo_video_download_fail
""".trimIndent()

/**
 * Reels worker — DownloadManager-based, no polling.
 */
internal fun compactReelDownloadWorkerInstructions(videoPathPrefix: String) = """
    move-object v1, p0
    iget-object v1, v1, LX/WKI;->A00:Ljava/lang/Object;
    check-cast v1, LX/3QZ;
    iget-object v0, v1, LX/3QZ;->A0C:Landroid/content/Context;

    :froggo_reel_dm_try_start
    move-object v2, p0
    iget-object v2, v2, LX/WKI;->A01:Ljava/lang/Object;
    check-cast v2, LX/4ta;
    iget-object v3, v2, LX/4ta;->A03:Lcom/facebook/video/engine/api/VideoPlayerParams;
    if-eqz v3, :froggo_reel_dm_fail
    iget-object v4, v3, Lcom/facebook/video/engine/api/VideoPlayerParams;->A0b:Lcom/facebook/video/engine/api/VideoDataSource;
    if-eqz v4, :froggo_reel_dm_fail
    iget-object v5, v4, Lcom/facebook/video/engine/api/VideoDataSource;->A08:Landroid/net/Uri;
    if-eqz v5, :froggo_reel_dm_fail

    invoke-virtual {v5}, Landroid/net/Uri;->getScheme()Ljava/lang/String;
    move-result-object v6
    if-eqz v6, :froggo_reel_dm_fail
    const-string v7, "http"
    invoke-virtual {v6, v7}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v8
    if-nez v8, :froggo_reel_dm_url_ok
    const-string v7, "https"
    invoke-virtual {v6, v7}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v8
    if-eqz v8, :froggo_reel_dm_fail

    :froggo_reel_dm_url_ok
    move-object v6, p0
    iget-object v6, v6, LX/WKI;->A02:Ljava/lang/Object;
    check-cast v6, LX/BsO;
    invoke-static {v6}, LX/2wL;->A0F(LX/BsO;)Ljava/lang/String;
    move-result-object v6
    if-nez v6, :froggo_reel_owner_ready
    const-string v6, "unknown"
    :froggo_reel_owner_ready
    const-string v7, "@"
    invoke-virtual {v6, v7}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v7
    if-eqz v7, :froggo_reel_owner_no_at
    const/4 v7, 0x1
    invoke-virtual {v6, v7}, Ljava/lang/String;->substring(I)Ljava/lang/String;
    move-result-object v6
    :froggo_reel_owner_no_at
    const-string v7, "[^A-Za-z0-9._-]"
    const-string v8, "_"
    invoke-virtual {v6, v7, v8}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v6

    new-instance v13, Ljava/lang/StringBuilder;
    invoke-direct {v13}, Ljava/lang/StringBuilder;-><init>()V
    const-string v7, "FB_reel_"
    invoke-virtual {v13, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v13, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v7, "_"
    invoke-virtual {v13, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v7
    invoke-virtual {v13, v7, v8}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;
    const-string v7, ".mp4"
    invoke-virtual {v13, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v13

    new-instance v14, Ljava/lang/StringBuilder;
    invoke-direct {v14}, Ljava/lang/StringBuilder;-><init>()V
    const-string v7, "FroggoPatches/Facebook/"
    invoke-virtual {v14, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v14, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v14

    const-string v7, "download"
    invoke-virtual {v0, v7}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v6
    check-cast v6, Landroid/app/DownloadManager;

    new-instance v7, Landroid/app/DownloadManager${'$'}Request;
    invoke-direct {v7, v5}, Landroid/app/DownloadManager${'$'}Request;-><init>(Landroid/net/Uri;)V
    invoke-virtual {v7, v13}, Landroid/app/DownloadManager${'$'}Request;->setTitle(Ljava/lang/CharSequence;)Landroid/app/DownloadManager${'$'}Request;
    const-string v8, "video/mp4"
    invoke-virtual {v7, v8}, Landroid/app/DownloadManager${'$'}Request;->setMimeType(Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const/4 v8, 0x1
    invoke-virtual {v7, v8}, Landroid/app/DownloadManager${'$'}Request;->setNotificationVisibility(I)Landroid/app/DownloadManager${'$'}Request;
    const-string v8, "User-Agent"
    const-string v9, "Mozilla/5.0"
    invoke-virtual {v7, v8, v9}, Landroid/app/DownloadManager${'$'}Request;->addRequestHeader(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    sget-object v8, Landroid/os/Environment;->DIRECTORY_MOVIES:Ljava/lang/String;
    invoke-virtual {v7, v8, v14}, Landroid/app/DownloadManager${'$'}Request;->setDestinationInExternalPublicDir(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    invoke-virtual {v6, v7}, Landroid/app/DownloadManager;->enqueue(Landroid/app/DownloadManager${'$'}Request;)J
    move-result-wide v8

    const/4 v9, 0x1
    invoke-static {v9}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const/4 v9, 0x1
    invoke-static {v0, v9}, LX/WKI;->froggoPostLocalizedDownloadSuccess(Landroid/content/Context;Z)V
    goto :froggo_reel_dm_finish

    :froggo_reel_dm_fail
    const-string v1, "FroggoPatches"
    const-string v2, "reel download failed"
    invoke-static {v1, v2}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    const/4 v1, 0x0
    invoke-static {v1}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v1, "🐸 No se pudo descargar el Reel"
    invoke-static {v0, v1}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V

    :froggo_reel_dm_finish
    return-void
    .catch Ljava/lang/Throwable; {:froggo_reel_dm_try_start .. :froggo_reel_dm_finish} :froggo_reel_dm_catch

    :froggo_reel_dm_catch
    move-exception v1
    const-string v2, "FroggoPatches"
    const-string v3, "reel download exception"
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    goto :froggo_reel_dm_fail
""".trimIndent()
