package se.de.hu_berlin.informatik.ghminer;

import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnMethodProvider;

/**
 * A simple event handler that downloads a file from git hub
 */
public class GHDownloadFilesEHProvider extends EHWithInputAndReturnMethodProvider<GHTreeEntryWrapper, Object> {

	/**
	 * Starts the download of a given GHTreeEntry into the target directory
	 * which was set with the creation of the thread object.
	 * @return 
	 * {@code null}
	 */
	@Override
	public Object processInput(GHTreeEntryWrapper input,
			EHWithInputAndReturn<GHTreeEntryWrapper, Object> executingHandler) {
		FileUtils.downloadFile(input.getDownloadURL(), input.getOutputPath().toFile());
		return null;
	}
	
}
