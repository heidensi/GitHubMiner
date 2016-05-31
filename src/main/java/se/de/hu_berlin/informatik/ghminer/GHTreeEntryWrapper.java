/**
 * 
 */
package se.de.hu_berlin.informatik.ghminer;

import java.nio.file.Path;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;

/**
 * A wrapper class to store the download url and the intended file name for an
 * object from the answer to a search query.
 * 
 * @author Simon
 *
 */
public class GHTreeEntryWrapper {

	// the name of the file in the file system after downloading
	private Path outputPath;
	// the url of the file on the git hub server
	private String downloadURL;

	// we need to work around the malfunctioning url and create our own
	private final static String PREFIX_URL = "https://raw.githubusercontent.com/";

	/**
	 * Builds the download url from the repository data (default branch + full
	 * name) and stores the output path
	 * 
	 * @param entry
	 *            One tree element from a previous search query. This is
	 *            considered to be a regular file and not another tree
	 * @param output
	 *            The absolute name of the file after the download has finished
	 * @param aRepo
	 *            The repository object with data regarding the default branch
	 *            and the owner name
	 */
	public GHTreeEntryWrapper(GHTreeEntry entry, Path output, GHRepository aRepo) {
		super();
		this.outputPath = output;

		String aRepoUser = aRepo.getFullName();
		String aBranch = aRepo.getDefaultBranch();

		String repoOwner = aRepoUser.endsWith("/") ? aRepoUser : aRepoUser + "/";
		String branch = aBranch.endsWith("/") ? aBranch : aBranch + "/";

		downloadURL = PREFIX_URL + repoOwner + branch + entry.getPath();
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

}
