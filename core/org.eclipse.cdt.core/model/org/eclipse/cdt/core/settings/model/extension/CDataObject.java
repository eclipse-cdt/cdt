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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICSettingBase;

public abstract class CDataObject implements ICSettingBase{
	CDataObject(){
	}
	
	public abstract String getId();

	public abstract String getName();

	public abstract boolean isValid();
/*
	public CConfigurationData getConfiguration(){
		return fConfiguration;
	}
	
	void setConfiguration(CConfigurationData cfg){
		fConfiguration = cfg;
	}
*/
	public abstract int getType();
}
