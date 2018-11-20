/*******************************************************************************
 * Copyright (C) 2006, 2011 Siemens AG.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionPathConverter;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This minimalistic testing implementation does not actually change the path
 * It just converts to an IPath object
 * TestPathConverter1 is the converter which can be inherited from the toolchain
 */
public class TestPathConverter1 implements IOptionPathConverter {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionPathConverter#convertToPlatformLocation(java.lang.String)
	 */
	@Override
	public IPath convertToPlatformLocation(String toolSpecificPath, IOption option, ITool tool) {
		Path path = new Path(toolSpecificPath);
		return path;
	}

}
