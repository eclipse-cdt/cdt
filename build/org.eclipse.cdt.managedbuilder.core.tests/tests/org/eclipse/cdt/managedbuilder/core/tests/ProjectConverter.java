/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;

public class ProjectConverter implements IConvertManagedBuildObject {

	@Override
	public IBuildObject convert(IBuildObject buildObj, String fromId, String toId, boolean isConfirmed) {

		String tmpDir = System.getProperty("java.io.tmpdir");

		File outputFile = new File(tmpDir + "/converterOutput.txt");
		try {
			FileWriter out = new FileWriter(outputFile);
			out.write("---------- Start-------");
			out.write("Converter for the build object : '" + buildObj.getName() + "' is invoked.");
			out.write("From Id : " + fromId);
			out.write("To Id : " + toId);
			out.write("---------- End-------");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
			System.out.println("Exception raised.");
		}

		return buildObj;
	}

}
