package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class BinaryRunner {
	IProject project;
	Thread runner;
	
	public BinaryRunner(IProject prj) {
		project = prj;
	}
	
	public void start() {
		runner = new Thread(new Runnable() {
			public void run() {
				ICProject cproject = CModelManager.getDefault().create(project);
				ArchiveContainer clib;
				BinaryContainer cbin;
				cbin = (BinaryContainer)cproject.getBinaryContainer();
				clib = (ArchiveContainer)cproject.getArchiveContainer();
				clib.removeChildren();
				cbin.removeChildren();
				try {
					cproject.getProject().accept(new Visitor(BinaryRunner.this));
				} catch (CoreException e) {
					//e.printStackTrace();
				} catch (Exception e) {
					// What is wrong ?
					e.printStackTrace();
				}
				if (!Thread.currentThread().isInterrupted()) {
					fireEvents(cproject, cbin);
					fireEvents(cproject, clib);
				}
				// Tell the listeners we are done.
				synchronized(BinaryRunner.this) {
					BinaryRunner.this.notifyAll();
					runner = null;
				}
			}

		}, "Binary Search Thread");
		runner.start();
	}


	/**
	 * wrap the wait call and the interrupteException.
	 */
	public synchronized void waitIfRunning() {
		while (runner != null && runner.isAlive()) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void stop() {
		if ( runner != null && runner.isAlive()) {
			runner.interrupt();
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
			factory.fire();
		}
	}

	void addChildIfBinary(IFile file) {
		CModelManager factory = CModelManager.getDefault();
		// Attempt to speed things up by rejecting up front
		// Things we know should not be Binary files.
		if (!factory.isTranslationUnit(file)) {
			IBinaryFile bin = factory.createBinaryFile(file);
			if (bin != null) {
				IResource res = file.getParent();
				ICElement parent = factory.create(res);
				// By creating the element, it will be added to the correct (bin/archive)container.
				factory.create(parent, file, bin);
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
			if (res instanceof IFile) {
				runner.addChildIfBinary((IFile)res);
				return false;
			}
			return true;
		}
	}
}
