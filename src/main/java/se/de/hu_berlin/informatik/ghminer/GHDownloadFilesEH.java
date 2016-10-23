package se.de.hu_berlin.informatik.ghminer;

import se.de.hu_berlin.informatik.utils.fileoperations.FileUtils;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturn;
import se.de.hu_berlin.informatik.utils.threaded.disruptor.eventhandler.EHWithInputAndReturnFactory;

/**
 * A simple event handler that downloads a file from git hub
 */
public class GHDownloadFilesEH extends EHWithInputAndReturn<GHTreeEntryWrapper, Object> {

	/**
	 * Starts the download of a given GHTreeEntry into the target directory
	 * which was set with the creation of the thread object.
	 * @return 
	 * {@code null}
	 */
	@Override
	public Object processInput(GHTreeEntryWrapper input) {
		FileUtils.downloadFile(input.getDownloadURL(), input.getOutputPath().toFile());
		return null;
	}
	
	@Override
	public void resetAndInit() {
		//not needed
	}

	public static class Factory extends EHWithInputAndReturnFactory<GHTreeEntryWrapper,Object> {
		
		public Factory() {
			super(GHDownloadFilesEH.class);
		}

		@Override
		public EHWithInputAndReturn<GHTreeEntryWrapper, Object> newFreshInstance() {
			return new GHDownloadFilesEH();
		}
	}
	
}
