/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.cdi.ICDIFormat;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 16, 2002
 */
public class HexVariableFormatActionDelegate extends VariableFormatActionDelegate
{
	/**
	 * Constructor for HexVariableFormatActionDelegate.
	 * @param format
	 */
	public HexVariableFormatActionDelegate()
	{
		super( ICDIFormat.HEXADECIMAL );
	}
}
