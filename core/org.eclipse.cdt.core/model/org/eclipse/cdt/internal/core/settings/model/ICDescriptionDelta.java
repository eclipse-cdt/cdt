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

import org.eclipse.cdt.core.settings.model.ICSettingObject;

public interface ICDescriptionDelta {
	int REMOVED = 1;
	int ADDED = 2;
	int CHANGED = 3;
	
	int ACTIVE_CFG = 1;
	int NAME = 1 << 1;
	int DESCRIPTION = 1 << 2;
//	int PATH = 1 << 3;
	int LANGUAGE_ID = 1 << 4;
	int SOURCE_CONTENT_TYPE = 1 << 5;
	int SOURCE_ENTENSIONS = 1 << 6;
//	int HEADER_CONTENT_TYPE = 1 << 7;
//	int HEADER_ENTENSIONS = 1 << 8;
	int SETTING_ENTRIES = 1 << 9;
	int BINARY_PARSER_IDS = 1 << 10;
	int ERROR_PARSER_IDS = 1 << 11;
	int EXCLUDE = 1 << 12;
	int SOURCE_ADDED = 1 << 13;
	int SOURCE_REMOVED = 1 << 14;
	int EXTERNAL_SETTINGS_ADDED = 1 << 15;
	int EXTERNAL_SETTINGS_REMOVED = 1 << 16;
	int CFG_REF_ADDED = 1 << 17;
	int CFG_REF_REMOVED = 1 << 18;
	int EXT_REF = 1 << 19;
	int OWNER = 1 << 20;
	int INDEX_CFG = 1 << 21;


	int getDeltaKind();
	
	int getChangeFlags();
	
	int getSettingType();

	int getAddedEntriesKinds();

	int getRemovedEntriesKinds();

	int getReorderedEntriesKinds();

	ICDescriptionDelta[] getChildren();

	ICSettingObject getNewSetting();

	ICSettingObject getSetting();
	
	ICSettingObject getOldSetting();
	
	ICDescriptionDelta getParent();
}
