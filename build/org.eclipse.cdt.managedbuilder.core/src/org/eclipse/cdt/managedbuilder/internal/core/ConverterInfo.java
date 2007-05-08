/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IConfigurationElement;

public class ConverterInfo {
	private IBuildObject fFromObject;
	private IBuildObject fConvertedFromObject;
	private IBuildObject fToObject;
	private IConfigurationElement fConverterElement;
	private boolean fIsConversionPerformed;
	private IResourceInfo fRcInfo;
//	private IManagedProject fMProj;
	
	public ConverterInfo(IResourceInfo rcInfo, IBuildObject fromObject, IBuildObject toObject, IConfigurationElement el){
		fFromObject = fromObject;
		fToObject = toObject;
		fConverterElement = el;
		fRcInfo = rcInfo;
	}
	
	public IBuildObject getFromObject(){
		return fFromObject;
	}

	public IBuildObject getToObject(){
		return fToObject;
	}
	
	public IConfigurationElement getConverterElement(){
		return fConverterElement;
	}
	
	public IBuildObject getConvertedFromObject(){
		if(!fIsConversionPerformed){
			ManagedProject mProj = getManagedProject();
			IConfiguration[] cfgs = mProj.getConfigurations();
			fConvertedFromObject = ManagedBuildManager.convert(fFromObject, fToObject.getId(), true);
			IConfiguration[] updatedCfgs = mProj.getConfigurations();
			Set oldSet = new HashSet(Arrays.asList(cfgs));
			Set updatedSet = new HashSet(Arrays.asList(updatedCfgs));
			Set oldSetCopy = new HashSet(oldSet);
			oldSet.removeAll(updatedSet);
			updatedSet.removeAll(oldSetCopy);
			if(updatedSet.size() != 0){
				for(Iterator iter = updatedSet.iterator(); iter.hasNext();){
					Configuration cfg = (Configuration)iter.next();
					mProj.removeConfiguration(cfg.getId());
				}
			}
			if(oldSet.size() != 0){
				for(Iterator iter = oldSet.iterator(); iter.hasNext();){
					mProj.applyConfiguration((Configuration)iter.next());
				}
			}
			fIsConversionPerformed = true;
		}
		return fConvertedFromObject;
	}
	
	private ManagedProject getManagedProject(){
		if(fRcInfo != null)
			return (ManagedProject)fRcInfo.getParent().getManagedProject();
		return null;
	}
}
