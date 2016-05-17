package se.de.hu_berlin.informatik.ghminer;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

public class GHGetRepos {

	private static Logger log = LoggerFactory.getLogger( GHGetRepos.class );
	
	/**
	 * Connects to the git hub and asks for a list of repositories that fulfill the
	 * specified demands
	 * @param aGitHub The git hub to connect to
	 * @param aProps A properties map with parameters for blacklist, language and minimal
	 * 	number of stars a project has to have
	 * @return An iterator with all repositories that the git hub could return
	 */
	public static PagedSearchIterable<GHRepository> findRepos( GitHub aGitHub, OptionParser aOptions ) {
		
		GHRepositorySearchBuilder ghrsb = aGitHub.searchRepositories();
//		 adding the blacklist - default is "NOT android" to avoid downloading android repos
		ghrsb.q( aOptions.getOptionValue( GHOptions.BLACKLIST, GHOptions.DEF_BLACKLIST ) );
		// adding the language with "Java" as its default
		ghrsb.language(  aOptions.getOptionValue( GHOptions.LANG, GHOptions.DEF_LANG ) );
		// restricting the number of stars already reduces the number of repositories greatly
		ghrsb.q( "stars:>=" +  aOptions.getOptionValue( GHOptions.MIN_STARS, GHOptions.DEF_MINSTARS ) );
		// sorting can be done by stars, forked and updated
		ghrsb.sort( GHRepositorySearchBuilder.Sort.STARS );

		PagedSearchIterable<GHRepository> result = ghrsb.list();
		
		if( result.getTotalCount() > 0 ) {
			log.info( "Found " + result.getTotalCount() + " repository that matched the query.");
		} else {
			log.info( "Found no repositories that matched the query..." );
		}
		
		return result;
	}
	
}
