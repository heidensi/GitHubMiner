package se.de.hu_berlin.informatik.ghminer;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;

/**
 * The options for the git hub downloader are defined in this class as well as
 * the default values.
 */
public class GHOptions {

	public final static String OUTPUT_DIR = "o";
	public final static String LANG = "l";
	public final static String USER = "u";
	public final static String PWD = "p";
	public final static String BLACKLIST = "bl";
	public final static String EXTENSION = "ext";
	public final static String MIN_STARS = "min";
	public final static String MAX_REPOS = "mr";
	public final static String MAX_DL_THREADS = "maxDLT";

	// Default values for the search queries
	public final static String DEF_LANG = "Java";
	public final static String DEF_EXTENSION = ".java";
	public final static String DEF_BLACKLIST = "NOT android";
	public final static String DEF_MINSTARS = "50";
	public final static String DEF_MAX_REPOS = "5000"; // has to be convertable
														// to an integer
	public final static String DEF_MAX_DL_THREADS = "20";

	// private static Logger log = LoggerFactory.getLogger( "OptionParser" );

	/**
	 * Parses the options from the command line.
	 * 
	 * @param args
	 *            the application's arguments
	 * @return an {@link OptionParser} object that provides access to all parsed
	 *         options and their values or their default values if no specific values
	 *         were set.
	 */
	public static OptionParser getOptions(String[] args) {
		final String tool_usage = "GitHubMiner";
		final OptionParser options = new OptionParser(tool_usage, args);

		options.add(OUTPUT_DIR, "output", true, "Path to output directory with all downloaded files.", true);
		options.add(USER, "user", true, "The user name to authenticate against GitHub. "
				+ " Should be provided together with a password at least once to generate an authentication token.");
		options.add(PWD, "password", true, "The password to authenticate against GitHub. "
				+ "Should be provided toether with a user name at least once to generate an authentication token.");
		options.add(BLACKLIST, "blacklist", true, "The blacklist to filter unwanted repositories. Use it like "
				+ "\"bl=" + DEF_BLACKLIST + "\" (which is also the default).", false);
		options.add(EXTENSION, "extensions", true,
				"The filter to only download files with a certain suffix. Default is \"" + DEF_EXTENSION + "\"", false);
		options.add(LANG, "language", true, "The language of the mined repositories. Default is " + DEF_LANG + ".",
				false);
		options.add(MIN_STARS, "minimalStars", true,
				"The minimum of stars that a project has to have. Default is " + DEF_MINSTARS + ".", false);
		options.add(MAX_REPOS, "maxNumberOfRepos", true,
				"The maximum number of repositories that will be used. Default is " + DEF_MAX_REPOS + ".", false);
		options.add(MAX_DL_THREADS, "maxNumberOfDLThreads", true,
				"The maximum number of download threads that will be used. Default is " + DEF_MAX_DL_THREADS + ".",
				false);

		options.parseCommandLine();

		// printReadOptions( options );

		return options;
	}

	// /**
	// * Just to see that the option handling was successful
	// * @param aOP An option parser object after the parsing of the command
	// line
	// */
	// private static void printReadOptions( OptionParser aOP ) {
	// log.info( "Found option " + OUTPUT_DIR + " " + aOP.getOptionValue(
	// OUTPUT_DIR ));
	// log.info( "Found option " + USER + " " + aOP.getOptionValue( USER ));
	// log.info( "Found option " + PWD + " *********");
	//
	// printOptionStatus( BLACKLIST, aOP );
	// printOptionStatus( LANG, aOP );
	// printOptionStatus( EXTENSION, aOP );
	// printOptionStatus( MIN_STARS, aOP );
	// printOptionStatus( MAX_REPOS, aOP );
	// }
	//
	// /**
	// * Prints if the option was set or not
	// * @param aOption The key for the option in the parser object
	// * @param aOP The option parser object
	// */
	// private static void printOptionStatus( String aOption, OptionParser aOP )
	// {
	// if( aOP.hasOption( aOption ) ) {
	// log.info( "Found option " + aOption + " " + aOP.getOptionValue( aOption
	// ));
	// } else {
	// log.info( "No option was set for " + aOption );
	// }
	// }

}
