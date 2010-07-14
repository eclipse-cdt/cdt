/*******************************************************************************
 * Copyright (C) 2006 Siemens AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This path converter will be used to test that a conversion actually takes place.
 * It is referenced from the tool pathconvertertest.config.tcyy.toyy.toolchain.tool
 * in the test projecttype.<br>
 * The tool pathconvertertest.config.tcyy.toyy.toolchain.tool inherits a path option
 * from the pathconvertertest.convertingtool tool. The include path option has the 
 * intentionally strange value file:///usr/local/include. 
 * The "file://" part gets stripped away to satisfy the test.
 */
public class TestPathConverter4 extends TestPathConverter1 {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.tests.TestPathConverter1#convertToPlatformLocation(java.lang.String)
	 */
	@Override
	public IPath convertToPlatformLocation(String toolSpecificPath, IOption option, ITool tool) {
		String convertedString = toolSpecificPath.substring("file://".length());
		IPath path = new Path(convertedString);
		return path ;
	}

	
}
