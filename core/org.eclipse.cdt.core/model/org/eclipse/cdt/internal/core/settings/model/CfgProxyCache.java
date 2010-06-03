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
import java.util.Map;

import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.cdt.core.settings.model.util.IPathSettingsContainerListener;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.runtime.IPath;

public class CfgProxyCache implements IProxyCache {
	private HashMap<String, CDataProxy> fProxyMap = new HashMap<String, CDataProxy>();
	private PathSettingsContainer fPathContainer;
	
	CfgProxyCache(PathSettingsContainer pathDesContainer){
		fPathContainer = pathDesContainer;
		fPathContainer.addContainerListener(new IPathSettingsContainerListener(){
			public void aboutToRemove(PathSettingsContainer container) {
				CDataProxy proxy = (CDataProxy)container.getValue();
				if(proxy != null)
					fProxyMap.remove(proxy.getId());
			}

			public void containerAdded(PathSettingsContainer container) {
			}

			public void containerValueChanged(PathSettingsContainer container, Object oldValue) {
				CDataProxy proxy = (CDataProxy)container.getValue();
				if(proxy != null)
					fProxyMap.put(proxy.getId(),proxy);
				else if(oldValue != null){
					fProxyMap.remove(oldValue);
				}
			}

			public void containerPathChanged(PathSettingsContainer container, IPath oldPath, boolean childrenMoved) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void addCachedProxy(CDataProxy proxy) {
		if(proxy instanceof IInternalResourceDescription){
			IInternalResourceDescription des = (IInternalResourceDescription)proxy;
			IPath path = des.getPath();
			PathSettingsContainer cr = fPathContainer.getChildContainer(path, true, true);
			cr.setValue(des);
			des.setPathContainer(cr);
		} else {
			fProxyMap.put(proxy.getId(), proxy);
		}
	}

	public void clear() {
		fPathContainer.removeChildren();
		fProxyMap.clear();
	}

	public CDataProxy[] getCachedProxies() {
		Collection<CDataProxy> c = fProxyMap.values();
		return c.toArray(new CDataProxy[c.size()]);
	}

	public CDataProxy getCachedProxy(String id) {
		return fProxyMap.get(id);
	}

	public void removeCachedProxy(String id) {
		CDataProxy proxy = fProxyMap.get(id);
		removeCachedProxy(proxy);
	}

	@SuppressWarnings("unchecked")
	public Map<String, CDataProxy> getCachedProxiesMap() {
		return (Map<String, CDataProxy>)fProxyMap.clone();
	}

	public CDataProxy getCachedProxy(CDataObject data) {
		return getCachedProxy(data.getId());
	}

	public void removeCachedProxy(CDataProxy proxy) {
		if(proxy instanceof IInternalResourceDescription){
			fPathContainer.removeChildContainer(((IInternalResourceDescription)proxy).getCachedPath());
		} else {
			fProxyMap.remove(proxy.getId());
		}
	}

//	public Map reuseProxies(List dataList, Map freeProxyMap) {
//		Collection proxies = freeProxyMap.values();
//		CDataProxy proxy;
//		CDataObject data;
//		Map result;
//		for(Iterator dataIter = dataList.iterator(); dataIter.hasNext();){
//			data = (CDataObject)dataIter.next();
//			for(Iterator proxyIter = proxies.iterator(); proxyIter.hasNext();){
//				proxy = (CDataProxy)proxyIter.next();
//				if(data.getType() != proxy.getType())
//					continue;
//				
//				switch(data.getType()){
//				case ICSettingBase.SETTING_TARGET_PLATFORM:
//				case ICSettingBase.SETTING_FILE:
//				case ICSettingBase.SETTING_FOLDER:
////					if(((CResourceData)data).getPath().equals(((CResourceDescription)proxy).getPath()))
//				}
//				
//				if(result == null)
//					result = new HashMap();
//				
//				
//			}
//		}
//	}
}

