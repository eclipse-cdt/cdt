/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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

	private PDOMManager fManager;
	private LinkedHashMap fLRUs= new LinkedHashMap(UPDATE_LR_CHANGED_FILES_COUNT, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > UPDATE_LR_CHANGED_FILES_COUNT;
		}
	};

	public CModelListener(PDOMManager manager) {
		fManager= manager;
	}
	
	public void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;
		
		// Walk the delta collecting tu's per project
		HashMap changeMap= new HashMap();
		processDelta(event.getDelta(), changeMap);
		
		// bug 171834 update last recently changed sources
		addLastRecentlyUsed(changeMap);
				
		for (Iterator it = changeMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			ICProject cproject = (ICProject) entry.getKey();
			DeltaAnalyzer analyzer= (DeltaAnalyzer) entry.getValue();
			fManager.changeProject(cproject, analyzer.getAddedTUs(), analyzer.getChangedTUs(), analyzer.getRemovedTUs());
		}
	}
	
	private void processDelta(ICElementDelta delta, HashMap changeMap) {
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

	private void processProjectDelta(ICProject project, ICElementDelta delta, HashMap changeMap) {
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

	private void addLastRecentlyUsed(HashMap changeMap) {
		boolean addLRUs= false;
		int count= 0;
		ITranslationUnit[] newLRUs= new ITranslationUnit[UPDATE_LR_CHANGED_FILES_COUNT];
		
		for (Iterator iterator = changeMap.values().iterator(); iterator.hasNext();) {
			DeltaAnalyzer analyzer = (DeltaAnalyzer) iterator.next();
			List l= analyzer.getAddedList();
			for (Iterator it = l.iterator(); it.hasNext();) {
				ITranslationUnit tu= (ITranslationUnit) it.next();
				newLRUs[count++ % UPDATE_LR_CHANGED_FILES_COUNT]= tu;
				if (!addLRUs && tu.isHeaderUnit()) {
					addLRUs= true;
				}
			}
			l= analyzer.getChangedList();
			for (Iterator it = l.iterator(); it.hasNext();) {
				ITranslationUnit tu= (ITranslationUnit) it.next();
				newLRUs[count++ % UPDATE_LR_CHANGED_FILES_COUNT]= tu;
				if (!addLRUs && tu.isHeaderUnit()) {
					addLRUs= true;
				}
			}
		}
		
		if (count > 0) {
			if (addLRUs) {
				for (Iterator it = fLRUs.keySet().iterator(); it.hasNext();) {
					final ITranslationUnit tu = (ITranslationUnit) it.next();
					if (tu.getResource().exists()) {
						final ICProject cproject= tu.getCProject();
						DeltaAnalyzer analyzer= (DeltaAnalyzer) changeMap.get(cproject);
						if (analyzer == null) {
							analyzer= new DeltaAnalyzer();
							changeMap.put(cproject, analyzer);
						}
						analyzer.getChangedList().add(tu);
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

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_BUILD) {
			fManager.handlePostBuildEvent();
		}
	}
}
