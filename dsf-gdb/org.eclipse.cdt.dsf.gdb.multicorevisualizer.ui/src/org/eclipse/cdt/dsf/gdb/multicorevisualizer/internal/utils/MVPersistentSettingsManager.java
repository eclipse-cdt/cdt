/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class MVPersistentSettingsManager {
	
	// G: Global setting, applicable to all MV views
	// 
	protected final static boolean G_ENABLE_DBG_ACTIONS_IN_MV_TOOLBAR = true;
	protected final static String A = "";
	protected final static String B = "";
	protected final static String C = "";
	
	
	
	protected Map<String, String> m_parameters = null;
	protected String m_sectionName = null;
	
	public MVPersistentSettingsManager(String section) {
		m_parameters = new HashMap<String, String>();
		m_sectionName = section;
	}
	
	public void dispose() {
		if (m_parameters != null) {
			m_parameters.clear();
			m_parameters = null;
		}
	}
	
	
	public void init() {
		m_parameters.put("", "");
	}
	
	public void addPersistentParameter(String param) {
		
	}
	
	
	protected void restorePersistentState() {
		m_parameters.clear();
		
		IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
		String memento = store.get(getStoreKey(), null);
		if (memento == null) return;
		
		// last saved attribute labels list
		List<String> labels =  MementoUtils.decodeListFromMemento(memento);
		
	}

	/** Saves the list of attributes to display, in a persistent way */
	protected void savePersistentState(List<String> labels) {
		IEclipsePreferences store = MulticoreVisualizerUIPlugin.getEclipsePreferenceStore();
		// create memento string from list of labels
		String memento = MementoUtils.encodeListIntoMemento(labels);
		if (memento != null) {
			store.put(getStoreKey(), memento);
			try {
				store.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 
	 * Returns the Eclipse store key used to save/retrieve persistent 
	 * information for this view instance. Each Visualizer view had it's own 
	 * independent entry in the "EMCA Visualizer" plugin store, which 
	 * permits saving per-view info in the store
	 */
	public String getStoreKey() {
		return "MulticoreVisualizerPreferenceStore-" + m_sectionName;
	}
	
}
