/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.executables;


/**
 * ISourceFileRemapping is used by the Executables Manager when finding missing
 * source files.
 * 
 * @author Ken Ryall
 * 
 */
public interface ISourceFileRemapping {

	/**
	 * @since 6.0
	 */
	String remapSourceFile(Executable executable, String filePath);

}