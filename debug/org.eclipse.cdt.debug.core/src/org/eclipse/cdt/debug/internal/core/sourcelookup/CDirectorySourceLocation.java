/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * Locates source elements in a directory in the local
 * file system. Returns instances of <code>FileStorage</code>.
 * 
 * @since Sep 23, 2002
 */
public class CDirectorySourceLocation implements ICSourceLocation
{
	/**
	 * The root directory of this source location
	 */
	private IPath fDirectory;

	/**
	 * The associted path of this source location. 
	 */
	private IPath fAssociation = null;

	/**
	 * Constructor for CDirectorySourceLocation.
	 */
	public CDirectorySourceLocation( IPath directory )
	{
		setDirectory( directory );
	}

	/**
	 * Constructor for CDirectorySourceLocation.
	 */
	public CDirectorySourceLocation( IPath directory, IPath association )
	{
		setDirectory( directory );
		setAssociation( association );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement( String name ) throws CoreException
	{
		if ( getDirectory() != null )
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
		if ( adapter.equals( CDirectorySourceLocation.class ) )
			return this;
		return null;
	}

	/**
	 * Sets the directory in which source elements will be searched for.
	 * 
	 * @param directory a directory
	 */
	protected void setDirectory( IPath directory )
	{
		fDirectory = directory;
	}

	/**
	 * Returns the root directory of this source location.
	 * 
	 * @return directory
	 */
	public IPath getDirectory()
	{
		return fDirectory;
	}

	protected void setAssociation( IPath association )
	{
		fAssociation = association;
	}

	public IPath getAssociation()
	{
		return fAssociation;
	}

	private Object findFileByAbsolutePath( String fileName )
	{
		IPath filePath = new Path( fileName );
		IPath path = getDirectory();
		IPath association = getAssociation();
		if ( path.isPrefixOf( filePath ) )
		{
			filePath = path.append( filePath.removeFirstSegments( path.segmentCount() ) );
		}
		else if ( association != null && association.isPrefixOf( filePath ) )
		{
			filePath = path.append( filePath.removeFirstSegments( association.segmentCount() ) );
		}
		else
		{
			return null;
		}
		// Try for a file in another workspace project
		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation( filePath );
		if ( f != null ) 
		{
			return f;
		} 

		File file = filePath.toFile();
		if ( file.exists() )
		{
			return createExternalFileStorage( filePath );
		}
		return null;
	}

	private Object findFileByRelativePath( String fileName )
	{
		IPath path = getDirectory();
		if ( path != null )
		{
			path = path.append( fileName );	
			File file = path.toFile();
			if ( file.exists() )
			{
				IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation( path );
				if ( f != null ) 
				{
					return f;
				} 
				return createExternalFileStorage( path );
			}
		}
		return null;
	}
	
	private IStorage createExternalFileStorage( IPath path )
	{
		return new FileStorage( path );
	}

	/**
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getPaths()
	 */
	public IPath[] getPaths()
	{
		return new IPath[] { fDirectory };
	}
}
