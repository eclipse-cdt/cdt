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
package org.eclipse.cdt.debug.ui.sourcelookup; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;
import org.w3c.dom.Element;
 
/**
 * The replacement of the old default source locator. Used only for migration purposes.
 */
public class DefaultSourceLocator extends CSourceLookupDirector {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector#initializeFromMemento(java.lang.String, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFromMemento( String memento, ILaunchConfiguration configuration ) throws CoreException {
		Element rootElement = DebugPlugin.parseDocument( memento );
		if ( rootElement.getNodeName().equalsIgnoreCase( OldDefaultSourceLocator.ELEMENT_NAME ) ) {
			initializeFromOldMemento( memento, configuration );
		}
		else {
			super.initializeFromMemento( memento, configuration );
		}
	}

	private void initializeFromOldMemento( String memento, ILaunchConfiguration configuration ) throws CoreException {
		dispose();
		setLaunchConfiguration( configuration );
		OldDefaultSourceLocator old = new OldDefaultSourceLocator();
		old.initializeFromMemento( memento );
		ICSourceLocator csl = (ICSourceLocator)old.getAdapter( ICSourceLocator.class );
		setFindDuplicates( csl.searchForDuplicateFiles() );
		ICSourceLocation[] locations = csl.getSourceLocations();
		
		// Check if the old source locator includes all referenced projects.
		// If so, DefaultSpourceContainer should be used.
		IProject project = csl.getProject();
		List list = CDebugUtils.getReferencedProjects( project );
		HashSet names = new HashSet( list.size() + 1 );
		names.add( project.getName() );
		Iterator it = list.iterator();
		while( it.hasNext() ) {
			names.add( ((IProject)it.next()).getName() );
		}
		boolean includesDefault = true;
		for( int i = 0; i < locations.length; ++i ) {
			if ( locations[i] instanceof IProjectSourceLocation && ((IProjectSourceLocation)locations[i]).isGeneric() ) {
				if ( !names.contains( ((IProjectSourceLocation)locations[i]).getProject().getName() ) ) {
					includesDefault = false;
					break;
				}
			}
		}
		
		// Generate an array of new source containers including DefaultSourceContainer
		ArrayList locs = new ArrayList( locations.length );
		for ( int i = 0; i < locations.length; ++i ) {
			if ( !includesDefault || !( locations[i] instanceof IProjectSourceLocation && names.contains( ((IProjectSourceLocation)locations[i]).getProject().getName() ) ) )
				locs.add( locations[i] );
		}
		
		ISourceContainer[] containers = SourceUtils.convertSourceLocations( (ICSourceLocation[])locs.toArray( new ICSourceLocation[locs.size()] ) );
		ArrayList cons = new ArrayList( Arrays.asList( containers ) );
		if ( includesDefault ) {
			DefaultSourceContainer defaultContainer = new DefaultSourceContainer();
			defaultContainer.init( this );
			cons.add( 0, defaultContainer );
		}
		setSourceContainers( (ISourceContainer[])cons.toArray( new ISourceContainer[cons.size()] ) );
	}
}
