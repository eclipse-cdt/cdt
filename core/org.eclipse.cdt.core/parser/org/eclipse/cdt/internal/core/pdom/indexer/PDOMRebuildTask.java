/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * A task for rebuilding an index, works for all indexers.
 */
public class PDOMRebuildTask implements IPDOMIndexerTask {
	protected static final String TRUE = String.valueOf(true);
	protected static final ITranslationUnit[] NO_TUS = {};

	private final IPDOMIndexer fIndexer;
	private final IndexerProgress fProgress;
	private volatile IPDOMIndexerTask fDelegate;
	private IProgressMonitor fProgressMonitor;

	public PDOMRebuildTask(IPDOMIndexer indexer) {
		fIndexer = indexer;
		fProgress = createProgress();
	}

	private IndexerProgress createProgress() {
		IndexerProgress progress = new IndexerProgress();
		progress.fTimeEstimate = 1000;
		return progress;
	}

	@Override
	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InterruptedException {
		fProgressMonitor = monitor;
		try {
			monitor.subTask(
					NLS.bind(Messages.PDOMIndexerTask_collectingFilesTask, fIndexer.getProject().getElementName()));

			ICProject cproject = fIndexer.getProject();
			IProject project = cproject.getProject();
			if (project.isOpen() && project.exists()) {
				try {
					IWritableIndex index = ((IWritableIndexManager) CCorePlugin.getIndexManager())
							.getWritableIndex(cproject);
					if (index != null) {
						clearIndex(cproject, index);
						if (!IPDOMManager.ID_NO_INDEXER.equals(fIndexer.getID())) {
							createDelegate(cproject, monitor);
						}
					}
					// Remove task-tags.
					TodoTaskUpdater.removeTasksFor(project);
				} catch (CoreException e) {
					CCorePlugin.log(NLS.bind(Messages.PDOMRebuildTask_0, cproject.getElementName()), e);
				} catch (InterruptedException e) {
				}
			}

			if (fDelegate != null) {
				fDelegate.run(monitor);
			}
		} finally {
			fProgressMonitor = null;
		}
	}

	private void clearIndex(ICProject project, IWritableIndex index) throws CoreException, InterruptedException {
		// First clear the pdom
		index.acquireWriteLock(fProgressMonitor);
		try {
			index.clear();
			IWritableIndexFragment wf = index.getWritableFragment();
			if (wf instanceof WritablePDOM) {
				PDOMManager.writeProjectPDOMProperties((WritablePDOM) wf, project.getProject());
			}
		} finally {
			index.releaseWriteLock();
		}
	}

	private void createDelegate(ICProject project, IProgressMonitor monitor) throws CoreException {
		boolean allFiles = TRUE
				.equals(fIndexer.getProperty(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG))
				|| TRUE.equals(fIndexer.getProperty(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG));
		List<ITranslationUnit> sources = new ArrayList<>();
		List<ITranslationUnit> headers = allFiles ? sources : null;
		TranslationUnitCollector collector = new TranslationUnitCollector(sources, headers, monitor);
		project.accept(collector);
		ITranslationUnit[] tus = sources.toArray(new ITranslationUnit[sources.size()]);
		IPDOMIndexerTask delegate = fIndexer.createTask(tus, NO_TUS, NO_TUS);
		if (delegate instanceof PDOMIndexerTask) {
			final PDOMIndexerTask pdomIndexerTask = (PDOMIndexerTask) delegate;
			pdomIndexerTask.setUpdateFlags(IIndexManager.UPDATE_ALL);
			pdomIndexerTask.setWriteInfoToLog();
		}
		synchronized (this) {
			fDelegate = delegate;
		}
	}

	@Override
	public synchronized IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}

	@Override
	public synchronized boolean acceptUrgentTask(IPDOMIndexerTask task) {
		return fDelegate != null && fDelegate.acceptUrgentTask(task);
	}

	@Override
	public void cancel() {
		if (fDelegate != null)
			fDelegate.cancel();
	}
}
