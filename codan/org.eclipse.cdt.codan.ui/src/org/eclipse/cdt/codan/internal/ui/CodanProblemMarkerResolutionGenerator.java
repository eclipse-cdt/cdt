/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.ui.ICodanMarkerResolution;
import org.eclipse.cdt.codan.ui.ICodanMarkerResolutionExtension;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class CodanProblemMarkerResolutionGenerator implements IMarkerResolutionGenerator {
	private static final String EXTENSION_POINT_NAME = "codanMarkerResolution"; //$NON-NLS-1$
	private static final Map<String, Collection<ConditionalResolution>> conditionalResolutions = new HashMap<>();
	private static final List<IMarkerResolution> universalResolutions = new ArrayList<>();
	private static boolean resolutionsLoaded;

	static class ConditionalResolution {
		private final Pattern messagePattern;
		private final IMarkerResolution resolutionInstance;

		public static ConditionalResolution createFrom(IConfigurationElement configurationElement) {
			String rawPattern = configurationElement.getAttribute("messagePattern"); //$NON-NLS-1$
			try {
				return new ConditionalResolution(configurationElement,
						rawPattern != null ? Pattern.compile(rawPattern) : null);
			} catch (PatternSyntaxException e) {
				CodanUIActivator.log("Invalid message pattern: " + rawPattern); //$NON-NLS-1$
			}
			return null;
		}

		private ConditionalResolution(IConfigurationElement resolutionElement, Pattern messagePattern) {
			this.messagePattern = messagePattern;
			this.resolutionInstance = instantiateResolution(resolutionElement);
		}

		public boolean isApplicableFor(IMarker marker) {
			if (resolutionInstance instanceof ICodanMarkerResolution) {
				if (!((ICodanMarkerResolution) resolutionInstance).isApplicable(marker)) {
					return false;
				}
			}

			return messagePattern == null || messagePattern.matcher(marker.getAttribute(IMarker.MESSAGE, "")).matches(); //$NON-NLS-1$
		}

		public IMarkerResolution getResolution() {
			return resolutionInstance;
		}

		public Pattern getMessagePattern() {
			return messagePattern;
		}

		public void setMarkerArguments(IMarker marker) {
			if (messagePattern == null) {
				return;
			}
			String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
			Matcher matcher = messagePattern.matcher(message);
			int n = matcher.groupCount();
			if (n == 0)
				return;
			if (!matcher.matches())
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
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		if (!resolutionsLoaded) {
			readExtensions();
		}
		String id = marker.getAttribute(ICodanProblemMarker.ID, null);
		if (id == null && conditionalResolutions.get(id) == null)
			return new IMarkerResolution[0];

		Collection<ConditionalResolution> candidates = conditionalResolutions.get(id);
		ArrayList<IMarkerResolution> resolutions = new ArrayList<>();

		if (candidates != null) {
			candidates.stream().filter(candidate -> candidate.isApplicableFor(marker))
					.peek(candidate -> candidate.setMarkerArguments(marker)).map(ConditionalResolution::getResolution)
					.forEach(resolutions::add);
		}

		universalResolutions.stream().filter(
				res -> !(res instanceof ICodanMarkerResolution) || ((ICodanMarkerResolution) res).isApplicable(marker))
				.forEach(resolutions::add);

		return resolutions.stream().peek(res -> {
			if (res instanceof ICodanMarkerResolutionExtension) {
				((ICodanMarkerResolutionExtension) res).prepareFor(marker);
			}
		}).toArray(IMarkerResolution[]::new);
	}

	/**
	 * @param matcher
	 * @param marker
	 */

	private static synchronized void readExtensions() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(CodanUIActivator.PLUGIN_ID,
				EXTENSION_POINT_NAME);
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
		final String elementName = configurationElement.getName();
		if (elementName.equals("resolution")) { //$NON-NLS-1$
			String id = configurationElement.getAttribute("problemId"); //$NON-NLS-1$
			String messagePattern = configurationElement.getAttribute("messagePattern"); //$NON-NLS-1$
			if (id == null && messagePattern == null) {
				CodanUIActivator.log("Extension for " + EXTENSION_POINT_NAME //$NON-NLS-1$
						+ " problemId is not defined"); //$NON-NLS-1$
				return;
			}
			ConditionalResolution candidate = ConditionalResolution.createFrom(configurationElement);
			if (candidate == null) {
				return;
			}
			addResolution(id, candidate);
		} else if (elementName.equals("universalResolution")) { //$NON-NLS-1$
			universalResolutions.add(instantiateResolution(configurationElement));
		}
	}

	private static IMarkerResolution instantiateResolution(IConfigurationElement element) {
		try {
			return (IMarkerResolution) element.createExecutableExtension("class");//$NON-NLS-1$
		} catch (CoreException e) {
			CodanUIActivator.log(e);
		}
		return null;
	}

	private static void addResolution(String id, ConditionalResolution res) {
		Collection<ConditionalResolution> candidates = conditionalResolutions.get(id);
		if (candidates == null) {
			candidates = new ArrayList<>();
			conditionalResolutions.put(id, candidates);
		}
		candidates.add(res);
	}
}