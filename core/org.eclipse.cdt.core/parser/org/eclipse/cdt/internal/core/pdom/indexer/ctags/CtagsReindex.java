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

import java.util.Arrays;

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

import com.sun.corba.se.impl.interceptors.PINoOpHandlerImpl;

/**
 * @author Doug Schaefer
 *
 */
public class CtagsReindex extends Job {

	private final CtagsIndexer indexer;
	private final PDOM pdom;
	
	public CtagsReindex(CtagsIndexer indexer) {
		super("ctags Indexer: " + ((PDOM)indexer.getPDOM()).getProject().getElementName());
		this.indexer = indexer;
		this.pdom = (PDOM)indexer.getPDOM();
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			// What do we need to index
			final ICProject project = pdom.getProject();
			final IIncludeReference[] pincludes = project.getIncludeReferences();
			IIncludeReference[] includes = new IIncludeReference[pincludes.length];
			System.arraycopy(pincludes, 0, includes, 0, pincludes.length);
			
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
			
			ISourceRoot[] sourceRoots = project.getAllSourceRoots();

			monitor.beginTask("Indexing", sourceRoots.length + includes.length);
			
			// Clear out the PDOM
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			monitor.subTask("Clearing Index");
			pdom.clear();
			monitor.worked(1);
			
			// Index the include path
			for (int i = 0; i < includes.length; ++i) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.subTask(includes[i].getElementName());
				indexer.runCtags(includes[i].getPath());
				monitor.worked(1);
			}
			
			// Index the source roots
		    for (int i = 0; i < sourceRoots.length; ++i) {
				ISourceRoot sourceRoot = sourceRoots[i];
				IPath sourcePath = sourceRoot.getResource().getLocation();
				if (sourcePath != null) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					monitor.subTask(sourceRoot.getElementName());
					indexer.runCtags(sourcePath);
					monitor.worked(1);
				}
		    }
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
		    monitor.done();
		}
		return Status.OK_STATUS;
	}

}
