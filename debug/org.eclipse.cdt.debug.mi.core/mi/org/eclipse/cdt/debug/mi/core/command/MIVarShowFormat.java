/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -var-show-format NAME
 *
 *  Returns the format used to display the value of the object NAME.
 *
 *     FORMAT ==>
 *     FORMAT-SPEC
 * 
 */
public class MIVarShowFormat extends MICommand 
{
	public MIVarShowFormat(String name) {
		super("-var-show-format", new String[]{name});
	}
}
