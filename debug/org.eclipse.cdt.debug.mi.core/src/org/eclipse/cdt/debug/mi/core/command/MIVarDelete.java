/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -var-delete NAME
 *
 *  Deletes a previously created variable object and all of its children.
 *
 *  Returns an error if the object NAME is not found.
 * 
 */
public class MIVarDelete extends MICommand 
{
	public MIVarDelete(String name) {
		super("-var-delete", new String[]{name});
	}
	
}
