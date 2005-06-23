/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

public class CPElementAttribute {

	private CPElement fParent;
	private String fKey;
	private Object fValue;

	public CPElementAttribute(CPElement parent, String key, Object value) {
		fKey = key;
		fValue = value;
		fParent = parent;
	}

	public CPElement getParent() {
		return fParent;
	}

	/**
	 * Returns the key.
	 * 
	 * @return String
	 */
	public String getKey() {
		return fKey;
	}

	/**
	 * Returns the value.
	 * 
	 * @return Object
	 */
	public Object getValue() {
		return fValue;
	}

	/**
	 * Returns the value.
	 */
	public void setValue(Object value) {
		fValue = value;
	}

}
