/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.generic;

/**
 * Record class for a generic documentation tool tag.
 * @since 5.0
 */
public class GenericDocTag {
	protected final String name, description;

	/**
	 * Create a tag
	 * @param name
	 * @param description
	 */
	public GenericDocTag(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the tag name (without any prefix e.g. no at or backslash)
	 */
	public String getTagName() {
		return name;
	}

	/**
	 * @return a human readable description of the tag. May be null.
	 */
	public String getTagDescription() {
		return description;
	}
}
