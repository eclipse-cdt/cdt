/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.resources;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Allows for looking up resources by location or name.
 */
public class ResourceLookup {
	private static ResourceLookupImpl sInstance= new ResourceLookupImpl();

	public static void startup() {
		sInstance.startup();
	}
	
	public static void shutdown() {
		sInstance.shutdown();
	}
	
	public static IFile[] findFilesForLocation(URI location, IProject[] projects) {
		return sInstance.findFilesForLocation(location, projects);
	}
	
	/**
	 * Searches for files with the given location suffix. 
	 * @param locationSuffix the suffix to match, always used as relative path.
	 * @param projects the projects to search
	 * @param ignoreCase whether or not to ignore case when comparing the suffix.
	 */
	public static IFile[] findFilesByName(IPath locationSuffix, IProject[] projects, boolean ignoreCase) {
		return sInstance.findFilesByName(locationSuffix, projects, ignoreCase);
	}

	/** 
	 * For testing, only.
	 */
	public static void dump() {
		sInstance.dump();
	}
	/** 
	 * For testing, only.
	 */
	public static void unrefNodeMap() {
		sInstance.unrefNodeMap();
	}
	/** 
	 * For testing, only.
	 */
	public static void simulateNodeMapCollection() {
		sInstance.simulateNodeMapCollection();
	}
}
