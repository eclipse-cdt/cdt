/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import org.eclipse.cdt.debug.core.IStackFrameInfo;
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
		return 0;
	}

	public Object getSourceElement( IStackFrame stackFrame )
	{
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
}
