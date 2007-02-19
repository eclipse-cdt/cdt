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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardCategory;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardRegistryElement;
import org.eclipse.rse.ui.wizards.registries.RSEAbstractWizardSelectionTreeDataManager;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeElement;

/**
 * New connection wizard selection tree data manager.
 */
public class RSENewConnectionWizardSelectionTreeDataManager extends RSEAbstractWizardSelectionTreeDataManager {
	
	/**
	 * Constructor.
	 */
	public RSENewConnectionWizardSelectionTreeDataManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.internal.wizards.newconnection.RSEAbstractWizardSelectionTreeDataManager#initialize(java.util.Set)
	 */
	protected void initialize(Set rootElement) {
		Map categoryCache = new HashMap();
		
		// The new connection wizard selection is combining system types
		// with registered new connection wizard.
		IRSESystemType[] systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypes();
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			// for the system type, lookup the corresponding wizard descriptor
			IRSENewConnectionWizardDescriptor descriptor = RSENewConnectionWizardRegistry.getInstance().getWizardForSystemType(systemType);
			if (descriptor == null) {
				// a system type without even the default RSE new connection wizard associated
				// is bad and should never happen. Drop a warning and skip the system type.
				String message = "System type " + systemType.getId() + " has no new connection wizard associated!"; //$NON-NLS-1$ //$NON-NLS-2$
				RSEUIPlugin.getDefault().getLogger().logWarning(message);
				continue;
			}
			
			// ok, we have wizard for the current system type. Create the wizard selection tree element
			// and categorise the wizard.
			RSENewConnectionWizardSelectionTreeElement wizardElement = new RSENewConnectionWizardSelectionTreeElement(systemType, descriptor);
			wizardElement.setParentElement(null);
			String categoryId = descriptor.getCategoryId();
			// if the wizard is of type IRSEDynamicNewConnectionWizard, call validateCategoryId!
			if (descriptor.getWizard() instanceof IRSEDynamicNewConnectionWizard) {
				categoryId = ((IRSEDynamicNewConnectionWizard)descriptor.getWizard()).validateCategoryId(systemType, categoryId);
			}
			
			// if the category id is null, the wizard will be sorted in as root element
			if (categoryId == null) {
				rootElement.add(wizardElement);
				continue;
			}

			// get the category. If failing, the wizard will end up as root element
			IRSEWizardRegistryElement candidate = RSENewConnectionWizardRegistry.getInstance().findElementById(categoryId);
			if (!(candidate instanceof IRSEWizardCategory)) {
				rootElement.add(wizardElement);
				continue;
			}
			
			IRSEWizardCategory category = (IRSEWizardCategory)candidate;
			
			// if the category id is not null, check if we have accessed the category
			// already once.
			RSEWizardSelectionTreeElement categoryElement = (RSEWizardSelectionTreeElement)categoryCache.get(category);
			if (categoryElement == null) {
				categoryElement = new RSEWizardSelectionTreeElement(category);
				categoryElement.setParentElement(null);
				categoryCache.put(category, categoryElement);
			}
			categoryElement.add(wizardElement);
			wizardElement.setParentElement(categoryElement);
			
			// The category itself does not have a parent category, the category is a root element
			String parentCategoryId = category.getParentCategoryId();
			if (parentCategoryId == null) {
				rootElement.add(categoryElement);
				continue;
			}
			
			while (parentCategoryId != null) {
				candidate = RSENewConnectionWizardRegistry.getInstance().findElementById(parentCategoryId);
				if (!(candidate instanceof IRSEWizardCategory)) {
					rootElement.add(categoryElement);
					break;
				}
				
				category = (IRSEWizardCategory)candidate;
				
				RSEWizardSelectionTreeElement parentElement = (RSEWizardSelectionTreeElement)categoryCache.get(category);
				if (parentElement == null) {
					parentElement = new RSEWizardSelectionTreeElement(category);
					parentElement.setParentElement(null);
					categoryCache.put(category, parentElement);
				}
				parentElement.add(categoryElement);
				categoryElement.setParentElement(parentElement);
				
				categoryElement = parentElement;
				parentCategoryId = category.getParentCategoryId();
			}
		}
	}
}
