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

public abstract class CFolderData extends CResourceData {

	protected CFolderData() {
		
	}

	public final int getType(){
		return ICSettingBase.SETTING_FOLDER;
	}
	
//	public abstract CResourceData[] getNestedResourceDatas(int kind);
	
//	public abstract CResourceData getNestedResourceData(IPath path);
	
//	public abstract CDataObject[] getChildrenOfKind(int kind);

//	public abstract CDataObject getChildById(String id);

	public abstract CLanguageData[] getLanguageDatas();
	
	public abstract CLanguageData createLanguageDataForContentTypes(String languageId, String cTypesIds[]);

	public abstract CLanguageData createLanguageDataForExtensions(String languageId, String extensions[]);
}
