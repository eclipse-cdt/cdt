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

package org.eclipse.cdt.internal.core.pdom;

import java.util.HashSet;

import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class IndexUpdatePolicy {

	public static final int POST_CHANGE = 0;
	public static final int POST_BUILD = 1;
	public static final int MANUAL= 2;
	
	private final ICProject fCProject;
	private int fKind;
	
	private HashSet fAdded= new HashSet();
	private HashSet fChanged= new HashSet();
	private HashSet fRemoved= new HashSet();
	private IPDOMIndexer fIndexer;

	public IndexUpdatePolicy(ICProject project, int kind) {
		fCProject= project;
		fKind= kind;
	}

	public ICProject getProject() {
		return fCProject;
	}

	public int getKind() {
		return fKind;
	}
	
	public IPDOMIndexerTask addDelta(ITranslationUnit[] added, ITranslationUnit[] changed,
			ITranslationUnit[] removed) {
		if (fIndexer != null && fIndexer.getID().equals(IPDOMManager.ID_NO_INDEXER)) {
			return null;
		}

		switch(fKind) {
		case IndexUpdatePolicy.MANUAL:
			return null;
		case IndexUpdatePolicy.POST_CHANGE:
			if (fIndexer != null) {
				return fIndexer.createTask(added, changed, removed);
			}
			break;
		}
		
		for (int i = 0; i < removed.length; i++) {
			ITranslationUnit tu = removed[i];
			fAdded.remove(tu);
			fChanged.remove(tu);
			fRemoved.add(tu);
		}
		for (int i = 0; i < added.length; i++) {
			ITranslationUnit tu = added[i];
			if (!fChanged.contains(tu)) {
				fAdded.add(tu);
			}
			fRemoved.remove(tu);
		}
		for (int i = 0; i < changed.length; i++) {
			ITranslationUnit tu = changed[i];
			if (!fAdded.contains(tu)) {
				fChanged.add(tu);
			}
			fRemoved.remove(tu);
		}
		return null;
	}

	public void clearTUs() {
		fAdded.clear();
		fChanged.clear();
		fRemoved.clear();
	}

	public boolean hasTUs() {
		return !(fAdded.isEmpty() && fChanged.isEmpty() && fRemoved.isEmpty());
	}

	public IPDOMIndexerTask createTask() {
		if (fIndexer != null && hasTUs()) {
			if (getKind() != IndexUpdatePolicy.MANUAL &&
					!fIndexer.getID().equals(IPDOMManager.ID_NO_INDEXER)) {
				return fIndexer.createTask(getAdded(), getChanged(), getRemoved());
			}
			clearTUs();
		}
		return null;
	}

	private ITranslationUnit[] getAdded() {
		return (ITranslationUnit[]) fAdded.toArray(new ITranslationUnit[fAdded.size()]);
	}

	private ITranslationUnit[] getChanged() {
		return (ITranslationUnit[]) fChanged.toArray(new ITranslationUnit[fChanged.size()]);
	}

	private ITranslationUnit[] getRemoved() {
		return (ITranslationUnit[]) fRemoved.toArray(new ITranslationUnit[fRemoved.size()]);
	}

	public void setIndexer(IPDOMIndexer indexer) {
		fIndexer= indexer;
	}

	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}
}
