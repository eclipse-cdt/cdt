/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Interface for extensions that provide additional custom pages as part of
 * project configuration.
 * @since 4.0
 */
public interface IPagesAfterTemplateSelectionProvider {
	/**
	 * Creates pages that will be appended to the pages returned from
	 * TemplatesChoiceWizard.getPagesAfterTemplateSelection()
	 * Parameters are those used to initialise the wizard.
	 * </p>
	 * @param wizard the wizard requesting the pages
	 * @param workbench the current workbench
	 * @param selection the current object selection, or null if no context is available
	 *
	 * @since 4.0
	 */
	IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
			IStructuredSelection selection);

	/**
	 * Gets the previously created pages
	 * @param wizard the wizard that requested creation of the pages
	 *
	 * @since 4.0
	 */
	IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard);
}
