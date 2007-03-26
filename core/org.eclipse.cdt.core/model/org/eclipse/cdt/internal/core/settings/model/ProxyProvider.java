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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.extension.CDataObject;


public class ProxyProvider implements IProxyProvider {
	private IProxyCache fCache;
	private ICDataScope fScope;
	private IProxyFactory fFactory;
	private boolean fProxiesCached;
	
	public ProxyProvider(ICDataScope scope, IProxyCache cache, IProxyFactory factory){
		fScope = scope;
		fCache = cache;
		fFactory = factory;
	}

	public CDataProxy[] getProxies() {
		if(!fProxiesCached || !fScope.isStatic()){
			fillCache();
			fProxiesCached = true;
		}
		return fCache.getCachedProxies();
	}
/*	
	private void clearInvalidCachedProxies(){
		CDataProxy proxies[] = fCache.getCachedProxies();
		for(int i = 0; i < proxies.length; i++){
			if(!proxies[i].isValid()){
				fCache.removeCachedProxy(proxies[i]);
			}
		}
	}
*/
	public CDataProxy getProxy(String id) {
		if(!fProxiesCached || !fScope.isStatic()){
			fillCache();
			fProxiesCached = true;
		}
		return fCache.getCachedProxy(id);
	}
	
	protected void fillCache(){
		Map map = fCache.getCachedProxiesMap();
		
		CDataObject datas[] =fScope.getChildren();
		for(int i = 0; i < datas.length; i++){
			CDataObject data = datas[i];
			CDataProxy proxy = fCache.getCachedProxy(data.getId());
			if(proxy == null || proxy.getType() != data.getType()){
				proxy = fFactory.createProxy(data);
				if(proxy != null){
					fCache.addCachedProxy(proxy);
				}
			} else {
				proxy.setData(data);
				map.remove(data.getId());
			}
		}
		
		if(!map.isEmpty()){
			for(Iterator iter = map.values().iterator(); iter.hasNext();){
				fCache.removeCachedProxy((CDataProxy)iter.next());
			}
		}
	}
	
	public CDataProxy getProxy(CDataObject data) {
		if(!fProxiesCached || !fScope.isStatic()){
			fillCache();
			fProxiesCached = true;
		}
		return fCache.getCachedProxy(data);
	}

	public void removeCachedProxy(String id) {
		fCache.removeCachedProxy(id);
		fProxiesCached = true;
	}

	public void removeCachedProxy(CDataProxy proxy) {
		fCache.removeCachedProxy(proxy);
		fProxiesCached = true;
	}

	public CDataProxy[] getCachedProxies() {
		return fCache.getCachedProxies();
	}

	public CDataProxy[] getProxiesOfKind(int kind) {
		CDataProxy[] proxies = getProxies();
		if(proxies.length > 0){
			CDataProxy[] tmp = new CDataProxy[proxies.length];
			int num = 0;
			for(int i = 0; i < proxies.length; i++){
				CDataProxy proxy = proxies[i];
				if((proxy.getType() & kind) == proxy.getType())
					tmp[num++] = proxy;
			}
			
			if(num != proxies.length){
				proxies = new CDataProxy[num];
				System.arraycopy(tmp, 0, proxies, 0, num);
			}
		}
		return proxies;
	}

	public void cacheValues() {
		if(!fProxiesCached || !fScope.isStatic()){
			fillCache();
			fProxiesCached = true;
		}
	}

	public void invalidateCache() {
		fProxiesCached = false;
		CDataProxy[] proxies = fCache.getCachedProxies();
		for(int i = 0; i < proxies.length; i++){
			proxies[i].doClearData();
		}
	}
	
	
}
