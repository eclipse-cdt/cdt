/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.core.testplugin;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;


public class CTestPlugin extends Plugin {
	
	public static final String PLUGIN_ID = "org.eclipse.cdt.core.tests";
	private static CTestPlugin fgDefault;
	
	public CTestPlugin() {
		super();
		fgDefault= this;
	}
	
	public static CTestPlugin getDefault() {
		return fgDefault;
	}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public static void enableAutobuild(boolean enable) throws CoreException {
		// disable auto build
		IWorkspace workspace= CTestPlugin.getWorkspace();
		IWorkspaceDescription desc= workspace.getDescription();
		desc.setAutoBuilding(enable);
		workspace.setDescription(desc);
	}
	
	public File getFileInPlugin(IPath path) {
		try {
			return new File(Platform.asLocalURL(find(path)).getFile());
		} catch (IOException e) {
			return null;
		}
	}
	
		

}