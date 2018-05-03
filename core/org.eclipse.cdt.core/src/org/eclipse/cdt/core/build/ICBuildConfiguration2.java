/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;

/**
 * @since 6.5
 */
public interface ICBuildConfiguration2 {

	/**
	 * Mark the Build Configuration as active
	 */
	void setActive();

	/**
	 * The URI for the directory in which the build is executed.
	 */
	URI getBuildDirectoryURI() throws CoreException;

}
