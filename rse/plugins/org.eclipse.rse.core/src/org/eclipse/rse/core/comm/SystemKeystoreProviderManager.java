/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [186525] Move keystoreProviders to core
 * Martin Oberhuber (Wind River) - [181939] Deferred class loading for keystoreProviders
 ********************************************************************************/

package org.eclipse.rse.core.comm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.osgi.framework.Bundle;

/**
 * A Registry of keystore providers, with the ability to instantiate
 * providers lazily when needed.
 */
public class SystemKeystoreProviderManager
{
	private static SystemKeystoreProviderManager _instance = new SystemKeystoreProviderManager();
	
	private List _extensions;
	
	private static class ExtensionInfo {
		public Bundle bundle;
		public String className;
		public ExtensionInfo(Bundle bundle, String className) {
			this.bundle = bundle;
			this.className = className;
		}
	}
	
	private SystemKeystoreProviderManager()
	{		
		_extensions= new ArrayList();
	}
	
	/**
	 * Return the SystemKeystoreProviderManager Instance.
	 * @return the singleton instance.
	 */
	public static SystemKeystoreProviderManager getInstance()
	{
		return _instance;
	}
	
	/**
	 * Register a keystore provider.
	 * @param ext keystore provider to register.
	 */
	public void registerKeystoreProvider(ISystemKeystoreProvider ext)
	{
		_extensions.add(ext);
	}

	/**
	 * Register a keystore provider for deferred (lazy) loading.
	 * 
	 * @param bundle the bundle that declares the extension. The bundle
	 *    must be installed and will be activated lazily when trying
	 *    to load the given class name.
	 * @param className fully qualified classname of the keystore provider
	 *     declared in the given bundle.
	 */
	public void registerKeystoreProvider(Bundle bundle, String className)
	{
		_extensions.add(new ExtensionInfo(bundle, className));
	}
	
	public boolean hasProvider()
	{
		return !_extensions.isEmpty();
	}

	/**
	 * Return the keystore provider at the given index in the registry,
	 * or <code>null</code> if there is no provider at the given index
	 * or it cannot be loaded.
	 * @return An ISystemKeystoreProvider instance, or <code>null</code>
	 *    if no provider is found at the given index.
	 */
	public ISystemKeystoreProvider getProviderAt(int idx)
	{
		if (idx >= 0 && idx < _extensions.size()) {
			Object o = _extensions.get(idx);
			if (o instanceof ISystemKeystoreProvider) {
				return (ISystemKeystoreProvider)o;
			} else if (o instanceof ExtensionInfo) {
				ExtensionInfo info = (ExtensionInfo)o;
				try {
				    Class keystoreProvider = info.bundle.loadClass(info.className);
				    ISystemKeystoreProvider extension = (ISystemKeystoreProvider)keystoreProvider.getConstructors()[0].newInstance(null);
				    _extensions.set(idx, extension);
				    return extension;
				} catch(Exception e) {
					RSECorePlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, info.bundle.getSymbolicName(), -1, e.getMessage(), e));
				}
			}
		}
		return null;
	}

	/**
	 * Return the default keystore provider.
	 * The default provider is the one which was added last by the
	 * extension registry, and loads properly.
	 * @return An ISystemKeystoreProvider instance, or <code>null</code>
	 *    if no provider is found at the given index.
	 */
	public ISystemKeystoreProvider getDefaultProvider()
	{
		int idx = _extensions.size()-1;
		while (idx>=0) {
			ISystemKeystoreProvider provider = getProviderAt(idx);
			if (provider!=null) {
				return provider;
			}
			_extensions.remove(idx);
			idx--;
		}
		return null;
	}
	
	/**
	 * Return an array of all registered keystore providers.
	 * The default provider is the one which was added last by the
	 * extension registry, and loads properly.
	 * @return An array of all registered keystore providers 
	 *    that load properly.
	 */
	public ISystemKeystoreProvider[] getProviders()
	{
		List providers = new ArrayList();
		for (int i = _extensions.size()-1; i>=0; i--) {
			ISystemKeystoreProvider provider = getProviderAt(i);
			if (provider!=null) {
				providers.add(0, provider);
			} else {
				_extensions.remove(i);
			}
		}
		ISystemKeystoreProvider[] result = (ISystemKeystoreProvider[])providers.toArray(new ISystemKeystoreProvider[providers.size()]);
		return result;
	}
}