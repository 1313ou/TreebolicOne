/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import org.treebolic.download.Deploy.copy
import org.treebolic.download.Deploy.expand
import org.treebolic.storage.Storage.getTreebolicStorage
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Download activity
 *
 * @author Bernard Bou
 */
class DownloadActivity : org.treebolic.download.DownloadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // parameters
        expandArchiveCheckbox!!.visibility = View.VISIBLE
        this.downloadUrl = Settings.getStringPref(this, Settings.PREF_DOWNLOAD)
        if (this.downloadUrl == null || downloadUrl!!.isEmpty()) {
            Toast.makeText(this, R.string.error_null_download_url, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    public override fun start() {
        start(R.string.treebolic)
    }

    // P O S T P R O C E S S I N G

    override fun doProcessing(): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun process(inputStream: InputStream): Boolean {
        val storage = getTreebolicStorage(this)

        if (this.expandArchive) {
            expand(inputStream, getTreebolicStorage(this), false)
            return true
        }

        val downloadUri = Uri.parse(this.downloadUrl)
        val lastSegment = downloadUri.lastPathSegment ?: return false
        val destFile = File(storage, lastSegment)
        copy(inputStream, destFile)
        return true
    }
}
