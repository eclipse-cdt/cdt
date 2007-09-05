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
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

class ToolInfo {
	private ITool fResultingTool;
	private ITool fInitialTool;
	private ITool fBaseTool;
	private ITool fRealTool;
	private IResourceInfo fRcInfo;
	private int fFlag;
	private ToolInfo fCorInfo;
	private ConverterInfo fConverterInfo;
	private IModificationStatus fModificationStatus;

	public static final int ADDED = 1;
	public static final int REMOVED = 1 << 1;
	public static final int REMAINED = 1 << 2;

	ToolInfo(IResourceInfo rcInfo, ITool tool, int flag){
		fRcInfo = rcInfo;
		fInitialTool = tool;
		
		if(tool.isExtensionElement())
			fBaseTool = tool;
		else if (rcInfo != tool.getParentResourceInfo())
			fBaseTool = tool;
		
		fFlag = flag;
	}
	
	public int getType(){
		return fFlag;
	}
	
	public ITool getRealTool(){
		if(fRealTool == null){
			ITool baseTool = getBaseTool();
			fRealTool = ManagedBuildManager.getRealTool(baseTool);
			if(fRealTool == null)
				fRealTool = fBaseTool;
		}
		return fRealTool;
	}
	
	public ITool getBaseTool(){
		if(fBaseTool == null){
			fBaseTool = ManagedBuildManager.getExtensionTool(fInitialTool);
			if(fBaseTool == null)
				fBaseTool = fInitialTool;
		}
		return fBaseTool;
	}
	
	public ITool getBaseExtensionTool(){
		ITool tool = getBaseTool();
		return ManagedBuildManager.getExtensionTool(tool);
	}
	
	public ITool getInitialTool(){
		return fInitialTool;
	}
	
	public IModificationStatus getModificationStatus(){
		if(fModificationStatus == null){
			getResultingTool();
		}
		return fModificationStatus;
	}

	public ITool getResultingTool() {
		switch(fFlag){
		case ADDED:
			if(fResultingTool == null || fResultingTool.getParentResourceInfo() != fRcInfo){
				Tool result = null;
				ModificationStatus status = null;
				if(fConverterInfo != null){
					IBuildObject resultBo = fConverterInfo.getConvertedFromObject();
					if(!(resultBo instanceof Tool)) {
						status = new ModificationStatus("conversion failure");
					} else {
						result = (Tool)resultBo;
						status = ModificationStatus.OK;
					}
				} 
				
				if(status != ModificationStatus.OK){
					ITool baseTool = getBaseTool();

					if(fRcInfo instanceof IFolderInfo){
						IFolderInfo foInfo = (IFolderInfo)fRcInfo;
						if(baseTool.isExtensionElement()){
							result = new Tool((ToolChain)foInfo.getToolChain(), baseTool, ManagedBuildManager.calculateChildId(baseTool.getId(), null), baseTool.getName(), false);
						} else {
							ITool extTool = ManagedBuildManager.getExtensionTool(baseTool);
							result = new Tool(foInfo.getToolChain(), extTool, ManagedBuildManager.calculateChildId(extTool.getId(), null), baseTool.getName(), (Tool)baseTool);
						}
					} else {
						ResourceConfiguration fiInfo = (ResourceConfiguration)fRcInfo;
						if(baseTool.isExtensionElement()){
							result = new Tool(fiInfo, baseTool, ManagedBuildManager.calculateChildId(baseTool.getId(), null), baseTool.getName(), false);
						} else {
							ITool extTool = ManagedBuildManager.getExtensionTool(baseTool);
							result = new Tool(fiInfo, extTool, ManagedBuildManager.calculateChildId(extTool.getId(), null), baseTool.getName(), (Tool)baseTool);
						}
					}
					
					if(status == null)
						status = ModificationStatus.OK;
				}

				result.updateParentResourceInfo(fRcInfo);
				fResultingTool = result;
				fModificationStatus = status;
			}
			return fResultingTool;
		case REMOVED:
			fModificationStatus = new ModificationStatus("the tool is removed");
			return null;
		case REMAINED:
		default:
			fModificationStatus = ModificationStatus.OK;
			return fResultingTool; 
		}
	}
	
	void setConversionInfo(ToolInfo corInfo, ConverterInfo converterInfo){
		fCorInfo = corInfo;
		fConverterInfo = converterInfo;
	}
}