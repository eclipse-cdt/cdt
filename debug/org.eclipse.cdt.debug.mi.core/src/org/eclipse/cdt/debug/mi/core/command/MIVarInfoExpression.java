/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *     -var-info-expression NAME
 *
 *  Returns what is represented by the variable object NAME:
 *
 *     lang=LANG-SPEC,exp=EXPRESSION
 *
 * where LANG-SPEC is `{"C" | "C++" | "Java"}'.
 * 
 */
public class MIVarInfoExpression extends MICommand 
{
	public MIVarInfoExpression(String name) {
		super("-var-info-expression", new String[]{name});
	}
}
