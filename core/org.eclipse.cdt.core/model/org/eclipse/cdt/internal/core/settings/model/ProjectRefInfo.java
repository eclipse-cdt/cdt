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
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICStorageElement;

class ProjectRefInfo {
	private final static String ATTRIBUTE_REF_PROJECT = "project";  //$NON-NLS-1$
	private final static String ATTRIBUTE_REF_CFG_ID = "configuration";  //$NON-NLS-1$
	final static String ELEMENT_REFERENCE = "reference";  //$NON-NLS-1$
	private String fProjectName;
	private String fCfgId;
	private CExternalSettingProvider fProvider;
	private boolean fIsSynchronized;
	
	ProjectRefInfo(ICStorageElement el){
		fProjectName = el.getAttribute(ATTRIBUTE_REF_PROJECT);
		fCfgId = el.getAttribute(ATTRIBUTE_REF_CFG_ID);
		if(fCfgId == null)
			fCfgId = "";  //$NON-NLS-1$
		
		ICStorageElement children[] = el.getChildren();
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			if(CExternalSettingProvider.ELEMENT_EXT_SETTINGS_CONTAINER.equals(name)){
				fProvider = new CExternalSettingProvider(child);
			}
		}
	}

	ProjectRefInfo(String projName, String cfgId){
		fProjectName = projName;
		fCfgId = cfgId;
	}

	ProjectRefInfo(ProjectRefInfo base){
		fProjectName = base.fProjectName;
		fCfgId = base.fCfgId;
		
		if(base.fProvider != null)
			fProvider = new CExternalSettingProvider(base.fProvider);
		
		fIsSynchronized = base.fIsSynchronized;
	}
	
	void serialize(ICStorageElement element){
		element.setAttribute(ATTRIBUTE_REF_PROJECT, fProjectName);
		if(fCfgId.length() != 0)
			element.setAttribute(ATTRIBUTE_REF_CFG_ID, fCfgId);

		if(fProvider != null){
			ICStorageElement child = element.createChild(CExternalSettingProvider.ELEMENT_EXT_SETTINGS_CONTAINER);
			fProvider.serialize(child);
		}
	}

	public String getProjectName() {
		return fProjectName;
	}

	public String getCfgId() {
		return fCfgId;
	}

	public CExternalSettingProvider getProvider() {
		if(fProvider == null)
			fProvider = new CExternalSettingProvider();
		return fProvider;
	}

	public void setProvider(CExternalSettingProvider provider) {
		fProvider = provider;
	}
	
	public boolean isSynchronized(){
		return fIsSynchronized;
	}
	
	public void setSynchronized(boolean s){
		fIsSynchronized = s;
	}

}
