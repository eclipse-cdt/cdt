/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.executables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener;
import org.eclipse.cdt.debug.internal.ui.views.executables.SourceFilesViewer.TranslationUnitInfo;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class SourceFilesContentProvider extends CElementContentProvider implements IExecutablesChangeListener {

	static class QuickParseJob extends Job {
		final Executable executable;
		ITranslationUnit[] tus;
		
		public QuickParseJob(Executable executable) {
			super (Messages.SourceFilesContentProvider_ReadingDebugSymbolInformationLabel 
					+ executable.getName());
			this.executable = executable;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			tus = executable.getSourceFiles(monitor);
			return Status.OK_STATUS;
		}
	}
	
	/** contains running jobs */ 
	private Map<IPath, QuickParseJob> pathToJobMap = new HashMap<IPath, SourceFilesContentProvider.QuickParseJob>();
	
	/** those executables for which we asked the question and got a result.
	 * NOTE: this contains a duplicate of into in Executable, because we can't
	 * guarantee or check whether Executable still has the info itself. */
	private Map<IPath, ITranslationUnit[]> fetchedExecutables = new HashMap<IPath, ITranslationUnit[]>();

	private final SourceFilesViewer viewer;
	
	public SourceFilesContentProvider(SourceFilesViewer viewer) {
		super(true, true);
		this.viewer = viewer;
		ExecutablesManager.getExecutablesManager().addExecutablesChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.CElementContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		ExecutablesManager.getExecutablesManager().removeExecutablesChangeListener(this);
		synchronized (fetchedExecutables) {
			fetchedExecutables.clear();
		}
		synchronized (pathToJobMap) {
			pathToJobMap.clear();
		}
		super.dispose();
	}
	
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITranslationUnit) {
			TranslationUnitInfo info = SourceFilesViewer.fetchTranslationUnitInfo(
					(Executable) viewer.getInput(), element);
			if (info != null && !info.exists)
				return false;
		}
		return super.hasChildren(element);
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Executable) {
			final Executable executable = (Executable) inputElement;
			final IPath exePath = executable.getPath();
			
			// look for a job that is currently fetching this info
			QuickParseJob job;
			synchronized (pathToJobMap) {
				job = pathToJobMap.get(exePath);
			}
			if (job != null) {
				// job is still running
				return new String[] { Messages.SourceFilesContentProvider_Refreshing };
			}
			
			// see if we already checked
			synchronized (fetchedExecutables) {
				if (fetchedExecutables.containsKey(exePath)) {
					return fetchedExecutables.get(exePath);
				}
			}
			
			// start a background job to look for the sources
			job = new QuickParseJob(executable);
			synchronized (pathToJobMap) {
				pathToJobMap.put(exePath, job);
			}
			
			// once the job finishes, update the viewer
			final QuickParseJob theJob = job;
			job.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					synchronized (pathToJobMap) {
						pathToJobMap.values().remove(theJob);
					}
					if (event.getResult().isOK()) {
						synchronized (fetchedExecutables) {
							fetchedExecutables.put(exePath, theJob.tus);
						}
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								// update the viewer
								if (!viewer.getControl().isDisposed()) {
									viewer.getTree().setLayoutDeferred(true);
									viewer.refresh(executable);
									viewer.packColumns();
									viewer.getTree().setLayoutDeferred(false);
								}
							}
						});
					}
				}
			});
			
			job.schedule();
			
			// while it's running...
			return new String[] { Messages.SourceFilesContentProvider_Refreshing };
		}
		return new Object[] {};
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesListChanged()
	 */
	public void executablesListChanged() {
		// Don't clear executables -- closing/opening project doesn't imply
		// the info is different.  But cancel all the jobs in case projects
		// were closed.  It's non-obvious how to map executables to projects,
		// so just bail and cancel all the current parsing.  The viewer
		// will be refreshed and re-request source lists for any executables
		// that are still applicable.
		cancelQuickParseJobs();
	}


	/**
	 * 
	 */
	private void cancelQuickParseJobs() {
		synchronized (pathToJobMap) {
			for (QuickParseJob job : pathToJobMap.values()) {
				job.cancel();
			}
			pathToJobMap.clear();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesChanged(java.util.List)
	 */
	public void executablesChanged(List<Executable> executables) {
		for (Executable executable : executables) {
			IPath exePath = executable.getPath();
			synchronized (fetchedExecutables) {
				fetchedExecutables.remove(exePath);
			}
			synchronized (pathToJobMap) {
				QuickParseJob job = pathToJobMap.get(exePath);
				if (job != null) {
					job.cancel();
					pathToJobMap.remove(exePath);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.CElementContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, final Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// pack because the quick parse job won't run
				if (newInput instanceof Executable 
						&& fetchedExecutables.containsKey(((Executable) newInput).getPath()))
					SourceFilesContentProvider.this.viewer.packColumns();
			}
		});
	}
}

