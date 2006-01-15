/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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

	private Object fSourceElement;

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
		ISourceLocator locator = disassembly.getDebugTarget().getLaunch().getSourceLocator();
		IAddressFactory factory = ((CDebugTarget)disassembly.getDebugTarget()).getAddressFactory();
		block.initialize( factory, locator, instructions );
		return block;
	}

	public static DisassemblyBlock create( IDisassembly disassembly, ICDIInstruction[] instructions ) {
		DisassemblyBlock block = new DisassemblyBlock( disassembly );
		IAddressFactory factory = ((CDebugTarget)disassembly.getDebugTarget()).getAddressFactory();
		block.initialize( factory, instructions );
		return block;
	}

	private void initialize( IAddressFactory factory, ICDIInstruction[] instructions ) {
		setMixedMode( false );
		createSourceLines( factory, instructions );
		initializeAddresses();
	}

	private void initialize( IAddressFactory factory, ISourceLocator locator, ICDIMixedInstruction[] mi ) {
		setMixedMode( true );
		createSourceLines( factory, locator, mi );
		initializeAddresses();
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
	 * @see org.eclipse.cdt.debug.core.model.IDisassemblyBlock#getSourceElement()
	 */
	public Object getSourceElement() {
		return fSourceElement;
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

	private void createSourceLines( IAddressFactory factory, ISourceLocator locator, ICDIMixedInstruction[] mi ) {
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
			fSourceElement = element;
			File file = null;
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
			boolean failed = false;
			int lineNumber = mi[i].getLineNumber();
			if ( reader != null ) {
				while( reader.getLineNumber() + 1 < lineNumber ) {
					try {
						if ( reader.readLine() == null ) {
							// break if the end of file is reached (see bug #123745)
							failed = true;
							break;
						}
					}
					catch( IOException e ) {
					}
				}
				if ( !failed && reader.getLineNumber() + 1 == lineNumber ) {
					try {
						text = reader.readLine() + '\n';
					}
					catch( IOException e ) {
					}
				}
			}
			result[i] = new AsmSourceLine( factory, text, lineNumber, mi[i].getInstructions() );
		}
		fSourceLines = result;
	}

	private void createSourceLines( IAddressFactory factory, ICDIInstruction[] instructions ) {
		fSourceLines = new IAsmSourceLine[] { new AsmSourceLine( factory, "", instructions ) }; //$NON-NLS-1$
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
}
