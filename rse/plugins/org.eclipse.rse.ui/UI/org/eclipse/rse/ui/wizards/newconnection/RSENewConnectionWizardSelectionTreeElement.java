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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.registries.IRSEWizardRegistryElement;
import org.eclipse.rse.ui.wizards.registries.RSEWizardSelectionTreeElement;
import org.eclipse.swt.graphics.Image;

/**
 * New connection wizard selection tree data element.
 */
public class RSENewConnectionWizardSelectionTreeElement extends RSEWizardSelectionTreeElement {
	private static final long serialVersionUID = -6061418626602868827L;

	private final IRSESystemType systemType;
	
	/**
	 * Constructor.
	 * 
	 * @param element The wizard registry element to associate. Must be not <code>null</code>.
	 */
	public RSENewConnectionWizardSelectionTreeElement(IRSESystemType systemType, IRSEWizardRegistryElement element) {
		super(element);
		assert systemType != null;
		this.systemType = systemType;
	}

	/**
	 * Returns the associated system type instance.
	 * 
	 * @return The associated system type instance. Must be never <code>null</code>.
	 */
	public IRSESystemType getSystemType() {
		return systemType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.internal.wizards.newconnection.RSEWizardSelectionTreeElement#getLabel()
	 */
	public String getLabel() {
		return getSystemType().getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.internal.wizards.newconnection.RSEWizardSelectionTreeElement#getImage()
	 */
	public Image getImage() {
		ImageRegistry imageRegistry = RSEUIPlugin.getDefault().getImageRegistry();
		String key = getSystemType().getId() + "::" + getWizardRegistryElement().getId(); //$NON-NLS-1$
		Image image = imageRegistry.get(key);
		if (image == null) {
			RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(getSystemType().getAdapter(IRSESystemType.class));
			if (adapter != null) {
				ImageDescriptor descriptor = adapter.getImageDescriptor(getSystemType());
				image = descriptor.createImage();
				imageRegistry.put(key, image);
			}
		}
		
		return image;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RSENewConnectionWizardSelectionTreeElement) {
			return getSystemType().equals(((RSENewConnectionWizardSelectionTreeElement)obj).getSystemType())
							&& getWizardRegistryElement().equals(((RSENewConnectionWizardSelectionTreeElement)obj).getWizardRegistryElement());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + getSystemType().hashCode();
	}
	
}
