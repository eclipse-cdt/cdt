/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IDirectorySourceLocation;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
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
	private static final String ELEMENT_NAME = "cDirectorySourceLocation";
	private static final String ATTR_DIRECTORY = "directory";
	private static final String ATTR_ASSOCIATION = "association";

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
	public CDirectorySourceLocation()
	{
	}

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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		Document doc = new DocumentImpl();
		Element node = doc.createElement( ELEMENT_NAME );
		doc.appendChild( node );
		node.setAttribute( ATTR_DIRECTORY, getDirectory().toOSString() );
		if ( getAssociation() != null )
			node.setAttribute( ATTR_ASSOCIATION, getAssociation().toOSString() );
		try
		{
			return CDebugUtils.serializeDocument( doc, " " );
		}
		catch( IOException e )
		{
			abort( MessageFormat.format( "Unable to create memento for C/C++ directory source location {0}", new String[] { getDirectory().toOSString() } ), e );
		}
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
				abort( "Unable to initialize source location - missing directory path", null );
			}
			else
			{
				IPath path = new Path( dir );
				if ( path.isValidPath( dir ) && path.toFile().isDirectory() )
				{
					setDirectory( path );
				}
				else
				{
					abort( MessageFormat.format( "Unable to initialize source location - invalid directory path {0}", new String[] { dir } ), null );
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
		abort( "Exception occurred initializing source location.", ex );
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
}
