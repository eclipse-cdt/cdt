/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.makegen.gnu;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @since 2.0
 */
public class DefaultGCCDependencyCalculator implements IManagedDependencyGenerator {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		return TYPE_COMMAND;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		/*
		 * For a given input, <path>/<resource_name>.<ext>, return a string containing
		 * 	echo -n $(@:%.<out_ext>=%.d) '<path>/' >> $(@:%.<out_ext>=%.d) && \
		 * 	<tool_command> -P -MM -MG <tool_flags> $< >> $(@:%.<out_ext>=%.d)
		 * 
		 */
		StringBuffer buffer = new StringBuffer();
		
		// Get what we need to create the dependency generation command
		String inputExtension = resource.getFileExtension();
		String cmd = info.getToolForSource(inputExtension);
		String outputExtension = info.getOutputExtension(inputExtension);
		String buildFlags = info.getFlagsForSource(inputExtension);
		
		// Work out the build-relative path
		IContainer resourceLocation = resource.getParent();
		String relativePath = new String();
		if (resourceLocation != null) {
			relativePath += resourceLocation.getProjectRelativePath().toString();
		}
		if (relativePath.length() > 0) {
			relativePath +=  IManagedBuilderMakefileGenerator.SEPARATOR;
		}
		
		// Calculate the dependency rule
		// <path>/$(@:%.<out_ext>=%.d)
		String depRule = "$(@:%." + //$NON-NLS-1$
			outputExtension + 
			"=%." + //$NON-NLS-1$
			IManagedBuilderMakefileGenerator.DEP_EXT + 
			")"; //$NON-NLS-1$
		
		// Add the rule that will actually create the right format for the dep 
		buffer.append(IManagedBuilderMakefileGenerator.TAB + 
				IManagedBuilderMakefileGenerator.ECHO + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				"-n" + //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE +
				depRule + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.SINGLE_QUOTE + 
				relativePath + 
				IManagedBuilderMakefileGenerator.SINGLE_QUOTE + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				">" + //$NON-NLS-1$ 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				depRule + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.LOGICAL_AND + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.LINEBREAK);
		
		// Add the line that will do the work
		buffer.append(IManagedBuilderMakefileGenerator.TAB + 
				cmd + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				"-MM -MG -P -w" +  //$NON-NLS-1$ 
				IManagedBuilderMakefileGenerator.WHITESPACE +
				buildFlags + 
				IManagedBuilderMakefileGenerator.WHITESPACE +  
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				IManagedBuilderMakefileGenerator.IN_MACRO + 
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				">>" +  //$NON-NLS-1$
				IManagedBuilderMakefileGenerator.WHITESPACE + 
				depRule);
		
		return buffer.toString();
	}

}
