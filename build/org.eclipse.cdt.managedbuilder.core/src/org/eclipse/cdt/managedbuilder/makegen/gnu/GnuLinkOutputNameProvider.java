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

package org.eclipse.cdt.managedbuilder.makegen.gnu;

import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

//
//  This class provides a name for the Gnu Linker tool when it is not used
//  as the target tool of a tool-chain
//
public class GnuLinkOutputNameProvider implements IManagedOutputNameProvider {

	public IPath[] getOutputNames(ITool tool, IPath[] primaryInputNames) {
		IPath[] name = new IPath[1];

		//  Determine a default name from the input file name
		String fileName = "default";	//$NON-NLS-1$
		if (primaryInputNames != null && primaryInputNames.length > 0) {
			fileName = primaryInputNames[0].removeFileExtension().lastSegment();
			if (fileName.startsWith("$(") && fileName.endsWith(")")) {	//$NON-NLS-1$ //$NON-NLS-2$
				fileName = fileName.substring(2,fileName.length()-1);
			}
		}
		
		//  If we are building a shared library, determine if the user has specified a name using the 
		//  soname option
		boolean isSO = false;
		String soName = "";	//$NON-NLS-1$
		if (hasAncestor(tool, "cdt.managedbuild.tool.gnu.c.linker")) {	//$NON-NLS-1$
			IOption optShared = tool.getOptionBySuperClassId("gnu.c.link.option.shared");	//$NON-NLS-1$
			if (optShared != null) {
				try {
					isSO = optShared.getBooleanValue();
				} catch (Exception e) {}
			}
			if (isSO) {
				IOption optSOName = tool.getOptionBySuperClassId("gnu.c.link.option.soname");	//$NON-NLS-1$
				if (optSOName != null) {
					try {
						soName = optSOName.getStringValue();
					} catch (Exception e) {}
				}
			}
		} else
		if (hasAncestor(tool, "cdt.managedbuild.tool.gnu.cpp.linker")) {	//$NON-NLS-1$
			IOption optShared = tool.getOptionBySuperClassId("gnu.cpp.link.option.shared");	//$NON-NLS-1$
			if (optShared != null) {
				try {
					isSO = optShared.getBooleanValue();
				} catch (Exception e) {}
			}
			if (isSO) {
				IOption optSOName = tool.getOptionBySuperClassId("gnu.cpp.link.option.soname");	//$NON-NLS-1$
				if (optSOName != null) {
					try {
						soName = optSOName.getStringValue();
					} catch (Exception e) {}
				}
			}
		} 
			
		//  If this is a shared library, use the specified name
		if (isSO && soName != null && soName.length() > 0) {
			fileName = soName;
		} else {
			//  Add the outputPrefix	
			String outputPrefix = tool.getPrimaryOutputType().getOutputPrefix();
			if (outputPrefix != null && outputPrefix.length() > 0) {
				fileName = outputPrefix + fileName;
			}
			//  Add the primary output type extension
			String[] exts = tool.getPrimaryOutputType().getOutputExtensions(tool);
			if (exts != null && exts[0].length() > 0) {
				fileName += IManagedBuilderMakefileGenerator.DOT + exts[0];
			}
		}

		name[0] = Path.fromOSString(fileName);
		return name;
	}

	protected boolean hasAncestor(ITool tool, String id) {
		do {
			if (id.equals(tool.getId())) return true;
		} while ((tool = tool.getSuperClass()) != null);
		return false;
	}
}
