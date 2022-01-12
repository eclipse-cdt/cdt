/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.cdtvariables;

import org.eclipse.core.runtime.IStatus;

/**
 * This interface represents the status of a build macro operation
 *
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICdtVariableStatus extends IStatus {
	/**
	 * This type is used to present that the inexistent macro reference
	 * is encountered while resolving macros in some expression
	 */
	public static final int TYPE_MACRO_UNDEFINED = 1;

	/**
	 * This type is used to present that two macros reference each other
	 */
	public static final int TYPE_MACROS_REFERENCE_EACHOTHER = 2;

	/**
	 * This type is used to present that the incorrect macro reference
	 * is encountered while resolving macros in some expression
	 */
	public static final int TYPE_MACRO_REFERENCE_INCORRECT = 3;

	/**
	 * The status of this type is created by the Build Macro of the String-List type
	 * when the String value is requested
	 */
	public static final int TYPE_MACRO_NOT_STRING = 4;

	/**
	 * The status of this type is created by the Build Macro of the String type
	 * when the String-List value is requested
	 */
	public static final int TYPE_MACRO_NOT_STRINGLIST = 5;

	/**
	 * This type is used to present that some error other than the one represented
	 * by other TYPE_xxx has occured
	 */
	public static final int TYPE_ERROR = -1;

	/**
	 * returns the name of the build macro whose resolution caused this status creation or null if none
	 * @return IBuildMacro
	 */
	public String getVariableName();

	/**
	 * returns the string whose resolutinon caused caused this status creation or null if none
	 * @return String
	 */
	public String getExpression();

	/**
	 * returns the macro name referenced in the resolution string that caused this this status creation or null if none
	 * @return String
	 */
	public String getReferencedMacroName();

	/**
	 * returns the context type used in the operation
	 * @return int
	 */
	//	public int getContextType();

	/**
	 * returns the context data used in the operation
	 * @return Object
	 */
	//	public Object getContextData();
}
