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

import org.eclipse.cdt.core.settings.model.extension.CDataObject;


public interface IProxyProvider {
	CDataProxy getProxy(String id);

	CDataProxy getProxy(CDataObject data);

	void removeCachedProxy(String id);

	void removeCachedProxy(CDataProxy proxy);

	CDataProxy[] getProxies();

	CDataProxy[] getProxiesOfKind(int kind);

	CDataProxy[] getCachedProxies();
	
	void cacheValues();
	
	void invalidateCache();
/*
	CDataProxy[] getProxies(Class arrayElClass);

	CDataProxy[] getProxiesOfKind(int kind, Class arrayElClass);

	CDataProxy[] getCachedProxies(Class arrayElClass);
*/	
//	IDataProvider getDataProvider();
	
//	IProxyCache getProxyCache();
}
