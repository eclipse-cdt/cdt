/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.gnu.cygwin;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;

public class CygwinPathResolver implements IBuildPathResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildPathResolver#resolveBuildPaths(int, java.lang.String, java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration)
	 */
	public String[] resolveBuildPaths(int pathType, String variableName,
			String variableValue, IConfiguration configuration) {
		// TODO implement
		return new String[0];
	}

}
