/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * Locates source elements in a Java project. Returns instances of <code>IFile</code>.
 * 
 * @since Sep 23, 2002
 */
public class CProjectSourceLocation implements ICSourceLocation
{
	/**
	 * The project associated with this source location
	 */
	private IProject fProject;

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation( IProject project )
	{
		setProject( project );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement( String name ) throws CoreException
	{
		if ( getProject() != null )
		{
			File file = new File( name );
			if ( file.isAbsolute() )
				return findFileByAbsolutePath( name );
			else
				return findFileByRelativePath( name );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICSourceLocation.class ) )
			return this;
		if ( adapter.equals( CProjectSourceLocation.class ) )
			return this;
		return null;
	}

	/**
	 * Sets the project in which source elements will be searched for.
	 * 
	 * @param project the project
	 */
	private void setProject( IProject project )
	{
		fProject = project;
	}

	/**
	 * Returns the project associated with this source location.
	 * 
	 * @return project
	 */
	public IProject getProject()
	{
		return fProject;
	}
/*
	private IFile findFile( IContainer parent, IPath name ) throws CoreException 
	{
		if ( name.isAbsolute() )
		{
			if ( name.toOSString().startsWith( parent.getLocation().toOSString() ) )
			{
				name = new Path( name.toOSString().substring( parent.getLocation().toOSString().length() + 1 ) );
			}
		}
		IResource found = parent.findMember( name );
		if ( found != null && found.getType() == IResource.FILE ) 
		{
			return (IFile)found;
		}
		IResource[] children= parent.members();
		for ( int i= 0; i < children.length; i++ ) 
		{
			if ( children[i] instanceof IContainer ) 
			{
				return findFile( (IContainer)children[i], name );
			}
		}
		return null;		
	}
*/
	private Object findFileByAbsolutePath( String name )
	{
		IPath path = new Path( name );
		return findFile( getProject(), path.toOSString() );
	}

	private Object findFileByRelativePath( String fileName )
	{
		IPath path = getProject().getLocation().append( fileName );
		Object result = findFileByAbsolutePath( path.toOSString() );
		if ( result == null )
		{
			try
			{
				IResource[] members = getProject().members();
				for ( int i = 0; i < members.length; ++i )
				{
					if ( members[i] instanceof IFolder )
					{
						path = members[i].getLocation().append( fileName );
						result = findFile( (IContainer)members[i], path.toOSString() );
						if ( result != null )
							break;
					}
				}
			}
			catch( CoreException e )
			{
				// do nothing
			}
		}
		return result;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getPaths()
	 */
	public IPath[] getPaths()
	{
		IPath[] result = new IPath[0];
		if ( getProject() != null )
		{
			result = new IPath[] { getProject().getLocation() };
		}
		return result;
	}
	
	private Object findFile( IContainer container, String fileName )
	{
		try
		{
			IResource[] members = container.members();
			for ( int i = 0; i < members.length; ++i )
			{
				if ( members[i] instanceof IFile )
				{
					if ( members[i].getLocation().toOSString().equals( fileName ) )
						return members[i];
				}
				else if ( members[i] instanceof IFolder && fileName.startsWith( members[i].getLocation().toOSString() ) )
				{
					Object result = findFile( (IContainer)members[i], fileName );
					if ( result != null )
						return result;
				}
			}
		}
		catch( CoreException e )
		{
			// do nothing
		}
		
		return null;
	}
}
