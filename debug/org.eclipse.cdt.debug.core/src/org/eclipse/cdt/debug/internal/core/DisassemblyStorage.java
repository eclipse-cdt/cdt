/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.debug.core.IDisassemblyStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
		return null;
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
}
