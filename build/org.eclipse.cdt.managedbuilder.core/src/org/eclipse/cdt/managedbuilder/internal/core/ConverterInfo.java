/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IConfigurationElement;

public class ConverterInfo {
	private IBuildObject fFromObject;
	private IBuildObject fConvertedFromObject;
	private IBuildObject fToObject;
	private boolean fIsConversionPerformed;
	private IResourceInfo fRcInfo;

	public ConverterInfo(IResourceInfo rcInfo, IBuildObject fromObject, IBuildObject toObject,
			IConfigurationElement el) {
		fFromObject = fromObject;
		fToObject = toObject;
		fRcInfo = rcInfo;
	}

	public IBuildObject getFromObject() {
		return fFromObject;
	}

	public IBuildObject getToObject() {
		return fToObject;
	}

	public IBuildObject getConvertedFromObject() {
		if (!fIsConversionPerformed) {
			ManagedProject mProj = getManagedProject();
			IConfiguration[] cfgs = mProj.getConfigurations();
			fConvertedFromObject = ManagedBuildManager.convert(fFromObject, fToObject.getId(), true);
			IConfiguration[] updatedCfgs = mProj.getConfigurations();
			Set<IConfiguration> oldSet = new HashSet<>(Arrays.asList(cfgs));
			Set<IConfiguration> updatedSet = new HashSet<>(Arrays.asList(updatedCfgs));
			Set<IConfiguration> oldSetCopy = new HashSet<>(oldSet);
			oldSet.removeAll(updatedSet);
			updatedSet.removeAll(oldSetCopy);
			if (updatedSet.size() != 0)
				for (IConfiguration cfg : updatedSet)
					mProj.removeConfiguration(cfg.getId());
			if (oldSet.size() != 0)
				for (IConfiguration cfg : oldSet)
					mProj.applyConfiguration((Configuration) cfg);
			fIsConversionPerformed = true;
		}
		return fConvertedFromObject;
	}

	private ManagedProject getManagedProject() {
		if (fRcInfo != null)
			return (ManagedProject) fRcInfo.getParent().getManagedProject();
		return null;
	}
}
