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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharValue;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * Represents the value of a variable in the CDI model.
 */
public class CValue extends AbstractCValue {
	/**
	 * Cached value.
	 */
	private String fValueString = null;

	/**
	 * Underlying CDI value.
	 */
	private ICDIValue fCDIValue;

	/**
	 * List of child variables.
	 */
	private List fVariables = Collections.EMPTY_LIST;

	private CType fType;

	/**
	 * Constructor for CValue.
	 */
	protected CValue( CVariable parent, ICDIValue cdiValue ) {
		super( parent );
		fCDIValue = cdiValue;
	}

	/**
	 * Constructor for CValue.
	 */
	protected CValue( CVariable parent, String message ) {
		super( parent );
		setStatus( ICDebugElementStatus.ERROR, message );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return ( getParentVariable() != null ) ? getParentVariable().getReferenceTypeName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		if ( fValueString == null && getUnderlyingValue() != null ) {
			resetStatus();
			ICStackFrame cframe = getParentVariable().getStackFrame();
			boolean isSuspended = (cframe == null)  ? getCDITarget().isSuspended() : cframe.isSuspended();
			if ( isSuspended ) {
				try {
					fValueString = processUnderlyingValue( getUnderlyingValue() );
				}
				catch( CDIException e ) {
					setStatus( ICDebugElementStatus.ERROR, e.getMessage() );
				}
			}
		}
		return fValueString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/* (non-Javadoc)
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
				List vars = getCDIVariables();
				fVariables = new ArrayList( vars.size() );
				Iterator it = vars.iterator();
				while( it.hasNext() ) {
					fVariables.add( CVariableFactory.createVariable( this, (ICDIVariable)it.next() ) );
				}
				resetStatus();
			}
			catch( DebugException e ) {
				setStatus( ICDebugElementStatus.ERROR, e.getMessage() );
			}
		}
		return fVariables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		try {
			ICDIValue value = getUnderlyingValue();
			if ( value != null )
				return value.getChildrenNumber() > 0;
		}
		catch( CDIException e ) {
			targetRequestFailed( e.getMessage(), null );
		}
		return false;
	}

	public ICDIValue getUnderlyingValue() {
		return fCDIValue;
	}

	protected List getCDIVariables() throws DebugException {
		ICDIVariable[] vars = null;
		try {
			ICDIValue value = getUnderlyingValue();
			if ( value != null ) {
				vars = value.getVariables();
				// Quick fix.
				// getVariables should return an empty array instead of null.
				if ( vars == null ) {
					vars = new ICDIVariable[0];
				}
			}
		}
		catch( CDIException e ) {
			requestFailed( e.getMessage(), e ); //$NON-NLS-1$
		}
		return Arrays.asList( vars );
	}

	protected synchronized void setChanged( boolean changed ) {
		if ( changed ) {
			fValueString = null;
			resetStatus();
		}
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((AbstractCVariable)it.next()).setChanged( changed );
		}
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

	private String processUnderlyingValue( ICDIValue cdiValue ) throws CDIException {
		if ( cdiValue != null ) {
			if ( cdiValue instanceof ICDICharValue )
				return getCharValueString( (ICDICharValue)cdiValue );
			else if ( cdiValue instanceof ICDIShortValue )
				return getShortValueString( (ICDIShortValue)cdiValue );
			else if ( cdiValue instanceof ICDIIntValue )
				return getIntValueString( (ICDIIntValue)cdiValue );
			else if ( cdiValue instanceof ICDILongValue )
				return getLongValueString( (ICDILongValue)cdiValue );
			else if ( cdiValue instanceof ICDILongLongValue )
				return getLongLongValueString( (ICDILongLongValue)cdiValue );
			else if ( cdiValue instanceof ICDIFloatValue )
				return getFloatValueString( (ICDIFloatValue)cdiValue );
			else if ( cdiValue instanceof ICDIDoubleValue )
				return getDoubleValueString( (ICDIDoubleValue)cdiValue );
			else if ( cdiValue instanceof ICDIPointerValue )
				return getPointerValueString( (ICDIPointerValue)cdiValue );
			else if ( cdiValue instanceof ICDIReferenceValue )
				return getReferenceValueString( (ICDIReferenceValue)cdiValue );
			else if ( cdiValue instanceof ICDIWCharValue )
				return getWCharValueString( (ICDIWCharValue)cdiValue );
			else
				return cdiValue.getValueString();
		}
		return null;
	}

	private String getCharValueString( ICDICharValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			byte byteValue = (byte)value.byteValue();
			return ((Character.isISOControl( (char)byteValue ) && byteValue != '\b' && byteValue != '\t' && byteValue != '\n' && byteValue != '\f' && byteValue != '\r') || byteValue < 0) ? "" : new String( new byte[]{ '\'', byteValue, '\'' } ); //$NON-NLS-1$
		}
		else if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return (isUnsigned()) ? Integer.toString( value.shortValue() ) : Integer.toString( (byte)value.byteValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = (isUnsigned()) ? Integer.toHexString( value.shortValue() ) : Integer.toHexString( (byte)value.byteValue() );
			sb.append( (stringValue.length() > 2) ? stringValue.substring( stringValue.length() - 2 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getShortValueString( ICDIShortValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.DECIMAL.equals( format ) ) {
			return (isUnsigned()) ? Integer.toString( value.intValue() ) : Short.toString( value.shortValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = Integer.toHexString( (isUnsigned()) ? value.intValue() : value.shortValue() );
			sb.append( (stringValue.length() > 4) ? stringValue.substring( stringValue.length() - 4 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getIntValueString( ICDIIntValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.DECIMAL.equals( format ) ) {
			return (isUnsigned()) ? Long.toString( value.longValue() ) : Integer.toString( value.intValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = (isUnsigned()) ? Long.toHexString( value.longValue() ) : Integer.toHexString( value.intValue() );
			sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getLongValueString( ICDILongValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.DECIMAL.equals( format ) ) {
			if ( isUnsigned() ) {
				BigInteger bigValue = new BigInteger( value.getValueString() );
				return bigValue.toString();
			}
			return Long.toString( value.longValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			if ( isUnsigned() ) {
				BigInteger bigValue = new BigInteger( value.getValueString() );
				sb.append( bigValue.toString( 16 ) );
			}
			else
				sb.append( Long.toHexString( value.longValue() ) );
			return sb.toString();
		}
		return null;
	}

	private String getLongLongValueString( ICDILongLongValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.DECIMAL.equals( format ) ) {
			if ( isUnsigned() ) {
				BigInteger bigValue = new BigInteger( value.getValueString() );
				return bigValue.toString();
			}
			return Long.toString( value.longValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			if ( isUnsigned() ) {
				BigInteger bigValue = new BigInteger( value.getValueString() );
				sb.append( bigValue.toString( 16 ) );
			}
			else
				sb.append( Long.toHexString( value.longValue() ) );
			return sb.toString();
		}
		return null;
	}

	private String getFloatValueString( ICDIFloatValue value ) throws CDIException {
		float floatValue = value.floatValue();
		Float flt = new Float( floatValue );
		if ( flt.isNaN() || flt.isInfinite() )
			return ""; //$NON-NLS-1$
		long longValue = flt.longValue();
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			return Float.toString( floatValue );
		}
		else if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return Long.toString( longValue );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = Long.toHexString( longValue );
			sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getDoubleValueString( ICDIDoubleValue value ) throws CDIException {
		double doubleValue = value.doubleValue();
		Double dbl = new Double( doubleValue );
		if ( dbl.isNaN() || dbl.isInfinite() )
			return ""; //$NON-NLS-1$
		long longValue = dbl.longValue();
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			return dbl.toString();
		}
		else if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return Long.toString( longValue );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = Long.toHexString( longValue );
			sb.append( (stringValue.length() > 16) ? stringValue.substring( stringValue.length() - 16 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getPointerValueString( ICDIPointerValue value ) throws CDIException {
		//TODO:IPF_TODO Workaround to solve incorrect handling of structures referenced by pointers or references
		IAddressFactory factory = ((CDebugTarget)getDebugTarget()).getAddressFactory();
		BigInteger pv = value.pointerValue();
		if ( pv == null )
			return ""; //$NON-NLS-1$
		IAddress address = factory.createAddress( value.pointerValue() );
		if ( address == null )
			return ""; //$NON-NLS-1$
		CVariableFormat format = getParentVariable().getFormat();
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.HEXADECIMAL.equals( format ) )
			return address.toHexAddressString();
		if ( CVariableFormat.DECIMAL.equals( format ) )
			return address.toString();
		return null;
	}

  	private String getReferenceValueString( ICDIReferenceValue value ) throws CDIException {
		//NOTE: Reference should be displayed identically to address
		//TODO:IPF_TODO Workaround to solve incoorect handling of structures referenced by pointers or references
		IAddressFactory factory = ((CDebugTarget)getDebugTarget()).getAddressFactory();
		BigInteger refValue = value.referenceValue();
		if ( refValue == null )
			return ""; //$NON-NLS-1$
		IAddress address = factory.createAddress( refValue );
		if ( address == null )
			return ""; //$NON-NLS-1$
		CVariableFormat format = getParentVariable().getFormat();
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.HEXADECIMAL.equals( format ) )
			return address.toHexAddressString();
		if ( CVariableFormat.DECIMAL.equals( format ) )
			return address.toString();
		return null;
	}

	private String getWCharValueString( ICDIWCharValue value ) throws CDIException {
		if ( getParentVariable() instanceof CVariable ) {
			int size = ((CVariable)getParentVariable()).sizeof();
			if ( size == 2 ) {
				CVariableFormat format = getParentVariable().getFormat(); 
				if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.DECIMAL.equals( format ) ) {
					return (isUnsigned()) ? Integer.toString( value.intValue() ) : Short.toString( value.shortValue() );
				}
				else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
					StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
					String stringValue = Integer.toHexString( (isUnsigned()) ? value.intValue() : value.shortValue() );
					sb.append( (stringValue.length() > 4) ? stringValue.substring( stringValue.length() - 4 ) : stringValue );
					return sb.toString();
				}
			}
			if ( size == 4 ) {
				CVariableFormat format = getParentVariable().getFormat(); 
				if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.DECIMAL.equals( format ) ) {
					return (isUnsigned()) ? Long.toString( value.longValue() ) : Integer.toString( value.intValue() );
				}
				else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
					StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
					String stringValue = (isUnsigned()) ? Long.toHexString( value.longValue() ) : Integer.toHexString( value.intValue() );
					sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
					return sb.toString();
				}
			}
		}
		return value.getValueString();
	}

	private boolean isUnsigned() {
		boolean result = false;
		try {
			ICType type = getParentVariable().getType();
			if ( type != null )
				result = type.isUnsigned();
		}
		catch( DebugException e ) {
		}
		return result;
	}

	/**
	 * Invalidates the string cache.
	 */
	protected void reset() {
		resetStatus();
		fValueString = null;
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((AbstractCVariable)it.next()).resetValue();
		}
	}

	public ICType getType() throws DebugException {
		ICDIValue cdiValue = getUnderlyingValue();
		if ( fType == null ) {
			if ( cdiValue != null ) {
				synchronized( this ) {
					if ( fType == null ) {
						try {
							fType = new CType( cdiValue.getType() );
						}
						catch( CDIException e ) {
							requestFailed( e.getMessage(), null );
						}
					}
				}
			}
		}
		return fType;
//		AbstractCVariable var = getParentVariable();
//		return ( var instanceof CVariable ) ? ((CVariable)var).getType() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#preserve()
	 */
	protected void preserve() {
		setChanged( false );
		resetStatus();
		Iterator it = fVariables.iterator();
		while( it.hasNext() ) {
			((AbstractCVariable)it.next()).preserve();
		}
	}
}