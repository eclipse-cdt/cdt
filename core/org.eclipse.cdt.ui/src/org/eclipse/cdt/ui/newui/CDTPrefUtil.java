/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     QNX Software Systems - [272416] Rework the working set configurations
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.workingsets.WorkingSetConfigurationManager;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CDTPrefUtil {
	// boolean keys (KEY_NO-s are to be inverted !)
	public static final String KEY_NOSUPP  = "wizard.show.unsupported.disable";  //$NON-NLS-1$
	public static final String KEY_OTHERS  = "wizard.group.others.enable";  //$NON-NLS-1$
	public static final String KEY_NOMNG   = "properties.manage.config.disable"; //$NON-NLS-1$
	public static final String KEY_DTREE   = "properties.data.hierarchy.enable"; //$NON-NLS-1$
	public static final String KEY_NOTOOLM   = "properties.toolchain.modification.disable"; //$NON-NLS-1$
	public static final String KEY_EXPORT   = "properties.export.page.enable"; //$NON-NLS-1$
	/** @since 5.2 Show the "Include Files" settings entry tab */
	public static final String KEY_SHOW_INC_FILES = "properties.includefiles.page.enable"; //$NON-NLS-1$
	/** @since 5.2 */
	public static final String KEY_TIPBOX   = "properties.option.tipbox.enable"; //$NON-NLS-1$
	// string keys
	public static final String KEY_PREFTC  = "wizard.preferred.toolchains";  //$NON-NLS-1$
	public static final String KEY_CONFSET = "workingsets.selected.configs";  //$NON-NLS-1$
	// integer keys
	public static final String KEY_POSSAVE  = "properties.save.position"; //$NON-NLS-1$
		public static final int POSITION_SAVE_SIZE = 0;
	    public static final int POSITION_SAVE_NONE = 2;
	    public static final int POSITION_SAVE_BOTH = 3;
	
	public static final String KEY_DISC_NAMES  = "properties.discovery.naming"; //$NON-NLS-1$
		public static final int DISC_NAMING_UNIQUE_OR_BOTH = 0;
		public static final int DISC_NAMING_UNIQUE_OR_IDS = 1;
		public static final int DISC_NAMING_ALWAYS_BOTH = 2;
		public static final int DISC_NAMING_ALWAYS_IDS = 3;
		public static final int DISC_NAMING_DEFAULT = DISC_NAMING_UNIQUE_OR_BOTH;
	
	public static final String KEY_DMODE = "properties.multi.displ.mode"; //$NON-NLS-1$
		public static final int DMODE_CONJUNCTION = 1;
		public static final int DMODE_DISJUNCTION = 2;
		
	public static final String KEY_WMODE = "properties.multi.write.mode"; //$NON-NLS-1$
		public static final int WMODE_MODIFY  = 4;
		public static final int WMODE_REPLACE = 8;
		
	public static final String NULL = "NULL"; //$NON-NLS-1$
	private static final IPreferenceStore pref = CUIPlugin.getDefault().getPreferenceStore();
	private static final String DELIMITER = " "; //$NON-NLS-1$
	public static final String CONFSETDEL = "\f"; //$NON-NLS-1$
	private static LinkedList<String> preferredTCs = null;
	
	public static final Object[] EMPTY_ARRAY = new Object[0];

	// low-level methods
	public static boolean getBool(String key) { return pref.getBoolean(key); }
	public static void setBool(String key, boolean val) { pref.setValue(key, val); }
	public static int getInt(String key) { return pref.getInt(key); }
	public static void setInt(String key, int val) { pref.setValue(key, val); }
	public static String getStr(String key) { return pref.getString(key); }
	public static void setStr(String key, String val) {	pref.setValue(key, val); }

	// up-level methods
	public static void readPreferredTCs() {
		preferredTCs = new LinkedList<String>(Arrays.asList(getStr(KEY_PREFTC).split(DELIMITER)));
	}
	public static List<String> getPreferredTCs() {
		if (preferredTCs == null) readPreferredTCs(); 
		return preferredTCs; 
	}
	public static void delPreferredTC(String s) { 
		if (preferredTCs == null) readPreferredTCs(); 
		preferredTCs.remove(s); 
	}
	public static void addPreferredTC(String s) {
		if (preferredTCs == null) readPreferredTCs(); 
		if (!preferredTCs.contains(s)) preferredTCs.add(s); 
	}
	public static void cleanPreferredTCs() {
		setStr(KEY_PREFTC, IPreferenceStore.STRING_DEFAULT_DEFAULT);
		readPreferredTCs(); 
	}
	public static void savePreferredTCs() {
		if (preferredTCs == null) return; 
		Iterator<String> it = preferredTCs.iterator();
		StringBuilder b = new StringBuilder(); 
		while (it.hasNext()) {
			String s = it.next();
			if (s == null) continue; 
			b.append(s);
			b.append(DELIMITER);
		}
		setStr(KEY_PREFTC, b.toString().trim());
	}
	
	@SuppressWarnings("fallthrough")
	public static String getDMode() {
		String s = null;
		switch(getInt(KEY_DMODE)) {
		default:
			setInt(KEY_DMODE, DMODE_CONJUNCTION);
			// fallthrough
		case DMODE_CONJUNCTION:
			s = UIMessages.getString("EnvironmentTab.17");  //$NON-NLS-1$
			break;
		case DMODE_DISJUNCTION:
			s = UIMessages.getString("EnvironmentTab.18");  //$NON-NLS-1$
			break;
		}
		return UIMessages.getString("EnvironmentTab.19") + s;  //$NON-NLS-1$
	}
	
	@SuppressWarnings("fallthrough")
	public static String getWMode() {
		String s = null;
		switch(getInt(KEY_WMODE)) {
		default:
			setInt(KEY_WMODE, WMODE_MODIFY);
			// fallthrough
		case WMODE_MODIFY:
			s = UIMessages.getString("EnvironmentTab.24");  //$NON-NLS-1$
			break;
		case WMODE_REPLACE:
			s = UIMessages.getString("EnvironmentTab.21");  //$NON-NLS-1$
			break;
		}
		return UIMessages.getString("EnvironmentTab.22") + s;  //$NON-NLS-1$
	}
	
	public static void spinDMode() {
		setInt(KEY_DMODE, 
				((getInt(KEY_DMODE) == DMODE_CONJUNCTION) ?
						DMODE_DISJUNCTION :
						DMODE_CONJUNCTION));
	}

	public static void spinWMode() {
		setInt(KEY_WMODE, 
				((getInt(KEY_WMODE) == WMODE_MODIFY) ? 
						WMODE_REPLACE : 
						WMODE_MODIFY));
	}

	public static final String[] getStrListForDisplay(String[][] input) {
		return getStrListForDisplay(input, getInt(KEY_DMODE));
	}
	
	private static final String[] getStrListForDisplay(String[][] input, int mode) {
		Object[] ob = getListForDisplay(input, getInt(KEY_DMODE), null);
		String[] ss = new String[ob.length];
		System.arraycopy(ob, 0, ss, 0, ob.length);
		return ss;
	}
	
	public static final Object[] getListForDisplay(Object[][] input, Comparator<Object> cmp) {
		return getListForDisplay(input, getInt(KEY_DMODE), cmp);
	}
	/**
	 * Utility method forms string list
	 * according to current list display mode
	 * 
	 * @param input - array of string arrays
	 * @return
	 */
	private static final Object[] getListForDisplay(Object[][] input, int mode, Comparator<Object> cmp) {
		if (input == null || input.length == 0)
			return EMPTY_ARRAY;
		if (input.length == 1) {
			return (input[0] == null) ?
					EMPTY_ARRAY :
					input[0];
		}

		Object[] s1 = input[0];
		if (s1 == null || 
			s1.length == 0)
			return EMPTY_ARRAY;
		if (getInt(KEY_DMODE) == DMODE_CONJUNCTION) 
		{ 
			ArrayList<Object> lst = new ArrayList<Object>();
			for (int i=0; i<s1.length; i++) {
				if (s1[i] == null)
					continue;
				boolean found = true;
				for (int k = 1; k<input.length; k++) {
					Object[] s2 = input[k];
					if (s2 == null || s2.length == 0)
						return EMPTY_ARRAY;
					if (i == 0)
						Arrays.sort(s2, cmp);
					if (Arrays.binarySearch(s2, s1[i], cmp) < 0) {
						found = false;
						break;
					}
				}
				if (found) {
					lst.add(s1[i]);
				}
			}
			return lst.toArray();
		}
		TreeSet<Object> lst = new TreeSet<Object>(cmp); // set, to avoid doubles
		for (Object[] element : input) {
			if (element == null ||
				element.length == 0)
				continue;
			for (Object element2 : element)
				lst.add(element2);
		}
		s1 = lst.toArray();
		Arrays.sort(s1, cmp);
		return s1;
	}
	
	/**
	 * @deprecated Use the {@link WorkingSetConfigurationManager} class, instead.
	 */
	@Deprecated
	public static List<String> readConfigSets() {
		return new LinkedList<String>(Arrays.asList(getStr(KEY_CONFSET).split(CONFSETDEL)));
	}
	
	/**
	 * @deprecated Use the {@link WorkingSetConfigurationManager} class, instead.
	 */
	@Deprecated
	public static void saveConfigSets(List<String> out) {
		StringBuilder b = new StringBuilder(); 
		for (String s : out) {
			if (s == null) continue; 
			b.append(s);
			b.append(CONFSETDEL);
		}
		setStr(KEY_CONFSET, b.toString());
	}
}
