/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.Arrays;

import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Enter type comment.
 * 
 * @since: Oct 15, 2002
 */
public class CMemoryManager implements ICMemoryManager
{
	private IFormattedMemoryBlock[] fBlocks = new IFormattedMemoryBlock[4];
	private CDebugTarget fDebugTarget;

	/**
	 * Constructor for CMemoryManager.
	 */
	public CMemoryManager( CDebugTarget target )
	{
		Arrays.fill( fBlocks, null );
		setDebugTarget( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#removeBlock(IFormattedMemoryBlock)
	 */
	public synchronized void removeBlock( IFormattedMemoryBlock memoryBlock ) throws DebugException
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#removeAllBlocks()
	 */
	public synchronized void removeAllBlocks() throws DebugException
	{
		for ( int i = 0; i < fBlocks.length; ++i )
		{
			if ( fBlocks[i] != null )
			{
				fBlocks[i].dispose();
				fBlocks[i] = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#getBlock(int)
	 */
	public IFormattedMemoryBlock getBlock( int index )
	{
		return ( index >= 0 && index < fBlocks.length ) ? fBlocks[index] : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#getBlocks()
	 */
	public IFormattedMemoryBlock[] getBlocks()
	{
		return fBlocks;
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
		for ( int i = 0; i < fBlocks.length; ++i )
		{
			if ( fBlocks[i] != null )
			{
				fBlocks[i].dispose();
				fBlocks[i] = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#removeBlock(int)
	 */
	public synchronized void removeBlock( int index ) throws DebugException
	{
		IFormattedMemoryBlock block = getBlock( index );
		if ( block != null )
		{
			block.dispose();
		}
		setBlockAt( index, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#setBlockAt(int, IFormattedMemoryBlock)
	 */
	public synchronized void setBlockAt( int index, IFormattedMemoryBlock memoryBlock ) throws DebugException
	{
		IFormattedMemoryBlock block = getBlock( index );
		if ( block != null )
		{
			block.dispose();
		}
		fBlocks[index] = memoryBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICMemoryManager#getSupportedFormats()
	 */
	public int[] getSupportedFormats() throws DebugException
	{
		return new int[0];
	}
}
