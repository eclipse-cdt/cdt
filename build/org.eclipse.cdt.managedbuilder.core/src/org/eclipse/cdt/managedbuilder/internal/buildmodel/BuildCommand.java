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
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.core.runtime.IPath;

/**
 *
 */
public class BuildCommand implements IBuildCommand {
	private IPath fCmd;
	private String fArgs[];
	private Map fEnv;
	private IPath fCWD;
	private BuildStep fStep;
	
	public BuildCommand(IPath cmd, String args[], Map env, IPath cwd, BuildStep step){
		fCmd = cmd;
		if(args != null)
			fArgs = (String[])args.clone();
		if(env != null)
			fEnv = new HashMap(env);
		
		fCWD = cwd;
		
		fStep = step;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getCommand()
	 */
	public IPath getCommand() {
		return fCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getArgs()
	 */
	public String[] getArgs() {
		if(fArgs != null)
			return (String[])fArgs.clone();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getEnvironment()
	 */
	public Map getEnvironment() {
		if(fEnv != null)
			return new HashMap(fEnv);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getCWD()
	 */
	public IPath getCWD() {
		return fCWD;
	}

}
