/*******************************************************************************
 * Copyright (c) 2005, 2013 Intel Corporation and others.
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
package org.eclipse.cdt.internal.core.envvar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;

/**
 * This class implements the "merging" functionality of environment variables
 * Used by the EnvironmentVariableProvider to "merge" the sets of macros returned
 * by different suppliers into one set returned to the user
 *
 * @since 3.0
 *
 */
public class EnvVarCollector {
	private Map<String, EnvVarDescriptor> fMap = null;

	public EnvVarCollector() {

	}

	/**
	 * adds an array of environment variables to the set of variables held by this collector
	 * performing environment variable operations
	 * @param vars
	 */
	public void add(IEnvironmentVariable vars[]) {
		add(vars, null, -1, null);
	}

	public void add(IEnvironmentVariable vars[], IEnvironmentContextInfo info, int num,
			ICoreEnvironmentVariableSupplier supplier) {
		if (vars == null)
			return;
		boolean isCaseInsensitive = !EnvironmentVariableManager.getDefault().isVariableCaseSensitive();
		for (int i = 0; i < vars.length; i++) {
			IEnvironmentVariable var = vars[i];
			if (var != null) {
				String name = var.getName();
				if (isCaseInsensitive)
					name = name.toUpperCase();

				boolean noCheck = false;

				if (fMap == null) {
					noCheck = true;
					fMap = new HashMap<>();
				}

				EnvVarDescriptor des = null;
				if (noCheck || (des = fMap.get(name)) == null) {
					des = new EnvVarDescriptor(var, info, num, supplier);
					fMap.put(name, des);
				} else {
					des.setContextInfo(info);
					des.setSupplierNum(num);
					des.setVariable(EnvVarOperationProcessor.performOperation(des.getOriginalVariable(), var));
				}
			}
		}
	}

	/**
	 * Returns an array of variables held by this collector
	 *
	 * @param includeRemoved true if removed variables should be included in the resulting array
	 * @return IBuildEnvironmentVariable[]
	 */
	public EnvVarDescriptor[] toArray(boolean includeRemoved) {
		if (fMap == null)
			return new EnvVarDescriptor[0];
		Collection<EnvVarDescriptor> values = fMap.values();
		List<EnvVarDescriptor> list = new ArrayList<>();
		Iterator<EnvVarDescriptor> iter = values.iterator();
		while (iter.hasNext()) {
			EnvVarDescriptor des = iter.next();
			if (des != null && (includeRemoved || des.getOperation() != IEnvironmentVariable.ENVVAR_REMOVE))
				list.add(des);
		}
		return list.toArray(new EnvVarDescriptor[list.size()]);
	}

	/**
	 * Returns a variable of a given name held by this collector
	 *
	 * @param name a variable name
	 * @return IBuildEnvironmentVariable
	 */
	public EnvVarDescriptor getVariable(String name) {
		if (fMap == null)
			return null;

		if (!EnvironmentVariableManager.getDefault().isVariableCaseSensitive())
			name = name.toUpperCase();

		return fMap.get(name);
	}

	/**
	 * Returns an array of variables held by this collector
	 * The call to this method is equivalent of calling toArray(true)
	 *
	 * @return IBuildEnvironmentVariable[]
	 */
	public EnvVarDescriptor[] getVariables() {
		return toArray(true);
	}

	public void clear() {
		if (fMap != null)
			fMap.clear();
	}

}
