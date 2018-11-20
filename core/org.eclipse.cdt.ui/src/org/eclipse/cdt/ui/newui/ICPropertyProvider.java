/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Button;

/**
 * Interface provides a set of utility methods
 * provided by new CDT model property page.
 * Property tabs associated to this page receive
 * link to this interface and, so, can access
 * required data, such as project, config etc.
 *
 * In addition, some methods allow to send
 * control messages to other pages / tabs.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */

public interface ICPropertyProvider extends ICOptionContainer {

	// new list of config descriptions for given project
	ICConfigurationDescription[] getCfgsReadOnly(IProject p);

	// list of loaded config descriptions for current project
	ICConfigurationDescription[] getCfgsEditable();

	// Resource description for given object in current cfg
	ICResourceDescription getResDesc();

	// Resource description for given object, in given cfg
	ICResourceDescription getResDesc(ICConfigurationDescription cfgd);

	// get Affected object (project, folder, file)
	IAdaptable getElement();

	// ask page to enable or disable config selection
	void enableConfigSelection(boolean enable);

	//
	// set of methods intended to handle messages
	//
	// 1. send message to all tabs in all pages
	void informAll(int code, Object data);

	// 2. send message to all pages.
	void informPages(int code, Object data);

	// 3. send message only to current page
	void handleMessage(int code, Object data);

	//
	// set of methods for object kind check
	//
	boolean isForProject();

	boolean isForFolder();

	boolean isForFile();

	boolean isForPrefs();

	// Checks whether a project is new CDT model-style
	boolean isCDTProject(IProject p);

	boolean isMultiCfg();

	// Gives access to buttons
	Button getAButton();

	Button getDButton();

}
