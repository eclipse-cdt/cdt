/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.ui.ICodanMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class CodanProblemMarkerResolutionGenerator implements IMarkerResolutionGenerator {
	private static final String EXTENSION_POINT_NAME = "codanMarkerResolution"; //$NON-NLS-1$
	private static final Map<String, Collection<ConditionalResolution>> resolutions = new HashMap<String, Collection<ConditionalResolution>>();
	private static boolean resolutionsLoaded;

	static class ConditionalResolution {
		IMarkerResolution res;
		String messagePattern;

		public ConditionalResolution(IMarkerResolution res, String messagePattern) {
			this.res = res;
			this.messagePattern = messagePattern;
		}
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		if (!resolutionsLoaded) {
			readExtensions();
		}
		String id = marker.getAttribute(ICodanProblemMarker.ID, null);
		if (id == null && resolutions.get(null) == null)
			return new IMarkerResolution[0];
		String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		Collection<ConditionalResolution> collection = resolutions.get(id);
		if (collection != null) {
			ArrayList<IMarkerResolution> list = new ArrayList<IMarkerResolution>();
			for (Iterator<ConditionalResolution> iterator = collection.iterator(); iterator.hasNext();) {
				ConditionalResolution res = iterator.next();
				if (res.messagePattern != null) {
					try {
						Pattern pattern = Pattern.compile(res.messagePattern);
						Matcher matcher = pattern.matcher(message);
						if (!matcher.matches())
							continue;
						if (id == null) {
							setArgumentsFromPattern(matcher, marker);
						}
					} catch (Exception e) {
						CodanUIActivator.log("Cannot compile regex: " + res.messagePattern); //$NON-NLS-1$
						continue;
					}
				}
				if (res.res instanceof ICodanMarkerResolution) {
					if (!((ICodanMarkerResolution) res.res).isApplicable(marker))
						continue;
				}
				list.add(res.res);
			}
			if (list.size() > 0)
				return list.toArray(new IMarkerResolution[list.size()]);
		}
		return new IMarkerResolution[0];
	}

	/**
	 * @param matcher
	 * @param marker
	 */
	private void setArgumentsFromPattern(Matcher matcher, IMarker marker) {
		int n = matcher.groupCount();
		if (n == 0)
			return;
		String[] res = new String[n];
		for (int i = 0; i < n; i++) {
			res[i] = matcher.group(i + 1);
		}
		String[] old = CodanProblemMarker.getProblemArguments(marker);
		if (!Arrays.deepEquals(res, old)) {
			try {
				CodanProblemMarker.setProblemArguments(marker, res);
			} catch (CoreException e) {
				CodanUIActivator.log(e);
			}
		}
	}

	private static synchronized void readExtensions() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(CodanUIActivator.PLUGIN_ID, EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		try {
			IConfigurationElement[] elements = ep.getConfigurationElements();
			// process categories
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement configurationElement = elements[i];
				processResolution(configurationElement);
			}
		} finally {
			resolutionsLoaded = true;
		}
	}

	/**
	 * @param configurationElement
	 */
	private static void processResolution(IConfigurationElement configurationElement) {
		if (configurationElement.getName().equals("resolution")) { //$NON-NLS-1$
			String id = configurationElement.getAttribute("problemId"); //$NON-NLS-1$
			String messagePattern = configurationElement.getAttribute("messagePattern"); //$NON-NLS-1$
			if (id == null && messagePattern == null) {
				CodanUIActivator.log("Extension for " + EXTENSION_POINT_NAME //$NON-NLS-1$
						+ " problemId is not defined"); //$NON-NLS-1$
				return;
			}
			IMarkerResolution res;
			try {
				res = (IMarkerResolution) configurationElement.createExecutableExtension("class");//$NON-NLS-1$
			} catch (CoreException e) {
				CodanUIActivator.log(e);
				return;
			}
			if (messagePattern != null) {
				try {
					Pattern.compile(messagePattern);
				} catch (Exception e) {
					// bad pattern log and ignore
					CodanUIActivator.log("Extension for " //$NON-NLS-1$
							+ EXTENSION_POINT_NAME + " messagePattern is invalid: " + e.getMessage()); //$NON-NLS-1$
					return;
				}
			}
			ConditionalResolution co = new ConditionalResolution(res, messagePattern);
			addResolution(id, co);
		}
	}

	public static void addResolution(String id, IMarkerResolution res, String messagePattern) {
		addResolution(id, new ConditionalResolution(res, messagePattern));
	}

	private static void addResolution(String id, ConditionalResolution res) {
		Collection<ConditionalResolution> collection = resolutions.get(id);
		if (collection == null) {
			collection = new ArrayList<ConditionalResolution>();
			resolutions.put(id, collection);
		}
		collection.add(res);
	}
}