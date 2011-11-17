/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
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

public class ProjectConverter20 implements IConvertManagedBuildObject {

	@Override
	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {

		String tmpDir = System.getProperty("java.io.tmpdir");	//$NON-NLS-1$

		File outputFile = new File(tmpDir + "/converterOutput20.txt");	//$NON-NLS-1$
		try {
			FileWriter out = new FileWriter(outputFile);
			out.write("Converter for CDT 2.0 Project is invoked");	//$NON-NLS-1$
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
			System.out.println("Exception raised.");	//$NON-NLS-1$
		}
		return buildObj;
	}

}
