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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
 
/**
 * The source container that maps a backend path to the local filesystem path.
 */
public class MapEntrySourceContainer extends AbstractSourceContainer {

	/**
	 * Unique identifier for the map entry source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.mapEntry</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.mapEntry";	 //$NON-NLS-1$

	private IPath fLocalPath;

	private IPath fBackendPath;

	/** 
	 * Constructor for MapEntrySourceContainer. 
	 */
	public MapEntrySourceContainer( IPath backend, IPath local ) {
		fBackendPath = backend;
		fLocalPath = local;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements( String name ) throws CoreException {
		IPath path = new Path( name );
		if ( getBackendPath().isPrefixOf( path ) ) {
			path = path.removeFirstSegments( getBackendPath().segmentCount() );
			path = getLocalPath().append( path );

			IFile[] wsFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation( path );
			ArrayList list = new ArrayList();
			for( int j = 0; j < wsFiles.length; ++j ) {
				if ( wsFiles[j].exists() ) {
					list.add( wsFiles[j] );
					if ( !isFindDuplicates() )
						break;
				}
			}
			if ( list.size() > 0 ) 
				return list.toArray();

			File file = path.toFile();
			if ( file.exists() && file.isFile() ) {
				return new Object[] { new LocalFileStorage( file ) };
			}
		}
		return EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return MessageFormat.format( "{0} - {1}", new String[] { getBackendPath().toString(), getLocalPath().toOSString() } ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType( TYPE_ID );
	}
	
	protected IPath getLocalPath() {
		return fLocalPath;
	}
	
	protected IPath getBackendPath() {
		return fBackendPath;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object o ) {
		if ( !(o instanceof MapEntrySourceContainer ) )
			return false;
		MapEntrySourceContainer entry = (MapEntrySourceContainer)o;
		return ( entry.getBackendPath().equals( getBackendPath() ) && entry.getLocalPath().equals( getLocalPath() ) );
	}
}
