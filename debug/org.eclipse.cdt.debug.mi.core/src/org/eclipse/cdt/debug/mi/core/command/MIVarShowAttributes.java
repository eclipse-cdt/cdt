/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -var-show-attributes NAME
 *
 *  List attributes of the specified variable object NAME:
 *
 *    status=ATTR [ ( ,ATTR )* ]
 *
 * where ATTR is `{ { editable | noneditable } | TBD }'.
 * 
 */
public class MIVarShowAttributes extends MICommand 
{
	public MIVarShowAttributes(String name) {
		super("-var-show-attributes", new String[]{name});
	}
}
