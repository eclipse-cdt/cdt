/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;


/**
 * 
 * Represents a value of a array type.
 * 
 * @since April 15, 2003
 */
public interface ICDIArrayValue extends ICDIDerivedValue {
	ICDIVariable[] getVariables(int index, int length) throws CDIException;		
}
