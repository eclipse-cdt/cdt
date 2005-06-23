/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
/**
 * The map entry container type.
 */
public class MapEntrySourceContainerType extends AbstractSourceContainerTypeDelegate {

	private final static String ELEMENT_NAME = "mapEntry"; //$NON-NLS-1$
	private final static String BACKEND_PATH = "backendPath"; //$NON-NLS-1$
	private final static String LOCAL_PATH = "localPath"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer( String memento ) throws CoreException {
		Node node = parseDocument( memento );
		if ( node.getNodeType() == Node.ELEMENT_NODE ) {
			Element element = (Element)node;
			if ( ELEMENT_NAME.equals( element.getNodeName() ) ) {
				String path = element.getAttribute( BACKEND_PATH );
				IPath backend = new Path( path );
				if ( !backend.isValidPath( path ) ) {
					abort( InternalSourceLookupMessages.getString( "MapEntrySourceContainerType.0" ), null ); //$NON-NLS-1$
				}
				path = element.getAttribute( LOCAL_PATH );
				IPath local = new Path( path );
				if ( !local.isValidPath( path ) ) {
					abort( InternalSourceLookupMessages.getString( "MapEntrySourceContainerType.1" ), null ); //$NON-NLS-1$
				}
				return new MapEntrySourceContainer( backend, local );
			}
			abort( InternalSourceLookupMessages.getString( "MapEntrySourceContainerType.2" ), null ); //$NON-NLS-1$
		}
		abort( InternalSourceLookupMessages.getString( "MapEntrySourceContainerType.3" ), null ); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento( ISourceContainer container ) throws CoreException {
		MapEntrySourceContainer entry = (MapEntrySourceContainer)container;
		Document document = newDocument();
		Element element = document.createElement( ELEMENT_NAME );
		element.setAttribute( BACKEND_PATH, entry.getBackendPath().toOSString() );
		element.setAttribute( LOCAL_PATH, entry.getLocalPath().toOSString() );
		document.appendChild( element );
		return serializeDocument( document );
	}
}
