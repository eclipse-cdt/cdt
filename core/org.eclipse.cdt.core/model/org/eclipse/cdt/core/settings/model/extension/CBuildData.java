/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
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
 * Baltasar Belyavsky (Texas Instruments) - bug 340219: Project metadata files are saved unnecessarily
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.runtime.IPath;

public abstract class CBuildData extends CDataObject {
	@Override
	public final int getType() {
		return ICSettingBase.SETTING_BUILD;
	}

	public abstract IPath getBuilderCWD();

	public abstract void setBuilderCWD(IPath path);

	public abstract ICOutputEntry[] getOutputDirectories();

	public abstract void setOutputDirectories(ICOutputEntry[] entries);

	public abstract String[] getErrorParserIDs();

	public abstract void setErrorParserIDs(String[] ids);

	public abstract IEnvironmentContributor getBuildEnvironmentContributor();

	/**
	 * Override to return the build-command overlayed with data (eg. builder-arguments) managed by the build-system.
	 *
	 * @since 5.4
	 */
	public ICommand getBuildSpecCommand() {
		return null;
	}

}
