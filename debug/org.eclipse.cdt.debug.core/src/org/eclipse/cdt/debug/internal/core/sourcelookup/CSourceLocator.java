/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Default source locator.
 * 
 * @since Aug 19, 2002
 */

public class CSourceLocator implements ICSourceLocator, IPersistableSourceLocator
{
	private static final String ELEMENT_NAME = "cSourceLocator";
	private static final String CHILD_NAME = "cSourceLocation";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_MEMENTO = "memento";

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
	 * of the given project and all of its referenced projects.
	 * 
	 * @param project a project
	 * @return a collection of source locations for all required
	 *  projects
	 * @exception CoreException 
	 */
	public static ICSourceLocation[] getDefaultSourceLocations( IProject project )
	{
		ArrayList list = new ArrayList();
		if ( project != null && project.exists() )
		{
			list.add( new CProjectSourceLocation( project ) );
			addReferencedSourceLocations( list, project );
		}
		return (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] );
	}

	private static void addReferencedSourceLocations( List list, IProject project )
	{
		if ( project != null )
		{
			try
			{
				IProject[] projects = project.getReferencedProjects();
				for ( int i = 0; i < projects.length; i++ )
				{
					if (  projects[i].exists() )
					{
						list.add( new CProjectSourceLocation( projects[i] ) );
						addReferencedSourceLocations( list, projects[i] );
					}
				}
			}
			catch( CoreException e )
			{
				// do nothing
			}
		}
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#findSourceElement(String)
	 */
	public Object findSourceElement( String fileName )
	{
		Object result = null;
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
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		Document doc = new DocumentImpl();
		Element node = doc.createElement( ELEMENT_NAME );
		doc.appendChild( node );

		ICSourceLocation[] locations = getSourceLocations();
		for ( int i = 0; i < locations.length; i++ )
		{
			Element child = doc.createElement( CHILD_NAME );
			child.setAttribute( ATTR_CLASS, locations[i].getClass().getName() );
			child.setAttribute( ATTR_MEMENTO, locations[i].getMemento() );
			node.appendChild( child );
		}
		try
		{
			return CDebugUtils.serializeDocument( doc, " " );
		}
		catch( IOException e )
		{
			abort( "Unable to create memento for C/C++ source locator.", e );
		}
		// execution will not reach here
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException
	{
		IProject project = getProject( configuration );
		if ( project != null )
		{
			setSourceLocations( getDefaultSourceLocations( project ) );
		}
		else
		{
			setSourceLocations( new ICSourceLocation[0] );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento( String memento ) throws CoreException
	{
		Exception ex = null;
		try
		{
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();

			if ( !root.getNodeName().equalsIgnoreCase( ELEMENT_NAME ) )
			{
				abort( "Unable to restore C/C++ source locator - invalid format.", null );
			}

			List sourceLocations = new ArrayList();
			ClassLoader classLoader = CDebugCorePlugin.getDefault() .getDescriptor().getPluginClassLoader();

			NodeList list = root.getChildNodes();
			int length = list.getLength();
			for ( int i = 0; i < length; ++i )
			{
				Node node = list.item( i );
				short type = node.getNodeType();
				if ( type == Node.ELEMENT_NODE )
				{
					Element entry = (Element)node;
					if ( entry.getNodeName().equalsIgnoreCase( CHILD_NAME ) )
					{
						String className = entry.getAttribute( ATTR_CLASS );
						String data = entry.getAttribute( ATTR_MEMENTO );
						if ( isEmpty( className ) )
						{
							abort( "Unable to restore C/C++ source locator - invalid format.", null );
						}
						Class clazz = null;
						try
						{
							clazz = classLoader.loadClass( className );
						}
						catch( ClassNotFoundException e )
						{
							abort( MessageFormat.format( "Unable to restore source location - class not found {0}", new String[] { className } ), e );
						}

						ICSourceLocation location = null;
						try
						{
							location = (ICSourceLocation)clazz.newInstance();
						}
						catch( IllegalAccessException e )
						{
							abort( "Unable to restore source location.", e );
						}
						catch( InstantiationException e )
						{
							abort( "Unable to restore source location.", e );
						}
						location.initializeFrom( data );
						sourceLocations.add( location );
					}
					else
					{
						abort( "Unable to restore C/C++ source locator - invalid format.", null );
					}
				}
			}
			setSourceLocations( (ICSourceLocation[])sourceLocations.toArray( new ICSourceLocation[sourceLocations.size()] ) );
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
		abort( "Exception occurred initializing source locator.", ex );
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
	
	private IProject getProject( ILaunchConfiguration configuration )
	{
		IProject project = null;
		try
		{
			String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "" );
			if ( !isEmpty( projectName ) )
				project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
		}
		catch( CoreException e )
		{
		}
		return project;
	}
}
