/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.registries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Data element for RSE wizard selection tree's.
 */
public class RSEWizardSelectionTreeElement {
	private final List children = new ArrayList();
	private final IRSEWizardRegistryElement wizardRegistryElement;
	private RSEWizardSelectionTreeElement parent;

	/**
	 * Constructor.
	 *
	 * @param element The wizard registry element to associate. Must be not <code>null</code>.
	 */
	public RSEWizardSelectionTreeElement(IRSEWizardRegistryElement element) {
		assert element != null;
		wizardRegistryElement = element;
    children.clear();
	}

	/**
	 * Returns the associated wizard registry element
	 *
	 * @return The wizard registry element. Must be never <code>null</code>.
	 */
	public IRSEWizardRegistryElement getWizardRegistryElement() {
		return wizardRegistryElement;
	}

	/**
	 * Adds a new child to this RSE wizard selection tree element. If
	 * the child had been added already before, the method will do nothing.
	 *
	 * @param child The child to add. Must be not <code>null</code>.
	 */
	public void add(RSEWizardSelectionTreeElement child) {
		assert child != null;
		if (!children.contains(child)) {
			children.add(child);
		}
	}

	/**
	 * Removes the given child from the list of children. If the child
	 * has not been added before, the method will do nothing.
	 *
	 * @param child The child to remove. Must be not <code>null</code>.
	 */
	public void remove(RSEWizardSelectionTreeElement child) {
		assert child != null;
		children.remove(child);
	}

	/**
	 * Returns the children of this wizard selection tree element.
	 *
	 * @return The list of children, May be empty but never <code>null</code>.
	 */
	public RSEWizardSelectionTreeElement[] getChildren() {
		return (RSEWizardSelectionTreeElement[])children.toArray(new RSEWizardSelectionTreeElement[children.size()]);
	}

	/**
	 * Associate a parent element to this wizard selection tree element.
	 *
	 * @param parent The parent element to associate or <code>null</code>.
	 */
	public void setParentElement(RSEWizardSelectionTreeElement parent) {
		this.parent = parent;
	}

	/**
	 * Returns the associated parent element of this wizard selection tree element.
	 *
	 * @return The parent element or <code>null</code>.
	 */
	public RSEWizardSelectionTreeElement getParentElement() {
		return this.parent;
	}

	/**
	 * Returns the description to show in the wizards message area.
	 *
	 * @return The description to show in the wizards message are or <code>null</code>.
	 */
	public String getDescription() {
		if (getWizardRegistryElement() instanceof IRSEWizardDescriptor) {
			return ((IRSEWizardDescriptor)getWizardRegistryElement()).getDescription();
		}
		return null;
	}

	/**
	 * Returns the label to show in the tree.
	 *
	 * @return The label to use for the tree node or <code>null</code>.
	 */
	public String getLabel() {
		return getWizardRegistryElement().getName();
	}

	/**
	 * Returns the image to show in the tree.
	 *
	 * @return The image to use for the tree node or <code>null</code>.
	 */
	public Image getImage() {
		if (getWizardRegistryElement() instanceof IRSEWizardDescriptor) {
			return ((IRSEWizardDescriptor)getWizardRegistryElement()).getImage();
		} else if (getWizardRegistryElement() instanceof IRSEWizardCategory) {
			ImageRegistry imageRegistry = RSEUIPlugin.getDefault().getImageRegistry();
			String key = "category::" + getWizardRegistryElement().getId(); //$NON-NLS-1$
			Image image = imageRegistry.get(key);
			if (image == null) {
				//ImageDescriptor descriptor = RSEUIPlugin.getDefault().getImageDescriptorFromIDE("obj16/fldr_obj.gif"); //$NON-NLS-1$
				ImageDescriptor descriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
				if (descriptor != null) {
					image = descriptor.createImage();
					imageRegistry.put(key, image);
				}
			}
			return image;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RSEWizardSelectionTreeElement) {
			return getWizardRegistryElement().equals(((RSEWizardSelectionTreeElement)obj).getWizardRegistryElement());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getWizardRegistryElement().hashCode();
	}
}
