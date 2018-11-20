/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Implementation of standard behaviours intended to be subclassed by clients.
 * @since 4.0.2
 */
public abstract class AbstractWizardDataPage extends WizardPage implements IWizardDataPage {
	protected IWizardPage next;

	/**
	* Creates a new wizard page with the given name, and
	* with no title or image.
	*
	* @param pageName the name of the page
	*/
	public AbstractWizardDataPage(String pageName) {
		super(pageName);
	}

	/**
	 * Creates a new wizard page with the given name, title, and image.
	 *
	 * @param pageName the name of the page
	 * @param title the title for this wizard page,
	 *   or <code>null</code> if none
	 * @param imageDescriptor the image descriptor for the title of this wizard page,
	 *   or <code>null</code> if none
	 */
	public AbstractWizardDataPage(String pageName, String title, ImageDescriptor imageDescriptor) {
		super(pageName, title, imageDescriptor);
	}

	@Override
	public void setNextPage(IWizardPage next) {
		this.next = next;
	}

	@Override
	public IWizardPage getNextPage() {
		if (next != null) {
			return next;
		}
		return super.getNextPage();
	}
}
