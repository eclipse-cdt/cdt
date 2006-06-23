/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.folding.ICFoldingStructureProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class CFoldingStructureProviderRegistry {

	private static final String EXTENSION_POINT= "foldingStructureProviders"; //$NON-NLS-1$
	
	/** The map of descriptors, indexed by their identifiers. */
	private Map fDescriptors;

	/**
	 * Creates a new instance. 
	 */
	public CFoldingStructureProviderRegistry() {
	}
	
	/**
	 * Returns an array of <code>ICFoldingProviderDescriptor</code> describing
	 * all extension to the <code>foldingProviders</code> extension point.
	 * 
	 * @return the list of extensions to the
	 *         <code>quickDiffReferenceProvider</code> extension point.
	 */
	public CFoldingStructureProviderDescriptor[] getFoldingProviderDescriptors() {
		synchronized (this) {
			ensureRegistered();
			return (CFoldingStructureProviderDescriptor[]) fDescriptors.values().toArray(new CFoldingStructureProviderDescriptor[fDescriptors.size()]);
		}
	}
	
	/**
	 * Returns the folding provider with identifier <code>id</code> or
	 * <code>null</code> if no such provider is registered.
	 * 
	 * @param id the identifier for which a provider is wanted
	 * @return the corresponding provider, or <code>null</code> if none can be
	 *         found
	 */
	public CFoldingStructureProviderDescriptor getFoldingProviderDescriptor(String id) {
		synchronized (this) {
			ensureRegistered();
			return (CFoldingStructureProviderDescriptor) fDescriptors.get(id);
		}
	}
	
	/**
	 * Instantiates and returns the provider that is currently configured in the
	 * preferences.
	 * 
	 * @return the current provider according to the preferences
	 */
	public ICFoldingStructureProvider getCurrentFoldingProvider() {
		String id= CUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.EDITOR_FOLDING_PROVIDER);
		CFoldingStructureProviderDescriptor desc= getFoldingProviderDescriptor(id);
		if (desc != null) {
			try {
				return desc.createProvider();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
		return null;
	}
	
	/**
	 * Ensures that the extensions are read and stored in
	 * <code>fDescriptors</code>.
	 */
	private void ensureRegistered() {
		if (fDescriptors == null)
			reloadExtensions();
	}

	/**
	 * Reads all extensions.
	 * <p>
	 * This method can be called more than once in
	 * order to reload from a changed extension registry.
	 * </p>
	 */
	public void reloadExtensions() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		Map map= new HashMap();

		IConfigurationElement[] elements= registry.getConfigurationElementsFor(CUIPlugin.getPluginId(), EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			CFoldingStructureProviderDescriptor desc= new CFoldingStructureProviderDescriptor(elements[i]);
			map.put(desc.getId(), desc);
		}
		
		synchronized(this) {
			fDescriptors= Collections.unmodifiableMap(map);
		}
	}


}
