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
package org.eclipse.cdt.debug.internal.core.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * A value for an array partition.
 */
public class CArrayPartitionValue extends AbstractCValue {

	/**
	 * The underlying CDI variable.
	 */
	private ICDIVariable fCDIVariable;

	/**
	 * List of child variables.
	 */
	private List fVariables = Collections.EMPTY_LIST;

	private int fStart;

	private int fEnd;

	/**
	 * Constructor for CArrayPartitionValue.
	 */
	public CArrayPartitionValue( AbstractCVariable parent, ICDIVariable cdiVariable, int start, int end ) {
		super( parent );
		fCDIVariable = cdiVariable;
		fStart = start;
		fEnd = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return ( getParentVariable() != null ) ? getParentVariable().getReferenceTypeName() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		List list = getVariables0();
		return (IVariable[])list.toArray( new IVariable[list.size()] );
	}

	protected synchronized List getVariables0() throws DebugException {
		if ( !isAllocated() || !hasVariables() )
			return Collections.EMPTY_LIST;
		if ( fVariables.size() == 0 ) {
			try {
				fVariables = CArrayPartition.splitArray( this, getCDIVariable(), getStart(), getEnd() );
			}
			catch( DebugException e ) {
				setStatus( ICDebugElementStatus.ERROR, e.getMessage() );
				getParentVariable().fireChangeEvent( DebugEvent.STATE );
			}
		}
		return fVariables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return true;
	}

	protected int getStart() {
		return fStart;
	}

	protected int getEnd() {
		return fEnd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#setChanged(boolean)
	 */
	protected void setChanged( boolean changed ) {
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((AbstractCVariable)it.next()).setChanged( changed );
		}
	}

	protected ICDIVariable getCDIVariable() {
		return fCDIVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#dispose()
	 */
	public void dispose() {
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((AbstractCVariable)it.next()).dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#reset()
	 */
	protected void reset() {
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((AbstractCVariable)it.next()).resetValue();
		}
	}

	public ICType getType() throws DebugException {
		return null;
	}
}