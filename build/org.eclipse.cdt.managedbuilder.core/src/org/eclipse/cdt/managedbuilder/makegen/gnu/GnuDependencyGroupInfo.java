/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

/**
 *
 * This class contains the description of a group of generated dependency files,
 * e.g., .d files created by compilations
 *
 */

public class GnuDependencyGroupInfo {

	//  Member Variables
	String groupBuildVar;
	boolean conditionallyInclude;
	//	ArrayList groupFiles;

	//  Constructor
	public GnuDependencyGroupInfo(String groupName, boolean bConditionallyInclude) {
		groupBuildVar = groupName;
		conditionallyInclude = bConditionallyInclude;
		//  Note: not yet needed
		//		groupFiles = null;
	}

}
