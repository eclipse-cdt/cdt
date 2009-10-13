/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.net.URI;

/**
 * An interface for utility classes that can extract meaningful information from EFS filesystems.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 *
 * @author crecoskie
 * @since 5.0.3
 *
 */
public interface IFilesystemUtility {
	/**
	 * Gets the path corresponding to the underlying file as the operating system on the target machine would see it.
	 * In the future, it would be better if EFS had an API for this.
	 * 
	 * @param locationURI
	 * @return String representing the path, or null if there is an error or if there is no such physical file.
	 */
	public String getPathFromURI(URI locationURI);
	
	/**
	 * In the case of a managed (linked) filesystem, returns the URI that this URI ultimately will
	 * point to.
	 * 
	 * @param locationURI
	 * @return URI
	 */
	public URI getBaseURI(URI locationURI);
	
	
	/**
	 * Creates a new URI on the same filesystem as another URI, but with a different path.
	 * 
	 * @param locationOnSameFilesystem A URI pointing to another resource on the same filesystem that this resource
	 * should be on.
	 * @param path The absolute path to the resource.
	 * @return URI
	 */
	public URI replacePathInURI(URI locationOnSameFilesystem, String path);
}
