/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mark Mitchell, CodeSourcery - Bug 136896: View variables in binary format
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.CVariableFormat;

/**
 * The delegate of the "Binary Format" action.
 */
public class BinaryVariableFormatActionDelegate extends VariableFormatActionDelegate {

	/**
	 * Constructor for BinaryVariableFormatActionDelegate.
	 */
	public BinaryVariableFormatActionDelegate() {
		super( CVariableFormat.BINARY );
	}
}
