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

/**
 * Class that keeps information about a remote resource. Clients should not
 * use this class.
 */
public class SystemRemoteResourceInfo implements Cloneable {



	/** 
	 * Set of flags which reflect various states of the info.
	 */
	private int flags = 0;

	/**
	 * The set of markers belonging to the resource.
	 */
	private SystemRemoteMarkerSet markers;

	/**
	 * Constructor for SystemRemoteResourceInfo.
	 */
	public SystemRemoteResourceInfo() {
		super();
	}

	/** 
	 * Sets all of the bits indicated by the mask.
	 * @param the mask
	 */
	public void set(int mask) {
		flags |= mask;
	}

	/** 
	 * Clears all of the bits indicated by the mask.
	 * @param the mask
	 */
	public void clear(int mask) {
		flags &= ~mask;
	}

	/** 
	 * Returns the flags for this info.
	 * @return the flags
	 */
	public int getFlags() {
		return flags;
	}
	
	/** 
 	 * Sets the flags for this info.
 	 * @return the flags
 	 */
	public void setFlags(int value) {
		flags = value;
	}

	/**
	 * Set the markers for the resource info. <code>null</code> can be used
	 * to indicate that the resource has no markers.
	 * @param the marker set
	 */
	public void setMarkers(SystemRemoteMarkerSet markerSet) {
		this.markers = markerSet;
	}

	/**
	 * Get the markers from the resource info.
	 * @return the marker set
	 */
	public SystemRemoteMarkerSet getMarkers() {
		return markers;
	}
}