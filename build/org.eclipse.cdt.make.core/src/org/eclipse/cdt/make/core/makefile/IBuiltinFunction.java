/*******************************************************************************
 * Copyright (c) 2013,2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

/**
 * Interface fore built-in internal functions.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 7.3
 */
public interface IBuiltinFunction extends IDirective {

	/**
	 * @return the name of the function
	 */
	String getName();

	/**
	 * @return the value of the function
	 */
	StringBuffer getValue();

}
