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
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class PDOMResourceDeltaTask implements IPDOMIndexerTask {
	private static final String TRUE = String.valueOf(true);

	final private IPDOMIndexer fIndexer;
	final private boolean fAllFiles;
	final private IPDOMIndexerTask fDelegate;
	final private IndexerProgress fProgress;

	private IIndex fIndex;

	public PDOMResourceDeltaTask(IPDOMIndexer indexer, ICElementDelta delta) throws CoreException {
		fIndexer= indexer;
		fProgress= new IndexerProgress();
		fAllFiles= TRUE.equals(getIndexer().getProperty(IndexerPreferences.KEY_INDEX_ALL_FILES));
		if (!IPDOMManager.ID_NO_INDEXER.equals(fIndexer.getID())) {
			List a= new ArrayList();
			List c= new ArrayList();
			List r= new ArrayList();
			
			fIndex= CCorePlugin.getIndexManager().getIndex(indexer.getProject());
			try {
				fIndex.acquireReadLock();
			} catch (InterruptedException e) {
				fDelegate= null;
				return;
			}
			try {
				processDelta(delta, a, c, r, new NullProgressMonitor());
				if (!a.isEmpty() || !c.isEmpty() || !r.isEmpty()) {
					ITranslationUnit[] aa= (ITranslationUnit[]) a.toArray(new ITranslationUnit[a.size()]);
					ITranslationUnit[] ca= (ITranslationUnit[]) c.toArray(new ITranslationUnit[c.size()]);
					ITranslationUnit[] ra= (ITranslationUnit[]) r.toArray(new ITranslationUnit[r.size()]);
					fDelegate= indexer.createTask(aa, ca, ra);
					if (fDelegate instanceof PDOMIndexerTask) {
						((PDOMIndexerTask) fDelegate).setCheckTimestamps(true);
					}
				}
				else {
					fDelegate= null;
				}
			}
			finally {
				fIndex.releaseReadLock();
			}
		}
		else {
			fDelegate= null;
		}
	}
	
	private void processDelta(ICElementDelta delta, List added, List changed, List removed, IProgressMonitor pm) throws CoreException {
		int flags = delta.getFlags();

		if ((flags & ICElementDelta.F_CHILDREN) != 0) {
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i], added, changed, removed, pm);
			}
		}

		ICElement element = delta.getElement();
		switch (element.getElementType()) {
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit)element;
			if (!tu.isWorkingCopy()) {
				switch (delta.getKind()) {
				case ICElementDelta.CHANGED:
					if ((flags & ICElementDelta.F_CONTENT) != 0) {
						if (fAllFiles || !CoreModel.isScannerInformationEmpty(tu.getResource()) || tu.isHeaderUnit()) {
							changed.add(tu);
						}
					}
					break;
				case ICElementDelta.ADDED:
					if (fAllFiles || !CoreModel.isScannerInformationEmpty(tu.getResource()) || tu.isHeaderUnit()) {
						added.add(tu);
					}
					break;
				case ICElementDelta.REMOVED:
					removed.add(tu);
					break;
				}
			}
			break;
		case ICElement.C_CCONTAINER:
			ICContainer folder= (ICContainer) element;
			if (delta.getKind() == ICElementDelta.ADDED) {
				collectSources(folder, added, pm);
			}
			break;
		}
	}

	private void collectSources(ICContainer container, Collection sources, IProgressMonitor pm) throws CoreException {
		container.accept(new TranslationUnitCollector(sources, sources, fAllFiles, pm));
	}

	public void run(IProgressMonitor monitor) {
		if (fDelegate != null) {
			fDelegate.run(monitor);
		}
	}

	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	public IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}

	public boolean isEmpty() {
		return fDelegate == null;
	}
}
