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
public class DecVariableFormatActionDelegate extends VariableFormatActionDelegate
{

	/**
	 * Constructor for DecVariableFormatActionDelegate.
	 * @param format
	 */
	public DecVariableFormatActionDelegate()
	{
		super( ICDIFormat.DECIMAL );
	}
}
