/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
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
					monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

					BinaryContainer vbin = (BinaryContainer) cproject.getBinaryContainer();
					ArchiveContainer vlib = (ArchiveContainer) cproject.getArchiveContainer();
					
					vlib.removeChildren();
					vbin.removeChildren();
					
					cproject.getProject().accept(new Visitor(monitor), IContainer.INCLUDE_PHANTOMS);
					fireEvents(cproject, vbin);
					fireEvents(cproject, vlib);
					
					monitor.done();
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

	private class Visitor implements IResourceProxyVisitor {
		private IProgressMonitor vMonitor;
		private IProject project;
		private IOutputEntry[] entries = new IOutputEntry[0];
		private IContentType textContentType;

		public Visitor(IProgressMonitor monitor) {
			vMonitor = monitor;
			this.project = cproject.getProject();
			try {
				entries = cproject.getOutputEntries();
			} catch (CModelException e) {
			}
			IContentTypeManager mgr = Platform.getContentTypeManager();
			textContentType = mgr.getContentType("org.eclipse.core.runtime.text"); //$NON-NLS-1$
		}

		public boolean visit(IResourceProxy proxy) throws CoreException {
			if (vMonitor.isCanceled()) {
				return false;
			}
			vMonitor.worked(1);
			
			// Attempt to speed things up by rejecting up front
			// Things we know should not be Binary files.
			
			// check if it's a file resource
			// and bail out early
			if (proxy.getType() != IResource.FILE) {
				return true;
			}
			
			// check against known content types
			String name = proxy.getName();
			IContentType contentType = CCorePlugin.getContentType(project, name);
			if (contentType != null && textContentType != null) {
				if (contentType != null && contentType.isKindOf(textContentType)) {
					return true;
				} else if (textContentType.isAssociatedWith(name)) {
					return true;
				}
			}

			// we have a candidate
			IPath path = proxy.requestFullPath();
			if (path != null) {
				for (int i = 0; i < entries.length; ++i) {
					if (isOnOutputEntry(entries[i], path)) {
						IFile file = (IFile) proxy.requestResource();
						CModelManager factory = CModelManager.getDefault();
						IBinaryFile bin = factory.createBinaryFile(file);
						if (bin != null) {
							// Create the file will add it to the {Archive,Binary}Containery.
							factory.create(file, bin, cproject);
							return true;
						}
					}
				}
			}
			return true;
		}
		
		private boolean isOnOutputEntry(IOutputEntry entry, IPath path) {
			if (entry.getPath().isPrefixOf(path) && !CoreModelUtil.isExcluded(path, entry.fullExclusionPatternChars())) {
				return true;
			}
			return false;
		}
	}
}
