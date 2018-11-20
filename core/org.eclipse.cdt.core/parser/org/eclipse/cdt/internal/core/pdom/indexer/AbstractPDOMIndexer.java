/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
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
	protected Properties fProperties = new Properties();

	public AbstractPDOMIndexer() {
		fProperties.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(true));
		fProperties.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INDEX_ON_OPEN, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INCLUDE_HEURISTICS, String.valueOf(true));
		fProperties.put(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB,
				String.valueOf(IndexerPreferences.DEFAULT_FILE_SIZE_LIMIT_MB));
		fProperties.put(IndexerPreferences.KEY_SKIP_INCLUDED_FILES_LARGER_THAN_MB,
				String.valueOf(IndexerPreferences.DEFAULT_INCLUDED_FILE_SIZE_LIMIT_MB));
		fProperties.put(IndexerPreferences.KEY_SKIP_ALL_REFERENCES, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INDEX_ALL_HEADER_VERSIONS, String.valueOf(false));
		fProperties.put(IndexerPreferences.KEY_INDEX_ALL_VERSIONS_SPECIFIC_HEADERS, ""); //$NON-NLS-1$
	}

	@Override
	public ICProject getProject() {
		return project;
	}

	@Override
	public void setProject(ICProject project) {
		this.project = project;
	}

	@Override
	public String getProperty(String key) {
		return fProperties.getProperty(key);
	}

	@Override
	public boolean needsToRebuildForProperties(Properties props) {
		for (Map.Entry<Object, Object> entry : fProperties.entrySet()) {
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();

			if (val != null) { // relevant property
				String v2 = (String) props.get(key);
				if (v2 != null && !val.equals(v2)) {
					return true;
				}
			}
		}
		return false;
	}

	public Properties getProperties() {
		return fProperties;
	}

	@Override
	public void setProperties(Properties props) {
		// Only set relevant properties as initialized in the constructor.
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();

			if (val != null && fProperties.get(key) != null) {
				fProperties.put(key, val);
			}
		}
	}
}
