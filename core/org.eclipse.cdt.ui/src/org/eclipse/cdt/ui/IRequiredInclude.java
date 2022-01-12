/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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
package org.eclipse.cdt.ui;

/**
 * Interface related to {@link IFunctionSummary} denoting an include required for a function.
 * <p>
 * Clients may implement this interface.
 * @see IFunctionSummary
 */
public interface IRequiredInclude {
	/**
	 * Returns the include name.
	 */
	String getIncludeName();

	/**
	 * Returns whether the include is to search on "standard places" like /usr/include first.
	 * An include is standard if it starts with <code>"&lt;"</code>.
	 */
	boolean isStandard();
}
