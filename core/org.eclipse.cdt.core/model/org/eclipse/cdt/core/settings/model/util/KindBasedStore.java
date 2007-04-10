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
package org.eclipse.cdt.core.settings.model.util;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class KindBasedStore implements Cloneable {
	private static final int INDEX_INCLUDE_PATH = 0;
	private static final int INDEX_INCLUDE_FILE = 1;
	private static final int INDEX_MACRO = 2;
	private static final int INDEX_MACRO_FILE = 3;
	private static final int INDEX_LIBRARY_PATH = 4;
	private static final int INDEX_LIBRARY_FILE = 5;
	private static final int LANG_STORAGE_SIZE = 6;

	private static final int INDEX_SOURCE_PATH = 6;
	private static final int INDEX_OUPUT_PATH = 7;
	private static final int ALL_STORAGE_SIZE = 8;
	
	public static final int ORED_LANG_ENTRY_KINDS = 
		ICLanguageSettingEntry.INCLUDE_PATH
		| ICLanguageSettingEntry.INCLUDE_FILE
		| ICLanguageSettingEntry.MACRO
		| ICLanguageSettingEntry.MACRO_FILE
		| ICLanguageSettingEntry.LIBRARY_PATH
		| ICLanguageSettingEntry.LIBRARY_FILE;


	private static final int LANG_ENTRY_KINDS[] = new int[]{
		ICLanguageSettingEntry.INCLUDE_PATH,
		ICLanguageSettingEntry.INCLUDE_FILE,
		ICLanguageSettingEntry.MACRO,
		ICLanguageSettingEntry.MACRO_FILE,
		ICLanguageSettingEntry.LIBRARY_PATH,
		ICLanguageSettingEntry.LIBRARY_FILE,
	};

//	private static final int INEXISTENT_INDEX = -1;
	
	private Object[] fEntryStorage;

	public KindBasedStore(){
		this(true);
	}

	public KindBasedStore(boolean langOnly){
		if(langOnly)
			fEntryStorage = new Object[LANG_STORAGE_SIZE];
		else
			fEntryStorage = new Object[ALL_STORAGE_SIZE];
	}

	private int kindToIndex(int kind){
		switch (kind){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			return INDEX_INCLUDE_PATH;
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return INDEX_INCLUDE_FILE;
		case ICLanguageSettingEntry.MACRO:
			return INDEX_MACRO;
		case ICLanguageSettingEntry.MACRO_FILE:
			return INDEX_MACRO_FILE;
		case ICLanguageSettingEntry.LIBRARY_PATH:
			return INDEX_LIBRARY_PATH;
		case ICLanguageSettingEntry.LIBRARY_FILE:
			return INDEX_LIBRARY_FILE;
		case ICSettingEntry.SOURCE_PATH:
			if(INDEX_SOURCE_PATH < fEntryStorage.length)
				return INDEX_SOURCE_PATH;
			break;
		case ICSettingEntry.OUTPUT_PATH:
			if(INDEX_OUPUT_PATH < fEntryStorage.length)
				return INDEX_OUPUT_PATH;
			break;
		}
		throw new IllegalArgumentException(UtilMessages.getString("KindBasedStore.0")); //$NON-NLS-1$
	}

	public static int[] getLanguageEntryKinds(){
		return (int[])LANG_ENTRY_KINDS.clone();
	}

	private int indexToKind(int index){
		switch (index){
		case INDEX_INCLUDE_PATH:
			return ICLanguageSettingEntry.INCLUDE_PATH;
		case INDEX_INCLUDE_FILE:
			return ICLanguageSettingEntry.INCLUDE_FILE;
		case INDEX_MACRO:
			return ICLanguageSettingEntry.MACRO;
		case INDEX_MACRO_FILE:
			return ICLanguageSettingEntry.MACRO_FILE;
		case INDEX_LIBRARY_PATH:
			return ICLanguageSettingEntry.LIBRARY_PATH;
		case INDEX_LIBRARY_FILE:
			return ICLanguageSettingEntry.LIBRARY_FILE;
		case INDEX_SOURCE_PATH:
			return ICSettingEntry.SOURCE_PATH;
		case INDEX_OUPUT_PATH:
			return ICSettingEntry.OUTPUT_PATH;
		}
		throw new IllegalArgumentException(UtilMessages.getString("KindBasedStore.1")); //$NON-NLS-1$
	}
	public Object get(int kind){
		return fEntryStorage[kindToIndex(kind)];
	}

	public Object put(int kind, Object object){
		int index = kindToIndex(kind);
		Object old = fEntryStorage[index];
		fEntryStorage[index] = object;
		return old;
	}
	
	private class KindBasedInfo implements IKindBasedInfo {
		int fIdex;
		int fKind;
		
		KindBasedInfo(int num, boolean isKind){
			if(isKind){
				fIdex = kindToIndex(num);
				fKind = num;
			} else {
				fIdex = num;
				fKind = indexToKind(num);
			}
		}
	
		public Object getInfo() {
			return fEntryStorage[fIdex];
		}

		public int getKind() {
			return fKind;
		}

		public Object setInfo(Object newInfo) {
			Object old = fEntryStorage[fIdex];
			fEntryStorage[fIdex] = newInfo;
			return old;
		}
		
	}
	
	public IKindBasedInfo[] getContents(){
		IKindBasedInfo infos[] = new IKindBasedInfo[fEntryStorage.length];
		for(int i = 0; i < fEntryStorage.length; i++){
			infos[i] = new KindBasedInfo(i, false);
		}
		return infos;
	}
	
	public IKindBasedInfo getInfo(int kind){
		return new KindBasedInfo(kind, true);
	}
	
	public void clear(){
		for(int i = 0; i < fEntryStorage.length; i++){
			fEntryStorage[i] = null;
		}
	}

	public Object clone() {
		try {
			KindBasedStore clone = (KindBasedStore)super.clone();
			clone.fEntryStorage = (Object[])fEntryStorage.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
	
	
}
