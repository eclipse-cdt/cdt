/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.internal.core.DisassemblyStorage;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Enter type comment.
 * 
 * @since: Oct 8, 2002
 */
public class DisassemblyManager
{
	// move to preferences
	final static private int DISASSEMBLY_MAX_LINE_COUNT = 100;
	final static private int DISASSEMBLY_BLOCK_SIZE = 100;
	
	private CDebugTarget fDebugTarget;
	private DisassemblyStorage fStorage = null;

	/**
	 * Constructor for DisassemblyManager.
	 */
	public DisassemblyManager( CDebugTarget target )
	{
		setDebugTarget( target );
	}

	public int getLineNumber( IStackFrameInfo frameInfo )
	{
		DisassemblyStorage storage = getSourceElement( frameInfo );
		if ( storage != null )
		{
			return storage.getLineNumber( frameInfo.getAddress() );
		}
		return 0;
	}

	public Object getSourceElement( IStackFrame stackFrame )
	{
		if ( stackFrame != null )
		{
			return getSourceElement( (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class ) );
		}
		return null;
	}
	
	private void setDebugTarget( CDebugTarget target )
	{
		fDebugTarget = target;
	}
	
	public CDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}
	
	private void setDisassemblyStorage( DisassemblyStorage ds )
	{
		fStorage = ds;
	}
	
	protected DisassemblyStorage getDisassemblyStorage()
	{
		return fStorage;
	}
	
	private DisassemblyStorage getSourceElement( IStackFrameInfo frameInfo )
	{
		DisassemblyStorage storage = null;
		if ( frameInfo != null )
		{
			long address = frameInfo.getAddress();
			if ( getDisassemblyStorage() != null && getDisassemblyStorage().containsAddress( address ) )
			{
				storage = getDisassemblyStorage();
			}
			else
			{
				storage = loadDisassemblyStorage( frameInfo );
			}			
		}
		return storage;
	}
	
	private DisassemblyStorage loadDisassemblyStorage( IStackFrameInfo frameInfo )
	{
		setDisassemblyStorage( null );
		if ( frameInfo != null && getDebugTarget() != null && getDebugTarget().isSuspended() )
		{
			ICDISourceManager sm = getDebugTarget().getCDISession().getSourceManager();
			if ( sm != null )
			{
				String fileName = frameInfo.getFile();
				int lineNumber = frameInfo.getFrameLineNumber();
				ICDIInstruction[] instructions = new ICDIInstruction[0];
				if ( fileName != null && fileName.length() > 0 )
				{
					try
					{
						instructions = sm.getInstructions( fileName, lineNumber, DISASSEMBLY_MAX_LINE_COUNT );
					}
					catch( CDIException e )
					{
					}
				}
				if ( instructions.length == 0 )
				{
					long address = frameInfo.getAddress();
					if ( address >= 0 )
					{
						try
						{
							instructions = getFunctionInstructions( sm.getInstructions( address, address + DISASSEMBLY_BLOCK_SIZE ) );
						}
						catch( CDIException e )
						{
							CDebugCorePlugin.log( e );
						}
					}
				}
				if ( instructions.length > 0 )
				{
					setDisassemblyStorage( new DisassemblyStorage( getDebugTarget(), instructions ) );
				}
			}
		}
		return getDisassemblyStorage();
	}
	
	private ICDIInstruction[] getFunctionInstructions( ICDIInstruction[] rawInstructions )
	{
		if ( rawInstructions.length > 0 && 
			 rawInstructions[0].getFuntionName() != null &&
			 rawInstructions[0].getFuntionName().length() > 0 )
		{
			ArrayList list = new ArrayList( rawInstructions.length );
			list.add( rawInstructions[0] );
			for ( int i = 1; i < rawInstructions.length; ++i )
			{
				if ( rawInstructions[0].getFuntionName().equals( rawInstructions[i].getFuntionName() ) )
				{
					list.add( rawInstructions[i] );
				}
			}
			return (ICDIInstruction[])list.toArray( new ICDIInstruction[list.size()] );
		}
		return rawInstructions;
	}
}
