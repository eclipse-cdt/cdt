/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.build.crossgcc;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;

public class CrossCommandLineGenerator extends ManagedCommandLineGenerator {

	@Override
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool, String commandName, String[] flags,
			String outputFlag, String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {
		IBuildObject parent = tool.getParent();
		IToolChain toolchain;
		if (parent instanceof ResourceConfiguration)
			toolchain = ((ResourceConfiguration) parent).getBaseToolChain();
		else
			toolchain = (IToolChain) parent;

		IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.prefix"); //$NON-NLS-1$
		String prefix = (String) option.getValue();
		String newCommandName = prefix + commandName;
		return super.generateCommandLineInfo(tool, newCommandName, flags, outputFlag, outputPrefix, outputName,
				inputResources, commandLinePattern);
	}

}
