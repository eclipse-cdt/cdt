/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core.model; 

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.debug.core.DebugException;

/**
 * The super class for all variable types.
 */
public abstract class AbstractCVariable extends CDebugElement implements ICVariable {

	/** 
	 * Constructor for AbstractCVariable. 
	 */
	public AbstractCVariable( CDebugTarget target ) {
		super( target );
	}

	/**
	 * Returns the text presentation of this variable as an expression.
	 * 
	 * @return the text presentation of this variable as an expression
	 * @throws DebugException
	 */
	public abstract String getExpressionString() throws DebugException;

	public abstract void dispose();
}
