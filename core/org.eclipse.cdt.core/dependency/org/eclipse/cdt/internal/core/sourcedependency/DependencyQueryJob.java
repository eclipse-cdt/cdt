/*
 * Created on Jul 23, 2003
 */
package org.eclipse.cdt.internal.core.sourcedependency;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author bgheorgh
 */
public class DependencyQueryJob implements IJob {

	IProject project;
	IFile file;
	ArrayList includeFiles;
	DependencyManager depManager;
	protected DependencySelector depSelector;
	
	public DependencyQueryJob(IProject project, IFile file, DependencyManager depMan, List includeFiles) {
		this.project = project;
		this.file = file;
		this.depManager = depMan;
		this.includeFiles = (ArrayList) includeFiles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#belongsTo(java.lang.String)
	 */
	public boolean belongsTo(String jobFamily) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#cancel()
	 */
	public void cancel() {}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean execute(IProgressMonitor progress) {
		
		if ((project == null) ||(file == null)) return false;		
		
		String[] tempFiles = this.depManager.getFileDependencies(project,file);
		if (tempFiles != null){
			for (int i=0; i<tempFiles.length; i++){
						includeFiles.add(tempFiles[i]);
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#isReadyToRun()
	 */
	public boolean isReadyToRun() {
		if (this.depSelector == null) { // only check once. As long as this job is used, it will keep the same index picture
			this.depSelector = new DependencySelector(SearchEngine.createWorkspaceScope(), null, false, this.depManager);
			this.depSelector.getIndexes(); // will only cache answer if all indexes were available originally
		}
		return true;
	}

}
