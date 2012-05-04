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

import org.eclipse.cdt.internal.ui.newui.Messages;
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
	/** @since 5.4 Show the "Scanner Discovery" tab*/
	public static final String KEY_SHOW_SD   = "properties.sd.page.enable"; //$NON-NLS-1$
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

	/** Property key used for string list display mode for multi-configuration edits (conjunction/disjunction) */
	public static final String KEY_DMODE = "properties.multi.displ.mode"; //$NON-NLS-1$
	/** Conjunction implies showing only common elements (intersection) */
	public static final int DMODE_CONJUNCTION = 1;
	/** Disjunction implies showing all elements (union) */
	public static final int DMODE_DISJUNCTION = 2;

	/** Property key used for string list write mode for multi-configuration edits (modify/replace) */
	public static final String KEY_WMODE = "properties.multi.write.mode"; //$NON-NLS-1$
	/** Modify implies changing only given elements and not changing any others */
	public static final int WMODE_MODIFY  = 4;
	/** Replace implies replacing the whole list with the given one, overwriting old entries */
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

	/**
	 * Returns string list display mode for multi-configuration edits (conjunction/disjunction).
	 *
	 * @return the mode which can be either {@link CDTPrefUtil#DMODE_CONJUNCTION} (default value)
	 *    or else {@link CDTPrefUtil#DMODE_DISJUNCTION}.
	 *
	 * @since 5.3
	 */
	public static int getMultiCfgStringListDisplayMode() {
		int mode = getInt(KEY_DMODE);
		if (mode!=DMODE_CONJUNCTION && mode!=DMODE_DISJUNCTION) {
			mode = DMODE_CONJUNCTION;
		}
		return mode;
	}

	/**
	 * Sets string list display mode for multi-configuration edits (conjunction/disjunction).
	 *
	 * @param mode must be either {@link CDTPrefUtil#DMODE_CONJUNCTION}
	 *    or {@link CDTPrefUtil#DMODE_DISJUNCTION}.
	 *
	 * @since 5.3
	 */
	public static void setMultiCfgStringListDisplayMode(int mode) {
		setInt(KEY_DMODE, mode);
	}

	/**
	 * Returns string list write mode for multi-configuration edits (modify/replace).
	 *
	 * @return the mode which can be either {@link CDTPrefUtil#WMODE_MODIFY} (default value)
	 *    or else {@link CDTPrefUtil#WMODE_REPLACE}.
	 *
	 * @since 5.3
	 */
	public static int getMultiCfgStringListWriteMode() {
		int mode = getInt(KEY_WMODE);
		if (mode!=WMODE_MODIFY && mode!=WMODE_REPLACE) {
			mode = WMODE_MODIFY;
		}
		return mode;
	}

	/**
	 * Sets string list write mode for multi-configuration edits (modify/replace).
	 *
	 * @param mode must be either {@link CDTPrefUtil#WMODE_MODIFY}
	 *    or {@link CDTPrefUtil#WMODE_REPLACE}.
	 *
	 * @since 5.3
	 */
	public static void setMultiCfgStringListWriteMode(int mode) {
		setInt(KEY_WMODE, mode);
	}

	/**
	 * @deprecated as of CDT 8.0. Use {@link StringListModeControl} to display string list modes.
	 */
	@Deprecated
	public static String getDMode() {
		String s = null;
		switch(getMultiCfgStringListDisplayMode()) {
		case DMODE_CONJUNCTION:
			s = Messages.EnvironmentTab_17;
			break;
		case DMODE_DISJUNCTION:
			s = Messages.EnvironmentTab_18;
			break;
		}
		return Messages.EnvironmentTab_19 + s;
	}

	/**
	 * @deprecated as of CDT 8.0. Use {@link StringListModeControl} to display string list modes.
	 */
	@Deprecated
	public static String getWMode() {
		String s = null;
		switch(getMultiCfgStringListWriteMode()) {
		case WMODE_MODIFY:
			s = Messages.EnvironmentTab_24;
			break;
		case WMODE_REPLACE:
			s = Messages.EnvironmentTab_21;
			break;
		}
		return Messages.EnvironmentTab_22 + s;
	}

	/**
	 * Toggle string list display mode: conjunction <-> disjunction.
	 */
	public static void spinDMode() {
		int mode = getMultiCfgStringListDisplayMode();
		if (mode==DMODE_CONJUNCTION) {
			mode = DMODE_DISJUNCTION;
		} else {
			mode = DMODE_CONJUNCTION;
		}
		setMultiCfgStringListDisplayMode(mode);
	}

	/**
	 * Toggle string list display mode: modify <-> replace.
	 */
	public static void spinWMode() {
		int mode = getMultiCfgStringListWriteMode();
		if (mode==WMODE_MODIFY) {
			mode = WMODE_REPLACE;
		} else {
			mode = WMODE_MODIFY;
		}
		setMultiCfgStringListWriteMode(mode);
	}

	public static final String[] getStrListForDisplay(String[][] input) {
		return getStrListForDisplay(input, getMultiCfgStringListDisplayMode());
	}

	private static final String[] getStrListForDisplay(String[][] input, int mode) {
		Object[] ob = getListForDisplay(input, getMultiCfgStringListDisplayMode(), null);
		String[] ss = new String[ob.length];
		System.arraycopy(ob, 0, ss, 0, ob.length);
		return ss;
	}

	public static final Object[] getListForDisplay(Object[][] input, Comparator<Object> cmp) {
		return getListForDisplay(input, getMultiCfgStringListDisplayMode(), cmp);
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
			return (input[0] == null) ? EMPTY_ARRAY : input[0];
		}

		Object[] s1 = input[0];
		if (s1 == null || s1.length == 0)
			return EMPTY_ARRAY;

		if (getMultiCfgStringListDisplayMode() == DMODE_CONJUNCTION) {
			ArrayList<Object> lst = new ArrayList<Object>();
			for (int i=0; i<s1.length; i++) {
				if (s1[i] == null)
					continue;
				boolean found = true;
				for (int k = 1; k<input.length; k++) {
					Object[] s2 = input[k];
					if (s2 == null || s2.length == 0) {
						return EMPTY_ARRAY;
					}
					if (i == 0) {
						Arrays.sort(s2, cmp);
					}
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
			if (element == null || element.length == 0) {
				continue;
			}
			for (Object element2 : element) {
				lst.add(element2);
			}
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
