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
 * Default implementation of the <code>IRSEWizardRegistryElement</code> interface
 */
public class RSEWizardRegistryElement implements IRSEWizardRegistryElement {
	protected final static IRSEWizardRegistryElement[] NO_ELEMENTS = new IRSEWizardRegistryElement[0];
	
	private final IConfigurationElement element;
	private final RSEAbstractWizardRegistry wizardRegistry;
	
	private String id;
	private String name;

	/**
	 * Constructor.
	 * 
	 * @param wizardRegistry The parent wizard registry this element belongs to. Must be not <code>null</code>.
	 * @param element The configuration element which is declaring a wizard category. Must be not <code>null</code>.
	 */
	public RSEWizardRegistryElement(RSEAbstractWizardRegistry wizardRegistry, IConfigurationElement element) {
		assert wizardRegistry != null && element != null;
		
		// Store the wizard registry reference
		this.wizardRegistry = wizardRegistry;
		
		// Store the configuration element reference
		this.element = element;
		
		// Read the required attributes from the configuration element and
		// check that these attributes are really set.
		id = element.getAttribute("id"); //$NON-NLS-1$
		name = element.getAttribute("name"); //$NON-NLS-1$
	}

	/**
	 * Returns the parent wizard registry of this element.
	 * 
	 * @return The parent wizard registry. Must be never <code>null</code>.
	 */
	protected final RSEAbstractWizardRegistry getWizardRegistry() {
		return wizardRegistry;
	}
	
	/**
	 * Returns the associated configuration element.
	 * 
	 * @return The configration element. Must be never <code>null</code>.
	 */
	protected final IConfigurationElement getConfigurationElement() {
		assert element != null;
		return element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardCategory#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardCategory#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardCategory#isValid()
	 */
	public boolean isValid() {
		return id != null && name != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IRSEWizardRegistryElement#getChildren()
	 */
	public IRSEWizardRegistryElement[] getChildren() {
		return NO_ELEMENTS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IRSEWizardRegistryElement#getParent()
	 */
	public IRSEWizardRegistryElement getParent() {
		return null;
	}
}
