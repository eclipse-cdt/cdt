/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.util.ArrayList;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * 
 * Default source locator.
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
	 * Constructor for CSourceLocator.
	 */
	public CSourceLocator()
	{
		setSourceLocations( new ICSourceLocation[0] );
	}

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
		if ( stackFrame != null && stackFrame.getAdapter( IStackFrameInfo.class ) != null )
		{
			return getInput( (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class ) );
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getLineNumber(IStackFrameInfo)
	 */
	public int getLineNumber( IStackFrame frame )
	{
		IStackFrameInfo info = (IStackFrameInfo)frame.getAdapter( IStackFrameInfo.class );
		return ( info != null ) ? info.getFrameLineNumber() : 0;
	}

	protected Object getInput( IStackFrameInfo info )
	{
		Object result = null;
		if ( info != null )
		{
			String fileName = info.getFile();
			if ( fileName != null && fileName.length() > 0 )
			{
				result = findFileByAbsolutePath( fileName );
				if ( result == null )
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
		}		
		return result;
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
					 ((CProjectSourceLocation)locations[i]).getProject().equals( resource ) )
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
	
	private Object findFileByAbsolutePath( String fileName )
	{
		Path path = new Path( fileName );
		if ( path.isAbsolute() && path.toFile().exists() )
		{
			// Try for a file in another workspace project
			IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation( path );
			if ( f != null ) 
			{
				return f;
			} 
			return new FileStorage( path );
		}
		return null;
	}
}
