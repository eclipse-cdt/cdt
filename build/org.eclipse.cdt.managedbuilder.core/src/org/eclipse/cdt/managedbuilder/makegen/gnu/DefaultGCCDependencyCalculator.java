/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DefaultGCCDependencyCalculator implements IManagedDependencyGenerator {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	public final String WHITESPACE = " "; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	@Override
	public IResource[] findDependencies(IResource resource, IProject project) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	@Override
	public int getCalculatorType() {
		return TYPE_COMMAND;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	@Override
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		/*
		 * For a given input, <path>/<resource_name>.<ext>, return a string containing
		 * 	echo -n $(@:%.<out_ext>=%.d) '<path>/' >> $(@:%.<out_ext>=%.d) && \
		 * 	<tool_command> -P -MM -MG <tool_flags> $< >> $(@:%.<out_ext>=%.d)
		 *
		 */
		StringBuilder buffer = new StringBuilder();

		// Get what we need to create the dependency generation command
		IConfiguration config = info.getDefaultConfiguration();

		//	We need to check whether we have any resource specific build information.
		IResourceConfiguration resConfig = null;
		if (config != null)
			resConfig = config.getResourceConfiguration(resource.getFullPath().toString());

		String inputExtension = resource.getFileExtension();
		String outputExtension = info.getOutputExtension(inputExtension);

		// Work out the build-relative path for the output files
		IContainer resourceLocation = resource.getParent();
		String relativePath = ""; //$NON-NLS-1$
		if (resourceLocation != null) {
			relativePath += resourceLocation.getProjectRelativePath().toString();
		}
		if (relativePath.length() > 0) {
			relativePath += IManagedBuilderMakefileGenerator.SEPARATOR;
		}

		// Calculate the dependency rule
		// <path>/$(@:%.<out_ext>=%.d)
		String depRule = "'$(@:%." + //$NON-NLS-1$
				outputExtension + "=%." + //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.DEP_EXT + ")'"; //$NON-NLS-1$

		// Note that X + Y are in-lineable constants by the compiler
		// Add the rule that will actually create the right format for the dep
		buffer.append(IManagedBuilderMakefileGenerator.TAB + IManagedBuilderMakefileGenerator.ECHO
				+ IManagedBuilderMakefileGenerator.WHITESPACE + "-n" //$NON-NLS-1$
				+ IManagedBuilderMakefileGenerator.WHITESPACE).append(depRule)
				.append(IManagedBuilderMakefileGenerator.WHITESPACE + "$(dir $@)" //$NON-NLS-1$
						+ IManagedBuilderMakefileGenerator.WHITESPACE + ">"
						+ IManagedBuilderMakefileGenerator.WHITESPACE)
				.append(depRule)
				.append(IManagedBuilderMakefileGenerator.WHITESPACE + IManagedBuilderMakefileGenerator.LOGICAL_AND
						+ IManagedBuilderMakefileGenerator.WHITESPACE + IManagedBuilderMakefileGenerator.LINEBREAK);

		// Add the line that will do the work to calculate dependencies
		IManagedCommandLineInfo cmdLInfo = null;
		String buildCmd = null;
		String[] inputs = new String[1];
		inputs[0] = IManagedBuilderMakefileGenerator.IN_MACRO;
		String outflag = ""; //$NON-NLS-1$
		String outputPrefix = ""; //$NON-NLS-1$
		String outputFile = ""; //$NON-NLS-1$
		ITool[] tools;
		if (resConfig != null && (tools = resConfig.getToolsToInvoke()) != null && tools.length > 0) {
			ITool tool = tools[0];
			String cmd = tool.getToolCommand();
			//try to resolve the build macros in the tool command
			try {
				String resolvedCommand = null;

				// does the resource have spaces in its name?
				if (resource.getProjectRelativePath().toString().indexOf(" ") != -1) { //$NON-NLS-1$
					// use fully qualified strings
					resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(cmd, "", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(resource.getLocation(), null, null, tool));
				} else {
					// use builder variables
					resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(cmd, "", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(resource.getLocation(), null, null, tool));
				}

				if ((resolvedCommand = resolvedCommand.trim()).length() > 0)
					cmd = resolvedCommand;

			} catch (BuildMacroException e) {
			}

			String[] toolFlags = null;
			try {
				toolFlags = tool.getToolCommandFlags(resource.getLocation(), null);
			} catch (BuildException ex) {
				// TODO add some routines to catch this
				toolFlags = EMPTY_STRING_ARRAY;
			}
			String[] flags = new String[toolFlags.length + 4];
			flags[0] = "-MM"; //$NON-NLS-1$
			flags[1] = "-MG"; //$NON-NLS-1$
			flags[2] = "-P"; //$NON-NLS-1$
			flags[3] = "-w"; //$NON-NLS-1$
			for (int i = 0; i < toolFlags.length; i++) {
				flags[4 + i] = toolFlags[i];
			}
			IManagedCommandLineGenerator cmdLGen = tool.getCommandLineGenerator();
			cmdLInfo = cmdLGen.generateCommandLineInfo(tool, cmd, flags, outflag, outputPrefix, outputFile, inputs,
					tool.getCommandLinePattern());
			buildCmd = cmdLInfo.getCommandLine();

			// resolve any remaining macros in the command after it has been generated
			try {
				String resolvedCommand = null;

				// does the resource have spaces in its name?
				if (resource.getProjectRelativePath().toString().indexOf(" ") != -1) { //$NON-NLS-1$
					// use fully qualified strings
					resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(buildCmd,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(resource.getLocation(), null, null, tool));
				} else {
					// use builder variables
					resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(buildCmd,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(resource.getLocation(), null, null, tool));
				}
				if ((resolvedCommand = resolvedCommand.trim()).length() > 0)
					buildCmd = resolvedCommand;

			} catch (BuildMacroException e) {
			}

		} else {
			ITool tool = null;
			IFolderInfo foInfo = (IFolderInfo) config.getResourceInfo(resource.getProjectRelativePath(), false);
			tools = foInfo.getFilteredTools();
			for (int index = 0; index < tools.length; index++) {
				ITool tmp = tools[index];
				if (tmp.buildsFileType(inputExtension)) {
					tool = tmp;
					break;
				}
			}
			String cmd = tool != null ? tool.getToolCommand() : null;

			//try to resolve the build macros in the tool command
			try {
				String resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(cmd,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(resource.getLocation(), null, null, tool));
				if ((resolvedCommand = resolvedCommand.trim()).length() > 0)
					cmd = resolvedCommand;

			} catch (BuildMacroException e) {
			}

			String buildFlags = "-MM -MG -P -w " //$NON-NLS-1$
					+ info.getToolFlagsForSource(inputExtension, resource.getLocation(), null);
			String[] flags = buildFlags.split("\\s"); //$NON-NLS-1$
			cmdLInfo = info.generateToolCommandLineInfo(inputExtension, flags, outflag, outputPrefix, outputFile,
					inputs, resource.getLocation(), null);
			// The command to build
			if (cmdLInfo == null)
				buildCmd = cmd + IManagedBuilderMakefileGenerator.WHITESPACE + "-MM -MG -P -w " + //$NON-NLS-1$
						buildFlags + IManagedBuilderMakefileGenerator.WHITESPACE
						+ IManagedBuilderMakefileGenerator.IN_MACRO;
			else {
				buildCmd = cmdLInfo.getCommandLine();
			}

			// resolve any remaining macros in the command after it has been
			// generated
			try {
				String resolvedCommand = null;

				// does the resource have spaces in its name?
				if (resource.getProjectRelativePath().toString().indexOf(" ") != -1) { //$NON-NLS-1$
					// use fully qualified strings
					resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(buildCmd,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(resource.getLocation(), null, null, tool));
				} else {
					// use builder variables
					resolvedCommand = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(buildCmd,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_FILE,
							new FileContextData(resource.getLocation(), null, null, tool));
				}
				if ((resolvedCommand = resolvedCommand.trim()).length() > 0)
					buildCmd = resolvedCommand;

			} catch (BuildMacroException e) {
			}
		}

		buffer.append(IManagedBuilderMakefileGenerator.TAB).append(buildCmd)
				.append(IManagedBuilderMakefileGenerator.WHITESPACE + ">>" //$NON-NLS-1$
						+ IManagedBuilderMakefileGenerator.WHITESPACE)
				.append(depRule);

		return buffer.toString();
	}

}
