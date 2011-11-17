/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;

public abstract class CDataProxyContainer extends CDataProxy implements ICDataProxyContainer{
	private IProxyProvider fChildProxyProvider;

	CDataProxyContainer(CDataObject data, ICDataProxyContainer parent, CConfigurationDescription cfg) {
		super(data, parent, cfg);
	}

/*	protected class ChildrenDataScope implements ICDataScope{
		public boolean isStatic() {
			return isWritable();
		}

		public CDataObject[] getChildren() {
			ICDataParent data = (ICDataParent)CDataProxyContainer.this.getData(false);
			return data.getChildren();
		}
	}
*/

	public ICSettingObject getChildById(String id){
		IProxyProvider provider = getChildrenProxyProvider();

		if(provider == null)
			throw new IllegalStateException();

		return provider.getProxy(id);
	}

	public ICSettingObject[] getChildrenOfKind(int kind){
		IProxyProvider provider = getChildrenProxyProvider();

		if(provider == null)
			throw new IllegalStateException();

		return provider.getProxiesOfKind(kind);
	}


	protected IProxyProvider getChildrenProxyProvider(){
		if(fChildProxyProvider == null)
			fChildProxyProvider = createChildProxyProvider();
		return fChildProxyProvider;
	}

	protected abstract IProxyProvider createChildProxyProvider();

	@Override
	public ICSettingObject[] getChildSettings() {
		IProxyProvider provider = getChildrenProxyProvider();

		if(provider == null)
			throw new IllegalStateException();

		return provider.getProxies();
	}

	@Override
	public void updateChild(CDataProxy child, boolean write){
		getData(write);
		getChildrenProxyProvider().cacheValues();
	}

	@Override
	protected void setRescan(boolean rescan){
		if(isRescan() == rescan)
			return;

		super.setRescan(rescan);

		if(rescan){
			setRescanChildren();
		}
	}

	@Override
	void setData(CDataObject data) {
		super.setData(data);
		setRescanChildren();
	}

	protected void setRescanChildren(){
		IProxyProvider provider = getChildrenProxyProvider();
		if(provider == null)
			throw new IllegalStateException();

		provider.invalidateCache();

		CDataProxy proxies[] = provider.getCachedProxies();
		for(int i = 0; i < proxies.length; i++){
			proxies[i].setRescan(true);
		}

	}

	public ICSettingObject getChildSettingById(String id) {
		return getChildrenProxyProvider().getProxy(id);
	}


}
