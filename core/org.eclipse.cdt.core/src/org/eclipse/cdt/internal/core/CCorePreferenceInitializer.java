/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.formatter.CodeFormatterConstants;
import org.eclipse.cdt.internal.core.model.CModelManager;
//import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;


public class CCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
        HashSet optionNames = CModelManager.OptionNames;
    
		// Formatter settings
		Map defaultOptionsMap = CodeFormatterConstants.getEclipseDefaultSettings(); // code formatter defaults

		// Compiler settings
		defaultOptionsMap.put(CCorePreferenceConstants.TRANSLATION_TASK_TAGS, CCorePreferenceConstants.DEFAULT_TASK_TAG); 
		defaultOptionsMap.put(CCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES, CCorePreferenceConstants.DEFAULT_TASK_PRIORITY); 
		defaultOptionsMap.put(CCorePreferenceConstants.CODE_FORMATTER, CCorePreferenceConstants.DEFAULT_CODE_FORMATTER); 
                
		// Store default values to default preferences
	 	IEclipsePreferences defaultPreferences = ((IScopeContext) new DefaultScope()).getNode(CCorePlugin.PLUGIN_ID);
		for (Iterator iter = defaultOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			defaultPreferences.put(optionName, (String)entry.getValue());
			optionNames.add(optionName);
		}
	}

}
