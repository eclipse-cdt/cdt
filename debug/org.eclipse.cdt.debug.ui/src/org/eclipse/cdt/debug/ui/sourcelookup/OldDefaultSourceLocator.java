/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLookupFactory;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
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
 * Old default source locator. We keep it for migration purposes.
 */
public class OldDefaultSourceLocator implements IPersistableSourceLocator, IAdaptable {

	/**
	 * Identifier for the 'Default C/C++ Source Locator' extension (value <code>"org.eclipse.cdt.debug.ui.DefaultSourceLocator"</code>).
	 */
	public static final String ID_DEFAULT_SOURCE_LOCATOR = CDebugUIPlugin.getUniqueIdentifier() + ".DefaultSourceLocator"; //$NON-NLS-1$

	// to support old configurations
	public static final String ID_OLD_DEFAULT_SOURCE_LOCATOR = "org.eclipse.cdt.launch" + ".DefaultSourceLocator"; //$NON-NLS-1$ //$NON-NLS-2$

	protected static final String ELEMENT_NAME = "PromptingSourceLocator"; //$NON-NLS-1$

	private static final String ATTR_PROJECT = "project"; //$NON-NLS-1$

	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	/**
	 * Underlying source locator.
	 */
	private ICSourceLocator fSourceLocator;

	public OldDefaultSourceLocator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException {
		if ( getCSourceLocator() != null ) {
			Document document = null;
			Throwable ex = null;
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element element = document.createElement( ELEMENT_NAME );
				document.appendChild( element );
				element.setAttribute( ATTR_PROJECT, getCSourceLocator().getProject().getName() );
				IPersistableSourceLocator psl = getPersistableSourceLocator();
				if ( psl != null ) {
					element.setAttribute( ATTR_MEMENTO, psl.getMemento() );
				}
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
			abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.1" ), ex ); //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
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
			if ( !root.getNodeName().equalsIgnoreCase( ELEMENT_NAME ) ) {
				abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.2" ), null ); //$NON-NLS-1$
			}
			String projectName = root.getAttribute( ATTR_PROJECT );
			String data = root.getAttribute( ATTR_MEMENTO );
			if ( isEmpty( projectName ) ) {
				abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.3" ), null ); //$NON-NLS-1$
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( getCSourceLocator() == null )
				setCSourceLocator( SourceLookupFactory.createSourceLocator( project ) );
			if ( getCSourceLocator().getProject() != null && !getCSourceLocator().getProject().equals( project ) )
				return;
			if ( project == null || !project.exists() || !project.isOpen() )
				abort( MessageFormat.format( SourceLookupMessages.getString( "OldDefaultSourceLocator.4" ), new String[]{ projectName } ), null ); //$NON-NLS-1$
			IPersistableSourceLocator psl = getPersistableSourceLocator();
			if ( psl != null )
				psl.initializeFromMemento( data );
			else
				abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.5" ), null );  //$NON-NLS-1$
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
		abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.6" ), ex ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException {
		setCSourceLocator( SourceLookupFactory.createSourceLocator( getProject( configuration ) ) );
		String memento = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "" ); //$NON-NLS-1$
		if ( !isEmpty( memento ) )
			initializeFromMemento( memento );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( getCSourceLocator() instanceof IAdaptable ) {
			if ( adapter.equals( ICSourceLocator.class ) ) {
				return ((IAdaptable)getCSourceLocator()).getAdapter( adapter );
			}
			if ( adapter.equals( IResourceChangeListener.class ) ) {
				return ((IAdaptable)getCSourceLocator()).getAdapter( adapter );
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(org.eclipse.debug.core.model.IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame ) {
		return null;
	}

	private ICSourceLocator getCSourceLocator() {
		return fSourceLocator;
	}

	private void setCSourceLocator( ICSourceLocator locator ) {
		fSourceLocator = locator;
	}

	private IPersistableSourceLocator getPersistableSourceLocator() {
		ICSourceLocator sl = getCSourceLocator();
		return (sl instanceof IPersistableSourceLocator) ? (IPersistableSourceLocator)sl : null;
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException {
		IStatus s = new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), 0, message, e );
		throw new CoreException( s );
	}

	private boolean isEmpty( String string ) {
		return string == null || string.trim().length() == 0;
	}

	private IProject getProject( ILaunchConfiguration configuration ) throws CoreException {
		String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
		if ( !isEmpty( projectName ) ) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project.exists() ) {
				return project;
			}
		}
		abort( MessageFormat.format( SourceLookupMessages.getString( "OldDefaultSourceLocator.9" ), new String[]{ projectName } ), null ); //$NON-NLS-1$
		return null;
	}
}
