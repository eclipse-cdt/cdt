/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine.processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Exclude Resources from a CDT project. This takes three arguments
 * <ul>
 *   <li> <b>projectName</b> the name of the project to exclude resources for. Must be a CDT Managed project.
 *   <li> <b>configIdPattern</b> a regular expression in java.util.regex.Pattern syntax for matching against the project configuration ids. The resources that
 *   match any of the regular expressions given in the <i>filePatterns</i> argument will be excluded for all matching project configurations.
 *   <li> <b>filePatterns</b> an array of regular expressions in java.util.regex.Pattern syntax for matching against project resources. The paths that
 *   will be matched against are workspace relative (include the project folder) and use forward slash as the file separator. That this argument is an
 *   array is purely to allow logically separate patterns to be given separately )rather than as one big string). If any of the regular expressions matches
 *   then the resource in question will be excluded for the matching configuration(s).
 *   <li> <b>invertConfigMatching</b> if this is set to "true" then the set of configurations for which resources matching any of the specified file patterns will
 *   be inverted. This enables you to specify which resources the files should not be excluded for without having to know what other configurations may exist.
 * </ul>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ExcludeResources extends ProcessRunner {
	
	@Override
	public void process(TemplateCore template, final ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		String configIdPattern = args[1].getSimpleValue();
		final String[] filePatterns = args[2].getSimpleArrayValue();
		String invertConfigMatching = args[3].getSimpleValue();
		
		boolean invert = Boolean.valueOf(invertConfigMatching).booleanValue();
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		if(info==null) {
			throw new ProcessFailureException(Messages.getString("ExcludeResources.0")); //$NON-NLS-1$
		}
		IManagedProject managedProject = info.getManagedProject();
		
		/*
		 * Determine which configurations to exclude for
		 */
		IConfiguration[] allConfigs = managedProject.getConfigurations();
		List/*<IConfiguration>*/ matchingConfigs = new ArrayList/*<IConfiguration>*/();
		for(int i=0; i<allConfigs.length; i++) {
			IConfiguration config = allConfigs[i];
			if(config.getId().matches(configIdPattern)) {
				matchingConfigs.add(config);
			}
		}
		
		if(invert) {
			List invertedConfigs = new ArrayList(Arrays.asList(allConfigs));
			invertedConfigs.removeAll(matchingConfigs);
			matchingConfigs = invertedConfigs;
		}
		
		/*
		 * Visit project resources and exclude them if they match any pattern
		 */
		final List configsToProcess = matchingConfigs;
		IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				IPath lPath = proxy.requestFullPath();
				
				if(proxy.getType() == IResource.FILE) { /* CDT does not support directory resource configurations */
					boolean isDerived = false;
					for(IResource res = proxy.requestResource(); res!=null; res = res.getParent()) {
						isDerived |= res.isDerived();
					}
					
					if(!isDerived) {					
						for(Iterator i = configsToProcess.iterator(); i.hasNext(); ) {
							IConfiguration config = (IConfiguration) i.next();
							
							
							IResourceConfiguration resourceConfig = config.getResourceConfiguration(lPath.toString());
							// Only add a resource configuration if the file pattern matches something that
							//is actually supposed to be excluded. Adding a resrouce configuration for all files
							// regardless of wheter they need it or not mess up the makefile generation.
							for(int j=0; j<filePatterns.length; j++) {
								String filePattern = filePatterns[j];
								if(lPath.toString().matches(filePattern)) {
									if(resourceConfig==null) {
										IFile file = (IFile) proxy.requestResource();
											resourceConfig = config.createResourceConfiguration(file);
									}
									
									if (resourceConfig != null){
										resourceConfig.setExclude(true);
									}
									
									break;
								
								}
							}
							
						}
					}
				}
				return true;
			}
		};
		
		try {
			project.accept(visitor, IResource.DEPTH_INFINITE);
			if (info.isDirty()){
				ManagedBuildManager.saveBuildInfo(project, true);											
			}
		} catch (CoreException ce) {
			throw new ProcessFailureException(ce);
		}
	}
}
