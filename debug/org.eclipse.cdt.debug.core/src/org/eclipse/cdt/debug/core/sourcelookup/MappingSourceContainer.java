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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
 
/**
 * The source container for path mappings.
 */
public class MappingSourceContainer extends AbstractSourceContainer {

	/**
	 * Unique identifier for the mapping source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.mapping</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.mapping";	 //$NON-NLS-1$

	private ArrayList fContainers;

	/** 
	 * Constructor for MappingSourceContainer. 
	 */
	public MappingSourceContainer() {
		fContainers = new ArrayList();
	}

	/** 
	 * Constructor for MappingSourceContainer. 
	 */
	public MappingSourceContainer( MapEntrySourceContainer[] entries ) {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return "Path Mappings";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType( TYPE_ID );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return !fContainers.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements( String name ) throws CoreException {
		return findSourceElements( name, getSourceContainers() );
	}

	protected Object[] findSourceElements( String name, ISourceContainer[] containers ) throws CoreException {
		List results = null;
		CoreException single = null;
		MultiStatus multiStatus = null;
		if ( isFindDuplicates() ) {
			results = new ArrayList();
		}
		for( int i = 0; i < containers.length; i++ ) {
			ISourceContainer container = containers[i];
			try {
				Object[] objects = container.findSourceElements( name );
				if ( objects.length > 0 ) {
					if ( isFindDuplicates() ) {
						for( int j = 0; j < objects.length; j++ ) {
							results.add( objects[j] );
						}
					}
					else {
						if ( objects.length == 1 ) {
							return objects;
						}
						return new Object[]{ objects[0] };
					}
				}
			}
			catch( CoreException e ) {
				if ( single == null ) {
					single = e;
				}
				else if ( multiStatus == null ) {
					multiStatus = new MultiStatus( DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, new IStatus[]{ single.getStatus() }, SourceLookupMessages.getString( "CompositeSourceContainer.0" ), null ); //$NON-NLS-1$
					multiStatus.add( e.getStatus() );
				}
				else {
					multiStatus.add( e.getStatus() );
				}
			}
		}
		if ( results == null ) {
			if ( multiStatus != null ) {
				throw new CoreException( multiStatus );
			}
			else if ( single != null ) {
				throw single;
			}
			return EMPTY;
		}
		return results.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer#getSourceContainers()
	 */
	public ISourceContainer[] getSourceContainers() throws CoreException {
		return (MapEntrySourceContainer[])fContainers.toArray( new MapEntrySourceContainer[fContainers.size()] );
	}

	public void addMapEntry( MapEntrySourceContainer entry ) {
		fContainers.add( entry );
	}

	public void addMapEntries( MapEntrySourceContainer[] entries ) {
		fContainers.addAll( Arrays.asList( entries ) );
	}

	public void removeMapEntry( MapEntrySourceContainer entry ) {
		fContainers.remove( entry );
	}

	public void removeMapEntries( MapEntrySourceContainer[] entries ) {
		fContainers.removeAll( Arrays.asList( entries ) );
	}

	public void clear() {
		Iterator it = fContainers.iterator();
		while( it.hasNext() ) {
			((ISourceContainer)it.next()).dispose();
		}
		fContainers.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#dispose()
	 */
	public void dispose() {
		super.dispose();
		Iterator it = fContainers.iterator();
		while( it.hasNext() ) {
			((ISourceContainer)it.next()).dispose();
		}
		fContainers.clear();
	}
}
