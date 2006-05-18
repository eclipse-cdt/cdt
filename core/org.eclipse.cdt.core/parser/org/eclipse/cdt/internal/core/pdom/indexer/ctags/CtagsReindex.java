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
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class CtagsReindex extends CtagsIndexerJob {

	public CtagsReindex(CtagsIndexer indexer) throws CoreException {
		super(indexer);
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			// What do we need to index
			final ICProject project = indexer.getProject();
//			final IIncludeReference[] pincludes = project.getIncludeReferences();
//			IIncludeReference[] includes = new IIncludeReference[pincludes.length];
//			System.arraycopy(pincludes, 0, includes, 0, pincludes.length);
			
			// Find common prefix paths
//			for (int i = 0; i < includes.length; ++i) {
//				if (includes[i] == null)
//					continue;
//				IPath pathi = includes[i].getPath();
//				for (int j = i + 1; j < includes.length; ++j) {
//					if (includes[j] == null)
//						continue;
//					IPath pathj = includes[j].getPath();
//					if (pathi.isPrefixOf(pathj)) {
//						includes[j] = null;
//					} else if (pathj.isPrefixOf(pathi)) {
//						includes[i] = null;
//						break;
//					}
//				}
//			}
			
//			includes = (IIncludeReference[])ArrayUtil.removeNulls(IIncludeReference.class, includes);
			
			ISourceRoot[] sourceRoots = project.getAllSourceRoots();

			monitor.beginTask("Indexing", sourceRoots.length + 1);
//					+ includes.length + 1);
			
			// Clear out the PDOM
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			monitor.subTask("Clearing Index");
			pdom.clear();
			monitor.worked(1);
			
			// Index the include path
//			for (int i = 0; i < includes.length; ++i) {
//				if (monitor.isCanceled())
//					return Status.CANCEL_STATUS;
//				monitor.subTask(includes[i].getElementName());
//				runCtags(includes[i].getPath());
//				monitor.worked(1);
//			}
			
			// Index the source roots
		    for (int i = 0; i < sourceRoots.length; ++i) {
				ISourceRoot sourceRoot = sourceRoots[i];
				IPath sourcePath = sourceRoot.getResource().getLocation();
				if (sourcePath != null) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					monitor.subTask(sourceRoot.getElementName());
					runCtags(sourcePath);
					monitor.worked(1);
				}
		    }
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
		    monitor.done();
		    pdom.fireChange();
		}
		return Status.OK_STATUS;
	}

}
