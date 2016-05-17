/**
 * 
 */
package se.de.hu_berlin.informatik.ghminer;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import se.de.hu_berlin.informatik.utils.miscellaneous.IOutputPathGenerator;
import se.de.hu_berlin.informatik.utils.threadwalker.CallableWithPaths;

/**
 * {@link Callable} object that tokenizes the provided (Java source code) file.
 * 
 * @author Simon Heiden
 */
public class URLCall extends CallableWithPaths<Object,Boolean> {
	
	/**
	 * Initializes a {@link URLCall} object with the given parameters.
	 * @param outputPathGenerator
	 * a generator to automatically create output paths
	 */
	public URLCall(IOutputPathGenerator<Path> outputPathGenerator) {
		super(outputPathGenerator);
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		System.out.print(".");

		Object element = getInput();
		
		/*do something with list element*/
		
		Path output = getOutputPath();
		
		/*save to output path*/
		
		return true;
	}

}

