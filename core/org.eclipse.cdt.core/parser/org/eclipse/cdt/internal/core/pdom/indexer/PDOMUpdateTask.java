/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

public class PDOMUpdateTask implements IPDOMIndexerTask {
	protected static final String TRUE= String.valueOf(true);
	protected static final ITranslationUnit[] NO_TUS = new ITranslationUnit[0];
	
	private final IPDOMIndexer fIndexer;
	private final IndexerProgress fProgress;
	private volatile IPDOMIndexerTask fDelegate;
	private boolean fCheckTimestamps= true;
	private ArrayList fFilesAndFolders= null;

	public PDOMUpdateTask(IPDOMIndexer indexer) {
		fIndexer= indexer;
		fProgress= createProgress();
	}

	private IndexerProgress createProgress() {
		IndexerProgress progress= new IndexerProgress();
		progress.fTimeEstimate= 1000;
		return progress;
	}

	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	public void run(IProgressMonitor monitor) {
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
	
	private synchronized void createDelegate(ICProject project, IProgressMonitor monitor) throws CoreException {
		boolean allFiles= TRUE.equals(fIndexer.getProperty(IndexerPreferences.KEY_INDEX_ALL_FILES));
		HashSet set= new HashSet();
		TranslationUnitCollector collector= new TranslationUnitCollector(set, set, allFiles, monitor);
		if (fFilesAndFolders == null) {
			project.accept(collector);
		}
		else {
			for (Iterator iterator = fFilesAndFolders.iterator(); iterator.hasNext();) {
				ICElement elem = (ICElement) iterator.next();
				elem.accept(collector);
			}
		}
		ITranslationUnit[] tus= (ITranslationUnit[]) set.toArray(new ITranslationUnit[set.size()]);
		fDelegate= fIndexer.createTask(tus, NO_TUS, NO_TUS);
		if (fDelegate instanceof PDOMIndexerTask) {
			((PDOMIndexerTask) fDelegate).setCheckTimestamps(fCheckTimestamps);
		}
	}


	public synchronized IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}

	public void setCheckTimestamps(boolean timestamps) {
		fCheckTimestamps= timestamps;
	}

	public void setTranslationUnitSelection(List filesAndFolders) {
		fFilesAndFolders= new ArrayList(filesAndFolders.size());
		fFilesAndFolders.addAll(filesAndFolders);
	}
}
