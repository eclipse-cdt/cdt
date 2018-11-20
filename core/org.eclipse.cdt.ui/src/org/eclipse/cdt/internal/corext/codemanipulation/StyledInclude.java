/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
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
package org.eclipse.cdt.internal.corext.codemanipulation;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.core.runtime.IPath;

/**
 * Represents a new or an existing include statement together with the style associated with it.
 */
public class StyledInclude {
	private final IPath header; // null for existing unresolved includes
	private final IncludeInfo includeInfo; // never null
	private final IncludeGroupStyle style;
	private IASTPreprocessorIncludeStatement existingInclude;

	/** Initializes a StyledInclude object for a new include */
	public StyledInclude(IPath header, IncludeInfo includeInfo, IncludeGroupStyle style) {
		this(header, includeInfo, style, null);
		if (header == null)
			throw new NullPointerException();
	}

	/**
	 * Initializes an include prototype object for an existing include. {@code header} may be
	 * {@code null} if the include was not resolved.
	 */
	public StyledInclude(IPath header, IncludeInfo includeInfo, IncludeGroupStyle style,
			IASTPreprocessorIncludeStatement existingInclude) {
		if (includeInfo == null)
			throw new NullPointerException();
		this.header = header;
		this.includeInfo = includeInfo;
		this.style = style;
		this.existingInclude = existingInclude;
	}

	public IPath getHeader() {
		return header;
	}

	public IncludeInfo getIncludeInfo() {
		return includeInfo;
	}

	public IncludeGroupStyle getStyle() {
		return style;
	}

	public IASTPreprocessorIncludeStatement getExistingInclude() {
		return existingInclude;
	}

	public void setExistingInclude(IASTPreprocessorIncludeStatement existingInclude) {
		this.existingInclude = existingInclude;
	}

	@Override
	public int hashCode() {
		if (header != null)
			return header.hashCode(); // includeInfo is ignored if header is not null
		return includeInfo.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StyledInclude other = (StyledInclude) obj;
		if (header != null)
			return header.equals(other.header); // includeInfo is ignored if header is not null
		if (other.header != null)
			return false;
		return includeInfo.equals(other.includeInfo);
	}

	/** For debugging only */
	@Override
	public String toString() {
		return header != null ? header.toString() : includeInfo.toString();
	}
}