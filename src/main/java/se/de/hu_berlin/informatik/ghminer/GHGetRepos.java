package se.de.hu_berlin.informatik.ghminer;

import java.util.HashSet;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.ghminer.GHOptions.CmdOptions;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;

/**
 * This class sends queries to the git hub api asking for repositories that
 * match the given options. Each repository is passed to a thread that downloads
 * all valid files within the repository.
 * 
 * @author rlieck
 *
 */
public class GHGetRepos {

	private static Logger log = LoggerFactory.getLogger(GHGetRepos.class);
	public final static int NO_UPPER_BOUND = -1;
	// this is a restriction from git hub and will be used to trigger the
	// loading
	// of the next results
	private final static int MAX_RESULTS_PER_QUERY = 1000;
	// git hub does not send more than 100 results per page
	private final static int MAX_PAGE_SIZE = 100;

	// after all results from one request are processed the upper limit
	// of stars will be increased to catch repositories which watcher numbers
	// are affected by current changes (increase of own or decrease of other)
	private final static int STARS_BUFFER = 10;

	// this set is used to make sure that no duplicates are downloaded
	private static HashSet<String> alreadyReadRepos = new HashSet<String>();
	// the overall number of repositories that were already processed
	// this number should not be greater than the option maxRepos
	private static int reposProcessed = 0;
	// the value from the options which is a limit on how many repositories
	// should be downloaded at most
	private static int maxRepos = 0;

	/**
	 * Connects to the git hub and asks for a list of repositories that fulfill
	 * the specified demands
	 * 
	 * @param aGitHub
	 *            The git hub to connect to
	 * @param aOptions
	 *            The parsed options
	 * @param aLinker
	 *            the linker that will use all found repos as input
	 */
	public static void findRepos(GitHub aGitHub, OptionParser aOptions, PipeLinker aLinker) {
		reposProcessed = 0;
		maxRepos = Integer.parseInt(aOptions.getOptionValue(CmdOptions.MAX_REPOS, GHOptions.DEF_MAX_REPOS));
		alreadyReadRepos.clear();

		findRepos(aGitHub, aOptions, aLinker, NO_UPPER_BOUND);
	}

	/**
	 * Connects to the git hub and asks for a list of repositories that fulfill
	 * the specified demands. This method uses an additional filter for the
	 * maximum number of stars the split the result sets. If the upper bound is
	 * -1 no limit is used.
	 * 
	 * @param aGitHub
	 *            The git hub to connect to
	 * @param aOptions
	 *            The parsed options
	 * @param aLinker
	 *            the linker that will use all found repos as input
	 * @param aMaxStars
	 *            The upper bound for result splitting. If this is -1 it will
	 *            not be used.
	 */
	public static void findRepos(GitHub aGitHub, OptionParser aOptions, PipeLinker aLinker, int aMaxStars) {

		GHRepositorySearchBuilder ghrsb = aGitHub.searchRepositories();
		// adding the blacklist - default is "NOT android" to avoid downloading
		// android repos
		ghrsb.q(aOptions.getOptionValue(CmdOptions.BLACKLIST, GHOptions.DEF_BLACKLIST));
		// adding the language with "Java" as its default
		ghrsb.language(aOptions.getOptionValue(CmdOptions.LANG, GHOptions.DEF_LANG));
		// restricting the number of stars already reduces the number of
		// repositories greatly
		if (aMaxStars == NO_UPPER_BOUND) {
			ghrsb.q("stars:>=" + aOptions.getOptionValue(CmdOptions.MIN_STARS, GHOptions.DEF_MINSTARS));
		} else {
			// using the interval
			ghrsb.q("stars:\"" + aOptions.getOptionValue(CmdOptions.MIN_STARS, GHOptions.DEF_MINSTARS) + " .. "
					+ aMaxStars + "\"");
		}

		// sorting can be done by stars, forked and updated
		ghrsb.sort(GHRepositorySearchBuilder.Sort.STARS);

		PagedSearchIterable<GHRepository> result_it = ghrsb.list();
		result_it.withPageSize(MAX_PAGE_SIZE);
		int totalCount = result_it.getTotalCount();
		log.info("Found " + totalCount + " repositories that matched the query.");

		PagedIterator<GHRepository> pi_it = result_it.iterator();
		GHRepository repo = null;

		while (pi_it.hasNext()) {
			repo = pi_it.next();

			if (reposProcessed < maxRepos) {
				if (!alreadyReadRepos.contains(repo.getFullName())) {
					alreadyReadRepos.add(repo.getFullName());
					aLinker.submit(repo);
					reposProcessed++;
				}
			} else {
				// we already processed as much repositories as necessary and
				// can return
				return;
			}
		}

		// we finished the processing of some repositories and may need to ask
		// for more
		// check the stars of the last repo that was handled
		if (totalCount >= MAX_RESULTS_PER_QUERY && repo != null) {
			int upperBound = repo.getWatchers() + STARS_BUFFER;
			log.info("Asking for more repositories. This time with an upper bound of " + upperBound);
			findRepos(aGitHub, aOptions, aLinker, upperBound);
		}
	}
}