/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.runtime.IPath;

public class MakeBuilder extends AbstractCExtension /*implements ICBuilder */ {

	public IPath[] getIncludePaths() {
		return new IPath[0];
	}

	public void setIncludePaths(IPath[] incPaths) {
	}

	public IPath[] getLibraryPaths() {
		return new IPath[0];
	}

	public void setLibraryPaths(IPath[] libPaths) {
	}

	public String[] getLibraries() {
		return new String[0];
	}

	public void setLibraries(String[] libs) {
	}

//	public IOptimization getOptimization() {
//		return null;
//	}
//
//	public IProject[] build(CIncrementalBuilder cbuilder) {
//		ICExtensionReference ref = getExtensionReference();
//		System.out.println("MakeBuilder!!!!\n Command is:" + ref.getExtensionData("command"));
//		return null;
//	}
//
//	public void setOptimization(IOptimization o) {
//	}

	public String getID() {
		return MakeCorePlugin.getUniqueIdentifier() + ".makeBuilder";
	}


}
