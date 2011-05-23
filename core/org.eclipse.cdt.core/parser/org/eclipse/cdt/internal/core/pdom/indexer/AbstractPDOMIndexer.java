/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICProject;

/**
 * Abstract base class for all indexers.
 */
public abstract class AbstractPDOMIndexer implements IPDOMIndexer {
	protected ICProject project;
	protected Properties fProperties= new Properties();
		
	public AbstractPDOMIndexer() {
		fProperties.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(true));
		fProperties.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INDEX_ON_OPEN, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INCLUDE_HEURISTICS, String.valueOf(true));
		fProperties.put(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB, String.valueOf(IndexerPreferences.DEFAULT_FILE_SIZE_LIMIT));
		fProperties.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, ""); //$NON-NLS-1$
		fProperties.put(IndexerPreferences.KEY_SKIP_ALL_REFERENCES, String.valueOf(false)); 
		fProperties.put(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES, String.valueOf(false)); 
		fProperties.put(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES, String.valueOf(false)); 
		fProperties.put(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES, String.valueOf(false)); 
	}

	public ICProject getProject() {
		return project;
	}
		
	public void setProject(ICProject project) {
		this.project = project;
	}
		
	public String getProperty(String key) {
		return fProperties.getProperty(key);
	}

	public boolean needsToRebuildForProperties(Properties props) {
		for (Map.Entry<Object,Object> entry : fProperties.entrySet()) {
			String key = (String) entry.getKey();
			String myval = (String) entry.getValue();

			if (myval != null) { // relevant property
				String v2= (String) props.get(key);
				if (v2 != null && !myval.equals(v2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Properties getProperties() {
		return fProperties;
	}

	public void setProperties(Properties props) {
		// only set relevant properties as initialized in the constructor
		for (Map.Entry<Object,Object> entry : props.entrySet()) {
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();

			if (val != null && fProperties.get(key) != null) {
				fProperties.put(key, val);
			}
		}
	}

	public String[] getFilesToParseUpFront() {
		String prefSetting= getProperty(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT);
		if (prefSetting != null) {
			prefSetting= prefSetting.trim();
			if (prefSetting.length() > 0) {
				return prefSetting.split(","); //$NON-NLS-1$
			}
		}
		return new String[0];
	}
}
