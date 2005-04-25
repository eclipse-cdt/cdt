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

import org.eclipse.cdt.debug.core.sourcelookup.CDirectorySourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
/**
 * See <code>CDirectorySourceContainer</code>.
 */
public class CDirectorySourceContainerType extends AbstractSourceContainerTypeDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#createSourceContainer(java.lang.String)
	 */
	public ISourceContainer createSourceContainer( String memento ) throws CoreException {
		Node node = parseDocument( memento );
		if ( node.getNodeType() == Node.ELEMENT_NODE ) {
			Element element = (Element)node;
			if ( "directory".equals( element.getNodeName() ) ) { //$NON-NLS-1$
				String string = element.getAttribute( "path" ); //$NON-NLS-1$
				if ( string == null || string.length() == 0 ) {
					abort( InternalSourceLookupMessages.getString( "CDirectorySourceContainerType.0" ), null ); //$NON-NLS-1$
				}
				String nest = element.getAttribute( "nest" ); //$NON-NLS-1$
				boolean nested = "true".equals( nest ); //$NON-NLS-1$
				return new CDirectorySourceContainer( new Path( string ), nested );
			}
			abort( InternalSourceLookupMessages.getString( "CDirectorySourceContainerType.1" ), null ); //$NON-NLS-1$
		}
		abort( InternalSourceLookupMessages.getString( "CDirectorySourceContainerType.2" ), null ); //$NON-NLS-1$
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainerType#getMemento(org.eclipse.debug.internal.core.sourcelookup.ISourceContainer)
	 */
	public String getMemento( ISourceContainer container ) throws CoreException {
		CDirectorySourceContainer folder = (CDirectorySourceContainer)container;
		Document document = newDocument();
		Element element = document.createElement( "directory" ); //$NON-NLS-1$
		element.setAttribute( "path", folder.getDirectory().getAbsolutePath() ); //$NON-NLS-1$
		String nest = "false"; //$NON-NLS-1$
		if ( folder.isComposite() ) {
			nest = "true"; //$NON-NLS-1$
		}
		element.setAttribute( "nest", nest ); //$NON-NLS-1$
		document.appendChild( element );
		return serializeDocument( document );
	}
}
