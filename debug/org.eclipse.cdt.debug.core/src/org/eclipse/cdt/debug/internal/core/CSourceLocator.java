/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.model.*;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * 
 * Locates source for a C/C++ debug session.
 * 
 * @since Aug 19, 2002
 */
public class CSourceLocator implements ICSourceLocator
{
	/**
	 * The array of source locations associated with this locator.
	 */
	private ICSourceLocation[] fSourceLocations;

	/**
	 * The source presentation mode.
	 */
	private int fMode = ICSourceLocator.MODE_SOURCE;

	private int fInternalMode = MODE_SOURCE;

	/**
	 * Constructor for CSourceLocator.
	 */
	public CSourceLocator( IProject project )
	{
		fSourceLocations = getDefaultSourceLocations( project );
	}

	/**
	 * Constructor for CSourceLocator.
	 */
	public CSourceLocator( ICSourceLocation[] locations )
	{
		fSourceLocations = locations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object result = null;
		if ( stackFrame != null && stackFrame.getAdapter( IStackFrameInfo.class ) != null )
		{
			try
			{
				result = getInput( (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class ) );
			}
			catch( DebugException e )
			{
				CDebugCorePlugin.log( e );
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getLineNumber(IStackFrameInfo)
	 */
	public int getLineNumber( IStackFrameInfo frameInfo )
	{
		int result = 0;
		if ( getMode() == MODE_SOURCE )
			result = frameInfo.getFrameLineNumber();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getMode()
	 */
	public int getMode()
	{
		return fMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#setMode(int)
	 */
	public void setMode( int mode )
	{
	}

	protected void setInternalMode( int mode ) 
	{
		fInternalMode = mode;
	}

	protected Object getInput( IStackFrameInfo frameInfo ) throws DebugException
	{
		Object result = null;
		switch( getMode() )
		{
			case ICSourceLocator.MODE_SOURCE:
				result = getSourceInput( frameInfo );
				break;
/*
			case ICSourceLocator.MODE_DISASSEMBLY:
				result = getDisassemblyInput( frameInfo );
				break;
			case ICSourceLocator.MODE_MIXED:
				result = getMixedInput( frameInfo );
				break;
*/
		}
		return result;
	}

	private Object getSourceInput( IStackFrameInfo info )
	{
		Object result = null;
		if ( info != null )
		{
			setInternalMode( ICSourceLocator.MODE_SOURCE );
			String fileName = info.getFile();
			if ( fileName != null && fileName.length() > 0 )
			{
				ICSourceLocation[] locations = getSourceLocations();
				for ( int i = 0; i < locations.length; ++i )
				{
					try
					{
						result = locations[i].findSourceElement( fileName );
					}
					catch( CoreException e )
					{
						// do nothing
					}
					if ( result != null )
						break;
				}
			}
		}		
		// switch to assembly mode if source file not found	
/*
		if ( result == null )
			result = getDisassemblyInput( info );
*/
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getSourceElement(String)
	 */
	public Object getSourceElement( String fileName )
	{
		Object result = null;
		if ( fileName != null && fileName.length() > 0 )
		{
			ICSourceLocation[] locations = getSourceLocations();
			for ( int i = 0; i < locations.length; ++i )
			{
				try
				{
					result = locations[i].findSourceElement( fileName );
				}
				catch( CoreException e )
				{
					// do nothing
				}
				if ( result != null )
					break;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getSourceElementForAddress(long)
	 */
	public Object getSourceElementForAddress( long address )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getSourceElementForFunction(String)
	 */
	public Object getSourceElementForFunction( String function )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#contains(IResource)
	 */
	public boolean contains( IResource resource )
	{
		ICSourceLocation[] locations = getSourceLocations();
		for ( int i = 0; i < locations.length; ++i )
		{
			if ( resource instanceof IProject )
			{
				if ( locations[i] instanceof CProjectSourceLocation && 
					 ((CProjectSourceLocation)locations[i]).equals( resource ) )
				{
					return true;
				}
			}
			if ( resource instanceof IFile )
			{
				try
				{
					if ( locations[i].findSourceElement( resource.getLocation().toOSString() ) != null )
						return true;
				}
				catch( CoreException e )
				{
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getSourceLocations()
	 */
	public ICSourceLocation[] getSourceLocations()
	{
		return fSourceLocations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#setSourceLocations(ICSourceLocation[])
	 */
	public void setSourceLocations( ICSourceLocation[] locations )
	{
		fSourceLocations = locations;
	}

	/**
	 * Returns a default collection of source locations for
	 * the given project. Default source locations consist
	 * of the given project and all of its referenced projects .
	 * 
	 * @param project a project
	 * @return a collection of source locations for all required
	 *  projects
	 * @exception CoreException 
	 */
	public static ICSourceLocation[] getDefaultSourceLocations( IProject project )
	{
		ArrayList list = new ArrayList();
		if ( project != null )
		{
			try
			{
				IProject[] projects = project.getReferencedProjects();
				list.add( new CProjectSourceLocation( project ) );
				for ( int i = 0; i < projects.length; i++ )
				{
					list.add( new CProjectSourceLocation( projects[i] ) );
				}
			}
			catch( CoreException e )
			{
				// do nothing
			}
		}
		return (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] );
	}
}
