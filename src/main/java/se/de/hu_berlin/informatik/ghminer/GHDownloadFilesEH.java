package se.de.hu_berlin.informatik.ghminer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.EHWithInputAndReturnFactory;

/**
 * A simple thread to download a file from git hub
 */
public class GHDownloadFilesEH extends EHWithInputAndReturn<GHTreeEntryWrapper, Object> {

	/**
	 * Starts the download of a given GHTreeEntry into the target directory
	 * which was set with the creation of the thread object.
	 * @return 
	 * {@code null}
	 */
	@Override
	public Object processInput(GHTreeEntryWrapper input) {
		FileOutputStream fos = null;

		try {
			File tar_f = input.getOutputPath().toFile();

			// Log.out(this, "Downloading " + ghte.getDownloadURL() + " to " + tar_f);
			URL website = new URL(input.getDownloadURL());
			// Log.out(this, "Thread: " + Thread.currentThread().getId());

			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			fos = new FileOutputStream(tar_f);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			Log.err(this, e, "Error with content " + input.getDownloadURL());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void resetAndInit() {
		//not needed
	}

	public static class Factory extends EHWithInputAndReturnFactory<GHTreeEntryWrapper,Object> {
		
		public Factory() {
			super(GHDownloadFilesEH.class);
		}

		@Override
		public EHWithInputAndReturn<GHTreeEntryWrapper, Object> newFreshInstance() {
			return new GHDownloadFilesEH();
		}
	}
	
}
