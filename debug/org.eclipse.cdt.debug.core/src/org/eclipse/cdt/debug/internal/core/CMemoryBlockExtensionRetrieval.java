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
package org.eclipse.cdt.debug.internal.core; 

import java.math.BigInteger;
import java.text.MessageFormat;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExpression;
import org.eclipse.cdt.debug.internal.core.model.CMemoryBlockExtension;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.cdt.debug.internal.core.model.CThread;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockExtensionRetrieval;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;

/**
 * Implements the memory retrieval features based on the CDI model.
 */
public class CMemoryBlockExtensionRetrieval implements IMemoryBlockExtensionRetrieval {

	/** 
	 * Constructor for CMemoryBlockExtensionRetrieval. 
	 */
	public CMemoryBlockExtensionRetrieval() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtensionRetrieval#getExtendedMemoryBlock(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
	 */
	public IMemoryBlockExtension getExtendedMemoryBlock( String expression, IDebugElement selected ) throws DebugException {
		String address = null;
		CExpression exp = null;
		String msg = null;
		try {
			CStackFrame frame = getStackFrame( selected );
			if ( frame != null ) {
				// We need to provide a better way for retrieving the address of expression
				ICDIExpression cdiExpression = frame.getCDITarget().createExpression( expression );
				exp = new CExpression( frame, cdiExpression, null );
				IValue value = exp.getValue();
				if ( value instanceof ICValue ) {
					ICType type = ((ICValue)value).getType();
					if ( type != null && (type.isPointer() || type.isIntegralType()) ) {
						address = value.getValueString();
						IDebugTarget target = selected.getDebugTarget();
						if ( target instanceof CDebugTarget ) {
							if ( address != null ) {
								// ???
								BigInteger a = ( address.startsWith( "0x" ) ) ? new BigInteger( address.substring( 2 ), 16 ) : new BigInteger( address ); //$NON-NLS-1$
								return new CMemoryBlockExtension( (CDebugTarget)target, expression, a );
							}
						}
					}
					else {
						msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockExtensionRetrieval.1" ), new String[] { expression } ); //$NON-NLS-1$
					}
				}
				else {
					msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockExtensionRetrieval.2" ), new String[] { expression } ); //$NON-NLS-1$
				}
			}
		}
		catch( CDIException e ) {
			msg = e.getMessage();
		}
		catch( NumberFormatException e ) {
			msg = MessageFormat.format( InternalDebugCoreMessages.getString( "CMemoryBlockExtensionRetrieval.0" ), new String[] { expression, address } ); //$NON-NLS-1$
		}
		throw new DebugException( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, msg, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock( long startAddress, long length ) throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	private CStackFrame getStackFrame( IDebugElement selected ) throws DebugException {
		if ( selected instanceof CStackFrame ) {
			return (CStackFrame)selected;
		}
		if ( selected instanceof CThread ) {
			IStackFrame frame = ((CThread)selected).getTopStackFrame();
			if ( frame instanceof CStackFrame )
				return (CStackFrame)frame;
		}
		return null;
	}
}
