/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Markus Schorn - initial API and implementation
 *	   Sergey Prigogin (Google)
******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.io.File;
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
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeSearchPath;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeSearchPathElement;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility;
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
	private static final ITranslationUnit[] NO_TUS = {};
	
	private final IPDOMIndexer fIndexer;
	private final int fUpdateOptions;
	private final IndexerProgress fProgress;
	private volatile IPDOMIndexerTask fDelegate;
	private ArrayList<ICElement> fFilesAndFolders;

	public PDOMUpdateTask(IPDOMIndexer indexer, int updateOptions) {
		fIndexer= indexer;
		fUpdateOptions= updateOptions;
		fProgress= createProgress();
	}

	private IndexerProgress createProgress() {
		IndexerProgress progress= new IndexerProgress();
		progress.fTimeEstimate= 1000;
		return progress;
	}

	@Override
	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	@Override
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
	
	private void createDelegate(ICProject project, IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		HashSet<ITranslationUnit> set= new HashSet<ITranslationUnit>();
		if ((fUpdateOptions & (IIndexManager.UPDATE_ALL | IIndexManager.UPDATE_CHECK_TIMESTAMPS)) != 0) {
			TranslationUnitCollector collector= new TranslationUnitCollector(set, set, monitor);
			boolean haveProject= false;
			if (fFilesAndFolders == null) {
				project.accept(collector);
			} else {
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
							ITranslationUnit tu = getTranslationUnit(floc, project);
							if (tu != null) {
								set.add(tu);
							}
						}
					}
				} finally {
					index.releaseReadLock();
				}
			}
		}

		if ((fUpdateOptions & IIndexManager.UPDATE_UNRESOLVED_INCLUDES) != 0) {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project);
			index.acquireReadLock();
			try {
				// Files that were indexed with I/O errors.
				IIndexFile[] files= index.getDefectiveFiles();
				for (IIndexFile file : files) {
					ITranslationUnit tu = getTranslationUnit(file.getLocation(), project);
					if (tu != null) {
						set.add(tu);
					}
				}

				// Files with unresolved includes.
				files= index.getFilesWithUnresolvedIncludes();
				if (files.length > 0) {
					ProjectIndexerInputAdapter inputAdapter = new ProjectIndexerInputAdapter(project, true);
			        ProjectIndexerIncludeResolutionHeuristics includeResolutionHeuristics =
			        		new ProjectIndexerIncludeResolutionHeuristics(project.getProject(), inputAdapter);
			        for (IIndexFile file : files) {
						ITranslationUnit tu = getTranslationUnit(file.getLocation(), project);
						if (tu != null) {
							IScannerInfo scannerInfo = tu.getScannerInfo(true);
							if (canResolveUnresolvedInclude(file, scannerInfo, includeResolutionHeuristics)) {
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
		setDelegate(delegate);
	}

	private ITranslationUnit getTranslationUnit(IIndexFileLocation location, ICProject project) {
		IPath path= IndexLocationFactory.getAbsolutePath(location);
		if (path == null)
			return null;
		ITranslationUnit tu= CoreModel.getDefault().createTranslationUnitFrom(project, path);
		if (tu != null) {
			final String fullPath = location.getFullPath();
			if (fullPath != null) {
				if (tu instanceof ExternalTranslationUnit) {
					IResource file= ResourcesPlugin.getWorkspace().getRoot().findMember(fullPath);
					if (file instanceof IFile) {
						((ExternalTranslationUnit) tu).setResource((IFile) file);
					}
				}
			}
		}
		return tu;
	}

	private static boolean canResolveUnresolvedInclude(IIndexFile file, IScannerInfo scannerInfo,
			ProjectIndexerIncludeResolutionHeuristics includeResolutionHeuristics) {
		try {
			String filePath = IndexLocationFactory.getAbsolutePath(file.getLocation()).toOSString();
			long fileReadTime = file.getSourceReadTime();
			IncludeSearchPath includeSearchPath =
					CPreprocessor.configureIncludeSearchPath(new File(filePath).getParentFile(), scannerInfo);
			for (IIndexInclude include : file.getIncludes()) {
				if (!include.isResolved() && include.isActive() &&
						canResolveInclude(include, filePath, fileReadTime, includeSearchPath, includeResolutionHeuristics)) {
					return true;
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return false;
	}

	private static boolean canResolveInclude(IIndexInclude include, String currentFile, long timestamp,
			IncludeSearchPath includeSearchPath,
			ProjectIndexerIncludeResolutionHeuristics includeResolutionHeuristics) throws CoreException {
		String includeName = include.getFullName();
        String filePath = CPreprocessor.getAbsoluteInclusionPath(includeName, currentFile);
        if (filePath != null && fileIsNotOlderThanTimestamp(filePath, timestamp)) {
        	return true;
        }

        if (currentFile != null && !include.isSystemInclude() && !includeSearchPath.isInhibitUseOfCurrentFileDirectory()) {
            // Check to see if we find a match in the current directory
    		final File currentDir= new File(currentFile).getParentFile();
    		if (currentDir != null) {
        		filePath = ScannerUtility.createReconciledPath(currentDir.getAbsolutePath(), includeName);
        		if (!filePath.equals(currentFile) && fileIsNotOlderThanTimestamp(filePath, timestamp)) {
        			return true;
        		}
    		}
        }

        // Unlike CPreprocessor.findInclusion we are searching include path from the beginning.
        // This simplification may produce false positives, but by checking file modification time
        // we guarantee that any false positive won't be produced again when this task runs
        // next time.
        for (IncludeSearchPathElement path : includeSearchPath.getElements()) {
        	if (!include.isSystemInclude() || !path.isForQuoteIncludesOnly()) {
        		filePath = path.getLocation(includeName);
        		if (filePath != null && fileIsNotOlderThanTimestamp(filePath, timestamp)) {
        			return true;
        		}
        	}
        }
        if (includeResolutionHeuristics != null) {
        	filePath= includeResolutionHeuristics.findInclusion(includeName, currentFile);
    		if (filePath != null && fileIsNotOlderThanTimestamp(filePath, timestamp)) {
    			return true;
    		}
        }

        return false;
	}

	/**
	 * Returns true if the file exists and is not older than the given timestamp.
	 */
	private static boolean fileIsNotOlderThanTimestamp(String filename, long timestamp) {
		// We are subtracting 1 second from the timestamp to account for limited precision
		// of File.lastModified() method and possible skew between clocks on a multi-CPU
		// system. This may produce false positives, but they are pretty harmless.
		return new File(filename).lastModified() >= timestamp - 1000;
	}

	private synchronized void setDelegate(IPDOMIndexerTask delegate) {
		fDelegate= delegate;
	}

	@Override
	public synchronized IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}

	@Override
	public synchronized boolean acceptUrgentTask(IPDOMIndexerTask task) {
		return fDelegate != null && fDelegate.acceptUrgentTask(task);
	}

	public void setTranslationUnitSelection(List<? extends ICElement> filesAndFolders) {
		fFilesAndFolders= new ArrayList<ICElement>(filesAndFolders);
	}
}
