/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;


public class CCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
        Preferences preferences = CCorePlugin.getDefault().getPluginPreferences();
        HashSet optionNames = CModelManager.OptionNames;
    
        // Compiler settings
        preferences.setDefault(CCorePreferenceConstants.TRANSLATION_TASK_TAGS, CCorePreferenceConstants.DEFAULT_TASK_TAG); 
        optionNames.add(CCorePreferenceConstants.TRANSLATION_TASK_TAGS);

        preferences.setDefault(CCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES, CCorePreferenceConstants.DEFAULT_TASK_PRIORITY); 
        optionNames.add(CCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES);
	}

}
