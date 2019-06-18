package org.treebolic.one;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.treebolic.download.Deploy;
import org.treebolic.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

		this.expandArchiveCheckbox.setVisibility(View.VISIBLE);
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

	@SuppressWarnings("SameReturnValue")
	@Override
	protected boolean doProcessing()
	{
		return true;
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	protected boolean process(final InputStream inputStream) throws IOException
	{
		final File storage = Storage.getTreebolicStorage(this);

		if (this.expandArchive)
		{
			Deploy.expand(inputStream, Storage.getTreebolicStorage(this), false);
			return true;
		}

		final File destFile = new File(storage, this.downloadUri.getLastPathSegment());
		Deploy.copy(inputStream, destFile);
		return true;
	}
}
