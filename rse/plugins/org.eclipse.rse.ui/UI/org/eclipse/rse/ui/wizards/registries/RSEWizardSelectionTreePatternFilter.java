/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation.
 * Uwe Stieber (Wind River) - [209193] RSE new connection wizard shows empty categories if typing something into the filter
 *******************************************************************************/
package org.eclipse.rse.ui.wizards.registries;

import java.util.Arrays;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.ui.wizards.newconnection.RSENewConnectionWizardSelectionPage;
import org.eclipse.rse.ui.wizards.newconnection.RSENewConnectionWizardSelectionTreeElement;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Standard RSE wizard selection tree pattern filter.
 */
public class RSEWizardSelectionTreePatternFilter extends PatternFilter {
	private final WizardPage parentPage;

	/**
	 * Constructor.<br>
	 * Creates a new pattern filter instance which is not associated
	 * with a parent wizard page.
	 */
	public RSEWizardSelectionTreePatternFilter() {
		this(null);
	}

	/**
	 * Constructor.<br>
	 * Creates a new pattern filter instance with the passed in wizard page
	 * associated as parent.
	 *
	 * @param page The parent wizard page or <code>null</code>.
	 * @since 3.0
	 */
	public RSEWizardSelectionTreePatternFilter(WizardPage page) {
		parentPage = page;
	}

	
	/**
	 * Returns the associated parent wizard parent.
	 *
	 * @return The parent wizard page or <code>null</code> if none.
	 * @since 3.0
	 */
	protected WizardPage getParentWizardPage() {
		return parentPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isElementVisible(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	public boolean isElementVisible(Viewer viewer, Object element) {
		// If the element is a new connection wizard selection tree element,
		// we have to check if the associated system type is enabled and/or
		// if the system type itself may filter it out from the visible elements.
		if (element instanceof RSENewConnectionWizardSelectionTreeElement) {
			// A system type must be associated with such tree element, otherwise it is filtered out
			IRSESystemType systemType = ((RSENewConnectionWizardSelectionTreeElement)element).getSystemType();
			if (systemType == null) return false;

			// if the page is restricted to a set of system types, check on them first
			WizardPage wizardPage = getParentWizardPage();
			if (wizardPage instanceof RSENewConnectionWizardSelectionPage) {
				IRSESystemType[] restricted = ((RSENewConnectionWizardSelectionPage)wizardPage).getRestrictToSystemTypes();
				if (restricted != null && restricted.length > 0) {
					if (!Arrays.asList(restricted).contains(systemType)) return false;
				}
			}

			// First, adapt the system type to a viewer filter and pass on the select request
			// to the viewer filter adapter if available
			ViewerFilter filter = (ViewerFilter)(systemType.getAdapter(ViewerFilter.class));
			// We don't know what the parent of the passed in element is.
			// So, we can pass on only null here.
			if (filter != null && !filter.select(viewer, null, element)) return false;

			// Second, double check if the system type passed the viewer filter but is disabled.
			if (!systemType.isEnabled()) return false;
		}

		return super.isElementVisible(viewer, element);
	}

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
			// we filter only the wizard nodes
			if (treeElement.getWizardRegistryElement() instanceof IRSEWizardDescriptor) {
				return wordMatches(treeElement.getLabel());
			}

		}

		return super.isLeafMatch(viewer, element);
	}

}
