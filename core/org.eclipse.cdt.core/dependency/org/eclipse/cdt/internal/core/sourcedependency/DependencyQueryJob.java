/*
 * Created on Jul 23, 2003
 */
package org.eclipse.cdt.internal.core.sourcedependency;

import java.util.ArrayList;
import java.util.List;

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
		
		String[] tempFiles = this.depManager.getFileDependencies(project,file);
		for (int i=0; i<tempFiles.length; i++){
			includeFiles.add(tempFiles[i]);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#isReadyToRun()
	 */
	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return true;
	}

}
