/*******************************************************************************
 * Copyright (c) 2014 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

/**
 * A semantic highlighting which has its own preferences for specifying
 * its color and style.
 *
 * Semantic highlightings not deriving from this class are associated
 * with a syntactic highlighting, and use the color and style of that
 * syntactic highlighting.
 */
public abstract class SemanticHighlightingWithOwnPreference extends SemanticHighlighting {
	/**
	 * @return the default default text color
	 * @since 5.4
	 */
	public abstract RGB getDefaultDefaultTextColor();

	/**
	 * @return the default text color
	 */
	public RGB getDefaultTextColor() {
		return findRGB(getThemeColorKey(), getDefaultDefaultTextColor());
	}

	/**
	 * @return <code>true</code> if the text attribute bold is set by default
	 */
	public boolean isBoldByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute italic is set by default
	 */
	public boolean isItalicByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute strikethrough is set by default
	 */
	public boolean isStrikethroughByDefault() {
		return false;
	}

	/**
	 * @return <code>true</code> if the text attribute underline is set by default
	 * @since 3.1
	 */
	public boolean isUnderlineByDefault() {
		return false;
	}

	/**
	 * @return the display name
	 */
	public abstract String getDisplayName();

	private String getThemeColorKey() {
		return CUIPlugin.PLUGIN_ID + "." + getPreferenceKey() + "Highlighting"; //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Returns the RGB for the given key in the given color registry.
	 *
	 * @param key the key for the constant in the registry
	 * @param defaultRGB the default RGB if no entry is found
	 * @return RGB the RGB
	 * @since 5.4
	 */
	private static RGB findRGB(String key, RGB defaultRGB) {
		if (!PlatformUI.isWorkbenchRunning())
			return defaultRGB;

		ColorRegistry registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
		RGB rgb = registry.getRGB(key);
		if (rgb != null)
			return rgb;
		return defaultRGB;
	}
}
