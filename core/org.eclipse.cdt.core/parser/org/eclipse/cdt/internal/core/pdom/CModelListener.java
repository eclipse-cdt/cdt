/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.DeltaAnalyzer;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;

/**
 * CModel listener used for the PDOMManager.
 * @since 4.0
 */
public class CModelListener implements IElementChangedListener, IResourceChangeListener {
	private static final int UPDATE_LR_CHANGED_FILES_COUNT = 5;

	// For testing purposes, only.
	public static boolean sSuppressUpdateOfLastRecentlyUsed = false;

	private PDOMManager fManager;
	private LinkedHashMap<ITranslationUnit, ITranslationUnit> fLRUs= new LinkedHashMap<ITranslationUnit, ITranslationUnit>(UPDATE_LR_CHANGED_FILES_COUNT, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<ITranslationUnit, ITranslationUnit> eldest) {
			return size() > UPDATE_LR_CHANGED_FILES_COUNT;
		}
	};

	public CModelListener(PDOMManager manager) {
		fManager= manager;
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;

		// Walk the delta collecting tu's per project
		HashMap<ICProject, DeltaAnalyzer> changeMap= new HashMap<ICProject, DeltaAnalyzer>();
		processDelta(event.getDelta(), changeMap);

		// bug 171834 update last recently changed sources
		if (!sSuppressUpdateOfLastRecentlyUsed) {
			addLastRecentlyUsed(changeMap);
		}

		for (Map.Entry<ICProject, DeltaAnalyzer> entry : changeMap.entrySet()) {
			ICProject cproject = entry.getKey();
			DeltaAnalyzer analyzer= entry.getValue();
			fManager.changeProject(cproject, analyzer.getForcedTUs(), analyzer.getChangedTUs(), analyzer.getRemovedTUs());
		}
	}

	private void processDelta(ICElementDelta delta, HashMap<ICProject, DeltaAnalyzer> changeMap) {
		int type = delta.getElement().getElementType();
		switch (type) {
		case ICElement.C_MODEL:
			// Loop through the children
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i], changeMap);
			}
			break;
		case ICElement.C_PROJECT:
			// Find the appropriate indexer and pass the delta on
			final ICProject project = (ICProject)delta.getElement();
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				fManager.addProject(project);
		    	break;

			case ICElementDelta.CHANGED:
				processProjectDelta(project, delta, changeMap);
				break;

			case ICElementDelta.REMOVED:
				fManager.removeProject(project, delta);
		    	break;
			}
		}
	}

	private void processProjectDelta(ICProject project, ICElementDelta delta, HashMap<ICProject, DeltaAnalyzer> changeMap) {
		IPDOMIndexer indexer = fManager.getIndexer(project);
		if (indexer != null && indexer.getID().equals(IPDOMManager.ID_NO_INDEXER)) {
			return;
		}

		DeltaAnalyzer deltaAnalyzer = new DeltaAnalyzer();
		try {
			deltaAnalyzer.analyzeDelta(delta);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		changeMap.put(project, deltaAnalyzer);
	}

	private void addLastRecentlyUsed(HashMap<ICProject, DeltaAnalyzer> changeMap) {
		boolean addLRUs= false;
		int count= 0;
		ITranslationUnit[] newLRUs= new ITranslationUnit[UPDATE_LR_CHANGED_FILES_COUNT];

		for (DeltaAnalyzer analyzer : changeMap.values()) {
			for (ITranslationUnit tu : analyzer.getChangedList()) {
				newLRUs[count++ % UPDATE_LR_CHANGED_FILES_COUNT]= tu;
				if (!addLRUs && tu.isHeaderUnit()) {
					addLRUs= true;
				}
			}
		}

		if (count > 0) {
			if (addLRUs) {
				for (final ITranslationUnit tu : fLRUs.keySet()) {
					if (tu.getResource().exists()) {
						final ICProject cproject= tu.getCProject();
						DeltaAnalyzer analyzer= changeMap.get(cproject);
						if (analyzer == null) {
							analyzer= new DeltaAnalyzer();
							changeMap.put(cproject, analyzer);
						}
						analyzer.getForcedList().add(tu);
					}
				}
			}
			count= Math.min(count, newLRUs.length);
			for (int i = 0; i < count; i++) {
				final ITranslationUnit tu = newLRUs[i];
				fLRUs.put(tu, tu);
			}
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_BUILD) {
			fManager.handlePostBuildEvent();
		}
	}
}
