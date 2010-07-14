/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine.processes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildDescriptionGnuMakefileGenerator;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class GenerateMakefileWithBuildDescription extends ProcessRunner{
	/**
	 * 
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projectHandle);
		if(info == null)
			throw new ProcessFailureException(ManagedMakeMessages.getString("GenerateMakefileWithBuildDescription.0")); //$NON-NLS-1$
		
		IConfiguration cfg = info.getDefaultConfiguration();
		if(cfg == null)
			throw new ProcessFailureException(ManagedMakeMessages.getString("GenerateMakefileWithBuildDescription.1")); //$NON-NLS-1$

		IBuildDescription des;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, 0);
			IFile file = projectHandle.getFile("makefile"); //$NON-NLS-1$
			ByteArrayOutputStream oStream = new ByteArrayOutputStream(100);
			BuildDescriptionGnuMakefileGenerator gen = new BuildDescriptionGnuMakefileGenerator(des);
			gen.store(oStream);
			byte[] bytes = oStream.toByteArray();
			ByteArrayInputStream iStream = new ByteArrayInputStream(bytes);
			
			if(!file.exists()){
				file.create(iStream, true, monitor);
			} else {
				file.setContents(iStream, true, false, monitor);
			}
		} catch (CoreException e1) {
			throw new ProcessFailureException(e1);
		}
	}
}
