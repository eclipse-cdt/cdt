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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.IAsmSourceLine;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IDisassemblyBlock;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * CDI-based implementation of <code>IDisassemblyBlock</code>.
 */
public class DisassemblyBlock implements IDisassemblyBlock, IAdaptable {

	private IDisassembly fDisassembly;
	
	private IAsmSourceLine[] fSourceLines;

	private IAddress fStartAddress = null;

	private IAddress fEndAddress = null;
	
	private boolean fMixedMode = false;

	/**
	 * Constructor for DisassemblyBlock.
	 */
	private DisassemblyBlock( IDisassembly disassembly ) {
		fDisassembly = disassembly;
	}

	public static DisassemblyBlock create( IDisassembly disassembly, ICDIMixedInstruction[] instructions ) {
		DisassemblyBlock block = new DisassemblyBlock( disassembly );
		block.setMixedMode( true );
		ISourceLocator locator = disassembly.getDebugTarget().getLaunch().getSourceLocator();
		IAddressFactory factory = ((CDebugTarget)disassembly.getDebugTarget()).getAddressFactory();
		block.setSourceLines( createSourceLines( factory, locator, instructions ) );
		block.initializeAddresses();
		return block;
	}

	public static DisassemblyBlock create( IDisassembly disassembly, ICDIInstruction[] instructions ) {
		DisassemblyBlock block = new DisassemblyBlock( disassembly );
		IAddressFactory factory = ((CDebugTarget)disassembly.getDebugTarget()).getAddressFactory();
		block.setMixedMode( false );
		block.setSourceLines( createSourceLines( factory, instructions ) );
		block.initializeAddresses();
		return block;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassemblyBlock#getDisassembly()
	 */
	public IDisassembly getDisassembly() {
		return fDisassembly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassemblyBlock#getModuleFile()
	 */
	public String getModuleFile() {
		IDisassembly d = getDisassembly();
		if ( d != null ) {
			IExecFileInfo info = (IExecFileInfo)d.getAdapter( IExecFileInfo.class );
			if ( info != null && info.getExecFile() != null ) {
				return info.getExecFile().getPath().toOSString();
			}
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassemblyBlock#contains(org.eclipse.cdt.debug.core.model.ICStackFrame)
	 */
	public boolean contains( ICStackFrame frame ) {
		if ( !getDisassembly().getDebugTarget().equals( frame.getDebugTarget() ) )
			return false;
		if ( fStartAddress == null || fEndAddress == null )
			return false;
		IAddress address = frame.getAddress();
		return (address.compareTo( fStartAddress ) >= 0 && address.compareTo( fEndAddress ) <= 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IDisassemblyBlock#getSourceLines()
	 */
	public IAsmSourceLine[] getSourceLines() {
		return fSourceLines;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IDisassemblyBlock#isMixedMode()
	 */
	public boolean isMixedMode() {
		return fMixedMode;
	}

	public void dispose() {
	}

	private static IAsmSourceLine[] createSourceLines( IAddressFactory factory, ISourceLocator locator, ICDIMixedInstruction[] mi ) {
		IAsmSourceLine[] result = new IAsmSourceLine[mi.length];
		LineNumberReader reader = null;
		if ( result.length > 0 && locator != null ) {
			String fileName = mi[0].getFileName();
			Object element = null;
			if ( locator instanceof ISourceLookupDirector ) {
				element = ((ISourceLookupDirector)locator).getSourceElement( fileName );
			}
			if ( locator instanceof ICSourceLocator ) {
				element = ((ICSourceLocator)locator).findSourceElement( fileName );
			}
			File file= null;
			if ( element instanceof IFile ) {
				file = ((IFile)element).getLocation().toFile();
			}
			else if ( element instanceof IStorage ) {
				file = ((IStorage)element).getFullPath().toFile();
			}
			if ( file != null ) {
				try {
					reader = new LineNumberReader( new FileReader( file ) );
				}
				catch( FileNotFoundException e ) {
				}				
			}
		}
		for ( int i = 0; i < result.length; ++i ) {
			String text = null;
			if ( reader != null ) {
				int lineNumber = mi[i].getLineNumber();
				while( reader.getLineNumber() + 1 < lineNumber ) {
					try {
						reader.readLine();
					}
					catch( IOException e ) {
					}
				}
				if ( reader.getLineNumber() + 1 == lineNumber ) {
					try {
						text = reader.readLine() + '\n';
					}
					catch( IOException e ) {
					}
				}
			}
			result[i] = new AsmSourceLine( factory, text, mi[i].getInstructions() );
		}
		return result;
	}

	private static IAsmSourceLine[] createSourceLines( IAddressFactory factory, ICDIInstruction[] instructions ) {
		return new IAsmSourceLine[] { new AsmSourceLine( factory, "", instructions ) }; //$NON-NLS-1$
	}

	private void initializeAddresses() {
		for ( int i = 0; i < fSourceLines.length; ++i ) {
			IAsmInstruction[] instr = fSourceLines[i].getInstructions();
			if ( instr.length > 0 ) {
				if ( fStartAddress == null )
					fStartAddress = instr[0].getAdress();
				fEndAddress = instr[instr.length - 1].getAdress();
			}
		}
	}

	private void setMixedMode( boolean mixedMode ) {
		this.fMixedMode = mixedMode;
	}

	private void setSourceLines( IAsmSourceLine[] sourceLines ) {
		this.fSourceLines = sourceLines;
	}
}
