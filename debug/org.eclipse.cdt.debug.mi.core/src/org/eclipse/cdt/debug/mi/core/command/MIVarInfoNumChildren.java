/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -var-info-num-children NAME
 *
 *  Returns the number of children of a variable object NAME:
 *
 *     numchild=N
 * 
 */
public class MIVarInfoNumChildren extends MICommand 
{
	public MIVarInfoNumChildren(String name) {
		super("-var-info-num-children", new String[]{name});
	}
}
