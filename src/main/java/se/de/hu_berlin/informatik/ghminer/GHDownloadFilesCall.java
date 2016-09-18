package se.de.hu_berlin.informatik.ghminer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;
import se.de.hu_berlin.informatik.utils.threaded.DisruptorEventHandler;
import se.de.hu_berlin.informatik.utils.threaded.IDisruptorEventHandlerFactory;

/**
 * A simple thread to download a file from git hub
 */
public class GHDownloadFilesCall extends CallableWithPaths<GHTreeEntryWrapper, Boolean> {

	public GHDownloadFilesCall() {
		super();
	}

	/**
	 * Starts the download of a given GHTreeEntry into the target directory
	 * which was set with the creation of the thread object.
	 * 
	 * @return True if no exception occurred during the download. False otherwise.
	 */
	@Override
	public Boolean call() {
		GHTreeEntryWrapper ghte = getInput();
		FileOutputStream fos = null;

		try {
			File tar_f = ghte.getOutputPath().toFile();

			// log.info( "Downloading " + ghte.getDownloadURL() + " to " +
			// tar_f);
			URL website = new URL(ghte.getDownloadURL());
			// Misc.out(this, "Thread: " + Thread.currentThread().getId());

			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(tar_f);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			Log.err(this, e, "Error with content " + ghte.getDownloadURL());
			return false;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}

		return true;
	}

	@Override
	public void resetAndInit() {
		//not needed
	}

	public static class Factory implements IDisruptorEventHandlerFactory<GHTreeEntryWrapper> {

		public Factory() {
			super();
		}
		
		@Override
		public Class<? extends DisruptorEventHandler<GHTreeEntryWrapper>> getEventHandlerClass() {
			return GHDownloadFilesCall.class;
		}

		@Override
		public DisruptorEventHandler<GHTreeEntryWrapper> newInstance() {
			return new GHDownloadFilesCall();
		}
	}
	
}
