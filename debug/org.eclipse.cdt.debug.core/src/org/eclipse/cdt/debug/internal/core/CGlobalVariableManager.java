/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core; 

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CVariable;
import org.eclipse.cdt.debug.internal.core.model.CVariableFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages all global variables registered with a debug target.
 */
public class CGlobalVariableManager implements ICGlobalVariableManager {

	private static final String GLOBAL_VARIABLE_LIST = "globalVariableList"; //$NON-NLS-1$
	private static final String GLOBAL_VARIABLE = "globalVariable"; //$NON-NLS-1$
	private static final String ATTR_GLOBAL_VARIABLE_PATH = "path"; //$NON-NLS-1$
	private static final String ATTR_GLOBAL_VARIABLE_NAME = "name"; //$NON-NLS-1$

	private CDebugTarget fDebugTarget;

	private IGlobalVariableDescriptor[] fInitialDescriptors = new IGlobalVariableDescriptor[0];

	private ArrayList fGlobals;

	/** 
	 * Constructor for CGlobalVariableManager. 
	 */
	public CGlobalVariableManager( CDebugTarget target ) {
		super();
		setDebugTarget( target );
		initialize();
	}

	protected CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	
	private void setDebugTarget( CDebugTarget debugTarget ) {
		fDebugTarget = debugTarget;
	}

	public ICGlobalVariable[] getGlobals() {
		if ( fGlobals == null ) {
			try {
				addGlobals( getInitialDescriptors() );
			}
			catch( DebugException e ) {
				DebugPlugin.log( e );
			}
		}
		return (ICGlobalVariable[])fGlobals.toArray( new ICGlobalVariable[fGlobals.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#addGlobals(IGlobalVariableDescriptor[])
	 */
	public void addGlobals( IGlobalVariableDescriptor[] descriptors ) throws DebugException {
		fGlobals = new ArrayList( 10 );
		MultiStatus ms = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(), 0, "", null ); //$NON-NLS-1$
		ArrayList globals = new ArrayList( descriptors.length );
		for ( int i = 0; i < descriptors.length; ++i ) {
			try {
				globals.add( getDebugTarget().createGlobalVariable( descriptors[i] ) );
			}
			catch( DebugException e ) {
				ms.add( e.getStatus() );
			}
		}
		if ( globals.size() > 0 ) {
			synchronized( fGlobals ) {
				fGlobals.addAll( globals );
			}
			getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
		}
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#removeGlobals(ICGlobalVariable[])
	 */
	public void removeGlobals( ICGlobalVariable[] globals ) {
		synchronized( fGlobals ) {
			fGlobals.removeAll( Arrays.asList( globals ) );
		}
		for ( int i = 0; i < globals.length; ++i ) {
			if ( globals[i] instanceof CVariable )
				((CVariable)globals[i]).dispose();
		}
		getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#removeAllGlobals()
	 */
	public void removeAllGlobals() {
		ICGlobalVariable[] globals = new ICGlobalVariable[0];
		synchronized( fGlobals ) {
			globals = (ICGlobalVariable[])fGlobals.toArray( new ICGlobalVariable[fGlobals.size()] );
			fGlobals.clear();
		}
		for ( int i = 0; i < globals.length; ++i ) {
			if ( globals[i] instanceof CVariable )
				((CVariable)globals[i]).dispose();
		}
		getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
	}

	public void dispose() {
		if ( fGlobals != null ) {
			Iterator it = fGlobals.iterator();
			while( it.hasNext() ) {
				((CVariable)it.next()).dispose();
			}
			fGlobals.clear();
			fGlobals = null;
		}
	}

	public String getMemento() {
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element node = document.createElement( GLOBAL_VARIABLE_LIST );
			document.appendChild( node );
			ICGlobalVariable[] globals = getGlobals();
			for ( int i = 0; i < globals.length; ++i ) {
				IGlobalVariableDescriptor descriptor = globals[i].getDescriptor();
				Element child = document.createElement( GLOBAL_VARIABLE );
				child.setAttribute( ATTR_GLOBAL_VARIABLE_NAME, descriptor.getName() );
				child.setAttribute( ATTR_GLOBAL_VARIABLE_PATH, descriptor.getPath().toOSString() );
				node.appendChild( child );
			}
			return CDebugUtils.serializeDocument( document );
		}
		catch( ParserConfigurationException e ) {
			DebugPlugin.log( e );
		}
		catch( IOException e ) {
			DebugPlugin.log( e );
		}
		catch( TransformerException e ) {
			DebugPlugin.log( e );
		}
		return null;
	}

	private void initializeFromMemento( String memento ) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();
			if ( root.getNodeName().equalsIgnoreCase( GLOBAL_VARIABLE_LIST ) ) {
				List descriptors = new ArrayList();
				NodeList list = root.getChildNodes();
				int length = list.getLength();
				for( int i = 0; i < length; ++i ) {
					Node node = list.item( i );
					short type = node.getNodeType();
					if ( type == Node.ELEMENT_NODE ) {
						Element entry = (Element)node;
						if ( entry.getNodeName().equalsIgnoreCase( GLOBAL_VARIABLE ) ) {
							String name = entry.getAttribute( ATTR_GLOBAL_VARIABLE_NAME );
							String pathString = entry.getAttribute( ATTR_GLOBAL_VARIABLE_PATH );
							IPath path = new Path( pathString );
							if ( path.isValidPath( pathString ) ) {
								descriptors.add( CVariableFactory.createGlobalVariableDescriptor( name, path ) );
							}
						}
					}
				}
				fInitialDescriptors = (IGlobalVariableDescriptor[])descriptors.toArray( new IGlobalVariableDescriptor[descriptors.size()] );
				return;
			}
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
		abort( InternalDebugCoreMessages.getString( "CGlobalVariableManager.0" ), ex ); //$NON-NLS-1$
	}

	private void initialize() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			String memento = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_GLOBAL_VARIABLES, "" ); //$NON-NLS-1$
			if ( memento != null && memento.trim().length() != 0 )
				initializeFromMemento( memento );
		}
		catch( CoreException e ) {
			DebugPlugin.log( e );
		}
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException {
		IStatus s = new Status( IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), CDebugCorePlugin.INTERNAL_ERROR, message, e );
		throw new CoreException( s );
	}

	/**
	 * @return Returns the initialDescriptors.
	 */
	private IGlobalVariableDescriptor[] getInitialDescriptors() {
		return fInitialDescriptors;
	}

	public void save() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_GLOBAL_VARIABLES, getMemento() );
			wc.doSave();
		}
		catch( CoreException e ) {
			DebugPlugin.log( e );
		}
	}
}
