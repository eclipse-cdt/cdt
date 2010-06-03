/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.index.provider.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyProviderTraces {
	static DummyProviderTraces instance = new DummyProviderTraces();
	
	public static DummyProviderTraces getInstance() { return instance; }

	/** Flag indicates if test is enabled, so we should trace projects and configs */
	public volatile boolean enabled;

	private DummyProviderTraces() {}
	
	Map/*<String, List>*/ id2prjTrace= new HashMap();
	Map/*<String, List>*/ id2cfgTrace= new HashMap();
	
	public List getProjectsTrace(Class provider) {
		String key= provider.getName();
		if(!id2prjTrace.containsKey(key)) {
			id2prjTrace.put(key, Collections.synchronizedList(new ArrayList()));
		}
		return (List) id2prjTrace.get(key);
	}
	
	public List getCfgsTrace(Class provider) {
		String key= provider.getName();
		if(!id2cfgTrace.containsKey(key)) {
			id2cfgTrace.put(key, Collections.synchronizedList(new ArrayList()));
		}
		return (List) id2cfgTrace.get(key);
	}
	
	public void reset(Class provider) {
		getProjectsTrace(provider).clear();
		getCfgsTrace(provider).clear();
	}
}
