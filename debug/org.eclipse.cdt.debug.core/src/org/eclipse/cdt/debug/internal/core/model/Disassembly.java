/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     oyvind.harboe@zylin.com - http://bugs.eclipse.org/250638
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.math.BigInteger;
import java.util.ArrayList;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

/**
 * CDI implementation of IDisassembly 
 */
public class Disassembly extends CDebugElement implements IDisassembly, ICDIEventListener {

	final static private int DISASSEMBLY_BLOCK_SIZE = 100;

	private DisassemblyBlock[] fBlocks = new DisassemblyBlock[1];

	/**
	 * Constructor for Disassembly.
	 *
	 * @param target
	 */
	public Disassembly( CDebugTarget target ) {
		super( target );
		getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassembly#getDisassemblyBlock(org.eclipse.cdt.debug.core.model.ICStackFrame)
	 */
	@Override
	public IDisassemblyBlock getDisassemblyBlock( ICStackFrame frame ) throws DebugException {
		if ( fBlocks[0] == null || !fBlocks[0].contains( frame ) ) {
			fBlocks[0] = createBlock( frame );
		}
		return fBlocks[0];		
	}

	private DisassemblyBlock createBlock( ICStackFrame frame ) throws DebugException {
		ICDITarget target = getDebugTarget().getAdapter( ICDITarget.class );
		if ( target != null ) {
			String fileName = frame.getFile();
			int lineNumber = frame.getLineNumber();
			ICDIMixedInstruction[] mixedInstrs = new ICDIMixedInstruction[0];
			IAddress address = frame.getAddress();
			if (address==null)
				return null;
			if ( fileName != null && fileName.length() > 0 ) {
				try {
					mixedInstrs = target.getMixedInstructions( fileName, 
							lineNumber, 
							CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_MAX_NUMBER_OF_INSTRUCTIONS ) );
				}
				catch( CDIException e ) {
					// ignore and try to get disassembly without source
				}
			}
			// Double check if debugger returns correct address range.
			if ( mixedInstrs.length == 0 ||
					!containsAddress( mixedInstrs, address ) ) {
				try {
					BigInteger addr = new BigInteger( address.toString() );
					ICDIInstruction[] instructions = getFunctionInstructions( target.getInstructions( addr, addr.add( BigInteger.valueOf( DISASSEMBLY_BLOCK_SIZE ) ) ) );
					return DisassemblyBlock.create( this, instructions );
				}
				catch( CDIException e ) {
					targetRequestFailed( e.getMessage(), e );
				}
			}
			else {
				return DisassemblyBlock.create( this, mixedInstrs );
			}
		}
		return null;
	}
	
	@Override
	public IDisassemblyBlock getDisassemblyBlock( IAddress address ) throws DebugException {
		fBlocks[0] = createBlock( address, null);
		return fBlocks[0];		
	}

	public IDisassemblyBlock getDisassemblyBlock( IAddress startAddress, IAddress endAddress ) throws DebugException {
		fBlocks[0] = createBlock( startAddress, endAddress );
		return fBlocks[0];		
	}
	
	private DisassemblyBlock createBlock( IAddress startAddress, IAddress endAddress) throws DebugException {
		ICDITarget target = getDebugTarget().getAdapter( ICDITarget.class );
		if ( target != null ) {
			ICDIMixedInstruction[] mixedInstrs = new ICDIMixedInstruction[0];
			if ( mixedInstrs.length == 0 ||
					!containsAddress( mixedInstrs, startAddress ) ) {
				try {
					BigInteger startAddr = new BigInteger( startAddress.toString() );
					BigInteger endAddr = null;
					if (endAddress != null) {
						endAddr = new BigInteger( endAddress.toString() );
					} else {
						endAddr = startAddr.add( BigInteger.valueOf( 
								CDebugCorePlugin.getDefault().getPluginPreferences().getInt(ICDebugConstants.PREF_MAX_NUMBER_OF_INSTRUCTIONS)));
					}
					mixedInstrs = target.getMixedInstructions( startAddr, endAddr);
					return DisassemblyBlock.create( this, mixedInstrs );
				}
				catch( CDIException e ) {
					targetRequestFailed( e.getMessage(), e );
				}
			}
			else {
				return DisassemblyBlock.create( this, mixedInstrs );
			}
		}
		return null;
	}


	private boolean containsAddress( ICDIMixedInstruction[] mi, IAddress address ) {
		for( int i = 0; i < mi.length; ++i ) {
			ICDIInstruction[] instructions = mi[i].getInstructions();
			for ( int j = 0; j < instructions.length; ++j ) {
				if ( address.getValue().equals( instructions[j].getAdress() ) )
					return true;
			}
		}
		return false;
	}

	private ICDIInstruction[] getFunctionInstructions( ICDIInstruction[] rawInstructions ) {
		if ( rawInstructions.length > 0 && rawInstructions[0].getFuntionName() != null && rawInstructions[0].getFuntionName().length() > 0 ) {
			ArrayList list = new ArrayList( rawInstructions.length );
			list.add( rawInstructions[0] );
			for( int i = 1; i < rawInstructions.length; ++i ) {
				if ( rawInstructions[0].getFuntionName().equals( rawInstructions[i].getFuntionName() ) ) {
					list.add( rawInstructions[i] );
				}
			}
			return (ICDIInstruction[])list.toArray( new ICDIInstruction[list.size()] );
		}
		return rawInstructions;
	}

	public void dispose() {
		getCDISession().getEventManager().removeEventListener( this );
        CDebugCorePlugin.getDefault().getDisassemblyContextService().unregister( this );
		for ( int i = 0; i < fBlocks.length; ++i )
			if ( fBlocks[i] != null ) {
				fBlocks[i].dispose();
				fBlocks[i] = null;
			}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter( Class adapter ) {
		if ( IExecFileInfo.class.equals( adapter ) )
			return getDebugTarget().getAdapter( adapter );
		return super.getAdapter( adapter );
	}

	public void reset() {
		for ( int i = 0; i < fBlocks.length; ++i )
			if ( fBlocks[i] != null ) {
				fBlocks[i].dispose();
				fBlocks[i] = null;
			}
		fireChangeEvent( DebugEvent.CONTENT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassembly#getAddressFactory()
	 */
	@Override
	public IAddressFactory getAddressFactory() {
		return ((CDebugTarget)getDebugTarget()).getAddressFactory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	@Override
	public void handleDebugEvents( ICDIEvent[] events ) {
		boolean update = false;
		for ( int i = 0; i < events.length; ++i ) {
			if ( events[i] instanceof ICDIMemoryChangedEvent ) {
				BigInteger[] addresses = ((ICDIMemoryChangedEvent)events[i]).getAddresses();
				for ( int j = 0; j < addresses.length; ++j ) {
					IAddress address = getAddressFactory().createAddress( addresses[j] );
					for ( int k = 0; k < fBlocks.length; ++k ) {
						if ( fBlocks[k] != null && fBlocks[k].contains( address ) ) {
							update = true;
							break;
						}
					}
				}
			}
		}
		if ( update )
			reset();
	}
}
