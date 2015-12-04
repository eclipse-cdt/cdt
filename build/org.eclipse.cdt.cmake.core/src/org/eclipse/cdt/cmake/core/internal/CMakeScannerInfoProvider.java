/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.io.IOException;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class CMakeScannerInfoProvider implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		try {
			IProject project = resource.getProject();
			IBuildConfiguration config = project.getActiveBuildConfig();
			CMakeBuildConfiguration cmakeConfig = config.getAdapter(CMakeBuildConfiguration.class);
			if (cmakeConfig != null) {
				return cmakeConfig.getScannerInfo(resource);
			}
		} catch (CoreException | IOException e) {
			Activator.log(e);
		}
		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
	}

}
