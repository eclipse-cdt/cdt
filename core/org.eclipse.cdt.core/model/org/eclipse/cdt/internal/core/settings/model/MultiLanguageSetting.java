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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.MultiItemsHolder;

/**
 * This class is intended to hold "similar" Language Setting objects. 
 * Normally, they should have the same name.
 */
public class MultiLanguageSetting extends MultiItemsHolder implements ICLanguageSetting {
	private static final Comparator<Object> comp = CDTListComparator.getInstance();

	ICLanguageSetting[] items = null;
	ICConfigurationDescription cfgd = null;
	
	public MultiLanguageSetting(List<ICLanguageSetting> data, ICConfigurationDescription cf) {
		items = (ICLanguageSetting[])data.toArray(new ICLanguageSetting[data.size()]);
		cfgd = cf;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getLanguageId()
	 */
	public String getLanguageId() {
		System.out.println("Bad multi access: MultiLanguageSetting.getLanguageId()");
		return null; // IDs are different.
	}

	private ICLanguageSettingEntry[] conv2LSE(Object[] ob) {
		ICLanguageSettingEntry[] se = new ICLanguageSettingEntry[ob.length];
		System.arraycopy(ob, 0, se, 0, ob.length);
		return se;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getResolvedSettingEntries(int)
	 */
	public ICLanguageSettingEntry[] getResolvedSettingEntries(int kind) {
		ICLanguageSettingEntry[][] le = new ICLanguageSettingEntry[items.length][];
		for (int i=0; i<items.length; i++)
			le[i] = items[i].getResolvedSettingEntries(kind);
		return conv2LSE(getListForDisplay(le, comp));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSettingEntries(int)
	 */
	public ICLanguageSettingEntry[] getSettingEntries(int kind) {
		ICLanguageSettingEntry[][] le = new ICLanguageSettingEntry[items.length][];
		for (int i=0; i<items.length; i++)
			le[i] = items[i].getSettingEntries(kind);
		return conv2LSE(getListForDisplay(le, comp));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSettingEntriesList(int)
	 */
	public List<ICLanguageSettingEntry> getSettingEntriesList(int kind) {
		return Arrays.asList(getSettingEntries(kind));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSourceContentTypeIds()
	 */
	public String[] getSourceContentTypeIds() {
	    String[][] ss = new String[items.length][];
		for (int i=0; i<items.length; i++)
			ss[i] = items[i].getSourceContentTypeIds();
		return this.getStrListForDisplay(ss);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSourceExtensions()
	 */
	public String[] getSourceExtensions() {
	    String[][] ss = new String[items.length][];
		for (int i=0; i<items.length; i++)
			ss[i] = items[i].getSourceExtensions();
		return this.getStrListForDisplay(ss);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSupportedEntryKinds()
	 */
	public int getSupportedEntryKinds() {
		int res = 0;
		for (int i=0; i<items.length; i++)
			res |= items[i].getSupportedEntryKinds();
		return res;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setLanguageId(java.lang.String)
	 */
	public void setLanguageId(String id) { // Do nothing
		System.out.println("Bad multi access: MultiLanguageSetting.setLanguageId()");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSettingEntries(int, org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry[])
	 */
	public void setSettingEntries(int kind, ICLanguageSettingEntry[] entries) {
		for (int i=0; i<items.length; i++)
			items[i].setSettingEntries(kind, entries);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSettingEntries(int, java.util.List)
	 */
	public void setSettingEntries(int kind, List<ICLanguageSettingEntry> entriesList) {
		for (int i=0; i<items.length; i++)
			items[i].setSettingEntries(kind, entriesList);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSourceContentTypeIds(java.lang.String[])
	 */
	public void setSourceContentTypeIds(String[] ids) {
		for (int i=0; i<items.length; i++)
			items[i].setSourceContentTypeIds(ids);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSourceExtensions(java.lang.String[])
	 */
	public void setSourceExtensions(String[] exts) {
		for (int i=0; i<items.length; i++)
			items[i].setSourceExtensions(exts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#supportsEntryKind(int)
	 */
	public boolean supportsEntryKind(int kind) {
		for (int i=0; i<items.length; i++)
			if (items[i].supportsEntryKind(kind))
					return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getConfiguration()
	 */
	public ICConfigurationDescription getConfiguration() {
		System.out.println("Bad multi access: MultiLanguageSetting.getConfiguration()");
		return null; // CFGs are different
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getId()
	 */
	public String getId() { // IDs are different
		System.out.println("Bad multi access: MultiLanguageSetting.getId()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getName()
	 */
	public String getName() { // names are proposed to be equal
		return items[0].getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getParent()
	 */
	public ICSettingContainer getParent() {
		System.out.println("Bad multi access: MultiLanguageSetting.getParent()");
		return null; // Parents are different
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getType()
	 */
	public int getType() {
		int x = items[0].getType();
		for (int i=0; i<items.length; i++)
			if (x != items[i].getType())
					return 0;
		return x;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#isReadOnly()
	 */
	public boolean isReadOnly() {
		for (int i=0; i<items.length; i++)
			if (! items[i].isReadOnly())
					return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#isValid()
	 */
	public boolean isValid() {
		for (int i=0; i<items.length; i++)
			if (! items[i].isValid())
					return false;
		return true;
	}

	public Object[] getItems() {
		return items;
	}

}
