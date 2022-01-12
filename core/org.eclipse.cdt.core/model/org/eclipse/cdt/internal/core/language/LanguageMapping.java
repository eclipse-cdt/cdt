/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language;

import org.eclipse.cdt.core.model.ILanguage;

/**
 * A language mapping.
 */
public class LanguageMapping {
	public ILanguage language;
	public int inheritedFrom;

	public LanguageMapping(ILanguage language, int inheritedFrom) {
		this.language = language;
		this.inheritedFrom = inheritedFrom;
	}
}
