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
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
 * Locates source elements in a Java project. Returns instances of <code>IFile</code>.
 * 
 * @since Sep 23, 2002
 */
public class CProjectSourceLocation implements IProjectSourceLocation
{
	private static final String ELEMENT_NAME = "cProjectSourceLocation";
	private static final String ATTR_PROJECT = "project";

	/**
	 * The project associated with this source location
	 */
	private IProject fProject;
	
	private HashMap fCache = new HashMap( 20 );
	
	private HashSet fNotFoundCache = new HashSet( 20 );

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation()
	{
	}

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
		Object result = null;
		if ( getProject() != null && !notFoundCacheLookup( name ) )
		{
			result = cacheLookup( name );
			if ( result == null )
			{ 
				result = doFindSourceElement( name );
				if ( result != null )
				{
					cacheSourceElement( name, result );
				}
			}
			if ( result == null )
			{
				cacheNotFound( name );
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
		if ( adapter.equals( CProjectSourceLocation.class ) )
			return this;
		if ( adapter.equals( IProject.class ) )
			return getProject();
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

	private Object doFindSourceElement( String name )
	{
		File file = new File( name );
		return ( file.isAbsolute() ) ? findFileByAbsolutePath( name ) : 
									   findFileByRelativePath( getProject(), name );
	}

	private Object findFileByAbsolutePath( String name )
	{
		IPath path = new Path( name );
		return findFile( getProject(), path.toOSString() );
	}

	private Object findFileByRelativePath( IContainer container, String fileName )
	{
		IPath rootPath = container.getLocation();
		IPath path = rootPath.append( fileName );
		Object result = findFileByAbsolutePath( path.toOSString() );
		if ( result == null )
		{
			try
			{
				IResource[] members = container.members();
				for ( int i = 0; i < members.length; ++i )
				{
					if ( members[i] instanceof IFolder )
					{
						path = members[i].getLocation().append( fileName );
						result = findFileByAbsolutePath( path.toOSString() );
						if ( result == null )
						{
							result = findFileByRelativePath( (IFolder)members[i], fileName );
						}
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
	
	private Object cacheLookup( String name )
	{
		return fCache.get( name );
	}
	
	private boolean notFoundCacheLookup( String name )
	{
		return fNotFoundCache.contains( name );
	}
	
	private void cacheSourceElement( String name, Object element )
	{
		fCache.put( name, element );
	}

	private void cacheNotFound( String name )
	{
		fNotFoundCache.add( name );
	}

	protected void dispose()
	{
		fCache.clear();
		fNotFoundCache.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		Document doc = new DocumentImpl();
		Element node = doc.createElement( ELEMENT_NAME );
		doc.appendChild( node );
		node.setAttribute( ATTR_PROJECT, getProject().getName() );
		
		try
		{
			return CDebugUtils.serializeDocument( doc, " " );
		}
		catch( IOException e )
		{
			abort( MessageFormat.format( "Unable to create memento for C/C++ project source location {0}.", new String[] { getProject().getName() } ), e );
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

			String name = root.getAttribute( ATTR_PROJECT );
			if ( isEmpty( name ) )
			{
				abort( "Unable to initialize source location - missing project name", null );
			}
			else
			{
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( name );
				setProject( project );
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
