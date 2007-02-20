/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.extension.CDataObject;

public class MapProxyCache implements IProxyCache {
	private HashMap fMap;

	private HashMap getMap(boolean create){
		if(fMap == null && create)
			fMap = new HashMap();
		return fMap;
	}

	public CDataProxy[] getCachedProxies() {
		Map map = getMap(false);
		if(map != null){
			Collection c = map.values();
			return (CDataProxy[])c.toArray(new CDataProxy[c.size()]);
		}
		return new CDataProxy[0];
	}

	public CDataProxy getCachedProxy(String id) {
		Map map = getMap(false);
		if(map != null)
			return (CDataProxy)map.get(id);
		return null;
	}

	public void removeCachedProxy(String id) {
		Map map = getMap(false);
		if(map != null)
			map.remove(id);
	}

	public void clear() {
		fMap.clear();
	}

	public void addCachedProxy(CDataProxy proxy) {
		getMap(true).put(proxy.getId(), proxy);
	}

	public Map getCachedProxiesMap() {
		return (Map)getMap(true).clone();
	}

	public CDataProxy getCachedProxy(CDataObject data) {
		return getCachedProxy(data.getId());
	}

	public void removeCachedProxy(CDataProxy proxy) {
		removeCachedProxy(proxy.getId());
	}

	public void reuseProxies(List dataList, Map freeProxyMap) {
	}
}

