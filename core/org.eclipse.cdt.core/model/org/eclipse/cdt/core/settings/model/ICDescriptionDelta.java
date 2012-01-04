/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICDescriptionDelta {
	/*
	 * delta kinds
	 */
	/**
	 * kind specifying that the setting object returned by
	 * the {@link #getSetting()} was removed
	 * the {@link #getNewSetting()} returns null
	 * the {@link #getOldSetting()} returns the same object as {@link #getSetting()}
	 */
	int REMOVED = 1;

	/**
	 * kind specifying that the setting object returned by
	 * the {@link #getSetting()} was added
	 * the {@link #getNewSetting()} returns the same object as {@link #getSetting()}
	 * the {@link #getOldSetting()} returns null
	 */
	int ADDED = 2;

	/**
	 * kind specifying that the setting object was changed
	 * the {@link #getNewSetting()} returns new object
	 * the {@link #getOldSetting()} returns old object
	 * the {@link #getSetting()} returns the same object as {@link #getNewSetting()}
	 */
	int CHANGED = 3;

	/*
	 * delta change flags
	 */

	int ACTIVE_CFG = 1;
	int NAME = 1 << 1;
	int DESCRIPTION = 1 << 2;
//	int PATH = 1 << 3;
	int LANGUAGE_ID = 1 << 4;
	int SOURCE_CONTENT_TYPE = 1 << 5;
	/** @since 5.4 */
	int SOURCE_EXTENSIONS = 1 << 6;
	/** @deprecated Use ICDescriptionDelta.SOURCE_EXTENSIONS */
	@Deprecated
	int SOURCE_ENTENSIONS = SOURCE_EXTENSIONS;
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
	int SETTING_CFG = INDEX_CFG;
	/** @since 5.4 */
	int LANGUAGE_SETTINGS_PROVIDERS = 1 << 22;

	/**
	 * specifies that the project "isCdtProjectCreating" state was set to false
	 * the PROJECT_CREAION_COMPLETED delta gets notified ONLY in case
	 * the project previously contained the project description with
	 * the true "isCdtProjectCreating" state
	 *
	 * in case the initial project description does NOT contain the true "isCdtProjectCreating"
	 * the project is considered as initialized from the very beginning
	 * and the PROJECT_CREAION_COMPLETED delta is NOT notified
	 *
	 * @see ICProjectDescription#isCdtProjectCreating()
	 * @see ICProjectDescription#setCdtProjectCreated()
	 * @see ICProjectDescriptionManager#createProjectDescription(org.eclipse.core.resources.IProject, boolean, boolean)
	 */
	int PROJECT_CREAION_COMPLETED = 1 << 22;

	/**
	 * returns the kind
	 * @see #ADDED
	 * @see #REMOVED
	 * @see #CHANGED
	 *
	 * @return int
	 */
	int getDeltaKind();

	/**
	 * @return ored delta flags
	 */
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
