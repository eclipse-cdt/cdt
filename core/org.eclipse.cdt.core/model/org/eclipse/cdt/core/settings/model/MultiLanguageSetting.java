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

import java.util.Arrays;
import java.util.List;


/**
 * This class is intended to hold "similar" Language Setting objects. 
 * Normally, they should have the same name.
 */
public class MultiLanguageSetting extends MultiItemsHolder implements ICLanguageSetting {
	ICLanguageSetting[] items = null;
	ICConfigurationDescription cfgd = null;
	
	public MultiLanguageSetting(List<ICLanguageSetting> data, ICConfigurationDescription cf) {
		items = data.toArray(new ICLanguageSetting[data.size()]);
		cfgd = cf;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getLanguageId()
	 */
	public String getLanguageId() {
		return null; // IDs are different.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSettingEntries(int)
	 */
	public ICLanguageSettingEntry[][] getSettingEntriesM(int kind) {
		ICLanguageSettingEntry[][] le = new ICLanguageSettingEntry[items.length][];
		for (int i=0; i<items.length; i++)
			le[i] = items[i].getSettingEntries(kind);
		return le;
//		return conv2LSE(getListForDisplay(le, comp));
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
	public String[][] getSourceContentTypeIdsM() {
	    String[][] ss = new String[items.length][];
		for (int i=0; i<items.length; i++)
			ss[i] = items[i].getSourceContentTypeIds();
		return ss;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSourceExtensions()
	 */
	public String[][] getSourceExtensionsM() {
	    String[][] ss = new String[items.length][];
		for (int i=0; i<items.length; i++)
			ss[i] = items[i].getSourceExtensions();
		return ss;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#getSupportedEntryKinds()
	 */
	public int getSupportedEntryKinds() {
		int res = 0;
		for (ICLanguageSetting item : items)
			res |= item.getSupportedEntryKinds();
		return res;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setLanguageId(java.lang.String)
	 */
	public void setLanguageId(String id) {} // Do nothing

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSettingEntries(int, org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry[])
	 */
	public void setSettingEntries(int kind, ICLanguageSettingEntry[] entries) {
		for (ICLanguageSetting item : items)
			item.setSettingEntries(kind, entries);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSettingEntries(int, java.util.List)
	 */
	public void setSettingEntries(int kind, List<ICLanguageSettingEntry> entriesList) {
		for (ICLanguageSetting item : items)
			item.setSettingEntries(kind, entriesList);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSourceContentTypeIds(java.lang.String[])
	 */
	public void setSourceContentTypeIds(String[] ids) {
		for (ICLanguageSetting item : items)
			item.setSourceContentTypeIds(ids);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#setSourceExtensions(java.lang.String[])
	 */
	public void setSourceExtensions(String[] exts) {
		for (ICLanguageSetting item : items)
			item.setSourceExtensions(exts);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICLanguageSetting#supportsEntryKind(int)
	 */
	public boolean supportsEntryKind(int kind) {
		for (ICLanguageSetting item : items)
			if (item.supportsEntryKind(kind))
					return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getConfiguration()
	 */
	public ICConfigurationDescription getConfiguration() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiLanguageSetting.getConfiguration()"); //$NON-NLS-1$
		return null; // CFGs are different
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getId()
	 */
	public String getId() { // IDs are different
		if (DEBUG)
			System.out.println("Bad multi access: MultiLanguageSetting.getId()"); //$NON-NLS-1$
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
		if (DEBUG)
			System.out.println("Bad multi access: MultiLanguageSetting.getParent()"); //$NON-NLS-1$
		return null; // Parents are different
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getType()
	 */
	public int getType() {
		int x = items[0].getType();
		for (ICLanguageSetting item : items)
			if (x != item.getType())
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

	@Override
	public Object[] getItems() {
		return items;
	}

	public ICLanguageSettingEntry[] getResolvedSettingEntries(int kind) {
		return null;
	}

	public ICLanguageSettingEntry[] getSettingEntries(int kind) {
		ICLanguageSettingEntry[][] ses = getSettingEntriesM(kind);
		return ses[0];
	}

	public String[] getSourceContentTypeIds() {
		return null;
	}

	public String[] getSourceExtensions() {
		return null;
	}

}
