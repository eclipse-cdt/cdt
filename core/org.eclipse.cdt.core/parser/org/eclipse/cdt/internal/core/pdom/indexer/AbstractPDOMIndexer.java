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

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICProject;

public abstract class AbstractPDOMIndexer implements IPDOMIndexer {
	
	protected ICProject project;
	protected Properties fProperties= new Properties();
		
	public AbstractPDOMIndexer() {
		fProperties.put(IndexerPreferences.KEY_INDEX_ALL_FILES, String.valueOf(false));
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
		for (Iterator i= fProperties.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
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

	public void setProperties(Properties props) {
		// only set relevant properties as initialized in the constructor
		for (Iterator i= props.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();

			if (val != null && fProperties.get(key) != null) {
				fProperties.put(key, val);
			}
		}
	}
}
