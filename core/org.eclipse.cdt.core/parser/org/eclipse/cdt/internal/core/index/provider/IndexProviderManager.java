/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;


/**
 * The IndexProviderManager is responsible for maintaining the set of index
 * fragments contributed via the CIndex extension point.
 * <p>
 * It is an internal class, and is public only for testing purposes.
 */
public final class IndexProviderManager implements IElementChangedListener {
	public static final String READ_ONLY_PDOM_PROVIDER= "ReadOnlyPDOMProvider"; //$NON-NLS-1$
	private IIndexFragmentProvider[] allProviders;
	private Map provisionMap= new HashMap();
	
	public IndexProviderManager() {
		List providers = new ArrayList();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint indexProviders = registry.getExtensionPoint(CCorePlugin.INDEX_UNIQ_ID);
		IExtension[] extensions = indexProviders.getExtensions();
		for(int i=0; i<extensions.length; i++) {
			IExtension extension = extensions[i];
			try {
				IConfigurationElement[] ce = extension.getConfigurationElements();
				for(int j=0; j<ce.length; j++) {
					if(ce[j].getName().equals(READ_ONLY_PDOM_PROVIDER)) {
						IIndexProvider provider = (IIndexProvider) ce[0].createExecutableExtension("class"); //$NON-NLS-1$
						if(provider instanceof IReadOnlyPDOMProvider) {
							provider = new ReadOnlyPDOMProviderBridge((IReadOnlyPDOMProvider)provider);
							providers.add(provider);
						} else {
							CCorePlugin.log(MessageFormat.format(
									Messages.IndexProviderManager_0,
									new Object[] {extension.getContributor().getName()}
							));
						}
					}
				}
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			}
		}

		CoreModel.getDefault().addElementChangedListener(this);
		this.allProviders = (IIndexFragmentProvider[]) providers.toArray(new IIndexFragmentProvider[providers.size()]);
	}

	/**
	 * Get the array of IIndexFragment objects provided by all of the
	 * registered IIndexProvider objects for the specified project, and
	 * for the current state of the project.
	 * Order in the array is not significant.
	 * @param project
	 * @return the array of IIndexFragment objects for the current state
	 */
	public IIndexFragment[] getProvidedIndexFragments(ICConfigurationDescription config) throws CoreException {
		List preResult = new ArrayList();

		IProject project= config.getProjectDescription().getProject();
		for(int i=0; i<allProviders.length; i++) {
			try {
				if(providesForProject(allProviders[i], project)) {
					preResult.addAll(Arrays.asList(allProviders[i].getIndexFragments(config)));
				}
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			}
		}

		IIndexFragment[] result = (IIndexFragment[]) preResult.toArray(new IIndexFragment[preResult.size()]);
		return result;
	}

	/**
	 * This is only public for test purposes
	 * @param provider
	 */
	public void addIndexProvider(IIndexProvider provider) {
		if(!(provider instanceof IIndexFragmentProvider)) {
			/* This engineering compromise can be resolved when we address whether
			 * IIndexFragment can be made public. The extension point only accepts
			 * instances of IOfflinePDOMIndexProvider so this should never happen (tm)
			 */
			CCorePlugin.log("An unknown index provider implementation was plugged in to the CIndex extension point"); //$NON-NLS-1$
			return;
		}

		IIndexFragmentProvider[] newAllProviders = new IIndexFragmentProvider[allProviders.length+1];
		System.arraycopy(allProviders, 0, newAllProviders, 0, allProviders.length);
		newAllProviders[allProviders.length] = (IIndexFragmentProvider) provider;
		allProviders = newAllProviders;
	}

	private boolean providesForProject(IIndexProvider provider, IProject project) {
		List key = new ArrayList();
		key.add(provider);
		key.add(project);

		if(!provisionMap.containsKey(key)) {
			try {
				ICProject cproject= CoreModel.getDefault().create(project);
				provisionMap.put(key, new Boolean(provider.providesFor(cproject)));
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
				provisionMap.put(key, Boolean.FALSE);
			}
		}

		return ((Boolean) provisionMap.get(key)).booleanValue();
	}

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
			final ICProject cproject = (ICProject)delta.getElement();
			switch (delta.getKind()) {
			case ICElementDelta.REMOVED:
				List toRemove = new ArrayList();
				for(Iterator i = provisionMap.keySet().iterator(); i.hasNext(); ) {
					List key = (List) i.next();
					if(key.contains(cproject.getProject())) {
						toRemove.add(key);
					}
				}
				for(Iterator i = toRemove.iterator(); i.hasNext(); ) {
					provisionMap.remove(i.next());
				}
				break;
			}
		}
	}
}
