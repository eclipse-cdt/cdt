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
public interface ICDIFloatingPointValue extends ICDIValue {

	float floatValue() throws CDIException;

	double doubleValue() throws CDIException;
}
