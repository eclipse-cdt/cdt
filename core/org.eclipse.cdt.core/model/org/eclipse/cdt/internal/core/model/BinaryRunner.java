package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

public class BinaryRunner implements IJobChangeListener {
	IProject project;
	ICProject cproject;
	Job runner;
	ArchiveContainer vlib;
	BinaryContainer vbin;
	boolean done = false;

	public BinaryRunner(IProject prj) {
		project = prj;
		cproject = CModelManager.getDefault().create(project);
	}
	
	public void start() {
		String taskName = CCorePlugin.getResourceString("CoreModel.BinaryRunner.Binary_Search_Thread"); //$NON-NLS-1
		Job runner = new Job(taskName) {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				if (cproject == null || Thread.currentThread().isInterrupted()) {
					return Status.CANCEL_STATUS;
				}
				vbin = (BinaryContainer)cproject.getBinaryContainer();
				vlib = (ArchiveContainer)cproject.getArchiveContainer();
				vlib.removeChildren();
				vbin.removeChildren();
				try {
					project.accept(new Visitor(BinaryRunner.this));
				} catch (CoreException e) {
					//e.printStackTrace();
				} catch (Exception e) {
					// What is wrong ?
					e.printStackTrace();
				}
				if (!Thread.currentThread().isInterrupted()) {
					fireEvents(cproject, vbin);
					fireEvents(cproject, vlib);
				}
				// Tell the listeners we are done.
				synchronized(BinaryRunner.this) {
					BinaryRunner.this.notifyAll();
					BinaryRunner.this.runner = null;
				}
				return Status.OK_STATUS;
			}
		};
		runner.schedule();
		
	}


	/**
	 * wrap the wait call and the interrupteException.
	 */
	public synchronized void waitIfRunning() {
		while (runner != null && !done) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void stop() {
		if ( runner != null && !done) {
			runner.cancel();
		}
	}

	void fireEvents(ICProject cproject, Parent container) {
		// Fired the event.
		ICElement[] children = container.getChildren();
		if (children.length > 0) {
			CModelManager factory = CModelManager.getDefault();
			ICElement root = (ICModel)factory.getCModel();
			CElementDelta cdelta = new CElementDelta(root);
			cdelta.added(cproject);
			cdelta.added(container);
			for (int i = 0; i < children.length; i++) {
				cdelta.added(children[i]);
			}
			factory.registerCModelDelta(cdelta);
			factory.fire(ElementChangedEvent.POST_CHANGE);
		}
	}

	void addChildIfBinary(IFile file) {
		CModelManager factory = CModelManager.getDefault();
		// Attempt to speed things up by rejecting up front
		// Things we know should not be Binary files.
		if (!factory.isTranslationUnit(file)) {
			IBinaryFile bin = factory.createBinaryFile(file);
			if (bin != null) {
				// Create the file will add it to the {Archive,Binary}Containery.
				factory.create(file, bin, null);
			}
		}
	}

	class Visitor implements IResourceVisitor {
		BinaryRunner runner;

		public Visitor (BinaryRunner r) {
			runner = r;
		}

		public boolean visit(IResource res) throws CoreException {
			if (Thread.currentThread().isInterrupted()) {
				return false;
			}
			if (cproject.isOnOutputEntry(res)) {
				if (res instanceof IFile) {
					if (runner != null) {
						runner.addChildIfBinary((IFile)res);
					}
					return false;
				}
			}
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void aboutToRun(IJobChangeEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void awake(IJobChangeEvent event) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void done(IJobChangeEvent event) {
		done = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void running(IJobChangeEvent event) {
		done = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void scheduled(IJobChangeEvent event) {
		done = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	public void sleeping(IJobChangeEvent event) {
	}
}
