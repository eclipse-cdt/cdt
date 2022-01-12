/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
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
	private Map<String, String> fEnv;
	private IPath fCWD;

	public BuildCommand(IPath cmd, String args[], Map<String, String> env, IPath cwd, BuildStep step) {
		fCmd = cmd;
		if (args != null)
			fArgs = args.clone();
		if (env != null)
			fEnv = new HashMap<>(env);

		fCWD = cwd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getCommand()
	 */
	@Override
	public IPath getCommand() {
		return fCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getArgs()
	 */
	@Override
	public String[] getArgs() {
		if (fArgs != null)
			return fArgs.clone();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getEnvironment()
	 */
	@Override
	public Map<String, String> getEnvironment() {
		if (fEnv != null)
			return new HashMap<>(fEnv);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildCommand#getCWD()
	 */
	@Override
	public IPath getCWD() {
		return fCWD;
	}

}
