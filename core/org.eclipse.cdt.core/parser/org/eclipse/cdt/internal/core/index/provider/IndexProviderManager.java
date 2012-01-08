/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.provider.IIndexProvider;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private static final String ELEMENT_RO_PDOM_PROVIDER= "ReadOnlyPDOMProvider"; //$NON-NLS-1$
    private static final String ELEMENT_RO_INDEX_FRAGMENT_PROVIDER= "ReadOnlyIndexFragmentProvider"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	private IIndexFragmentProvider[] pdomFragmentProviders;
	private IIndexFragmentProvider[] nonPDOMFragmentProviders;
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
		Version minVersion= Version.parseVersion(PDOM.versionString(PDOM.getMinSupportedVersion()));
		Version maxVersion= Version.parseVersion(PDOM.versionString(PDOM.getMaxSupportedVersion()));
		reset(new VersionRange(minVersion, true, maxVersion, true));
	}

	/**
	 * <b>Note: This method should not be called by clients for purposes other than testing</b>
	 * @param pdomVersionRange
	 */
	public void reset(VersionRange pdomVersionRange) {
		this.pdomFragmentProviders= new IIndexFragmentProvider[0];
		this.provisionMap= new HashMap<ProvisionMapKey, Boolean>();
		this.pdomVersionRange= pdomVersionRange;
		this.compatibleFragmentUnavailable= new HashSet<String>();
	}

	public void startup() {
		List<IIndexFragmentProvider> pdomProviders = new ArrayList<IIndexFragmentProvider>();
		List<IIndexFragmentProvider> nonPDOMProviders = new ArrayList<IIndexFragmentProvider>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint indexProviders = registry.getExtensionPoint(CCorePlugin.INDEX_UNIQ_ID);
		for (IExtension extension : indexProviders.getExtensions()) {
			try {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (ELEMENT_RO_PDOM_PROVIDER.equals(element.getName())) {
						Object provider = element.createExecutableExtension(ATTRIBUTE_CLASS);

						if (provider instanceof IReadOnlyPDOMProvider) {
							pdomProviders.add(new ReadOnlyPDOMProviderBridge((IReadOnlyPDOMProvider) provider));
						} else {
							CCorePlugin.log(NLS.bind(Messages.IndexProviderManager_0,
									extension.getContributor().getName()));
						}
					} else if (ELEMENT_RO_INDEX_FRAGMENT_PROVIDER.equals(element.getName())) {
                        Object provider = element.createExecutableExtension(ATTRIBUTE_CLASS);

                        if (provider instanceof IIndexFragmentProvider) {
                            nonPDOMProviders.add((IIndexFragmentProvider) provider);
                        } else {
                            CCorePlugin.log(NLS.bind(Messages.IndexProviderManager_0,
                                    extension.getContributor().getName()));
                        }
                    }
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}

		CoreModel.getDefault().addElementChangedListener(this);
		this.pdomFragmentProviders = pdomProviders.toArray(new IIndexFragmentProvider[pdomProviders.size()]);
		this.nonPDOMFragmentProviders = nonPDOMProviders.toArray(new IIndexFragmentProvider[nonPDOMProviders.size()]);
	}

	/**
	 * Get the array of IIndexFragment objects provided by all of the
	 * registered IIndexProvider objects for the specified project, and
	 * for the current state of the project.
	 * Order in the array is not significant.
	 * @param config
	 * @return the array of IIndexFragment objects for the current state
	 */
	public IIndexFragment[] getProvidedIndexFragments(ICConfigurationDescription config,
			boolean includeNonPDOMFragments) throws CoreException {
		Map<String, IIndexFragment> id2fragment = new HashMap<String, IIndexFragment>();

		IProject project= config.getProjectDescription().getProject();
		IIndexFragmentProvider[][] groups = includeNonPDOMFragments ?
				new IIndexFragmentProvider[][] { pdomFragmentProviders, nonPDOMFragmentProviders } :
				new IIndexFragmentProvider[][] { pdomFragmentProviders };
		for (IIndexFragmentProvider[] group : groups) {
			for (IIndexFragmentProvider provider : group) {
				try {
					if (providesForProject(provider, project)) {
						IIndexFragment[] fragments= provider.getIndexFragments(config);
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
		List<IIndexFragment> preresult= new ArrayList<IIndexFragment>();
		for (Map.Entry<String, IIndexFragment> entry : id2fragment.entrySet()) {
			if (entry.getValue() == null) {
				String key= entry.getKey();
				if (!compatibleFragmentUnavailable.contains(key)) {
					String msg= NLS.bind(
							Messages.IndexProviderManager_NoCompatibleFragmentsAvailable, key);
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
		String cid= null, csver= null, cformatID= null;
		candidate.acquireReadLock();
		try {
			cid= candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
			csver= candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION);
			cformatID= candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID);
		} finally {
			candidate.releaseReadLock();
		}
		assert cid != null && csver != null && cformatID != null;

		Version cver= Version.parseVersion(csver); // illegal argument exception
		IIndexFragment existing= id2fragment.get(cid);

		VersionRange versionRange = getCurrentlySupportedVersionRangeForFormat(cformatID);
		if (versionRange == null || versionRange.isIncluded(cver)) {
			if (existing != null) {
				String esver= null, eformatID= null;
				existing.acquireReadLock();
				try {
					esver= existing.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION);
					eformatID= existing.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID);
				} finally {
					existing.releaseReadLock();
				}

				if (eformatID.equals(cformatID)) {
					Version ever= Version.parseVersion(esver); // illegal argument exception
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

		IIndexFragmentProvider[] newAllProviders = new IIndexFragmentProvider[pdomFragmentProviders.length + 1];
		System.arraycopy(pdomFragmentProviders, 0, newAllProviders, 0, pdomFragmentProviders.length);
		newAllProviders[pdomFragmentProviders.length] = (IIndexFragmentProvider) provider;
		pdomFragmentProviders = newAllProviders;
	}

	/**
	 * Removes the specified provider by object identity. Only a PDOM-based provider can be removed
	 * using this method.
	 *
	 * <b>Note: This method should not be called for purposes other than testing</b>
	 * @param provider
	 */
	public void removeIndexProvider(IIndexProvider provider) {
		ArrayUtil.remove(pdomFragmentProviders, provider);
		if (pdomFragmentProviders[pdomFragmentProviders.length - 1] == null) {
			IIndexFragmentProvider[] newAllProviders = new IIndexFragmentProvider[pdomFragmentProviders.length - 1];
			System.arraycopy(pdomFragmentProviders, 0, newAllProviders, 0, pdomFragmentProviders.length - 1);
			pdomFragmentProviders= newAllProviders;
		}
	}

	private boolean providesForProject(IIndexProvider provider, IProject project) {
		ProvisionMapKey key= new ProvisionMapKey(provider, project);

		if (!provisionMap.containsKey(key)) {
			try {
				ICProject cproject= CoreModel.getDefault().create(project);
				provisionMap.put(key, new Boolean(provider.providesFor(cproject)));
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
			for (int i = 0; i < children.length; ++i)
				processDelta(children[i]);
			break;
		case ICElement.C_PROJECT:
			final ICProject cproject = (ICProject) delta.getElement();
			switch (delta.getKind()) {
			case ICElementDelta.REMOVED:
				List<ProvisionMapKey> toRemove = new ArrayList<ProvisionMapKey>();
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
			this.provider= provider;
			this.project= project;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ProvisionMapKey) {
				ProvisionMapKey other= (ProvisionMapKey) obj;
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
