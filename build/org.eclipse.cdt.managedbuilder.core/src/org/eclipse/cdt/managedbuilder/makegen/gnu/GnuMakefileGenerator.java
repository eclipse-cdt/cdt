package org.eclipse.cdt.managedbuilder.makegen.gnu;

/**********************************************************************
 * Copyright (c) 2003,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This is a specialized makefile generator that takes advantage of the 
 * extensions present in Gnu Make.
 * 
 * @since 1.2
 */
public class GnuMakefileGenerator implements IManagedBuilderMakefileGenerator {

	/**
	 * This class walks the delta supplied by the build system to determine
	 * what resources have been changed. The logic is very simple. If a 
	 * buildable resource (non-header) has been added or removed, the directories 
	 * in which they are located are "dirty" so the makefile fragments for them 
	 * have to be regenerated.
	 * <p>
	 * The actual dependencies are recalculated as a result of the build step 
	 * itself. We are relying on make to do the right things when confronted 
	 * with a dependency on a moved header file. That said, make will treat 
	 * the missing header file in a dependency rule as a target it has to build 
	 * unless told otherwise. These dummy targets are added to the makefile 
	 * to avoid a missing target error. 
	 */
	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private GnuMakefileGenerator generator;
		private IManagedBuildInfo info;

		/**
		 * The constructor 
		 */
		public ResourceDeltaVisitor(GnuMakefileGenerator generator, IManagedBuildInfo info) {
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
				boolean moved = false;
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						if (!generator.isGeneratedResource(resource)) {
							// This is a source file so just add its container
							if (info.buildsFileType(ext)) {
								generator.appendModifiedSubdirectory(resource);
							}
						}
						break;
					case IResourceDelta.REMOVED:
						// we get this notification if a resource is moved too
						if (!generator.isGeneratedResource(resource)) {
							// This is a source file so just add its container
							if (info.buildsFileType(ext)) {
								generator.appendModifiedSubdirectory(resource);
								generator.appendDeletedFile(resource);
							}
						}
						break;
					default:
						keepLooking = true;
						break;
				}
			} if (resource.getType() == IResource.PROJECT) {
				// If there is a zero-length delta, something the project depends on has changed so just call make
				IResourceDelta[] children = delta.getAffectedChildren();
				if (children != null && children.length > 0) {
					keepLooking = true;
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
	 * This class is used to recursively walk the project and determine which
	 * modules contribute buildable source files. 
	 */
	protected class ResourceProxyVisitor implements IResourceProxyVisitor {
		private GnuMakefileGenerator generator;
		private IManagedBuildInfo info;

		/**
		 * Constructs a new resource proxy visitor to quickly visit project
		 * resources.
		 */
		public ResourceProxyVisitor(GnuMakefileGenerator generator, IManagedBuildInfo info) {
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

	// String constants for makefile contents and messages
	private static final String COMMENT = "MakefileGenerator.comment";	//$NON-NLS-1$
	private static final String AUTO_DEP = COMMENT + ".autodeps";	//$NON-NLS-1$
	private static final String MESSAGE = "ManagedMakeBuilder.message";	//$NON-NLS-1$
	private static final String BUILD_ERROR = MESSAGE + ".error";	//$NON-NLS-1$
	
	private static final String DEP_INCL = COMMENT + ".module.dep.includes";	//$NON-NLS-1$
	private static final String HEADER = COMMENT + ".header"; //$NON-NLS-1$
	
	protected static final String MESSAGE_FINISH_BUILD = ManagedMakeMessages.getResourceString("MakefileGenerator.message.finish.build");	//$NON-NLS-1$
	protected static final String MESSAGE_FINISH_FILE = ManagedMakeMessages.getResourceString("MakefileGenerator.message.finish.file");	//$NON-NLS-1$
	protected static final String MESSAGE_START_BUILD = ManagedMakeMessages.getResourceString("MakefileGenerator.message.start.build");	//$NON-NLS-1$
	protected static final String MESSAGE_START_FILE = ManagedMakeMessages.getResourceString("MakefileGenerator.message.start.file");	//$NON-NLS-1$
	private static final String MOD_INCL = COMMENT + ".module.make.includes";	//$NON-NLS-1$	
	private static final String MOD_LIST = COMMENT + ".module.list";	//$NON-NLS-1$	
	private static final String MOD_RULES = COMMENT + ".build.rule";	//$NON-NLS-1$	
	private static final String SRC_LISTS = COMMENT + ".source.list";	//$NON-NLS-1$	
	
	// Local variables needed by generator
	private String buildTargetName;
	private Vector deletedFileList;
	private Vector dependencyMakefiles;
	private String extension;
	protected IManagedBuildInfo info;
	protected Vector modifiedList;
	protected IProgressMonitor monitor;
	protected IProject project;
	protected Vector ruleList;
	protected Vector subdirList;
	protected IPath topBuildDir;

	public GnuMakefileGenerator() {
		super();
	}
	
	/* (non-Javadoc)
	 * Outputs a comment formatted as follows:
	 * ##### ....... #####
	 * # <Comment message>
	 * ##### ....... ##### 
	 */
	protected StringBuffer addDefaultHeader() {
		StringBuffer buffer = new StringBuffer();
		outputCommentLine(buffer);
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(HEADER) + NEWLINE);
		outputCommentLine(buffer);
		buffer.append(NEWLINE);
		return buffer;
	}

	/* (non-Javadoc)
	 * Answers a <code>StringBuffer</code> containing the comment(s) 
	 * for a dependency makefile.
	 */
	protected StringBuffer addFragmentDependenciesHeader() {
		return addDefaultHeader();
	}
	
	/* (non-Javadoc)
	 * Answers a <code>StringBuffer</code> containing the comment(s) 
	 * for a fragment makefile.
	 */
	protected StringBuffer addFragmentMakefileHeader() {
		return addDefaultHeader();
	}
	
	/* (non-javadoc)
	 */
	private StringBuffer addMacros() {
		StringBuffer buffer = new StringBuffer();
		
		// Add the ROOT macro
		buffer.append("ROOT := .." + NEWLINE); //$NON-NLS-1$
		buffer.append(NEWLINE);
		
		// include makefile.init supplementary makefile
		buffer.append("-include $(ROOT)" + SEPARATOR + MAKEFILE_INIT + NEWLINE); //$NON-NLS-1$
		buffer.append(NEWLINE);

		// Get the clean command from the build model
		buffer.append("RM := "); //$NON-NLS-1$
		buffer.append(info.getCleanCommand() + NEWLINE);
		buffer.append(NEWLINE);
		
		// Now add the source providers
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(SRC_LISTS) + NEWLINE);
		buffer.append("-include sources.mk" + NEWLINE); //$NON-NLS-1$
		buffer.append("-include $(SUBDIRS:%=%/subdir.mk)" + NEWLINE); //$NON-NLS-1$
		buffer.append("-include objects.mk" + NEWLINE); //$NON-NLS-1$
		buffer.append("-include $(DEPS)" + NEWLINE); //$NON-NLS-1$
		// Include makefile.defs supplemental makefile
		buffer.append("-include $(ROOT)" + SEPARATOR + MAKEFILE_DEFS + NEWLINE); //$NON-NLS-1$
		
		
		return (buffer.append(NEWLINE));
	}

	/* (non-javadoc)
	 * Create the pattern rule in the format:
	 * <relative_path>/<name>.<outputExtension>: $(ROOT)/<relative_path>/<name>.<inputExtension>
	 * 		@echo 'Building file: $<'
	 * 		@echo <tool> <flags> <output_flag><output_extension>$@ $<
	 * 		@<tool> <flags> <output_flag><output_extension>$@ $< && \
	 * 		echo -n '<relative_path>/<name>.d <relative_path>/' >> <relative_path>/<name>.d && \
	 * 		<tool> -P -MM -MG <flags> $< >> <relative_path>/<name>.d
	 * 		@echo 'Finished building: $<'
	 * 		@echo ' '
	 * 
	 * Note that the macros all come from the build model and are 
	 * resolved to a real command before writing to the module
	 * makefile, so a real command might look something like:
	 * source1/foo.o: $(ROOT)/source1/foo.cpp
	 * 		@echo 'Building file: $<'
	 * 		@echo g++ -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers -o$@ $<
	 * 		@ g++ -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers -o$@ $< && \
	 * 		echo -n 'source1/foo.d source1/' >> source1/foo.d && \
	 * 		g++ -P -MM -MG -g -O2 -c -I/cygdrive/c/eclipse/workspace/Project/headers $< >> source1/foo.d
	 * 		@echo 'Finished building: $<'
	 * 		@echo ' '
	 * 
	 * @param relativePath
	 * @param buffer
	 * @param resource
	 */
	private void addRule(String relativePath, StringBuffer buffer, IResource resource) {
		String buildFlags = null;
		String resourceName = getFileName(resource);
		String inputExtension = resource.getFileExtension();
		String cmd = info.getToolForSource(inputExtension);
		String outputExtension = info.getOutputExtension(inputExtension);
		String outflag = null;
		String outputPrefix = null;
		
		// Add the rule and command to the makefile
		String buildRule = relativePath + resourceName + DOT + outputExtension + COLON + WHITESPACE + ROOT + SEPARATOR + relativePath + resourceName + DOT + inputExtension; 
		buffer.append(buildRule + NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_START_FILE + WHITESPACE + IN_MACRO + SINGLE_QUOTE + NEWLINE);
		buildFlags = info.getFlagsForSource(inputExtension);
		outflag = info.getOutputFlag(outputExtension);
		outputPrefix = info.getOutputPrefix(outputExtension);
		
		// The command to build
		String buildCmd = cmd + WHITESPACE + buildFlags + WHITESPACE + outflag + outputPrefix + OUT_MACRO + WHITESPACE + IN_MACRO;
		buffer.append(TAB + AT + ECHO + WHITESPACE + buildCmd + NEWLINE);
		buffer.append(TAB + AT + buildCmd);
		
		// TODO determine if there are any deps to calculate
		if (true) {
			buffer.append(WHITESPACE + LOGICAL_AND + WHITESPACE + LINEBREAK);
			// TODO get the dep rule out of the tool
			String depRule =  relativePath + resourceName + DOT + DEP_EXT;
			getDependencyMakefiles().add(depRule);
			buffer.append(TAB + ECHO + WHITESPACE + "-n" + WHITESPACE + SINGLE_QUOTE + depRule + WHITESPACE + relativePath + SINGLE_QUOTE + WHITESPACE + ">" + WHITESPACE + depRule + WHITESPACE + LOGICAL_AND + WHITESPACE + LINEBREAK); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append(TAB + cmd + WHITESPACE + "-MM -MG -P -w" + WHITESPACE + buildFlags + WHITESPACE + IN_MACRO + WHITESPACE + ">>" + WHITESPACE + depRule); //$NON-NLS-1$ //$NON-NLS-2$
			
		}
		
		// Say goodbye to the nice user
		buffer.append(NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_FINISH_FILE + WHITESPACE + IN_MACRO + SINGLE_QUOTE + NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + WHITESPACE + SINGLE_QUOTE + NEWLINE + NEWLINE);
	}


	/* (non-javadoc)
	 * Answers a <code>StringBuffer</code> containing all of the sources contributed by
	 * a container to the build.
	 * 
	 * @param module
	 * @return StringBuffer
	 */
	private StringBuffer addSources(IContainer module) throws CoreException {
		// Calculate the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		String relativePath = moduleRelativePath.toString();
		relativePath += relativePath.length() == 0 ? "" : SEPARATOR;  //$NON-NLS-1$

 		// get the target for this project
 		ITarget myTarget = info.getDefaultTarget();
 		
 		// get the list of tools associated with our target
 		ITool toolArray[] = myTarget.getTools();
 		
 		// For each tool for the target, lookup the kinds of sources it can handle and
 		// create a map which will map its extension to a string which holds its list of sources.
  		HashMap extensionToRuleStringMap = new HashMap();
 		
 		// get the set of output extensions for all tools
 		Set outputExtensionsSet = getOutputExtentions();
 		
 		// put in rules if the file type is not a generated file
 		for(int k = 0; k < toolArray.length; k++) {
 			List extensionsList = toolArray[k].getInputExtensions();
  			// iterate over all extensions that the tool knows how to handle
 			Iterator exListIterator = extensionsList.iterator();
  			while(exListIterator.hasNext())	{
  				// create a macro of the form "EXTENSION_SRCS := "
  				String extensionName = exListIterator.next().toString();
  				if(!extensionToRuleStringMap.containsKey(extensionName) && // do we already have a map entry?
  						!outputExtensionsSet.contains(extensionName)) { // is the file generated?

  					// Get the name in the proper macro format
  					StringBuffer macroName = getMacroName(extensionName);
  					
  					// there is no entry in the map, so create a buffer for this extension
 					StringBuffer tempBuffer = new StringBuffer();
 					tempBuffer.append(macroName + WHITESPACE + "+=" + WHITESPACE + LINEBREAK);	//$NON-NLS-1$
 					tempBuffer.append("${addprefix $(ROOT)/" + relativePath + "," + LINEBREAK);	//$NON-NLS-1$ //$NON-NLS-2$
 					
 					// have to store the buffer in String form as StringBuffer is not a sublcass of Object
 					extensionToRuleStringMap.put(extensionName, tempBuffer.toString());
 				}
 			}	
 		}
 		
 		// String buffers
 		StringBuffer buffer = new StringBuffer();
 		StringBuffer ruleBuffer = new StringBuffer(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MOD_RULES) + NEWLINE);

		// Visit the resources in this folder
		IResource[] resources = module.members();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE) {
				String ext = resource.getFileExtension();
				if (info.buildsFileType(ext)) {
 					// look for the extension in the map
 					StringBuffer bufferForExtension = new StringBuffer();
 					bufferForExtension.append(extensionToRuleStringMap.get(ext).toString());
 					if(bufferForExtension != null &&
 							!outputExtensionsSet.contains(bufferForExtension.toString())) {
 						
 						bufferForExtension.append(resource.getName() + WHITESPACE + LINEBREAK);
 						
 						// re-insert string in the map
 						extensionToRuleStringMap.put(ext, bufferForExtension.toString());
 						
 						// Try to add the rule for the file
 						addRule(relativePath, ruleBuffer, resource);
 					}
				}
			}
		}
					
		// Write out the source info to the buffer
 		Collection bufferCollection = extensionToRuleStringMap.values();
 		Iterator collectionIterator = bufferCollection.iterator();
 		while(collectionIterator.hasNext())
 		{
 			// close off the rule and put two newlines to the buffer
 			StringBuffer currentBuffer = new StringBuffer();
 			currentBuffer.append(collectionIterator.next().toString());
 			currentBuffer.append("}" + NEWLINE + NEWLINE); //$NON-NLS-1$
 			
 			// append the contents of the buffer to the master buffer for the whole file
 			buffer.append(currentBuffer);
 		}
		return buffer.append(ruleBuffer + NEWLINE);
	}

	/* (non-javadoc)
	 * @return
	 */
	private StringBuffer addSubdirectories() {
		StringBuffer buffer = new StringBuffer();
		// Add the comment
		buffer.append(COMMENT_SYMBOL + WHITESPACE + ManagedMakeMessages.getResourceString(MOD_LIST) + NEWLINE);
		
		buffer.append("SUBDIRS := " + LINEBREAK); //$NON-NLS-1$
		
		// Get all the module names
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer container = (IContainer) iter.next();
			// Check the special case where the module is the project root
			if (container.getFullPath() == project.getFullPath()) {
				buffer.append(DOT + WHITESPACE + LINEBREAK);
			} else {
				IPath path = container.getProjectRelativePath();
				buffer.append(path.toString() +  WHITESPACE + LINEBREAK);
			}
		}

		buffer.append(NEWLINE);
		return buffer;
	}

	/* (non-javadoc)
	 * Answers a <code>StrinBuffer</code> containing all of the required targets to
	 * properly build the project.
	 * 
	 * @return StringBuffer
	 */
	private StringBuffer addTargets(boolean rebuild) {
		StringBuffer buffer = new StringBuffer();

		// Assemble the information needed to generate the targets
		String cmd = info.getToolForTarget(extension);
		String flags = info.getFlagsForTarget(extension);
		String outflag = info.getOutputFlag(extension);
		String outputPrefix = info.getOutputPrefix(extension);
		String targets = rebuild ? "clean all" : "all"; //$NON-NLS-1$ //$NON-NLS-2$

		// Get all the projects the build target depends on
		IProject[] refdProjects = null;
		try {
			refdProjects = project.getReferencedProjects();
		} catch (CoreException e) {
			// There are 2 exceptions; the project does not exist or it is not open
			// and neither conditions apply if we are building for it ....
		}
		
		// Write out the all target first in case someone just runs make
		// 	all: targ_<target_name> 
		String defaultTarget = "all:"; //$NON-NLS-1$
		buffer.append(defaultTarget + WHITESPACE + outputPrefix + buildTargetName);
		if (extension.length() > 0) {
			buffer.append(DOT + extension);
		}
		buffer.append(NEWLINE + NEWLINE);

		/*
		 * The build target may depend on other projects in the workspace. These are
		 * captured in the deps target:
		 * deps:
		 * 		<cd <Proj_Dep_1/build_dir>; $(MAKE) [clean all | all]> 
		 */
		Vector managedProjectOutputs = new Vector(refdProjects.length);
		if (refdProjects.length > 0) {
			boolean addDeps = true;
			if (refdProjects != null) {
				for (int i = 0; i < refdProjects.length; i++) {
					IProject dep = refdProjects[i];
					if (!dep.exists()) continue;
					if (addDeps) {
						buffer.append("dependents:" + NEWLINE); //$NON-NLS-1$						
						addDeps = false;
					}
					String buildDir = dep.getLocation().toString();
					String depTargets = targets;
					if (ManagedBuildManager.manages(dep)) {
						// Add the current configuration to the makefile path
						IManagedBuildInfo depInfo = ManagedBuildManager.getBuildInfo(dep);
						buildDir += SEPARATOR + depInfo.getConfigurationName();
					
						// Extract the build artifact to add to the dependency list
						String depTarget = depInfo.getBuildArtifactName();
						String depExt = depInfo.getBuildArtifactExtension();
						String depPrefix = depInfo.getOutputPrefix(depExt);
						if (depInfo.needsRebuild()) {
							depTargets = "clean all"; //$NON-NLS-1$
						}
						String dependency = buildDir + SEPARATOR + depPrefix + depTarget;
						if (depExt.length() > 0) {
							dependency += DOT + depExt;
						}
						managedProjectOutputs.add(dependency);
					}
					buffer.append(TAB + "-cd" + WHITESPACE + buildDir + WHITESPACE + LOGICAL_AND + WHITESPACE + "$(MAKE) " + depTargets + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			buffer.append(NEWLINE);
		}

		/*
		 * Write out the target rule as:
		 * targ_<prefix><target>.<extension>: $(OBJS) <refd_project_1 ... refd_project_n>
		 * 		@echo 'Building target: $@'
		 * 		$(BUILD_TOOL) $(FLAGS) $(OUTPUT_FLAG)$@ $(OBJS) $(USER_OBJS) $(LIB_DEPS)
		 * 		@echo 'Finished building: $@'
		 * 		@echo
		 */
		buffer.append(outputPrefix + buildTargetName);
		if (extension.length() > 0) {
			buffer.append(DOT + extension);
		}
		buffer.append(COLON + WHITESPACE + "$(OBJS)"); //$NON-NLS-1$
		Iterator refIter = managedProjectOutputs.listIterator();
		while (refIter.hasNext()) {
			buffer.append(WHITESPACE + (String)refIter.next());
		}
		buffer.append(NEWLINE);
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_START_BUILD + WHITESPACE + OUT_MACRO + SINGLE_QUOTE + NEWLINE);
		buffer.append(TAB + cmd + WHITESPACE + flags + WHITESPACE + outflag + OUT_MACRO + WHITESPACE + "$(OBJS) $(USER_OBJS) $(LIBS)" + NEWLINE); //$NON-NLS-1$
		buffer.append(TAB + AT + ECHO + WHITESPACE + SINGLE_QUOTE + MESSAGE_FINISH_FILE + WHITESPACE + OUT_MACRO + SINGLE_QUOTE + NEWLINE + NEWLINE);

		// Always add a clean target
		buffer.append("clean:" + NEWLINE); //$NON-NLS-1$
		buffer.append(TAB + "-$(RM)" + WHITESPACE + "$(OBJS)" + WHITESPACE + "$(DEPS)" + WHITESPACE + outputPrefix + buildTargetName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (extension.length() > 0) {
			buffer.append(DOT + extension);
		}
		buffer.append(NEWLINE + NEWLINE); 
		
		// Add all the eneded dummy and phony targets
		buffer.append(".PHONY: all clean dependents" + NEWLINE); //$NON-NLS-1$
		refIter = managedProjectOutputs.listIterator();
		while(refIter.hasNext()) {
			buffer.append((String)refIter.next() + COLON + NEWLINE);
		}
		buffer.append(NEWLINE);
		
		// Include makefile.targets supplemental makefile
		buffer.append("-include $(ROOT)" + SEPARATOR + MAKEFILE_TARGETS + NEWLINE); //$NON-NLS-1$

		return buffer;
	}

	/* (non-Javadoc)
	 * Answers a <code>StringBuffer</code> containing the comment(s) 
	 * for the top-level makefile.
	 */
	protected StringBuffer addTopHeader() {
		return addDefaultHeader();
	}
	
	/**
	 * Adds the container of the argument to the list of folders in the project that
	 * contribute source files to the build. The resource visitor has already established 
	 * that the build model knows how to build the files. It has also checked that
	 * the resource is not generated as part of the build.
	 *  
	 * @param resource
	 */
	protected void appendBuildSubdirectory(IResource resource) {
		IContainer container = resource.getParent();
		if (!getSubdirList().contains(container)) {
			getSubdirList().add(container);		
		}
	}

	/**
	 * If a file is removed from a source folder (either because of a delete 
	 * or move action on the part of the user), the makefilegenerator has to
	 * remove the dependency makefile along with the old build goal 
	 *  
	 * @param resource
	 */
	protected void appendDeletedFile(IResource resource) {
		// Cache this for now
		getDeletedFileList().add(resource);
	}
	
	/**
	 * Adds the container of the argument to a list of subdirectories that are part 
	 * of an incremental rebuild of the project. The makefile fragments for these 
	 * directories will be regenerated as a result of the build.
	 * 
	 * @param resource
	 */
	protected void appendModifiedSubdirectory(IResource resource) {
		IContainer container = resource.getParent();
		if (!getModifiedList().contains(container)) {
			getModifiedList().add(container);		
		}
	}
	
	/* (non-Javadoc)
	 * @param message
	 */
	protected void cancel(String message) {
		if (monitor != null && !monitor.isCanceled()) {
			throw new OperationCanceledException(message);
		}
	}

	/**
	 * Check whether the build has been cancelled. Cancellation requests 
	 * propagated to the caller by throwing <code>OperationCanceledException</code>.
	 * 
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	protected void checkCancel() {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/* (non-Javadoc)
	 * Return or create the folder needed for the build output. If we are
	 * creating the folder, set the derived bit to true so the CM system 
	 * ignores the contents. If the resource exists, respect the existing 
	 * derived setting. 
	 * 
	 * @param string
	 * @return IPath
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

			// Make sure the folder is marked as derived so it is not added to CM
			if (!folder.isDerived()) {
				folder.setDerived(true);
			}
		}
	
		return folder.getFullPath();
	}

	/* (non-Javadoc)
	 * Return or create the makefile needed for the build. If we are creating 
	 * the resource, set the derived bit to true so the CM system ignores 
	 * the contents. If the resource exists, respect the existing derived 
	 * setting.
	 *  
	 * @param makefilePath
	 * @return IFile
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
			newFile.create(contents, false, new SubProgressMonitor(monitor, 1));
			// Make sure the new file is marked as derived
			if (!newFile.isDerived()) {
				newFile.setDerived(true);
			}

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
	 * @param deletedFile
	 */
	private void deleteBuildTarget(IResource deletedFile) {
		// Get the project relative path of the file
		String fileName = getFileName(deletedFile);
		String srcExtension = deletedFile.getFileExtension();
		String targetExtension = info.getOutputExtension(srcExtension);
		fileName += DOT + targetExtension;
		IPath projectRelativePath = deletedFile.getProjectRelativePath().removeLastSegments(1);
		IPath targetFilePath = getBuildWorkingDir().append(projectRelativePath).append(fileName);
		IResource depFile = project.findMember(targetFilePath);
		if (depFile != null && depFile.exists()) {
			try {
				depFile.delete(true, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// This had better be allowed during a build
				ManagedBuilderCorePlugin.log(e);
			}
		}		
	}
	
	/**
	 * @param deletedFile
	 */
	private void deleteDepFile(IResource deletedFile) {
		// Get the project relative path of the file
		String fileName = getFileName(deletedFile);
		fileName += DOT + DEP_EXT;
		IPath projectRelativePath = deletedFile.getProjectRelativePath().removeLastSegments(1);
		IPath depFilePath = getBuildWorkingDir().append(projectRelativePath).append(fileName);
		IResource depFile = project.findMember(depFilePath);
		if (depFile != null && depFile.exists()) {
			try {
				depFile.delete(true, new SubProgressMonitor(monitor, 1));
			} catch (CoreException e) {
				// This had better be allowed during a build
				ManagedBuilderCorePlugin.log(e);
			}
		}
	}

	/* (non-Javadoc)
	 * Answers the argument with all whitespaces replaced with an escape sequence.
	 * 
	 * @param path
	 */
	protected String escapeWhitespaces(String path) {
		// Escape the spaces in the path/filename if it has any
		String[] segments = path.split("\\s"); //$NON-NLS-1$
		if (segments.length > 1) {
			StringBuffer escapedPath = new StringBuffer();
			for (int index = 0; index < segments.length; ++index) {
				escapedPath.append(segments[index]);
				if (index + 1 < segments.length) {
					escapedPath.append("\\ "); //$NON-NLS-1$
				}
			}
			return escapedPath.toString().trim();
		} else {
			return path;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateDependencies()
	 */
	public void generateDependencies() throws CoreException {
		// This is a hack for the pre-3.x GCC compilers
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		Iterator subDirs = getSubdirList().listIterator();
		while(subDirs.hasNext()) {
			// The builder creates a subdir with same name as source in the build location
			IContainer subDir = (IContainer)subDirs.next();
			IPath projectRelativePath = subDir.getProjectRelativePath();
			IPath buildRelativePath = topBuildDir.append(projectRelativePath);
			IFolder buildFolder = root.getFolder(buildRelativePath);
			if (buildFolder == null) continue;

			// Find all of the dep files in the generated subdirectories
			IResource[] files = buildFolder.members();
			for (int index = 0; index < files.length; ++index){
				IResource file = files[index];
				if (DEP_EXT.equals(file.getFileExtension())) {
					IFile depFile = root.getFile(file.getFullPath());
					if (depFile == null) continue;
					try {
						populateDummyTargets(depFile, false);
					} catch (CoreException e) {
						throw e;
					} catch (IOException e) {
						// Keep trying
						ManagedBuilderCorePlugin.log(e);
						continue;
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#generateMakefiles(org.eclipse.core.resources.IResourceDelta)
	 */
	public void generateMakefiles(IResourceDelta delta) throws CoreException {
		/*
		 * Let's do a sanity check right now. 
		 * 
		 * 1. This is an incremental build, so if the top-level directory is not 
		 * there, then a rebuild is needed.
		 */
		IFolder folder = project.getFolder(info.getConfigurationName());
		if (!folder.exists()) {
			regenerateMakefiles();
			return;
		}
		
		// Make sure the build directory is available
		topBuildDir = createDirectory(info.getConfigurationName());
		checkCancel();
		
		// Visit the resources in the delta and compile a list of subdirectories to regenerate
		ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(this, info);
		delta.accept(visitor);
		checkCancel();
	
		// Get all the subdirectories participating in the build
		ResourceProxyVisitor resourceVisitor = new ResourceProxyVisitor(this, info);
		project.accept(resourceVisitor, IResource.NONE);
		IPath srcsFilePath = topBuildDir.addTrailingSeparator().append(SRCSFILE_NAME);
		IFile srcsFileHandle = createFile(srcsFilePath);
		populateSourcesMakefile(srcsFileHandle);
		checkCancel();
		
		// Regenerate any fragments that are missing for the exisiting directories NOT modified
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer subdirectory = (IContainer)iter.next();
			if (!getModifiedList().contains(subdirectory)) {
				// Make sure a fragment makefile exists
				IPath fragmentPath = getBuildWorkingDir().append(subdirectory.getProjectRelativePath()).addTrailingSeparator().append(MODFILE_NAME);
				IFile makeFragment = project.getFile(fragmentPath);
				if (!makeFragment.exists()) {
					// If one or both are missing, then add it to the list to be generated
					getModifiedList().add(subdirectory);
				}
			}
		}

		// Delete the old dependency files for any deleted resources
		iter = getDeletedFileList().listIterator();
		while (iter.hasNext()) {
			IResource deletedFile = (IResource)iter.next();
			deleteDepFile(deletedFile);
			deleteBuildTarget(deletedFile);
		}

		// Re-create the top-level makefile
		IPath makefilePath = topBuildDir.addTrailingSeparator().append(MAKEFILE_NAME);
		IFile makefileHandle = createFile(makefilePath);
		populateTopMakefile(makefileHandle, false);
		checkCancel();
		
		// Regenerate any fragments for modified directories
		iter = getModifiedList().listIterator();
		while (iter.hasNext()) {
			IContainer subDir = (IContainer) iter.next();
			populateFragmentMakefile(subDir);
			checkCancel();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getTopBuildDir()
	 */
	public IPath getBuildWorkingDir() {
		if (topBuildDir != null) {
			return topBuildDir.removeFirstSegments(1);
		}
		return null;
	}


	/**
	 * @return
	 */
	private Vector getDeletedFileList() {
		if (deletedFileList == null) {
			deletedFileList = new Vector();
		}
		return deletedFileList;
	}

	/**
	 * @return
	 */
	private Vector getDependencyMakefiles() {
		if (dependencyMakefiles == null) {
			dependencyMakefiles = new Vector();
		}
		return dependencyMakefiles;
	}
	
	/**
	 * Strips off the file extension from the argument and returns 
	 * the name component in a <code>String</code>
	 * 
	 * @param file
	 * @return
	 */
	private String getFileName(IResource file) {
		String answer = new String();
		String lastSegment = file.getName();
		int extensionSeparator = lastSegment.lastIndexOf(DOT);
		if (extensionSeparator != -1) {
			answer = lastSegment.substring(0, extensionSeparator);
		}
		return answer;
	}
	
	protected StringBuffer getMacroName(String extensionName) {
		StringBuffer macroName = new StringBuffer();
		
		// We need to handle case sensitivity in file extensions (e.g. .c vs .C), so if the
		// extension was already upper case, tack on an "UPPER_" to the macro name.
		// In theory this means there could be a conflict if you had for example,
		// extensions .c_upper, and .C, but realistically speaking the chances of this are
		// practically nil so it doesn't seem worth the hassle of generating a truly
		// unique name.
		if(extensionName.equals(extensionName.toUpperCase())) {
			macroName.append(extensionName.toUpperCase() + "_UPPER");	//$NON-NLS-1$
		} else {
			// lower case... no need for "UPPER_"
			macroName.append(extensionName.toUpperCase());
		}
		macroName.append("_SRCS");	//$NON-NLS-1$
		return macroName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#getMakefileName()
	 */
	public String getMakefileName() {
		return new String(MAKEFILE_NAME);
	}
	
	/* (non-javadoc)
	 * 
	 * @return List
	 */
	private Vector getModifiedList() {
		if (modifiedList == null) {
			modifiedList = new Vector();
		}
		return modifiedList;
	}

	protected Set getOutputExtentions() {
		// Get the target for this project
		ITarget myTarget = info.getDefaultTarget();

		// Get the list of tools associated with our target
		ITool toolArray[] = myTarget.getTools();
		
		// The set of output extensions which will be produced by this tool.
		// It is presumed that this set is not very large (likely < 10) so
		// a HashSet should provide good performance.
		HashSet outputExtensionsSet = new HashSet();
		
		// For each tool for the target, lookup the kinds of sources it outputs
		// and add that to our list of output extensions.
		for(int k = 0; k < toolArray.length; k++)
		{
			String[] outputs = toolArray[k].getOutputExtensions();
			outputExtensionsSet.addAll(Arrays.asList(outputs));
		}
 		return outputExtensionsSet;
	}

	/* (non-javadoc)
	 * Answers the list of known build rules. This keeps me from generating duplicate
	 * rules for known file extensions.
	 * 
	 * @return List
	 */
	private Vector getRuleList() {
		if (ruleList == null) {
			ruleList = new Vector();
		}
		return ruleList;
	}

	/* (non-javadoc)
	 * Answers the list of subdirectories contributing source code to the build
	 * 
	 * @return List
	 */
	private Vector getSubdirList() {
		if (subdirList == null) {
			subdirList = new Vector();
		}
		return subdirList;
	}
	
	/**
	 * @param project
	 * @param info
	 * @param monitor
	 */
	public void initialize(IProject project, IManagedBuildInfo info, IProgressMonitor monitor) {
		// Save the project so we can get path and member information
		this.project = project;
		// Save the monitor reference for reporting back to the user
		this.monitor = monitor;
		// Get the build info for the project
		this.info = info;
		// Get the name of the build target
		buildTargetName = info.getBuildArtifactName();
		// Get its extension
		extension = info.getBuildArtifactExtension();
		if (extension == null) {
			extension = new String();
		}
	}
	
	/**
	 * Answers <code>true</code> if the argument is found in a generated container 
	 * @param resource
	 * @return boolean
	 */
	protected boolean isGeneratedResource(IResource resource) {
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

	/* (non-Javadoc)
	 * Put COLS_PER_LINE comment charaters in the argument.
	 */
	protected void outputCommentLine(StringBuffer buffer) {
		for (int i = 0; i < COLS_PER_LINE; i++) {
			buffer.append(COMMENT_SYMBOL);
		}
		buffer.append(NEWLINE);		
	}

	protected void populateDummyTargets(IFile makefile, boolean force) throws CoreException, IOException {
		if (makefile == null || !makefile.exists()) return;
		
		// Found that bad boy, so let's get its contents
		InputStream contentStream = makefile.getContents(false);
		Reader in = new InputStreamReader(contentStream);
		StringBuffer inBuffer = null;
		int chunkSize = contentStream.available();
		inBuffer = new StringBuffer(chunkSize);
		char[] readBuffer = new char[chunkSize];
		int n = in.read(readBuffer);
		while (n > 0) {
			inBuffer.append(readBuffer);
			n = in.read(readBuffer);
		}
		contentStream.close();  
		
		// Now find the header file dependencies and make dummy targets for them
		boolean save = false;
		StringBuffer outBuffer = null;
		if (inBuffer != null) {
			// Here are the tokens in the file
			String[] dependencies = inBuffer.toString().split("\\s");	//$NON-NLS-1$
			if (dependencies.length == 0) return;
			
			// If we are doing an incremental build, only update the files that do not have a comment
			String firstLine = dependencies[0];
			if (!force) {
				if (firstLine.startsWith(COMMENT_SYMBOL)) {
					return;
				}
			}

			// Put the generated comments in
			if (!firstLine.startsWith(COMMENT_SYMBOL)) {
				outBuffer = addDefaultHeader();
			} else {
				outBuffer = new StringBuffer();
			}
			
			// Some echo implementations misbehave and put the -n and newline in the output
			if (firstLine.startsWith("-n")) { //$NON-NLS-1$
				// Create a vector with all the strings
				Vector tokens = new Vector(dependencies.length);
				for (int index = 1; index < dependencies.length; ++index) {
					String token = dependencies[index];
					if (token.length() > 0) {
						tokens.add(token);
					}
				}
				tokens.trimToSize();
				
				// Now let's parse:
				// Win32 outputs -n '<path>/<file>.d <path>/'
				// POSIX outputs -n <path>/<file>.d <path>/
				// Get the dep file name
				String secondLine;
				try {
					secondLine = (String) tokens.get(0);
				} catch (ArrayIndexOutOfBoundsException e) {
					secondLine = new String();
				}
				if (secondLine.startsWith("'")) { //$NON-NLS-1$
					// This is the Win32 implementation of echo (MinGW without MSYS)
					outBuffer.append(secondLine.substring(1) + WHITESPACE);
				} else {
					outBuffer.append(secondLine + WHITESPACE);
				}
				
				// The relative path to the build goal comes next
				String thirdLine;
				try {
					thirdLine = (String) tokens.get(1);
				} catch (ArrayIndexOutOfBoundsException e) {
					thirdLine = new String();
				}
				int lastIndex = thirdLine.lastIndexOf("'"); //$NON-NLS-1$
				if (lastIndex != -1) {
					if (lastIndex == 0) {
						outBuffer.append(WHITESPACE);
					} else {
						outBuffer.append(thirdLine.substring(0, lastIndex - 1));
					}
				} else {
					outBuffer.append(thirdLine);
				}
				
				// followed by the actual dependencies
				String fourthLine;
				try {
					fourthLine = (String) tokens.get(2);
				} catch (ArrayIndexOutOfBoundsException e) {
					fourthLine = new String();
				}
				outBuffer.append(fourthLine + WHITESPACE);
				
				// Now do the rest
				try {
					Iterator iter = tokens.listIterator(3);
					while (iter.hasNext()) {
						String nextElement = (String)iter.next();
						if (nextElement.endsWith("\\")) { //$NON-NLS-1$
							outBuffer.append(nextElement + NEWLINE + WHITESPACE);
						} else {
							outBuffer.append(nextElement + WHITESPACE);
						}
					}
				} catch (IndexOutOfBoundsException e) {					
				}

			} else {
				outBuffer.append(inBuffer);
			}
			
			outBuffer.append(NEWLINE);
			save = true;
			
			// Dummy targets to add to the makefile
			for (int i = 0; i < dependencies.length; ++i) {
				IPath dep = new Path(dependencies[i]);
				String extension = dep.getFileExtension();
				if (info.isHeaderFile(extension)) {
					/*
					 * The formatting here is 
					 * <dummy_target>:
					 */
					outBuffer.append(dependencies[i] + COLON + NEWLINE + NEWLINE);
				}
			}
		}
		
		// Write them out to the makefile
		if (save) {
			Util.save(outBuffer, makefile);
		}		
	}
	
	/* (non-javadoc)
	 * @param module
	 * @throws CoreException
	 */
	protected void populateFragmentMakefile(IContainer module) throws CoreException {
		// Calculate the new directory relative to the build output
		IPath moduleRelativePath = module.getProjectRelativePath();
		IPath buildRoot = getBuildWorkingDir();
		if (buildRoot == null) {
			return;
		}
		IPath moduleOutputPath = buildRoot.append(moduleRelativePath);
		
		// Now create the directory
		IPath moduleOutputDir = createDirectory(moduleOutputPath.toString());
		
		// Create a module makefile
		IFile modMakefile = createFile(moduleOutputDir.addTrailingSeparator().append(MODFILE_NAME));
		StringBuffer makeBuf = new StringBuffer();
		makeBuf.append(addFragmentMakefileHeader());
		makeBuf.append(addSources(module));

		// Save the files
		Util.save(makeBuf, modMakefile);
	}

	/**
	 * The makefile generator "knows" about source files participating in the 
	 * the build. It does not keep track of the targets that the build produces. 
	 * Instead, it keeps a set of transformation macros that it supplies to the 
	 * makefile so that the source names can be transofrmed into the target names 
	 * using the built-in string substitution functions of <code>make</code>.  
	 * 
	 * @param fileHandle The file that should be populated with the output 
	 * @throws CoreException
	 */
	protected void populateObjectsMakefile(IFile fileHandle) throws CoreException {
		// Master list of "object" dependencies, i.e. dependencies between input files and output files.
		StringBuffer macroBuffer = new StringBuffer();
		macroBuffer.append(addDefaultHeader()); 
		StringBuffer objectsBuffer = new StringBuffer();
		objectsBuffer.append("OBJS := " + LINEBREAK);	//$NON-NLS-1$
		StringBuffer depFilesBuffer = new StringBuffer();
		depFilesBuffer.append("DEPS := " + LINEBREAK);	//$NON-NLS-1$
		
		// Add the libraries this project depends on
		macroBuffer.append("LIBS := "); //$NON-NLS-1$
		String[] libs = info.getLibsForTarget(extension);
		for (int i = 0; i < libs.length; i++) {
			String string = libs[i];
			macroBuffer.append(LINEBREAK + string);
		}
		macroBuffer.append(NEWLINE);
		
		// Add the extra user-specified objects
		macroBuffer.append("USER_OBJS := "); //$NON-NLS-1$
		String[] userObjs = info.getUserObjectsForTarget(extension);
		for (int j = 0; j < userObjs.length; j++) {
			String string = userObjs[j];
			macroBuffer.append(LINEBREAK + string);
		}
		macroBuffer.append(NEWLINE);
		
		// Dependencies for generated files will not appear here.  I.e., if you have a tool which turns
		// A into B, and then another tool which turns B into C, you will only get dependency info
		// which says that B depends on A.
 		// TODO Handle dependencies for complex chains of the form A->B->C
 		ITarget myTarget = info.getDefaultTarget();
 		
 		// get the list of tools associated with our target
 		ITool toolArray[] = myTarget.getTools();
 		
 		// get the set of output extensions for all tools
 		Set outputExtensionsSet = getOutputExtentions();
 		
 		// set of input extensions for which rules have been created so far
 		HashSet handledInputExtensionsSet = new HashSet();
 		
 		// Look at each input extension and generate an appropriate macro for that extension
 		// based on whether the file is generated or not.  We do not want to create rules for
 		// generated files due to the current way the makefile is structured.
 		for(int k = 0; k < toolArray.length; k++) {
 			List extensionsList = toolArray[k].getInputExtensions();
 			
 			// iterate over all extensions that the tool knows how to handle
 			Iterator exListIterator = extensionsList.iterator();
 			while(exListIterator.hasNext()) {
 				String extensionName = exListIterator.next().toString();
 				
 				// If we are a regular file we get added to the list of object dependencies
 				// if we have not already created a rule for this filetype.  It is assumed that
 				// if multiple tools can handle the same input file extension that they will
 				// all map the input extension to the same output extension.  This is not explicitly
 				// checked however.
 				
 				// Generated files should not appear in the list.
 				if(!outputExtensionsSet.contains(extensionName) && !handledInputExtensionsSet.contains(extensionName)) {
 					handledInputExtensionsSet.add(extensionName);
 					StringBuffer macroName = getMacroName(extensionName);
 					
 					// create dependency rule of the form
 					// OBJS = $(macroName1: $(ROOT)/%.input1=%.output1) ... $(macroNameN: $(ROOT)/%.inputN=%.outputN)
 					objectsBuffer.append(WHITESPACE + "$(" + macroName + COLON + "$(ROOT)" + SEPARATOR + WILDCARD	//$NON-NLS-1$ //$NON-NLS-2$
 							+ DOT + extensionName + "=" + WILDCARD + DOT +	//$NON-NLS-1$
 							toolArray[k].getOutputExtension(extensionName) + ")" );	//$NON-NLS-1$
 					
 					// And another for the deps makefiles
 					// DEPS = $(macroName1: $(ROOT)/%.input1=%.DEP_EXT) ... $(macroNameN: $(ROOT)/%.inputN=%.DEP_EXT)
 					depFilesBuffer.append(WHITESPACE + "$(" + macroName + COLON + "$(ROOT)" + SEPARATOR + WILDCARD	//$NON-NLS-1$ //$NON-NLS-2$
 							+ DOT + extensionName + "=" + WILDCARD + DOT +	//$NON-NLS-1$
 							DEP_EXT + ")" );	//$NON-NLS-1$
 					
 				}
 			}
 		}		
 		
 		macroBuffer.append(NEWLINE + NEWLINE + objectsBuffer);
 		macroBuffer.append(NEWLINE + NEWLINE + depFilesBuffer);
 
 		// For now, just save the buffer that was populated when the rules were created
		Util.save(macroBuffer, fileHandle);
	}

	/**
	 * @param fileHandle
	 * @throws CoreException
	 */
	protected void populateSourcesMakefile(IFile fileHandle) throws CoreException {
		// Add the comment
		StringBuffer buffer = addDefaultHeader();
		
		// Add the known macros
 		ITarget myTarget = info.getDefaultTarget();
 		ITool toolArray[] = myTarget.getTools();
 		Set outputExtensionsSet = getOutputExtentions();
 		HashSet handledInputExtensionsSet = new HashSet();
 		for(int k = 0; k < toolArray.length; k++) {
 			List extensionsList = toolArray[k].getInputExtensions();
 			Iterator exListIterator = extensionsList.iterator();
 			while(exListIterator.hasNext()) {
 				// create a macro of the form "EXTENSION_SRCS :="
 				String extensionName = exListIterator.next().toString();
 				if(!outputExtensionsSet.contains(extensionName) && !handledInputExtensionsSet.contains(extensionName)) {
 					handledInputExtensionsSet.add(extensionName);
 					StringBuffer macroName = getMacroName(extensionName);
 					buffer.append(macroName + WHITESPACE + ":=" + WHITESPACE + NEWLINE);	//$NON-NLS-1$
 				}
 			}
 		}
 		
		// Add a list of subdirectories
		buffer.append(NEWLINE + addSubdirectories());
		
		// Save the file
		Util.save(buffer, fileHandle);
	}

	/* (non-javadoc)
	 * Create the entire contents of the makefile.
	 * 
	 * @param fileHandle The file to place the contents in.
	 * @param rebuild FLag signalling that the user is doing a full rebuild
	 * @throws CoreException
	 */
	protected void populateTopMakefile(IFile fileHandle, boolean rebuild) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		
		// Add the header
		buffer.append(addTopHeader());
		
		// Add the macro definitions
		buffer.append(addMacros());

		// Add targets
		buffer.append(addTargets(rebuild));

		// Save the file
		Util.save(buffer, fileHandle);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateDependencies()
	 */
	public void regenerateDependencies(boolean force) throws CoreException {
		// A hack for the pre-3.x GCC compilers is to put dummy targets for deps
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();

		Iterator iter = getDependencyMakefiles().listIterator();
		while (iter.hasNext()) {
			// The path to search for the dependency makefile
			IPath relDepFilePath = topBuildDir.append(new Path((String)iter.next()));
			IFile depFile = root.getFile(relDepFilePath);
			if (depFile == null || !depFile.exists()) continue;
			try {
				populateDummyTargets(depFile, true);
			} catch (CoreException e) {
				throw e;
			} catch (IOException e) {
				// This looks like a problem reading or writing the file
				ManagedBuilderCorePlugin.log(e);
				continue;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator#regenerateMakefiles()
	 */
	public void regenerateMakefiles() throws CoreException {
		// Visit the resources in the project
		ResourceProxyVisitor visitor = new ResourceProxyVisitor(this, info);
		project.accept(visitor, IResource.NONE);
		
		// See if the user has cancelled the build
		checkCancel();

		// Populate the makefile if any source files have been found in the project
		if (getSubdirList().isEmpty()) {
			return;
		}

		// Create the top-level directory for the build output
		topBuildDir = createDirectory(info.getConfigurationName());
		checkCancel();
		
		// Get the list of subdirectories
		IPath srcsFilePath = topBuildDir.addTrailingSeparator().append(SRCSFILE_NAME);
		IFile srcsFileHandle = createFile(srcsFilePath);
		populateSourcesMakefile(srcsFileHandle);
		checkCancel();
		
		// Now populate the module makefiles
		Iterator iter = getSubdirList().listIterator();
		while (iter.hasNext()) {
			IContainer subDir = (IContainer)iter.next();
			try {
				populateFragmentMakefile(subDir);
			} catch (CoreException e) {
				// Probably should ask user if they want to continue
				checkCancel();
				continue;
			}
			checkCancel();
		}

		// Create the top-level makefile
		IPath makefilePath = topBuildDir.addTrailingSeparator().append(MAKEFILE_NAME);
		IFile makefileHandle = createFile(makefilePath);
		populateTopMakefile(makefileHandle, true);
		checkCancel();
		
		// Now finish up by adding all the object files
		IPath objFilePath = topBuildDir.addTrailingSeparator().append(OBJECTS_MAKFILE);
		IFile objsFileHandle = createFile(objFilePath);
		populateObjectsMakefile(objsFileHandle);
		checkCancel();
	}

}
