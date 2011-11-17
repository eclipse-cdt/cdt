/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Miwako Tokugawa (Intel Corporation) - bug 222817 (OptionCategoryApplicability)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

public class CheckBuildPropertyExpression implements IBooleanExpression {
	public static final String NAME = "checkBuildProperty"; 	//$NON-NLS-1$

	public static final String PROPERTY = "property"; 	//$NON-NLS-1$
	public static final String VALUE = "value"; 	//$NON-NLS-1$

	private String fPropertyId;
	private String fValueId;

	public CheckBuildPropertyExpression(IManagedConfigElement element){
		fPropertyId = element.getAttribute(PROPERTY);
		if(fPropertyId == null)
			fPropertyId = ""; //$NON-NLS-1$

		fValueId = element.getAttribute(VALUE);
//		if(fValueId == null)
//			fValueId = ""; //$NON-NLS-1$

//		fIsRegex = getBooleanValue(element.getAttribute(IS_REGEX));
	}


	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder,
			IOption option) {
		return evaluate(rcInfo);
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder,
			IOptionCategory category) {
		return evaluate(rcInfo);
	}

	private boolean evaluate(IResourceInfo rcInfo) {
		IConfiguration cfg = rcInfo.getParent();
		IBuildProperty prop = getBuildProperty(cfg, fPropertyId);
		if(prop != null){
			return fValueId != null ? fValueId.equals(prop.getValue().getId()) : true;
		}
		return false;
	}
/*
	public static IBuildProperty getBuildProperty(IHoldsOptions ho, String id){
		if(ho instanceof ITool)
			return getBuildProperty((ITool)ho, id);
		if(ho instanceof IToolChain)
			return getBuildProperty((IToolChain)ho, id);
		return null;
	}

	public static IBuildProperty getBuildProperty(ITool tool, String id){
		IBuildProperty prop = tool.getBuildProperties().getProperty(id);
		if(prop == null){
			IBuildObject parent = tool.getParent();
			if(parent instanceof IToolChain){
				prop = getBuildProperty((IToolChain)parent, id);
			} else if (parent instanceof IFileInfo) {
				prop = getBuildProperty((IFileInfo)parent, id);
			}
		}
		return prop;
	}

	public static IBuildProperty getBuildProperty(IFileInfo info, String id){
		return getBuildProperty(info.getParent(), id);
	}

	public static IBuildProperty getBuildProperty(IFolderInfo info, String id){
		return getBuildProperty(info.getParent(), id);
	}

	public static IBuildProperty getBuildProperty(IToolChain toolChain, String id){
		IBuildProperty prop = null;
		if(toolChain.getParentFolderInfo() == toolChain.getParent().getRootFolderInfo())
			prop = toolChain.getBuildProperties().getProperty(id);

		if(prop == null){
			prop = getBuildProperty(toolChain.getParentFolderInfo(), id);
		}
		return prop;
	}
*/
	public static IBuildProperty getBuildProperty(IConfiguration cfg, String id){
		IBuildProperty prop = cfg.getBuildProperties().getProperty(id);

//		if(prop == null)
//			prop = cfg.getManagedProject().getBuildProperties().getProperty(id);

		return prop;
	}

	public String getPropertyId(){
		return fPropertyId;
	}

	public String getValueId(){
		return fValueId;
	}
}
