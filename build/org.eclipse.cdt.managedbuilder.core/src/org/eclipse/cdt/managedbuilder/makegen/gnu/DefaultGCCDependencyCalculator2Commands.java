/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
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

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * This dependency calculator uses the GCC -MMD -MF -MP -MT options in order to
 * generate .d files as a side effect of compilation.
 * See bugzilla 108715 for the discussion of dependency management that led to
 * the creation of this dependency calculator.  Note also that this technique
 * exhibits the failure modes discussed in comment #5.
 *
 * This class is used with DefaultGCCDependencyCalculator2.
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DefaultGCCDependencyCalculator2Commands implements
	IManagedDependencyCommands {

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
     * @param resource The IResource corresponding to the source file.
     * @param buildContext  The IConfiguration or IResourceConfiguration that
     *   contains the context in which the source file will be built
     * @param tool  The tool associated with the source file
     * @param topBuildDirectory  The top build directory of the configuration.  This is
     *   the working directory for the tool.  This IPath is relative to the project directory.
	 */
	public DefaultGCCDependencyCalculator2Commands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory) {
		this.source = source;
		this.resource = resource;
		this.buildContext = buildContext;
		this.tool = tool;
		this.topBuildDirectory = topBuildDirectory;

		//  Compute the project
		if (buildContext instanceof IConfiguration) {
			IConfiguration config = (IConfiguration)buildContext;
			project = (IProject)config.getOwner();
		} else if (buildContext instanceof IResourceInfo) {
			IResourceInfo rcInfo = (IResourceInfo)buildContext;
			project = rcInfo.getParent().getOwner().getProject();
		}

		IPath projectPath = new Path(EFSExtensionManager.getDefault().getPathFromURI(project.getLocationURI()));

		sourceLocation = (source.isAbsolute() ? source : projectPath.append(source));
		outputLocation = projectPath.append(topBuildDirectory).append(getDependencyFiles()[0]);

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
				BuildMacroProvider.getReferencedExplitFileMacros(tool).length > 0
				|| BuildMacroProvider.getReferencedExplitFileMacros(
						tool.getToolCommand(),
						IBuildMacroProvider.CONTEXT_FILE,
						new FileContextData(sourceLocation, outputLocation,
								null, tool)).length > 0;

		if (needExplicitRuleForFile) genericCommands = new Boolean(false);
	}

	/**
	 * Constructor. This constructor calls
	 * DefaultGCCDependencyCalculator2Commands(IPath source, IResource resource,
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
	 * @see #DefaultGCCDependencyCalculator2Commands(IPath source, IResource resource, IBuildObject buildContext, ITool tool, IPath topBuildDirectory)
	 */
	public DefaultGCCDependencyCalculator2Commands(IPath source, IBuildObject buildContext, ITool tool, IPath topBuildDirectory)
	{
		this(source, (IResource) null, buildContext, tool, topBuildDirectory);
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#areCommandsGeneric()
	 */
	@Override
	public boolean areCommandsGeneric() {
		if (genericCommands == null) genericCommands = new Boolean(true);
		return genericCommands.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getDependencyCommandOptions()
	 */
	@Override
	public String[] getDependencyCommandOptions() {

		String[] options = new String[4];
		// -MMD
		options[0] = "-MMD";						//$NON-NLS-1$
		// -MP
		options[1] = "-MP";							//$NON-NLS-1$
		// -MF$(@:%.o=%.d)
		// Due to bug in GNU make $(@:%.o=%.d) applied to "/buggy   path_with_3_spaces/f.o"
		// becomes "/buggy path_with_3_spaces/f.d". To avoid this we have to insert
		// filename explicitly instead of substitution rule
		if ( needExplicitRuleForFile ) {
			IPath outPath = getDependencyFiles()[0];
			options[2] = "-MF\"" + outPath.toString() + "\"";	//$NON-NLS-1$ //$NON-NLS-2$
		} else {
			options[2] = "-MF\"$(@:%.o=%.d)\"";			//$NON-NLS-1$
		}
		if( buildContext instanceof IResourceConfiguration || needExplicitRuleForFile ) {
			IPath outPath = getDependencyFiles()[0];
			// -MT"dependency-file-name"
			String optTxt = "-MT\"" + GnuMakefileGenerator.escapeWhitespaces(outPath.toString()) + "\"";	//$NON-NLS-1$ //$NON-NLS-2$
			options[3] = optTxt;
		} else {
			// -MT"$(@:%.o=%.d) %.o"
			options[3] = "-MT\"$(@:%.o=%.d)\"";			//$NON-NLS-1$
		}

		return options;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getDependencyFiles()
	 */
	@Override
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
	@Override
	public String[] getPostToolDependencyCommands() {
		// Nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands#getPreToolDependencyCommands()
	 */
	@Override
	public String[] getPreToolDependencyCommands() {
		// Nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getBuildContext()
	 */
	@Override
	public IBuildObject getBuildContext() {
		return buildContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getSource()
	 */
	@Override
	public IPath getSource() {
		return source;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getTool()
	 */
	@Override
	public ITool getTool() {
		return tool;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo#getTopBuildDirectory()
	 */
	@Override
	public IPath getTopBuildDirectory() {
		return topBuildDirectory;
	}

}
