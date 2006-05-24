/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.MacroResolver;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * This dependency calculator uses the same dependency management technique as the
 * DefaultGCCDependencyCalculator.  That is:
 * 
 *  1.  An echo command creates the dependency file (.d).
 *  2.  A second invocation of the compiler is made in order to append to the dependency file.  
 *      The additional options -MM -MG -P -w are added to the command line.
 *  3.  The dependency files are post-processed to add the empty header rules. 
 * 
 * This class is used with DefaultGCCDependencyCalculator3.
 *
 * This is an example dependency calculator that is not used by the CDT GCC tool-chain.
 * 
 * @since 3.1
 */

public class DefaultGCCDependencyCalculator3Commands implements
	IManagedDependencyCommands {

	private static final String EMPTY_STRING = new String();

	//  Member variables set by the constructor
	IPath source;
	IResource resource;
	IBuildObject buildContext;
	ITool tool; 
	IPath topBuildDirectory;
	
	//  Other Member variables
	IProject project;
	IConfiguration config;
	IResourceConfiguration resConfig;
	IPath sourceLocation;
	IPath outputLocation;
	boolean needExplicitRuleForFile;
	boolean genericCommands = true;
	
	/**
     * Constructor
	 * 
     * @param source  The source file for which dependencies should be calculated
     *    The IPath can be either relative to the project directory, or absolute in the file system.
     * @param buildContext  The IConfiguration or IResourceConfiguration that
     *   contains the context in which the source file will be built
     * @param tool  The tool associated with the source file
     * @param topBuildDirectory  The top build directory of the configuration.  This is
     *   the working directory for the tool.  This IPath is relative to the project directory.
	 */
	public DefaultGCCDependencyCalculator3Commands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		this.source = source;
		this.resource = resource;
		this.buildContext = buildContext;
		this.tool = tool;
		this.topBuildDirectory = topBuildDirectory;
		
		//  Compute the project
		if (buildContext instanceof IConfiguration) {
			resConfig = null;
			config = (IConfiguration)buildContext;
			project = (IProject)config.getOwner();
		} else if (buildContext instanceof IResourceConfiguration) {
			resConfig = (IResourceConfiguration)buildContext;
			config = resConfig.getParent();
			project = (IProject)resConfig.getOwner();
		}
		
		sourceLocation = (source.isAbsolute() ? source : project.getLocation().append(source));
		outputLocation = project.getLocation().append(topBuildDirectory).append(getDependencyFiles()[0]);

		// A separate rule is needed for the resource in the case where explicit file-specific macros
		// are referenced, or if the resource contains special characters in its path (e.g., whitespace)
		
		/* fix for 137674
		 * 
		 * We only need an explicit rule if one of the following is true:
		 * - The resource is linked, and its full path to its real location contains special characters
		 * - The resource is not linked, but its project relative path contains special characters
		*/ 
		boolean resourceNameRequiresExplicitRule = true;
		
		if(resource != null)
		{
			resourceNameRequiresExplicitRule = (resource.isLinked() && GnuMakefileGenerator
						.containsSpecialCharacters(sourceLocation.toString()))
				|| (!resource.isLinked() && GnuMakefileGenerator
						.containsSpecialCharacters(resource.getProjectRelativePath().toString()));
		}
		
		needExplicitRuleForFile = resourceNameRequiresExplicitRule || 
				MacroResolver.getReferencedExplitFileMacros(tool).length > 0
				|| MacroResolver.getReferencedExplitFileMacros(
						tool.getToolCommand(), 
						IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(sourceLocation, outputLocation,
								null, tool)).length > 0;
		
		if (buildContext instanceof IResourceConfiguration || needExplicitRuleForFile) 
			genericCommands = false;
	}
	
	/**
	 * Constructor. This constructor calls
	 * DefaultGCCDependencyCalculator3Commands(IPath source, IResource resource,
	 * IBuildObject buildContext, ITool tool, IPath topBuildDirectory) with a
	 * null resource. The net result of this is that dependency rules will
	 * always be explicit and will never use pattern rules, as it is impossible
	 * for the calculator to know whether the resource is linked or not.
	 * 
	 * @param source
	 *            The source file for which dependencies should be calculated
	 *            The IPath can be either relative to the project directory, or
	 *            absolute in the file system.
	 * @param buildContext
	 *            The IConfiguration or IResourceConfiguration that contains the
	 *            context in which the source file will be built
	 * @param tool
	 *            The tool associated with the source file
	 * @param topBuildDirectory
	 *            The top build directory of the configuration. This is the
	 *            working directory for the tool. This IPath is relative to the
	 *            project directory.
	 *            
	 * @see DefaultGCCDependencyCalculator3Commands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory)
	 */
	public DefaultGCCDependencyCalculator3Commands(IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory)
	{
		this(source, (IResource) null, buildContext, tool, topBuildDirectory); 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#areCommandsGeneric()
	 */	
	public boolean areCommandsGeneric() {
		return genericCommands;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getDependencyCommandOptions()
	 */
	public String[] getDependencyCommandOptions() {
		// Nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getDependencyFiles()
	 */
	public IPath[] getDependencyFiles() {
		//  The source file is project relative and the dependency file is top build directory relative
		//  Remove the source extension and add the dependency extension
		IPath depFilePath = source.removeFileExtension().addFileExtension(IManagedBuilderMakefileGenerator.DEP_EXT);
		//  Remember that the source folder hierarchy and the build output folder hierarchy are the same
		//  but if this is a generated resource, then it may already be under the top build directory
		if (!depFilePath.isAbsolute()) {
			if (topBuildDirectory.isPrefixOf(depFilePath)) {
				depFilePath = depFilePath.removeFirstSegments(1);
			}
		}
		IPath[] paths = new IPath[1];
		paths[0] = depFilePath;
		return paths;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getPostToolDependencyCommands()
	 */
	public String[] getPostToolDependencyCommands() {
		/*
		 * For a given input, <path>/<resource_name>.<ext>, return a string containing
		 * 	echo -n $(@:%.<out_ext>=%.d) '<path>/' >> $(@:%.<out_ext>=%.d) && \
		 * 	<tool_command> -P -MM -MG <tool_flags> $< >> $(@:%.<out_ext>=%.d)
		 * 
		 */
		String[] commands = new String[2];

		// Get what we need to create the dependency generation command
		String inputExtension = source.getFileExtension();
		String outputExtension = tool.getOutputExtension(inputExtension);
		
		// Calculate the dependency rule
		// <path>/$(@:%.<out_ext>=%.d)
		String depRule = "'$(@:%." + //$NON-NLS-1$
			outputExtension + 
			"=%." + //$NON-NLS-1$
			IManagedBuilderMakefileGenerator.DEP_EXT + 
			")'"; //$NON-NLS-1$
		
		// Add the Echo command that will actually create the right format for the dep 
		commands[0] = 
				IManagedBuilderMakefileGenerator.TAB + 
				IManagedBuilderMakefileGenerator.ECHO + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				"-n" + //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE +
				depRule + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				"$(dir $@)" + //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				">" + //$NON-NLS-1$ 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				depRule;
				
		// Add the line that will do the work to calculate dependencies
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
		IManagedCommandLineInfo cmdLInfo = null;
		String buildCmd = null;
		String[] inputs= new String[1]; inputs[0] = IManagedBuilderMakefileGenerator.IN_MACRO;
		String outputFile = "";   //$NON-NLS-1$
		String outflag = "";  //$NON-NLS-1$
		String outputPrefix = "";   //$NON-NLS-1$
		String cmd = tool.getToolCommand();
		//try to resolve the build macros in the tool command
		try {
			String resolvedCommand = null;

			if (!needExplicitRuleForFile) {
				resolvedCommand = provider.resolveValueToMakefileFormat(
								cmd,
								EMPTY_STRING,
								IManagedBuilderMakefileGenerator.WHITESPACE,
								IBuildMacroProvider.CONTEXT_FILE,
								new FileContextData(sourceLocation,
										outputLocation, null, tool));
			} else {
				// if we need an explicit rule then don't use any builder
				// variables, resolve everything
				// to explicit strings
				resolvedCommand = provider.resolveValue(
								cmd,
								EMPTY_STRING,
								IManagedBuilderMakefileGenerator.WHITESPACE,
								IBuildMacroProvider.CONTEXT_FILE,
								new FileContextData(sourceLocation,
										outputLocation, null, tool));
			}
			
			if((resolvedCommand = resolvedCommand.trim()).length() > 0)
				cmd = resolvedCommand;
				
		} catch (BuildMacroException e){
		}

		String[] toolFlags = null;
		try { 
			toolFlags = tool.getToolCommandFlags(sourceLocation, outputLocation);
		} catch( BuildException ex ) {
			toolFlags = new String[0];
		}
		String[] flags = new String[toolFlags.length + 4];
		flags[0] = "-MM";  //$NON-NLS-1$
		flags[1] = "-MG";  //$NON-NLS-1$
		flags[2] = "-P";   //$NON-NLS-1$
		flags[3] = "-w";   //$NON-NLS-1$
		for (int i=0; i<toolFlags.length; i++) {
			flags[4+i] = toolFlags[i];
		}
		IManagedCommandLineGenerator cmdLGen = tool.getCommandLineGenerator();
		cmdLInfo = cmdLGen.generateCommandLineInfo( tool, cmd, flags, outflag, outputPrefix,
				outputFile, inputs, tool.getCommandLinePattern() );

		// The command to build
		buildCmd = cmdLInfo.getCommandLine();
			
		// resolve any remaining macros in the command after it has been generated
		try {
			String resolvedCommand = null;

			if (!needExplicitRuleForFile) {
				resolvedCommand = provider.resolveValueToMakefileFormat(
								buildCmd,
								EMPTY_STRING,
								IManagedBuilderMakefileGenerator.WHITESPACE,
								IBuildMacroProvider.CONTEXT_FILE,
								new FileContextData(sourceLocation,
										outputLocation, null, tool));
			} else {
				// if we need an explicit rule then don't use any builder
				// variables, resolve everything to explicit strings
				resolvedCommand = provider.resolveValue(
								buildCmd,
								EMPTY_STRING,
								IManagedBuilderMakefileGenerator.WHITESPACE,
								IBuildMacroProvider.CONTEXT_FILE,
								new FileContextData(sourceLocation,
										outputLocation, null, tool));
			}

			if((resolvedCommand = resolvedCommand.trim()).length() > 0)
				buildCmd = resolvedCommand;
				
		} catch (BuildMacroException e){
		}

		commands[1] = 
				IManagedBuilderMakefileGenerator.TAB + 
				buildCmd +
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				">>" +  //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE + depRule;

		return commands;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getPreToolDependencyCommands()
	 */
	public String[] getPreToolDependencyCommands() {
		// Nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getBuildContext()
	 */
	public IBuildObject getBuildContext() {
		return buildContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getSource()
	 */
	public IPath getSource() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getTool()
	 */
	public ITool getTool() {
		return tool;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getTopBuildDirectory()
	 */
	public IPath getTopBuildDirectory() {
		return topBuildDirectory;
	}

}
