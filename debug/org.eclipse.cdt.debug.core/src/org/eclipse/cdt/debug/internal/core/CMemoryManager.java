/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * Enter type comment.
 * 
 * @since: Oct 15, 2002
 */
public class CMemoryManager implements ICMemoryManager
{
	private List fBlocks;
	private CDebugTarget fDebugTarget;

	/**
	 * Constructor for CMemoryManager.
	 */
	public CMemoryManager( CDebugTarget target )
	{
		fBlocks = new ArrayList( 4 );
		setDebugTarget( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#addBlock(IMemoryBlock)
	 */
	public void addBlock( IMemoryBlock memoryBlock ) throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#removeBlock(IMemoryBlock)
	 */
	public void removeBlock( IMemoryBlock memoryBlock ) throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#removeAllBlocks()
	 */
	public void removeAllBlocks() throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#getBlock(int)
	 */
	public IMemoryBlock getBlock( int index )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#getBlocks()
	 */
	public IMemoryBlock[] getBlocks()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICMemoryManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( CMemoryManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( IDebugTarget.class ) )
		{
			return fDebugTarget;
		}
		return null;
	}

	public IDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}
	
	protected void setDebugTarget( CDebugTarget target )
	{
		fDebugTarget = target;
	}
	
	public void dispose()
	{
	}
}
