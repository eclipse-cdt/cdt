/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
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
package org.eclipse.cdt.utils.envvar;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

public class EnvironmentCollector {
	private Map<String, IEnvironmentVariable> fEnfironmentMap = new HashMap<>();

	public IEnvironmentVariable addVariable(IEnvironmentVariable var) {
		if (var == null)
			return null;

		String name = var.getName();

		name = EnvVarOperationProcessor.normalizeName(name);

		if (name != null) {
			IEnvironmentVariable old = fEnfironmentMap.get(name);
			if (old != null) {
				var = EnvVarOperationProcessor.performOperation(old, var);
			}
			fEnfironmentMap.put(name, var);
		}

		return var;
	}

	public void addVariables(IEnvironmentVariable[] vars) {
		if (vars == null)
			return;
		for (int i = 0; i < vars.length; i++) {
			addVariable(vars[i]);
		}
	}

	public IEnvironmentVariable getVariable(String name) {
		name = EnvVarOperationProcessor.normalizeName(name);
		if (name != null)
			return fEnfironmentMap.get(name);
		return null;
	}

	public IEnvironmentVariable[] getVariables() {
		return fEnfironmentMap.values().toArray(new IEnvironmentVariable[fEnfironmentMap.size()]);
	}
}
