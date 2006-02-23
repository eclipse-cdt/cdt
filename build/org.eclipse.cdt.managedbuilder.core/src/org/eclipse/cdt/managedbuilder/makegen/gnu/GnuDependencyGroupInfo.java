/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import java.util.ArrayList;

/**
 * 
 * This class contains the desciption of a group of generated dependency files,
 * e.g., .d files created by compilations
 *
 */

public class GnuDependencyGroupInfo {
	
	//  Member Variables
	String groupBuildVar;
	boolean conditionallyInclude;
	ArrayList groupFiles;
	
	//  Constructor
	public GnuDependencyGroupInfo(String groupName, boolean bConditionallyInclude) {
		groupBuildVar = groupName;
		conditionallyInclude = bConditionallyInclude;
		//  Note: not yet needed
		groupFiles = null;
	}

}
