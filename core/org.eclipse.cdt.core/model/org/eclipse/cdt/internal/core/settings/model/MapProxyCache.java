/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
	private HashMap<String, CDataProxy> fMap;

	private HashMap<String, CDataProxy> getMap(boolean create){
		if(fMap == null && create)
			fMap = new HashMap<String, CDataProxy>();
		return fMap;
	}

	@Override
	public CDataProxy[] getCachedProxies() {
		Map<String, CDataProxy> map = getMap(false);
		if(map != null){
			Collection<CDataProxy> c = map.values();
			return c.toArray(new CDataProxy[c.size()]);
		}
		return new CDataProxy[0];
	}

	@Override
	public CDataProxy getCachedProxy(String id) {
		Map<String, CDataProxy> map = getMap(false);
		if(map != null)
			return map.get(id);
		return null;
	}

	@Override
	public void removeCachedProxy(String id) {
		Map<String, CDataProxy> map = getMap(false);
		if(map != null)
			map.remove(id);
	}

	@Override
	public void clear() {
		fMap.clear();
	}

	@Override
	public void addCachedProxy(CDataProxy proxy) {
		getMap(true).put(proxy.getId(), proxy);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, CDataProxy> getCachedProxiesMap() {
		return (Map<String, CDataProxy>)getMap(true).clone();
	}

	@Override
	public CDataProxy getCachedProxy(CDataObject data) {
		return getCachedProxy(data.getId());
	}

	@Override
	public void removeCachedProxy(CDataProxy proxy) {
		removeCachedProxy(proxy.getId());
	}

	public void reuseProxies(List<CDataObject> dataList, Map<String, CDataProxy> freeProxyMap) {
	}
}

