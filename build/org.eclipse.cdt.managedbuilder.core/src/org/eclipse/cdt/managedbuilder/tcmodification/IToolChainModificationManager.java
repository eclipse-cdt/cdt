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
package org.eclipse.cdt.managedbuilder.tcmodification;

import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;

public interface IToolChainModificationManager {
	int OBJECT_CONFIGURATION = IRealBuildObjectAssociation.OBJECT_CONFIGURATION;
	int OBJECT_TOOLCHAIN = IRealBuildObjectAssociation.OBJECT_TOOLCHAIN;
	int OBJECT_TOOL = IRealBuildObjectAssociation.OBJECT_TOOL;
	int OBJECT_BUILDER = IRealBuildObjectAssociation.OBJECT_BUILDER;
	
	/**
	 * returns the modification info calculator for the given folder info
	 * if the folder info is a root folder info,
	 * returns the {@link IConfigurationModification}
	 * 
	 * @param rcInfo
	 * @return
	 */
	IFolderInfoModification getModification(IFolderInfo rcInfo);

	/**
	 * returns the modification info calculator for the given file info
	 * 
	 * @param rcInfo
	 * @return
	 */
	IFileInfoModification getModification(IFileInfo rcInfo);
}
