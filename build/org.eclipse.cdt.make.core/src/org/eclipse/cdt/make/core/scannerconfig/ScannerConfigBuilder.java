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
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.scannerconfig.*;
import org.eclipse.core.resources.IProject;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Runs after standard make builder.
 * Consolidates discovered scanner configuration and updates project's scanner configuration.
 * 
 * @see IncrementalProjectBuilder
 */
public class ScannerConfigBuilder extends ACBuilder {
	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".ScannerConfigBuilder"; //$NON-NLS-1$

	public ScannerConfigBuilder() {
		super();
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	protected IProject [] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("", 100); //$NON-NLS-1$
		monitor.subTask(MakeCorePlugin.getResourceString("ScannerConfigBuilder.Invoking_Builder") +	//$NON-NLS-1$ 
				getProject().getName());
		ScannerInfoCollector.getInstance().updateScannerConfiguration(getProject(), new SubProgressMonitor(monitor, 100));
		return getProject().getReferencedProjects();
	}
}
