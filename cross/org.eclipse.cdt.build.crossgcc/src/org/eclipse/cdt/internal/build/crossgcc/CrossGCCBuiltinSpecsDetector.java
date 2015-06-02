/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.build.crossgcc;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;

public class CrossGCCBuiltinSpecsDetector extends GCCBuiltinSpecsDetector {

	@Override
	protected String getCompilerCommand(String languageId) {
		// Include the cross command prefix (tool option) in the ${COMMAND} macro
		// For example: "arch-os-" + "gcc"
		String prefix = ""; //$NON-NLS-1$
		IToolChain toolchain = null;
		if (currentCfgDescription != null) {
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(currentCfgDescription);
			toolchain = cfg != null ? cfg.getToolChain() : null;
			if (toolchain != null) {
				IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.prefix"); //$NON-NLS-1$
				if (option != null) {
					prefix = (String) option.getValue();
				}
			}
		}
		return prefix + super.getCompilerCommand(languageId);
	}
}
