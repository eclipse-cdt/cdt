/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMUpdator extends Job {

	private PDOMUpdator prevJob;
	private ICElementDelta delta;
	private ICProject project;
	private List addedTUs;
	private List changedTUs;
	private List removedTUs;
	private int count;
	
	public PDOMUpdator(ICElementDelta delta, PDOMUpdator prevJob) {
		super("PDOM Updator");
		this.prevJob = prevJob;
		this.delta = delta;
	}
	
	public PDOMUpdator(ICProject project, PDOMUpdator prevJob) {
		super("PDOM Project Updator");
		this.prevJob = prevJob;
		this.project = project;
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		if (prevJob != null)
			try {
				prevJob.join();
			} catch (InterruptedException e) {
			}
		
		try {
			long start = System.currentTimeMillis();
			
			if (delta != null)
				processDelta(delta);
			if (project != null)
				processNewProject(project);
			
			if (addedTUs != null)
				for (Iterator i = addedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					monitor.subTask("Files remaining: " + (count--));
					ITranslationUnit tu = (ITranslationUnit)i.next();
					processAddedTU(tu);
				}
			
			if (changedTUs != null)
				for (Iterator i = changedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					monitor.subTask("Files remaining: " + (count--));
					ITranslationUnit tu = (ITranslationUnit)i.next();
					processChangedTU(tu);
				}
			
			if (removedTUs != null)
				for (Iterator i = removedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					monitor.subTask("Files remaining: " + (count--));
					ITranslationUnit tu = (ITranslationUnit)i.next();
					processRemovedTU(tu);
				}

			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID + "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings!= null)
				if (showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
					System.out.println("Updator Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
					
			return Status.OK_STATUS;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return e.getStatus();
		}
	}

	private void processDelta(ICElementDelta delta) {
		// process the children first
		ICElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; ++i)
			processDelta(children[i]);

		// what have we got
		ICElement element = delta.getElement();
		if (element.getElementType() == ICElement.C_PROJECT) {
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				processNewProject((ICProject)element);
				break;
			}
		} else if (element.getElementType() == ICElement.C_UNIT) {
			ITranslationUnit tu = (ITranslationUnit)element;
			if (tu.isWorkingCopy())
				// Don't care about working copies either
				return;
			
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				if (addedTUs == null)
					addedTUs = new LinkedList();
				addedTUs.add(element);
				++count;
				break;
			case ICElementDelta.CHANGED:
				if (changedTUs == null)
					changedTUs = new LinkedList();
				changedTUs.add(element);
				++count;
				break;
			case ICElementDelta.REMOVED:
				if (removedTUs == null)
					removedTUs = new LinkedList();
				removedTUs.add(element);
				++count;
				break;
			}
		}
	}
	
	private void processNewProject(final ICProject project) {
		try {
			project.getProject().accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						String fileName = proxy.getName();
						IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
						if (contentType == null)
							return true;
						String contentTypeId = contentType.getId();
						
						if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentTypeId)
								|| CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentTypeId)) {
							if (addedTUs == null)
								addedTUs = new LinkedList();
							addedTUs.add(CoreModel.getDefault().create((IFile)proxy.requestResource()));
							++count;
						}
						// TODO handle header files
						return false;
					} else {
						return true;
					}
				}
			}, 0);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	private void processAddedTU(ITranslationUnit tu) throws CoreException {
		IPDOM pdom = tu.getCProject().getIndex();
		if (pdom == null || !(pdom instanceof PDOMDatabase))
			return;
		
		PDOMDatabase mypdom = (PDOMDatabase)pdom;
		mypdom.addSymbols(tu);
	}

	private void processRemovedTU(ITranslationUnit tu) throws CoreException {
		IProject project = tu.getCProject().getProject();
		IPDOM pdom = PDOM.getPDOM(project);
		if (pdom == null || !(pdom instanceof PDOMDatabase))
			return;

		PDOMDatabase mypdom = (PDOMDatabase)pdom;
		mypdom.removeSymbols(tu);
		// TODO delete the file itself from the database
		// the removeSymbols only removes the names in the file
	}

	private void processChangedTU(ITranslationUnit tu) throws CoreException {
		IPDOM pdom = PDOM.getPDOM(tu.getCProject().getProject());
		if (pdom == null || !(pdom instanceof PDOMDatabase))
			return;
		PDOMDatabase mypdom = (PDOMDatabase)pdom;
		
		mypdom.removeSymbols(tu);
		mypdom.addSymbols(tu);
	}

}
