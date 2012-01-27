/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.xlc.aix;

import org.eclipse.cdt.managedbuilder.core.IBuildPathResolver;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;

public class AixPathResolver implements IBuildPathResolver {
	static final String DELIMITER_AIX = ":";

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildPathResolver#resolveBuildPaths(int, java.lang.String, java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration)
	 */
	@Override
	public String[] resolveBuildPaths(int pathType, String variableName,
			String variableValue, IConfiguration configuration) {
		return variableValue.split(DELIMITER_AIX);
	}
}
