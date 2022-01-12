/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

/**
 * IMacroDefinitions are in the form:
 * string1 = [string2]
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMacroDefinition extends IDirective {

	/**
	 * @return the name of the macro
	 */
	String getName();

	/**
	 * @return the value of the macro
	 */
	StringBuffer getValue();

	/**
	 * @return the macro is a built-in
	 */
	boolean isFromDefault();

	/**
	 * @return the macro was found in a Makefile.
	 *
	 */
	boolean isFromMakefile();

	/**
	 * @return the macro came from the environment.
	 */
	boolean isFromEnviroment();

	/**
	 * The macro came from the make command option -e
	 */
	boolean isFromEnvironmentOverride();

	/**
	 * @return the macro was pass from an option to make.
	 */
	boolean isFromCommand();
}
