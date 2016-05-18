package se.de.hu_berlin.informatik.ghminer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.threadwalker.CallableWithPaths;

/**
 * A simple thread to download a set of files from git hub
 */
public class GHDownloadFilesCall extends CallableWithPaths<Object,Boolean> {

	// we need to work around the malfunctioning url and create our own
	private final static String PREFIX_URL = "https://raw.githubusercontent.com/";
	private String branch = "master";
	private String repoOwner = "";
	
	
	public GHDownloadFilesCall(IOutputPathGenerator<Path> outputPathGenerator, String aRepoUser, String aBranch) {
		super(outputPathGenerator);
		repoOwner = aRepoUser.endsWith( "/" ) ? aRepoUser : aRepoUser + "/";
		branch = aBranch.endsWith( "/" ) ? aBranch : aBranch + "/";
	}

	public Logger log = LoggerFactory.getLogger( GHDownloadFilesCall.class );;

	/**
	 * Starts the download of all files from the given collection into the target
	 * directory which were set with the creation of the thread object.
	 */
	@Override
	public Boolean call() throws Exception {
		
		String downloadURLRoot = PREFIX_URL + repoOwner + branch;
		String downloadURL = "";

		GHTreeEntry ghte = (GHTreeEntry) getInput();
		FileOutputStream fos = null;
		
		try {
			File tar_f = getOutputPath().toFile();
			
			downloadURL = downloadURLRoot + ghte.getPath();
			log.info( "Downloading " + downloadURL + " to " + tar_f);
			URL website = new URL( downloadURL );
			ReadableByteChannel rbc = Channels.newChannel( website.openStream() );
			fos = new FileOutputStream( tar_f );
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);	
		} catch (IOException e) {
			log.error( "Error with content " + ghte.getPath(), e );
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
		
		ghte = (GHTreeEntry) getInput();
		
		return true;
	}
	
}
