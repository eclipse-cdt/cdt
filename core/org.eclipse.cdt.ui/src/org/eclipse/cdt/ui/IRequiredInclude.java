/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;


/**
 * Interface related to {@link IFunctionSummary} denoting an include required for a function.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IFunctionSummary
 */
public interface IRequiredInclude {

	/**
	 * Get the include name.
	 */
	String getIncludeName();

	/**
	 * Returns whether the include is to search on "standard places" like /usr/include first .
	 * An include is standard if it starts with <code>"&lt;"</code>.
	 */
	boolean isStandard();
}

