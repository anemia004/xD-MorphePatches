package app.froggo.patches.facebook.download

internal fun compactStoryDownloadWorkerInstructions(
    imagePathPrefix: String,
    videoPathPrefix: String,
) = """
    move-object v10, p0
    iget-object v10, v10, LX/WKI;->A02:Ljava/lang/Object;
    check-cast v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;
    iget-object v0, v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A09:Landroid/content/Context;
    iget-object v1, v10, Lcom/facebook/stories/viewer/ui/buckets/regular/topbar/menu/StoryViewerMoreButtonCallback;->A02:Lcom/facebook/stories/model/StoryCard;
    const/4 v15, 0x0
    const-string v4, "FroggoPatches"
    const-string v5, "story-worker-start"
    invoke-static {v4, v5}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
    const/4 v2, 0x0
    const/4 v3, 0x0
    const/4 v6, 0x0
    const/4 v11, 0x0
    const/4 v14, 0x0

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
    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;
    move-result-object v2
    new-instance v10, Ljava/net/URL;
    invoke-direct {v10, v11}, Ljava/net/URL;-><init>(Ljava/lang/String;)V
    invoke-virtual {v10}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;
    move-result-object v14
    check-cast v14, Ljava/net/HttpURLConnection;
    const-string v5, "User-Agent"
    const-string v6, "Mozilla/5.0"
    invoke-virtual {v14, v5, v6}, Ljava/net/HttpURLConnection;->setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {v14}, Ljava/net/HttpURLConnection;->getResponseCode()I
    move-result v5
    const/16 v6, 0xc8
    if-lt v5, v6, :froggo_story_download_fail
    const/16 v6, 0x190
    if-ge v5, v6, :froggo_story_download_fail
    invoke-virtual {v14}, Ljava/net/HttpURLConnection;->getContentType()Ljava/lang/String;
    move-result-object v4
    if-eqz v4, :froggo_story_download_default_mime
    const-string v10, ";"
    invoke-virtual {v4, v10}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I
    move-result v5
    if-lez v5, :froggo_story_download_mime_ready
    const/4 v10, 0x0
    invoke-virtual {v4, v10, v5}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    move-result-object v4

    :froggo_story_download_mime_ready
    invoke-virtual {v4}, Ljava/lang/String;->length()I
    move-result v5
    if-lez v5, :froggo_story_download_default_mime
    goto :froggo_story_download_collection

    :froggo_story_download_default_mime
    if-eqz v8, :froggo_story_download_photo_mime
    const-string v4, "video/mp4"
    goto :froggo_story_download_collection

    :froggo_story_download_photo_mime
    const-string v4, "image/jpeg"

    :froggo_story_download_collection
    if-eqz v8, :froggo_story_download_images_collection
    sget-object v9, Landroid/provider/MediaStore${'$'}Video${'$'}Media;->EXTERNAL_CONTENT_URI:Landroid/net/Uri;
    const-string v5, "$videoPathPrefix"
    goto :froggo_story_download_path_prefix_ready

    :froggo_story_download_images_collection
    sget-object v9, Landroid/provider/MediaStore${'$'}Images${'$'}Media;->EXTERNAL_CONTENT_URI:Landroid/net/Uri;
    const-string v5, "$imagePathPrefix"

    :froggo_story_download_path_prefix_ready
    new-instance v10, Ljava/lang/StringBuilder;
    invoke-direct {v10, v5}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
    invoke-virtual {v10, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    if-eqz v8, :froggo_story_download_image_owner_folder
    const-string v5, "/Historias/"
    goto :froggo_story_download_owner_folder_ready
    :froggo_story_download_image_owner_folder
    const-string v5, "/"
    :froggo_story_download_owner_folder_ready
    invoke-virtual {v10, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v7
    move-object v1, v7

    invoke-static {v11}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
    move-result-object v10
    invoke-virtual {v10}, Landroid/net/Uri;->getPath()Ljava/lang/String;
    move-result-object v11
    if-eqz v11, :froggo_story_download_default_extension
    const-string v5, "."
    invoke-virtual {v11, v5}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I
    move-result v10
    if-lez v10, :froggo_story_download_default_extension
    add-int/lit8 v6, v10, 0x1
    invoke-virtual {v11}, Ljava/lang/String;->length()I
    move-result v5
    if-ge v6, v5, :froggo_story_download_default_extension
    invoke-virtual {v11, v10}, Ljava/lang/String;->substring(I)Ljava/lang/String;
    move-result-object v13
    sget-object v5, Ljava/util/Locale;->US:Ljava/util/Locale;
    invoke-virtual {v13, v5}, Ljava/lang/String;->toLowerCase(Ljava/util/Locale;)Ljava/lang/String;
    move-result-object v13
    goto :froggo_story_download_extension_ready

    :froggo_story_download_default_extension
    if-eqz v8, :froggo_story_download_jpg_extension
    const-string v13, ".mp4"
    goto :froggo_story_download_extension_ready

    :froggo_story_download_jpg_extension
    const-string v13, ".jpg"

    :froggo_story_download_extension_ready
    move-object p0, v13
    new-instance v10, Ljava/text/SimpleDateFormat;
    const-string v11, "yyyyMMdd_HHmmss"
    sget-object v5, Ljava/util/Locale;->US:Ljava/util/Locale;
    invoke-direct {v10, v11, v5}, Ljava/text/SimpleDateFormat;-><init>(Ljava/lang/String;Ljava/util/Locale;)V
    new-instance v11, Ljava/util/Date;
    invoke-direct {v11}, Ljava/util/Date;-><init>()V
    invoke-virtual {v10, v11}, Ljava/text/SimpleDateFormat;->format(Ljava/util/Date;)Ljava/lang/String;
    move-result-object v12
    const/4 v8, 0x1

    :froggo_story_download_unique_name
    const-string v10, "%s_story-%02d%s"
    const/4 v11, 0x3
    new-array v11, v11, [Ljava/lang/Object;
    const/4 v5, 0x0
    aput-object v12, v11, v5
    const/4 v5, 0x1
    invoke-static {v8}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v13
    aput-object v13, v11, v5
    const/4 v5, 0x2
    aput-object p0, v11, v5
    sget-object v13, Ljava/util/Locale;->US:Ljava/util/Locale;
    invoke-static {v13, v10, v11}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v13

    const-string v10, "_id"
    const/4 v11, 0x1
    new-array v11, v11, [Ljava/lang/String;
    const/4 v5, 0x0
    aput-object v10, v11, v5
    const-string v10, "relative_path=? AND _display_name=?"
    const/4 v6, 0x2
    new-array v6, v6, [Ljava/lang/String;
    const/4 v5, 0x0
    aput-object v1, v6, v5
    const/4 v5, 0x1
    aput-object v13, v6, v5
    move-object v3, v9
    check-cast v3, Landroid/net/Uri;
    move-object v9, v4
    check-cast v9, Ljava/lang/String;
    move-object v4, v11
    move-object v5, v10
    move-object v6, v6
    const/4 v7, 0x0
    invoke-virtual/range {v2 .. v7}, Landroid/content/ContentResolver;->query(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
    move-result-object v10
    const/4 v11, 0x0
    if-eqz v10, :froggo_story_download_name_available
    invoke-interface {v10}, Landroid/database/Cursor;->moveToFirst()Z
    move-result v6
    invoke-interface {v10}, Landroid/database/Cursor;->close()V
    if-eqz v6, :froggo_story_download_name_available
    add-int/lit8 v8, v8, 0x1
    goto :froggo_story_download_unique_name

    :froggo_story_download_name_available
    new-instance v11, Landroid/content/ContentValues;
    invoke-direct {v11}, Landroid/content/ContentValues;-><init>()V
    const-string v10, "_display_name"
    invoke-virtual {v11, v10, v13}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V
    const-string v10, "mime_type"
    invoke-virtual {v11, v10, v9}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V
    const-string v10, "relative_path"
    invoke-virtual {v11, v10, v1}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V
    const-string v10, "is_pending"
    const/4 v5, 0x1
    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v5
    invoke-virtual {v11, v10, v5}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
    invoke-virtual {v2, v3, v11}, Landroid/content/ContentResolver;->insert(Landroid/net/Uri;Landroid/content/ContentValues;)Landroid/net/Uri;
    move-result-object v15
    if-eqz v15, :froggo_story_download_fail
    invoke-virtual {v2, v15}, Landroid/content/ContentResolver;->openOutputStream(Landroid/net/Uri;)Ljava/io/OutputStream;
    move-result-object v5
    if-eqz v5, :froggo_story_download_fail
    invoke-virtual {v14}, Ljava/net/HttpURLConnection;->getInputStream()Ljava/io/InputStream;
    move-result-object v6
    if-eqz v6, :froggo_story_download_fail
    const/16 v7, 0x2000
    new-array v7, v7, [B

    :froggo_story_download_copy_loop
    invoke-virtual {v6, v7}, Ljava/io/InputStream;->read([B)I
    move-result v10
    if-lez v10, :froggo_story_download_copy_done
    const/4 v4, 0x0
    invoke-virtual {v5, v7, v4, v10}, Ljava/io/OutputStream;->write([BII)V
    goto :froggo_story_download_copy_loop

    :froggo_story_download_copy_done
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V
    invoke-virtual {v5}, Ljava/io/OutputStream;->close()V
    invoke-virtual {v14}, Ljava/net/HttpURLConnection;->disconnect()V
    new-instance v10, Landroid/content/ContentValues;
    invoke-direct {v10}, Landroid/content/ContentValues;-><init>()V
    const-string v4, "is_pending"
    const/4 v5, 0x0
    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v5
    invoke-virtual {v10, v4, v5}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
    const/4 v4, 0x0
    invoke-virtual {v2, v15, v10, v4, v4}, Landroid/content/ContentResolver;->update(Landroid/net/Uri;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I
    const/4 v10, 0x1
    invoke-static {v10}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const/4 v10, 0x0
    invoke-static {v0, v10}, LX/WKI;->froggoPostLocalizedDownloadSuccess(Landroid/content/Context;Z)V
    goto :froggo_story_download_finish

    :froggo_story_download_finish
    return-void
    .catch Ljava/lang/Throwable; {:froggo_story_download_try_start .. :froggo_story_download_finish} :froggo_story_download_catch

    :froggo_story_download_fail
    const-string v10, "FroggoPatches"
    const-string v5, "story download failed"
    invoke-static {v10, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    if-eqz v14, :froggo_story_download_pending
    invoke-virtual {v14}, Ljava/net/HttpURLConnection;->disconnect()V
    const/4 v14, 0x0

    :froggo_story_download_pending
    if-eqz v15, :froggo_story_download_fail_notice
    const/4 v4, 0x0
    invoke-virtual {v2, v15, v4, v4}, Landroid/content/ContentResolver;->delete(Landroid/net/Uri;Ljava/lang/String;[Ljava/lang/String;)I
    const/4 v15, 0x0

    :froggo_story_download_fail_notice
    const/4 v10, 0x0
    invoke-static {v10}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v4, "🐸 No se pudo descargar la Historia"
    invoke-static {v0, v4}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    goto :froggo_story_download_finish

    :froggo_story_download_catch
    move-exception v5
    const-string v10, "FroggoPatches"
    const-string v4, "story download exception"
    invoke-static {v10, v4, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
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

internal fun compactReelDownloadWorkerInstructions(videoPathPrefix: String) = """
    move-object v1, p0
    iget-object v1, v1, LX/WKI;->A00:Ljava/lang/Object;
    check-cast v1, LX/3QZ;
    iget-object v0, v1, LX/3QZ;->A0C:Landroid/content/Context;
    const-string v1, "FroggoPatches"
    const-string v2, "reel-dm-start"
    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :froggo_reel_dm_try_start
    move-object v1, p0
    iget-object v1, v1, LX/WKI;->A01:Ljava/lang/Object;
    check-cast v1, LX/4ta;
    iget-object v2, v1, LX/4ta;->A03:Lcom/facebook/video/engine/api/VideoPlayerParams;
    if-eqz v2, :froggo_reel_dm_fail
    iget-object v3, v2, Lcom/facebook/video/engine/api/VideoPlayerParams;->A0b:Lcom/facebook/video/engine/api/VideoDataSource;
    if-eqz v3, :froggo_reel_dm_fail
    iget-object v4, v3, Lcom/facebook/video/engine/api/VideoDataSource;->A08:Landroid/net/Uri;
    if-eqz v4, :froggo_reel_dm_fail

    invoke-virtual {v4}, Landroid/net/Uri;->getScheme()Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_reel_dm_fail
    const-string v6, "http"
    invoke-virtual {v5, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v7
    if-nez v7, :froggo_reel_dm_url_ok
    const-string v6, "https"
    invoke-virtual {v5, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v7
    if-eqz v7, :froggo_reel_dm_fail

    :froggo_reel_dm_url_ok
    new-instance v5, Landroid/app/DownloadManager${'$'}Request;
    invoke-direct {v5, v4}, Landroid/app/DownloadManager${'$'}Request;-><init>(Landroid/net/Uri;)V
    const-string v6, "User-Agent"
    const-string v7, "Mozilla/5.0"
    invoke-virtual {v5, v6, v7}, Landroid/app/DownloadManager${'$'}Request;->addRequestHeader(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const-string v6, "Facebook Reel"
    invoke-virtual {v5, v6}, Landroid/app/DownloadManager${'$'}Request;->setTitle(Ljava/lang/CharSequence;)Landroid/app/DownloadManager${'$'}Request;
    const-string v6, "video/mp4"
    invoke-virtual {v5, v6}, Landroid/app/DownloadManager${'$'}Request;->setMimeType(Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const/4 v6, 0x1
    invoke-virtual {v5, v6}, Landroid/app/DownloadManager${'$'}Request;->setNotificationVisibility(I)Landroid/app/DownloadManager${'$'}Request;

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

    new-instance v7, Ljava/lang/StringBuilder;
    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V
    const-string v8, "FB_Reel_"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v7, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v8, "_"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v8
    invoke-virtual {v7, v8, v9}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;
    const-string v8, ".mp4"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v6

    sget-object v7, Landroid/os/Environment;->DIRECTORY_DOWNLOADS:Ljava/lang/String;
    invoke-virtual {v5, v7, v6}, Landroid/app/DownloadManager${'$'}Request;->setDestinationInExternalPublicDir(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;

    const-string v6, "download"
    invoke-virtual {v0, v6}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v6
    check-cast v6, Landroid/app/DownloadManager;
    invoke-virtual {v6, v5}, Landroid/app/DownloadManager;->enqueue(Landroid/app/DownloadManager${'$'}Request;)J

    const/4 v15, 0x1
    invoke-static {v15}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v1, "🐸 Descarga de Reel iniciada"
    invoke-static {v0, v1}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void

    :froggo_reel_dm_fail
    const-string v1, "FroggoPatches"
    const-string v2, "reel DownloadManager failed"
    invoke-static {v1, v2}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    const/4 v15, 0x0
    invoke-static {v15}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v1, "🐸 No se pudo descargar el Reel"
    invoke-static {v0, v1}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void
    .catch Ljava/lang/Throwable; {:froggo_reel_dm_try_start .. :froggo_reel_dm_fail} :froggo_reel_dm_catch

    :froggo_reel_dm_catch
    move-exception v1
    const-string v2, "FroggoPatches"
    const-string v3, "reel DownloadManager exception"
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    const/4 v15, 0x0
    invoke-static {v15}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v1, "🐸 No se pudo descargar el Reel"
    invoke-static {v0, v1}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void
""".trimIndent()

internal fun compactVideoDownloadWorkerInstructions(videoPathPrefix: String) = """
    move-object v1, p0
    iget-object v1, v1, LX/bq4;->A01:LX/b1P;
    invoke-virtual {v1}, Landroid/view/View;->getContext()Landroid/content/Context;
    move-result-object v0
    const-string v10, "FroggoPatches"
    const-string v11, "video-worker-start"
    invoke-static {v10, v11}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

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
    if-nez v7, :froggo_video_dm_url_ok
    const-string v6, "https"
    invoke-virtual {v5, v6}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result v7
    if-eqz v7, :froggo_video_download_fail

    :froggo_video_dm_url_ok
    const-string v5, "unknown"
    iget-object v6, v1, LX/a8S;->A04:LX/4ta;
    invoke-static {v6}, LX/2lw;->A05(LX/4ta;)Lcom/facebook/graphql/model/GraphQLMedia;
    move-result-object v6
    if-eqz v6, :froggo_video_download_author_from_param
    invoke-virtual {v6}, Lcom/facebook/graphql/model/GraphQLMedia;->A0O()LX/41Q;
    move-result-object v7
    if-eqz v7, :froggo_video_download_author_media_id
    const v8, 0xf02988d6
    invoke-virtual {v7, v8}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_video_download_author_name
    invoke-virtual {v5}, Ljava/lang/String;->length()I
    move-result v8
    if-lez v8, :froggo_video_download_author_name
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_name
    const v8, 0x337a8b
    invoke-virtual {v7, v8}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_video_download_author_owner_id
    invoke-virtual {v5}, Ljava/lang/String;->length()I
    move-result v8
    if-lez v8, :froggo_video_download_author_owner_id
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_owner_id
    const v8, 0xd1b
    invoke-virtual {v7, v8}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_video_download_author_media_id
    invoke-virtual {v5}, Ljava/lang/String;->length()I
    move-result v8
    if-lez v8, :froggo_video_download_author_media_id
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_media_id
    const v8, 0xd1b
    invoke-virtual {v6, v8}, Lcom/facebook/graphql/modelutil/BaseModelWithTree;->getCachedString(I)Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_video_download_author_from_param
    invoke-virtual {v5}, Ljava/lang/String;->length()I
    move-result v8
    if-lez v8, :froggo_video_download_author_from_param
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_from_param
    iget-object v5, v2, Lcom/facebook/video/engine/api/VideoPlayerParams;->A0v:Ljava/lang/String;
    if-eqz v5, :froggo_video_download_author_unknown
    invoke-virtual {v5}, Ljava/lang/String;->length()I
    move-result v8
    if-lez v8, :froggo_video_download_author_unknown
    goto :froggo_video_download_author_ready

    :froggo_video_download_author_unknown
    const-string v5, "unknown"

    :froggo_video_download_author_ready
    const-string v6, "@"
    invoke-virtual {v5, v6}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z
    move-result v6
    if-eqz v6, :froggo_video_download_author_no_at
    const/4 v6, 0x1
    invoke-virtual {v5, v6}, Ljava/lang/String;->substring(I)Ljava/lang/String;
    move-result-object v5

    :froggo_video_download_author_no_at
    const-string v6, "[^A-Za-z0-9._-]"
    const-string v7, "_"
    invoke-virtual {v5, v6, v7}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    move-result-object v5
    if-eqz v5, :froggo_video_download_author_sanitized_unknown
    invoke-virtual {v5}, Ljava/lang/String;->length()I
    move-result v6
    if-lez v6, :froggo_video_download_author_sanitized_unknown
    goto :froggo_video_download_author_sanitized

    :froggo_video_download_author_sanitized_unknown
    const-string v5, "unknown"

    :froggo_video_download_author_sanitized
    new-instance v6, Landroid/app/DownloadManager${'$'}Request;
    invoke-direct {v6, v4}, Landroid/app/DownloadManager${'$'}Request;-><init>(Landroid/net/Uri;)V
    const-string v7, "User-Agent"
    const-string v8, "Mozilla/5.0"
    invoke-virtual {v6, v7, v8}, Landroid/app/DownloadManager${'$'}Request;->addRequestHeader(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const-string v7, "Facebook Video"
    invoke-virtual {v6, v7}, Landroid/app/DownloadManager${'$'}Request;->setTitle(Ljava/lang/CharSequence;)Landroid/app/DownloadManager${'$'}Request;
    const-string v7, "video/mp4"
    invoke-virtual {v6, v7}, Landroid/app/DownloadManager${'$'}Request;->setMimeType(Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;
    const/4 v7, 0x1
    invoke-virtual {v6, v7}, Landroid/app/DownloadManager${'$'}Request;->setNotificationVisibility(I)Landroid/app/DownloadManager${'$'}Request;

    new-instance v7, Ljava/lang/StringBuilder;
    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V
    const-string v8, "FB_Video_"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v7, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v8, "_"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v8
    invoke-virtual {v7, v8, 9}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;
    const-string v8, ".mp4"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v7
    sget-object v8, Landroid/os/Environment;->DIRECTORY_DOWNLOADS:Ljava/lang/String;
    invoke-virtual {v6, v8, v7}, Landroid/app/DownloadManager${'$'}Request;->setDestinationInExternalPublicDir(Ljava/lang/String;Ljava/lang/String;)Landroid/app/DownloadManager${'$'}Request;

    const-string v7, "download"
    invoke-virtual {v0, v7}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v7
    check-cast v7, Landroid/app/DownloadManager;
    invoke-virtual {v7, v6}, Landroid/app/DownloadManager;->enqueue(Landroid/app/DownloadManager${'$'}Request;)J

    const/4 v10, 0x1
    invoke-static {v10}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v11, "🐸 Descarga iniciada"
    invoke-static {v0, v11}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void

    :froggo_video_download_fail
    const/4 v10, 0x0
    invoke-static {v10}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v11, "🐸 No se pudo descargar el video"
    invoke-static {v0, v11}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void
    .catch Ljava/lang/Throwable; {:froggo_video_download_try_start .. :froggo_video_download_fail} :froggo_video_download_catch

    :froggo_video_download_catch
    move-exception v1
    const-string v2, "FroggoPatches"
    const-string v3, "video download exception"
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    const/4 v10, 0x0
    invoke-static {v10}, LX/WKI;->froggoShowDownloadFeedbackResult(Z)V
    const-string v11, "🐸 No se pudo descargar el video"
    invoke-static {v0, v11}, LX/WKI;->froggoPostToast(Landroid/content/Context;Ljava/lang/CharSequence;)V
    return-void
""".trimIndent()
