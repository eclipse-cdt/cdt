/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public abstract class PDOMIndexerTask implements IPDOMIndexerTask {
	private static final Object NO_CONTEXT = new Object();
	protected static final int MAX_ERRORS = 10;

	protected int fErrorCount;
	protected Map fContextMap= new HashMap();
	
	protected void processDelta(ICElementDelta delta, Collection added, Collection changed, Collection removed) throws CoreException {
		int flags = delta.getFlags();
		
		if ((flags & ICElementDelta.F_CHILDREN) != 0) {
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i], added, changed, removed);
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
	
	protected void collectSources(ICProject project, final Collection sources, final Collection headers) throws CoreException {
		project.accept(new ICElementVisitor() {
			public boolean visit(ICElement element) throws CoreException {
				switch (element.getElementType()) {
				case ICElement.C_UNIT:
					ITranslationUnit tu = (ITranslationUnit)element;
					if (tu.isSourceUnit()) {
						sources.add(tu);
					}
					else if (tu.isHeaderUnit()) {
						headers.add(tu);
					}
					return false;
				case ICElement.C_CCONTAINER:
				case ICElement.C_PROJECT:
					return true;
				}
				return false;
			}
		});
	}

	protected void removeTU(IWritableIndex index, ITranslationUnit tu) throws CoreException, InterruptedException {
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
	
	protected void parseTU(ITranslationUnit tu) throws CoreException, InterruptedException {
		try {
			doParseTU(tu);
		}
		catch (CoreException e) {
			if (++fErrorCount <= MAX_ERRORS) {
				CCorePlugin.log(e);
			}
			else {
				throw e;
			}
		}
	}

	abstract protected void doParseTU(ITranslationUnit tu) throws CoreException, InterruptedException;
	
	protected void clearIndex(IWritableIndex index) throws InterruptedException, CoreException {
		// reset error count
		fErrorCount= 0;
		// First clear the pdom
		index.acquireWriteLock(0);
		try {
			index.clear();
		}
		finally {
			index.releaseWriteLock(0);
		}
	}
	
	protected boolean indexAllHeaders() {
		return getIndexer().getIndexAllHeaders();
	}
	
	protected ITranslationUnit findContext(IIndex index, String path) {
		Object cachedContext= fContextMap.get(path);
		if (cachedContext != null) {
			return cachedContext == NO_CONTEXT ? null : (ITranslationUnit) cachedContext;
		}
		
		ITranslationUnit context= null;
		fContextMap.put(path, NO_CONTEXT); // prevent recursion
		IIndexFile pdomFile;
		try {
			pdomFile = index.getFile(new Path(path));
			if (pdomFile != null) {
				ICProject project= getIndexer().getProject();
				IIndexInclude[] includedBy = index.findIncludedBy(pdomFile, IIndex.DEPTH_ZERO);
				ArrayList paths= new ArrayList(includedBy.length);
				for (int i = 0; i < includedBy.length; i++) {
					IIndexInclude include = includedBy[i];
					String incfilename = include.getIncludedByLocation();
					if (CoreModel.isValidSourceUnitName(project.getProject(), incfilename)) {
						context= CoreModelUtil.findTranslationUnitForLocation(new Path(incfilename), project);
						if (context != null) {
							fContextMap.put(path, context);
							return context;
						}
					}
					paths.add(incfilename);
				}
				for (Iterator iter = paths.iterator(); iter.hasNext();) {
					String nextLevel = (String) iter.next();
					context= findContext(index, nextLevel);
					if (context != null) {
						fContextMap.put(path, context);
						return context;
					}
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
