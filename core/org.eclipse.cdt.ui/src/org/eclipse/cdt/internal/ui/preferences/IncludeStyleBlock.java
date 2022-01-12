/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The preference block for configuring style of include statements.
 */
public class IncludeStyleBlock extends TabConfigurationBlock {
	static final Key KEY_STYLE_RELATED = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_RELATED);
	static final Key KEY_STYLE_PARTNER = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_PARTNER);
	static final Key KEY_STYLE_SAME_FOLDER = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_SAME_FOLDER);
	static final Key KEY_STYLE_SUBFOLDER = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_SUBFOLDER);
	static final Key KEY_STYLE_SYSTEM = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_SYSTEM);
	static final Key KEY_STYLE_SYSTEM_WITH_EXTENSION = getCDTUIKey(
			PreferenceConstants.INCLUDE_STYLE_SYSTEM_WITH_EXTENSION);
	static final Key KEY_STYLE_SYSTEM_WITHOUT_EXTENSION = getCDTUIKey(
			PreferenceConstants.INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION);
	static final Key KEY_STYLE_OTHER = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_OTHER);
	static final Key KEY_STYLE_SAME_PROJECT = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_SAME_PROJECT);
	static final Key KEY_STYLE_OTHER_PROJECT = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_OTHER_PROJECT);
	static final Key KEY_STYLE_EXTERNAL = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_EXTERNAL);
	static final Key KEY_STYLE_MATCHING_PATTERN = getCDTUIKey(PreferenceConstants.INCLUDE_STYLE_MATCHING_PATTERN);

	static final Map<IncludeKind, Key> KEY_MAP = createKeyMap();
	static final Key[] STYLE_KEYS = KEY_MAP.values().toArray(new Key[KEY_MAP.size()]);

	private static final String[] TAB_LABELS = { PreferencesMessages.IncludeStyleBlock_categories_tab,
			PreferencesMessages.IncludeStyleBlock_order_tab, };

	private final List<IncludeGroupStyle> styles;

	public IncludeStyleBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
		this(context, project, container, new ArrayList<IncludeGroupStyle>());
	}

	private IncludeStyleBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container,
			List<IncludeGroupStyle> styles) {
		super(context, project, createTabs(context, project, container, styles), TAB_LABELS, container);
		this.styles = styles;
		settingsUpdated();
	}

	private static OptionsConfigurationBlock[] createTabs(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container, List<IncludeGroupStyle> styles) {
		IncludeCategoriesBlock includeCategoriesBlock = new IncludeCategoriesBlock(context, project, container, styles);
		IncludeOrderBlock includeOrderBlock = new IncludeOrderBlock(context, project, container, styles);
		return new OptionsConfigurationBlock[] { includeCategoriesBlock, includeOrderBlock };
	}

	private static Map<IncludeKind, Key> createKeyMap() {
		Map<IncludeKind, Key> map = new HashMap<>();
		map.put(IncludeKind.RELATED, KEY_STYLE_RELATED);
		map.put(IncludeKind.PARTNER, KEY_STYLE_PARTNER);
		map.put(IncludeKind.IN_SAME_FOLDER, KEY_STYLE_SAME_FOLDER);
		map.put(IncludeKind.IN_SUBFOLDER, KEY_STYLE_SUBFOLDER);
		map.put(IncludeKind.SYSTEM, KEY_STYLE_SYSTEM);
		map.put(IncludeKind.SYSTEM_WITH_EXTENSION, KEY_STYLE_SYSTEM_WITH_EXTENSION);
		map.put(IncludeKind.SYSTEM_WITHOUT_EXTENSION, KEY_STYLE_SYSTEM_WITHOUT_EXTENSION);
		map.put(IncludeKind.OTHER, KEY_STYLE_OTHER);
		map.put(IncludeKind.IN_SAME_PROJECT, KEY_STYLE_SAME_PROJECT);
		map.put(IncludeKind.IN_OTHER_PROJECT, KEY_STYLE_OTHER_PROJECT);
		map.put(IncludeKind.EXTERNAL, KEY_STYLE_EXTERNAL);
		return Collections.unmodifiableMap(map);
	}

	@Override
	protected boolean processChanges(IWorkbenchPreferenceContainer container) {
		boolean result = super.processChanges(container);
		for (IncludeGroupStyle style : styles) {
			IncludeKind includeKind = style.getIncludeKind();
			Key key = KEY_MAP.get(includeKind);
			if (includeKind != IncludeKind.MATCHING_PATTERN) {
				setValue(key, style.toXmlString());
			} else {
				// TODO(sprigogin): Support custom include categories.
			}
		}
		return result;
	}

	@Override
	protected void settingsUpdated() {
		if (styles != null) {
			styles.clear();
			for (Map.Entry<IncludeKind, Key> entry : KEY_MAP.entrySet()) {
				IncludeKind includeKind = entry.getKey();
				IncludeGroupStyle style = null;
				String str = getValue(entry.getValue());
				if (str != null)
					style = IncludeGroupStyle.fromXmlString(str, includeKind);
				if (style == null)
					style = new IncludeGroupStyle(includeKind);
				styles.add(style);
			}
		}
		// TODO Propagate styles to tabs.
		super.settingsUpdated();
	}
}
