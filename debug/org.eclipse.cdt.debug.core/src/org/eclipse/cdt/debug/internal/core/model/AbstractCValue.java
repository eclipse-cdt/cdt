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

import org.eclipse.cdt.debug.core.model.ICValue;

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

	abstract protected void setChanged( boolean changed );

	abstract public void dispose();

	abstract protected void reset();
}
