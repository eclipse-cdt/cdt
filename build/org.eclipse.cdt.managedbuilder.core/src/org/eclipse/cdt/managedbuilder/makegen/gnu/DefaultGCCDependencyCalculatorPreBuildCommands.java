/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
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
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyPreBuild;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import java.util.Vector;

/**
 * This dependency calculator uses the GCC -MM -MF -MP -MT options in order to
 * generate .d files as separate step prior to the source compilations.
 * 
 * This dependency calculator uses the class DefaultGCCDependencyCalculatorPreBuildCommands
 * which implements the per-source command information
 * 
 * This class is used with DefaultGCCDependencyCalculatorPreBuild.
 * 
 * @since 3.1
 */

public class DefaultGCCDependencyCalculatorPreBuildCommands implements IManagedDependencyPreBuild {

	private static final String EMPTY_STRING = new String();

	//  Member variables set by the constructor
	IPath source;
	IResource resource;
	IBuildObject buildContext;
	ITool tool; 
	IPath topBuildDirectory;
	
	//  Other Member variables
	IProject project;
	IPath sourceLocation;
	IPath outputLocation;
	boolean needExplicitRuleForFile;
	Boolean genericCommands = null;
	
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
	public DefaultGCCDependencyCalculatorPreBuildCommands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		this.source = source;
		this.resource = resource;
		this.buildContext = buildContext;
		this.tool = tool;
		this.topBuildDirectory = topBuildDirectory;
		
		//  Compute the project
		if (buildContext instanceof IConfiguration) {
			IConfiguration config = (IConfiguration)buildContext;
			project = (IProject)config.getOwner();
		} else if (buildContext instanceof IResourceConfiguration) {
			IResourceConfiguration resConfig = (IResourceConfiguration)buildContext;
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
		
		if (needExplicitRuleForFile) genericCommands = new Boolean(false);
	}

	/**
	 * Constructor. This constructor calls
	 * DefaultGCCDependencyCalculatorPreBuildCommands(IPath source, IResource resource,
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
	 * @see DefaultGCCDependencyCalculatorPreBuildCommands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory)
	 */
	public DefaultGCCDependencyCalculatorPreBuildCommands(IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory)
	{
		this(source, (IResource) null, buildContext, tool, topBuildDirectory); 
	}
	
	public boolean areCommandsGeneric() {
		if (genericCommands != null) return genericCommands.booleanValue();
		//  If the context is a Configuration, yes
		if (buildContext instanceof IConfiguration) {
			genericCommands = new Boolean(true);
			return true;
		}
		//  If the context is a Resource Configuration, determine if it overrides any
		//  of its parent configuration's options that would affect dependency file
		//  generation.
		// TODO
		genericCommands = new Boolean(false);
		return false;
	}

	public String getBuildStepName() {
		return new String("GCC_DEPENDS");	//$NON-NLS-1$
	}

	public String[] getDependencyCommands() {
		
		String[] commands = new String[1];
		String depCmd = EMPTY_STRING;
		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();
		
		// Get and resolve the command
		String cmd = tool.getToolCommand();
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

			if ((resolvedCommand = resolvedCommand.trim()).length() > 0)
				cmd = resolvedCommand;

		} catch (BuildMacroException e) {
		}
			
		IManagedCommandLineInfo cmdLInfo = null;
		
		//  Set up the command line options that will generate the dependency file
		Vector options = new Vector();
		// -w 
		options.add("-w");						//$NON-NLS-1$
		// -MM 
		options.add("-MM");						//$NON-NLS-1$
		// -MP 
		options.add("-MP");						//$NON-NLS-1$

		String optTxt;
		
		if( buildContext instanceof IResourceConfiguration || needExplicitRuleForFile ) {
			IPath outPath = getDependencyFiles()[0];
			// -MT"dependecy-file-name"
			optTxt = "-MT\"";					//$NON-NLS-1$
			optTxt += GnuMakefileGenerator.escapeWhitespaces(outPath.toString()) + "\"";	//$NON-NLS-1$
			options.add(optTxt);
			// -MT"object-file-filename"
			optTxt = "-MT\"";					//$NON-NLS-1$
			optTxt += GnuMakefileGenerator.escapeWhitespaces((outPath.removeFileExtension()).toString());
			String outExt = tool.getOutputExtension(source.getFileExtension()); 
			if (outExt != null) optTxt += "." + outExt;		//$NON-NLS-1$
			optTxt += "\""; 					//$NON-NLS-1$
			options.add(optTxt);
		} else {
			// -MT"$@"
			options.add("-MT\"$@\"");			//$NON-NLS-1$
			// -MT'$(@:%.d=%.o)'
			optTxt = "-MT\"$(@:%.d=%.o)\"";		//$NON-NLS-1$
			//optTxt = "-MT\"${OutputDirRelPath}${OutputFileBaseName}";
			//if (outExt != null) optTxt += "." + outExt;
			//optTxt += "\""; 						//$NON-NLS-1$
			options.add(optTxt);
		}

		// Save the -I, -D, -U options and discard the rest
		try { 
			String[] allFlags = tool.getToolCommandFlags(sourceLocation, outputLocation);
			for (int i=0; i<allFlags.length; i++) {
				if (allFlags[i].startsWith("-I") ||		//$NON-NLS-1$
				    allFlags[i].startsWith("-D") ||		//$NON-NLS-1$
				    allFlags[i].startsWith("-U")) {		//$NON-NLS-1$
					options.add(allFlags[i]);
				}
			}
		} catch( BuildException ex ) {
		}

		// Call the command line generator
		IManagedCommandLineGenerator cmdLGen = tool.getCommandLineGenerator();
		String[] flags = (String[])options.toArray(new String[options.size()]);
		String[] inputs = new String[1];
		inputs[0] = IManagedBuilderMakefileGenerator.IN_MACRO;
		cmdLInfo = cmdLGen.generateCommandLineInfo( 
				tool, cmd, flags, "-MF", EMPTY_STRING,	//$NON-NLS-1$
				IManagedBuilderMakefileGenerator.OUT_MACRO, 
				inputs, 
				tool.getCommandLinePattern() );
		
		// The command to build
		if (cmdLInfo != null) {
			depCmd = cmdLInfo.getCommandLine();
        
	        // resolve any remaining macros in the command after it has been
	        // generated
			try {
				String resolvedCommand;
				if (!needExplicitRuleForFile) {
					resolvedCommand = provider.resolveValueToMakefileFormat(
									depCmd,
									EMPTY_STRING,
									IManagedBuilderMakefileGenerator.WHITESPACE,
									IBuildMacroProvider.CONTEXT_FILE,
									new FileContextData(sourceLocation,
											outputLocation, null, tool));
				} else {
					// if we need an explicit rule then don't use any builder
					// variables, resolve everything to explicit strings
					resolvedCommand = provider.resolveValue(
									depCmd,
									EMPTY_STRING,
									IManagedBuilderMakefileGenerator.WHITESPACE,
									IBuildMacroProvider.CONTEXT_FILE,
									new FileContextData(sourceLocation,
											outputLocation, null, tool));
				}
	
				if ((resolvedCommand = resolvedCommand.trim()).length() > 0)
					depCmd = resolvedCommand;
	
			} catch (BuildMacroException e) {
			}
		}

		commands[0] = depCmd;
		return commands;
	}

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
