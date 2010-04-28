/*******************************************************************************
 * $QNXLicenseC:
 * Copyright 2008, QNX Software Systems. All Rights Reserved.
 * 
 * You must obtain a written license from and pay applicable license fees to QNX 
 * Software Systems before you may reproduce, modify or distribute this software, 
 * or any work that includes all or part of this software.   Free development 
 * licenses are available for evaluation and non-commercial purposes.  For more 
 * information visit http://licensing.qnx.com or email licensing@qnx.com.
 *  
 * This file may contain contributions from others.  Please review this entire 
 * file for other proprietary rights or license notices, as well as the QNX 
 * Development Suite License Guide at http://licensing.qnx.com/license-guide/ 
 * for other information.
 * $
 *******************************************************************************/
/*
 * Created by: Elena Laskavaia
 * Created on: 2010-04-28
 * Last modified by: $Author$
 */
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
	private static final String ALL = "*";//$NON-NLS-1$
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addExtension(String id, AbstractCodanProblemDetailsProvider provider) {
		Collection collection = getCollection(id);
		collection.add(provider);
	}
}
