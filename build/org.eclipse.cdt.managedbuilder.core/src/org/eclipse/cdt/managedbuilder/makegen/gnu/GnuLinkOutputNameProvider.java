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

package org.eclipse.cdt.managedbuilder.makegen.gnu;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This class provides a name for the Gnu Linker tool when it is not used
 * as the target tool of a tool-chain
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class GnuLinkOutputNameProvider implements IManagedOutputNameProvider {

	@Override
	public IPath[] getOutputNames(ITool tool, IPath[] primaryInputNames) {
		IPath[] name = new IPath[1];

		//  Determine a default name from the input file name
		String fileName = "default"; //$NON-NLS-1$
		if (primaryInputNames != null && primaryInputNames.length > 0) {
			fileName = primaryInputNames[0].removeFileExtension().lastSegment();
			if (fileName.startsWith("$(") && fileName.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
				fileName = fileName.substring(2, fileName.length() - 1);
			}
		}

		//  If we are building a shared library, determine if the user has specified a name using the
		//  soname option
		boolean isSO = false;
		String soName = ""; //$NON-NLS-1$
		if (hasAncestor(tool, "cdt.managedbuild.tool.gnu.c.linker")) { //$NON-NLS-1$
			IOption optShared = tool.getOptionBySuperClassId("gnu.c.link.option.shared"); //$NON-NLS-1$
			if (optShared != null) {
				try {
					isSO = optShared.getBooleanValue();
				} catch (Exception e) {
				}
			}
			if (isSO) {
				IOption optSOName = tool.getOptionBySuperClassId("gnu.c.link.option.soname"); //$NON-NLS-1$
				if (optSOName != null) {
					try {
						soName = optSOName.getStringValue();
					} catch (Exception e) {
					}
				}
			}
		} else if (hasAncestor(tool, "cdt.managedbuild.tool.gnu.cpp.linker")) { //$NON-NLS-1$
			IOption optShared = tool.getOptionBySuperClassId("gnu.cpp.link.option.shared"); //$NON-NLS-1$
			if (optShared != null) {
				try {
					isSO = optShared.getBooleanValue();
				} catch (Exception e) {
				}
			}
			if (isSO) {
				IOption optSOName = tool.getOptionBySuperClassId("gnu.cpp.link.option.soname"); //$NON-NLS-1$
				if (optSOName != null) {
					try {
						soName = optSOName.getStringValue();
					} catch (Exception e) {
					}
				}
			}
		}

		//  If this is a shared library, use the specified name
		if (isSO && soName != null && soName.length() > 0) {
			fileName = soName;
		} else {
			//  Add the outputPrefix
			String outputPrefix = tool.getPrimaryOutputType().getOutputPrefix();

			// Resolve any macros in the outputPrefix
			// Note that we cannot use file macros because if we do a clean
			// we need to know the actual
			// name of the file to clean, and cannot use any builder
			// variables such as $@. Hence
			// we use the next best thing, i.e. configuration context.

			// figure out the configuration we're using
			IBuildObject toolParent = tool.getParent();
			IConfiguration config = null;
			// if the parent is a config then we're done
			if (toolParent instanceof IConfiguration)
				config = (IConfiguration) toolParent;
			else if (toolParent instanceof IToolChain) {
				// must be a toolchain
				config = ((IToolChain) toolParent).getParent();
			}

			else if (toolParent instanceof IResourceConfiguration) {
				config = ((IResourceConfiguration) toolParent).getParent();
			}

			else {
				// bad
				throw new AssertionError(ManagedMakeMessages.getResourceString("GnuLinkOutputNameProvider.0")); //$NON-NLS-1$
			}

			if (config != null) {

				boolean explicitRuleRequired = false;

				// if any input files have spaces in the name, then we must
				// not use builder variables
				for (int k = 0; k < primaryInputNames.length; k++) {
					if (primaryInputNames[k].toString().indexOf(" ") != -1) //$NON-NLS-1$
						explicitRuleRequired = true;
				}

				try {

					if (explicitRuleRequired) {
						outputPrefix = ManagedBuildManager.getBuildMacroProvider().resolveValue(outputPrefix, "", //$NON-NLS-1$
								" ", //$NON-NLS-1$
								IBuildMacroProvider.CONTEXT_CONFIGURATION, config);
					}

					else {
						outputPrefix = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
								outputPrefix, "", //$NON-NLS-1$
								" ", //$NON-NLS-1$
								IBuildMacroProvider.CONTEXT_CONFIGURATION, config);
					}
				}

				catch (BuildMacroException e) {
				}

			}

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
			if (id.equals(tool.getId()))
				return true;
		} while ((tool = tool.getSuperClass()) != null);
		return false;
	}
}
