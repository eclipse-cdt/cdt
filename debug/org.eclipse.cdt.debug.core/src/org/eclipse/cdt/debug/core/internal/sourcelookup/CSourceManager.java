/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.internal.sourcelookup;

import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Locates sources for a C/C++ debug session.
 * 
 * @since: Oct 8, 2002
 */
public class CSourceManager implements ICSourceLocator, ISourceMode, IAdaptable
{
	private ISourceLocator fSourceLocator;
	private DisassemblyManager fDisassemblyManager;
	private int fMode = ISourceMode.MODE_SOURCE;
	private int fRealMode = fMode;
	
	/**
	 * Constructor for CSourceManager.
	 */
	public CSourceManager( ISourceLocator sourceLocator, DisassemblyManager disassemblyManager )
	{
		fSourceLocator = sourceLocator;
		fDisassemblyManager = disassemblyManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getLineNumber(IStackFrameInfo)
	 */
	public int getLineNumber( IStackFrameInfo frameInfo )
	{
		if ( getRealMode() == ISourceMode.MODE_SOURCE )
		{
			if ( getCSourceLocator() != null )
			{
				return getCSourceLocator().getLineNumber( frameInfo );
			}
			if ( frameInfo != null )
			{
				return frameInfo.getFrameLineNumber();
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getSourceElement(String)
	 */
	public Object getSourceElement( String fileName )
	{
		return ( getCSourceLocator() != null ) ? getCSourceLocator().getSourceElement( fileName ) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getSourceLocations()
	 */
	public ICSourceLocation[] getSourceLocations()
	{
		return ( getCSourceLocator() != null ) ? getCSourceLocator().getSourceLocations() : new ICSourceLocation[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#setSourceLocations(ICSourceLocation[])
	 */
	public void setSourceLocations( ICSourceLocation[] locations )
	{
		if ( getCSourceLocator() != null )
		{
			getCSourceLocator().setSourceLocations( locations );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#contains(IResource)
	 */
	public boolean contains( IResource resource )
	{
		return ( getCSourceLocator() != null ) ? getCSourceLocator().contains( resource ) : false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ISourceMode#getMode()
	 */
	public int getMode()
	{
		return fMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ISourceMode#setMode(int)
	 */
	public void setMode( int mode )
	{
		fMode = mode;
	}

	protected int getRealMode()
	{
		return fRealMode;
	}

	protected void setRealMode( int mode )
	{
		fRealMode = mode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		return ( getSourceLocator() != null ) ? getSourceLocator().getSourceElement( stackFrame ) : null;
	}
	
	protected ICSourceLocator getCSourceLocator()
	{
		if ( getSourceLocator() instanceof ICSourceLocator )
			return (ICSourceLocator)getSourceLocator();
		return null;
	}
	
	protected ISourceLocator getSourceLocator()
	{
		return fSourceLocator;
	}
}
