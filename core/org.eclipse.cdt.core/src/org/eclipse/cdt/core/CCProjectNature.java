package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.internal.CCorePlugin;
import org.eclipse.cdt.core.*;


public class CCProjectNature extends CProjectNature {

	public static final String CC_NATURE_ID= CCorePlugin.PLUGIN_ID + ".ccnature";

	public static void addCCNature(IProject project, IProgressMonitor mon) throws CoreException {
		addNature(project, CC_NATURE_ID, mon);
	}

}
