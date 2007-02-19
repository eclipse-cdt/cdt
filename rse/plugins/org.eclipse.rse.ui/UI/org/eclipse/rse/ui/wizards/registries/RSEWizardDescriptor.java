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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of the <code>IRSEWizardDescriptor</code> interfaces.
 */
public class RSEWizardDescriptor extends RSEWizardRegistryElement implements IRSEWizardDescriptor {

	private IWizard wizard;
	
	/**
	 * Constructor
	 * 
	 * @param wizardRegistry The parent wizard registry this element belongs to. Must be not <code>null</code>.
	 * @param element The configuration element which is declaring a wizard. Must be not <code>null</code>.
	 */
	public RSEWizardDescriptor(RSEAbstractWizardRegistry wizardRegistry, IConfigurationElement element) {
		super(wizardRegistry, element);

		// Try to instanciate the wizard.
		try {
			wizard = (IWizard)element.createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException e) {
			String message = "RSE new connection wizard failed creation (plugin: {0}, id: {1})."; //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { element.getContributor().getName(), element.getDeclaringExtension().getSimpleIdentifier()});
			RSECorePlugin.getDefault().getLogger().logError(message, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#isValid()
	 */
	public boolean isValid() {
		return super.isValid() && wizard != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#getWizard()
	 */
	public IWizard getWizard() {
		return wizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#canFinishEarly()
	 */
	public boolean canFinishEarly() {
		String canFinishEarly = getConfigurationElement().getAttribute("canFinishEarly"); //$NON-NLS-1$
		if (canFinishEarly != null) return Boolean.TRUE.equals(Boolean.valueOf(canFinishEarly));
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#getCategoryId()
	 */
	public String getCategoryId() {
		return getConfigurationElement().getAttribute("categoryId"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#getDescription()
	 */
	public String getDescription() {
		return getConfigurationElement().getAttribute("description"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#hasPages()
	 */
	public boolean hasPages() {
		String hasPages = getConfigurationElement().getAttribute("hasPages"); //$NON-NLS-1$
		if (hasPages != null) return Boolean.TRUE.equals(Boolean.valueOf(hasPages));
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.registries.IWizardDescriptor#getImage()
	 */
	public Image getImage() {
		return null;
	}
}
