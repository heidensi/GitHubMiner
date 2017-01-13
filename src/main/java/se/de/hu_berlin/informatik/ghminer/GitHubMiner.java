package se.de.hu_berlin.informatik.ghminer;

import java.io.File;
import java.io.IOException;
import org.kohsuke.github.GitHub;

import se.de.hu_berlin.informatik.ghminer.GHOptions.CmdOptions;
import se.de.hu_berlin.informatik.ghminer.modules.GHRepoHandlerModule;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;
import se.de.hu_berlin.informatik.utils.tm.pipes.ListSequencerPipe;
import se.de.hu_berlin.informatik.utils.tm.pipes.ThreadedProcessorPipe;

/**
 * A tool for downloading multiple source files from git hub. By default this
 * miner looks for the 5000 best rated java projects that do not have the
 * keyword "android" in their description and tries to download all of their
 * .java files from the master branch.
 */
public class GitHubMiner {

	private OptionParser options = null;

	private String FILE_SEP = File.separator;

	/**
	 * The constructor that also inits the parsing of the arguments
	 * 
	 * @param aArgs
	 *            The arguments
	 */
	public GitHubMiner(String[] aArgs) {
		options = OptionParser.getOptions("GitHubMiner", true, CmdOptions.class, aArgs);
	}

	/**
	 * Entry method that starts the tool. If this miner is part of another
	 * program use the method {@link #downloadFromGH() downloadFromGH} after
	 * passing the options to the constructor.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		GitHubMiner ghd = new GitHubMiner(args);
		ghd.downloadFromGH();
	}

	/**
	 * Connects to the git hub and triggers the download of all relevant files.
	 */
	public void downloadFromGH() {
		Log.out(this, "Started the github downloader");
		try {
			String user = options.getOptionValue(CmdOptions.USER);
			String password = options.getOptionValue(CmdOptions.PWD);
			GitHub gh = GHConnectionBuilder.getConnection(user, password);

			Log.out(this, "Connected to git hub with a rate limit of: " + gh.getRateLimit().limit + " per hour");

			findAllFilesInAllRepos(gh);

		} catch (IOException e) {
			// this is not very specific...
			Log.err(this, e, "IOException...");
		}
	}

	/**
	 * Starts the search for all files that are of interest. The output
	 * directory will be created if it does not exist already.
	 * 
	 * @param aGitHub
	 *            The git hub object
	 */
	private void findAllFilesInAllRepos(GitHub aGitHub) {

		String targetDir = options.getOptionValue(CmdOptions.OUTPUT);
		targetDir = targetDir.endsWith(FILE_SEP) ? targetDir : targetDir + FILE_SEP;

		int maxDLThreads = options.getNumberOfThreads();
		String extension = options.getOptionValue(CmdOptions.EXTENSION, GHOptions.DEF_EXTENSION);
		String bl = options.getOptionValue(CmdOptions.BLACKLIST, GHOptions.DEF_BLACKLIST);

		File tDir_f = new File(targetDir);
		if (!tDir_f.exists()) {
			tDir_f.mkdirs();
		}

		int maxRepos = Integer.parseInt(options.getOptionValue(CmdOptions.MAX_REPOS, GHOptions.DEF_MAX_REPOS));
		Log.out(this, "Reducing the number of repositories to " + maxRepos);

		PipeLinker linker = new PipeLinker().append(
				new GHRepoHandlerModule(aGitHub, targetDir, extension, bl),
				new ListSequencerPipe<GHTreeEntryWrapper>(),
				new ThreadedProcessorPipe<GHTreeEntryWrapper, Object>(maxDLThreads, new GHDownloadFilesEH.Factory())
				.enableTracking(50));

		GHGetRepos.findRepos(aGitHub, options, linker);

		Log.out(this, "All repositories submitted. Waiting for shutdown...");
		linker.shutdown();
		Log.out(this, "Finished all downloads");
	}
}
