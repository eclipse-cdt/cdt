package org.eclipse.cdt.internal.core.search.indexing;

import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.core.runtime.IProgressMonitor;


public class CleanEncounteredHeaders implements IJob {
	
	IndexManager manager = null;
	
	public CleanEncounteredHeaders(IndexManager manager){
		this.manager = manager;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean execute(IProgressMonitor progress) {
		
		//Clean out the headers
		this.manager.resetEncounteredHeaders();
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#belongsTo(java.lang.String)
	 */
	public boolean belongsTo(String jobFamily) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#isReadyToRun()
	 */
	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return true;
	}

}