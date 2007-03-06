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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class CDataUtil {
	private static Random randomNumber;
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static int genRandomNumber(){
		if (randomNumber == null) {
			// Set the random number seed
			randomNumber = new Random();
			randomNumber.setSeed(System.currentTimeMillis());
		}
		int i = randomNumber.nextInt();
		if (i < 0) {
			i *= -1;
		}
		return i;
	}
	
	public static String genId(String baseId){
		String suffix = new Integer(genRandomNumber()).toString();
		return baseId != null ? 
				new StringBuffer(baseId).append(".").append(suffix).toString() 	//$NON-NLS-1$
				: suffix;
	}
	
	public static boolean objectsEqual(Object o1, Object o2){
		if(o1 == null)
			return o2 == null;
		return o1.equals(o2);
	}
	
	public static String arrayToString(String[] array, String separator){
		if(array == null)
			return null;
		if(array.length == 0)
			return ""; //$NON-NLS-1$
		if(array.length == 1)
			return array[0];
		StringBuffer buf = new StringBuffer();
		buf.append(array[0]);
		for(int i = 1; i < array.length; i++){
			buf.append(separator).append(array[i]);
		}
		
		return buf.toString();
	}

	public static String[] stringToArray(String string, String separator){
		if(string == null)
			return null;
		if(string.length() == 0)
			return EMPTY_STRING_ARRAY;
		StringTokenizer t = new StringTokenizer(string, separator);
		List list = new ArrayList(t.countTokens());
		while (t.hasMoreElements()) {
			list.add(t.nextToken());
		}
		return (String[])list.toArray(new String[list.size()]);
	}
	
	public static ICLanguageSettingEntry[] resolveEntries(ICLanguageSettingEntry entries[], ICConfigurationDescription cfgDes){
		if(entries.length == 0)
			return entries;
		
		ICLanguageSettingEntry[] resolved = new ICLanguageSettingEntry[entries.length];
		ICdtVariableManager mngr = CCorePlugin.getDefault().getCdtVariableManager();

		for(int i = 0; i < entries.length; i++){
			ICLanguageSettingEntry entry = entries[i];
			resolved[i] = createResolvedEntry(entry, cfgDes, mngr);
		}
		
		return resolved;
	}
	
	private static ICLanguageSettingEntry createResolvedEntry(ICLanguageSettingEntry entry, ICConfigurationDescription cfg, ICdtVariableManager mngr){
		if(entry.isResolved())
			return entry;
		
		String name = entry.getName();
		try {
			name = mngr.resolveValue(name, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
		} catch (CdtVariableException e) {
			CCorePlugin.log(e);
		}
		
		switch (entry.getKind()) {
		case ICLanguageSettingEntry.INCLUDE_PATH:
			return new CIncludePathEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.INCLUDE_FILE:
			return new CIncludeFileEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.MACRO:
			String value = entry.getValue();
			try {
				value = mngr.resolveValue(value, "", " ", cfg);  //$NON-NLS-1$  //$NON-NLS-2$
			} catch (CdtVariableException e) {
				CCorePlugin.log(e);
			}
			return new CMacroEntry(name, value, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.MACRO_FILE:
			return new CMacroFileEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.LIBRARY_PATH:
			return new CLibraryPathEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		case ICLanguageSettingEntry.LIBRARY_FILE:
			return new CLibraryFileEntry(name, ICSettingEntry.RESOLVED | entry.getFlags());
		default:
			throw new IllegalArgumentException();
		}
	}

	public static ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, int flagsToAdd, int flafsToClear){
		return createEntry(entry, (entry.getFlags() | flagsToAdd) & (~flafsToClear));
	}

	public static ICLanguageSettingEntry createEntry(ICLanguageSettingEntry entry, int flags){
		switch (entry.getKind()){
		case ICLanguageSettingEntry.INCLUDE_PATH:
			entry = new CIncludePathEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.MACRO:
			entry = new CMacroEntry(entry.getName(), entry.getValue(), flags);
			break;
		case ICLanguageSettingEntry.INCLUDE_FILE:
			entry = new CIncludeFileEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.MACRO_FILE:
			entry = new CMacroFileEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.LIBRARY_PATH:
			entry = new CLibraryPathEntry(entry.getName(), flags);
			break;
		case ICLanguageSettingEntry.LIBRARY_FILE:
			entry = new CLibraryFileEntry(entry.getName(), flags);
			break;
		}
		return entry;
	}
}
