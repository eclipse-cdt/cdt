/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import java.io.File;
import java.io.IOException;

/**
 * Configurable strategy for canonicalizing file paths. File paths can be canonicalized by calling
 * either File.getCanonicalPath or File.getAbsolutePath. File.getCanonicalPath resolves symbolic
 * links and guarantees path uniqueness. File.getAbsolutePath can be used when resolution of
 * symbolic links is undesirable. The default is to use File.getCanonicalPath.  
 */
public abstract class PathCanonicalizationStrategy {
	private static volatile PathCanonicalizationStrategy instance;

	static {
		setPathCanonicalization(true);
	}

	public static String getCanonicalPath(File file) {
		return instance.getCanonicalPathInternal(file);
	}

	/**
	 * Sets path canonicalization strategy. If <code>canonicalize</code> is <code>true</code>,
	 * file paths will be canonicalized by calling File.getCanonicalPath, otherwise
	 * File.getAbsolutePath is used.
	 * 
	 * @param canonicalize <code>true</code> to use File.getCanonicalPath, <code>false</code>
	 * to use File.getAbsolutePath.
	 */
	public static void setPathCanonicalization(boolean canonicalize) {
		if (canonicalize) {
			instance = new PathCanonicalizationStrategy() {
				@Override
				protected String getCanonicalPathInternal(File file) {
					try {
						return file.getCanonicalPath();
					} catch (IOException e) {
						return file.getAbsolutePath();
					}
				}
			};
		} else {
			instance = new PathCanonicalizationStrategy() {
				@Override
				protected String getCanonicalPathInternal(File file) {
					return file.getAbsolutePath();
				}
			};
		}
	}

	protected abstract String getCanonicalPathInternal(File file);
}
