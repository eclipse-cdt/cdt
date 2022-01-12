/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface to be used by extension point:
 * org.eclipse.cdt.managedbuilder.ui.CDTWizard
 *
 * Implementors should provide 1 or more
 * items in "Project types" list (left pane on
 * the 1st page in any CDT new project wizard)
 */
public abstract class CNewWizard {
	/**
	 * Creates tree items to be displayed in left pane.
	 *
	 * Method should add 1 or more tree items,
	 * each of them should have data object attached,
	 * data should be lt;ICProjectTypeHandler&gt;
	 *
	 * @param supportedOnly - whether display supported types only
	 * @param wizard - New Project wizard to be passed to ICWizardHandler
	 */
	public abstract EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard);

	/**
	 * Implementor will be informed about widget where additional
	 * data should be displayed. Normally, it is right pane in the
	 * 1st Wizard page.
	 *
	 * @param parent - composite where widgets are to be created
	 * @param page   - reference to object which will be informed
	 *                 about changes (usually 1st page in Wizard)
	 *                 May be null if notification is not required
	 *                 or implementor does not really support it.
	 */
	public void setDependentControl(Composite parent, IWizardItemsListListener page) {
	}
}
