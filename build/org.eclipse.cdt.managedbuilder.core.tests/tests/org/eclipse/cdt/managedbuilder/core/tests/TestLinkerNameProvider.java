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

import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class TestLinkerNameProvider implements IManagedOutputNameProvider {

	@Override
	public IPath[] getOutputNames(ITool tool, IPath[] primaryInputNames) {
		IPath[] name = new IPath[1];
		boolean isSO = false;
		IOption optShared = tool.getOptionBySuperClassId("gnu.c.link.option30.shared"); //$NON-NLS-1$
		if (optShared != null) {
			try {
				isSO = optShared.getBooleanValue();
			} catch (Exception e) {
			}
		}
		if (isSO) {
			String soName = ""; //$NON-NLS-1$
			IOption optSOName = tool.getOptionBySuperClassId("gnu.c.link.option30.soname"); //$NON-NLS-1$
			if (optSOName != null) {
				try {
					soName = optSOName.getStringValue();
				} catch (Exception e) {
				}
			}
			if (soName != null && soName.length() > 0) {
				name[0] = Path.fromOSString(soName);
			} else {
				name[0] = Path
						.fromOSString(primaryInputNames[0].removeFileExtension().addFileExtension("so").lastSegment()); //$NON-NLS-1$
			}
			return name;
		}
		String fileName = "default"; //$NON-NLS-1$
		if (primaryInputNames != null && primaryInputNames.length > 0) {
			fileName = primaryInputNames[0].removeFileExtension().toString();
			if (fileName.startsWith("$(") && fileName.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
				fileName = fileName.substring(2, fileName.length() - 1);
			}
		}
		String[] exts = tool.getPrimaryOutputType().getOutputExtensions(tool);
		if (exts != null && exts[0].length() > 0) {
			fileName += IManagedBuilderMakefileGenerator.DOT + exts[0];
		}
		name[0] = Path.fromOSString(fileName);
		name[0] = name[0].removeFirstSegments(name[0].segmentCount() - 1);
		return name;
	}

}
