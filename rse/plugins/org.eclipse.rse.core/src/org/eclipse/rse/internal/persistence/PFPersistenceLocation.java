/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.internal.persistence;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

interface PFPersistenceLocation {

	/**
	 * @return true if the location actually exists
	 */
	boolean exists();
	
	/**
	 * Ensures the existence of the base location
	 */
	void ensure();
	
	/**
	 * @return true if the location has contents associated with it. The
	 * contents may be retrieved using getContents()
	 */
	boolean hasContents();
	
	/**
	 * @return the URI of this location. Should only be used for display
	 * purposes since the protocol is not guaranteed to be understood by
	 * any existing URI processor.
	 */
	URI getLocator();

	/**
	 * @return the locations of the children of this location. It is possible
	 * for a location to have both contents and children. Since this location
	 * is a handle it may not exist, in which case this returns an empty array.
	 */
	PFPersistenceLocation[] getChildren();
	
	
	/**
	 * Return the child of a particular simple name. This is a handle operation.
	 * The child does not need to exist to for this to produce a location.
	 * @param childName The name of the child.
	 * @return The child of that name.
	 */
	PFPersistenceLocation getChild(String childName);
	
	/**
	 * @return The simple name of this location, relative to its parent location.
	 */
	String getName();

	/**
	 * Keeps only those children from this location that are in the keep set.
	 * Typically used to clean renamed nodes from the tree on a save operation.
	 * If the location does not yet exist, this does nothing.
	 * @param keepSet The names of the children that should be kept. Others are discarded.
	 */
	void keepChildren(Set keepSet);
	
	/**
	 * Sets the contents of this location to a particular stream.
	 * Implementations must close this stream when finished.
	 * This forces the location to come into existence if it does not already exist.
	 * @param stream the stream from which to read the new contents of this location.
	 */
	void setContents(InputStream stream);
	
	/**
	 * Returns an open stream which can be read to retrieve the contents of this
	 * location. The client is responsible for closing this stream.
	 * If the location does not yet exist this will return null and log the attempt.
	 * @return a stream that will retrieve the contents of this location.
	 */
	InputStream getContents();
}
