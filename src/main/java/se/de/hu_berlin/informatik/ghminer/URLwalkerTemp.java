/**
 * 
 */
package se.de.hu_berlin.informatik.ghminer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.miscellaneous.OutputPathGenerator;
import se.de.hu_berlin.informatik.utils.optionparser.OptionParser;
import se.de.hu_berlin.informatik.utils.threadwalker.ExecutorServiceProvider;
import se.de.hu_berlin.informatik.utils.tm.modules.ThreadedListProcessorModule;

/**
 * @author Simon Heiden
 */
public class URLwalkerTemp {

	/**
	 * Parses the options from the command line.
	 * @param args
	 * the application's arguments
	 * @return
	 * an {@link OptionParser} object that provides access to all parsed options and their values
	 */
	private static OptionParser getOptions(String[] args) {
		final String tool_usage = "something";
		final OptionParser options = new OptionParser(tool_usage, args);
		
		options.add("o", "output", true, "Path to output directory.", true);
        
        options.parseCommandLine();
        
        return options;
	}
	
	/**
	 * @param args
	 * something
	 */
	public static void main(String[] args) {		
		
		OptionParser options = getOptions(args);
        
		Path output = options.isDirectory('o', false);

		//change or use option value
		int threadCount = 20;
		ExecutorServiceProvider executor = new ExecutorServiceProvider(threadCount);

		//output file extension
		final String extension = ".java";
		
		
		//here is the loop in which to submit multiple lists for different output directories
		for (int i = 0; i < 10; ++i) {
		//load with real list!!
		List<Object> listWithURLs = null;
		//set repository output main path
		Path repOutput = Paths.get(output.toString(), "output","path","of","repository");
		
		IOutputPathGenerator<Path> generator = new OutputPathGenerator(repOutput, extension, true);

		//apply URLCall to all list elements
		new ThreadedListProcessorModule<Object>(executor.getExecutorService(), URLCall.class, generator)
				.submitAndStart(listWithURLs);
		
		}
		
		//we are done! Shutdown of the executor service is necessary! (That means: No new task submissions!)
		executor.shutdownAndWaitForTermination();

	}
}
