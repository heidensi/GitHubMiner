package se.de.hu_berlin.informatik.ghminer;

import org.apache.commons.cli.Option;

import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapperInterface;
import se.de.hu_berlin.informatik.utils.optionparser.OptionWrapper;

/**
 * The options for the git hub downloader are defined in this class as well as
 * the default values.
 */
public class GHOptions {

	// Default values for the search queries
	public final static String DEF_LANG = "Java";
	public final static String DEF_EXTENSION = ".java";
	public final static String DEF_BLACKLIST = "NOT android";
	public final static String DEF_MINSTARS = "50";
	public final static String DEF_MAX_REPOS = "5000"; // has to be convertable
														// to an integer
	public static enum CmdOptions implements OptionWrapperInterface {
		/* add options here according to your needs */
		OUTPUT("o", "output", true, "Path to output directory with all downloaded files.", true),
		USER("u", "user", true, "The user name to authenticate against GitHub. "
				+ " Should be provided together with a password at least once to generate an authentication token.", false),
		PWD("p", "password", true, "The password to authenticate against GitHub. "
				+ "Should be provided toether with a user name at least once to generate an authentication token.", false),
		BLACKLIST("bl", "blacklist", true, "The blacklist to filter unwanted repositories. Use it like "
				+ "\"bl=" + DEF_BLACKLIST + "\" (which is also the default).", false),
		EXTENSION("ext", "extensions", true,
				"The filter to only download files with a certain suffix. Default is \"" + DEF_EXTENSION + "\"", false),
		LANG("l", "language", true, "The language of the mined repositories. Default is " + DEF_LANG + ".",
				false),
		MIN_STARS("min", "minimalStars", true,
				"The minimum of stars that a project has to have. Default is " + DEF_MINSTARS + ".", false),
		MAX_REPOS("mr", "maxNumberOfRepos", true,
				"The maximum number of repositories that will be used. Default is " + DEF_MAX_REPOS + ".", false);

		/* the following code blocks should not need to be changed */
		final private OptionWrapper option;

		//adds an option that is not part of any group
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, final boolean required) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(required).
					hasArg(hasArg).desc(description).build(), NO_GROUP);
		}
		
		//adds an option that is part of the group with the specified index (positive integer)
		//a negative index means that this option is part of no group
		//this option will not be required, however, the group itself will be
		CmdOptions(final String opt, final String longOpt, 
				final boolean hasArg, final String description, int groupId) {
			this.option = new OptionWrapper(
					Option.builder(opt).longOpt(longOpt).required(false).
					hasArg(hasArg).desc(description).build(), groupId);
		}
		
		//adds the given option that will be part of the group with the given id
		CmdOptions(Option option, int groupId) {
			this.option = new OptionWrapper(option, groupId);
		}
		
		//adds the given option that will be part of no group
		CmdOptions(Option option) {
			this(option, NO_GROUP);
		}

		@Override public String toString() { return option.getOption().getOpt(); }
		@Override public OptionWrapper getOptionWrapper() { return option; }
	}

}
