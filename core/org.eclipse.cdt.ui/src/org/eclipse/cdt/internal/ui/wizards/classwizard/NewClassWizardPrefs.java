/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

public class NewClassWizardPrefs {

	/**
	 * Checks if the base classes need to be verified (ie they must exist in the project)
	 *
	 * @return <code>true</code> if the base classes should be verified
	 */
	public static boolean verifyBaseClasses() {
		//TODO this should be a prefs option
		return true;
	}

	/**
	 * Checks if include paths can be added to the project as needed.
	 *
	 * @return <code>true</code> if the include paths should be added
	 */
	public static boolean createIncludePaths() {
		//TODO this should be a prefs option
		return true;
	}

	/**
	 * Returns whether the generated header and source files should be
	 * opened in editors after the finish button is pressed.
	 *
	 * @return <code>true</code> if the header and source file should be
	 * displayed
	 */
	public static boolean openClassInEditor() {
		//TODO this should be a prefs option
		return true;
	}

}
