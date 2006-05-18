/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.ctags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class CtagsHandleDelta extends CtagsIndexerJob {

	private final ICElementDelta delta;

	private List added = new ArrayList();
	private List changed = new ArrayList();
	private List removed = new ArrayList();
	
	public CtagsHandleDelta(CtagsIndexer indexer, ICElementDelta delta) throws CoreException {
		super(indexer);
		this.delta = delta;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
		
			processDelta(delta);
			
			int count = changed.size() + added.size() + removed.size();

			if (count > 0) {
				monitor.beginTask("Indexing", count);
					
				Iterator i = changed.iterator();
				while (i.hasNext()) {
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(tu.getElementName());
					changeTU(tu);
					monitor.worked(1);
				}
				
				i = added.iterator();
				while (i.hasNext()) {
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(tu.getElementName());
					addTU(tu);
					monitor.worked(1);
				}
				
				i = removed.iterator();
				while (i.hasNext()) {
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(tu.getElementName());
					removeTU(tu);
					monitor.worked(1);
				}
				
				String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
						+ "/debug/pdomtimings"); //$NON-NLS-1$
				if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
					System.out.println("PDOM Full Delta Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
			}
		
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		} finally {
			pdom.fireChange();
		}
	}

	protected void processDelta(ICElementDelta delta) throws CoreException {
		int flags = delta.getFlags();
		
		if ((flags & ICElementDelta.F_CHILDREN) != 0) {
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i]);
			}
		}
		
		ICElement element = delta.getElement();
		switch (element.getElementType()) {
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit)element;
			switch (delta.getKind()) {
			case ICElementDelta.CHANGED:
				if ((flags & ICElementDelta.F_CONTENT) != 0)
					changed.add(tu);
				break;
			case ICElementDelta.ADDED:
				if (!tu.isWorkingCopy())
					added.add(tu);
				break;
			case ICElementDelta.REMOVED:
				if (!tu.isWorkingCopy())
					removed.add(tu);
				break;
			}
			break;
		}
	}
	
	protected void addTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		IPath path = ((IFile)tu.getResource()).getLocation();
		runCtags(path);
	}
	
	protected void changeTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		IPath path = ((IFile)tu.getResource()).getLocation();
		pdom.acquireWriteLock();
		try {
			// Remove the old symbols in the tu
			PDOMFile file = pdom.getFile(path);
			if (file != null)
				file.clear();

		} finally {
			pdom.releaseWriteLock();
		}
		
		// Add the new symbols
		runCtags(path);
	}

	protected void removeTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		pdom.acquireWriteLock();
		try {
			IPath path = ((IFile)tu.getResource()).getLocation();
			PDOMFile file = pdom.getFile(path);
			if (file != null)
				file.clear();
		} finally {
			pdom.releaseWriteLock();
		}
	}

}
