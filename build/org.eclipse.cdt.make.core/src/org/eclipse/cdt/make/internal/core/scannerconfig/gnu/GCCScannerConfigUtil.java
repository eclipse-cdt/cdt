/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * GCC related utility class
 * 
 * @author vhirsl
 */
public class GCCScannerConfigUtil {
	public static final String CPP_SPECS_FILE = "specs.cpp"; //$NON-NLS-1$ 
	public static final String C_SPECS_FILE = "specs.c";  //$NON-NLS-1$

	public static void createSpecs() {
		IPath path = MakeCorePlugin.getWorkingDirectory();
		try {
			createSpecsFile(path, CPP_SPECS_FILE);
			createSpecsFile(path, C_SPECS_FILE);
		} catch (CoreException e) {
			MakeCorePlugin.log(e);
		}
	}

	private static void createSpecsFile(IPath path, String fileName) throws CoreException {
		IPath specs = path.append(fileName);
		File specsFile = specs.toFile();
		if (!specsFile.exists()) {
			try {
				FileOutputStream file = new FileOutputStream(specsFile);
				file.write('\n');
				file.close();
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						MakeCorePlugin.getUniqueIdentifier(), -1,
						MakeMessages.getString("GCCScannerConfigUtil.Error_Message"), e));	//$NON-NLS-1$
			}
		}
	}
}
