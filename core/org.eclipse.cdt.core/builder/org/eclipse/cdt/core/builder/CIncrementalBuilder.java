package org.eclipse.cdt.core.builder;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 *  
 * Note: This class/interface is part of an interim API that is still under development and
 * expected to change significantly before reaching stability. It is being made available at
 * this early stage to solicit feedback from pioneering adopters on the understanding that any
 * code that uses this API will almost certainly be broken (repeatedly) as the API evolves.
 */
public class CIncrementalBuilder extends IncrementalProjectBuilder {
	int kind;
	Map args;
	IProgressMonitor monitor;

	private ICBuilder fCurrentBuilder;

	public int getkind() {
		return kind;
	}

	public Map getMap() {
		return args;
	}

	public IProgressMonitor monitor() {
		return monitor;
	}

	public IConsole getConsole() {
		String id = fCurrentBuilder.getID();
		return CCorePlugin.getDefault().getConsole(id);
	}

	//FIXME: Not implemented
	public IPath getBuildDirectory() {
		return getProject().getLocation();
	}

	//FIXME: Not implemented
	public String[] getBuildParameters() {
		return new String[0];
	}

	protected  IProject[] build(int kind, Map args, IProgressMonitor monitor)
		throws CoreException {

		this.kind = kind;
		this.args = args;
		this.monitor = monitor;

		// Get the ICBuilder
		ICBuilder cbuilder[] = getCBuilder();

		// FIXME: Check preference for non-modal builds
		fCurrentBuilder = cbuilder[0];
		return fCurrentBuilder.build(this);
	}

	//FIXME: Not implemented
	private ICBuilder[] getCBuilder () throws CoreException {
		return CCorePlugin.getDefault().getBuilders(getProject());
	}
}
