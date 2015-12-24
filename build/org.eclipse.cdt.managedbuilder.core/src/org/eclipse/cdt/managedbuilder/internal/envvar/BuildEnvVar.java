/*******************************************************************************
 * Copyright (c) 2005, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;

/**
 * a trivial implementation of the IBuildEnvironmentVariable
 *
 * @since 3.0
 */
public class BuildEnvVar extends EnvironmentVariable implements IBuildEnvironmentVariable {
	protected BuildEnvVar(){

	}

	public BuildEnvVar(String name){
		super(name);
	}

	public BuildEnvVar(String name, String value){
		super(name, value);
	}

	public BuildEnvVar(String name, String value, String delimiter){
		super(name, value, delimiter);
	}

	public BuildEnvVar(String name, String value, int op){
		super(name, value, op, EnvironmentVariableManager.getDefault().getDefaultDelimiter());
	}

	public BuildEnvVar(String name, String value, int op, String delimiter){
		super(name, value, op, delimiter);
	}

	public BuildEnvVar(IEnvironmentVariable var){
		super(var);
	}
}
