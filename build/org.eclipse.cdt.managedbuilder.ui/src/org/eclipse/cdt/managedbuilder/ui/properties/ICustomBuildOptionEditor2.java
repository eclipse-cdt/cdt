/*******************************************************************************
 * Copyright (c) 2020 Torbjörn Svensson, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Torbjörn Svensson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

/**
 * This interface can be implemented by clients to contribute custom build-option
 * editors to the CDT Build Settings page in the project Properties dialog.
 *
 * In addition to implementing this interface, the custom build-option editor class
 * must also extend the {@link org.eclipse.jface.preference.FieldEditor} class. The
 * custom build-option editor class should be contributed through the <fieldEditor>
 * element of the org.eclipse.cdt.managedbuilder.ui.buildDefinitionsUI extension-point,
 * and then referenced, by its ID, from the <option>/fieldEditorId attribute of the
 * org.eclipse.cdt.managedbuilder.core.buildDefinitions extension-point.
 *
 * @since 9.3
 */
public interface ICustomBuildOptionEditor2 extends ICustomBuildOptionEditor {
	/**
	 * Save the option value to the resource info
	 * Typical usage is:
	 * <code>ManagedBuildManager.setOption(resConfig, holder, option, theValue);</code>
	 *
	 * @param resConfig The resource configuration the option belongs to.
	 * @param holder The holder/parent of the option.
	 * @param option The option to set the value for.
	 * @return {@code true} if the save was successful. Returning {@code false}
	 * 			to fall back to generic save implementation.
	 */
	boolean save(IResourceInfo resConfig, IHoldsOptions holder, IOption option);
}
