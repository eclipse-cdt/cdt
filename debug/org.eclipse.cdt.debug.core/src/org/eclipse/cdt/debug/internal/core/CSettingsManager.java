/*******************************************************************************
 * Copyright (c) 2007, 2008 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IDebugTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Settings manager
 * 
 * The settings manager stores a set of settings, 
 * (key/value) pairs in the launch configuration so they exist across debug sessions.
 * 
 * All active settings are stored together in a single configuration entry 
 * (ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_FORMAT).
 * 
 * Every setting is identified by a string identifier. That string identifier can be used to 
 * store an additional setting, to remove an exiting one or to retrieve a previously stored setting.
 *
 * The setting value consists of a String.
 *
 * Settings fade out automatically so clients do not necessarily need to delete old settings. This makes it 
 * possible to build the string identifiers with names out of the user application, like function names or 
 * variable names, without the danger of a constantly growing launch configuration.
 * However it also causes that the settings manager must only be used for configurations and customizations for which
 * always reasonable defaults exist.
 *
 * As cleanup policy the settings manager only keeps a certain number of settings and drops the 
 * least recently used one when more settings are added. The least recently used order is maintained 
 * across debug sessions. 
 * 
 */

public class CSettingsManager {

	/**
	 * the name of the XML node for the list 
	 */
	private static final String CONTENT_LIST = "contentList"; //$NON-NLS-1$
	
	/**
	 * the name of the XML node for every format entry 
	 */
	private static final String CONTENT = "content"; //$NON-NLS-1$
	
	/**
	 * the attribute name used to identify the object to store the content for. 
	 */
	private static final String ATTR_CONTENT_ID = "id"; //$NON-NLS-1$
	
	/**
	 * the attribute name of the actual content
	 */
	private static final String ATTR_CONTENT_VALUE = "val"; //$NON-NLS-1$

	/**
	 * Number defining how many settings are stored.
	 * Whenever an additional setting is added when there are already MAX_USED_COUNT settings, the 
	 * least recently used setting is dropped. 
	 *
	 * The actual value is chosen to be high enough for normal use cases, but still low enough to avoid that the launch configuration
	 * gets arbitrarily large 
	 */
	private static int MAX_ELEMENT_COUNT = 100;

	/**
	 * the map used to actually store the format information
	 * as key String are used, values are of type String too.
	 * 
	 * The map automatically is limited to MAX_ELEMENT_COUNT
	 * elements, dropping the least recently used one
	 * when more elements are added.
	 */
	private Map fContentMap = new LinkedHashMap(MAX_ELEMENT_COUNT, 0.75f, true) {
		private static final long serialVersionUID = 1;
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > MAX_ELEMENT_COUNT;
		}
	};

	/**
	 * the debug target we store the values for
	 */
	private CDebugTarget fDebugTarget;
	
	/**
	 * Store the value for the given id.
	 * @param id used to identify the information. Different objects/topics should use different identifiers.
	 * @param value content to be stored
	 */
	public synchronized void putValue( String id, String value ) {
		fContentMap.put(id, value);
	}
	/**
	 * remove the stored format for the given id.
	 * @param id used to identify the formatting information. Different objects/topics should use different identifiers.
	 */
	public synchronized void removeValue( String id ) {
		fContentMap.remove( id );
	}

	/** Retrieve the value for the given id.
	 * @param id used to identify the formatting information. Different objects/topics should use different identifiers.
	 * @return returns the entry information for the given id, or null if no such information is available.
	 */
	public synchronized String getValue( String id ) {
		String entry= (String) fContentMap.get( id );
		return entry;
	}

	/** constructor.
	 * @param debugTarget
	 */
	public CSettingsManager( CDebugTarget debugTarget ) {
		fDebugTarget = debugTarget;
		initialize();
	}
	
	/** get the string format of the current content.
	 * Only stores entries which have been used in the last MAX_USED_COUNT debug sessions.
	 * @return
	 */
	private String getMemento() {
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element node = document.createElement( CONTENT_LIST );
			document.appendChild( node );
			Set entrySet = fContentMap.entrySet();
			Iterator it =  entrySet.iterator();
			while ( it.hasNext() ) {
				Map.Entry entry= (Map.Entry) it.next();
				String id= (String)entry.getKey();
				String value= (String)entry.getValue();
				Element child = document.createElement( CONTENT );
				child.setAttribute( ATTR_CONTENT_ID, id );
				child.setAttribute( ATTR_CONTENT_VALUE, value );
				node.appendChild( child );
			}
			return CDebugUtils.serializeDocument( document, false );
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

	/** set the current state to the one given by the memento.
	 * @param memento a string representation of the state to be loaded.
	 * @throws CoreException
	 */
	private void initializeFromMemento( String memento ) throws CoreException {
		try {
			fContentMap.clear();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			Element root = parser.parse( source ).getDocumentElement();
			if ( root.getNodeName().equalsIgnoreCase( CONTENT_LIST ) ) {
				NodeList list = root.getChildNodes();
				int i = list.getLength() - 1; // backwards to keep least recent access order.
				for( ; i >= 0; i-- ) {
					Node node = list.item( i );
					short type = node.getNodeType();
					if ( type == Node.ELEMENT_NODE ) {
						Element elem = (Element)node;
						if ( elem.getNodeName().equalsIgnoreCase( CONTENT ) ) {
							String id = elem.getAttribute( ATTR_CONTENT_ID );
							String value= elem.getAttribute( ATTR_CONTENT_VALUE );
							if ( id == null || id.length() == 0 ) {
								DebugPlugin.logMessage( "unexpected entry in CSettingsManager.initializeFromMemento", null ); //$NON-NLS-1$
								continue;
							}
							putValue( id, value );
						}
					}
				}
				return;
			}
			DebugPlugin.logMessage( "unexpected content", null ); //$NON-NLS-1$
		}
		catch( ParserConfigurationException e ) {
			DebugPlugin.log( e );
		}
		catch( SAXException e ) {
			DebugPlugin.log( e );
		}
		catch( IOException e ) {
			DebugPlugin.log( e );
		}
	}

	/**
	 * read the stored format from the launch configuration
	 */
	private void initialize() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			String memento = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_FORMAT, "" ); //$NON-NLS-1$
			if ( memento != null && memento.trim().length() != 0 )
				initializeFromMemento( memento );
		}
		catch( CoreException e ) {
			DebugPlugin.log( e );
		}
	}

	/**
	 * store the current content in the launch configuration.
	 */
	public synchronized void save() {
		ILaunchConfiguration config = getDebugTarget().getLaunch().getLaunchConfiguration();
		try {
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			wc.setAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_FORMAT, getMemento() );
			wc.doSave();
		}
		catch( CoreException e ) {
			DebugPlugin.log( e );
		}
	}
	
	/**
	 * accessor to the debug target
	 */
	IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
}
