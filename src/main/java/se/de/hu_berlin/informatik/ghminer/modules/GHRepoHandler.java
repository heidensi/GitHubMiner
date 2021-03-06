/**
 * 
 */
package se.de.hu_berlin.informatik.ghminer.modules;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;

import se.de.hu_berlin.informatik.ghminer.GHTreeEntryWrapper;
import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputPathGenerator;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * The repository handler searches for the source code files in a given set of
 * repositories and analyzes if they meet the desired requirements. In case they
 * do, they are returned in a list.
 * 
 * @author Roy Lieck, Simon Heiden
 */
public class GHRepoHandler extends AbstractProcessor<GHRepository, List<GHTreeEntryWrapper>> {

	private GitHub aGitHub = null;
	private String extension = null;
	// private String bl = "";
	private int repoCounter = 0;
	private String targetDir;
	// 100 is the max and this should be a constant somewhere in this object
	// private final static int MAX_PAGE_SIZE = 100;

	// for parsing the trees
	private final static String TREE_TYPE_TREE = "tree";
	private final static String TREE_TYPE_FILE = "blob";

	/**
	 * @param aGitHub
	 *            The git hub object
	 * @param targetDir
	 *            the main output directory
	 * @param extension
	 *            an extension that should be matching with downloaded files
	 * @param bl
	 *            blacklist option argument
	 */
	public GHRepoHandler(GitHub aGitHub, String targetDir, String extension, String bl) {
		super();
		this.extension = extension;
		// this.bl = bl;
		this.aGitHub = aGitHub;
		this.targetDir = targetDir;
	}

	@Override
	public List<GHTreeEntryWrapper> processItem(GHRepository repo) {
		return handleRepo(aGitHub, repo);
	}

	/**
	 * Inspects the given repository object
	 * 
	 * @param aGitHub
	 *            The git hub object
	 * @param aRepo
	 *            A git hub repository object
	 * @return A collection containing all results that match the filter (or
	 *         null in the case of an error or no found files)
	 */
	private List<GHTreeEntryWrapper> handleRepo(GitHub aGitHub, GHRepository aRepo) {
		Log.out(GHRepoHandler.class, "Handling " + ++repoCounter + ". repository object: " + aRepo.getFullName() + " / "
				+ aRepo.getSvnUrl());

		// // because there is no reset for the query the searcher has to be
		// recreated for each repo
		// GHContentSearchBuilder ghcsb = aGitHub.searchContent();
		// ghcsb.repo( aRepo.getFullName() );
		// if( extension != null ) {
		// ghcsb.extension( extension );
		// }
		//
		// PagedSearchIterable<GHContent> psi = ghcsb.list();
		// psi.withPageSize( MAX_PAGE_SIZE );
		// int allMatchedFiles = psi.getTotalCount();
		//
		// log.info( "Found " + allMatchedFiles + " matching files to download
		// in repository " + aRepo.getName() );
		//
		// // check if the black list parameter
		// ghcsb.q( bl );
		//
		// psi = ghcsb.list();
		// psi.withPageSize( MAX_PAGE_SIZE );
		// int notBlacklistedFiles = psi.getTotalCount();
		//
		// log.info( "Found " + notBlacklistedFiles + " valid files (invalid:" +
		// (allMatchedFiles - notBlacklistedFiles) + ") to download in
		// repository " + aRepo.getName() );
		//
		// // abort if there are no matching files
		// if( allMatchedFiles == 0 ) {
		// return null;
		// }
		//
		// // check if the ratio is good enough to download the valid files
		// // TODO rework this maybe add an option for it or remove it
		// completely
		// if( (double) notBlacklistedFiles / (double) allMatchedFiles < 0.8 ) {
		// log.info( "The ratio from valid to invalid files was too bad and no
		// files will be downloaded from " +
		// aRepo.getFullName() );
		// return null;
		// }

		return handleRepoAsTree(aRepo);
	}

	/**
	 * Tries to get all files from the repository by accessing it using the tree
	 * data structure.
	 * 
	 * @param aRepo
	 *            The repository object
	 * @return A collection containing all results that match the filter (or
	 *         null in the case of an error)
	 */
	private List<GHTreeEntryWrapper> handleRepoAsTree(GHRepository aRepo) {
		Path repOutput = Paths.get(targetDir,
				aRepo.getFullName().replace("/", "_").replace("\\", "_").replace(".", "_"));
		IOutputPathGenerator<Path> generator = new OutputPathGenerator(repOutput, extension, true);

		try {
			List<GHTreeEntryWrapper> allEntries = new LinkedList<GHTreeEntryWrapper>();
			findAllFilesInTree(allEntries, aRepo.getTreeRecursive(aRepo.getDefaultBranch(), 1), aRepo, generator);

			Log.out(GHRepoHandler.class, "Found " + allEntries.size() + " matching files in repository " + aRepo.getName());
			return allEntries;
		} catch (IOException e) {
			Log.err(GHRepoHandler.class, "IOException during retrieving rescursive tree", e);
		}
		return null;
	}

	/**
	 * Searches for all files that match the given extension in the given tree
	 * and adds them to the collection. If a tree is truncated more queries are
	 * send.
	 * 
	 * @param aAllEntries
	 *            A collection containing all results that match the filter
	 * @param aTree
	 *            The tree object
	 * @param aRepo
	 *            The repository object
	 */
	private void findAllFilesInTree(Collection<GHTreeEntryWrapper> aAllEntries, GHTree aTree, GHRepository aRepo,
			IOutputPathGenerator<Path> generator) {
		if (aTree.isTruncated()) {
			Log.out(GHRepoHandler.class, "The tree " + aTree.getUrl() + " was truncated and will be reduced" + " to a normal tree");
			try {
				// switch to normal tree handling
				findAllFilesInNormalTree(aAllEntries, aRepo.getTree(aTree.getSha()), aRepo, generator);
			} catch (IOException e) {
				Log.err(GHRepoHandler.class, "IOException while parsing tree for " + aRepo.getName(), e);
			}
		} else {

			for (GHTreeEntry node : aTree.getTree()) {
				if (node.getType().equals(TREE_TYPE_FILE)) {
					// add it if it fits the extension
					if (node.getPath().endsWith(extension)) {
						aAllEntries.add(new GHTreeEntryWrapper(node, generator.getNewOutputPath(), aRepo));
					}
				}
			}
		}
	}

	/**
	 * Searches for all files that match the given extension in the given tree
	 * and adds them to the collection. If a tree is truncated more queries are
	 * send. This method is different to the normal findAllFilesMixedMode method
	 * because it does not ignore subtrees because it is no recursive tree.
	 * 
	 * @param aAllEntries
	 *            A collection containing all results that match the filter
	 * @param aTree
	 *            The tree object
	 * @param aRepo
	 *            The repository object
	 */
	private void findAllFilesInNormalTree(Collection<GHTreeEntryWrapper> aAllEntries, GHTree aTree, GHRepository aRepo,
			IOutputPathGenerator<Path> generator) {

		// a normal tree is never truncated and therefore needs no check

		for (GHTreeEntry node : aTree.getTree()) {
			if (node.getType().equals(TREE_TYPE_FILE)) {
				// add it if it fits the extension
				if (node.getPath().endsWith(extension)) {
					aAllEntries.add(new GHTreeEntryWrapper(node, generator.getNewOutputPath(), aRepo));
				}
			} else if (node.getType().equals(TREE_TYPE_TREE)) {
				try {
					// assume it is a non truncated recursive tree and switch if
					// proven wrong
					findAllFilesInTree(aAllEntries, aRepo.getTree(node.getSha()), aRepo, generator);
				} catch (IOException e) {
					Log.err(GHRepoHandler.class, "IOException while parsing tree for " + aRepo.getName(), e);
				}
			}
		}
	}
}
