/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.resources;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 *
 * @author crecoskie
 * @since 5.3
 *
 */
public class RefreshExclusionContributionManager {

	public static final String EXCLUSION_CONTRIBUTOR = "exclusionContributor"; //$NON-NLS-1$
	public static final String EXTENSION_ID = "RefreshExclusionContributor"; //$NON-NLS-1$
	private static RefreshExclusionContributionManager fInstance;

	public static synchronized RefreshExclusionContributionManager getInstance() {
		if (fInstance == null) {
			fInstance = new RefreshExclusionContributionManager();
		}

		return fInstance;
	}

	private LinkedHashMap<String, RefreshExclusionContributor> fIDtoContributorsMap;

	private RefreshExclusionContributionManager() {
		fIDtoContributorsMap = new LinkedHashMap<>();
		loadExtensions();
	}

	public RefreshExclusionContributor getContributor(String id) {
		return fIDtoContributorsMap.get(id);
	}

	public List<RefreshExclusionContributor> getContributors() {
		return getContributors(false);
	}

	public List<RefreshExclusionContributor> getContributors(boolean returnTestContributors) {
		List<RefreshExclusionContributor> retVal = new LinkedList<>();

		if (!returnTestContributors) {
			for (RefreshExclusionContributor contributor : fIDtoContributorsMap.values()) {
				if (!contributor.isTest()) {
					retVal.add(contributor);
				}
			}

			return retVal;
		}

		else {
			return new LinkedList<>(fIDtoContributorsMap.values());
		}
	}

	public synchronized void loadExtensions() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID,
				EXTENSION_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {

					if (configElement.getName().equals(EXCLUSION_CONTRIBUTOR)) {

						String id = configElement.getAttribute("id"); //$NON-NLS-1$
						String name = configElement.getAttribute("name"); //$NON-NLS-1$
						String contributorClassName = configElement.getAttribute("class"); //$NON-NLS-1$
						boolean isTest = false;
						String isTestString = configElement.getAttribute("isTest"); //$NON-NLS-1$
						if (isTestString != null) {
							isTest = Boolean.getBoolean(isTestString);
						}

						if (contributorClassName != null) {
							try {
								Object execExt = configElement.createExecutableExtension("class"); //$NON-NLS-1$
								if ((execExt instanceof RefreshExclusionContributor) && id != null) {
									RefreshExclusionContributor exclusionContributor = (RefreshExclusionContributor) execExt;
									exclusionContributor.setID(id);
									exclusionContributor.setName(name);
									exclusionContributor.setIsTest(isTest);
									fIDtoContributorsMap.put(id, exclusionContributor);

								}
							} catch (CoreException e) {
								CUIPlugin.log(e);
							}
						}
					}
				}
			}
		}
	}
}
