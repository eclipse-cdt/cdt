/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IPath;

/**
 * ExternalTranslationUnit
 */
public class ExternalTranslationUnit extends TranslationUnit {

	/**
	 * @param parent
	 * @param path
	 */
	public ExternalTranslationUnit(ICElement parent, IPath path, String contentTypeID) {
		super(parent, path, contentTypeID);
	}
}
