/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *    -var-show-format NAME
 *
 *  Returns the format used to display the value of the object NAME.
 *
 *     FORMAT ==>
 *     FORMAT-SPEC
 * 
 */
public class MIVarShowFormat extends MICommand 
{
	public MIVarShowFormat(String miVersion, String name) {
		super(miVersion, "-var-show-format", new String[]{name}); //$NON-NLS-1$
	}
}
