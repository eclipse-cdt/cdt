/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -var-update {NAME | "*"}
 *
 *  Update the value of the variable object NAME by evaluating its
 * expression after fetching all the new values from memory or registers.
 * A `*' causes all existing variable objects to be updated.
 * 
 */
public class MIVarUpdate extends MICommand 
{
	public MIVarUpdate() {
		this("*");
	}
	public MIVarUpdate(String name) {
		super("-var-update", new String[]{name});
	}
}
