/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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
 * Common wizard category descriptor used by the <code>RSEAbstractWizardRegistry</code>
 * to handle wizard categories contributed via a wizard extension point.
 */
public interface IRSEWizardCategory extends IRSEWizardRegistryElement {

	/**
	 * Returns the fully qualified parent category id or <code>null</code>
	 * if this wizard category is itself a root category.
	 * 
	 * @return The parent category id or <code>null</code>.
	 */
	public String getParentCategoryId();
}
