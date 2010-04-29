/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * A task for updating an index, suitable for all indexers.
 */
public class PDOMUpdateTask implements IPDOMIndexerTask {
	protected static final String TRUE= String.valueOf(true);
	protected static final ITranslationUnit[] NO_TUS = new ITranslationUnit[0];
	
	private final IPDOMIndexer fIndexer;
	private final IndexerProgress fProgress;
	private final int fUpdateOptions;
	private volatile IPDOMIndexerTask fDelegate;
	private ArrayList<ICElement> fFilesAndFolders= null;

	public PDOMUpdateTask(IPDOMIndexer indexer, int updateOptions) {
		fIndexer= indexer;
		fProgress= createProgress();
		fUpdateOptions= updateOptions;
	}

	private IndexerProgress createProgress() {
		IndexerProgress progress= new IndexerProgress();
		progress.fTimeEstimate= 1000;
		return progress;
	}

	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	public void run(IProgressMonitor monitor) throws InterruptedException {
		monitor.subTask(NLS.bind(Messages.PDOMIndexerTask_collectingFilesTask, 
				fIndexer.getProject().getElementName()));

		ICProject project= fIndexer.getProject();
		if (project.getProject().isOpen()) {
			try {
				if (!IPDOMManager.ID_NO_INDEXER.equals(fIndexer.getID())) {
					createDelegate(project, monitor);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			} 
		}
		
		if (fDelegate != null) {
			fDelegate.run(monitor);
		}
	}
	
	private void createDelegate(ICProject project, IProgressMonitor monitor) throws CoreException, InterruptedException {
		HashSet<ITranslationUnit> set= new HashSet<ITranslationUnit>();
		TranslationUnitCollector collector= new TranslationUnitCollector(set, set, monitor);
		boolean haveProject= false;
		if (fFilesAndFolders == null) {
			project.accept(collector);
		}
		else {
			for (ICElement elem : fFilesAndFolders) {
				if (elem.getElementType() == ICElement.C_PROJECT) {
					haveProject= true;
				}
				elem.accept(collector);
			}
		}
		if (haveProject && (fUpdateOptions & IIndexManager.UPDATE_EXTERNAL_FILES_FOR_PROJECT) != 0) {
			final String projectPrefix= project.getProject().getFullPath().toString() + IPath.SEPARATOR;
			IIndex index= CCorePlugin.getIndexManager().getIndex(project);
			index.acquireReadLock();
			try {
				IIndexFile[] files= index.getAllFiles();
				for (IIndexFile indexFile : files) {
					IIndexFileLocation floc= indexFile.getLocation();
					final String fullPath = floc.getFullPath();
					if (fullPath == null || !fullPath.startsWith(projectPrefix)) {
						IPath path= IndexLocationFactory.getAbsolutePath(floc);
						if (path != null) {
							ITranslationUnit tu= CoreModel.getDefault().createTranslationUnitFrom(project, path);
							if (tu != null) {
								if (fullPath != null) {
									if (tu instanceof ExternalTranslationUnit) {
										IResource file= ResourcesPlugin.getWorkspace().getRoot().findMember(fullPath);
										if (file instanceof IFile) {
											((ExternalTranslationUnit) tu).setResource((IFile) file);
										}
									}
								}
								set.add(tu);
							}
						}
					}
				}
			} finally {
				index.releaseReadLock();
			}
		}
		ITranslationUnit[] tus= set.toArray(new ITranslationUnit[set.size()]);
		IPDOMIndexerTask delegate= fIndexer.createTask(NO_TUS, tus, NO_TUS);
		if (delegate instanceof PDOMIndexerTask) {
			final PDOMIndexerTask task = (PDOMIndexerTask) delegate;
			task.setUpdateFlags(fUpdateOptions);
		}
		synchronized (this) {
			fDelegate= delegate;
		}
	}

	public synchronized IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}

	public void setTranslationUnitSelection(List<ICElement> filesAndFolders) {
		fFilesAndFolders= new ArrayList<ICElement>(filesAndFolders.size());
		fFilesAndFolders.addAll(filesAndFolders);
	}
}
