/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -break-insert [ -t ] [ -h ] [ -r ]
 *       [ -c CONDITION ] [ -i IGNORE-COUNT ]
 *       [ -p THREAD ] [ LINE | ADDR ]
 * 
 * If specified, LINE, can be one of:
 * 
 *  * function
 *
 *  * filename:linenum
 *
 *  * filename:function
 *
 *  * *address
 * 
 *  The possible optional parameters of this command are:
 *
 * `-t'
 *     Insert a tempoary breakpoint.
 *
 * `-h'
 *     Insert a hardware breakpoint.
 *
 * `-c CONDITION'
 *     Make the breakpoint conditional on CONDITION.
 *
 * `-i IGNORE-COUNT'
 *     Initialize the IGNORE-COUNT.
 *
 * `-r'
 *
 *     Insert a regular breakpoint in all the functions whose names match
 *     the given regular expression.  Other flags are not applicable to
 *     regular expresson.
 *
 *  The result is in the form:
 *
 *     ^done,bkptno="NUMBER",func="FUNCNAME",
 *      file="FILENAME",line="LINENO"
 * 
 */
public class MIBreakInsert extends MICommand 
{
	public MIBreakInsert(String[] params) {
		super("-break-insert", params);
	}

	public MIBreakInsert(String[] opts, String[] params) {
		super("-break-insert", opts, params);
	}
}
