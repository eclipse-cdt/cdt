/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.launch.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.ui.sourcelookup.CUISourceLocator;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The wrapper for the CUISourceLocator class.
 * 
 * @since: Dec 11, 2002
 */
public class DefaultSourceLocator implements IPersistableSourceLocator, IAdaptable
{
	private static final String ELEMENT_NAME = "PromptingSourceLocator";
	private static final String ATTR_PROJECT = "project";
	private static final String ATTR_MEMENTO = "memento";

	/**
	 * Identifier for the 'Default C/C++ Source Locator' extension
	 * (value <code>"org.eclipse.cdt.launch.DefaultSourceLocator"</code>).
	 */
	public static final String ID_DEFAULT_SOURCE_LOCATOR = LaunchUIPlugin.getUniqueIdentifier() + ".DefaultSourceLocator"; //$NON-NLS-1$

	private CUISourceLocator fSourceLocator = null;
	private final static int ERROR = 1000;   // ????

	/**
	 * Constructor for DefaultSourceLocator.
	 */
	public DefaultSourceLocator()
	{
	}

	/**
	 * Constructor for DefaultSourceLocator.
	 */
	public DefaultSourceLocator( CUISourceLocator locator )
	{
		fSourceLocator = locator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		if ( fSourceLocator != null )
		{
			Document doc = new DocumentImpl();
			Element node = doc.createElement( ELEMENT_NAME );
			doc.appendChild( node );
			node.setAttribute( ATTR_PROJECT, fSourceLocator.getProject().getName() );

			IPersistableSourceLocator psl = getPersistableSourceLocator();
			if ( psl != null )
			{
				node.setAttribute( ATTR_MEMENTO, psl.getMemento() );
			}
			try
			{
				return CDebugUtils.serializeDocument( doc, " " );
			}
			catch( IOException e )
			{
				abort( "Unable to create memento for C/C++ source locator.", e );
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(String)
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
				abort( "Unable to restore prompting source locator - invalid format.", null );
			}

			String projectName = root.getAttribute( ATTR_PROJECT );
			String data = root.getAttribute( ATTR_MEMENTO );
			if ( isEmpty( projectName ) )
			{
				abort( "Unable to restore prompting source locator - invalid format.", null );
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project != null )
			{
				fSourceLocator = new CUISourceLocator( project );
			}
			else
			{
				abort( MessageFormat.format( "Unable to restore prompting source locator - project {0} not found.", new String[] { projectName } ), null );
			}
			
			IPersistableSourceLocator psl = getPersistableSourceLocator();
			if ( psl != null )
			{
				psl.initializeFromMemento( data );
			}
			else
			{
				abort( "Unable to restore C/C++ source locator - invalid format.", null );
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
		abort( "Exception occurred initializing source locator.", ex );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException
	{
		fSourceLocator = new CUISourceLocator( getProject( configuration ) );
		String memento = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "" );
		if ( !isEmpty( memento ) )
			initializeFromMemento( memento );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame )
	{
		return ( fSourceLocator != null ) ? fSourceLocator.getSourceElement( stackFrame ) : null;
	}
	
	private IProject getProject( ILaunchConfiguration configuration ) throws CoreException
	{
		String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
		if ( !isEmpty( projectName ) )
		{
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project.exists() )
			{
				return project;
			}
		}
		abort( MessageFormat.format( "Project \"{0}\" does not exist.", new String[] { projectName } ), null );
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( fSourceLocator != null )
		{
			if ( adapter.equals( ICSourceLocator.class ) )
			{
				return fSourceLocator.getAdapter( adapter );
			}
			if ( adapter.equals( IResourceChangeListener.class ) )
			{
				return fSourceLocator.getAdapter( adapter );
			}
			if ( adapter.equals( ISourceMode.class ) )
			{
				return fSourceLocator.getAdapter( adapter );
			}
		}
		return null;
	}	

	private ICSourceLocator getCSourceLocator()
	{
		if ( fSourceLocator != null )
		{
			return (ICSourceLocator)fSourceLocator.getAdapter( ICSourceLocator.class );
		}
		return null;
	}
	
	private IPersistableSourceLocator getPersistableSourceLocator()
	{
		ICSourceLocator sl = getCSourceLocator();
		return ( sl instanceof IPersistableSourceLocator ) ? (IPersistableSourceLocator)sl : null;
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException
	{
		IStatus s = new Status( IStatus.ERROR,
								LaunchUIPlugin.getUniqueIdentifier(),
								ERROR,
								message,
								e );
		throw new CoreException( s );
	}


	private boolean isEmpty( String string )
	{
		return string == null || string.length() == 0;
	}
}
