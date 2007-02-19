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
package org.eclipse.rse.ui.wizards.registries;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Default implementation of the <code>IRSEWizardCategory</code> interface.
 */
public class RSEWizardCategory extends RSEWizardRegistryElement  implements IRSEWizardCategory {
	
	/**
	 * Constructor
	 * 
	 * @param wizardRegistry The parent wizard registry this element belongs to. Must be not <code>null</code>.
	 * @param element The configuration element which is declaring a wizard category. Must be not <code>null</code>.
	 */
	public RSEWizardCategory(RSEAbstractWizardRegistry wizardRegistry, IConfigurationElement element) {
		super(wizardRegistry, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardCategory#getParentCategoryId()
	 */
	public String getParentCategoryId() {
		return getConfigurationElement().getAttribute("parentCategoryId"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.RSEWizardRegistryElement#getParent()
	 */
	public IRSEWizardRegistryElement getParent() {
		String parentCategoryId = getParentCategoryId(); 
		if (parentCategoryId != null && !"".equals(parentCategoryId.trim())) { //$NON-NLS-1$
			// Try to get the corresponding wizard category element for this id
			return getWizardRegistry().findElementById(parentCategoryId);
		}
		return super.getParent();
	}
	
}
