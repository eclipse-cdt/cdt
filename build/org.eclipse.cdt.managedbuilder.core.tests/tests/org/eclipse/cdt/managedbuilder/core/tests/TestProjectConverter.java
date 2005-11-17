/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;


public class TestProjectConverter implements IConvertManagedBuildObject {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject#convert(org.eclipse.cdt.managedbuilder.core.IBuildObject, java.lang.String, java.lang.String, boolean)
	 */
	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {
		
		String tmpDir = System.getProperty("java.io.tmpdir");	//$NON-NLS-1$
		
		File outputFile = new File(tmpDir + "/testProjectConverterOutput.txt");	//$NON-NLS-1$
		try {
			FileWriter out = new FileWriter(outputFile);			
			out.write("The converter for the projectType testProject_1.0.0 is invoked");	//$NON-NLS-1$
			out.close();
		} catch (IOException e) {
			System.out.println("Exception raised.");	//$NON-NLS-1$
		}
		return buildObj;
	}
}

