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
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;

public final class InfoContext{
	private static final int NULL_OBJ_CODE = 29;
	private IConfiguration fCfg;
	private IResourceInfo fRcInfo;
	private ITool fTool;
	private IInputType fInType;

	public InfoContext(IResourceInfo rcInfo, ITool tool, IInputType inType){
		this.fRcInfo = rcInfo;
		this.fTool = tool;
		this.fInType = inType;
		this.fCfg = fRcInfo.getParent();
	}

	public InfoContext(IConfiguration cfg){
		this.fCfg = cfg;
	}
	
	public IConfiguration getConfiguration(){
		return fCfg;
	}
	
	public IResourceInfo getResourceInfo(){
		return fRcInfo;
	}
	
	public ITool getTool(){
		return fTool;
	}
	
	public IInputType getInputType(){
		return fInType;
	}

	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(!(obj instanceof InfoContext))
			return false;
		
		InfoContext other = (InfoContext)obj;
		if(!checkBuildObjects(other.fCfg, fCfg))
			return false;

		if(!checkBuildObjects(other.fRcInfo, fRcInfo))
			return false;

		if(!checkBuildObjects(other.fTool, fTool))
			return false;

		if(!checkBuildObjects(other.fInType, fInType))
			return false;

		return true;
	}

	public int hashCode() {
		int code = getCode(fCfg);
		code += getCode(fRcInfo);
		code += getCode(fTool);
		code += getCode(fInType);
		return code;
	}
	
	private boolean checkBuildObjects(IBuildObject bo1, IBuildObject bo2){
		if(bo1 == null)
			return bo2 == null;
		if(bo2 == null)
			return false;
		return bo1.getId().equals(bo2.getId());
	}
	
	private int getCode(IBuildObject bo){
		if(bo == null)
			return NULL_OBJ_CODE;
		return bo.getId().hashCode();
	}
}
