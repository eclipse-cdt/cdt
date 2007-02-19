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

/**
 * Basic interface of elements which can be handled by
 * the <code>RSEAbstractWizardRegistry</code>.
 */
public interface IRSEWizardRegistryElement {

	/**
	 * Returns the full qualified unique id of the wizard registry element.
	 * 
	 * @return The unique wizard registry element id. Must be never <code>null</code>.
	 */
	public String getId();
	
	/**
	 * Returns the UI name of the wizard registry element.
	 * 
	 * @return The UI name of the wizard registry element. Must be never <code>null</code>.
	 */
	public String getName();
	
	/**
	 * Validates the wizard registry element. This method should return <code>false</code>
	 * if any required wizard registry element attribute is missing. Subclasses should
	 * override this method to validate additional required attribtes.
	 * 
	 * @return <code>True</code> if the wizard registry element is valid, <code>false</code> otherwise.
	 */
	public boolean isValid();
	
	/**
	 * Returns the parent wizard registry element if any or <code>null</code>
	 * if this element is a root element.
	 * 
	 * @return The parent wizard registry element or <code>null</code>.
	 */
	public IRSEWizardRegistryElement getParent();
	
	/**
	 * Returns the list of children if this element or an empty list if
	 * the element does not have children.
	 * 
	 * @return The list of children wizard registry elements. May be empty but never <code>null</code>.
	 */	
	public IRSEWizardRegistryElement[] getChildren();
}
