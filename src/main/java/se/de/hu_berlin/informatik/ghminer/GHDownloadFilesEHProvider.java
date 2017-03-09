package se.de.hu_berlin.informatik.ghminer;

import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * A simple event handler that downloads a file from git hub
 */
public class GHDownloadFilesEHProvider extends AbstractProcessor<GHTreeEntryWrapper, Object> {

	/**
	 * Starts the download of a given GHTreeEntry into the target directory
	 * which was set with the creation of the thread object.
	 * @return 
	 * {@code null}
	 */
	@Override
	public Object processItem(GHTreeEntryWrapper input) {
		FileUtils.downloadFile(input.getDownloadURL(), input.getOutputPath().toFile());
		return null;
	}
	
}
