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
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExtendedMemoryBlock;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.cdt.debug.internal.core.model.CThread;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlockRetrieval;

/**
 * Implements the memory retrieval features based on the CDI model.
 */
public class CExtendedMemoryBlockRetrieval implements IExtendedMemoryBlockRetrieval {

	/** 
	 * Constructor for CExtendedMemoryBlockRetrieval. 
	 */
	public CExtendedMemoryBlockRetrieval() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlockRetrieval#getExtendedMemoryBlock(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
	 */
	public IExtendedMemoryBlock getExtendedMemoryBlock( String expression, IDebugElement selected ) throws DebugException {
		String address = null;
		if ( selected instanceof CStackFrame ) {
			address = ((CStackFrame)selected).evaluateExpressionToString( expression );
		}
		else if ( selected instanceof CThread ) {
			IStackFrame frame = ((CThread)selected).getTopStackFrame();
			if ( frame instanceof CStackFrame ) {
				address = ((CStackFrame)selected).evaluateExpressionToString( expression );
			}
		}
		IDebugTarget target = selected.getDebugTarget();
		if ( target instanceof CDebugTarget ) {
			if ( address == null ) {
				address = ((CDebugTarget)target).evaluateExpressionToString( expression );
			}
			if ( address != null ) {
				try {
					BigInteger a = ( address.startsWith( "0x" ) ) ? new BigInteger( address.substring( 2 ), 16 ) : new BigInteger( address ); //$NON-NLS-1$
					return new CExtendedMemoryBlock( (CDebugTarget)target, expression, a );
				}
				catch( NumberFormatException e ) {
					throw new DebugException( new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, MessageFormat.format( InternalDebugCoreMessages.getString( "CExtendedMemoryBlockRetrieval.0" ), new String[] { expression, address } ), null ) ); //$NON-NLS-1$
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.memory.IExtendedMemoryBlockRetrieval#getPaddedString()
	 */
	public String getPaddedString() {
		// TODO Auto-generated method stub
		return null;
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
}
