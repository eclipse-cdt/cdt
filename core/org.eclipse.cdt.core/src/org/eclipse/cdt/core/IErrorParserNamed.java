/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev (Quoin Inc.) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

/**
 * Extension of IErrorParser interface to attach id and names to an error parser.
 * Clients must implement {@link Object#clone} and {@link Object#equals} methods to avoid slicing.
 * @since 5.2
 */
public interface IErrorParserNamed extends IErrorParser, Cloneable {
	/**
	 * Set error parser ID.
	 * @param id of error parser
	 */
	public void setId(String id);

	/**
	 * Set error parser name.
	 * @param name of error parser
	 */
	public void setName(String name);

	/**
	 * @return id of error parser
	 */
	public String getId();

	/**
	 * @return name of error parser
	 */
	public String getName();
}
