/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
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

	ToolInfo(IResourceInfo rcInfo, ITool tool, int flag) {
		fRcInfo = rcInfo;

		updateInitialTool(tool);

		fFlag = flag;
	}

	private static ITool calculateBaseTool(IResourceInfo rcInfo, ITool tool) {
		ITool baseTool = null;
		if (tool.isExtensionElement()) {
			IToolChain baseTc;
			if (rcInfo instanceof IFolderInfo) {
				baseTc = ((IFolderInfo) rcInfo).getToolChain();
			} else {
				IFolderInfo foInfo = ((ResourceConfiguration) rcInfo).getParentFolderInfo();
				baseTc = foInfo.getToolChain();
			}

			ITool realTool = ManagedBuildManager.getRealTool(tool);
			if (realTool == null) {
				baseTool = tool;
			} else {
				//				ITool[] tcTools = baseTc.getTools();
				//				baseTool = getBestMatchTool(realTool, tcTools);
				//
				//				if(baseTool == null){
				//					IToolChain extTc = ManagedBuildManager.getExtensionToolChain(baseTc);
				//					if(extTc != null){
				//						baseTool = getBestMatchTool(realTool, extTc.getTools());
				//					}
				//				}

				if (baseTool == null) {
					baseTool = tool;
				}
			}
		} else if (rcInfo != tool.getParentResourceInfo()) {
			baseTool = tool;
		}

		return baseTool;
	}

	public int getType() {
		return fFlag;
	}

	public ITool getRealTool() {
		if (fRealTool == null) {
			ITool baseTool = getBaseTool();
			fRealTool = ManagedBuildManager.getRealTool(baseTool);
			if (fRealTool == null)
				fRealTool = fBaseTool;
		}
		return fRealTool;
	}

	void updateInitialTool(ITool tool) {
		if (fInitialTool == tool)
			return;

		fResultingTool = null;
		fRealTool = null;

		fInitialTool = tool;

		fModificationStatus = null;

		fBaseTool = calculateBaseTool(fRcInfo, tool);
	}

	public ITool getBaseTool() {
		if (fBaseTool == null) {
			fBaseTool = ManagedBuildManager.getExtensionTool(fInitialTool);
			if (fBaseTool == null)
				fBaseTool = fInitialTool;
		}
		return fBaseTool;
	}

	public ITool getBaseExtensionTool() {
		ITool tool = getBaseTool();
		return ManagedBuildManager.getExtensionTool(tool);
	}

	public ITool getInitialTool() {
		return fInitialTool;
	}

	public IModificationStatus getModificationStatus() {
		if (fModificationStatus == null) {
			getResultingTool();
		}
		return fModificationStatus;
	}

	public ITool getResultingTool() {
		switch (fFlag) {
		case ADDED:
			if (fResultingTool == null || fResultingTool.getParentResourceInfo() != fRcInfo) {
				Tool result = null;
				ModificationStatus status = null;
				if (fConverterInfo != null) {
					IBuildObject resultBo = fConverterInfo.getConvertedFromObject();
					if (!(resultBo instanceof Tool)) {
						status = new ModificationStatus(ManagedMakeMessages.getString("ToolInfo.0")); //$NON-NLS-1$
					} else {
						result = (Tool) resultBo;
						status = ModificationStatus.OK;
					}
				}

				if (status != ModificationStatus.OK) {
					ITool baseTool = getBaseTool();

					if (fRcInfo instanceof IFolderInfo) {
						IFolderInfo foInfo = (IFolderInfo) fRcInfo;
						if (baseTool.isExtensionElement()) {
							result = new Tool((ToolChain) foInfo.getToolChain(), baseTool,
									ManagedBuildManager.calculateChildId(baseTool.getId(), null), baseTool.getName(),
									false);
						} else {
							ITool extTool = ManagedBuildManager.getExtensionTool(baseTool);
							result = new Tool(foInfo.getToolChain(), extTool,
									ManagedBuildManager.calculateChildId(extTool.getId(), null), baseTool.getName(),
									(Tool) baseTool);
						}
					} else {
						ResourceConfiguration fiInfo = (ResourceConfiguration) fRcInfo;
						if (baseTool.isExtensionElement()) {
							result = new Tool(fiInfo, baseTool,
									ManagedBuildManager.calculateChildId(baseTool.getId(), null), baseTool.getName(),
									false);
						} else {
							ITool extTool = ManagedBuildManager.getExtensionTool(baseTool);
							result = new Tool(fiInfo, extTool,
									ManagedBuildManager.calculateChildId(extTool.getId(), null), baseTool.getName(),
									(Tool) baseTool);
						}
					}

					if (status == null)
						status = ModificationStatus.OK;
				}

				result.updateParentResourceInfo(fRcInfo);
				fResultingTool = result;
				fModificationStatus = status;
			}
			return fResultingTool;
		case REMOVED:
			fModificationStatus = new ModificationStatus(ManagedMakeMessages.getString("ToolInfo.1")); //$NON-NLS-1$
			return null;
		case REMAINED:
		default:
			if (fResultingTool == null) {
				fModificationStatus = ModificationStatus.OK;
				fResultingTool = fInitialTool;
			}
			return fResultingTool;
		}
	}

	void setConversionInfo(ToolInfo corInfo, ConverterInfo converterInfo) {
		fCorInfo = corInfo;
		fConverterInfo = converterInfo;
	}
}