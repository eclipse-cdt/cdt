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
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
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
class PDOMFullHandleDelta extends PDOMFullIndexerJob {

	// Map of filename, TU of files that need to be parsed.
	private Map changedMap = new HashMap();
	private List changed = new ArrayList();
	private List added = new ArrayList();
	private List removed = new ArrayList();
	private volatile int fFilesToIndex= 0;
	
	public PDOMFullHandleDelta(PDOMFullIndexer indexer, ICElementDelta delta) throws CoreException {
		super(indexer);
		processDelta(delta, added, changed, removed);
	}

	public void run(IProgressMonitor monitor) {
		setupIndexAndReaderFactory();
		try {
			long start = System.currentTimeMillis();
					
			int count = changed.size() + added.size() + removed.size();

			for (Iterator iter = changed.iterator(); iter.hasNext();) {
				ITranslationUnit tu = (ITranslationUnit) iter.next();
				processTranslationUnit(tu);
				fFilesToIndex--;
			}
			changed.clear();
			
			if (count > 0) {
				Iterator i = changedMap.values().iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					changeTU(tu);
					fFilesToIndex--;
				}
				
				i = added.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					changeTU(tu);
					fFilesToIndex--;
				}
				
				i = removed.iterator();
				while (i.hasNext()) {
					if (monitor.isCanceled())
						return;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					removeTU(index, tu);
					fFilesToIndex--;
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
						if (changedMap.get(incfilename) == null) {
							IFile[] rfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(incfilename));
							for (int j = 0; j < rfiles.length; ++j) {
								if (rfiles[j].getProject().equals(project)) {
									ITranslationUnit inctu = (ITranslationUnit)CoreModel.getDefault().create(rfiles[j]);
									changedMap.put(incfilename, inctu);
									found = true;
									fFilesToIndex++;
								}
							}
						}
					}
				}
			}
		}
		if (!found) {
			changedMap.put(path.toOSString(), tu);
			fFilesToIndex++;
		}
	}

	public int getFilesToIndexCount() {
		return fFilesToIndex;
	}
}
