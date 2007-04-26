/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;


/**
 * This class defines various types of UIElement groups.
 */

public class UIGroupTypeEnum {
	public static final UIGroupTypeEnum PAGES_ONLY = new UIGroupTypeEnum("PAGES_ONLY"); //$NON-NLS-1$
	public static final UIGroupTypeEnum PAGES_TAB = new UIGroupTypeEnum("PAGES_TAB"); //$NON-NLS-1$
	
	private String id;
	
	private UIGroupTypeEnum(String id) {
		this.id = id;
	}
	
	public boolean equals(Object other) {
		if(other instanceof UIGroupTypeEnum) {
			return id.equals(((UIGroupTypeEnum)other).id);
		}
		return false;
	}
	
	public int hashCode() {
		return id.hashCode();
	}
	
	public String toString() {
		return id;
	}
}
