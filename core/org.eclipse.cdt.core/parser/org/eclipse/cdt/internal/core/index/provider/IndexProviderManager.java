/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.provider.IIndexProvider;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;

/**
 * The IndexProviderManager is responsible for maintaining the set of index
 * fragments contributed via the CIndex extension point.
 * <p>
 * For bug 196338, the role of this class was extended. It now has responsibility to look
 * at the pool of fragments available depending on their IDs, and select the most appropriate.
 * The following rules are applied:
 * <ul>
 * 	<li>If a fragment is not compatible, don't use it.</li>
 *  <li>If multiple fragments are compatible, pick the latest.</li>
 * </ul>
 *
 * A warning is logged if a fragment is contributed which is incompatible, and for which there is
 * no compatible equivalent.
 *
 * It is an internal class, and is public only for testing purposes.
 * @since 4.0
 */
public final class IndexProviderManager implements IElementChangedListener {
	private static final String ELEMENT_RO_PDOM_PROVIDER = "ReadOnlyPDOMProvider"; //$NON-NLS-1$
	private static final String ELEMENT_RO_INDEX_FRAGMENT_PROVIDER = "ReadOnlyIndexFragmentProvider"; //$NON-NLS-1$
	private static final String ELEMENT_PROVIDER_USAGE = "FragmentProviderUsage"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String ATTRIBUTE_CLASS = "class", ATTRIBUTE_NAVIGATION = "navigation",
			ATTRIBUTE_CONTENT_ASSIST = "content_assist", ATTRIBUTE_ADD_IMPORT = "add_import",
			ATTRIBUTE_CALL_HIERARCHY = "call_hierarchy", ATTRIBUTE_TYPE_HIERARCHY = "type_hierarchy",
			ATTRIBUTE_INCLUDE_BROWSER = "include_browser", ATTRIBUTE_SEARCH = "search", ATTRIBUTE_EDITOR = "editor";

	private IIndexFragmentProvider[] fragmentProviders;
	private int[] fragmentProviderUsage;
	private Map<ProvisionMapKey, Boolean> provisionMap;
	private Set<String> compatibleFragmentUnavailable;
	private VersionRange pdomVersionRange;

	public IndexProviderManager() {
		reset();
	}

	/**
	 * <b>Note: This method should not be called by clients for purposes other than testing</b>
	 */
	public void reset() {
		Version minVersion = Version.parseVersion(PDOM.versionString(PDOM.getMinSupportedVersion()));
		Version maxVersion = Version.parseVersion(PDOM.versionString(PDOM.getMaxSupportedVersion()));
		reset(new VersionRange(minVersion, true, maxVersion, true));
	}

	/**
	 * <b>Note: This method should not be called by clients for purposes other than testing</b>
	 * @param pdomVersionRange
	 */
	public void reset(VersionRange pdomVersionRange) {
		this.fragmentProviders = new IIndexFragmentProvider[0];
		this.provisionMap = new HashMap<>();
		this.pdomVersionRange = pdomVersionRange;
		this.compatibleFragmentUnavailable = new HashSet<>();
	}

	public void startup() {
		List<IIndexFragmentProvider> providers = new ArrayList<>();
		List<IConfigurationElement[]> usageSpecifications = new ArrayList<>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint indexProviders = registry.getExtensionPoint(CCorePlugin.INDEX_UNIQ_ID);
		for (IExtension extension : indexProviders.getExtensions()) {
			try {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (ELEMENT_RO_PDOM_PROVIDER.equals(element.getName())) {
						Object provider = element.createExecutableExtension(ATTRIBUTE_CLASS);
						if (provider instanceof IReadOnlyPDOMProvider) {
							providers.add(new ReadOnlyPDOMProviderBridge((IReadOnlyPDOMProvider) provider));
							usageSpecifications.add(element.getChildren(ELEMENT_PROVIDER_USAGE));
						} else {
							CCorePlugin.log(NLS.bind(Messages.IndexProviderManager_InvalidIndexProvider,
									extension.getContributor().getName()));
						}
					} else if (ELEMENT_RO_INDEX_FRAGMENT_PROVIDER.equals(element.getName())) {
						Object provider = element.createExecutableExtension(ATTRIBUTE_CLASS);

						if (provider instanceof IIndexFragmentProvider) {
							providers.add((IIndexFragmentProvider) provider);
							usageSpecifications.add(element.getChildren(ELEMENT_PROVIDER_USAGE));
						} else {
							CCorePlugin.log(NLS.bind(Messages.IndexProviderManager_InvalidIndexProvider,
									extension.getContributor().getName()));
						}
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}

		CoreModel.getDefault().addElementChangedListener(this);
		this.fragmentProviders = providers.toArray(new IIndexFragmentProvider[providers.size()]);
		this.fragmentProviderUsage = computeProviderUsage(usageSpecifications);
		assert fragmentProviders.length == fragmentProviderUsage.length;
	}

	private int[] computeProviderUsage(List<IConfigurationElement[]> usageFilters) {
		int[] usage = new int[usageFilters.size()];
		for (int i = 0; i < usage.length; i++) {
			IConfigurationElement[] usageFilter = usageFilters.get(i);
			usage[i] = computeProviderUsage(usageFilter);
		}
		return usage;
	}

	private int computeProviderUsage(IConfigurationElement[] usageFilter) {
		if (usageFilter == null || usageFilter.length == 0)
			return -1; // Allow usage for all tools.

		int result = 0;
		IConfigurationElement elem = usageFilter[0];
		result |= getOption(elem, ATTRIBUTE_ADD_IMPORT, IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
		result |= getOption(elem, ATTRIBUTE_CALL_HIERARCHY, IIndexManager.ADD_EXTENSION_FRAGMENTS_CALL_HIERARCHY);
		result |= getOption(elem, ATTRIBUTE_CONTENT_ASSIST, IIndexManager.ADD_EXTENSION_FRAGMENTS_CONTENT_ASSIST);
		result |= getOption(elem, ATTRIBUTE_INCLUDE_BROWSER, IIndexManager.ADD_EXTENSION_FRAGMENTS_INCLUDE_BROWSER);
		result |= getOption(elem, ATTRIBUTE_NAVIGATION, IIndexManager.ADD_EXTENSION_FRAGMENTS_NAVIGATION);
		result |= getOption(elem, ATTRIBUTE_SEARCH, IIndexManager.ADD_EXTENSION_FRAGMENTS_SEARCH);
		result |= getOption(elem, ATTRIBUTE_TYPE_HIERARCHY, IIndexManager.ADD_EXTENSION_FRAGMENTS_TYPE_HIERARCHY);
		result |= getOption(elem, ATTRIBUTE_EDITOR, IIndexManager.ADD_EXTENSION_FRAGMENTS_EDITOR);

		return result;
	}

	public int getOption(IConfigurationElement elem, String attributeName, int option) {
		if (Boolean.parseBoolean(elem.getAttribute(attributeName)))
			return option;
		return 0;
	}

	/**
	 * Get the array of IIndexFragment objects provided by all of the
	 * registered IIndexProvider objects for the specified project, and
	 * for the current state of the project.
	 * Order in the array is not significant.
	 * @param config
	 * @return the array of IIndexFragment objects for the current state
	 */
	public IIndexFragment[] getProvidedIndexFragments(ICConfigurationDescription config, int usage)
			throws CoreException {
		Map<String, IIndexFragment> id2fragment = new HashMap<>();

		IProject project = config.getProjectDescription().getProject();
		for (int i = 0; i < fragmentProviders.length; i++) {
			if ((fragmentProviderUsage[i] & usage) != 0) {
				IIndexFragmentProvider provider = fragmentProviders[i];
				try {
					if (providesForProject(provider, project)) {
						IIndexFragment[] fragments = provider.getIndexFragments(config);
						for (IIndexFragment fragment : fragments) {
							try {
								processCandidate(id2fragment, fragment);
							} catch (InterruptedException e) {
								CCorePlugin.log(e); // continue with next candidate
							} catch (CoreException e) {
								CCorePlugin.log(e); // continue with next candidate
							}
						}
					}
				} catch (CoreException e) {
					CCorePlugin.log(e); // move to next provider
				}
			}
		}

		// Make log entries for any fragments which have no compatible equivalents
		List<IIndexFragment> preresult = new ArrayList<>();
		for (Map.Entry<String, IIndexFragment> entry : id2fragment.entrySet()) {
			if (entry.getValue() == null) {
				String key = entry.getKey();
				if (!compatibleFragmentUnavailable.contains(key)) {
					String msg = NLS.bind(Messages.IndexProviderManager_NoCompatibleFragmentsAvailable, key,
							collectVersions(config, project, usage, key));
					CCorePlugin.log(new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg));
					compatibleFragmentUnavailable.add(key);
				}
			} else {
				preresult.add(entry.getValue());
			}
		}
		return preresult.toArray(new IIndexFragment[preresult.size()]);
	}

	/**
	 * Used for logging a problem.
	 */
	private String collectVersions(ICConfigurationDescription config, IProject project, int usage, String fragid) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < fragmentProviders.length; i++) {
			if ((fragmentProviderUsage[i] & usage) != 0) {
				IIndexFragmentProvider provider = fragmentProviders[i];
				try {
					if (providesForProject(provider, project)) {
						IIndexFragment[] fragments = provider.getIndexFragments(config);
						for (IIndexFragment fragment : fragments) {
							try {
								fragment.acquireReadLock();
								try {
									if (fragid.equals(fragment.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID))) {
										String csver = fragment
												.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION);
										if (csver != null) {
											if (result.length() > 0)
												result.append(", "); //$NON-NLS-1$
											result.append(csver);
										}
									}
								} finally {
									fragment.releaseReadLock();
								}
							} catch (Exception e) {
								// No logging, we are generating a msg for the log.
							}
						}
					}
				} catch (CoreException e) {
					// No logging, we are generating a msg for the log.
				}
			}
		}
		return result.toString();
	}

	/**
	 * Returns the version range supported by the format identified by the specified formatID.
	 * @param formatID
	 */
	private VersionRange getCurrentlySupportedVersionRangeForFormat(String formatID) {
		if (PDOM.FRAGMENT_PROPERTY_VALUE_FORMAT_ID.equals(formatID)) {
			return pdomVersionRange;
		}
		// Version range checks do not apply to non-PDOM IIndexFragments.
		return null;
	}

	/**
	 * Examines the candidate fragment, adding it to the map (using its fragment id as key) if
	 * it compatible with the current run-time, and it is better than any existing fragments for
	 * the same fragment id.
	 * @param id2fragment
	 * @param candidate
	 */
	private void processCandidate(Map<String, IIndexFragment> id2fragment, IIndexFragment candidate)
			throws InterruptedException, CoreException {
		String cid = null, csver = null, cformatID = null;
		candidate.acquireReadLock();
		try {
			cid = candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			csver = candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION);
			cformatID = candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID);
		} finally {
			candidate.releaseReadLock();
		}
		assert cid != null && csver != null && cformatID != null;

		Version cver = Version.parseVersion(csver); // illegal argument exception
		IIndexFragment existing = id2fragment.get(cid);

		VersionRange versionRange = getCurrentlySupportedVersionRangeForFormat(cformatID);
		if (versionRange == null || versionRange.isIncluded(cver)) {
			if (existing != null) {
				String esver = null, eformatID = null;
				existing.acquireReadLock();
				try {
					esver = existing.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION);
					eformatID = existing.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID);
				} finally {
					existing.releaseReadLock();
				}

				if (eformatID.equals(cformatID)) {
					Version ever = Version.parseVersion(esver); // illegal argument exception
					if (ever.compareTo(cver) < 0) {
						id2fragment.put(cid, candidate);
					}
				} else {
					/*
					 * In future we could allow users to specify
					 * how format (i.e. PDOM -> custom IIndexFragment implementation)
					 * changes are coped with.
					 */
				}
			} else {
				id2fragment.put(cid, candidate);
			}
		} else {
			if (existing == null) {
				id2fragment.put(cid, null); // signifies candidate is unusable
			}
		}
	}

	/**
	 * Adds a PDOM-based index fragment provider.
	 *
	 * <b>Note: This method should not be called for purposes other than testing</b>
	 * @param provider
	 */
	public void addIndexProvider(IIndexProvider provider) {
		if (!(provider instanceof IIndexFragmentProvider)) {
			/* This engineering compromise can be resolved when we address whether
			 * IIndexFragment can be made public. The extension point only accepts
			 * instances of IOfflinePDOMIndexProvider so this should never happen (tm)
			 */
			CCorePlugin.log("An unknown index provider implementation was plugged in to the CIndex extension point"); //$NON-NLS-1$
			return;
		}

		final int length = fragmentProviders.length;

		IIndexFragmentProvider[] newProviders = new IIndexFragmentProvider[length + 1];
		System.arraycopy(fragmentProviders, 0, newProviders, 0, length);
		newProviders[length] = (IIndexFragmentProvider) provider;
		fragmentProviders = newProviders;

		int[] newFilters = new int[length + 1];
		System.arraycopy(fragmentProviderUsage, 0, newFilters, 0, length);
		newFilters[length] = -1;
		fragmentProviderUsage = newFilters;
	}

	/**
	 * Removes the specified provider by object identity. Only a PDOM-based provider can be removed
	 * using this method.
	 *
	 * <b>Note: This method should not be called for purposes other than testing</b>
	 * @param provider
	 */
	public void removeIndexProvider(IIndexProvider provider) {
		for (int i = 0; i < fragmentProviders.length; i++) {
			if (fragmentProviders[i] == provider) {
				final int length = fragmentProviders.length;
				IIndexFragmentProvider[] newProviders = new IIndexFragmentProvider[length - 1];
				System.arraycopy(fragmentProviders, 0, newProviders, 0, i);
				System.arraycopy(fragmentProviders, i + 1, newProviders, i, length - i - 1);
				fragmentProviders = newProviders;

				int[] newFilters = new int[length - 1];
				System.arraycopy(fragmentProviderUsage, 0, newFilters, 0, i);
				System.arraycopy(fragmentProviderUsage, i + 1, newFilters, i, length - i - 1);
				fragmentProviderUsage = newFilters;
				return;
			}
		}
	}

	private boolean providesForProject(IIndexProvider provider, IProject project) {
		ProvisionMapKey key = new ProvisionMapKey(provider, project);

		if (!provisionMap.containsKey(key)) {
			try {
				ICProject cproject = CoreModel.getDefault().create(project);
				provisionMap.put(key, Boolean.valueOf(provider.providesFor(cproject)));
			} catch (CoreException e) {
				CCorePlugin.log(e);
				provisionMap.put(key, Boolean.FALSE);
			}
		}

		return provisionMap.get(key).booleanValue();
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		try {
			if (event.getType() == ElementChangedEvent.POST_CHANGE) {
				processDelta(event.getDelta());
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	private void processDelta(ICElementDelta delta) throws CoreException {
		int type = delta.getElement().getElementType();
		switch (type) {
		case ICElement.C_MODEL:
			// Loop through the children
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i]);
			}
			break;
		case ICElement.C_PROJECT:
			final ICProject cproject = (ICProject) delta.getElement();
			switch (delta.getKind()) {
			case ICElementDelta.REMOVED:
				List<ProvisionMapKey> toRemove = new ArrayList<>();
				for (ProvisionMapKey key : provisionMap.keySet()) {
					if (key.getProject().equals(cproject.getProject())) {
						toRemove.add(key);
					}
				}
				for (ProvisionMapKey key : toRemove) {
					provisionMap.remove(key);
				}
				break;
			}
		}
	}

	private static class ProvisionMapKey {
		private final IIndexProvider provider;
		private final IProject project;

		ProvisionMapKey(IIndexProvider provider, IProject project) {
			this.provider = provider;
			this.project = project;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ProvisionMapKey) {
				ProvisionMapKey other = (ProvisionMapKey) obj;
				return other.project.equals(project) && other.provider.equals(provider);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return project.hashCode() ^ provider.hashCode();
		}

		public IProject getProject() {
			return project;
		}
	}
}
