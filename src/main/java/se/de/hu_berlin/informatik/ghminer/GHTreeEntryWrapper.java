/**
 * 
 */
package se.de.hu_berlin.informatik.ghminer;

import java.nio.file.Path;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;

/**
 * @author Simon
 *
 */
public class GHTreeEntryWrapper {

	private Path outputPath;
	private String downloadURL;
	
	// we need to work around the malfunctioning url and create our own
	private final static String PREFIX_URL = "https://raw.githubusercontent.com/";
	
	public GHTreeEntryWrapper(GHTreeEntry entry, Path output, GHRepository aRepo) {
		super();
		this.outputPath = output;
		
		String aRepoUser = aRepo.getFullName();
		String aBranch = aRepo.getDefaultBranch();
		
		String repoOwner = aRepoUser.endsWith( "/" ) ? aRepoUser : aRepoUser + "/";
		String branch = aBranch.endsWith( "/" ) ? aBranch : aBranch + "/";
		
		downloadURL = PREFIX_URL + repoOwner + branch + entry.getPath();
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public String getDownloadURL() {
		return downloadURL;
	}
	
}
