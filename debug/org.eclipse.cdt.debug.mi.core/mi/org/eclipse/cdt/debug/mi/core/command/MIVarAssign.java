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

/**
 * 
 *    -var-assign NAME EXPRESSION
 *
 *  Assigns the value of EXPRESSION to the variable object specified by
 * NAME.  The object must be `editable'.
 * 
 */
public class MIVarAssign extends MICommand 
{
	public MIVarAssign(String name, String expression) {
		super("-var-assign", new String[]{name, expression}); //$NON-NLS-1$
	}
}
