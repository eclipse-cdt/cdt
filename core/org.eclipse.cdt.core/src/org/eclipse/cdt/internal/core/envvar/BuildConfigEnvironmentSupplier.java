/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;

public class BuildConfigEnvironmentSupplier implements ICoreEnvironmentVariableSupplier {

	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if (context instanceof IBuildConfiguration) {
			ICBuildConfiguration cconfig = ((IBuildConfiguration) context).getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				try {
					return cconfig.getVariable(name);
				} catch (CoreException e) {
					CCorePlugin.log(e);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if (context instanceof IBuildConfiguration) {
			ICBuildConfiguration cconfig = ((IBuildConfiguration) context).getAdapter(ICBuildConfiguration.class);
			if (cconfig != null) {
				try {
					return cconfig.getVariables();
				} catch (CoreException e) {
					CCorePlugin.log(e);
					return null;
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
