/*******************************************************************************
 * Copyright (c) 2013 Andreas Muelder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Muelder (itemis) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.model.IProblemFilter;
import org.eclipse.cdt.codan.internal.core.model.NoProblemsFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

public class FilterRegistry implements IPreferenceChangeListener {
	public static class ProblemFilterDescriptor {
		private String name;
		private String id;
		private IProblemFilter filter;

		public ProblemFilterDescriptor(String name, String id, IProblemFilter filter) {
			this.name = name;
			this.filter = filter;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public IProblemFilter getFilter() {
			return filter;
		}

		public String getId() {
			return id;
		}
	}
	private static final String NAME_ATTR = "name"; //$NON-NLS-1$
	private static final String CLASS_ATTR = "class"; //$NON-NLS-1$
	private static final String ID_ATTR = "id"; //$NON-NLS-1$
	private static final String FILTERS_EXTENSION_POINT_NAME = "filters"; //$NON-NLS-1$
	public static final String FILTER_PREFERENCE = "org.eclipse.cdt.codan.internal.core.filter"; //$NON-NLS-1$
	private static final String FILTER_NONE = "None"; //$NON-NLS-1$
	private Collection<ProblemFilterDescriptor> filters = new ArrayList<ProblemFilterDescriptor>();
	private static FilterRegistry instance;
	private IProblemFilter activeFilter;

	private FilterRegistry() {
		instance = this;
		CodanCorePlugin.getDefault().getStorePreferences().addPreferenceChangeListener(this);
		initFilters();
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (FILTER_PREFERENCE.equals(event.getKey())) {
			activeFilter = null;
		}
	}

	private void initFilters() {
		IExtensionPoint ep = getExtensionPoint(FILTERS_EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			try {
				String name = element.getAttribute(NAME_ATTR);
				String id = element.getAttribute(ID_ATTR);
				IProblemFilter filter = (IProblemFilter) element.createExecutableExtension(CLASS_ATTR);
				filters.add(new ProblemFilterDescriptor(name, id, filter));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private IExtensionPoint getExtensionPoint(String extensionPointName) {
		return Platform.getExtensionRegistry().getExtensionPoint(CodanCorePlugin.PLUGIN_ID, extensionPointName);
	}

	public IProblemFilter getActiveFilter() {
		if (activeFilter != null)
			return activeFilter;
		activeFilter = new NoProblemsFilter();
		String selectedId = CodanCorePlugin.getDefault().getStorePreferences().get(FILTER_PREFERENCE, FILTER_NONE);
		Iterator<ProblemFilterDescriptor> iterator = filters.iterator();
		while (iterator.hasNext()) {
			ProblemFilterDescriptor next = iterator.next();
			if (selectedId.equals(next.getId())) {
				activeFilter = next.getFilter();
			}
		}
		return activeFilter;
	}

	/**
	 * @return the singleton filter registry
	 */
	public static synchronized FilterRegistry getInstance() {
		if (instance == null)
			return new FilterRegistry();
		return instance;
	}

	public Collection<ProblemFilterDescriptor> getFilters() {
		return Collections.unmodifiableCollection(filters);
	}
}
