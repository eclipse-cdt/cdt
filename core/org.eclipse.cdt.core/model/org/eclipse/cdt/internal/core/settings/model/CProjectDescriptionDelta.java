/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICSettingObject;

public class CProjectDescriptionDelta implements ICDescriptionDelta {
	private List<CProjectDescriptionDelta> fChildList = new ArrayList<CProjectDescriptionDelta>();
	private CProjectDescriptionDelta fParent;
	private ICSettingObject fSetting;
	private ICSettingObject fOldSetting;
	private ICSettingObject fNewSetting;
	private int fStatus;
	private int fAddedLanguageEntriesKinds;
	private int fRemovedLanguageEntriesKinds;
	private int fReorderedLanguageEntriesKinds;

	private static final int KIND_MASK = 3;
	private static final int FLAGS_OFFSET = 2;

	public CProjectDescriptionDelta(ICSettingObject newSetting, ICSettingObject oldSetting){
		fNewSetting = newSetting;
		fOldSetting = oldSetting;
		if(newSetting != null){
			fSetting = newSetting;
			if(oldSetting != null)
				setDeltaKind(CHANGED);
			else
				setDeltaKind(ADDED);
		} else {
			fSetting = oldSetting;
			setDeltaKind(REMOVED);
		}
	}

	void addChild(CProjectDescriptionDelta child){
		fChildList.add(child);
		child.setParent(this);
	}

	private void setParent(CProjectDescriptionDelta parent){
		fParent = parent;
	}

	@Override
	public ICDescriptionDelta[] getChildren() {
		return fChildList.toArray(new CProjectDescriptionDelta[fChildList.size()]);
	}

	@Override
	public ICSettingObject getOldSetting() {
		return fOldSetting;
	}

	@Override
	public ICSettingObject getSetting() {
		return fSetting;
	}

	@Override
	public int getSettingType() {
		return fSetting.getType();
	}

	@Override
	public ICDescriptionDelta getParent() {
		return fParent;
	}

	public boolean isEmpty(){
		return fChildList.size() == 0
			&& getDeltaKind() == CHANGED
			&& getChangeFlags() == 0;
	}

	@Override
	public int getChangeFlags() {
		return (fStatus & (~KIND_MASK)) >> FLAGS_OFFSET;
	}

	void addChangeFlags(int flags){
		flags |= getChangeFlags();
		setChangeFlags(flags);
	}

	void removeChangeFlags(int flags){
		flags = (getChangeFlags() & (~flags));
		setChangeFlags(flags);
	}

	void setChangeFlags(int flags){
		fStatus = (fStatus & KIND_MASK) | (flags << FLAGS_OFFSET);
	}

	void setDeltaKind(int kind){
		fStatus = (fStatus & (~KIND_MASK)) | (kind & KIND_MASK);
	}

	@Override
	public int getDeltaKind() {
		return fStatus & KIND_MASK;
	}

	@Override
	public ICSettingObject getNewSetting() {
		return fNewSetting;
	}

	void setAddedLanguageEntriesKinds(int kinds){
		fAddedLanguageEntriesKinds = kinds;
		checkSettingEntriesChangeFlag();
	}

	@Override
	public int getAddedEntriesKinds() {
		return fAddedLanguageEntriesKinds;
	}

	@Override
	public int getRemovedEntriesKinds() {
		return fRemovedLanguageEntriesKinds;
	}

	@Override
	public int getReorderedEntriesKinds() {
		return fReorderedLanguageEntriesKinds;
	}

	void setRemovedLanguageEntriesKinds(int kinds){
		fRemovedLanguageEntriesKinds = kinds;
		checkSettingEntriesChangeFlag();
	}

	void setReorderedLanguageEntriesKinds(int kinds){
		fReorderedLanguageEntriesKinds = kinds;
		checkSettingEntriesChangeFlag();
	}

	private void checkSettingEntriesChangeFlag(){
		if(fAddedLanguageEntriesKinds != 0
				|| fRemovedLanguageEntriesKinds != 0
				|| fReorderedLanguageEntriesKinds != 0)
			addChangeFlags(SETTING_ENTRIES);
		else
			removeChangeFlags(SETTING_ENTRIES);
	}

	@SuppressWarnings("nls")
	private static String flagsToString(int flags) {
		StringBuilder str = new StringBuilder();
		str.append(", flags=0x" + Integer.toHexString(flags));

		str.append(":");
		if ((flags&ACTIVE_CFG)!=0) str.append("ACTIVE_CFG|");
		if ((flags&NAME)!=0) str.append("NAME|");
		if ((flags&DESCRIPTION)!=0) str.append("DESCRIPTION|");
		if ((flags&LANGUAGE_ID)!=0) str.append("LANGUAGE_ID|");
		if ((flags&SOURCE_CONTENT_TYPE)!=0) str.append("SOURCE_CONTENT_TYPE|");
		if ((flags&SOURCE_EXTENSIONS)!=0) str.append("SOURCE_EXTENSIONS|");
		if ((flags&SETTING_ENTRIES)!=0) str.append("SETTING_ENTRIES|");
		if ((flags&BINARY_PARSER_IDS)!=0) str.append("BINARY_PARSER_IDS|");
		if ((flags&ERROR_PARSER_IDS)!=0) str.append("ERROR_PARSER_IDS|");
		if ((flags&EXCLUDE)!=0) str.append("EXCLUDE|");
		if ((flags&SOURCE_ADDED)!=0) str.append("SOURCE_ADDED|");
		if ((flags&SOURCE_REMOVED)!=0) str.append("SOURCE_REMOVED|");
		if ((flags&EXTERNAL_SETTINGS_ADDED)!=0) str.append("EXTERNAL_SETTINGS_ADDED|");
		if ((flags&EXTERNAL_SETTINGS_REMOVED)!=0) str.append("EXTERNAL_SETTINGS_REMOVED|");
		if ((flags&CFG_REF_ADDED)!=0) str.append("CFG_REF_ADDED|");
		if ((flags&CFG_REF_REMOVED)!=0) str.append("CFG_REF_REMOVED|");
		if ((flags&EXT_REF)!=0) str.append("EXT_REF|");
		if ((flags&OWNER)!=0) str.append("OWNER|");
		if ((flags&INDEX_CFG)!=0) str.append("INDEX_CFG|");
		if ((flags&LANGUAGE_SETTINGS_PROVIDERS)!=0) str.append("LANGUAGE_SETTINGS_PROVIDERS|");

		if (str.charAt(str.length()-1)=='|') str.deleteCharAt(str.length()-1);
		return str.toString();
	}

	/**
	 * Helper method to make debugging easier.
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		String type = fSetting.getClass().getSimpleName();
		str.append("[" + type + "]");

		int kind = getDeltaKind();
		str.append(", kind="+kind);
		switch (kind) {
		case ADDED: str.append(":ADDED");break;
		case REMOVED: str.append(":REMOVED");break;
		case CHANGED: str.append(":CHANGED");break;
		default: str.append(":<unknown>");
		}

		str.append(flagsToString(getChangeFlags()));

		ICDescriptionDelta[] children = getChildren();
		if (children==null) {
			str.append(", no children");
		} else {
			str.append(", " + getChildren().length + " children");
		}

		return str.toString();
	}
}
