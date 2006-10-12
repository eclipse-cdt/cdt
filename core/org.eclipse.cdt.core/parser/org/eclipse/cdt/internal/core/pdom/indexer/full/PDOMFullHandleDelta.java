/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFullHandleDelta extends PDOMFullIndexerJob {

	private final ICElementDelta delta;
	
	// Map of filename, TU of files that need to be parsed.
	private Map changed = new HashMap();
	private List added = new ArrayList();
	private List removed = new ArrayList();
	
	public PDOMFullHandleDelta(PDOMFullIndexer indexer, ICElementDelta delta) throws CoreException {
		super(indexer);
		this.delta = delta;
	}

	public void run(IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
		
			processDelta(delta);
			
			int count = changed.size() + added.size() + removed.size();

			if (count > 0) {
				Iterator i = changed.values().iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					try {
						changeTU(tu);
					} catch (Throwable e) {
						CCorePlugin.log(e);
						if (++errorCount > MAX_ERRORS)
							return;
					}
				}
				
				i = added.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					try {
						addTU(tu);
					} catch (Throwable e) {
						CCorePlugin.log(e);
						if (++errorCount > MAX_ERRORS)
							return;
					}
				}
				
				i = removed.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					removeTU(tu);
				}
				
				String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
						+ "/debug/pdomtimings"); //$NON-NLS-1$
				if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
					System.out.println("PDOM Full Delta Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
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
					processTranslationUnit(tu);
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
	
	protected void processTranslationUnit(ITranslationUnit tu) throws CoreException {
		IPath path = tu.getUnderlyingResource().getLocation();
		IIndexFile pdomFile= index.getFile(path);
		boolean found = false;
		if (pdomFile != null) {
			// Look for all source units in the included list,
			// If none, then add the header
			IIndexInclude[] includedBy = index.findIncludedBy(pdomFile, IIndex.DEPTH_INFINITE); 
			if (includedBy.length > 0) {
				IProject project = tu.getCProject().getProject();
				for (int i = 0; i < includedBy.length; ++i) {
					String incfilename = includedBy[i].getIncludedByLocation();
					if (CoreModel.isValidSourceUnitName(project, incfilename)) {
						if (changed.get(incfilename) == null) {
							IFile[] rfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(incfilename));
							for (int j = 0; j < rfiles.length; ++j) {
								if (rfiles[j].getProject().equals(project)) {
									ITranslationUnit inctu = (ITranslationUnit)CoreModel.getDefault().create(rfiles[j]);
									changed.put(incfilename, inctu);
									found = true;
								}
							}
						}
					}
				}
			}
		}
		if (!found)
			changed.put(path.toOSString(), tu);
	}

	protected void removeTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		index.acquireWriteLock(0);
		try {
			IPath path = ((IFile)tu.getResource()).getLocation();
			IIndexFragmentFile file = (IIndexFragmentFile) index.getFile(path);
			if (file != null)
				index.clearFile(file);
		} finally {
			index.releaseWriteLock(0);
		}
	}

}
