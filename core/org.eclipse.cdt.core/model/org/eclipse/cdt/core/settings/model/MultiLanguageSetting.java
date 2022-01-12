/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public String getLanguageId() {
		return items[0].getLanguageId(); // Assuming language is the same.
	}

	public ICLanguageSettingEntry[][] getSettingEntriesM(int kind) {
		ICLanguageSettingEntry[][] le = new ICLanguageSettingEntry[items.length][];
		for (int i = 0; i < items.length; i++)
			le[i] = items[i].getSettingEntries(kind);
		return le;
		//		return conv2LSE(getListForDisplay(le, comp));
	}

	@Override
	public List<ICLanguageSettingEntry> getSettingEntriesList(int kind) {
		return Arrays.asList(getSettingEntries(kind));
	}

	public String[][] getSourceContentTypeIdsM() {
		String[][] ss = new String[items.length][];
		for (int i = 0; i < items.length; i++)
			ss[i] = items[i].getSourceContentTypeIds();
		return ss;
	}

	public String[][] getSourceExtensionsM() {
		String[][] ss = new String[items.length][];
		for (int i = 0; i < items.length; i++)
			ss[i] = items[i].getSourceExtensions();
		return ss;
	}

	@Override
	public int getSupportedEntryKinds() {
		int res = 0;
		for (ICLanguageSetting item : items)
			res |= item.getSupportedEntryKinds();
		return res;
	}

	@Override
	public void setLanguageId(String id) {
	} // Do nothing

	@Override
	public void setSettingEntries(int kind, ICLanguageSettingEntry[] entries) {
		for (ICLanguageSetting item : items)
			item.setSettingEntries(kind, entries);
	}

	@Override
	public void setSettingEntries(int kind, List<ICLanguageSettingEntry> entriesList) {
		for (ICLanguageSetting item : items)
			item.setSettingEntries(kind, entriesList);
	}

	@Override
	public void setSourceContentTypeIds(String[] ids) {
		for (ICLanguageSetting item : items)
			item.setSourceContentTypeIds(ids);
	}

	@Override
	public void setSourceExtensions(String[] exts) {
		for (ICLanguageSetting item : items)
			item.setSourceExtensions(exts);
	}

	@Override
	public boolean supportsEntryKind(int kind) {
		for (ICLanguageSetting item : items)
			if (item.supportsEntryKind(kind))
				return true;
		return false;
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiLanguageSetting.getConfiguration()"); //$NON-NLS-1$
		return null; // CFGs are different
	}

	@Override
	public String getId() { // IDs are different
		if (DEBUG)
			System.out.println("Bad multi access: MultiLanguageSetting.getId()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public String getName() { // names are proposed to be equal
		return items[0].getName();
	}

	@Override
	public ICSettingContainer getParent() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiLanguageSetting.getParent()"); //$NON-NLS-1$
		return null; // Parents are different
	}

	@Override
	public int getType() {
		int x = items[0].getType();
		for (ICLanguageSetting item : items)
			if (x != item.getType())
				return 0;
		return x;
	}

	@Override
	public boolean isReadOnly() {
		for (int i = 0; i < items.length; i++)
			if (!items[i].isReadOnly())
				return false;
		return true;
	}

	@Override
	public boolean isValid() {
		for (int i = 0; i < items.length; i++)
			if (!items[i].isValid())
				return false;
		return true;
	}

	@Override
	public Object[] getItems() {
		return items;
	}

	@Override
	public ICLanguageSettingEntry[] getResolvedSettingEntries(int kind) {
		return null;
	}

	@Override
	public ICLanguageSettingEntry[] getSettingEntries(int kind) {
		ICLanguageSettingEntry[][] ses = getSettingEntriesM(kind);
		return ses[0];
	}

	@Override
	public String[] getSourceContentTypeIds() {
		return null;
	}

	@Override
	public String[] getSourceExtensions() {
		return null;
	}

}
