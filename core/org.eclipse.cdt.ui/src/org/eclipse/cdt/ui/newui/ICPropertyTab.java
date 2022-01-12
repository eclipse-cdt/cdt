/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
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
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface for tabs in new CDT model.
 * All tabs available via extension point
 * "org.eclipse.cdt.ui.cPropertyTab"
 * should implement this interface.
 */
public interface ICPropertyTab {

	// kinds of message to be sent (and appropriate data class)
	public static final int OK = 0; // perform OK (null)
	public static final int APPLY = 1; // apply changes (IResourceDescription)
	public static final int CANCEL = 2; // cancel changes (null)
	public static final int DEFAULTS = 3; // set defaults (null)
	public static final int UPDATE = 4; // re-read cfg (IConfiguration)
	public static final int VISIBLE = 5; // set visible (not-null means true)
	public static final int DISPOSE = 6; // dispose (null)
	public static final int SET_ICON = 7; // inform tab about its icon (Image)

	public static final int MAXCOMMON = 100; // values below are common
												// values above are private
												// Informs other tabs about changes in managed build settings.
												// It may result in hiding/showing some tabs or changing their
												// contents. Data field is not used (null).
	public static final int MANAGEDBUILDSTATE = MAXCOMMON + 1;

	/**
	 * Creation of all visible elements
	 * @param parent   - composite where widgets should be created
	 * @param provider - underlying page
	 */
	public void createControls(Composite parent, ICPropertyProvider provider);

	/**
	 * Handle events sent by another tabs or pages
	 * Most of them are processed in <link>AbstractCPropertyTab</link>
	 * but this functionality can be overridden partially or fully.
	 * @param kind - message ID (see <link>AbstractCPropertyTab</link>)
	 * @param data - additional info, depanding of message kind.
	 */
	public void handleTabEvent(int kind, Object data);

	/**
	 * Returns true (by default) if page's contents is correct
	 * Returns false if page cannot be shown because it does
	 * not fit to other settings (for example, managed build
	 * settings are not possible when managed build is off).
	 */
	public boolean canBeVisible();

	//*********************************
	// TODO: in next version, add :
	//*********************************
	/**
	 * @return Help Context Id
	 */
	// public String getHelpContextId();
	/**
	 * set Help Context Id for the tab
	 */
	// public void setHelpContextId(String id);
}
