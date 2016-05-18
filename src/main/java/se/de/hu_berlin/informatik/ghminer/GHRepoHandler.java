package se.de.hu_berlin.informatik.ghminer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHContentSearchBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputPathGenerator;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.threadwalker.ExecutorServiceProvider;
import se.de.hu_berlin.informatik.utils.tm.modules.ThreadedListProcessorModule;

/**
 * The repository handler searches for the source code files in a given
 * set of repositories and analyzes if they meat the desired requirements.
 * In case they do download threads will be triggered that store the files
 * in the target directory.
 */
public class GHRepoHandler {

	private Logger log = LoggerFactory.getLogger( GHRepoHandler.class );
	private String extension = null;
	private String bl = "";
	private String targetDir = "";
	private String FILE_SEP = System.getProperty( "file.separator" );
	private int repoCounter = 0;
	private int maxDLThreads = 20;
	// 100 is the max and this should be a constant somewhere in this object
	private final static int MAX_PAGE_SIZE = 100;
	
	// for parsing the trees
	private final static String TREE_TYPE_TREE = "tree";
	private final static String TREE_TYPE_FILE = "blob";
	
	private ExecutorServiceProvider executor = null;
	
	/**
	 * Constructor
	 * @param aOptions all options
	 */
	public GHRepoHandler( OptionParser aOptions ) {		
		extension = aOptions.getOptionValue( GHOptions.EXTENSION, GHOptions.DEF_EXTENSION );
		bl = aOptions.getOptionValue( GHOptions.BLACKLIST, GHOptions.DEF_BLACKLIST );
		
		maxDLThreads = Integer.parseInt( aOptions.getOptionValue( GHOptions.MAX_DL_THREADS, GHOptions.DEF_MAX_DL_THREADS ) );
		
		// these threads will be needed for downloading the files
		executor = new ExecutorServiceProvider(maxDLThreads);
		
		targetDir = aOptions.getOptionValue( GHOptions.OUTPUT_DIR );
		targetDir = targetDir.endsWith( FILE_SEP ) ? targetDir :
			targetDir + FILE_SEP;
	}
	
	/**
	 * Starts the search for all files that are of interest.
	 * @param aGitHub The git hub object
	 * @param aOptions The arguments including credentials
	 * @param aPsi The paged results for the repositories
	 */
	public void findAllFilesInAllRepos( GitHub aGitHub, OptionParser aOptions, 
			PagedSearchIterable<GHRepository> aPsi ) {
		
		PagedIterator<GHRepository> pi = aPsi.iterator();
		
		File tDir_f = new File ( targetDir );
		if( !tDir_f.exists() ) {
			tDir_f.mkdirs();
		}

		int maxHandles = Integer.parseInt( aOptions.getOptionValue( GHOptions.MAX_REPOS, GHOptions.DEF_MAX_REPOS ) );
		log.info( "Reducing the number of repositories to " + maxHandles );
		
		while( pi.hasNext() && --maxHandles > -1 ) {
			handleRepo( aGitHub, pi.next() );
		}

		//we are done! Shutdown of the executor service is necessary! (That means: No new task submissions!)
		log.info( "Finished collecting all files. Waiting for download threads to complete");
		executor.shutdownAndWaitForTermination();
		log.info( "Finished all downloads");
	}
	
	/**
	 * Inspects the given repository object
	 * @param aGitHub The git hub object
	 * @param aRepo A repository from the iterator
	 */
	private void handleRepo( GitHub aGitHub, GHRepository aRepo ) {
		log.info( "Handling " + ++repoCounter + " repository object " + 
				aRepo.getFullName() + " / " + aRepo.getSvnUrl() );
		
		// because there is no reset for the query the searcher has to be recreated for each repo
		GHContentSearchBuilder ghcsb = aGitHub.searchContent();
		ghcsb.repo( aRepo.getFullName() );
		if( extension != null ) {
			ghcsb.extension( extension );
		}
		
		PagedSearchIterable<GHContent> psi = ghcsb.list();
		psi.withPageSize( MAX_PAGE_SIZE );
		int numberUnfilteredFiles = psi.getTotalCount();
		
		// check if the black list parameter
		ghcsb.q( bl );
		
		psi = ghcsb.list();
		psi.withPageSize( MAX_PAGE_SIZE );
		int numberFilteredFiles = psi.getTotalCount();
		
		log.info( "Found " + numberFilteredFiles + " valid files (invalid:" + 
				(numberUnfilteredFiles - numberFilteredFiles) + ") to download in repo " + aRepo.getName() );
		
		// abort if there are no files
		if( numberFilteredFiles == 0 ) {
			return;
		}
		
		// check if the ratio is good enough to download the valid files
		// TODO rework this maybe add an option for it or remove it completely
		if( (double) numberFilteredFiles / (double) numberUnfilteredFiles < 0.8 ) {
			log.info( "The ratio from valid to invalid files was to bad and no files will be downloaded from " +
						aRepo.getFullName() );
			return;
		}

		handleRepoAsTree( aRepo );
	}
	
	/**
	 * Tries to get all files from the repository by accessing it using the tree data structure.
	 * @param aRootDirName The target directory for all files
	 * @param aRepo The repository object
	 */
	private void handleRepoAsTree( GHRepository aRepo ) {
		try {
			List<GHTreeEntry> allEntries = new ArrayList<GHTreeEntry>();
			findAllFilesInTree( allEntries, aRepo.getTreeRecursive( aRepo.getDefaultBranch(), 1 ), aRepo );
			
			log.info( "Found " + allEntries.size() + " files while parsing the tree for " +
						"repo " + aRepo.getName() );
			
			// pack all files and make them persistent
			Path repOutput = Paths.get( targetDir, aRepo.getFullName().replace( "/" , "_").replace( "\\", "_") );
			IOutputPathGenerator<Path> generator = new OutputPathGenerator(repOutput, extension, true);
			
			new ThreadedListProcessorModule<Object>(executor.getExecutorService(), GHDownloadFilesCall.class,
					generator, aRepo.getFullName(), aRepo.getDefaultBranch() ).submitAndStart(allEntries);
	
		} catch (IOException e) {
			log.error( "IOException during retrieving rescursive tree", e );
		}
	}
	
	/**
	 * Searches for all files that match the given extension in the given tree
	 * and adds them to the collection. If a tree is truncated more queries are
	 * send.
	 * @param aAllEntries A collection containing all results that match the filter
	 * @param aTree The tree object
	 * @param aRepo The repository object
	 */
	private void findAllFilesInTree( Collection<GHTreeEntry> aAllEntries, GHTree aTree, GHRepository aRepo ) {
		if( aTree.isTruncated() ) {
			log.info( "The tree " + aTree.getUrl() + " was truncated and will be reduced" +
						" to a normal tree" );
			try {
				// switch to normal tree handling
				findAllFilesInNormalTree( aAllEntries, aRepo.getTree( aTree.getSha() ), aRepo);
			} catch (IOException e) {
				log.error( "IOException while parsing tree for " + aRepo.getName(), e );
			}
		} else {
		
			for( GHTreeEntry node : aTree.getTree() ) {
				if( node.getType().equals( TREE_TYPE_FILE ) ) {
					// add it if it fits the extension
					if( node.getPath().endsWith( extension ) ) {
						aAllEntries.add( node );
					}
				}
			}
		}
	}
	
	/**
	 * Searches for all files that match the given extension in the given tree
	 * and adds them to the collection. If a tree is truncated more queries are
	 * send.
	 * This method is different to the normal findAllFilesMixedMode method because
	 * it does not ignore subtrees because it is no recursive tree.
	 * @param aAllEntries A collection containing all results that match the filter
	 * @param aTree The tree object
	 * @param aRepo The repository object
	 */
	private void findAllFilesInNormalTree( Collection<GHTreeEntry> aAllEntries, GHTree aTree, GHRepository aRepo ) {
		
		// a normal tree is never truncated and therefore needs no check
		
		for( GHTreeEntry node : aTree.getTree() ) {
			if( node.getType().equals( TREE_TYPE_FILE ) ) {
				// add it if it fits the extension
				if( node.getPath().endsWith( extension ) ) {
					aAllEntries.add( node );
				}
			} else if ( node.getType().equals( TREE_TYPE_TREE ) ) {
				try {
					// assume it is a non truncated recursive tree and switch if proven wrong
					findAllFilesInTree( aAllEntries, aRepo.getTree( node.getSha() ), aRepo );
				} catch (IOException e) {
					log.error( "IOException while parsing tree for " + aRepo.getName(), e );
				}
			}
		}
	}
}
