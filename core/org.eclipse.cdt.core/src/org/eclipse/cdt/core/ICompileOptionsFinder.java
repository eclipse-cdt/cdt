/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * A class that can find compiler options for a given file name.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.7
 */
public interface ICompileOptionsFinder {

	/**
	 * Get compiler options for a given file name.
	 *
	 * @param fileName - absolute source file name
	 * @return a String containing the compiler options used or null.
	 *
	 */
	public String getCompileOptions(String fileName);
}
