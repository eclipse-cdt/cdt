/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.newconnection;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemTypeMatcher;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.wizards.registries.RSEAbstractWizardRegistry;
import org.eclipse.rse.ui.wizards.registries.RSEWizardDescriptor;


/**
 * RSE new connection wizard descriptor implementation
 */
public class RSENewConnectionWizardDescriptor extends RSEWizardDescriptor implements IRSENewConnectionWizardDescriptor {
	private final SystemTypeMatcher systemTypeMatcher;

	// The list of resolved system types supported by this wizard.
	private List resolvedSystemTypes;
	
	/**
	 * Constructor
	 * 
	 * @param wizardRegistry The parent wizard registry this element belongs to. Must be not <code>null</code>.
	 * @param element The configuration element which is declaring a wizard. Must be not <code>null</code>.
	 */
	public RSENewConnectionWizardDescriptor(RSEAbstractWizardRegistry wizardRegistry, IConfigurationElement element) {
		super(wizardRegistry, element);
		systemTypeMatcher = new SystemTypeMatcher(getDeclaredSystemTypeIds());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.newconnection.INewConnectionWizardDescriptor#getDeclaredSystemTypeIds()
	 */
	public String getDeclaredSystemTypeIds() {
		return getConfigurationElement().getAttribute("systemTypeIds"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.newconnection.INewConnectionWizardDescriptor#getSystemTypeIds()
	 */
	public String[] getSystemTypeIds() {
		if (resolvedSystemTypes == null) {
			resolvedSystemTypes = new LinkedList();
			
			// If the subsystem configuration supports all system types, just add all
			// currently registered system types to th resolved list
			if (systemTypeMatcher.supportsAllSystemTypes()) {
				String[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypeNames();
				if (systemTypes != null) resolvedSystemTypes.addAll(Arrays.asList(systemTypes));
			} else {
				// We have to match the given lists of system type ids against
				// the list of available system types. As the list of system types cannot
				// change ones it has been initialized, we filter out the not matching ones
				// here directly.
				IRSESystemType[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
				for (int i = 0; i < systemTypes.length; i++) {
					IRSESystemType systemType = systemTypes[i];
					RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemType.getAdapter(IRSESystemType.class));
					if (systemTypeMatcher.matches(systemType)
							|| (adapter != null
									&& adapter.acceptWizardDescriptor(getConfigurationElement().getName(), this))) {
						if (!resolvedSystemTypes.contains(systemType.getId())) {
								resolvedSystemTypes.add(systemType.getId());
						}
					}
				}
			}
		}

		return (String[])resolvedSystemTypes.toArray(new String[resolvedSystemTypes.size()]);
	}

}
