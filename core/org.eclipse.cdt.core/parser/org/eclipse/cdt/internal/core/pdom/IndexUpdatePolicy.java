/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.HashSet;

import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMUpdateTask;

public class IndexUpdatePolicy {
	public static final int POST_CHANGE = IndexerPreferences.UPDATE_POLICY_IMMEDIATE;
	public static final int POST_BUILD = IndexerPreferences.UPDATE_POLICY_LAZY;
	public static final int MANUAL = IndexerPreferences.UPDATE_POLICY_MANUAL;

	private final ICProject fCProject;
	private int fKind;

	private HashSet<ITranslationUnit> fForce = new HashSet<>();
	private HashSet<ITranslationUnit> fTimestamp = new HashSet<>();
	private HashSet<ITranslationUnit> fRemoved = new HashSet<>();
	private IPDOMIndexer fIndexer;
	private boolean fReindexRequested;

	public IndexUpdatePolicy(ICProject project, int kind) {
		fCProject = project;
		fKind = getLegalPolicy(kind);
	}

	private int getLegalPolicy(int kind) {
		switch (kind) {
		case POST_BUILD:
		case POST_CHANGE:
		case MANUAL:
			return kind;
		}
		return POST_CHANGE;
	}

	public ICProject getProject() {
		return fCProject;
	}

	public void clearTUs() {
		fForce.clear();
		fTimestamp.clear();
		fRemoved.clear();
	}

	public boolean hasTUs() {
		return !(fForce.isEmpty() && fTimestamp.isEmpty() && fRemoved.isEmpty());
	}

	public void setIndexer(IPDOMIndexer indexer) {
		fIndexer = indexer;
	}

	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	public boolean isAutomatic() {
		return fKind != MANUAL;
	}

	public IPDOMIndexerTask handleDelta(ITranslationUnit[] force, ITranslationUnit[] changed,
			ITranslationUnit[] removed) {
		if (isNullIndexer()) {
			return null;
		}

		switch (fKind) {
		case MANUAL:
			return null;
		case POST_CHANGE:
			if (fIndexer != null) {
				return fIndexer.createTask(force, changed, removed);
			}
			break;
		}

		for (ITranslationUnit tu : removed) {
			fForce.remove(tu);
			fTimestamp.remove(tu);
			fRemoved.add(tu);
		}
		for (ITranslationUnit tu : force) {
			fForce.add(tu);
			fTimestamp.remove(tu);
			fRemoved.remove(tu);
		}
		for (ITranslationUnit element : changed) {
			ITranslationUnit tu = element;
			if (!fForce.contains(tu)) {
				fTimestamp.add(tu);
			}
			fRemoved.remove(tu);
		}
		return null;
	}

	public IPDOMIndexerTask createTask() {
		IPDOMIndexerTask task = null;
		if (fIndexer != null && hasTUs()) {
			if (fKind != IndexUpdatePolicy.MANUAL && !isNullIndexer()) {
				task = fIndexer.createTask(toarray(fForce), toarray(fTimestamp), toarray(fRemoved));
			}
			clearTUs();
		}
		return task;
	}

	private ITranslationUnit[] toarray(HashSet<ITranslationUnit> set) {
		return set.toArray(new ITranslationUnit[set.size()]);
	}

	private boolean isNullIndexer() {
		return fIndexer != null && fIndexer.getID().equals(IPDOMManager.ID_NO_INDEXER);
	}

	public IPDOMIndexerTask changePolicy(int updatePolicy) {
		int oldPolicy = fKind;
		fKind = getLegalPolicy(updatePolicy);

		IPDOMIndexerTask task = null;
		if (fKind == MANUAL || isNullIndexer()) {
			clearTUs();
		} else if (fIndexer != null) {
			if (oldPolicy == MANUAL) {
				task = new PDOMUpdateTask(fIndexer,
						IIndexManager.UPDATE_CHECK_TIMESTAMPS | IIndexManager.UPDATE_CHECK_CONTENTS_HASH);
				clearTUs();
			} else if (fKind == POST_CHANGE) {
				task = createTask();
			}
		}
		return task;
	}

	public void requestInitialReindex() {
		fReindexRequested = true;
	}

	public void clearInitialFlags() {
		fReindexRequested = false;
	}

	public boolean isInitialRebuildRequested() {
		return fReindexRequested;
	}
}
