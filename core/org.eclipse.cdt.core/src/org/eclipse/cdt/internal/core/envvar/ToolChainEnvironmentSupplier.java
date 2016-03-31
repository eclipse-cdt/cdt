/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.resources.IBuildConfiguration;

public class ToolChainEnvironmentSupplier implements ICoreEnvironmentVariableSupplier {

	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if (context instanceof IBuildConfiguration) {
			ICBuildConfiguration config = ((IBuildConfiguration) context)
					.getAdapter(ICBuildConfiguration.class);
			if (config != null) {
				IToolChain toolChain = config.getToolChain();
				if (toolChain != null) {
					return toolChain.getVariable(name);
				}
			}
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if (context instanceof IBuildConfiguration) {
			ICBuildConfiguration config = ((IBuildConfiguration) context)
					.getAdapter(ICBuildConfiguration.class);
			if (config != null) {
				IToolChain toolChain = config.getToolChain();
				if (toolChain != null) {
					return toolChain.getVariables();
				}
			}
		}
		return null;
	}

	@Override
	public boolean appendEnvironment(Object context) {
		return true;
	}

}
