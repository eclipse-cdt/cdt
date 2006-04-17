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

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class CtagsReindex extends Job {

	private final CtagsIndexer indexer;
	private final PDOM pdom;
	
	public CtagsReindex(CtagsIndexer indexer) {
		super("Ctags Indexer");
		this.indexer = indexer;
		this.pdom = (PDOM)indexer.getPDOM();
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			// Index the include path
			indexIncludes();
			
			// Index the source roots
			final ICProject project = pdom.getProject();
			ISourceRoot[] sourceRoots = project.getAllSourceRoots();
			
		    for (int i = 0; i < sourceRoots.length; ++i) {
				ISourceRoot sourceRoot = sourceRoots[i];
				IPath sourcePath = sourceRoot.getResource().getLocation();
				if (sourcePath != null)
					indexer.runCtags(sourcePath);
		    }
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	protected void indexIncludes() throws CoreException {
		ICProject project = pdom.getProject();
		IIncludeReference[] includes = project.getIncludeReferences();
		
		// This project has no references, don't bother processing any further
		if (includes.length == 0)
			return;
		
		// Find common prefix paths
		for (int i = 0; i < includes.length; ++i) {
			if (includes[i] == null)
				continue;
			IPath pathi = includes[i].getPath();
			for (int j = i + 1; j < includes.length; ++j) {
				if (includes[j] == null)
					continue;
				IPath pathj = includes[j].getPath();
				if (pathi.isPrefixOf(pathj)) {
					includes[j] = null;
				} else if (pathj.isPrefixOf(pathi)) {
					includes[i] = null;
					break;
				}
			}
		}
		
		includes = (IIncludeReference[])ArrayUtil.removeNulls(IIncludeReference.class, includes);
		for (int i = 0; i < includes.length; ++i) {
			indexer.runCtags(includes[i].getPath());
		}
	}
	
	
}
