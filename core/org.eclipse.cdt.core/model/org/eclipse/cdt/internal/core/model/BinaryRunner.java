package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class BinaryRunner {
	IProject project;
	Thread runner;
	ArchiveContainer vlib;
	BinaryContainer vbin;
	
	public BinaryRunner(IProject prj) {
		project = prj;
	}
	
	public void start() {
		runner = new Thread(new Runnable() {
			public void run() {
				ICProject cproject = CModelManager.getDefault().create(project);
				if (cproject == null || Thread.currentThread().isInterrupted()) {
					return;
				}
				IOutputEntry[] outs = null;
				try {
					outs = cproject.getOutputEntries();
				} catch (CModelException e) {
					outs = new IOutputEntry[0];
				}
				
				vbin = (BinaryContainer)cproject.getBinaryContainer();
				vlib = (ArchiveContainer)cproject.getArchiveContainer();
				vlib.removeChildren();
				vbin.removeChildren();
				IPath projectPath = project.getFullPath();
				for (int i = 0; i < outs.length; i++) {
					IPath path = outs[i].getPath();
					if (projectPath.equals(path)) {
						try {
							project.accept(new Visitor(BinaryRunner.this));
						} catch (CoreException e) {
							//e.printStackTrace();
						} catch (Exception e) {
							// What is wrong ?
							e.printStackTrace();
						}
						break; // We are done.
					} else if (projectPath.isPrefixOf(path)) {
						path = path.removeFirstSegments(projectPath.segmentCount());
						IResource res =project.findMember(path);
						if (res != null) {
							try {
								res.accept(new Visitor(BinaryRunner.this));
							} catch (CoreException e) {
								//e.printStackTrace();
							} catch (Exception e) {
								// What is wrong ?
								e.printStackTrace();
							}
						}
					}
				}
				if (!Thread.currentThread().isInterrupted()) {
					fireEvents(cproject, vbin);
					fireEvents(cproject, vlib);
				}
				// Tell the listeners we are done.
				synchronized(BinaryRunner.this) {
					BinaryRunner.this.notifyAll();
					runner = null;
				}
			}

		}, CCorePlugin.getResourceString("CoreModel.BinaryRunner.Binary_Search_Thread")); //$NON-NLS-1$
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
				ICElement parent = factory.create(file.getParent(), null);
				if (bin.getType() == IBinaryFile.ARCHIVE) {
					if (parent == null) {
						parent = vlib;
					}
					Archive ar = new Archive(parent, file, (IBinaryArchive)bin);
					vlib.addChild(ar);
				} else if (bin.getType() == IBinaryFile.EXECUTABLE || bin.getType() == IBinaryFile.SHARED) {
					if (parent == null) {
						parent = vbin;
					}
					Binary binary = new Binary(parent, file, (IBinaryObject)bin);
					vbin.addChild(binary);
				}
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
