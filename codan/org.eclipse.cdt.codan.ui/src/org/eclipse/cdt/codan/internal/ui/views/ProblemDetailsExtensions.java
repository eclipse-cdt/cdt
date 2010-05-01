/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Class that can load extension for problemDetails
 */
public class ProblemDetailsExtensions {
	public static final String ALL = "*";//$NON-NLS-1$
	private static final String EXTENSION_POINT_NAME = "codanProblemDetails"; //$NON-NLS-1$
	private static boolean extensionsLoaded;
	private static HashMap<String, Collection<?>> map = new HashMap<String, Collection<?>>();

	private static synchronized void readExtensions() {
		if (extensionsLoaded) return;
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(CodanUIActivator.PLUGIN_ID, EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		try {
			IConfigurationElement[] elements = ep.getConfigurationElements();
			// process categories
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement configurationElement = elements[i];
				processDetails(configurationElement);
			}
		} finally {
			extensionsLoaded = true;
		}
	}

	/**
	 * @param configurationElement
	 */
	private static void processDetails(IConfigurationElement configurationElement) {
		if (configurationElement.getName().equals("problemDetails")) { //$NON-NLS-1$
			String id = configurationElement.getAttribute("problemId"); //$NON-NLS-1$
			if (id == null) {
				id = ALL;
			}
			addExtension(id, configurationElement);
		}
	}

	public static AbstractCodanProblemDetailsProvider resolveClass(IConfigurationElement configurationElement) {
		AbstractCodanProblemDetailsProvider res;
		try {
			res = (AbstractCodanProblemDetailsProvider) configurationElement.createExecutableExtension("class");//$NON-NLS-1$
		} catch (CoreException e) {
			CodanUIActivator.log(e);
			return null;
		}
		return res;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void addExtension(String id, IConfigurationElement configurationElement) {
		Collection collection = getCollection(id);
		collection.add(configurationElement);
	}

	/**
	 * Remove provider from the list
	 * @param id - codan problem id or ALL
	 * @param el - details provider (class extending AbstractCodanProblemDetailsProvider) or ElementConfiguration (user internally) 
	 */
	@SuppressWarnings("rawtypes")
	public static void removeExtension(String id, Object el) {
		Collection collection = getCollection(id);
		collection.remove(el);
	}

	@SuppressWarnings("rawtypes")
	private static Collection getCollection(String id) {
		Collection collection = map.get(id);
		if (collection == null) {
			collection = new ArrayList();
			map.put(id, collection);
		}
		return collection;
	}

	public static Collection<AbstractCodanProblemDetailsProvider> getProviders(String id) {
		readExtensions();
		Collection<AbstractCodanProblemDetailsProvider> providers = new ArrayList<AbstractCodanProblemDetailsProvider>();
		Collection<?> collection1 = getCollection(id);
		Collection<?> collection2 = getCollection(ALL);
		providers.addAll(resolveProviders(collection1));
		providers.addAll(resolveProviders(collection2));
		return providers;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection<AbstractCodanProblemDetailsProvider> resolveProviders(Collection collection) {
		Collection res = new ArrayList(collection);
		for (Iterator iterator = res.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof IConfigurationElement) {
				// resolve
				collection.remove(object);
				AbstractCodanProblemDetailsProvider provider = resolveClass((IConfigurationElement) object);
				if (provider!=null)
					collection.add(provider);
			}
		}
		return collection;
	}

	/**
	 * Add extension (details provider) using API
	 * @param id - codan problem id or ALL
	 * @param provider - class extending AbstractCodanProblemDetailsProvider
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addExtension(String id, AbstractCodanProblemDetailsProvider provider) {
		Collection collection = getCollection(id);
		collection.add(provider);
	}
}
