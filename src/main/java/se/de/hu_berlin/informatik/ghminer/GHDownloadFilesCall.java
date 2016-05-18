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

	public GHDownloadFilesCall(IOutputPathGenerator<Path> outputPathGenerator) {
		super(outputPathGenerator);
	}

	public static Logger log = LoggerFactory.getLogger( GHDownloadFilesCall.class );;

	/**
	 * Starts the download of all files from the given collection into the target
	 * directory which were set with the creation of the thread object.
	 */
	@Override
	public Boolean call() throws Exception {
		
		log.info( "Downloading files into " + getOutputPath() );
		GHTreeEntry ghte = (GHTreeEntry) getInput();
		while( ghte != null ) {

			FileOutputStream fos = null;
			
			try {
				
				File tar_f = getOutputPath().toFile();
				
				// to download from this url seems to be forbidden
				log.info( "Downloading " + ghte.getPath() + " from " + ghte.getUrl());
				URL website = ghte.getUrl();
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
		}
		
		return true;
	}
	
}
