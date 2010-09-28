/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashSet;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.parser.CodeReaderCache;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;


public class CCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
        HashSet<String> optionNames = CModelManager.OptionNames;
    
		// Formatter settings
		Map<String, String> defaultOptionsMap = DefaultCodeFormatterConstants.getDefaultSettings(); // code formatter defaults

		// Compiler settings
		defaultOptionsMap.put(CCorePreferenceConstants.TODO_TASK_TAGS, CCorePreferenceConstants.DEFAULT_TASK_TAGS); 
		defaultOptionsMap.put(CCorePreferenceConstants.TODO_TASK_PRIORITIES, CCorePreferenceConstants.DEFAULT_TASK_PRIORITY); 
		defaultOptionsMap.put(CCorePreferenceConstants.TODO_TASK_CASE_SENSITIVE, CCorePreferenceConstants.DEFAULT_TASK_CASE_SENSITIVE); 
		defaultOptionsMap.put(CCorePreferenceConstants.CODE_FORMATTER, CCorePreferenceConstants.DEFAULT_CODE_FORMATTER); 
		defaultOptionsMap.put(CCorePreferenceConstants.INDEX_DB_CACHE_SIZE_PCT, CCorePreferenceConstants.DEFAULT_INDEX_DB_CACHE_SIZE_PCT); 
		defaultOptionsMap.put(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB, CCorePreferenceConstants.DEFAULT_MAX_INDEX_DB_CACHE_SIZE_MB); 
		defaultOptionsMap.put(CCorePreferenceConstants.WORKSPACE_LANGUAGE_MAPPINGS, CCorePreferenceConstants.DEFAULT_WORKSPACE_LANGUAGE_MAPPINGS);
		defaultOptionsMap.put(CodeReaderCache.CODE_READER_BUFFER, CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB_STRING);

		// Store default values to default preferences
	 	IEclipsePreferences defaultPreferences = ((IScopeContext) new DefaultScope()).getNode(CCorePlugin.PLUGIN_ID);
		for (Map.Entry<String,String> entry : defaultOptionsMap.entrySet()) {
			String optionName = entry.getKey();
			defaultPreferences.put(optionName, entry.getValue());
			optionNames.add(optionName);
		}

		defaultPreferences.putBoolean(CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES, true);
		defaultPreferences.putBoolean(CCorePlugin.PREF_USE_STRUCTURAL_PARSE_MODE, false);
		defaultPreferences.putBoolean(CCorePreferenceConstants.FILE_PATH_CANONICALIZATION, true);
		defaultPreferences.putBoolean(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT, true);

		// build defaults
		defaultPreferences.putBoolean(CCorePreferenceConstants.PREF_BUILD_ALL_CONFIGS, false);
		defaultPreferences.putBoolean(CCorePreferenceConstants.PREF_BUILD_CONFIGS_RESOURCE_CHANGES, false);
		
		// indexer defaults
		IndexerPreferences.initializeDefaultPreferences(defaultPreferences);
	}
}
