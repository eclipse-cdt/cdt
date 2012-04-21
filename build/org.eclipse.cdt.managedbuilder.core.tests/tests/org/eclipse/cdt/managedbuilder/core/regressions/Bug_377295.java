/*******************************************************************************
 * Copyright (c) 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.regressions;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

public class Bug_377295 extends AbstractBuilderTest {

	// Tests that the URI used to get the time stamp of the artifact is escaped correctly
	// See AdditionalInput.getArtifactTimeStamp(IToolChain toolChain)
	public void testGetArtifactTimeStampEscapeURI() throws CoreException {
		setWorkspace("regressions");
		final IProject project = loadProject("helloworldC");
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration configuration : configs) {
			configuration.setArtifactName("test [377295]");
		}
		
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		// This will trigger AdditionalInput.getArtifactTimeStamp to get called, no IllegalArgumentException should be thrown
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	}
}
