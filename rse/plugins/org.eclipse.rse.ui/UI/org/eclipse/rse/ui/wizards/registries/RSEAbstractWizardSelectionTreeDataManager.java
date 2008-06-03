/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Martin Oberhuber (Wind River) - [235197][api] Unusable wizard after cancelling on first page
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.registries;

import java.util.HashSet;
import java.util.Set;


/**
 * Data manager for RSE wizard selection tree's.
 */
public abstract class RSEAbstractWizardSelectionTreeDataManager {
	private final Set rootElement = new HashSet();
	private RSEAbstractWizardRegistry wizardRegistry;

	/**
	 * Constructor.
     * @since org.eclipse.rse.ui 3.0
	 */
	public RSEAbstractWizardSelectionTreeDataManager(RSEAbstractWizardRegistry wizardRegistry) {
		this.wizardRegistry = wizardRegistry;
		rootElement.clear();

		// start the initialization of the data tree.
		initialize(rootElement);
	}

	/**
	 * Constructor.
	 * 
	 * @deprecated Use
	 *             {@link #RSEAbstractWizardSelectionTreeDataManager(RSEAbstractWizardRegistry)}
	 *             to control the lifetime of the wizard registry
	 */
	public RSEAbstractWizardSelectionTreeDataManager() {
		this(null);
	}

	/**
	 * Returns the currently active wizard registry, which allows looking up
	 * wizard instances by various search keys. The wizard registry is valid as
	 * long as a particular wizard is open.
	 *
	 * @return the current wizard registry
	 * @since org.eclipse.rse.ui 3.0
	 */
	protected RSEAbstractWizardRegistry getWizardRegistry() {
		return wizardRegistry;
	}

	/**
	 * Returns the children of this wizard selection tree element.
	 *
	 * @return The list of children, May be empty but never <code>null</code>.
	 */
	public RSEWizardSelectionTreeElement[] getChildren() {
		return (RSEWizardSelectionTreeElement[])rootElement.toArray(new RSEWizardSelectionTreeElement[rootElement.size()]);
	}

	/**
	 * Initialize the data tree.
	 *
	 * @param rootElement The root element which is the container for all user visible tree root elements. Must be not <code>null</code>.
	 */
	protected abstract void initialize(Set rootElement);
}
