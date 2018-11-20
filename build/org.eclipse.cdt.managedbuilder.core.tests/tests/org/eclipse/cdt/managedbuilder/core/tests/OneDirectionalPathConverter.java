/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class OneDirectionalPathConverter implements IOptionPathConverter {
	public static Path PREFIX = new Path("/test");

	@Override
	public IPath convertToPlatformLocation(String toolSpecificPath, IOption option, ITool tool) {
		IPath path = new Path(toolSpecificPath);
		if (path.isAbsolute())
			return PREFIX.append(toolSpecificPath);
		return path;
	}

}
