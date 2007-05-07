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

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IConfigurationElement;

public class ConverterInfo {
	private IBuildObject fFromObject;
	private IBuildObject fConvertedFromObject;
	private IBuildObject fToObject;
	private IConfigurationElement fConverterElement;
	private boolean fIsConversionPerformed;
	
	public ConverterInfo(IBuildObject fromObject, IBuildObject toObject, IConfigurationElement el){
		fFromObject = fromObject;
		fToObject = toObject;
		fConverterElement = el;
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
			fConvertedFromObject = ManagedBuildManager.convert(fFromObject, fToObject.getId(), true);
			fIsConversionPerformed = true;
		}
		return fConvertedFromObject;
	}
}
