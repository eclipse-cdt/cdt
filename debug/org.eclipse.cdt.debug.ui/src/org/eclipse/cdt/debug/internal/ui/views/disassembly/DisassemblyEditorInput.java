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
import org.eclipse.cdt.debug.core.model.IAsmSourceLine;
import org.eclipse.cdt.debug.core.model.IBreakpointTarget;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Editor input associated with a debug element.
 */
public class DisassemblyEditorInput implements IEditorInput {

	public static final IEditorInput EMPTY_EDITOR_INPUT = new DisassemblyEditorInput();

	public static final IEditorInput PENDING_EDITOR_INPUT = 
		new DisassemblyEditorInput() {
				public String getContents() {
					return DisassemblyMessages.getString( "DisassemblyDocumentProvider.Pending_1" ); //$NON-NLS-1$
				}
			};
	
	/**
	 * Disassembly block associated with this editor input
	 */
	private IDisassemblyBlock fBlock;

	private String fContents = ""; //$NON-NLS-1$
	
	private IRegion[] fSourceRegions = new IRegion[0];

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
	private DisassemblyEditorInput( IDisassemblyBlock block ) {
		fBlock = block;
		createContents();
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
		if ( fBlock != null ) {
			return fBlock.contains( frame );
		}
		return false;
	}

	public String getContents() {
		return fContents;
	}

	public int getInstructionLine( long address ) {
		if ( fBlock != null ) {
			IAsmSourceLine[] lines = fBlock.getSourceLines();
			int result = 0;
			for ( int i = 0; i < lines.length; ++i ) {
				IAsmInstruction[] instructions = lines[i].getInstructions();
				++result;
				for ( int j = 0; j < instructions.length; ++j ) {
					++result;
					if ( instructions[j].getAdress() == address ) {
						return result;
					}
				}
			}
		}
		return 0;
	}

	public int getInstructionLine( ICLineBreakpoint breakpoint ) {
		if ( fBlock != null ) {
			IDisassembly dis = fBlock.getDisassembly();
			if ( dis != null ) {
				IBreakpointTarget bt = (IBreakpointTarget)dis.getDebugTarget().getAdapter( IBreakpointTarget.class );
				if ( bt != null ) {
					try {
						long address = bt.getBreakpointAddress( breakpoint );
						if ( address != 0 )
							return getInstructionLine( address );
					}
					catch( DebugException e ) {
					}
				}
			}
		}
		return 0;
	}

	public long getAddress( int lineNumber ) {
		if ( fBlock != null ) {
			IAsmSourceLine[] lines = fBlock.getSourceLines();
			int current = 0;
			for ( int i = 0; i < lines.length; ++i ) {
				IAsmInstruction[] instructions = lines[i].getInstructions();
				++current;
				if ( lineNumber == current && instructions.length > 0 )
					return instructions[0].getAdress();
				if ( lineNumber > current && lineNumber <= current + instructions.length )
					return instructions[lineNumber - current - 1].getAdress();
				current += instructions.length;
			}
		}
		return 0;
	}

	public String getModuleFile() {
		return ( fBlock != null ) ? fBlock.getModuleFile() : null;
	}

	public static DisassemblyEditorInput create( ICStackFrame frame ) throws DebugException {
		DisassemblyEditorInput input = null;
		IDisassembly disassembly = ((ICDebugTarget)frame.getDebugTarget()).getDisassembly();
		if ( disassembly != null ) {
			IDisassemblyBlock block = disassembly.getDisassemblyBlock( frame );
			input = new DisassemblyEditorInput( block );
		}
		return input;
	}

	private void createContents() {
		fSourceRegions = new IRegion[0];
		StringBuffer lines = new StringBuffer();
		int maxFunctionName = 0;
		int maxOpcodeLength = 0;
		long maxOffset = 0;
		if ( fBlock != null ) {
			IAsmSourceLine[] mi = fBlock.getSourceLines();
			for ( int j = 0; j < mi.length; ++j ) {
				IAsmInstruction[] instructions = mi[j].getInstructions();
				for( int i = 0; i < instructions.length; ++i ) {
					String functionName = instructions[i].getFunctionName();
					if ( functionName.length() > maxFunctionName ) {
						maxFunctionName = functionName.length();
					}
					String opcode = instructions[i].getOpcode();
					if ( opcode.length() > maxOpcodeLength )
						maxOpcodeLength = opcode.length();
					if ( instructions[i].getOffset() > maxOffset ) {
						maxOffset = instructions[i].getOffset();
					}
				}
			}
			int instrPos = calculateInstructionPosition( maxFunctionName, maxOffset );
			int argPosition = instrPos + maxOpcodeLength + 1;
			if ( fBlock.isMixedMode() )
				fSourceRegions = new IRegion[mi.length]; 
			for ( int j = 0; j < mi.length; ++j ) {
				if ( fBlock.isMixedMode() ) {
					String sl = getSourceLineString( mi[j] );
					fSourceRegions[j] = new Region( lines.length(), sl.length() );
					lines.append( sl );
				}
				IAsmInstruction[] instructions = mi[j].getInstructions();
				for( int i = 0; i < instructions.length; ++i ) {
					lines.append( getInstructionString( instructions[i], instrPos, argPosition ) );
				}
			}
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

	private String getSourceLineString( IAsmSourceLine line ) {
		String text = line.toString();
		if ( text == null ) {
			text = DisassemblyMessages.getString( "DisassemblyEditorInput.source_line_is_not_available_1" ) + '\n'; //$NON-NLS-1$
		}
		return text;
	}

	public IRegion[] getSourceRegions() {
		return this.fSourceRegions;
	}
	
	protected IDisassembly getDisassembly() {
		return ( fBlock != null ) ? fBlock.getDisassembly() : null;
	}
}
