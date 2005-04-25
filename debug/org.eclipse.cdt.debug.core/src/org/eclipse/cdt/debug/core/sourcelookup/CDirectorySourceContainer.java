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
package org.eclipse.cdt.debug.core.sourcelookup; 

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
 
/**
 * It is not supposed to subclass DirectorySourceContainer, but we need to use 
 * the different browser.
 */
public class CDirectorySourceContainer extends CompositeSourceContainer {

	/**
	 * Unique identifier for the CDT directory source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.directory</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.directory"; //$NON-NLS-1$

	// root directory
	private File fDirectory;
	// whether to search subfolders
	private boolean fSubfolders = false;

	/**
	 * Consutructs an external folder container for the
	 * directory identified by the given path.
	 * 
	 * @param dirPath path to a directory in the local file system
	 * @param subfolders whether folders within the root directory
	 *  should be searched for source elements
	 */
	public CDirectorySourceContainer( IPath dirPath, boolean subfolders ) {
		this( dirPath.toFile(), subfolders );
	}
	
	/**
	 * Consutructs an external folder container for the
	 * directory identified by the given file.
	 * 
	 * @param dir a directory in the local file system
	 * @param subfolders whether folders within the root directory
	 *  should be searched for source elements
	 */
	public CDirectorySourceContainer( File dir, boolean subfolders ) {
		fDirectory = dir;
		fSubfolders = subfolders;
	}	
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fDirectory.getName();
	}	
	
	/**
	 * Returns the root directory in the local file system associated
	 * with this source container.
	 * 
	 * @return the root directory in the local file system associated
	 * with this source container
	 */
	public File getDirectory() {
		return fDirectory;
	}

	public boolean searchSubfolders() {
		return fSubfolders;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType( TYPE_ID );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements( String name ) throws CoreException {
		ArrayList sources = new ArrayList();
		File directory = getDirectory();
		File file = new File( directory, name );
		if ( file.exists() && file.isFile() ) {
			sources.add( new LocalFileStorage( file ) );
		}
		// check subfolders
		if ( (isFindDuplicates() && fSubfolders) || (sources.isEmpty() && fSubfolders) ) {
			ISourceContainer[] containers = getSourceContainers();
			for( int i = 0; i < containers.length; i++ ) {
				Object[] objects = containers[i].findSourceElements( name );
				if ( objects == null || objects.length == 0 ) {
					continue;
				}
				if ( isFindDuplicates() ) {
					for( int j = 0; j < objects.length; j++ )
						sources.add( objects[j] );
				}
				else {
					sources.add( objects[0] );
					break;
				}
			}
		}
		if ( sources.isEmpty() )
			return EMPTY;
		return sources.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return fSubfolders;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj ) {
		if ( obj instanceof CDirectorySourceContainer ) {
			CDirectorySourceContainer container = (CDirectorySourceContainer)obj;
			return container.getDirectory().equals( getDirectory() );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getDirectory().hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.containers.CompositeSourceContainer#createSourceContainers()
	 */
	protected ISourceContainer[] createSourceContainers() throws CoreException {
		if ( isComposite() ) {
			String[] files = fDirectory.list();
			if ( files != null ) {
				List dirs = new ArrayList();
				for( int i = 0; i < files.length; i++ ) {
					String name = files[i];
					File file = new File( getDirectory(), name );
					if ( file.exists() && file.isDirectory() ) {
						dirs.add( new CDirectorySourceContainer( file, true ) );
					}
				}
				ISourceContainer[] containers = (ISourceContainer[])dirs.toArray( new ISourceContainer[dirs.size()] );
				for( int i = 0; i < containers.length; i++ ) {
					ISourceContainer container = containers[i];
					container.init( getDirector() );
				}
				return containers;
			}
		}
		return new ISourceContainer[0];
	}
}
