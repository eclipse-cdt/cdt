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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * A sub-range of an array.
 */
public class CArrayPartition extends AbstractCVariable {

	static final protected int SLOT_SIZE = 100;

	private int fStart;

	private int fEnd;

	private ICDIVariableDescriptor fCDIVariableObject;

	private ICDIVariable fCDIVariable;

	private ICType fType = null;

	private String fQualifiedName = null;

	/**
	 * Cached value.
	 */
	private CArrayPartitionValue fArrayPartitionValue = null;

	/** 
	 * Constructor for CArrayPartition. 
	 */
	private CArrayPartition( CDebugElement parent, ICDIVariable cdiVariable, int start, int end ) {
		super( parent );
		fStart = start;
		fEnd = end;
		fCDIVariable = cdiVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getType()
	 */
	public ICType getType() throws DebugException {
		if ( fType == null ) {
			try {
				ICDIVariableDescriptor varObject = getVariableObject();
				if ( varObject != null )
					fType = new CType( varObject.getType() );
			}
			catch( CDIException e ) {
				requestFailed( CoreModelMessages.getString( "CArrayPartition.0" ), e ); //$NON-NLS-1$
			}
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#setEnabled(boolean)
	 */
	public void setEnabled( boolean enabled ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isArgument()
	 */
	public boolean isArgument() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		if ( fArrayPartitionValue == null ) {
			fArrayPartitionValue = CValueFactory.createArrayValue( this, getCDIVariable(), getStart(), getEnd() );
		}
		return fArrayPartitionValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		StringBuffer name = new StringBuffer();
		name.append( '[' );
		name.append( fStart );
		name.append( ".." ); //$NON-NLS-1$
		name.append( fEnd );
		name.append( ']' );
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		ICType type = getType();
		return ( type != null ) ? type.getName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#supportsFormatting()
	 */
	public boolean supportsFormatting() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#getFormat()
	 */
	public CVariableFormat getFormat() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#changeFormat(org.eclipse.cdt.debug.core.model.CVariableFormat)
	 */
	public void changeFormat( CVariableFormat format ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#canCastToArray()
	 */
	public boolean canCastToArray() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#castToArray(int, int)
	 */
	public void castToArray( int startIndex, int length ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue( String expression ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue( IValue value ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue( String expression ) throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue( IValue value ) throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#canCast()
	 */
	public boolean canCast() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#getCurrentType()
	 */
	public String getCurrentType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#cast(java.lang.String)
	 */
	public void cast( String type ) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#restoreOriginal()
	 */
	public void restoreOriginal() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#isCasted()
	 */
	public boolean isCasted() {
		return false;
	}

	private ICDIVariableDescriptor getVariableObject() throws CDIException {
		if ( fCDIVariableObject == null ) {
			fCDIVariableObject = getCDIVariable().getVariableDescriptorAsArray(getStart(), getEnd() - getStart() + 1 );
			//fCDIVariableObject = getCDISession().getVariableManager().getVariableObjectAsArray( getCDIVariable(), getStart(), getEnd() - getStart() + 1 );
		}
		return fCDIVariableObject;
	}

	private ICDIVariable getCDIVariable() {
		return fCDIVariable;
	}

	private int getEnd() {
		return fEnd;
	}

	private int getStart() {
		return fStart;
	}

	static public List splitArray( CDebugElement parent, ICDIVariable cdiVariable, int start, int end ) throws DebugException {
		ArrayList children = new ArrayList();
		int len = end - start + 1;
		int perSlot = 1;
		while( len > perSlot * SLOT_SIZE ) {
			perSlot *= SLOT_SIZE;
		}
		if ( perSlot == 1 ) {
			try {
				ICDIValue value = cdiVariable.getValue();
				if ( value instanceof ICDIArrayValue ) {
					ICDIVariable[] cdiVars = ((ICDIArrayValue)value).getVariables( start, len );
					for( int i = 0; i < cdiVars.length; ++i )
						children.add( CVariableFactory.createVariable( parent, cdiVars[i] ) );
				}
			}
			catch( CDIException e ) {
				requestFailed( e.getMessage(), e );
			}
		}
		else {
			int pos = start;
			while( pos <= end ) {
				if ( pos + perSlot > end ) {
					perSlot = end - pos + 1;
				}
				children.add( new CArrayPartition( parent, cdiVariable, pos, pos + perSlot - 1 ) );
				pos += perSlot;
			}
		}
		return children;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#getExpressionString()
	 */
	public String getExpressionString() throws DebugException {
		if ( fQualifiedName == null ) {
			try {
				if ( getVariableObject() != null ) {
					fQualifiedName = getVariableObject().getQualifiedName();
				}
			}
			catch( CDIException e ) {
				requestFailed( CoreModelMessages.getString( "CArrayPartition.1" ), e ); //$NON-NLS-1$
			}
		}
		return fQualifiedName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	public void dispose() {
		if ( fType != null ) {
			((CType)fType).dispose();
			fType = null;
		}
		if ( fArrayPartitionValue != null ) {
			fArrayPartitionValue.dispose();
			fArrayPartitionValue = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#resetValue()
	 */
	protected void resetValue() {
		if ( fArrayPartitionValue != null ) {
			fArrayPartitionValue.reset();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#setChanged(boolean)
	 */
	protected void setChanged( boolean changed ) {
		if ( fArrayPartitionValue != null ) {
			fArrayPartitionValue.setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#preserve()
	 */
	protected void preserve() {
		setChanged( false );
		resetStatus();
		if ( fArrayPartitionValue != null ) {
			fArrayPartitionValue.preserve();
		}
	}
}
