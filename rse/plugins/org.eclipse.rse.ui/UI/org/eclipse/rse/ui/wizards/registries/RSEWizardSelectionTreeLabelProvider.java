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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Standard RSE wizard selection tree label provider.
 */
public class RSEWizardSelectionTreeLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		Image image = null;
		
		if (element instanceof RSEWizardSelectionTreeElement) {
			image = ((RSEWizardSelectionTreeElement)element).getImage();
		}
		
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String text = super.getText(element);
		
		if (element instanceof RSEWizardSelectionTreeElement) {
			text = ((RSEWizardSelectionTreeElement)element).getLabel();
		}
		
		return text;
	}
}
