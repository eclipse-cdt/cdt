/*******************************************************************************
 * Copyright (c) 2005, 2007 Freescale, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.cdi;


/**
 * The CDI client's implementation of ICDIValue should implement this interface
 * if it wants to dictate the variable/register's natural format. If it doesn't,
 * CDT will provide a default behavior (e.g., all integral, non-pointer
 * ICDIValue variants will display as decimal).
 * 
 * CDT will exercise this interface only for ICDIValue's whose natural
 * format isn't obvious or implied. For example, it will not be exercised for
 * ICDIDoubleValue, ICDICharValue or ICDIBoolValue, to name a few. 
 * 
 * 
 */
public interface ICDIFormattable {
	/**
	 * Called when there is no obvious or implied natural format for the
	 * ICDIValue.
	 * 
	 * @return one of the ICDIFormat constants, excluding 'NATURAL' and 'OCTAL'.
	 *         Octal is not supported simply because the general support for it
	 *         is lacking in CDT (apparently no one is asking for it).
	 * @throws CDIException
	 */
	int getNaturalFormat() throws CDIException;
}
