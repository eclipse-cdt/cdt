/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Doug Schaefer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

/**
 * Represents a build configuration.
 * 
 * @author Doug Schaefer
 * @since 5.1
 */
public interface IConfiguration {

	/**
	 * Get the scanner info for the given resource in this configuration.
	 * 
	 * @param resource
	 * @return scanner info for this resource in this configuration
	 */
	IScannerInfo getScannerInfo(IResource resource);

}
