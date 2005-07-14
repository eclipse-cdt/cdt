/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model; 

import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;

/**
 * The abstract super class for the C/C++ value types.
 */
public abstract class AbstractCValue extends CDebugElement implements ICValue {

	/**
	 * Parent variable.
	 */
	private AbstractCVariable fParent = null;

	/** 
	 * Constructor for AbstractCValue. 
	 */
	public AbstractCValue( AbstractCVariable parent ) {
		super( (CDebugTarget)parent.getDebugTarget() );
		fParent = parent;
	}

	public AbstractCVariable getParentVariable() {
		return fParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#evaluateAsExpression(org.eclipse.cdt.debug.core.model.ICStackFrame)
	 */
	public String evaluateAsExpression( ICStackFrame frame ) {
		String valueString = ""; //$NON-NLS-1$
		AbstractCVariable parent = getParentVariable();
		if ( parent != null ) {
			if ( frame != null && frame.canEvaluate() ) {
				try {
					valueString = frame.evaluateExpressionToString( parent.getExpressionString() );
				}
				catch( DebugException e ) {
					valueString = e.getMessage();
				}
			}
		}
		return valueString;
	}

	abstract protected void setChanged( boolean changed );

	abstract public void dispose();

	abstract protected void reset();

	abstract protected void preserve();
}
