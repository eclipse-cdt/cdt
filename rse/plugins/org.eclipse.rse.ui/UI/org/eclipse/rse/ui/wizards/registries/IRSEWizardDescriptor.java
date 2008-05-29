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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;

/**
 * Common wizard descriptor used by the <code>RSEAbstractWizardRegistry</code>
 * to handle wizards contributed via a wizard extension point.
 */
public interface IRSEWizardDescriptor extends IRSEWizardRegistryElement {

	/**
	 * The wizard implementation object instance.
	 * 
	 * @return The wizard instance. Must be never <code>null</code>.
	 */
	public IWizard getWizard();
	
	/**
	 * Returns a optional short description of the wizards purpose.
	 * 
	 * @return The short wizard description or <code>null</code>.
	 */
	public String getDescription();
	
	/**
	 * Returns the fully qualified category id if the wizard belongs
	 * to a category. If empty or <code>null</code> or the returned 
	 * category id does not exist, the wizard will be sorted in as
	 * root element.
	 * 
	 * @return The category id or <code>null</code>.
	 */
	public String getCategoryId();
	
	/**
	 * Returns if the wizard can be finished without ever showing
	 * a specific page to the user.
	 * 
	 * @return <code>True</code> if the wizard can finish without any page presentation, <code>false</code> otherwise.
	 */
	public boolean canFinishEarly();
	
	/**
	 * Returns if the wizard has pages to show to the user.
	 * 
	 * @return <code>True</code> if the wizard has presentable pages, <code>false</code> otherwise.
	 */	
	public boolean hasPages();
	
	/**
	 * Returns a optional image for representing the wizard within the UI besides
	 * the wizard name. The default wizard descriptor implementation returns always
	 * <code>null</code>.
	 * 
	 * @return The wizard image or <code>null</code> if none.
	 */
	public Image getImage();
}
