package se.de.hu_berlin.informatik.ghminer;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.ghminer.modules.GHRepoHandlerModule;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ListSequencerPipe;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;

/**
 * A tool for downloading multiple source files from git hub.
 */
public class GitHubMiner {

	private static Logger log = LoggerFactory.getLogger( GitHubMiner.class );
	private OptionParser options = null;
	
	private String FILE_SEP = File.separator;
	
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
	 * the command line arguments
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
			
			findAllFilesInAllRepos( gh, allRepos );
			
			log.info( "Finished downloading of all files" );
		} catch (IOException e) {
			// this is not very specific...
			log.error( "IOException..." , e );
		}
	}
	
	/**
	 * Starts the search for all files that are of interest.
	 * @param aGitHub The git hub object
	 * @param allRepos The paged results for the repositories
	 */
	private void findAllFilesInAllRepos( GitHub aGitHub, 
			PagedSearchIterable<GHRepository> allRepos ) {
		
		String targetDir = options.getOptionValue( GHOptions.OUTPUT_DIR );
		targetDir = targetDir.endsWith( FILE_SEP ) ? targetDir :
			targetDir + FILE_SEP;
		
		int maxDLThreads = Integer.parseInt( options.getOptionValue( GHOptions.MAX_DL_THREADS, GHOptions.DEF_MAX_DL_THREADS ) );
		String extension = options.getOptionValue( GHOptions.EXTENSION, GHOptions.DEF_EXTENSION );
		String bl = options.getOptionValue( GHOptions.BLACKLIST, GHOptions.DEF_BLACKLIST );

		PagedIterator<GHRepository> pi = allRepos.iterator();
		
		File tDir_f = new File ( targetDir );
		if( !tDir_f.exists() ) {
			tDir_f.mkdirs();
		}

		int maxRepos = Integer.parseInt( options.getOptionValue( GHOptions.MAX_REPOS, GHOptions.DEF_MAX_REPOS ) );
		log.info( "Reducing the number of repositories to " + maxRepos );
		
		PipeLinker linker = new PipeLinker().link(
				new GHRepoHandlerModule(aGitHub, targetDir, extension, bl),
				new ListSequencerPipe<List<GHTreeEntryWrapper>,GHTreeEntryWrapper>(),
				new ThreadedProcessorPipe<GHTreeEntryWrapper>(maxDLThreads, GHDownloadFilesCall.class));
		
		while( pi.hasNext() && --maxRepos > -1 ) {
			linker.submit(pi.next());
		}

		log.info( "All repositories submitted. Waiting for shutdown...");
		linker.waitForShutdown();
		log.info( "Finished all downloads");
	}
}
