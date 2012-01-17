/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Mark Mitchell, CodeSourcery - Bug 136896: View variables in binary format
 *     Warren Paul (Nokia) - 150860, 150864, 150862, 150863, 217493
 *     Ken Ryall (Nokia) - 207675
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.core.cdi.ICDIFormattable;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBigIntegerValue;
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
	private List<AbstractCVariable> fVariables = new ArrayList<AbstractCVariable>();

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
	@Override
	public String getReferenceTypeName() throws DebugException {
		return ( getParentVariable() != null ) ? getParentVariable().getReferenceTypeName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	@Override
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
					fValueString = e.getLocalizedMessage();
				}
			}
		}
		return fValueString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	@Override
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	@Override
	public IVariable[] getVariables() throws DebugException {
		List<AbstractCVariable> list = getVariables0();
		return list.toArray( new IVariable[list.size()] );
	}

	protected synchronized List<AbstractCVariable> getVariables0() throws DebugException {
		if ( !isAllocated() || !hasVariables() )
			return new ArrayList<AbstractCVariable>();
		if ( fVariables.size() == 0 ) {
			try {
				List<ICDIVariable> vars = getCDIVariables();
				for (ICDIVariable var : vars) {
					if (getParentVariable() instanceof CGlobalVariable) {
						fVariables.add(CVariableFactory.createGlobalVariable( 
								this, 
								null, 
								var));
					}
					else {
						fVariables.add(CVariableFactory.createLocalVariable(this, var));
					}
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
	@Override
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

	protected List<ICDIVariable> getCDIVariables() throws DebugException {
		ICDIVariable[] vars = null;
		try {
			ICDIValue value = getUnderlyingValue();
			if ( value != null ) {
				vars = value.getVariables();
			}
		}
		catch( CDIException e ) {
			requestFailed( e.getMessage(), e );
		}

		// getVariables should return an empty array instead of null.
		if ( vars == null ) {
			vars = new ICDIVariable[0];
		}
		
		return Arrays.asList( vars );
	}

	@Override
	protected synchronized void setChanged( boolean changed ) {
		if ( changed ) {
			fValueString = null;
			resetStatus();
		}
		else {
			if (getCDITarget().getConfiguration() instanceof ICDITargetConfiguration2 &&
					((ICDITargetConfiguration2)getCDITarget().getConfiguration()).supportsPassiveVariableUpdate())
				fValueString = null;
		}
		
		for (AbstractCVariable var : fVariables) {
			var.setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#dispose()
	 */
	@Override
	public void dispose() {
		for (AbstractCVariable var : fVariables) {
			var.dispose();
		}
	}

	private String processUnderlyingValue( ICDIValue cdiValue ) throws CDIException {
		if ( cdiValue != null ) {
			if ( cdiValue instanceof ICDIBoolValue )
				return getBoolValueString( (ICDIBoolValue)cdiValue );
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
			else if ( cdiValue instanceof ICDIBigIntegerValue )
				return getBigIntegerValueString( (ICDIBigIntegerValue)cdiValue );
			else if ( cdiValue instanceof ICDIFloatValue )
				return getFloatValueString( (ICDIFloatValue)cdiValue );
			else if ( cdiValue instanceof ICDIDoubleValue )
				return getDoubleValueString( (ICDIDoubleValue)cdiValue );
			else if ( cdiValue instanceof ICDIPointerValue )
				return getPointerValueString( (ICDIPointerValue)cdiValue );
			else if ( cdiValue instanceof ICDIReferenceValue )
				return processUnderlyingValue(((ICDIReferenceValue)cdiValue).referenceValue());
			else if ( cdiValue instanceof ICDIWCharValue )
				return getWCharValueString( (ICDIWCharValue)cdiValue );
			else
				return  getGenericValueString(cdiValue.getValueString());
		}
		return null;
	}

	private String getBoolValueString( ICDIBoolValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			short byteValue = value.shortValue();
			if (byteValue == 0)
				return "false";//$NON-NLS-1$
			else if (byteValue == 1)
				return "true";//$NON-NLS-1$
			else
				return Integer.toString( value.shortValue() );
		}
		else if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return Integer.toString( value.shortValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = (isUnsigned()) ? Integer.toHexString( value.shortValue() ) : Integer.toHexString( (byte)value.byteValue() );
			sb.append( (stringValue.length() > 2) ? stringValue.substring( stringValue.length() - 2 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getCharValueString( ICDICharValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			byte byteValue = (byte)value.byteValue();
			switch (byteValue) {
			case '\b':
				return "'\\b'";		//$NON-NLS-1$
			case '\t':
				return "'\\t'";		//$NON-NLS-1$
			case '\n':
				return "'\\n'";		//$NON-NLS-1$
			case '\f':
				return "'\\f'";		//$NON-NLS-1$
			case '\r':
				return "'\\r'";		//$NON-NLS-1$
			}

			if (Character.isISOControl(byteValue))
				return Byte.toString(byteValue);
			else if (byteValue < 0)
				return isUnsigned() ? Short.toString(value.shortValue()) : Byte.toString(byteValue);

			return new String( new byte[]{ '\'', byteValue, '\'' } );
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
		else if ( CVariableFormat.BINARY.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
			String stringValue = (isUnsigned()) ? Integer.toBinaryString( value.shortValue() ) : Integer.toBinaryString( (byte)value.byteValue() );
			sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getShortValueString( ICDIShortValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat();
		
		if (CVariableFormat.NATURAL.equals(format)) {
			format = getNaturalFormat(value, CVariableFormat.DECIMAL);
		}
		
		if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return (isUnsigned()) ? Integer.toString( value.intValue() ) : Short.toString( value.shortValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = Integer.toHexString( (isUnsigned()) ? value.intValue() : value.shortValue() );
			sb.append( (stringValue.length() > 4) ? stringValue.substring( stringValue.length() - 4 ) : stringValue );
			return sb.toString();
		}
		else if ( CVariableFormat.BINARY.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
			String stringValue = Integer.toBinaryString( (isUnsigned()) ? value.intValue() : value.shortValue() );
			sb.append( (stringValue.length() > 16) ? stringValue.substring( stringValue.length() - 16 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getIntValueString( ICDIIntValue value ) throws CDIException {
		CVariableFormat format = getParentVariable().getFormat();
		
		if (CVariableFormat.NATURAL.equals(format)) {
			format = getNaturalFormat(value, CVariableFormat.DECIMAL);
		}
		
		if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return (isUnsigned()) ? Long.toString( value.longValue() ) : Integer.toString( value.intValue() );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = (isUnsigned()) ? Long.toHexString( value.longValue() ) : Integer.toHexString( value.intValue() );
			sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
			return sb.toString();
		}
		else if ( CVariableFormat.BINARY.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
			String stringValue = (isUnsigned()) ? Long.toBinaryString( value.longValue() ) : Integer.toBinaryString( value.intValue() );
			sb.append( (stringValue.length() > 32) ? stringValue.substring( stringValue.length() - 32 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getLongValueString( ICDILongValue value ) throws CDIException {
		try {
			CVariableFormat format = getParentVariable().getFormat();

			if (CVariableFormat.NATURAL.equals(format)) {
				format = getNaturalFormat(value, CVariableFormat.DECIMAL);
			}
			
			if ( CVariableFormat.DECIMAL.equals( format ) ) {
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
			else if ( CVariableFormat.BINARY.equals( format ) ) {
				StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
				if ( isUnsigned() ) {
					BigInteger bigValue = new BigInteger( value.getValueString() );
					sb.append( bigValue.toString( 2 ) );
				}
				else
					sb.append( Long.toBinaryString( value.longValue() ) );
				return sb.toString();
			}
		}
		catch( NumberFormatException e ) {
		}
		return null;
	}

	private String getLongLongValueString( ICDILongLongValue value ) throws CDIException {
		try {
			CVariableFormat format = getParentVariable().getFormat();
			
			if (CVariableFormat.NATURAL.equals(format)) {
				format = getNaturalFormat(value, CVariableFormat.DECIMAL);
			}
			
			if ( CVariableFormat.DECIMAL.equals( format ) ) {
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
			else if ( CVariableFormat.BINARY.equals( format ) ) {
				StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
				if ( isUnsigned() ) {
					BigInteger bigValue = new BigInteger( value.getValueString() );
					sb.append( bigValue.toString( 2 ) );
				}
				else
					sb.append( Long.toBinaryString( value.longValue() ) );
				return sb.toString();
			}
		}
		catch( NumberFormatException e ) {
		}
		return null;
	}

	private String getGenericValueString(String svalue) throws CDIException {
		try {
			BigInteger bigValue = new BigInteger(svalue);
			CVariableFormat format = getParentVariable().getFormat();
			if (CVariableFormat.NATURAL.equals(format)) {
				format = CVariableFormat.DECIMAL;
			}
			if (CVariableFormat.DECIMAL.equals(format)) {
				return svalue;
			} else if (CVariableFormat.HEXADECIMAL.equals(format)) {
				StringBuffer sb = new StringBuffer("0x"); //$NON-NLS-1$
				if (isUnsigned()) {
					sb.append(bigValue.toString(16));
				} else
					sb.append(Long.toHexString(bigValue.longValue()));
				return sb.toString();
			} else if (CVariableFormat.BINARY.equals(format)) {
				StringBuffer sb = new StringBuffer("0b"); //$NON-NLS-1$
				if (isUnsigned()) {
					sb.append(bigValue.toString(2));
				} else
					sb.append(Long.toBinaryString(bigValue.longValue()));
				return sb.toString();
			}
		} catch (NumberFormatException e) {
		}
		return svalue;
	}
	
	
	private String getFloatValueString( ICDIFloatValue value ) throws CDIException {
		float floatValue = value.floatValue();
		if ( Float.isNaN(floatValue) )
			return "NaN"; //$NON-NLS-1$
		if ( Float.isInfinite(floatValue) )
			return "inf"; //$NON-NLS-1$

		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			return Float.toString( floatValue );
		}
		else if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return Long.toString( Float.floatToIntBits(floatValue) );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = Long.toHexString( Float.floatToIntBits(floatValue) );
			sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
			return sb.toString();
		}
		else if ( CVariableFormat.BINARY.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
			String stringValue = Long.toBinaryString( Float.floatToIntBits(floatValue) );
			sb.append( (stringValue.length() > 32) ? stringValue.substring( stringValue.length() - 32 ) : stringValue );
			return sb.toString();
		}
		return null;
	}

	private String getDoubleValueString( ICDIDoubleValue value ) throws CDIException {
		double doubleValue = value.doubleValue();
		if ( Double.isNaN(doubleValue) )
			return "NaN"; //$NON-NLS-1$
		if ( Double.isInfinite(doubleValue) )
			return "inf"; //$NON-NLS-1$

		CVariableFormat format = getParentVariable().getFormat(); 
		if ( CVariableFormat.NATURAL.equals( format ) ) {
			return Double.toString(doubleValue);
		}
		else if ( CVariableFormat.DECIMAL.equals( format ) ) {
			return Long.toString( Double.doubleToLongBits(doubleValue) );
		}
		else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
			String stringValue = Long.toHexString( Double.doubleToLongBits(doubleValue) );
			sb.append( (stringValue.length() > 16) ? stringValue.substring( stringValue.length() - 16 ) : stringValue );
			return sb.toString();
		}
		else if ( CVariableFormat.BINARY.equals( format ) ) {
			StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
			String stringValue = Long.toBinaryString( Double.doubleToLongBits(doubleValue) );
			sb.append( (stringValue.length() > 64) ? stringValue.substring( stringValue.length() - 64 ) : stringValue );
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
		IAddress address = factory.createAddress( pv );
		if ( address == null )
			return ""; //$NON-NLS-1$
		CVariableFormat format = getParentVariable().getFormat();
		if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.HEXADECIMAL.equals( format ) )
			return address.toHexAddressString();
		if ( CVariableFormat.DECIMAL.equals( format ) )
			return address.toString();
		if ( CVariableFormat.BINARY.equals( format ) )
			return address.toBinaryAddressString();
		return null;
	}

	private String getWCharValueString( ICDIWCharValue value ) throws CDIException {
		if ( getParentVariable() instanceof CVariable ) {
			int size = ((CVariable)getParentVariable()).sizeof();
			if ( size == 2 ) {
				CVariableFormat format = getParentVariable().getFormat(); 
				if ( CVariableFormat.NATURAL.equals( format ) ) {					
					ByteBuffer buffer = ByteBuffer.allocate(4);
					buffer.putInt(value.intValue());
					buffer.position(2);					
					String stringValue;
					try {
						stringValue = new String(CDebugUtils.getCharsetDecoder().decode(buffer).array());
					} catch (CharacterCodingException e) {
						stringValue = e.toString();
					}
					StringBuffer sb = new StringBuffer("'"); //$NON-NLS-1$
					sb.append(stringValue);
					sb.append('\'');
					return sb.toString();
				}
				else if ( CVariableFormat.DECIMAL.equals( format ) ) {
					return (isUnsigned()) ? Integer.toString( value.intValue() ) : Short.toString( value.shortValue() );
				}
				else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
					StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
					String stringValue = Integer.toHexString( (isUnsigned()) ? value.intValue() : value.shortValue() );
					sb.append( (stringValue.length() > 4) ? stringValue.substring( stringValue.length() - 4 ) : stringValue );
					return sb.toString();
				}
				else if ( CVariableFormat.BINARY.equals( format ) ) {
					StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
					String stringValue = Integer.toBinaryString( (isUnsigned()) ? value.intValue() : value.shortValue() );
					sb.append( (stringValue.length() > 16) ? stringValue.substring( stringValue.length() - 16 ) : stringValue );
					return sb.toString();
				}
			}
			if ( size == 4 ) {
				CVariableFormat format = getParentVariable().getFormat(); 
				if ( CVariableFormat.NATURAL.equals( format ) ) {					
					ByteBuffer buffer = ByteBuffer.allocate(8);
					buffer.putLong(value.longValue());
					buffer.position(4);					
					String stringValue;
					try {
						stringValue = new String(CDebugUtils.getCharsetDecoder().decode(buffer).array());
					} catch (CharacterCodingException e) {
						stringValue = e.toString();
					}
					StringBuffer sb = new StringBuffer("'"); //$NON-NLS-1$
					sb.append(stringValue);
					sb.append('\'');
					return sb.toString();
				}
				else if ( CVariableFormat.DECIMAL.equals( format ) ) {
					return (isUnsigned()) ? Long.toString( value.longValue() ) : Integer.toString( value.intValue() );
				}
				else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
					StringBuffer sb = new StringBuffer( "0x" ); //$NON-NLS-1$
					String stringValue = (isUnsigned()) ? Long.toHexString( value.longValue() ) : Integer.toHexString( value.intValue() );
					sb.append( (stringValue.length() > 8) ? stringValue.substring( stringValue.length() - 8 ) : stringValue );
					return sb.toString();
				}
				else if ( CVariableFormat.BINARY.equals( format ) ) {
					StringBuffer sb = new StringBuffer( "0b" ); //$NON-NLS-1$
					String stringValue = (isUnsigned()) ? Long.toBinaryString( value.longValue() ) : Integer.toHexString( value.intValue() );
					sb.append( (stringValue.length() > 32) ? stringValue.substring( stringValue.length() - 32 ) : stringValue );
					return sb.toString();
				}
			}
		}
		return value.getValueString();
	}

	private String getBigIntegerValueString( ICDIBigIntegerValue value ) throws CDIException {
		try {
			CVariableFormat format = getParentVariable().getFormat();
			
			if (CVariableFormat.NATURAL.equals(format)) {
				format = getNaturalFormat(value, CVariableFormat.DECIMAL);
			}
			
			if ( CVariableFormat.DECIMAL.equals( format ) ) {
				BigInteger bigValue = value.bigIntegerValue();
				return bigValue.toString(10); 
			}
			else if ( CVariableFormat.HEXADECIMAL.equals( format ) ) {
				StringBuffer sb = new StringBuffer("0x"); //$NON-NLS-1$
				BigInteger bigValue = value.bigIntegerValue();
				sb.append(bigValue.toString(16));
				return sb.toString();
			}
			else if ( CVariableFormat.BINARY.equals( format ) ) {
				StringBuffer sb = new StringBuffer("0b"); //$NON-NLS-1$
				BigInteger bigValue = value.bigIntegerValue();
				sb.append(bigValue.toString(2));
				return sb.toString();
			}
		}
		catch( NumberFormatException e ) {
		}
		return null;
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
	@Override
	protected void reset() {
		resetStatus();
		fValueString = null;
		for (AbstractCVariable var : fVariables) {
			var.resetValue();
		}
	}

	@Override
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
	@Override
	protected void preserve() {
		setChanged( false );
		resetStatus();
		for (AbstractCVariable var : fVariables) {
			var.preserve();
		}
	}

	private static CVariableFormat getNaturalFormat(ICDIValue value, CVariableFormat defaultFormat) throws CDIException {
		if (value instanceof ICDIFormattable) {
			int naturalFormat = ((ICDIFormattable)value).getNaturalFormat();
			switch (naturalFormat) {
				case ICDIFormat.DECIMAL:
					return CVariableFormat.DECIMAL;
				case ICDIFormat.BINARY:
					return CVariableFormat.BINARY;
				case ICDIFormat.OCTAL:
					return CVariableFormat.OCTAL;
				case ICDIFormat.HEXADECIMAL:
					return CVariableFormat.HEXADECIMAL;
			}
		}
		return defaultFormat;
	}
}
