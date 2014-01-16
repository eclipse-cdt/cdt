/*
 * Copyright (c) 2014 BlackBerry Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IProblemMarkerFilter;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * The purpose of ProblemMarkerFilterManager is to manage ProblemMarkerFilter extension points.
 * {@link ErrorParserManager} use this manager to filter out unnecessary problem markers
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProblemMarkerFilterManager {

	/**
	 * Name of ProblemMarkerFilter extension point
	 */
	private final static String EXTENSION_POINT = "ProblemMarkerFilter"; //$NON-NLS-1$

	/**
	 * Singleton instance of ProblemMarkerFilterManager
	 */
	private final static ProblemMarkerFilterManager INSTANCE = new ProblemMarkerFilterManager();

	/**
	 * List of all executable extension registered in Extension Registry
	 */
	private final List<ProblemMarkerFilterDesc> filters = new ArrayList<ProblemMarkerFilterDesc>();

	/**
	 * Cache of active filters for known projects.
	 * This cache allow to skip evaluation of enablementExpression for every marker.
	 */
	private final Map<IProject, List<ProblemMarkerFilterDesc>> filtersCache = new WeakHashMap<IProject, List<ProblemMarkerFilterDesc>>();

	/**
	 * Return singleton instance of ProblemMarkerFilterManager 
	 * 
	 * @return singleton instance of ProblemMarkerFilterManager 
	 */
	public static ProblemMarkerFilterManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Constructor.
	 *
	 * Creates instances of executable extension for ProblemMarkerFilter extension point 
	 *
	 */
	private ProblemMarkerFilterManager() {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = reg
				.getConfigurationElementsFor(CCorePlugin.PLUGIN_ID, EXTENSION_POINT);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			ProblemMarkerFilterDesc filterDesc = new ProblemMarkerFilterDesc(element);
			filters.add(filterDesc);
		}
	}

	/**
	 * Called by {@link ErrorParserManager#addProblemMarker(ProblemMarkerInfo)} to filter out unnecessary problem markers 
	 * 
	 * Problem marker is ignored if any plug-in that implements ProblemMarkerFilter extension point rejects it.
	 * 
	 * @see IProblemMarkerFilter#acceptMarker(ProblemMarkerInfo) 
	 * 
	 * @param markerInfo description of the problem marker that is going to be added
	 * @return true if markers should be reported, false if should be ignored
	 */
	public boolean acceptMarker(ProblemMarkerInfo markerInfo) {
		IProject project = markerInfo.file.getProject();
		if (project == null || !project.isOpen())
			return true;
		List<ProblemMarkerFilterDesc> enabledFilters = findEnabledFilters(project);
		for (ProblemMarkerFilterDesc filterDesc: enabledFilters) {
			if ( ! filterDesc.getFilter().acceptMarker(markerInfo) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Try to find enabled filter for project and cache results
	 * @param project project for which we want know enabled filters
	 * @return list of enabled filters
	 */
	private List<ProblemMarkerFilterDesc> findEnabledFilters(IProject project) {
		synchronized (filtersCache) {
			List<ProblemMarkerFilterDesc> result = filtersCache.get(project);
			if (result == null) {
				result = new ArrayList<ProblemMarkerFilterDesc>();
				for (ProblemMarkerFilterDesc filterDesc: filters) {
					if ( filterDesc.matches(project) ) {
						result.add(filterDesc);
					}
				}
				filtersCache.put(project, result);
			}
			return result;
		}
	}

}
