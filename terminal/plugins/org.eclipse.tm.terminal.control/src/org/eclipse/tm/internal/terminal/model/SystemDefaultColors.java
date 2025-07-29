/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.model;

import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Wrapper class to get standard colors from Eclipse trying to match existing theme where possible
 * by using standard editor colors.
 *
 * This class has an implied and optional dependency on org.eclipse.ui.editors bundle by reading
 * that bundles preferences.
 */
public enum SystemDefaultColors implements Supplier<RGB> {

	/**
	 * Standard text foreground. Typically black in Light theme.
	 */
	FOREGROUND("Foreground", "COLOR_LIST_FOREGROUND", new RGB(0, 0, 0)), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Standard text background. Typically white in Light theme.
	 */
	BACKGROUND("Background", "COLOR_LIST_BACKGROUND", new RGB(255, 255, 255)), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Selection foreground. Typically white in Light theme.
	 */
	SELECTION_FOREGROUND("SelectionForeground", "COLOR_LIST_SELECTION_TEXT", //$NON-NLS-1$ //$NON-NLS-2$
			new RGB(255, 255, 255)),

	/**
	 * Selection background. Typically blue in Light theme.
	 */
	SELECTION_BACKGROUND("SelectionBackground", "COLOR_LIST_SELECTION", new RGB(74, 144, 9)); //$NON-NLS-1$ //$NON-NLS-2$

	private static final String EDITOR_SCOPE = "org.eclipse.ui.editors"; //$NON-NLS-1$
	private static final String PREF_PREFIX = "AbstractTextEditor.Color."; //$NON-NLS-1$
	private static final String PREF_SYSTEM_DEFAULT_SUFFIX = ".SystemDefault"; //$NON-NLS-1$

	/**
	 * SWT Name of Color
	 *
	 * Values from SWT
	 */
	private String swtColor;

	/**
	 * Preference name for color.
	 *
	 * Values from org.eclipse.ui.texteditor.AbstractTextEditor....
	 */
	private String editorColor;

	/** If all else fails, use this standard color */
	private RGB fallbackColor;

	SystemDefaultColors(String editorColor, String swtColor, RGB rgb) {
		this.editorColor = editorColor;
		this.swtColor = swtColor;
		this.fallbackColor = rgb;
	}

	/**
	 * Get the color for this enum value.
	 *
	 * @return the RGB color or a non-<code>null</code> color as a fallback.
	 */
	@Override
	public RGB get() {
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, EDITOR_SCOPE);

		RGB rgb = null;
		String pref = PREF_PREFIX + editorColor;
		String prefSystemDefault = pref + PREF_SYSTEM_DEFAULT_SUFFIX;
		if (Platform.getPreferencesService() != null) {
			if (!store.getBoolean(prefSystemDefault)) {
				if (store.contains(pref)) {
					if (store.isDefault(pref))
						rgb = PreferenceConverter.getDefaultColor(store, pref);
					else {
						rgb = PreferenceConverter.getColor(store, pref);
					}
				}
			}
		}

		if (rgb == null) {
			rgb = ColorUtil.getColorValue(swtColor);
		}

		if (rgb == null) {
			rgb = fallbackColor;
		}

		return rgb;
	}
}