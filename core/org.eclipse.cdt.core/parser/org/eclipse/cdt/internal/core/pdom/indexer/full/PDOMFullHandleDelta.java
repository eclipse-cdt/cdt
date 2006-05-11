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

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

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
	
	public PDOMFullHandleDelta(PDOM pdom, ICElementDelta delta) {
		super(pdom);
		this.delta = delta;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
		
			processDelta(delta);
			
			int count = changed.size() + added.size() + removed.size();

			if (count > 0) {
				monitor.beginTask("Indexing", count);
					
				Iterator i = changed.values().iterator();
				while (i.hasNext()) {
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(tu.getElementName());
					try {
						changeTU(tu);
					} catch (Throwable e) {
						CCorePlugin.log(e);
						if (++errorCount > MAX_ERRORS)
							return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
				}
				
				i = added.iterator();
				while (i.hasNext()) {
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(tu.getElementName());
					try {
						addTU(tu);
					} catch (Throwable e) {
						CCorePlugin.log(e);
						if (++errorCount > MAX_ERRORS)
							return Status.CANCEL_STATUS;
					}
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
		PDOMFile pdomFile = pdom.getFile(path);
		boolean found = false;
		if (pdomFile != null) {
			// Look for all source units in the included list,
			// If none, then add the header
			PDOMFile[] includedBy = pdomFile.getAllIncludedBy();
			if (includedBy.length > 0) {
				IProject project = tu.getCProject().getProject();
				for (int i = 0; i < includedBy.length; ++i) {
					String incfilename = includedBy[i].getFileName().getString();
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
	
	protected void changeTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		IASTTranslationUnit ast = parse(tu);
		if (ast == null)
			return;

		// Remove the old symbols in the tu and all the headers
		pdom.acquireWriteLock();
		try {
			IPath path = ((IFile)tu.getResource()).getLocation();
			PDOMFile file = pdom.getFile(path);
			if (file != null)
				file.clear();
	
			IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
			for (int i = 0; i < includes.length; ++i) {
				String incname = includes[i].getFileLocation().getFileName();
				PDOMFile incfile = pdom.getFile(incname);
				if (incfile != null)
					incfile.clear();
			}
			
			// Add the new symbols
			addSymbols(tu.getLanguage(), ast);
		} finally {
			pdom.releaseWriteLock();
		}
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
