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

/**
 * Options to configure doxygen
 *
 */
public interface DoxygenOptions {

	/**
	 * Use always brief tag in auto-generation of doxygen comment
	 *
	 */
	boolean useBriefTags();

	/**
	 * Use always structured commands in auto-generation of doxygen comment
	 *
	 */
	boolean useStructuralCommands();

	/**
	 * Use always javadoc tag style in auto-generation of doxygen comment
	 *
	 */
	boolean useJavadocStyle();

	/**
	 * Use always a new line after brief tag in auto-generation of doxygen comment
	 *
	 */
	boolean newLineAfterBrief();

	/**
	 * Use always pre/post tags in auto-generation of doxygen comment
	 *
	 */
	boolean usePrePostTag();

}
