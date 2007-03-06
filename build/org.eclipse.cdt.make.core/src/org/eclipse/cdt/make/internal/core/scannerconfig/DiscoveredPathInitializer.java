/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class DiscoveredPathInitializer extends PathEntryContainerInitializer {

	public void initialize(IPath containerPath, ICProject cProject) throws CoreException {
        IProject project = cProject.getProject();
        IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(project);
        ScannerConfigScope profileScope = ScannerConfigProfileManager.getInstance().
                getSCProfileConfiguration(buildInfo.getSelectedProfileId()).getProfileScope();
        if (ScannerConfigScope.PROJECT_SCOPE.equals(profileScope)) {
            CoreModel.setPathEntryContainer(new ICProject[]{cProject}, new DiscoveredPathContainer(project), null);
        }
        else if (ScannerConfigScope.FILE_SCOPE.equals(profileScope)) {
            CoreModel.setPathEntryContainer(new ICProject[]{cProject}, new PerFileDiscoveredPathContainer(project), null);
        }
        else {
            throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.getUniqueIdentifier(), 1,
                    MakeMessages.getString("DiscoveredContainer.ScopeErrorMessage"), null));  //$NON-NLS-1$
        }
	}

}
