/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class CDTPrefUtil {
	// boolean keys (KEY_NO-s are to be inverted !)
	public static final String KEY_NOSUPP  = "wizard.show.unsupported.disable";  //$NON-NLS-1$
	public static final String KEY_OTHERS  = "wizard.group.others.enable";  //$NON-NLS-1$
	public static final String KEY_NOMNG   = "properties.manage.config.disable"; //$NON-NLS-1$
	public static final String KEY_MULTI   = "properties.multi.config.enable"; //$NON-NLS-1$
	public static final String KEY_DTREE   = "properties.data.hierarchy.enable"; //$NON-NLS-1$
	public static final String KEY_NOTOOLM   = "properties.toolchain.modification.disable"; //$NON-NLS-1$
	public static final String KEY_EXPORT   = "properties.export.page.enable"; //$NON-NLS-1$
	// string keys
	public static final String KEY_PREFTC  = "wizard.preferred.toolchains";  //$NON-NLS-1$
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
	
	public static final String NULL = "NULL"; //$NON-NLS-1$
	private static final IPreferenceStore pref = CUIPlugin.getDefault().getPreferenceStore();
	private static final String DELIMITER = " "; //$NON-NLS-1$
	private static LinkedList preferredTCs = null;
	
	// low-level methods
	public static boolean getBool(String key) { return pref.getBoolean(key); }
	public static void setBool(String key, boolean val) { pref.setValue(key, val); }
	public static int getInt(String key) { return pref.getInt(key); }
	public static void setInt(String key, int val) { pref.setValue(key, val); }
	public static String getStr(String key) { return pref.getString(key); }
	public static void setStr(String key, String val) {	pref.setValue(key, val); }

	// up-level methods
	public static void readPreferredTCs() {
		preferredTCs = new LinkedList(Arrays.asList(getStr(KEY_PREFTC).split(DELIMITER)));
	}
	public static List getPreferredTCs() {
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
		Iterator it = preferredTCs.iterator();
		StringBuffer b = new StringBuffer(); 
		while (it.hasNext()) {
			String s = (String)it.next();
			if (s == null) continue; 
			b.append(s);
			b.append(DELIMITER);
		}
		setStr(KEY_PREFTC, b.toString().trim());
	}
}
