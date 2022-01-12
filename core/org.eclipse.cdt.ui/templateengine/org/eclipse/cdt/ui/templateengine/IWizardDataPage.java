/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
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

import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * IWizardDataPage is a page which can participate in a custom project template
 * wizard sequence.
 *
 * @since 4.0
 */
public interface IWizardDataPage extends IWizardPage {
	/**
	 * @return a map of (key,value) pairs that should be added to the
	 * associated project template's value store.
	 */
	Map<String, String> getPageData();

	/**
	 * Set the page that follows this one. Implementations must ensure
	 * {@link IWizardPage#getNextPage()} returns the specified value
	 * @param next
	 */
	public void setNextPage(IWizardPage next);
}
