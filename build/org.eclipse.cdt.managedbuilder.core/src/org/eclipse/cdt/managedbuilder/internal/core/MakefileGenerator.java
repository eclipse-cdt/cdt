package org.eclipse.cdt.managedbuilder.internal.core;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.internal.core.search.PathCollector;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyQueryJob;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class MakefileGenerator {
	// String constants for messages
	private static final String MESSAGE = "MakeBuilder.message";	//$NON-NLS-1$
	private static final String BUILD_ERROR = MESSAGE + ".error";	//$NON-NLS-1$
	private static final String COMMENT = "MakeBuilder.comment";	//$NON-NLS-1$
	private static final String MOD_LIST = COMMENT + ".module.list";	//$NON-NLS-1$	
	private static final String SRC_LISTS = COMMENT + ".source.list";	//$NON-NLS-1$	
	private static final String MOD_RULES = COMMENT + ".build.rule";	//$NON-NLS-1$	
	private static final String MOD_INCL = COMMENT + ".module.make.includes";	//$NON-NLS-1$	
	private static final String DEP_INCL = COMMENT + ".module.dep.includes";	//$NON-NLS-1$
	private static final String AUTO_DEP = COMMENT + ".autodeps";	//$NON-NLS-1$

	// String constants for makefile contents
	protected static final String COLON = ":";
	protected static final String DEPFILE_NAME = "module.dep";	//$NON-NLS-1$
	protected static final String DOT = ".";
	protected static final String MAKEFILE_NAME = "makefile";	//$NON-NLS-1$
	protected static final String MODFILE_NAME = "module.mk";	//$NON-NLS-1$
	protected static final String LINEBREAK = "\\";
	protected static final String NEWLINE = System.getProperty("line.separator");
	protected static final String SEMI_COLON = ";";
	protected static final String SEPARATOR = "/";
	protected static final String TAB = "\t";	
	protected static final String WHITESPACE = " ";
	protected static final String WILDCARD = "%";

	// Local variables needed by generator
	protected IManagedBuildInfo info;
	protected List modifiedList;
	protected IProgressMonitor monitor;
	protected List subdirList;
	protected IProject project;
	protected List ruleList;
	protected IPath topBuildDir;
	protected boolean shouldRunBuild;
	
	private String target;
	
	private String extension;
	/**
	 * This class is used to recursively walk the project and determine which
	 * modules contribute buildable source files. 
	 */
	protected class ResourceProxyVisitor implements IResourceProxyVisitor {
		private MakefileGenerator generator;
		private IManagedBuildInfo info;

		/**
		 * Constructs a new resource proxy visitor to quickly visit project
		 * resources.
		 */
		public ResourceProxyVisitor(MakefileGenerator generator, IManagedBuildInfo info) {
			this.generator = generator;
			this.info = info;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
		public boolean visit(IResourceProxy proxy) throws CoreException {
			// No point in proceeding, is there 
			if (generator == null) {
				return false;
			}
			
			// Is this a resource we should even consider
			if (proxy.getType() == IResource.FILE) {
				// Check extension to see if build model should build this file
				IResource resource = proxy.requestResource();
				String ext = resource.getFileExtension();
				if (info.buildsFileType(ext)) {
					if (!generator.isGeneratedResource(resource)) {
						generator.appendBuildSubdirectory(resource);
					}
				}
				return false;
			}

			// Recurse into subdirectories
			return true;
		}

	}

	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private MakefileGenerator generator;
		private IManagedBuildInfo info;

		/**
		 * 
		 */
		public ResourceDeltaVisitor(MakefileGenerator generator, IManagedBuildInfo info) {
			this.generator = generator;
			this.info = info;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			// Should the visitor keep iterating in current directory 
			boolean keepLooking = false;
			IResource resource = delta.getResource();
			
			// What kind of resource change has occurred
			if (resource.getType() == IResource.FILE) {
				String ext = resource.getFileExtension();
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.REMOVED:
						// Add the container of the resource and any resources that depend on it
						if (info.buildsFileType(ext)) {
							if (!generator.isGeneratedResource(resource)) {
								// Here's the container
								generator.appendModifiedSubdirectory(resource);
								// and all the dependents
								PathCollector pathCollector = new PathCollector();
								ICSearchScope scope = SearchEngine.createWorkspaceScope();
								CSearchPattern pattern = CSearchPattern.createPattern(resource.getLocation().toOSString(),ICSearchConstants.INCLUDE, ICSearchConstants.REFERENCES,ICSearchConstants.EXACT_MATCH,true);
								IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
							    indexManager.performConcurrentJob( 
								  new PatternSearchJob(
									  (CSearchPattern) pattern,
									  scope,
									  pathCollector,
									  indexManager 
								  ),
								  ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
								  null );
								 String[] deps = pathCollector.getPaths();
								 if (deps.length > 0 ) {
								 	for (int i=0; i<deps.length; i++){
										generator.appendModifiedSubdirectory(resource);
								 	}
								}
								// A build should run						
								generator.shouldRunBuild(true);
							}
						}
						break;
					case IResourceDelta.CHANGED:
						if (info.buildsFileType(ext)) {
							switch (delta.getFlags()) {
								case IResourceDelta.CONTENT:
									// If the contents changed then just do a build
									generator.shouldRunBuild(true);
									keepLooking = true;
								default:
									keepLooking = true;
							}							
						}
						break;
					default:
						keepLooking = true;
						break;
				}
			} else {
				// If the resource is part of the generated directory structure don't recurse
				if (!generator.isGeneratedResource(resource)) {
					keepLooking = true;
				}
			}
			return keepLooking;
		}
	}	

	/**
	 * @param project
	 * @param info
	 * @param monitor
	 */
	public MakefileGenerator(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
		super();
		// Save the project so we can get path and member information
		this.project = project;
		// Save the monitor reference for reporting back to the user
		this.monitor = monitor;
		// Get the build info for the project
		this.info = info;
		// By default a build never runs
		shouldRunBuild = false;
		// Get the name of the build target
		target = info.getBuildArtifactName();
		// Get its extension
		extension = (new Path(target)).getFileExtension();
		if (extension == null) {
			extension = new String();
		}
	}

	/* (non-javadoc)
	 * Calculates dependencies for all the source files in the argument. A source 
	 * file can depend on any number of header files, so the dependencies have to 
	 * be added to its dependency list. 
	 * 
	 * @param module
	 * @return
	 */
	protected StringBuffer addSourceDependencies(IContainer module) throws CoreException {
		// Calculate the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		String relativePath = moduleRelativePath.toString();
		relativePath += relativePath.length() == 0 ? "" : SEPARATOR; 

		// Create the buffer to hold the output for the module and a dep calculator
		StringBuffer buffer = new StringBuffer();
		buffer.append(ManagedBuilderCorePlugin.getResourceString(AUTO_DEP) + NEWLINE);
		IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();

		/*
		 * Visit each resource in the folder that we have a rule to build.
		 * The dependency output for each resource will be in the format
		 * <relativePath>/<resourceName>.<outputExtension> : <dep1> ... <depn>
		 * with long lines broken.
		 */
		IResource[] resources = module.members();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE) {
				String inputExt = resource.getFileExtension();
				if (info.buildsFileType(inputExt)) {
					// Get the filename without an extension
					String fileName = resource.getFullPath().removeFileExtension().lastSegment();
					if (fileName == null) continue;
					String outputExt = info.getOutputExtension(inputExt);
					if (outputExt != null) {
						fileName += DOT + outputExt;
					}
					// ASk the dep generator to find all the deps for this resource
					ArrayList dependencies = new ArrayList();
					try {
						indexManager.performConcurrentJob(new DependencyQueryJob(project, (IFile)resource, indexManager, dependencies), ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
					} catch (Exception e) {
						continue;
					}
					if (dependencies.size() == 0) continue;
					buffer.append(relativePath + fileName + COLON + WHITESPACE);
					Iterator iter = dependencies.listIterator();
					while (iter.hasNext()) {
						buffer.append(LINEBREAK + NEWLINE);
						String path = (String)iter.next();
						buffer.append(path + WHITESPACE);
					}
					buffer.append(NEWLINE);
				}
			}
		}		
		return buffer;
	}

	/* (non-javadoc)
	 * @param buffer
	 * @param info
	 */
	protected StringBuffer addMacros() {
		StringBuffer buffer = new StringBuffer();
		
		// Add the ROOT macro
		buffer.append("ROOT := .." + NEWLINE);
		
		// Get the clean command from the build model
		buffer.append("RM := ");
		buffer.append(info.getCleanCommand() + NEWLINE + NEWLINE);
		
		buffer.append(ManagedBuilderCorePlugin.getResourceString(SRC_LISTS) + NEWLINE);
		buffer.append("C_SRCS := " + NEWLINE);
		buffer.append("CC_SRCS := " + NEWLINE);
		buffer.append("CXX_SRCS := " + NEWLINE);
		buffer.append("CAPC_SRCS := " + NEWLINE);
		buffer.append("CPP_SRCS := " + NEWLINE + NEWLINE);
		
		// Add the libraries this project depends on
		buffer.append("LIBS := ");
		String[] libs = info.getLibsForTarget(extension);
		for (int i = 0; i < libs.length; i++) {
			String string = libs[i];
			buffer.append(LINEBREAK + NEWLINE + string);
		}
		buffer.append(NEWLINE + NEWLINE);
		
		// Add the extra user-specified objects
		buffer.append("USER_OBJS := ");
		String[] userObjs = info.getUserObjectsForTarget(extension);
		for (int j = 0; j < userObjs.length; j++) {
			String string = userObjs[j];
			buffer.append(LINEBREAK + NEWLINE + string);
		}
		buffer.append(NEWLINE + NEWLINE);
		
		buffer.append("OBJS = $(C_SRCS:$(ROOT)/%.c=%.o) $(CC_SRCS:$(ROOT)/%.cc=%.o) $(CXX_SRCS:$(ROOT)/%.cxx=%.o) $(CAPC_SRCS:$(ROOT)/%.C=%.o) $(CPP_SRCS:$(ROOT)/%.cpp=%.o)" + NEWLINE);
		return (buffer.append(NEWLINE));
	}

	/* (non-javadoc)
	 * @return
	 */
	protected StringBuffer addSubdirectories() {
		StringBuffer buffer = new StringBuffer();
		// Add the comment
		buffer.append(ManagedBuilderCorePlugin.getResourceString(MOD_LIST) + NEWLINE);
		buffer.append("SUBDIRS := " + LINEBREAK + NEWLINE);
		
		// Get all the module names
		ListIterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer container = (IContainer) iter.next();
			// Check the special case where the module is the project root
			if (container.getFullPath() == project.getFullPath()) {
				buffer.append("." +  WHITESPACE + LINEBREAK + NEWLINE);
			} else {
				IPath path = container.getProjectRelativePath();
				buffer.append(path.toString() +  WHITESPACE + LINEBREAK + NEWLINE);
			}
		}

		// Now add the makefile instruction to include all the subdirectory makefile fragments
		buffer.append(NEWLINE);
		buffer.append(ManagedBuilderCorePlugin.getResourceString(MOD_INCL) + NEWLINE);
		buffer.append("-include ${patsubst %, %/module.mk, $(SUBDIRS)}" + NEWLINE);

		buffer.append(NEWLINE + NEWLINE);
		return buffer;
	}


	/* (non-javadoc)
	 * Answers a <code>StringBuffer</code> containing all of the sources contributed by
	 * a container to the build.
	 * @param module
	 * @return
	 */
	protected StringBuffer addSources(IContainer module) throws CoreException {
		// Calculate the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		String relativePath = moduleRelativePath.toString();
		relativePath += relativePath.length() == 0 ? "" : SEPARATOR; 
		
		// String buffers
		StringBuffer buffer = new StringBuffer();
		StringBuffer cBuffer = new StringBuffer("C_SRCS += " + LINEBREAK + NEWLINE);
		cBuffer.append("${addprefix $(ROOT)/" + relativePath + "," + LINEBREAK + NEWLINE);
		StringBuffer ccBuffer = new StringBuffer("CC_SRCS += \\" + NEWLINE);
		ccBuffer.append("${addprefix $(ROOT)/" + relativePath + "," + LINEBREAK + NEWLINE);
		StringBuffer cxxBuffer = new StringBuffer("CXX_SRCS += \\" + NEWLINE);
		cxxBuffer.append("${addprefix $(ROOT)/" + relativePath + "," + LINEBREAK + NEWLINE);
		StringBuffer capcBuffer = new StringBuffer("CAPC_SRCS += \\" + NEWLINE);
		capcBuffer.append("${addprefix $(ROOT)/" + relativePath + "," + LINEBREAK + NEWLINE);
		StringBuffer cppBuffer = new StringBuffer("CPP_SRCS += \\" + NEWLINE);
		cppBuffer.append("${addprefix $(ROOT)/" + relativePath + "," + LINEBREAK + NEWLINE);
		StringBuffer ruleBuffer = new StringBuffer(ManagedBuilderCorePlugin.getResourceString(MOD_RULES) + NEWLINE);

		// Put the comment in		
		buffer.append(ManagedBuilderCorePlugin.getResourceString(SRC_LISTS) + NEWLINE);

		// Visit the resources in this folder
		IResource[] resources = module.members();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE) {
				String ext = resource.getFileExtension();
				if (info.buildsFileType(ext)) {
					if (new String("c").equals(ext)) {
						cBuffer.append(resource.getName() + WHITESPACE + LINEBREAK + NEWLINE);
					} else if (new String("cc").equalsIgnoreCase(ext)) {
						ccBuffer.append(resource.getName() + WHITESPACE + LINEBREAK + NEWLINE);
					} else if (new String("cxx").equalsIgnoreCase(ext)) {
						cxxBuffer.append(resource.getName() + WHITESPACE + LINEBREAK + NEWLINE);
					} else if (new String("C").equals(ext)) {
						capcBuffer.append(resource.getName() + WHITESPACE + LINEBREAK + NEWLINE);
					} else {
						cppBuffer.append(resource.getName() + WHITESPACE + LINEBREAK + NEWLINE);
					}

					// Try to add the rule for the file
					addRule(relativePath, ruleBuffer, resource);
				}
			}
		}

		// Finish the commands in the buffers
		buffer.append(cBuffer.append("}" + NEWLINE + NEWLINE));
		buffer.append(ccBuffer.append("}" + NEWLINE + NEWLINE));
		buffer.append(cxxBuffer.append("}" + NEWLINE + NEWLINE));
		buffer.append(capcBuffer.append("}" + NEWLINE + NEWLINE));
		buffer.append(cppBuffer.append("}" + NEWLINE + NEWLINE));

		return buffer.append(ruleBuffer + NEWLINE);
	}

	/* (non-javadoc)
	 * Answers a <code>StrinBuffer</code> containing all of the required targets to
	 * properly build the project.
	 */
	protected StringBuffer addTargets(boolean rebuild) {
		StringBuffer buffer = new StringBuffer();

		// Assemble the information needed to generate the targets
		String cmd = info.getToolForTarget(extension);
		String flags = info.getFlagsForTarget(extension);
		String outflag = info.getOutputFlag(extension);
		String outputPrefix = info.getOutputPrefix(extension);
		String targets = rebuild ? "clean all" : "all";

		// Get all the projects the build target depends on
		IProject[] deps = null;
		try {
			deps = project.getReferencedProjects();
		} catch (CoreException e) {
			// There are 2 exceptions; the project does not exist or it is not open
			// and neither conditions apply if we are building for it ....
		}

		// Write out the all target first in case someone just runs make
		buffer.append("all: deps" + WHITESPACE + outputPrefix + target + NEWLINE);
		buffer.append(NEWLINE);

		/*
		 * The build target may depend on other projects in the workspace. These are
		 * captured in the deps target:
		 * deps:
		 * 		<cd <Proj_Dep_1/build_dir>; $(MAKE) [clean all | all]> 
		 */
		List managedProjectOutputs = new ArrayList();
		buffer.append("deps:" + NEWLINE);
		if (deps != null) {
			for (int i = 0; i < deps.length; i++) {
				IProject dep = deps[i];
				String buildDir = dep.getLocation().toString();
				if (ManagedBuildManager.manages(dep)) {
					// Add the current configuration to the makefile path
					IManagedBuildInfo depInfo = ManagedBuildManager.getBuildInfo(dep);
					buildDir += SEPARATOR + depInfo.getConfigurationName();
					
					// Extract the build artifact to add to the dependency list
					String depTarget = depInfo.getBuildArtifactName();
					String depExt = (new Path(depTarget)).getFileExtension();
					String depPrefix = depInfo.getOutputPrefix(depExt);
					managedProjectOutputs.add(buildDir + SEPARATOR + depPrefix + depTarget);
				}
				buffer.append(TAB + "cd" + WHITESPACE + buildDir + SEMI_COLON + WHITESPACE + "$(MAKE) " + targets + NEWLINE);
			}
		}
		buffer.append(NEWLINE);

		/*
		 * Write out the target rule as:
		 * <prefix><target>.<extension>: $(OBJS) [<dep_proj_1_output> ... <dep_proj_n_output>]
		 * 		$(BUILD_TOOL) $(FLAGS) $(OUTPUT_FLAG) $@ $(OBJS) $(USER_OBJS) $(LIB_DEPS)
		 */
		//
		buffer.append(outputPrefix + target + COLON + WHITESPACE + "$(OBJS)");
		Iterator iter = managedProjectOutputs.listIterator();
		while (iter.hasNext()) {
			buffer.append(WHITESPACE + (String)iter.next());
		}
		buffer.append(NEWLINE);
		buffer.append(TAB + cmd + WHITESPACE + flags + WHITESPACE + outflag + WHITESPACE + "$@" + WHITESPACE + "$(OBJS) $(USER_OBJS) $(LIBS)");
		buffer.append(NEWLINE + NEWLINE);

		// Always add a clean target
		buffer.append("clean:" + NEWLINE);
		buffer.append(TAB + "-$(RM)" + WHITESPACE + "$(OBJS)" + WHITESPACE + outputPrefix + target + NEWLINE + NEWLINE);
		
		buffer.append(".PHONY: all clean deps" + NEWLINE + NEWLINE);
		
		buffer.append(ManagedBuilderCorePlugin.getResourceString(DEP_INCL) + NEWLINE);
		buffer.append("-include ${patsubst %, %/module.dep, $(SUBDIRS)}" + NEWLINE);
		return buffer;
	}

	protected void addRule(String relativePath, StringBuffer buffer, IResource resource) {
		String rule = null;
		String cmd = null;
		String buildFlags = null;
		String inputExtension = null;
		String outputExtension = null;
		String outflag = null;
		String outputPrefix = null;
		
		// Is there a special rule for this file
		if (false) {
		} 
		else {
			// Get the extension of the resource
			inputExtension = resource.getFileExtension();
			// ASk the build model what it will produce from this
			outputExtension = info.getOutputExtension(inputExtension);
			/*
			 * Create the pattern rule in the format
			 * <relative_path>/%.o: $(ROOT)/<relative_path>/%.cpp
			 * 		$(CC) $(CFLAGS) $(OUTPUT_FLAG) $@ $<
			 * 
			 * Note that CC CFLAGS and OUTPUT_FLAG all come from the build model
			 * and are resolved to a real command before writing to the module
			 * makefile, so a real command might look something like
			 * source1/%.o: $(ROOT)/source1/%.cpp
			 * 		g++ -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers -o $@ $<
			 */ 
			rule = relativePath + WILDCARD + DOT + outputExtension + COLON + WHITESPACE + "$(ROOT)" + SEPARATOR + relativePath + WILDCARD + DOT + inputExtension;
		}

		// Check if the rule is listed as something we already generated in the makefile
		if (!getRuleList().contains(rule)) {
			// Add it to the list
			getRuleList().add(rule);

			// Add the rule and command to the makefile
			buffer.append(rule + NEWLINE);
			cmd = info.getToolForSource(inputExtension);
			buildFlags = info.getFlagsForSource(inputExtension);
			outflag = info.getOutputFlag(outputExtension);
			outputPrefix = info.getOutputPrefix(outputExtension);
			buffer.append(TAB + cmd + WHITESPACE + buildFlags + WHITESPACE + outflag + WHITESPACE + outputPrefix + "$@" + WHITESPACE + "$<" + NEWLINE + NEWLINE);
		}
	}
	
	/**
	 * Adds the container of the argument to the list of folders in the project that
	 * contribute source files to the build. The resource visitor has already established 
	 * that the build model knows how to build the files. It has also checked that
	 * the resouce is not generated as part of the build.
	 *  
	 * @param resource
	 */
	public void appendBuildSubdirectory(IResource resource) {
		IContainer container = resource.getParent();
		if (!getSubdirList().contains(container)) {
			getSubdirList().add(container);		
		}
	}
	
	/**
	 * Adds the container of the argument to a list of subdirectories that are part 
	 * of an incremental rebuild of the project. The makefile fragments for these 
	 * directories will be regenerated as a result of the build.
	 * 
	 * @param resource
	 */
	public void appendModifiedSubdirectory(IResource resource) {
		IContainer container = resource.getParent();
		if (!getModifiedList().contains(container)) {
			getModifiedList().add(container);		
		}
	}

	/**
	 * Check whether the build has been cancelled. Cancellation requests 
	 * propagated to the caller by throwing <code>OperationCanceledException</code>.
	 * 
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	public void checkCancel() {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}


	/**
	 * Clients call this method when an incremental rebuild is required. The argument
	 * contains a set of resource deltas that will be used to determine which 
	 * subdirectories need a new makefile and dependency list (if any). 
	 * 
	 * @param delta
	 * @throws CoreException
	 */
	public void generateMakefiles(IResourceDelta delta) throws CoreException {
		/*
		 * Let's do a sanity check right now. This is an incremental build, so if the top-level directory is not there, then
		 * a rebuild is needed anyway.
		 */
		IFolder folder = project.getFolder(info.getConfigurationName());
		if (!folder.exists()) {
			regenerateMakefiles();
			shouldRunBuild(true);
			return;
		}

		// Visit the resources in the delta and compile a list of subdirectories to regenerate
		ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(this, info);
		delta.accept(visitor);
		// There may be nothing to regenerate and no content changes that require a rebuild 
		if (getModifiedList().isEmpty() && !shouldRunBuild()) {
			// There is nothing to build
			IStatus status = new Status(IStatus.INFO, ManagedBuilderCorePlugin.getUniqueIdentifier(), GeneratedMakefileBuilder.EMPTY_PROJECT_BUILD_ERROR, "", null);
			throw new CoreException(status);
		}

		// See if the user has cancelled the build
		checkCancel();
	
		// The top-level makefile needs this information
		ResourceProxyVisitor resourceVisitor = new ResourceProxyVisitor(this, info);
		project.accept(resourceVisitor, IResource.NONE);
		if (getSubdirList().isEmpty()) {
			// There is nothing to build (but we should never throw this exception)
			IStatus status = new Status(IStatus.INFO, ManagedBuilderCorePlugin.getUniqueIdentifier(), GeneratedMakefileBuilder.EMPTY_PROJECT_BUILD_ERROR, "", null);
			throw new CoreException(status);
		}
		checkCancel();

		// Regenerate any fragments that are missing for the exisiting directories NOT modified
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer subdirectory = (IContainer)iter.next();
			if (!getModifiedList().contains(subdirectory)) {
				// Make sure a fragment makefile and dependency file exist
				IFile makeFragment = project.getFile(subdirectory.getFullPath().addTrailingSeparator().append(MODFILE_NAME));
				IFile depFragment = project.getFile(subdirectory.getFullPath().addTrailingSeparator().append(DEPFILE_NAME));
				if (!makeFragment.exists() || !depFragment.exists()) {
					// If one or both are missing, then add it to the list to be generated
					getModifiedList().add(subdirectory);
				}
			}
		}

		// Re-create the top-level makefile
		topBuildDir = createDirectory(info.getConfigurationName());
		IPath makefilePath = topBuildDir.addTrailingSeparator().append(MAKEFILE_NAME);
		IFile makefileHandle = createFile(makefilePath);
		populateTopMakefile(makefileHandle, false);
		checkCancel();
		
		// Regenerate any fragments for modified directories
		iter = getModifiedList().listIterator();
		while (iter.hasNext()) {
			populateFragmentMakefile((IContainer) iter.next());
			checkCancel();
		}
	}
	
	/* (non-javadoc)
	 * @return
	 */
	private List getModifiedList() {
		if (modifiedList == null) {
			modifiedList = new ArrayList();
		}
		return modifiedList;
	}

	/* (non-javadoc)
	 * Answers the list of known build rules. This keeps me from generating duplicate
	 * rules for known file extensions.
	 * 
	 */
	private List getRuleList() {
		if (ruleList == null) {
			ruleList = new ArrayList();
		}
		return ruleList;
	}

	/* (non-javadoc)
	 * Answers the list of subdirectories contributing source code to the build
	 * @return
	 */
	private List getSubdirList() {
		if (subdirList == null) {
			subdirList = new ArrayList();
		}
		return subdirList;
	}

	/* (non-javadoc)
	 * @param string
	 * @return
	 */
	private IPath createDirectory(String dirName) throws CoreException {
		// Create or get the handle for the build directory 
		IFolder folder = project.getFolder(dirName);
		if (!folder.exists()) {

			// Make sure that parent folders exist
			IPath parentPath = (new Path(dirName)).removeLastSegments(1);
			// Assume that the parent exists if the path is empty
			if (!parentPath.isEmpty()) {
				IFolder parent = project.getFolder(parentPath);
				if (!parent.exists()) {
					createDirectory(parentPath.toString());
				}
			}
			
			// Now make the requested folder
			try {
				folder.create(true, true, null);
			}
			catch (CoreException e) {
				if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
					folder.refreshLocal(IResource.DEPTH_ZERO, null);
				else
					throw e;
			}
		}
		return folder.getFullPath();
	}

	/* (non-javadoc)
	 * @param makefilePath
	 * @param monitor
	 * @return
	 */
	private IFile createFile(IPath makefilePath) throws CoreException {
		// Create or get the handle for the makefile
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile newFile = root.getFileForLocation(makefilePath);
		if (newFile == null) {
			newFile = root.getFile(makefilePath);
		}
		// Create the file if it does not exist
		ByteArrayInputStream contents = new ByteArrayInputStream(new byte[0]);
		try {
			newFile.create(contents, false, monitor);
		}
		catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				newFile.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}

		return newFile;
	}

	/**
	 * Answers the <code>IPath</code> of the top directory generated for the build
	 * output, or <code>null</code> if none has been generated.
	 * 
	 * @return
	 */
	public IPath getTopBuildDir() {
		return topBuildDir;
	}

	/**
	 * Answers <code>true</code> if the argument is found in a generated container 
	 * @param resource
	 * @return
	 */
	public boolean isGeneratedResource(IResource resource) {
		// Is this a generated directory ...
		IPath path = resource.getProjectRelativePath();
		String[] configNames = info.getConfigurationNames();
		for (int i = 0; i < configNames.length; i++) {
			String name = configNames[i];
			IPath root = new Path(name);
			// It is if it is a root of the resource pathname
			if (root.isPrefixOf(path)) return true;
		}
		
		return false;
	}

	/* (non-javadoc)
	 * Create the entire contents of the makefile.
	 * 
	 * @param fileHandle The file to place the contents in.
	 * @param rebuild FLag signalling that the user is doing a full rebuild
	 */
	protected void populateTopMakefile(IFile fileHandle, boolean rebuild) {
		StringBuffer buffer = new StringBuffer();
		
		// Add the macro definitions
		buffer.append(addMacros());

		// Append the module list		
		buffer.append(addSubdirectories()); 

		// Add targets
		buffer.append(addTargets(rebuild));

		// Save the file
		try {
			Util.save(buffer, fileHandle);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-javadoc)
	 * @param module
	 */
	protected void populateFragmentMakefile(IContainer module) throws CoreException {
		// Calcualte the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		IPath buildRoot = getTopBuildDir().removeFirstSegments(1);
		if (buildRoot == null) {
			return;
		}
		IPath moduleOutputPath = buildRoot.append(moduleRelativePath);
		
		// Now create the directory
		IPath moduleOutputDir = createDirectory(moduleOutputPath.toString());
		
		// Create a module makefile
		IFile modMakefile = createFile(moduleOutputDir.addTrailingSeparator().append(MODFILE_NAME));
		StringBuffer makeBuf = new StringBuffer();
		makeBuf.append(addSources(module));

		// Create a module dep file
		IFile modDepfile = createFile(moduleOutputDir.addTrailingSeparator().append(DEPFILE_NAME));
		StringBuffer depBuf = new StringBuffer();
		depBuf.append(addSourceDependencies(module));

		// Save the files
		Util.save(makeBuf, modMakefile);
		Util.save(depBuf, modDepfile);
	}


	/**
	 * @throws CoreException
	 */
	public void regenerateMakefiles() throws CoreException {
		// Visit the resources in the project
		ResourceProxyVisitor visitor = new ResourceProxyVisitor(this, info);
		project.accept(visitor, IResource.NONE);
		if (getSubdirList().isEmpty()) {
			// There is nothing to build
			IStatus status = new Status(IStatus.INFO, ManagedBuilderCorePlugin.getUniqueIdentifier(), GeneratedMakefileBuilder.EMPTY_PROJECT_BUILD_ERROR, "", null);
			throw new CoreException(status);
		}

		// See if the user has cancelled the build
		checkCancel();

		// Create the top-level directory for the build output
		topBuildDir = createDirectory(info.getConfigurationName());
		
		// Create the top-level makefile
		IPath makefilePath = topBuildDir.addTrailingSeparator().append(MAKEFILE_NAME);
		IFile makefileHandle = createFile(makefilePath);
		
		// Populate the makefile
		populateTopMakefile(makefileHandle, true);
		checkCancel();
		
		// Now populate the module makefiles
		ListIterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			populateFragmentMakefile((IContainer)iter.next());
			checkCancel();
		}
	}
	/**
	 * Answers whether a build is required after the makefiles have been 
	 * generated.
	 * 
	 * @return
	 */
	public boolean shouldRunBuild() {
		return shouldRunBuild;
	}

	/**
	 * Sets the build flag to the value of the argument.
	 * 
	 * @param b
	 */
	public void shouldRunBuild(boolean b) {
		shouldRunBuild = b;
	}

}
