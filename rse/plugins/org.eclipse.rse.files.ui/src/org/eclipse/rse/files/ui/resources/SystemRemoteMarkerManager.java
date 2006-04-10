/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This singleton class manages all remote markers.
 */
public class SystemRemoteMarkerManager implements ISystemRemoteManager {



	private static SystemRemoteMarkerManager instance;

	// cache of marker type definitions
	private SystemRemoteMarkerTypeDefinitionCache cache;

	// the next marker id
	private long nextMarkerId = 0;

	// constant to indicate no marker infos
	private static final SystemRemoteMarkerInfo[] NO_MARKER_INFO = new SystemRemoteMarkerInfo[0];

	// constant to indicate no markers
	private static final ISystemRemoteMarker[] NO_MARKER = new ISystemRemoteMarker[0];

	/**
	 * Constructor for SystemRemoteMarkerManager.
	 */
	private SystemRemoteMarkerManager() {
		super();
		cache = new SystemRemoteMarkerTypeDefinitionCache();
	}

	/**
	 * Get the singleton instance.
	 * @return the manager object
	 */
	public static SystemRemoteMarkerManager getInstance() {

		if (instance == null) {
			instance = new SystemRemoteMarkerManager();
		}

		return instance;
	}

	/**
	 * Get the cache.
	 * @return the cache
	 */
	public SystemRemoteMarkerTypeDefinitionCache getCache() {
		return cache;
	}

	/**
	 * Get the next marker id.
	 * @return the next marker id
	 */
	public long getNextMarkerId() {
		return nextMarkerId++;
	}

	/**
	 * Adds the given markers to the given resource.
	 * @param the resource
	 * @param the marker infos 
	 */
	public void add(ISystemRemoteResource resource, SystemRemoteMarkerInfo[] newMarkers) {

		if (newMarkers.length == 0)
			return;

		SystemRemoteResource target = (SystemRemoteResource) resource;
		SystemRemoteResourceInfo info = target.getResourceInfo();

		if (info == null) {
			return;
		}

		// set the flag to indicate that the resource's markers have changed
		if (isPersistent(newMarkers))
			info.set(ISystemRemoteCoreConstants.M_MARKERS_DIRTY);

		SystemRemoteMarkerSet markers = info.getMarkers();

		if (markers == null) {
			markers = new SystemRemoteMarkerSet(newMarkers.length);
		}

		basicAdd(resource, markers, newMarkers);

		if (!markers.isEmpty()) {
			info.setMarkers(markers);
		}
	}

	/**
	 * Adds the new markers to the given set of markers for the given resource.
	 * @param the resource
	 * @param the set of markers for the resource
	 * @param the new marker infos
	 */
	private void basicAdd(ISystemRemoteResource resource, SystemRemoteMarkerSet markers, SystemRemoteMarkerInfo[] newMarkers) {
		
		for (int i = 0; i < newMarkers.length; i++) {
			
			SystemRemoteMarkerInfo newMarker = newMarkers[i];
			newMarker.setId(getNextMarkerId());
			markers.add(newMarker);
		}
	}

	/**
	 * Removes a marker.
	 * @param the resource.
	 * @param the marker id
	 */
	public void removeMarker(ISystemRemoteResource resource, long id) {
		SystemRemoteMarkerInfo markerInfo = findMarkerInfo(resource, id);
	
		if (markerInfo == null)
			return;
	
		SystemRemoteResourceInfo info = ((SystemRemoteResource)(resource)).getResourceInfo();
		
		if (info == null) {
			return;
		}
		
		SystemRemoteMarkerSet markers = info.getMarkers();
		int size = markers.size();
		markers.remove(markerInfo);
		
		// if that was the last marker remove the set to save space.
		if (markers.size() == 0) {
			info.setMarkers(null);
		}
		
		// if we actually did remove a marker, post a delta for the change.
		if (markers.size() != size) {
			
			if (isPersistent(markerInfo)) {
				info.set(ISystemRemoteCoreConstants.M_MARKERS_DIRTY);
			}
		}
	}

	/**
	 * Finds a marker info given a resource, and the marker id.
	 * @param the resource.
	 * @param the marker id
	 * @return the marker info
	 */
	public SystemRemoteMarkerInfo findMarkerInfo(ISystemRemoteResource resource, long id) {
		SystemRemoteResourceInfo info = ((SystemRemoteResource)(resource)).getResourceInfo();
	
		if (info == null) {
			return null;
		}
		
		SystemRemoteMarkerSet markers = info.getMarkers();
	
		if (markers == null) {
			return null;
		}
		
		return (SystemRemoteMarkerInfo)(markers.get(id));
	}
	
	/**
	 * Removes markers of the given type, and optionally all the subtypes, from
	 * the given resource.
	 * @param the resource
	 * @param the type
	 * @param flag indicating whether to include subtypes
	 */
	public void removeMarkers(ISystemRemoteResource resource, String type, boolean includeSubtypes) {
		SystemRemoteResourceInfo info = ((SystemRemoteResource)(resource)).getResourceInfo();
		
		if (info == null) {
			return;
		}
		
		SystemRemoteMarkerSet markers = info.getMarkers();
		
		if (markers == null) {
			return;
		}
		
		ISystemRemoteMarkerSetElement[] matching;
		
		// if type is null, then we want all the markers
		// otherwise we need a subset
		if (type == null) {		
			matching = markers.elements();
			info.setMarkers(null);
		}
		else {
			matching = basicFindMatching(markers, type, includeSubtypes);
			
			// if nothing matches, simply return
			if (matching.length == 0) {
				return;
			}
			else {
				markers.removeAll(matching);
				
				// if the marker set is empty, then make it null to save memory
				if (markers.size() == 0) {
					info.setMarkers(null);
				}
			}
		}	
	}
	
	/**
 	 * Returns the markers in the given set of markers which match the given type,
 	 * and optionally including subtypes.
 	 * @param the marker set
 	 * @param the type
 	 * @param flag indicating whether to include subtypes
 	 */
	private SystemRemoteMarkerInfo[] basicFindMatching(SystemRemoteMarkerSet markers, String type, boolean includeSubtypes) {
		int size = markers.size();
		
		if (size <= 0) {
			return NO_MARKER_INFO;
		}
		
		List result = new ArrayList(size);
		
		ISystemRemoteMarkerSetElement[] elements = markers.elements();
	
		for (int i = 0; i < elements.length; i++) {
			SystemRemoteMarkerInfo marker = (SystemRemoteMarkerInfo)(elements[i]);
		
			// if the type is null, then we are looking for all types of markers
			if (type == null) {
				result.add(marker);
			}
			else {
				if (includeSubtypes) {
				
					if (cache.isSubtype(marker.getType(), type)) {
						result.add(marker);
					}
				}
				else {
				
					if (marker.getType().equals(type)) {
						result.add(marker);
					}
				}
			}
		}
	
		size = result.size();
	
		if (size <= 0) {
			return NO_MARKER_INFO;
		}
	
		return (SystemRemoteMarkerInfo[])(result.toArray(new SystemRemoteMarkerInfo[size]));
	}
	
	/**
 	 * Returns the marker with the given id or <code>null</code> if none is found.
 	 * @param the resource
 	 * @param the id of the marker to find
 	 * @return the marker, or <code>null</code> if none s found.
 	 */
	public ISystemRemoteMarker findMarker(ISystemRemoteResource resource, long id) {
		SystemRemoteMarkerInfo info = findMarkerInfo(resource, id);
		return info == null ? null : new SystemRemoteMarker(resource, info.getId());
	}
	
	/**
 	 * Returns all markers of the specified type on the given target, and optionally the subtypes as well.
 	 * Passing <code>null</code> for the type specifies a matching target for all types.
 	 * @param the resource
 	 * @param the type
 	 * @param flag indicating whether to include subtypes
  	 */
	public ISystemRemoteMarker[] findMarkers(ISystemRemoteResource resource, String type, boolean includeSubtypes) {
		SystemRemoteResourceInfo info = ((SystemRemoteResource)resource).getResourceInfo();
		
		if (info == null) {
			return NO_MARKER;
		}
		
		ArrayList result = new ArrayList();
		
		SystemRemoteMarkerSet markers = info.getMarkers();
	
		// add the matching markers for this resource
		if (markers != null) {
			
			ISystemRemoteMarkerSetElement[] matching;
			
			if (type == null) {
				matching = markers.elements();
			}
			else {
				matching = basicFindMatching(markers, type, includeSubtypes);
			}
		
			buildMarkers(resource, matching, result);
		}
		
		return (ISystemRemoteMarker[])(result.toArray(new ISystemRemoteMarker[result.size()]));
	}
	
	/**
 	 * Adds the markers on the given target which match the specified type to the list.
 	 * @param the target resource
 	 * @param the marker elements
 	 * @param the list to add to
 	 */
	private void buildMarkers(ISystemRemoteResource resource, ISystemRemoteMarkerSetElement[] markers, ArrayList list) {
	
		if (markers.length == 0) {
			return;
		}
		
		list.ensureCapacity(list.size() + markers.length);
		
		for (int i = 0; i < markers.length; i++) {
			list.add(new SystemRemoteMarker(resource, ((SystemRemoteMarkerInfo)markers[i]).getId()));
		}
	}

	/**
	 * Returns whether marker info is persistent.
	 * @param the marker info
	 * @return true if the given marker represented by this info is persistent,
	 * and false otherwise.
	 */
	public boolean isPersistent(SystemRemoteMarkerInfo info) {
		return cache.isPersistent(info.getType());
	}

	/**
	 * Returns whether given marker is persistent.
	 * @param the marker
	 * @return true if the given marker is persistent, and false
	 * otherwise.
	 */
	public boolean isPersistent(ISystemRemoteMarker marker) {
		return cache.isPersistent(marker.getType());
	}

	/**
	 * Returns whether any of the marker infos are persistent.
	 * @param the marker infos
	 * @return true if the a marker represented by any of the infos is persistent,
	 * and false otherwise.
	 */
	public boolean isPersistent(SystemRemoteMarkerInfo[] infos) {

		for (int i = 0; i < infos.length; i++) {

			if (cache.isPersistent(infos[i].getType())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteManager#startup(IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) {
	}

	/**
	 * @see org.eclipse.rse.files.ui.resources.ISystemRemoteManager#shutdown(IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) {
	}
}