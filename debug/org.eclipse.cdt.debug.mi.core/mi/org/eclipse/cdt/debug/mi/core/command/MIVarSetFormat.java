/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
		super("-var-set-format"); //$NON-NLS-1$
		String format = "hexadecimal"; //$NON-NLS-1$
		switch (fmt) {
		case MIFormat.NATURAL:
			format = "natural"; //$NON-NLS-1$
			break;
		case MIFormat.DECIMAL:
			format = "decimal"; //$NON-NLS-1$
			break;
		case MIFormat.BINARY:
			format = "binary"; //$NON-NLS-1$
			break;
		case MIFormat.OCTAL:
			format = "octal"; //$NON-NLS-1$
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
