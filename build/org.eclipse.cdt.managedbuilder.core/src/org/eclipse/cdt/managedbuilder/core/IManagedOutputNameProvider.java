/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import org.eclipse.core.runtime.IPath;

public interface IManagedOutputNameProvider {
	/**
	 * 
	 * @param tool
	 * @param primaryInputNames
	 * @return IPath[]
	 */
	public IPath[] getOutputNames( ITool tool, IPath[] primaryInputNames );
}
