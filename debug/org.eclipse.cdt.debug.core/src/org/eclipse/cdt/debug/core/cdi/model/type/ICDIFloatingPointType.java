/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model.type;


/**
 * 
 * Represents the type of a variable.
 * 
 * @since Apr 15, 2003
 */
public interface ICDIFloatingPointType extends ICDIType {

	boolean isImaginary();

	boolean isComplex();

	boolean isLong();
}
