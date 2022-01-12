/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

/**
 * This class defines various types of UIElement groups.
 */
public class UIGroupTypeEnum {
	public static final UIGroupTypeEnum PAGES_ONLY = new UIGroupTypeEnum("PAGES-ONLY"); //$NON-NLS-1$
	public static final UIGroupTypeEnum PAGES_TAB = new UIGroupTypeEnum("PAGES-TAB"); //$NON-NLS-1$

	private String id;

	private UIGroupTypeEnum(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UIGroupTypeEnum) {
			return id.equals(((UIGroupTypeEnum) other).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}
}
