package se.de.hu_berlin.informatik.ghminer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.threaded.CallableWithPaths;

/**
 * A simple thread to download a set of files from git hub
 */
public class GHDownloadFilesCall extends CallableWithPaths<GHTreeEntryWrapper,Boolean> {

	
	public GHDownloadFilesCall() {
		super();
	}

	public Logger log = LoggerFactory.getLogger( GHDownloadFilesCall.class );

	/**
	 * Starts the download of a given GHTreeEntry into the target
	 * directory which was set with the creation of the thread object.
	 */
	@Override
	public Boolean call() {
		System.out.print(".");
		
		GHTreeEntryWrapper ghte = getInput();
		FileOutputStream fos = null;
		
		try {
			File tar_f = ghte.getOutputPath().toFile();
			
//			log.info( "Downloading " + ghte.getDownloadURL() + " to " + tar_f);
			URL website = new URL( ghte.getDownloadURL() );
//			Misc.out(this, "Thread: " + Thread.currentThread().getId());
			
			ReadableByteChannel rbc = Channels.newChannel( website.openStream() );
			fos = new FileOutputStream( tar_f );
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);	
		} catch (IOException e) {
			log.error( "Error with content " + ghte.getDownloadURL(), e );
			return false;
		} finally {
			if( fos != null ) {
				try {
					fos.close();
				} catch (IOException e) {
					// nothing to do
				}
			}
		}
		
		return true;
	}
	
}
