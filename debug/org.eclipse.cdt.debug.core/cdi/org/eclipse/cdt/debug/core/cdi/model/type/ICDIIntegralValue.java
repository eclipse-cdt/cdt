/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model.type;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;


/**
 * 
 * Represents the value of a variable.
 * 
 * @since April 15, 2003
 */
public interface ICDIIntegralValue extends ICDIValue {

	public BigInteger bigIntegerValue() throws CDIException;

	public long longValue() throws CDIException;

	public int intValue() throws CDIException;
	
	public short shortValue() throws CDIException;

	public int byteValue() throws CDIException;

}
