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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICSettingObject;

public class CProjectDescriptionDelta implements ICDescriptionDelta {
	private List fChildList = new ArrayList();
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

	public ICDescriptionDelta[] getChildren() {
		return (CProjectDescriptionDelta[])fChildList.toArray(new CProjectDescriptionDelta[fChildList.size()]);
	}

	public ICSettingObject getOldSetting() {
		return fOldSetting;
	}

	public ICSettingObject getSetting() {
		return fSetting;
	}

	public int getSettingType() {
		return fSetting.getType();
	}

	public ICDescriptionDelta getParent() {
		return fParent;
	}
	
	public boolean isEmpty(){
		return fChildList.size() == 0 
			&& getDeltaKind() == CHANGED 
			&& getChangeFlags() == 0;
	}

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

	public int getDeltaKind() {
		return fStatus & KIND_MASK;
	}

	public ICSettingObject getNewSetting() {
		return fNewSetting;
	}

	void setAddedLanguageEntriesKinds(int kinds){
		fAddedLanguageEntriesKinds = kinds;
		checkSettingEntriesChangeFlag();
	}

	public int getAddedEntriesKinds() {
		return fAddedLanguageEntriesKinds;
	}

	public int getRemovedEntriesKinds() {
		return fRemovedLanguageEntriesKinds;
	}

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
}
