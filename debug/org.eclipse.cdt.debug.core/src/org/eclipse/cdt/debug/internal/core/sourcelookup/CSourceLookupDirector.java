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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
 
/**
 * C/C++ source lookup director.
 */
public class CSourceLookupDirector extends AbstractSourceLookupDirector {

	private static Set fFilteredTypes;

	static {
		fFilteredTypes = new HashSet();
		fFilteredTypes.add( WorkspaceSourceContainer.TYPE_ID );
		fFilteredTypes.add( ProjectSourceContainer.TYPE_ID );
		fFilteredTypes.add( FolderSourceContainer.TYPE_ID );
		fFilteredTypes.add( DirectorySourceContainer.TYPE_ID );
		fFilteredTypes.add( MappingSourceContainer.TYPE_ID );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants( new ISourceLookupParticipant[]{ new CSourceLookupParticipant() } );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#supportsSourceContainerType(org.eclipse.debug.core.sourcelookup.ISourceContainerType)
	 */
	public boolean supportsSourceContainerType( ISourceContainerType type ) {
		return fFilteredTypes.contains( type.getId() );
	}
}
