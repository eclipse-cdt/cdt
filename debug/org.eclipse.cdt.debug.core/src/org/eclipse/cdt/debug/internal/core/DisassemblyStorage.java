/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.sourcelookup.IDisassemblyStorage;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Enter type comment.
 * 
 * @since: Oct 8, 2002
 */
public class DisassemblyStorage implements IDisassemblyStorage
{
	protected ICDIInstruction[] fInstructions;
	protected IDebugTarget fDebugTarget;
	protected ByteArrayInputStream fInputStream = null;
	protected long fStartAddress = 0;
	protected long fEndAddress = 0;
	
	/**
	 * Constructor for DisassemblyStorage.
	 */
	public DisassemblyStorage( IDebugTarget target, ICDIInstruction[] instructions )
	{
		setDebugTarget( target );
		setInstructions( instructions );
		initializeAddresses();
		createContent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IDisassemblyStorage#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IDisassemblyStorage#containsAddress(Long)
	 */
	public boolean containsAddress( long address )
	{
		return ( address >= fStartAddress && address <= fEndAddress );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.IDisassemblyStorage#getLineNumber(Long)
	 */
	public int getLineNumber( long address )
	{
		for ( int i = 0; i < fInstructions.length; ++i )
		{
			if ( fInstructions[i].getAdress() == address )
			{
				return i + 1;
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException
	{
		if ( fInputStream != null )
			fInputStream.reset();
		return fInputStream;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getFullPath()
	 */
	public IPath getFullPath()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#getName()
	 */
	public String getName()
	{
		try
		{
			if ( getDebugTarget() != null )
			{ 
				return  getDebugTarget().getName();
			}
		}
		catch( DebugException e )
		{
			// ignore
		}
		return "disassembly";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IStorage#isReadOnly()
	 */
	public boolean isReadOnly()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( IStorage.class ) )
			return this;
		if ( adapter.equals( IDisassemblyStorage.class ) )
			return this;
		if ( adapter.equals( DisassemblyStorage.class ) )
			return this;
		if ( adapter.equals( IResource.class ) )
			return getBinary();
		return null;
	}

	protected void setDebugTarget( IDebugTarget target )
	{
		fDebugTarget = target;
	}
	
	protected void setInstructions( ICDIInstruction[] intructions )
	{
		fInstructions = intructions;
	}

	private void createContent()
	{
		StringBuffer lines = new StringBuffer();
		int maxFunctionName = 0;
		long maxOffset = 0;
		for ( int i = 0; i < fInstructions.length; ++i )
		{
			if ( fInstructions[i].getFuntionName().length() > maxFunctionName )
			{
				maxFunctionName = fInstructions[i].getFuntionName().length();
			}
			if ( fInstructions[i].getOffset() > maxOffset )
			{
				maxOffset = fInstructions[i].getOffset();
			}
		}
		int instrPos = calculateInstructionPosition( maxFunctionName, maxOffset );		
		for ( int i = 0; i < fInstructions.length; ++i )
		{
			lines.append( getInstructionString( fInstructions[i], instrPos ) );
		}
		fInputStream = new ByteArrayInputStream( lines.toString().getBytes() );
	}

	private void initializeAddresses()
	{
		if ( fInstructions.length > 0 )
		{
			fStartAddress = fInstructions[0].getAdress();
			fEndAddress = fInstructions[fInstructions.length - 1].getAdress();
		}
	}
	
	private String getInstructionString( ICDIInstruction instruction, int instrPosition )
	{
		char[] spaces= new char[instrPosition];
		Arrays.fill( spaces, ' ' );
		StringBuffer sb = new StringBuffer();
		if ( instruction != null )
		{
			sb.append( CDebugUtils.toHexAddressString( instruction.getAdress() ) );
			sb.append( ' ' );
			if ( instruction.getFuntionName() != null && instruction.getFuntionName().length() > 0 )
			{
				sb.append( '<' );
				sb.append( instruction.getFuntionName() );
				if ( instruction.getOffset() != 0 )
				{
					sb.append( '+' );
					sb.append( instruction.getOffset() );
				}
				sb.append( ">:" );
				sb.append( spaces, 0, instrPosition - sb.length() );
			}
			sb.append( instruction.getInstruction() );
			sb.append( '\n' );
		}
		return sb.toString();
	}
	
	private int calculateInstructionPosition( int maxFunctionName, long maxOffset )
	{
		return ( 16 + maxFunctionName + Long.toString( maxOffset ).length() );
	}
	
	private IResource getBinary()
	{
		if ( getDebugTarget() != null )
		{
			IExecFileInfo info = (IExecFileInfo)getDebugTarget().getAdapter( IExecFileInfo.class );
			if ( info != null )
			{
				return info.getExecFile();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.IDisassemblyStorage#getAddress(int)
	 */
	public long getAddress( int lineNumber )
	{
		if ( fInstructions.length > lineNumber && lineNumber >= 0 )
		{
			return fInstructions[lineNumber].getAdress();
		}
		return 0;
	}
}
