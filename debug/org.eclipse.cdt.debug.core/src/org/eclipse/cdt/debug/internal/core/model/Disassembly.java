/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.debug.core.DebugException;

/**
 * CDI implementation of IDisassembly 
 */
public class Disassembly extends CDebugElement implements IDisassembly {

	final static private int DISASSEMBLY_BLOCK_SIZE = 100;

	private IAsmInstruction[] fInstructions = new IAsmInstruction[0];

	/**
	 * Constructor for Disassembly.
	 *
	 * @param target
	 */
	public Disassembly( CDebugTarget target ) {
		super( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassembly#getInstructions(org.eclipse.cdt.debug.core.model.ICStackFrame)
	 */
	public IAsmInstruction[] getInstructions( ICStackFrame frame ) throws DebugException {
		long address = frame.getAddress();
		if ( !containsAddress( address ) ) {
			loadInstructions( frame );
		}
		return fInstructions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassembly#getInstructions(long, int)
	 */
	public IAsmInstruction[] getInstructions( long address, int length ) throws DebugException {
		if ( !containsAddress( address ) ) {
			loadInstructions( address, length );
		}
		return fInstructions;
	}

	private boolean containsAddress( long address ) {
		for ( int i = 0; i < fInstructions.length; ++i ) {
			if ( fInstructions[i].getAdress() == address )
				return true;
		}
		return false;
	}

	private boolean containsAddress( ICDIInstruction[] instructions, long address ) {
		for( int i = 0; i < instructions.length; ++i ) {
			if ( instructions[i].getAdress() == address )
				return true;
		}
		return false;
	}

	private void loadInstructions( ICStackFrame frame ) throws DebugException {
		fInstructions = new IAsmInstruction[0];
		ICDISession session = (ICDISession)getDebugTarget().getAdapter( ICDISession.class );
		if ( session != null ) {
			ICDISourceManager sm = session.getSourceManager();
			if ( sm != null ) {
				String fileName = frame.getFile();
				int lineNumber = frame.getLineNumber();
				ICDIInstruction[] instructions = new ICDIInstruction[0];
				long address = frame.getAddress();				
				if ( fileName != null && fileName.length() > 0 ) {
					try {
						instructions = sm.getInstructions( fileName, 
														   lineNumber, 
														   CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_MAX_NUMBER_OF_INSTRUCTIONS ) );
					}
					catch( CDIException e ) {
						targetRequestFailed( CoreModelMessages.getString( "Disassembly.Unable_to_get_disassembly_instructions_1" ), e ); //$NON-NLS-1$
					}
				}
				if ( instructions.length == 0 ||
				// Double check if debugger returns correct address range.
						!containsAddress( instructions, address ) ) {
					if ( address >= 0 ) {
						try {
							instructions = getFunctionInstructions( sm.getInstructions( address, address + DISASSEMBLY_BLOCK_SIZE ) );
						}
						catch( CDIException e ) {
							targetRequestFailed( CoreModelMessages.getString( "Disassembly.Unable_to_get_disassembly_instructions_2" ), e ); //$NON-NLS-1$
						}
					}
				}
				fInstructions = new IAsmInstruction[instructions.length];
				for ( int i = 0; i < fInstructions.length; ++i ) {
					fInstructions[i] = new AsmInstruction( instructions[i] );
				}
			}
		}
	}

	private void loadInstructions( long address, int length ) throws DebugException {
		fInstructions = new IAsmInstruction[0];
		
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
		fInstructions = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( IExecFileInfo.class.equals( adapter ) )
			return getDebugTarget().getAdapter( adapter );
		return super.getAdapter( adapter );
	}
}
