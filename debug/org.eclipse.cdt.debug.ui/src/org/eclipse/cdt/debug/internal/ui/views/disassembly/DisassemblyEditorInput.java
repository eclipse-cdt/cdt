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
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import java.util.Arrays;

import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input associated with a debug element.
 */
public class DisassemblyEditorInput implements IEditorInput {

	/**
	 * A storage object used by Disassembly view.
	 */
	private static class DisassemblyStorage {

		private IDisassembly fDisassembly;
		private IAsmInstruction[] fInstructions;
		protected String fContents;
		protected long fStartAddress = 0;
		protected long fEndAddress = 0;

		/**
		 * Constructor for DisassemblyStorage.
		 */
		public DisassemblyStorage( IDisassembly disassembly, IAsmInstruction[] instructions ) {
			fDisassembly = disassembly;
			fInstructions = ( instructions != null ) ? instructions : new IAsmInstruction[0];
			initializeAddresses();
			createContent();
		}

		public String getContents() {
			return fContents;
		}

		public IDisassembly getDisassembly() {
			return this.fDisassembly;
		}

		public boolean containsAddress( long address ) {
			return (address >= fStartAddress && address <= fEndAddress);
		}

		private void createContent() {
			StringBuffer lines = new StringBuffer();
			int maxFunctionName = 0;
			int maxOpcodeLength = 0;
			long maxOffset = 0;
			for( int i = 0; i < fInstructions.length; ++i ) {
				String functionName = fInstructions[i].getFunctionName();
				if ( functionName.length() > maxFunctionName ) {
					maxFunctionName = functionName.length();
				}
				String opcode = fInstructions[i].getOpcode();
				if ( opcode.length() > maxOpcodeLength )
					maxOpcodeLength = opcode.length();
				if ( fInstructions[i].getOffset() > maxOffset ) {
					maxOffset = fInstructions[i].getOffset();
				}
			}
			int instrPos = calculateInstructionPosition( maxFunctionName, maxOffset );
			int argPosition = instrPos + maxOpcodeLength + 1;
			for( int i = 0; i < fInstructions.length; ++i ) {
				lines.append( getInstructionString( fInstructions[i], instrPos, argPosition ) );
			}
			fContents = lines.toString();
		}

		private String getInstructionString( IAsmInstruction instruction, int instrPosition, int argPosition ) {
			int worstCaseSpace = Math.max( instrPosition, argPosition );
			char[] spaces = new char[worstCaseSpace];
			Arrays.fill( spaces, ' ' );
			StringBuffer sb = new StringBuffer();
			if ( instruction != null ) {
				sb.append( CDebugUIUtils.toHexAddressString( instruction.getAdress() ) );
				sb.append( ' ' );
				String functionName = instruction.getFunctionName();
				if ( functionName != null && functionName.length() > 0 ) {
					sb.append( '<' );
					sb.append( functionName );
					if ( instruction.getOffset() != 0 ) {
						sb.append( '+' );
						sb.append( instruction.getOffset() );
					}
					sb.append( ">:" ); //$NON-NLS-1$
					sb.append( spaces, 0, instrPosition - sb.length() );
				}
				sb.append( instruction.getOpcode() );
				sb.append( spaces, 0, argPosition - sb.length() );
				sb.append( instruction.getArguments() );
				sb.append( '\n' );
			}
			return sb.toString();
		}

		private int calculateInstructionPosition( int maxFunctionName, long maxOffset ) {
			return (16 + maxFunctionName + Long.toString( maxOffset ).length());
		}

		private void initializeAddresses() {
			if ( fInstructions.length > 0 ) {
				fStartAddress = fInstructions[0].getAdress();
				fEndAddress = fInstructions[fInstructions.length - 1].getAdress();
			}
		}

		public int getLineNumber( long address ) {
			for( int i = 0; i < fInstructions.length; ++i ) {
				if ( fInstructions[i].getAdress() == address ) {
					return i + 1;
				}
			}
			return 0;
		}
	}

	public static final IEditorInput EMPTY_EDITOR_INPUT = new DisassemblyEditorInput();

	public static final IEditorInput PENDING_EDITOR_INPUT = 
		new DisassemblyEditorInput() {
				public String getContents() {
					return DisassemblyMessages.getString( "DisassemblyDocumentProvider.Pending_1" ); //$NON-NLS-1$
				}
			};
	
	/**
	 * Storage associated with this editor input
	 */
	private DisassemblyStorage fStorage;

	/**
	 * Constructor for DisassemblyEditorInput.
	 */
	protected DisassemblyEditorInput() {
	}

	/**
	 * Constructor for DisassemblyEditorInput.
	 * 
	 * @param disassembly
	 * @param instructions
	 */
	public DisassemblyEditorInput( IDisassembly disassembly, IAsmInstruction[] instructions ) {
		fStorage = new DisassemblyStorage( disassembly, instructions );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		return null;
	}

	public boolean contains( ICStackFrame frame ) {
		if ( fStorage != null ) {
			return fStorage.containsAddress( frame.getAddress() );
		}
		return false;
	}

	public String getContents() {
		return ( fStorage != null ) ? fStorage.getContents() : ""; //$NON-NLS-1$
	}

	public int getLineNumber( long address ) {
		return ( fStorage != null ) ? fStorage.getLineNumber( address ) : 0;
	}
}
