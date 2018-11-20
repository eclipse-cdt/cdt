/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.List;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingEntriesSerializer;

public class CExternalSettingSerializer {
	static final String ELEMENT_SETTING_INFO = "externalSetting"; //$NON-NLS-1$
	//	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_EXTENSIONS = "extensions"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT_TYPE_IDS = "contentTypes"; //$NON-NLS-1$
	private static final String ATTRIBUTE_LANGUAGE_IDS = "languages"; //$NON-NLS-1$
	//	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String SEPARATOR = ":"; //$NON-NLS-1$

	public static CExternalSetting load(ICStorageElement element) {
		String langIds[] = null;
		String cTypeIds[] = null;
		String exts[] = null;
		String tmp = element.getAttribute(ATTRIBUTE_LANGUAGE_IDS);
		if (tmp != null)
			langIds = CDataUtil.stringToArray(tmp, SEPARATOR);

		tmp = element.getAttribute(ATTRIBUTE_CONTENT_TYPE_IDS);
		if (tmp != null)
			cTypeIds = CDataUtil.stringToArray(tmp, SEPARATOR);

		tmp = element.getAttribute(ATTRIBUTE_EXTENSIONS);
		if (tmp != null)
			exts = CDataUtil.stringToArray(tmp, SEPARATOR);

		List<ICSettingEntry> entriesList = LanguageSettingEntriesSerializer.loadEntriesList(element,
				KindBasedStore.ORED_LANG_ENTRY_KINDS);
		ICSettingEntry[] entries = entriesList.toArray(new ICSettingEntry[entriesList.size()]);
		return new CExternalSetting(langIds, cTypeIds, exts, entries);
	}

	public static void store(CExternalSetting setting, ICStorageElement el) {
		String[] tmp;
		tmp = setting.getCompatibleLanguageIds();
		if (tmp != null)
			el.setAttribute(ATTRIBUTE_LANGUAGE_IDS, CDataUtil.arrayToString(tmp, SEPARATOR));

		tmp = setting.getCompatibleContentTypeIds();
		if (tmp != null)
			el.setAttribute(ATTRIBUTE_CONTENT_TYPE_IDS, CDataUtil.arrayToString(tmp, SEPARATOR));

		tmp = setting.getCompatibleExtensions();
		if (tmp != null)
			el.setAttribute(ATTRIBUTE_EXTENSIONS, CDataUtil.arrayToString(tmp, SEPARATOR));

		LanguageSettingEntriesSerializer.serializeEntries(setting.getEntries(), el);
	}
}
