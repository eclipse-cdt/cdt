/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLookupFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Default source locator.
 */
public class CSourceLocator implements ICSourceLocator, IPersistableSourceLocator, IResourceChangeListener {

	private static final String SOURCE_LOCATOR_NAME = "cSourceLocator"; //$NON-NLS-1$

	private static final String DISABLED_GENERIC_PROJECT_NAME = "disabledGenericProject"; //$NON-NLS-1$

	private static final String ADDITIONAL_SOURCE_LOCATION_NAME = "additionalSourceLocation"; //$NON-NLS-1$

	private static final String SOURCE_LOCATION_NAME = "cSourceLocation"; //$NON-NLS-1$

	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	private static final String ATTR_PROJECT_NAME = "projectName"; //$NON-NLS-1$

	private static final String ATTR_DUPLICATE_FILES = "duplicateFiles"; //$NON-NLS-1$

	/**
	 * The project associated with this locator.
	 */
	private IProject fProject = null;

	/**
	 * The array of source locations associated with this locator.
	 */
	private ICSourceLocation[] fSourceLocations;

	/**
	 * The array of projects referenced by main project.
	 */
	private List fReferencedProjects = new ArrayList( 10 );

	/**
	 * The flag specifies whether to search for all source elements, or just the first match.
	 */
	private boolean fDuplicateFiles = false;

	/**
	 * Constructor for CSourceLocator.
	 */
	public CSourceLocator( IProject project ) {
		setProject( project );
		setReferencedProjects();
		setSourceLocations( getDefaultSourceLocations() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame ) {
		return getInput( stackFrame );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#getLineNumber(IStackFrameInfo)
	 */
	public int getLineNumber( IStackFrame frame ) {
		return (frame instanceof ICStackFrame) ? ((ICStackFrame)frame).getFrameLineNumber() : 0;
	}

	protected Object getInput( IStackFrame f ) {
		if ( f instanceof ICStackFrame ) {
			ICStackFrame frame = (ICStackFrame)f;
			LinkedList list = new LinkedList();
			if ( frame != null ) {
				Object result = null;
				String fileName = frame.getFile();
				if ( fileName != null && fileName.length() > 0 ) {
					ICSourceLocation[] locations = getSourceLocations();
					for( int i = 0; i < locations.length; ++i ) {
						try {
							result = locations[i].findSourceElement( fileName );
						}
						catch( CoreException e ) {
							// do nothing
						}
						if ( result != null ) {
							if ( result instanceof List )
								list.addAll( (List)result );
							else
								list.add( result );
							if ( !searchForDuplicateFiles() )
								break;
						}
					}
				}
			}
			return (list.size() > 0) ? ((list.size() == 1) ? list.getFirst() : list) : null;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICSourceLocator#contains(IResource)
	 */
	public boolean contains( IResource resource ) {
		ICSourceLocation[] locations = getSourceLocations();
		for( int i = 0; i < locations.length; ++i ) {
			if ( resource instanceof IProject ) {
				if ( locations[i] instanceof CProjectSourceLocation && ((CProjectSourceLocation)locations[i]).getProject().equals( resource ) ) {
					return true;
				}
			}
			if ( resource instanceof IFile ) {
				try {
					Object result = locations[i].findSourceElement( resource.getLocation().toOSString() );
					if ( result instanceof IFile && ((IFile)result).equals( resource ) )
						return true;
					if ( result instanceof List && ((List)result).contains( resource ) )
						return true;
				}
				catch( CoreException e ) {
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getSourceLocations()
	 */
	public ICSourceLocation[] getSourceLocations() {
		return fSourceLocations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#setSourceLocations(ICSourceLocation[])
	 */
	public void setSourceLocations( ICSourceLocation[] locations ) {
		fSourceLocations = locations;
	}

	/**
	 * Returns a default collection of source locations for the given project. Default source locations consist of the given project and all of its referenced
	 * projects.
	 * 
	 * @param project
	 *            a project
	 * @return a collection of source locations for all required projects
	 * @exception CoreException
	 */
	public static ICSourceLocation[] getDefaultSourceLocations( IProject project ) {
		ArrayList list = new ArrayList();
		if ( project != null && project.exists() ) {
			list.add( SourceLookupFactory.createProjectSourceLocation( project ) );
			addReferencedSourceLocations( list, project );
		}
		return (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] );
	}

	private static void addReferencedSourceLocations( List list, IProject project ) {
		if ( project != null ) {
			try {
				IProject[] projects = project.getReferencedProjects();
				for( int i = 0; i < projects.length; i++ ) {
					if ( projects[i].exists() && !containsProject( list, projects[i] ) ) {
						list.add( SourceLookupFactory.createProjectSourceLocation( projects[i] ) );
						addReferencedSourceLocations( list, projects[i] );
					}
				}
			}
			catch( CoreException e ) {
				// do nothing
			}
		}
	}

	private static boolean containsProject( List list, IProject project ) {
		Iterator it = list.iterator();
		while( it.hasNext() ) {
			CProjectSourceLocation location = (CProjectSourceLocation)it.next();
			if ( project.equals( location.getProject() ) )
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#findSourceElement(String)
	 */
	public Object findSourceElement( String fileName ) {
		Object result = null;
		if ( fileName != null && fileName.length() > 0 ) {
			ICSourceLocation[] locations = getSourceLocations();
			for( int i = 0; i < locations.length; ++i ) {
				try {
					result = locations[i].findSourceElement( fileName );
				}
				catch( CoreException e ) {
					// do nothing
				}
				if ( result != null )
					break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException {
		Document document = null;
		Throwable ex = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element node = document.createElement( SOURCE_LOCATOR_NAME );
			document.appendChild( node );
			ICSourceLocation[] locations = getSourceLocations();
			saveDisabledGenericSourceLocations( locations, document, node );
			saveAdditionalSourceLocations( locations, document, node );
			node.setAttribute( ATTR_DUPLICATE_FILES, Boolean.valueOf( searchForDuplicateFiles() ).toString() );
			return CDebugUtils.serializeDocument( document );
		}
		catch( ParserConfigurationException e ) {
			ex = e;
		}
		catch( IOException e ) {
			ex = e;
		}
		catch( TransformerException e ) {
			ex = e;
		}
		abort( InternalSourceLookupMessages.getString( "CSourceLocator.0" ), ex ); //$NON-NLS-1$
		// execution will not reach here
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException {
		setSourceLocations( getDefaultSourceLocations() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento( String memento ) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();
			if ( !root.getNodeName().equalsIgnoreCase( SOURCE_LOCATOR_NAME ) ) {
				abort( InternalSourceLookupMessages.getString( "CSourceLocator.1" ) , null ); //$NON-NLS-1$
			}
			List sourceLocations = new ArrayList();
			// Add locations based on referenced projects
			IProject project = getProject();
			if ( project != null && project.exists() && project.isOpen() )
				sourceLocations.addAll( Arrays.asList( getDefaultSourceLocations() ) );
			removeDisabledLocations( root, sourceLocations );
			addAdditionalLocations( root, sourceLocations );
			// To support old launch configuration
			addOldLocations( root, sourceLocations );
			setSourceLocations( (ICSourceLocation[])sourceLocations.toArray( new ICSourceLocation[sourceLocations.size()] ) );
			setSearchForDuplicateFiles( Boolean.valueOf( root.getAttribute( ATTR_DUPLICATE_FILES ) ).booleanValue() );
			return;
		}
		catch( ParserConfigurationException e ) {
			ex = e;
		}
		catch( SAXException e ) {
			ex = e;
		}
		catch( IOException e ) {
			ex = e;
		}
		abort( InternalSourceLookupMessages.getString( "CSourceLocator.2" ), ex ); //$NON-NLS-1$
	}

	private void removeDisabledLocations( Element root, List sourceLocations ) {
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		HashSet disabledProjects = new HashSet( length );
		for( int i = 0; i < length; ++i ) {
			Node node = list.item( i );
			short type = node.getNodeType();
			if ( type == Node.ELEMENT_NODE ) {
				Element entry = (Element)node;
				if ( entry.getNodeName().equalsIgnoreCase( DISABLED_GENERIC_PROJECT_NAME ) ) {
					String projectName = entry.getAttribute( ATTR_PROJECT_NAME );
					if ( isEmpty( projectName ) ) {
						CDebugCorePlugin.log( "Unable to restore C/C++ source locator - invalid format." ); //$NON-NLS-1$
					}
					disabledProjects.add( projectName.trim() );
				}
			}
		}
		Iterator it = sourceLocations.iterator();
		while( it.hasNext() ) {
			ICSourceLocation location = (ICSourceLocation)it.next();
			if ( location instanceof IProjectSourceLocation && disabledProjects.contains( ((IProjectSourceLocation)location).getProject().getName() ) )
				it.remove();
		}
	}

	private void addAdditionalLocations( Element root, List sourceLocations ) throws CoreException {
		Bundle bundle = CDebugCorePlugin.getDefault().getBundle();
		MultiStatus status = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, InternalSourceLookupMessages.getString( "CSourceLocator.3" ), null ); //$NON-NLS-1$
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for( int i = 0; i < length; ++i ) {
			Node node = list.item( i );
			short type = node.getNodeType();
			if ( type == Node.ELEMENT_NODE ) {
				Element entry = (Element)node;
				if ( entry.getNodeName().equalsIgnoreCase( ADDITIONAL_SOURCE_LOCATION_NAME ) ) {
					String className = entry.getAttribute( ATTR_CLASS );
					String data = entry.getAttribute( ATTR_MEMENTO );
					if ( isEmpty( className ) ) {
						CDebugCorePlugin.log( "Unable to restore C/C++ source locator - invalid format." ); //$NON-NLS-1$
						continue;
					}
					Class clazz = null;
					try {
						clazz = bundle.loadClass( className );
					}
					catch( ClassNotFoundException e ) {
						CDebugCorePlugin.log( MessageFormat.format( "Unable to restore source location - class not found {0}", new String[]{ className } ) ); //$NON-NLS-1$
						continue;
					}
					ICSourceLocation location = null;
					try {
						location = (ICSourceLocation)clazz.newInstance();
					}
					catch( IllegalAccessException e ) {
						CDebugCorePlugin.log( "Unable to restore source location." ); //$NON-NLS-1$
						continue;
					}
					catch( InstantiationException e ) {
						CDebugCorePlugin.log( "Unable to restore source location." ); //$NON-NLS-1$
						continue;
					}
					try {
						location.initializeFrom( data );
						sourceLocations.add( location );
					}
					catch( CoreException e ) {
						status.addAll( e.getStatus() );
					}
				}
			}
		}
		if ( status.getSeverity() > IStatus.OK )
			throw new CoreException( status );
	}

	private void addOldLocations( Element root, List sourceLocations ) throws CoreException {
		Bundle bundle = CDebugCorePlugin.getDefault().getBundle();
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for( int i = 0; i < length; ++i ) {
			Node node = list.item( i );
			short type = node.getNodeType();
			if ( type == Node.ELEMENT_NODE ) {
				Element entry = (Element)node;
				if ( entry.getNodeName().equalsIgnoreCase( SOURCE_LOCATION_NAME ) ) {
					String className = entry.getAttribute( ATTR_CLASS );
					String data = entry.getAttribute( ATTR_MEMENTO );
					if ( isEmpty( className ) ) {
						CDebugCorePlugin.log( "Unable to restore C/C++ source locator - invalid format." ); //$NON-NLS-1$
						continue;
					}
					Class clazz = null;
					try {
						clazz = bundle.loadClass( className );
					}
					catch( ClassNotFoundException e ) {
						CDebugCorePlugin.log( MessageFormat.format( "Unable to restore source location - class not found {0}", new String[]{ className } ) ); //$NON-NLS-1$
						continue;
					}
					ICSourceLocation location = null;
					try {
						location = (ICSourceLocation)clazz.newInstance();
					}
					catch( IllegalAccessException e ) {
						CDebugCorePlugin.log( "Unable to restore source location." ); //$NON-NLS-1$
						continue;
					}
					catch( InstantiationException e ) {
						CDebugCorePlugin.log( "Unable to restore source location." ); //$NON-NLS-1$
						continue;
					}
					location.initializeFrom( data );
					if ( !sourceLocations.contains( location ) ) {
						if ( location instanceof CProjectSourceLocation )
							((CProjectSourceLocation)location).setGenerated( isReferencedProject( ((CProjectSourceLocation)location).getProject() ) );
						sourceLocations.add( location );
					}
				}
			}
		}
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException {
		IStatus s = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, message, e );
		throw new CoreException( s );
	}

	private boolean isEmpty( String string ) {
		return string == null || string.trim().length() == 0;
	}

	public void resourceChanged( IResourceChangeEvent event ) {
		if ( event.getSource() instanceof IWorkspace && event.getDelta() != null ) {
			IResourceDelta[] deltas = event.getDelta().getAffectedChildren();
			if ( deltas != null ) {
				ArrayList list = new ArrayList( deltas.length );
				for( int i = 0; i < deltas.length; ++i )
					if ( deltas[i].getResource() instanceof IProject )
						list.add( deltas[i].getResource() );
				resetSourceLocations( list );
			}
		}
	}

	private void saveDisabledGenericSourceLocations( ICSourceLocation[] locations, Document doc, Element node ) {
		IProject project = getProject();
		if ( project != null && project.exists() && project.isOpen() ) {
			List list = CDebugUtils.getReferencedProjects( project );
			HashSet names = new HashSet( list.size() + 1 );
			names.add( project.getName() );
			Iterator it = list.iterator();
			while( it.hasNext() ) {
				names.add( ((IProject)it.next()).getName() );
			}
			for( int i = 0; i < locations.length; ++i )
				if ( locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation)locations[i]).isGeneric() )
					names.remove( ((IProjectSourceLocation)locations[i]).getProject().getName() );
			it = names.iterator();
			while( it.hasNext() ) {
				Element child = doc.createElement( DISABLED_GENERIC_PROJECT_NAME );
				child.setAttribute( ATTR_PROJECT_NAME, (String)it.next() );
				node.appendChild( child );
			}
		}
	}

	private void saveAdditionalSourceLocations( ICSourceLocation[] locations, Document doc, Element node ) {
		for( int i = 0; i < locations.length; i++ ) {
			if ( locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation)locations[i]).isGeneric() )
				continue;
			Element child = doc.createElement( ADDITIONAL_SOURCE_LOCATION_NAME );
			child.setAttribute( ATTR_CLASS, locations[i].getClass().getName() );
			try {
				child.setAttribute( ATTR_MEMENTO, locations[i].getMemento() );
			}
			catch( CoreException e ) {
				CDebugCorePlugin.log( e );
				continue;
			}
			node.appendChild( child );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#getProject()
	 */
	public IProject getProject() {
		return fProject;
	}

	protected void setProject( IProject project ) {
		fProject = project;
	}

	private boolean isReferencedProject( IProject ref ) {
		if ( getProject() != null ) {
			try {
				return Arrays.asList( getProject().getReferencedProjects() ).contains( ref );
			}
			catch( CoreException e ) {
				CDebugCorePlugin.log( e );
			}
		}
		return false;
	}

	private void setReferencedProjects() {
		fReferencedProjects.clear();
		fReferencedProjects = CDebugUtils.getReferencedProjects( getProject() );
	}

	protected ICSourceLocation[] getDefaultSourceLocations() {
		Iterator it = fReferencedProjects.iterator();
		ArrayList list = new ArrayList( fReferencedProjects.size() );
		if ( getProject() != null && getProject().exists() && getProject().isOpen() )
			list.add( SourceLookupFactory.createProjectSourceLocation( getProject() ) );
		while( it.hasNext() ) {
			IProject project = (IProject)it.next();
			if ( project != null && project.exists() && project.isOpen() )
				list.add( SourceLookupFactory.createProjectSourceLocation( project ) );
		}
		return (ICSourceLocation[])list.toArray( new ICSourceLocation[list.size()] );
	}

	private void resetSourceLocations( List affectedProjects ) {
		if ( affectedProjects.size() != 0 && getProject() != null ) {
			if ( !getProject().exists() || !getProject().isOpen() ) {
				removeGenericSourceLocations();
			}
			else {
				updateGenericSourceLocations( affectedProjects );
			}
		}
	}

	private void removeGenericSourceLocations() {
		fReferencedProjects.clear();
		ICSourceLocation[] locations = getSourceLocations();
		ArrayList newLocations = new ArrayList( locations.length );
		for( int i = 0; i < locations.length; ++i )
			if ( !(locations[i] instanceof IProjectSourceLocation) || !((IProjectSourceLocation)locations[i]).isGeneric() )
				newLocations.add( locations[i] );
		setSourceLocations( (ICSourceLocation[])newLocations.toArray( new ICSourceLocation[newLocations.size()] ) );
	}

	private void updateGenericSourceLocations( List affectedProjects ) {
		List newRefs = CDebugUtils.getReferencedProjects( getProject() );
		ICSourceLocation[] locations = getSourceLocations();
		ArrayList newLocations = new ArrayList( locations.length );
		for( int i = 0; i < locations.length; ++i ) {
			if ( !(locations[i] instanceof IProjectSourceLocation) || !((IProjectSourceLocation)locations[i]).isGeneric() ) {
				newLocations.add( locations[i] );
			}
			else {
				IProject project = ((IProjectSourceLocation)locations[i]).getProject();
				if ( project.exists() && project.isOpen() ) {
					if ( newRefs.contains( project ) || project.equals( getProject() ) ) {
						newLocations.add( locations[i] );
						newRefs.remove( project );
					}
				}
			}
		}
		Iterator it = newRefs.iterator();
		while( it.hasNext() ) {
			IProject project = (IProject)it.next();
			if ( !fReferencedProjects.contains( project ) )
				newLocations.add( SourceLookupFactory.createProjectSourceLocation( project ) );
		}
		fReferencedProjects = newRefs;
		setSourceLocations( (ICSourceLocation[])newLocations.toArray( new ICSourceLocation[newLocations.size()] ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator#searchForDuplicateFiles()
	 */
	public boolean searchForDuplicateFiles() {
		return fDuplicateFiles;
	}

	public void setSearchForDuplicateFiles( boolean search ) {
		fDuplicateFiles = search;
		ICSourceLocation[] locations = getSourceLocations();
		for( int i = 0; i < locations.length; ++i )
			locations[i].setSearchForDuplicateFiles( search );
	}
}
