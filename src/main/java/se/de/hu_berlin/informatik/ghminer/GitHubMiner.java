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
public class GitHubMiner {

	private static Logger log = LoggerFactory.getLogger( GitHubMiner.class );
	private OptionParser options = null;
	
	/**
	 * Just the constructor
	 * @param aArgs The arguments
	 */
	public GitHubMiner( String[] aArgs ) {		
		options = GHOptions.getOptions( aArgs );
	}
	
	/**
	 * Entry method
	 * @param args
	 */
	public static void main(String[] args) {
		GitHubMiner ghd = new GitHubMiner( args );
		ghd.downloadFromGH();
	}
	
	/**
	 * Connects to the git hub and triggers the download of all relevant files
	 */
	public void downloadFromGH() {
		log.debug( "Started the github downloader" );
		try {
			String user = options.getOptionValue( GHOptions.USER );
			String password = options.getOptionValue(GHOptions.PWD );
			GitHub gh = GHConnectionBuilder.getConnection( user, password );
			
			log.info( "Connected to git hub with a rate limit of: " + 
					gh.getRateLimit().limit + " per hour" );
			
			PagedSearchIterable<GHRepository> allRepos = GHGetRepos.findRepos( gh, options );
			GHRepoHandler ghrh = new GHRepoHandler( options );
			ghrh.findAllFilesInAllRepos( gh, options, allRepos );
			
			log.info( "Finished downloading of all files" );
		} catch (IOException e) {
			// this is not very specific...
			log.error( "IOException..." , e );
		}
	}
}
