/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

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
	public final static int HEXADECIMAL = 0;
	public final static int OCTAL = 1;
	public final static int BINARY = 2;
	public final static int DECIMAL = 3;
//	public final static int RAW = 4;
	public final static int NATURAL = 5;

	public MIVarSetFormat(String name, int fmt) {
		super("-var-set-format");
		String format = "hexadecimal";
		switch (fmt) {
		case NATURAL:
			format = "natural";
			break;
		case DECIMAL:
			format = "decimal";
			break;
		case BINARY:
			format = "binary";
			break;
		case OCTAL:
			format = "octal";
		break;
		/*
		case HEXADECIMAL:
		default:
			format = "x";
			break;
		*/
		}
		setParameters(new String[]{name, format});
	}
}
