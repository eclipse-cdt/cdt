/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class BinaryRunner {

	ICProject cproject;
	Job runner;

	public BinaryRunner(IProject prj) {
		cproject = CModelManager.getDefault().create(prj);
	}

	public void start() {
		String taskName = CCorePlugin.getResourceString("CoreModel.BinaryRunner.Binary_Search_Thread"); //$NON-NLS-1$
		runner = new Job(taskName) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				if (cproject == null || monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				try {
					BinaryContainer vbin = (BinaryContainer) cproject.getBinaryContainer();
					ArchiveContainer vlib = (ArchiveContainer) cproject.getArchiveContainer();
					
					vlib.removeChildren();
					vbin.removeChildren();
					cproject.getProject().accept(new Visitor(BinaryRunner.this, monitor));
					fireEvents(cproject, vbin);
					fireEvents(cproject, vlib);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		runner.schedule();

	}

	/**
	 * wrap the wait call and the interrupteException.
	 */
	public void waitIfRunning() {
		if (runner != null) {
			try {
				runner.join();
			} catch (InterruptedException e) {
			}
		}
	}

	public void stop() {
		if (runner != null && runner.getState() == Job.RUNNING) {
			runner.cancel();
		}
	}

	void fireEvents(ICProject cproj, Parent container) {
		// Fired the event.
		try {
			ICElement[] children = container.getChildren();
			if (children.length > 0) {
				CModelManager factory = CModelManager.getDefault();
				ICElement root = factory.getCModel();
				CElementDelta cdelta = new CElementDelta(root);
				cdelta.added(cproj);
				cdelta.added(container);
				for (int i = 0; i < children.length; i++) {
					cdelta.added(children[i]);
				}
				factory.registerCModelDelta(cdelta);
				factory.fire(ElementChangedEvent.POST_CHANGE);
			}
		} catch (CModelException e) {
			//
		}
	}

	void addChildIfBinary(IFile file) {
		CModelManager factory = CModelManager.getDefault();
		// Attempt to speed things up by rejecting up front
		// Things we know should not be Binary files.
		if (!CoreModel.isTranslationUnit(file)) {
			IBinaryFile bin = factory.createBinaryFile(file);
			if (bin != null) {
				// Create the file will add it to the {Archive,Binary}Containery.
				factory.create(file, bin, null);
			}
		}
	}

	class Visitor implements IResourceVisitor {

		private BinaryRunner vRunner;
		private IProgressMonitor vMonitor;

		public Visitor(BinaryRunner r, IProgressMonitor monitor) {
			vRunner = r;
			vMonitor = monitor;
		}

		public boolean visit(IResource res) throws CoreException {
			if (vMonitor.isCanceled()) {
				return false;
			}
			if (cproject.isOnOutputEntry(res)) {
				if (res instanceof IFile) {
					if (vRunner != null) {
						vRunner.addChildIfBinary((IFile) res);
					}
					return false;
				}
			}
			return true;
		}
	}
}
