/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
