/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core.make;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.builder.CIncrementalBuilder;
import org.eclipse.cdt.core.builder.ICBuilder;
import org.eclipse.cdt.core.builder.IOptimization;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class MakeBuilder extends AbstractCExtension implements ICBuilder {

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

	public IOptimization getOptimization() {
		return null;
	}

	public IProject[] build(CIncrementalBuilder cbuilder) {
		ICExtensionReference ref = getExtensionReference();
		System.out.println("MakeBuilder!!!!\n Command is:" + ref.getExtensionData("command"));
		return null;
	}

	public String getID() {
		return CCorePlugin.PLUGIN_ID + ".makeBuilder";
	}

	public void setOptimization(IOptimization o) {
	}

}
