/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;


/**
 * 
 * Represents the value of a variable.
 * 
 * @since April 15, 2003
 */
public interface ICDIIntegralValue extends ICDIValue {

	public long longValue() throws CDIException;

	public int intValue() throws CDIException;
	
	public short shortValue() throws CDIException;

	public int byteValue() throws CDIException;

}
