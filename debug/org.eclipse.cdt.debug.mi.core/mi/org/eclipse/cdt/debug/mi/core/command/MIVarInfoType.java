/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -var-info-type NAME
 *
 *   Returns the type of the specified variable NAME.  The type is
 * returned as a string in the same format as it is output by the GDB CLI:
 *
 *     type=TYPENAME
 * 
 */
public class MIVarInfoType extends MICommand 
{
	public MIVarInfoType(String name) {
		super("-var-info-type", new String[]{name}); //$NON-NLS-1$
	}
}
