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
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
/**
 * The mapping container type.
 */
public class MappingSourceContainerType extends AbstractSourceContainerTypeDelegate {

	private final static String ELEMENT_MAPPING = "mapping"; //$NON-NLS-1$
	private final static String ELEMENT_MAP_ENTRY = "mapEntry"; //$NON-NLS-1$
	private final static String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer( String memento ) throws CoreException {
		Node node = parseDocument( memento );
		if ( node.getNodeType() == Node.ELEMENT_NODE ) {
			Element element = (Element)node;
			if ( ELEMENT_MAPPING.equals( element.getNodeName() ) ) {
				List entries = new ArrayList();
				Node childNode = element.getFirstChild();
				while( childNode != null ) {
					if ( childNode.getNodeType() == Node.ELEMENT_NODE ) {
						Element child = (Element)childNode;
						if ( ELEMENT_MAP_ENTRY.equals( child.getNodeName() ) ) {
							String childMemento = child.getAttribute( ATTR_MEMENTO );
							if ( childMemento == null || childMemento.length() == 0 ) {
								abort( "Source lookup: unable to restore map entry - expecting memnto attribute.", null );
							}
							ISourceContainerType type = DebugPlugin.getDefault().getLaunchManager().getSourceContainerType( MapEntrySourceContainer.TYPE_ID );
							MapEntrySourceContainer entry = (MapEntrySourceContainer)type.createSourceContainer( childMemento );
							entries.add( entry );
						}
					}
					childNode = childNode.getNextSibling();
				}
				MappingSourceContainer container = new MappingSourceContainer();
				Iterator it = entries.iterator();
				while( it.hasNext() ) {
					container.addMapEntry( (MapEntrySourceContainer)it.next() );
				}
				return container;
			}
			abort( "Source lookup: unable to restore mapping - expecting mapping element.", null );
		}
		abort( "Source lookup: unable to restore mapping - invalid memento.", null );
		return null;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerTypeDelegate#getMemento(org.eclipse.debug.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento( ISourceContainer container ) throws CoreException {
		Document document = newDocument();
		Element element = document.createElement( ELEMENT_MAPPING );
		ISourceContainer[] entries = ((MappingSourceContainer)container).getSourceContainers();
		for ( int i = 0; i < entries.length; ++i ) {
			Element child = document.createElement( ELEMENT_MAP_ENTRY );
			child.setAttribute( ATTR_MEMENTO, entries[i].getType().getMemento( entries[i] ) );
			element.appendChild( child );
		}
		document.appendChild( element );
		return serializeDocument( document );
	}
}
