package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.runtime.IPath;

public abstract class IndexRequest implements IIndexJob {

	protected boolean isCancelled = false;
	protected IPath indexPath = null;
	
	public IndexRequest(IPath indexPath) {
		this.indexPath = indexPath;
	}
	
	public boolean belongsTo(String projectName) {
		return projectName.equals(this.indexPath.segment(0));
	}
	
	protected Integer updatedIndexState() {
		return CIndexStorage.UPDATING_STATE;
	}
	
	public IPath getIndexPath(){
		return indexPath;
	}	
}
