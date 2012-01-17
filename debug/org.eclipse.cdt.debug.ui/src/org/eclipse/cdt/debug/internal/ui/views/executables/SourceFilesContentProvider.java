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
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener2;
import org.eclipse.cdt.debug.internal.core.Trace;
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
import org.eclipse.ui.progress.WorkbenchJob;

public class SourceFilesContentProvider extends CElementContentProvider implements IExecutablesChangeListener2 {

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
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Quick parsing of executable for source files has begun (" + this + ')');			 //$NON-NLS-1$
			
			// Ask the Executable for its source files. This could take a while...
			ITranslationUnit[] mytus = executable.getSourceFiles(monitor);
			
			IStatus status;
			if (!monitor.isCanceled()) {
				tus = mytus;
				status = Status.OK_STATUS;
			}
			else { 
				status = Status.CANCEL_STATUS;
			}
			
			if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Quick parsing of executable has finished, status is " + status);			 //$NON-NLS-1$
			return status;
		}
	}
	
	/**
	 * The collection of running file parsing jobs. Each executable file (not
	 * object) can independently be parsed, and these parses can happen
	 * simultaneously. Normally, each executable file has at most one ongoing
	 * parse. An exception is when a search is canceled. We don't wait for the
	 * search to actually end if a subsequent search comes in shortly after the
	 * first one is canceled. We cancel the first one, remove it from this list,
	 * schedule a new one, then add that to the list. It's safe to assume the
	 * canceled one will complete before the new one.
	 * 
	 * <p> This collection must be accessed only from the UI thread 
	 */
	private Map<IPath, QuickParseJob> pathToJobMap = new HashMap<IPath, SourceFilesContentProvider.QuickParseJob>();
	
	/** those executables for which we asked the question and got a result.
	 * NOTE: this contains a duplicate of into in Executable, because we can't
	 * guarantee or check whether Executable still has the info itself. */
	private static class TUData{
		/** Constructor used when search completes successfully */
		public TUData(ITranslationUnit[] tus, long timestamp) {
			this.tus = tus;
			this.timestamp = timestamp;
		}
		
		/** Constructor used when search is canceled */
		public TUData() {
			this.canceled = true;
		}
		
		ITranslationUnit[] tus;
		/** IResource.getModificationStamp value of when this data was last updated */ 
		long timestamp;
		
		boolean canceled;
	}

	/**
	 * The cached file info. Key is the path of the executable. This collection must be accessed only on the UI thread. 
	 */
	private Map<IPath, TUData> fetchedExecutables = new HashMap<IPath, TUData>();

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
		new WorkbenchJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fetchedExecutables.clear();
				pathToJobMap.clear();
				return Status.OK_STATUS;
			}
		}.schedule();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, inputElement);
		
		if (inputElement instanceof Executable) {
			final Executable executable = (Executable) inputElement;
			final IPath exePath = executable.getPath();
			
			// look for a job that is currently fetching this info
			QuickParseJob job;
			job = pathToJobMap.get(exePath);
			if (job != null) {
				// job is still running
				return new String[] { Messages.SourceFilesContentProvider_Refreshing };
			}
			// create a background job to look for the sources but don't start it yet
			job = new QuickParseJob(executable);
			pathToJobMap.put(exePath, job);
			
			// See if we have the result cached for this executable. If so
			// return that. It's also possible that the most resent search was
			// canceled
			Object[] cachedResult = null;
			TUData tud = fetchedExecutables.get(exePath);
			if (tud != null) {
				if (tud.canceled)
					cachedResult = new String[]{Messages.SourceFilesContentProvider_Canceled};
				else
					cachedResult = tud.tus;
			}
			if (cachedResult != null) {
				pathToJobMap.remove(exePath); // removed the unused search job
				return cachedResult;
			}
			
			// Schedule the job; once it finishes, update the viewer
			final QuickParseJob theJob = job;
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(final IJobChangeEvent event) {
					new WorkbenchJob("refreshing source files viewer"){ //$NON-NLS-1$
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							if (event.getResult().isOK()) {
								fetchedExecutables.put(exePath, new TUData(theJob.tus, theJob.executable.getResource().getModificationStamp()));
							}
							else {
								fetchedExecutables.put(exePath, new TUData());
							}

							pathToJobMap.values().remove(theJob);
							
							refreshViewer(executable);
							return Status.OK_STATUS;
						}
					}.schedule();
				}
			});

			job.schedule();
			
			// show the user a string that lets him know we're searching
			return new String[] { Messages.SourceFilesContentProvider_Refreshing };
		}
		return new Object[] {};
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesListChanged()
	 */
	@Override
	public void executablesListChanged() {
		// we react via IExecutablesChangeListener2 methods
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener#executablesChanged(java.util.List)
	 */
	@Override
	public void executablesChanged(final List<Executable> executables) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, executables);
	
		new WorkbenchJob("Refreshing viewer") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Executable executable : executables) {
					IPath exePath = executable.getPath();
					fetchedExecutables.remove(exePath);
					QuickParseJob job = pathToJobMap.get(exePath);
					if (job != null) {
						if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Cancelling QuickParseJob: " + job); 						 //$NON-NLS-1$
						job.cancel();
						pathToJobMap.remove(exePath);
					}
				}
				
				if (!viewer.getControl().isDisposed()) {
					// See if our current input is one of the executables that has changed.
					for (Executable executable : executables) {
						if (executable.equals(fInput)) {
							// Executable.equals() is not a simple reference
							// check. Two Executable objects are equal if they
							// represent the same file on disk. I.e., our input
							// object might not be one of the instances on the  
							// changed-list, but for sure the file on disk has
							// changed. Now, the manager that called this
							// listener has already told the Executable
							// instances on the changed list to flush their
							// source files list. However, if our input is not
							// exactly one of those references, it means the
							// manager is no longer managing the Executable
							// that's our input. In that case, it's up to us to
							// tell that Executable to flush its source file
							// cache so that refreshing the viewer will cause a
							// fresh fetch of the source file information.
							Executable execInput = (Executable)fInput;
							if (executable != execInput) {
								execInput.setRefreshSourceFiles(true);
							}
							refreshViewer(execInput);
							break;
						}
					}
				}
				return Status.OK_STATUS;
			}
			
		}.schedule();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.CElementContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, final Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		
		new WorkbenchJob("Refreshing viewer") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// pack because the quick parse job won't run
				if (newInput instanceof Executable 
						&& fetchedExecutables.containsKey(((Executable) newInput).getPath()))
					SourceFilesContentProvider.this.viewer.packColumns();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener2#executablesAdded(java.util.List)
	 */
	@Override
	public void executablesAdded(final List<Executable> executables) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, executables);
		
		// Throw out our cached translation units for the executable *file* but
		// only if the file hasn't changed. Executable objects come and go
		// independently of the file on disk.
		new WorkbenchJob("executables removed") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Executable exec : executables) {
					final IPath exePath = exec.getPath();
					final long timestamp = exec.getResource().getModificationStamp();
					TUData tud = fetchedExecutables.get(exePath);
					if (tud != null && tud.timestamp != timestamp) {
						fetchedExecutables.remove(exePath);
					}
				}
				
				if (!viewer.getControl().isDisposed()) {
					// See if current viewer input is one of the executables that
					// was added. If so, this is likely an exec that was rebuilt
					// and CDT missed sending a REMOVED model event. There's
					// some crazy race condition going on, but basically CDT
					// sends out an event that the binary has changed, then
					// sends one that says it was added. Anyway, the best thing
					// for us to do is to cause a refresh of the viewer since
					// the addition notification probably caused us to cancel
					// the parse of the exec that was initiated by the change
					// event and the viewer will be stuck with a "canceled"
					// message in the viewer table.
					for (Executable executable : executables) {
						if (executable.equals(fInput)) {
							if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "refreshing viewer; added executable is our current input"); //$NON-NLS-1$
							refreshViewer((Executable)fInput);
							break;
						}
					}
				}
				
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.executables.IExecutablesChangeListener2#executablesRemoved(java.util.List)
	 */
	@Override
	public void executablesRemoved(final List<Executable> executables) {
		if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().traceEntry(null, executables);
		
		// The fact that the Executable was removed from the workspace doesn't
		// mean we need to throw out the source file info we've cached. If a
		// project is closed then reopened, we are able to reuse the info as
		// long as the timestamp of the resource hasn't changed. But, there's no
		// point in continuing any ongoing searches in the executables.
		new WorkbenchJob("executables removed") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Executable exec : executables) {
					final IPath exePath = exec.getPath();
					QuickParseJob job = pathToJobMap.get(exePath);
					if (job != null) {
						if (Trace.DEBUG_EXECUTABLES) Trace.getTrace().trace(null, "Cancelling QuickParseJob: " + job); 						 //$NON-NLS-1$
						job.cancel();
						pathToJobMap.remove(exePath);
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * Restarts a parse of the current input (Executable) if and only if its
	 * last search was canceled. The viewer is refresh accordingly.
	 * 
	 * <p>
	 * Must be called on the UI thread
	 * 
	 */
	public void restartCanceledExecutableParse() {
		assert Display.getCurrent() != null;
		
		Object input = viewer.getInput();
		if (input instanceof Executable) {
			final Executable executable = (Executable)input;
			final IPath exePath = executable.getPath();

			// Ignore restart if there's an ongoing search. 
			QuickParseJob job;
			job = pathToJobMap.get(exePath);
			if (job != null) {
				return;
			}

			TUData tud = fetchedExecutables.get(exePath);

			// Ignore request if the most recent search wasn't canceled
			if (tud != null && !tud.canceled) {
				pathToJobMap.remove(exePath);
				return;
			}

			// Create and schedule a parse job. Once the job finishes, update
			// the viewer
			job = new QuickParseJob(executable);
			pathToJobMap.put(exePath, job);
			final QuickParseJob theJob = job;
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(final IJobChangeEvent event) {
					
					new WorkbenchJob("refreshing source files viewer"){ //$NON-NLS-1$
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							// Update the model with the search results
							if (event.getResult().isOK()) {
								fetchedExecutables.put(exePath, new TUData(theJob.tus, theJob.executable.getResource().getModificationStamp()));
							}
							else {
								// The search job apparently always completes
								// successfully or it was canceled (failure was
								// not a considered outcome). If it was canceled,
								// well then we're back to where we started
								fetchedExecutables.put(exePath, new TUData());
							}
							pathToJobMap.values().remove(theJob);
							
							refreshViewer(executable);
							return Status.OK_STATUS;
						}
					}.schedule();
				}
			});

			job.schedule();

			// The viewer is currently showing "search canceled". Cause an
			// immediate refresh so that it shows "refreshing" while the new
			// search is ongoing
			refreshViewer(executable);
		}
	}
	
	/**
	 * Utility method to invoke a viewer refresh for the given element
	 * @param input the Executable to show content for
	 * 
	 * <p> Must be called on the UI thread 
	 */
	private void refreshViewer(Executable input) {
		if (!viewer.getControl().isDisposed()) {
			viewer.getTree().setLayoutDeferred(true);
			viewer.refresh(input);
			viewer.packColumns();
			viewer.getTree().setLayoutDeferred(false);
		}
	}
}

