/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
 
/**
 * The source container for path mappings.
 */
public class MappingSourceContainer extends AbstractSourceContainer implements IMappingSourceContainer {
	/**
	 * Unique identifier for the mapping source container type
	 * (value <code>org.eclipse.cdt.debug.core.containerType.mapping</code>).
	 */
	public static final String TYPE_ID = CDebugCorePlugin.getUniqueIdentifier() + ".containerType.mapping";	//$NON-NLS-1$

	private String fName;
	private ArrayList<MapEntrySourceContainer> fContainers;

	/** 
	 * Constructor for MappingSourceContainer. 
	 */
	public MappingSourceContainer(String name) {
		fName = name;
		fContainers = new ArrayList<MapEntrySourceContainer>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	@Override
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	@Override
	public boolean isComposite() {
		return !fContainers.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	@Override
	public Object[] findSourceElements(String name) throws CoreException {
		return findSourceElements(name, getSourceContainers());
	}

	protected Object[] findSourceElements(String name, ISourceContainer[] containers) throws CoreException {
		List<Object> results = null;
		CoreException single = null;
		MultiStatus multiStatus = null;
		if (isFindDuplicates()) {
			results = new ArrayList<Object>();
		}
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = containers[i];
			try {
				container.init(this.getDirector());
				Object[] objects = container.findSourceElements(name);
				if (objects.length > 0) {
					if (isFindDuplicates() && results != null) {
						for (int j = 0; j < objects.length; j++) {
							results.add(objects[j]);
						}
					} else {
						if (objects.length == 1) {
							return objects;
						}
						return new Object[]{ objects[0] };
					}
				}
			} catch (CoreException e) {
				if (single == null) {
					single = e;
				} else if (multiStatus == null) {
					multiStatus = new MultiStatus(DebugPlugin.getUniqueIdentifier(),
							DebugPlugin.INTERNAL_ERROR, new IStatus[] { single.getStatus() },
							SourceLookupMessages.MappingSourceContainer_0, null);
					multiStatus.add(e.getStatus());
				} else {
					multiStatus.add(e.getStatus());
				}
			}
		}
		if (results == null) {
			if (multiStatus != null) {
				throw new CoreException(multiStatus);
			} else if (single != null) {
				throw single;
			}
			return EMPTY;
		}
		return results.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer#getSourceContainers()
	 */
	@Override
	public ISourceContainer[] getSourceContainers() throws CoreException {
		return fContainers.toArray(new MapEntrySourceContainer[fContainers.size()]);
	}

	public void addMapEntry(MapEntrySourceContainer entry) {
		fContainers.add(entry);
	}

	public void addMapEntries(MapEntrySourceContainer[] entries) {
		fContainers.addAll(Arrays.asList(entries));
	}

	public void removeMapEntry(MapEntrySourceContainer entry) {
		fContainers.remove(entry);
	}

	public void removeMapEntries(MapEntrySourceContainer[] entries) {
		fContainers.removeAll(Arrays.asList(entries));
	}

	public void clear() {
		Iterator<MapEntrySourceContainer> it = fContainers.iterator();
		while (it.hasNext()) {
			((ISourceContainer) it.next()).dispose();
		}
		fContainers.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		Iterator<MapEntrySourceContainer> it = fContainers.iterator();
		while (it.hasNext()) {
			((ISourceContainer) it.next()).dispose();
		}
		fContainers.clear();
	}

	public MappingSourceContainer copy() {
		MappingSourceContainer copy = new MappingSourceContainer(fName);
		MapEntrySourceContainer[] entries = new MapEntrySourceContainer[fContainers.size()];
		for (int i = 0; i < entries.length; ++i) {
			copy.addMapEntry(fContainers.get(i).copy());
		}
		return copy;
	}
	
	public void setName(String name) {
		fName = name;
	}

	/* (non-Javadoc)
	 * @see IMappingSourceContainer#getCompilationPath(String)
	 */
	@Override
	public IPath getCompilationPath(String sourceName) {
		IPath path = new Path(sourceName);
		IPath result = null;
		try {
			ISourceContainer[] containers = getSourceContainers();
			for (int i = 0; i < containers.length; ++i) {
				MapEntrySourceContainer entry = (MapEntrySourceContainer) containers[i];
				IPath local = entry.getLocalPath();
				if (local.isPrefixOf(path)) {
					result = entry.getBackendPath().append(path.removeFirstSegments(local.segmentCount()));
					break;
				}
			}
		} catch (CoreException e) {
			CDebugCorePlugin.log(e);
		}
		return result;
	}
}
