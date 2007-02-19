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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Standard RSE wizard selection tree pattern filter.
 */
public class RSEWizardSelectionTreePatternFilter extends PatternFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		if (element instanceof RSEWizardSelectionTreeElement) {
			return !(((RSEWizardSelectionTreeElement)element).getWizardRegistryElement() instanceof IRSEWizardCategory);
		}
		return super.isElementSelectable(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isLeafMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		if (element instanceof RSEWizardSelectionTreeElement) {
			RSEWizardSelectionTreeElement treeElement = (RSEWizardSelectionTreeElement)element;
			// we filter only the wizard nodes, not the category nodes (yet).
			if (treeElement.getWizardRegistryElement() instanceof IRSEWizardCategory) {
				return true;
			} else {
				return wordMatches(treeElement.getLabel());
			}

		}
		
		return super.isLeafMatch(viewer, element);
	}
	
}
