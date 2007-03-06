/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


public interface IScannerConfigBuilderInfo2Set {
	/**
	 * 
	 * @return InfoContext - to IScannerConfigBuilderInfo2 map
	 */
	Map getInfoMap();
	
	IScannerConfigBuilderInfo2 getInfo(InfoContext context);
	
	IScannerConfigBuilderInfo2 removeInfo(InfoContext context) throws CoreException;
	
	InfoContext[] getContexts();
	
	IScannerConfigBuilderInfo2 createInfo(InfoContext context, IScannerConfigBuilderInfo2 base);

	IScannerConfigBuilderInfo2 createInfo(InfoContext context, IScannerConfigBuilderInfo2 base, String profileId);

	IScannerConfigBuilderInfo2 createInfo(InfoContext context);

	IScannerConfigBuilderInfo2 createInfo(InfoContext context, String profileId);

	void save() throws CoreException;
	
	IProject getProject();
}
