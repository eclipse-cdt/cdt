/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.executables;

import org.eclipse.core.runtime.IPath;

/**
 * ISourceFileRemapping is used by the Executables Manager when finding missing
 * source files.
 * 
 * @author Ken Ryall
 * 
 */
public interface ISourceFileRemapping {

	/**
	 * @since 7.0
	 */
	String remapSourceFile(IPath executable, String filePath);

}