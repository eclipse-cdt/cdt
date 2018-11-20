/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import org.eclipse.cdt.make.internal.core.scannerconfig2.GCCSpecsRunSIProvider;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.runtime.Path;

/**
 * @author laggarcia
 *
 */
public class XlCSpecsRunSIProvider extends GCCSpecsRunSIProvider {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	@Override
	protected boolean initialize() {

		boolean rc = super.initialize();

		if (rc) {
			try {
				this.fCompileCommand = new Path(ManagedBuildManager.getBuildMacroProvider().resolveValue(
						this.fCompileCommand.toString(), EMPTY_STRING, null, IBuildMacroProvider.CONTEXT_CONFIGURATION,
						ManagedBuildManager.getBuildInfo(this.resource.getProject()).getDefaultConfiguration()));
			} catch (BuildMacroException e) {
				e.printStackTrace();
				return false;
			}
		}

		return rc;

	}

}
