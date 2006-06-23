/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Locates source elements in a directory in the local
 * file system. Returns instances of <code>FileStorage</code>.
 * 
 * @since Sep 23, 2002
 */
public class CDirectorySourceLocation implements IDirectorySourceLocation
{
	private static final String ELEMENT_NAME = "cDirectorySourceLocation"; //$NON-NLS-1$
	private static final String ATTR_DIRECTORY = "directory"; //$NON-NLS-1$
	private static final String ATTR_ASSOCIATION = "association"; //$NON-NLS-1$
	private static final String ATTR_SEARCH_SUBFOLDERS = "searchSubfolders"; //$NON-NLS-1$

	/**
	 * The root directory of this source location
	 */
	private IPath fDirectory;

	/**
	 * The associted path of this source location. 
	 */
	private IPath fAssociation = null;

	private boolean fSearchForDuplicateFiles = false;

	private boolean fSearchSubfolders = false;

	private File[] fFolders = null;

	/**
	 * Constructor for CDirectorySourceLocation.
	 */
	public CDirectorySourceLocation()
	{
	}

	/**
	 * Constructor for CDirectorySourceLocation.
	 */
	public CDirectorySourceLocation( IPath directory, IPath association, boolean searchSubfolders )
	{
		setDirectory( directory );
		setAssociation( association );
		setSearchSubfolders( searchSubfolders );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement( String name ) throws CoreException
	{
		Object result = null;
		if ( !isEmpty( name ) && getDirectory() != null )
		{
			File file = new File( name );
			if ( file.isAbsolute() )
				result = findFileByAbsolutePath( name );
			else
				result = findFileByRelativePath( name );
			if ( result == null && getAssociation() != null )
			{
				IPath path = new Path( name );
				if ( path.segmentCount() > 1 && getAssociation().isPrefixOf( path ) )
				{
					path = getDirectory().append( path.removeFirstSegments( getAssociation().segmentCount() ) );
					result = findFileByAbsolutePath( path.toOSString() );
				}
			}
		}
		return result;
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
		if ( adapter.equals( IPath.class ) )
			return getDirectory();
		return null;
	}

	/**
	 * Sets the directory in which source elements will be searched for.
	 * 
	 * @param directory a directory
	 */
	private void setDirectory( IPath directory )
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

	public void getDirectory( IPath path )
	{
		fDirectory = path;
	}

	public void setAssociation( IPath association )
	{
		fAssociation = association;
	}

	public IPath getAssociation()
	{
		return fAssociation;
	}

	private Object findFileByAbsolutePath( String name )
	{
		File file = new File( name );
		if ( !file.isAbsolute() )
			return null;
		File[] folders = getFolders();
		if ( folders != null )
		{
			LinkedList list = new LinkedList();		
			for ( int i = 0; i < folders.length; ++i )
			{
				Object result = findFileByAbsolutePath( folders[i], name );
				if ( result instanceof List )
				{
					if ( searchForDuplicateFiles() )
						list.addAll( (List)result );
					else
						return list.getFirst();
				}
				else if ( result != null )
				{
					if ( searchForDuplicateFiles() )
						list.add( result );
					else
						return result;
				}
			}
			if ( list.size() > 0 ) 
				return ( list.size() == 1 ) ? list.getFirst() : list;
		}
		return null;
	}

	private Object findFileByAbsolutePath( File folder, String name )
	{
		File file = new File( name );
		if ( !file.isAbsolute() )
			return null;
		IPath filePath = new Path( name );
		IPath path = new Path( folder.getAbsolutePath() );
		IPath association = getAssociation();
		if ( !isPrefix( path, filePath ) || path.segmentCount() + 1 != filePath.segmentCount() )
		{
			if ( association != null && isPrefix( association, filePath ) && association.segmentCount() + 1 == filePath.segmentCount() )
				filePath = path.append( filePath.removeFirstSegments( association.segmentCount() ) );
			else
				return null;
		}

		// Try for a file in another workspace project
		IFile[] wsFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation( filePath );
		LinkedList list = new LinkedList();
		for ( int j = 0; j < wsFiles.length; ++j )
			if ( wsFiles[j].exists() )
			{
				if ( !searchForDuplicateFiles() )
					return wsFiles[j];
				list.add( wsFiles[j] );
			}
		if ( list.size() > 0 ) 
			return ( list.size() == 1 ) ? list.getFirst() : list;

		file = filePath.toFile();
		if ( file.exists() && file.isFile() )
		{
			return createExternalFileStorage( filePath );
		}
		return null;
	}

	private Object findFileByRelativePath( String fileName )
	{
		File[] folders = getFolders();
		if ( folders != null )
		{
			LinkedList list = new LinkedList();		
			for ( int i = 0; i < folders.length; ++i )
			{
				Object result = findFileByRelativePath( folders[i], fileName );
				if ( result instanceof List )
				{
					if ( searchForDuplicateFiles() )
						list.addAll( (List)result );
					else
						return list.getFirst();
				}
				else if ( result != null )
				{
					if ( searchForDuplicateFiles() )
						list.add( result );
					else
						return result;
				}
			}
			if ( list.size() > 0 )
				return ( list.size() == 1 ) ? list.getFirst() : list;
		}
		return null;
	}

	private Object findFileByRelativePath( File folder, String fileName )
	{
		IPath path = new Path( folder.getAbsolutePath() );
		path = path.append( fileName );	
		File file = path.toFile();
		if ( file.exists() && file.isFile() )
		{
			path = new Path( file.getAbsolutePath() );
			IFile[] wsFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation( path );
			LinkedList list = new LinkedList();
			for ( int j = 0; j < wsFiles.length; ++j )
				if ( wsFiles[j].exists() )
				{
					if ( !searchForDuplicateFiles() )
						return wsFiles[j];
					list.add( wsFiles[j] );
				}
			if ( list.size() > 0 ) 
				return ( list.size() == 1 ) ? list.getFirst() : list;
			return createExternalFileStorage( path );
		}
		return null;
	}
	
	private IStorage createExternalFileStorage( IPath path )
	{
		return new FileStorage( path );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException
	{
        Document document = null;
        Throwable ex = null;
        try 
		{
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element node = document.createElement( ELEMENT_NAME );
            document.appendChild( node );
    		node.setAttribute( ATTR_DIRECTORY, getDirectory().toOSString() );
    		if ( getAssociation() != null )
    			node.setAttribute( ATTR_ASSOCIATION, getAssociation().toOSString() );
    		node.setAttribute( ATTR_SEARCH_SUBFOLDERS, Boolean.valueOf( searchSubfolders() ).toString() );
			return CDebugUtils.serializeDocument( document );
        }
        catch( ParserConfigurationException e ) 
		{
        	ex = e;
        }
		catch( IOException e )
		{
			ex = e;
		}
		catch( TransformerException e )
		{
			ex = e;
		}
		abort( MessageFormat.format( InternalSourceLookupMessages.getString( "CDirectorySourceLocation.0" ), new String[] { getDirectory().toOSString() } ), ex ); //$NON-NLS-1$
		// execution will not reach here
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#initializeFrom(java.lang.String)
	 */
	public void initializeFrom( String memento ) throws CoreException
	{
		Exception ex = null;
		try
		{
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();

			String dir = root.getAttribute( ATTR_DIRECTORY );
			if ( isEmpty( dir ) )
			{
				abort( InternalSourceLookupMessages.getString( "CDirectorySourceLocation.1" ), null ); //$NON-NLS-1$
			}
			else
			{
				IPath path = new Path( dir );
				if ( path.isValidPath( dir ) && path.toFile().isDirectory() && path.toFile().exists() )
				{
					setDirectory( path );
				}
				else
				{
					abort( MessageFormat.format( InternalSourceLookupMessages.getString( "CDirectorySourceLocation.2" ), new String[] { dir } ), null ); //$NON-NLS-1$
				}
			}
			dir = root.getAttribute( ATTR_ASSOCIATION );
			if ( isEmpty( dir ) )
			{
				setAssociation( null );
			}
			else
			{
				IPath path = new Path( dir );
				if ( path.isValidPath( dir ) )
				{
					setAssociation( path );
				}
				else
				{
					setAssociation( null );
				}
			}
			setSearchSubfolders( Boolean.valueOf( root.getAttribute( ATTR_SEARCH_SUBFOLDERS ) ).booleanValue() );
			return;
		}
		catch( ParserConfigurationException e )
		{
			ex = e;
		}
		catch( SAXException e )
		{
			ex = e;
		}
		catch( IOException e )
		{
			ex = e;
		}
		abort( InternalSourceLookupMessages.getString( "CDirectorySourceLocation.3" ), ex ); //$NON-NLS-1$
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException
	{
		IStatus s = new Status( IStatus.ERROR,
								CDebugCorePlugin.getUniqueIdentifier(),
								CDebugCorePlugin.INTERNAL_ERROR,
								message,
								e );
		throw new CoreException( s );
	}

	private boolean isEmpty( String string )
	{
		return string == null || string.length() == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj )
	{
		if ( obj instanceof IDirectorySourceLocation )
		{
			IPath dir = ((IDirectorySourceLocation)obj).getDirectory();
			IPath association = ((IDirectorySourceLocation)obj).getAssociation();
			if ( dir == null )
				return false;
			boolean result = dir.equals( getDirectory() );
			if ( result )
			{
				if ( association == null && getAssociation() == null )
					return true;
				if ( association != null )
					return association.equals( getAssociation() );
			}
		}
		return false;
	}

	private boolean isPrefix( IPath prefix, IPath path )
	{
		int segCount = prefix.segmentCount();
		if ( segCount >= path.segmentCount() )
			return false;
		String prefixString = prefix.toOSString();
		String pathString = path.removeLastSegments( path.segmentCount() - segCount ).toOSString();
		return prefixString.equalsIgnoreCase( pathString );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#setSearchForDuplicateFiles(boolean)
	 */
	public void setSearchForDuplicateFiles( boolean search )
	{
		fSearchForDuplicateFiles = search;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#searchForDuplicateFiles()
	 */
	public boolean searchForDuplicateFiles()
	{
		return fSearchForDuplicateFiles;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation#searchSubfolders()
	 */
	public boolean searchSubfolders()
	{
		return fSearchSubfolders;
	}

	public void setSearchSubfolders( boolean search )
	{
		resetFolders();
		fSearchSubfolders = search;
	}

	protected File[] getFolders()
	{
		if ( fFolders == null )
			initializeFolders();
		return fFolders;
	}

	protected void resetFolders()
	{
		fFolders = null;
	}

	private void initializeFolders()
	{
		if ( getDirectory() != null )
		{
			ArrayList list = new ArrayList();
			File root = getDirectory().toFile();
			list.add( root );
			if ( searchSubfolders() )
				list.addAll( getFileFolders( root ) );
			fFolders = (File[])list.toArray( new File[list.size()] );
		}
	}

	private List getFileFolders( File file )
	{
		ArrayList list = new ArrayList();
		File[] folders = file.listFiles( 
									new FileFilter()
										{
											public boolean accept( File pathname )
											{
												return pathname.isDirectory();
											}
										} );
		list.addAll( Arrays.asList( folders ) );
		for ( int i = 0; i < folders.length; ++i )
			list.addAll( getFileFolders( folders[i] ) );
		return list;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return ( getDirectory() != null ) ? getDirectory().toOSString() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#dispose()
	 */
	public void dispose()
	{
	}
}
