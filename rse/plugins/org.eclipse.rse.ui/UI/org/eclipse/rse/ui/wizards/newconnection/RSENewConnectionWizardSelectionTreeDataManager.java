/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [235197][api] Unusable wizard after cancelling on first page
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
	// The element map is required to translate from IRSESystemType object instance
	// into RSENewConnectionWizardSelectionTreeElement object instances as the tree
	// and the wizard using these different object instances in their selections!
	private Map elementMap;

	// The category map is doing the same as the element but for categories.
	private Map categoryMap;

	/**
	 * Constructor.
     * @since org.eclipse.rse.ui 3.0
	 */
	public RSENewConnectionWizardSelectionTreeDataManager(RSENewConnectionWizardRegistry wizardRegistry) {
		super(wizardRegistry);
	}

	/**
	 * Constructor.
	 *
	 * @deprecated Use
	 *             {@link #RSENewConnectionWizardSelectionTreeDataManager(RSENewConnectionWizardRegistry)}
	 *             to control the lifetime of the wizard registry
	 */
	public RSENewConnectionWizardSelectionTreeDataManager() {
		this(RSENewConnectionWizardRegistry.getInstance());
	}

	/**
	 * Returns the corresponding wizard selection tree element for the specified
	 * system type.
	 *
	 * @param systemType The system type. Must be not <code>null</code>.
	 * @return The wizard selection tree element or <code>null</code>.
	 */
	public RSENewConnectionWizardSelectionTreeElement getTreeElementForSystemType(IRSESystemType systemType) {
		assert systemType != null;
		return (RSENewConnectionWizardSelectionTreeElement)elementMap.get(systemType);
	}

	/**
	 * Returns the corresponding wizard selection tree element for the specified category.
	 *
	 * @param category The category. Must be not <code>null</code>.
	 * @return The wizard selection tree element or <code>null</code>.
	 */
	public RSEWizardSelectionTreeElement getTreeElementForCategory(IRSEWizardCategory category) {
		assert category != null;
		return (RSEWizardSelectionTreeElement)categoryMap.get(category);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.internal.wizards.newconnection.RSEAbstractWizardSelectionTreeDataManager#initialize(java.util.Set)
	 */
	protected void initialize(Set rootElement) {
		// we must check the elementMap here for null as the static
		// constructors may not have called yet as this method is called
		// from the base classes constructor!
		if (elementMap == null) elementMap = new HashMap();
		elementMap.clear();

		if (categoryMap == null) categoryMap = new HashMap();
		categoryMap.clear();

		// The new connection wizard selection is combining system types
		// with registered new connection wizard.
		IRSESystemType[] systemTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			// for the system type, lookup the corresponding wizard descriptor
			IRSENewConnectionWizardDescriptor descriptor = ((RSENewConnectionWizardRegistry)getWizardRegistry()).getWizardForSystemType(systemType);
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
			elementMap.put(systemType, wizardElement);

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
			IRSEWizardRegistryElement candidate = getWizardRegistry().findElementById(categoryId);
			if (!(candidate instanceof IRSEWizardCategory)) {
				rootElement.add(wizardElement);
				continue;
			}

			IRSEWizardCategory category = (IRSEWizardCategory)candidate;

			// if the category id is not null, check if we have accessed the category
			// already once.
			RSEWizardSelectionTreeElement categoryElement = (RSEWizardSelectionTreeElement)categoryMap.get(category);
			if (categoryElement == null) {
				categoryElement = new RSEWizardSelectionTreeElement(category);
				categoryElement.setParentElement(null);
				categoryMap.put(category, categoryElement);
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
				candidate = getWizardRegistry().findElementById(parentCategoryId);
				if (!(candidate instanceof IRSEWizardCategory)) {
					rootElement.add(categoryElement);
					break;
				}

				category = (IRSEWizardCategory)candidate;

				RSEWizardSelectionTreeElement parentElement = (RSEWizardSelectionTreeElement)categoryMap.get(category);
				if (parentElement == null) {
					parentElement = new RSEWizardSelectionTreeElement(category);
					parentElement.setParentElement(null);
					categoryMap.put(category, parentElement);
				}
				parentElement.add(categoryElement);
				categoryElement.setParentElement(parentElement);

				categoryElement = parentElement;
				parentCategoryId = category.getParentCategoryId();
			}
		}
	}
}
