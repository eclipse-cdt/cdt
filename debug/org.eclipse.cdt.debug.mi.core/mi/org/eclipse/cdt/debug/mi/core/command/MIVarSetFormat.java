/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIFormat;

/**
 * 
 *    -var-set-format NAME FORMAT-SPEC
 *
 *  Sets the output format for the value of the object NAME to be
 * FORMAT-SPEC.
 *
 *  The syntax for the FORMAT-SPEC is as follows:
 *
 *     FORMAT-SPEC ==>
 *     {binary | decimal | hexadecimal | octal | natural}
 * 
 */
public class MIVarSetFormat extends MICommand 
{
	public MIVarSetFormat(String name, int fmt) {
		super("-var-set-format");
		String format = "hexadecimal";
		switch (fmt) {
		case MIFormat.NATURAL:
			format = "natural";
			break;
		case MIFormat.DECIMAL:
			format = "decimal";
			break;
		case MIFormat.BINARY:
			format = "binary";
			break;
		case MIFormat.OCTAL:
			format = "octal";
		break;
		/*
		case MIFormat.HEXADECIMAL:
		case MIFormat.RAW:
		default:
			format = "hexadecimal";
			break;
		*/
		}
		setParameters(new String[]{name, format});
	}
}
