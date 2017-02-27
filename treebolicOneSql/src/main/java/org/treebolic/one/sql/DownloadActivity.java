package org.treebolic.one.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.treebolic.download.Deploy;
import org.treebolic.one.sql.R;
import org.treebolic.storage.Storage;

import android.os.Bundle;
import android.widget.Toast;

/**
 * Dot download activity
 *
 * @author Bernard Bou
 */
public class DownloadActivity extends org.treebolic.download.DownloadActivity
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.destDir = Storage.getCacheDir(this);
		this.downloadUrl = Settings.getStringPref(this, Settings.PREF_DOWNLOAD);
		this.downloadUrl = Settings.getStringPref(this, Settings.PREF_DOWNLOAD);
		if (this.downloadUrl == null || this.downloadUrl.isEmpty())
		{
			Toast.makeText(this, R.string.error_null_download_url, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void start()
	{
		start(R.string.treebolic);
	}

	// P O S T P R O C E S S I N G

	@Override
	protected boolean doProcessing()
	{
		return true;
	}

	@Override
	protected boolean process(final InputStream inputStream) throws IOException
	{
		if(this.expandArchive)
		{
			Deploy.expand(inputStream, Storage.getTreebolicStorage(this), false);
			return true;
		}
		Deploy.copy(inputStream, new File(Storage.getTreebolicStorage(this), this.destUri.getLastPathSegment()));
		return true;
	}
}
