/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.debug.core.DebugException;

/**
 * The super class for all variable types.
 */
public abstract class AbstractCVariable extends CDebugElement implements ICVariable {

	/**
	 * The parent object this variable is contained in.
	 */
	private CDebugElement fParent;

	/** 
	 * Constructor for AbstractCVariable. 
	 */
	public AbstractCVariable( CDebugElement parent ) {
		super( (CDebugTarget)parent.getDebugTarget() );
		setParent( parent );
	}

	protected CDebugElement getParent() {
		return fParent;
	}

	private void setParent( CDebugElement parent ) {
		fParent = parent;
	}

	protected ICStackFrame getStackFrame() {
		CDebugElement parent = getParent();
		if ( parent instanceof AbstractCValue ) {
			AbstractCVariable pv = ((AbstractCValue)parent).getParentVariable();
			if ( pv != null )
				return pv.getStackFrame();
		}
		if ( parent instanceof CStackFrame )
			return (CStackFrame)parent;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( IEnableDisableTarget.class.equals( adapter ) )
			return this;
		return super.getAdapter( adapter );
	}

	/**
	 * Returns the text presentation of this variable as an expression.
	 * 
	 * @return the text presentation of this variable as an expression
	 * @throws DebugException
	 */
	public abstract String getExpressionString() throws DebugException;

	public abstract void dispose();

	protected abstract void resetValue();

	protected abstract void setChanged( boolean changed );

	protected abstract void preserve();
}
