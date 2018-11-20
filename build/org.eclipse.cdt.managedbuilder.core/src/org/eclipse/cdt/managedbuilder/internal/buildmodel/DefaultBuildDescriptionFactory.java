/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescriptionFactory;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;

public class DefaultBuildDescriptionFactory implements IBuildDescriptionFactory {
	static private DefaultBuildDescriptionFactory fInstance;

	protected DefaultBuildDescriptionFactory() {

	}

	public static DefaultBuildDescriptionFactory getInstance() {
		if (fInstance == null)
			fInstance = new DefaultBuildDescriptionFactory();
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescriptionFactory#createBuildDescription(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.core.resources.IResourceDelta, int)
	 */
	@Override
	public IBuildDescription createBuildDescription(IConfiguration cfg, IResourceDelta delta, int flags)
			throws CoreException {
		return createBuildDescription(cfg, null, delta, flags);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescriptionFactory#createBuildDescription(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.core.resources.IResourceDelta, int)
	 */
	public IBuildDescription createBuildDescription(IConfiguration cfg, IConfigurationBuildState bs,
			IResourceDelta delta, int flags) throws CoreException {
		BuildDescription info = new BuildDescription();
		info.init(cfg, bs, delta, flags);
		return info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescriptionFactory#getSupportedMethods()
	 */
	@Override
	public int getSupportedMethods() {
		return BuildDescriptionManager.REMOVED | BuildDescriptionManager.REBUILD | BuildDescriptionManager.DEPFILES
				| BuildDescriptionManager.DEPS;
	}

}
