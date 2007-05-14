/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.newconnection;

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

	// The list of resolved system type ids supported by this wizard.
	private List resolvedSystemTypeIds;
	
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
		if (resolvedSystemTypeIds == null) {
			resolvedSystemTypeIds = new LinkedList();
			
			// If the subsystem configuration supports all system types, just add all
			// currently registered system types to th resolved list
			if (systemTypeMatcher.supportsAllSystemTypes()) {
				IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
				for (int i = 0; i < systemTypes.length; i++) resolvedSystemTypeIds.add(systemTypes[i].getId());
			} else {
				// We have to match the given lists of system type ids against
				// the list of available system types. As the list of system types cannot
				// change ones it has been initialized, we filter out the not matching ones
				// here directly.
				IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
				for (int i = 0; i < systemTypes.length; i++) {
					IRSESystemType systemType = systemTypes[i];
					RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(systemType.getAdapter(RSESystemTypeAdapter.class));
					if (systemTypeMatcher.matches(systemType)
							|| (adapter != null
									&& adapter.acceptWizardDescriptor(getConfigurationElement().getName(), this))) {
						if (!resolvedSystemTypeIds.contains(systemType.getId())) {
								resolvedSystemTypeIds.add(systemType.getId());
						}
					}
				}
			}
		}

		return (String[])resolvedSystemTypeIds.toArray(new String[resolvedSystemTypeIds.size()]);
	}

}
