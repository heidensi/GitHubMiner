package se.de.hu_berlin.informatik.ghminer;
import java.io.IOException;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * A tool for downloading multiple source files from git hub.
 */
public class GitHubDownloader {

	private static Logger log = LoggerFactory.getLogger( GitHubDownloader.class );
	private OptionParser options = null;
	
	/**
	 * Just the constructor
	 * @param aArgs The arguments
	 */
	public GitHubDownloader( String[] aArgs ) {		
		options = GHOptions.getOptions( aArgs );
	}
	
	/**
	 * Entry method
	 * @param args
	 */
	public static void main(String[] args) {
		GitHubDownloader ghd = new GitHubDownloader( args );
		ghd.downloadFromGH();
	}
	
	/**
	 * Connects to the git hub and triggers the download of all relevant files
	 */
	public void downloadFromGH() {
		log.debug( "Started the github downloader" );
		try {
			String user = options.getOptionValue( GHOptions.USER);
			String password = options.getOptionValue(GHOptions.PWD);
			GitHub gh = GitHub.connectUsingPassword( user, password );
			
			log.info( "Connected to git hub with a rate limit of: " + 
					gh.getRateLimit().limit + " per hour" );
			
			PagedSearchIterable<GHRepository> allRepos = GHGetRepos.findRepos( gh, options );
			GHRepoHandler ghrh = new GHRepoHandler( options );
			ghrh.findAllFilesInAllRepos( gh, options, allRepos );
			
			log.info( "Finished downloading of all files" );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
