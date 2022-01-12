/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.doxygen;

import java.util.List;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * The metadata for options to configure doxygen
 *
 */
public interface DoxygenMetadata {

	/**
	 * Returns the metadata for the "Use brief tag" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Use brief tag" option
	 *
	 * @see DoxygenOptions#useBriefTags()
	 */
	PreferenceMetadata<Boolean> useBriefTagOption();

	/**
	 * Returns the metadata for the "Use structural commands" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Use structural commands" option
	 *
	 * @see DoxygenOptions#useStructuralCommands()
	 */
	PreferenceMetadata<Boolean> useStructuralCommandsOption();

	/**
	 * Returns the metadata for the "Use javadoc style for tags" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Use javadoc style for tags" option
	 *
	 * @see DoxygenOptions#useJavadocStyle()
	 */
	PreferenceMetadata<Boolean> useJavadocStyleOption();

	/**
	 * Returns the metadata for the "Add new line after brief tag" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Add new line after brief tag" option
	 *
	 * @see DoxygenOptions#newLineAfterBrief()
	 */
	PreferenceMetadata<Boolean> newLineAfterBriefOption();

	/**
	 * Returns the metadata for the "Add pre/post tags to functions" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Add pre/post tags to functions" option
	 *
	 * @see DoxygenOptions#usePrePostTag()
	 */
	PreferenceMetadata<Boolean> usePrePostTagOption();

	/**
	 * Returns the list of available boolean options to be shown in UI, must not return <code>null</code>.
	 *
	 * @return the list of boolean options
	 */
	List<PreferenceMetadata<Boolean>> booleanOptions();

}
