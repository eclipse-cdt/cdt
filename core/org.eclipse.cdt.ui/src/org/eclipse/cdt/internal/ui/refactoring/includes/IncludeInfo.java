/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

public class IncludeInfo {
	private final String name;
	private final boolean isSystem;

	public IncludeInfo(String name, boolean isSystem) {
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException();
		this.name = name;
		this.isSystem = isSystem;
	}

	public IncludeInfo(String includeText) {
		if (includeText == null || includeText.isEmpty())
			throw new IllegalArgumentException();
		boolean isSystem = false;
		int begin = 0;
		switch (includeText.charAt(0)) {
		case '<':
			isSystem = true;
			//$FALL-THROUGH$
		case '"':
			++begin;
			break;
		}
		int end = includeText.length();
		switch (includeText.charAt(end - 1)) {
		case '>':
		case '"':
			--end;
			break;
		}
		if (begin >= end)
			throw new IllegalArgumentException();

		this.name = includeText.substring(begin, end);
		this.isSystem = isSystem;
	}

	/**
	 * Returns the part of the include statement identifying the included header file without
	 * quotes or angle brackets.
	 */
	public final String getName() {
		return name;
	}

	public final boolean isSystem() {
		return isSystem;
	}

	@Override
	public int hashCode() {
		return name.hashCode() * 31 + (isSystem ? 1 : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncludeInfo other = (IncludeInfo) obj;
		return name.equals(other.name) && isSystem == other.isSystem;
	}

	/**
	 * Returns the include string as it appears in an {@code #include} statement.
	 */
	@Override
	public String toString() {
		return (isSystem ? '<' : '"') + name + (isSystem ? '>' : '"');
	}
}
