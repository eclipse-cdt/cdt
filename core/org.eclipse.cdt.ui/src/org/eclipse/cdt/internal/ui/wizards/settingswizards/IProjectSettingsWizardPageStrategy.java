/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

/**
 * The wizard page is very similar for importing and exporting,
 * the wizard page delegates to a strategy object which defines
 * the behavior that is specific to import and export.
 *
 * @author Mike Kucera
 * @since 5.1
 */
public interface IProjectSettingsWizardPageStrategy {

	enum MessageType {
		TITLE, MESSAGE, SETTINGS, CHECKBOX, FILE
	}

	/**
	 * Some of the strings displayed on the wizard page are
	 * different, this method returns the correct string
	 * to display depending on the strategy.
	 */
	String getMessage(MessageType messageType);

	/**
	 * Event sent to strategy object when the
	 * page has been created (at the end of createControl())
	 */
	void pageCreated(IProjectSettingsWizardPage page);

	/**
	 * Event sent to strategy object when the user selects
	 * a file name.
	 */
	void fileSelected(IProjectSettingsWizardPage page);

	/**
	 * Event sent to strategy object when the user clicks
	 * finish on the wizard page.
	 */
	boolean finish(IProjectSettingsWizardPage page);

}
