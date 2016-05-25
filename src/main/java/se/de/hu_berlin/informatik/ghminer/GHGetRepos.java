package se.de.hu_berlin.informatik.ghminer;

import java.util.List;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.tm.pipeframework.PipeLinker;

public class GHGetRepos {

	private static Logger log = LoggerFactory.getLogger( GHGetRepos.class );
	public final static int NO_UPPER_BOUND = -1;
	private final static int MAX_RESULTS_PER_QUERY = 1000;
	
	/**
	 * Connects to the git hub and asks for a list of repositories that fulfill the
	 * specified demands
	 * @param aGitHub The git hub to connect to
	 * @param aOptions The parsed options
	 * @param PipeLinker the linker that will use all found repos as input
	 * @return An iterator with all repositories that the git hub could return
	 */
	public static void findRepos( GitHub aGitHub, OptionParser aOptions, PipeLinker aLinker ) {
		findRepos( aGitHub, aOptions, aLinker, NO_UPPER_BOUND );
	}
	
	/**
	 * Connects to the git hub and asks for a list of repositories that fulfill the
	 * specified demands
	 * @param aGitHub The git hub to connect to
	 * @param aOptions The parsed options
	 * @param PipeLinker the linker that will use all found repos as input
	 * @param aMaxStars The upper bound for result splitting
	 * @return An iterator with all repositories that the git hub could return
	 */
	public static void findRepos( GitHub aGitHub, OptionParser aOptions,
			PipeLinker aLinker, int aMaxStars ) {
		
		GHRepositorySearchBuilder ghrsb = aGitHub.searchRepositories();
		// adding the blacklist - default is "NOT android" to avoid downloading android repos
		ghrsb.q( aOptions.getOptionValue( GHOptions.BLACKLIST, GHOptions.DEF_BLACKLIST ) );
		// adding the language with "Java" as its default
		ghrsb.language(  aOptions.getOptionValue( GHOptions.LANG, GHOptions.DEF_LANG ) );
		// restricting the number of stars already reduces the number of repositories greatly
		if( aMaxStars == NO_UPPER_BOUND ) {
			ghrsb.q( "stars:>=" +  aOptions.getOptionValue( GHOptions.MIN_STARS, GHOptions.DEF_MINSTARS ) );
		} else {
			// using the interval 
			ghrsb.q( "stars:\"" +  aOptions.getOptionValue( GHOptions.MIN_STARS, GHOptions.DEF_MINSTARS ) + 
					" .. " + aMaxStars + "\"" );
		}
		
		// sorting can be done by stars, forked and updated
		ghrsb.sort( GHRepositorySearchBuilder.Sort.STARS );

		List<GHRepository> result = ghrsb.list().asList();
		log.info( "Found " + result.size() + " repository that matched the query.");
		
		if( result.size() >= MAX_RESULTS_PER_QUERY ) {
			// we need to split the result at the stars value of the last entries
			// to not lose or duplicate repositories with identical stars the last value
			// will only be used by the next call
			int lastStarsValue = result.get(MAX_RESULTS_PER_QUERY - 1 ).getWatchers();
			for( GHRepository ghr : result ) {
				if( ghr.getWatchers() > lastStarsValue ) {
					aLinker.submit( ghr );
				}
			}
			
			// request the next 1000 repos
			log.info( "Asking for more repositories..." );
			findRepos( aGitHub, aOptions, aLinker, lastStarsValue );
		} else {
			// submit all of them
			for( GHRepository ghr : result ) {
				aLinker.submit(ghr);
			}
		}
	}	
}