/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.core.runtime.IPath;

public interface ICBuildSetting extends ICSettingObject {
	IPath getBuilderCWD();

	void setBuilderCWD(IPath path);

	ICOutputEntry[] getOutputDirectories();
	
	ICOutputEntry[] getResolvedOutputDirectories();
	
	void setOutputDirectories(ICOutputEntry[] entries);
	
	String[] getErrorParserIDs();

	void setErrorParserIDs(String[] ids);
	
	IEnvironmentContributor getBuildEnvironmentContributor();
}
