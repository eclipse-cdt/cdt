/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
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
}
