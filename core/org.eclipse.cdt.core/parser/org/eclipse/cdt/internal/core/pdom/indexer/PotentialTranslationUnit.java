/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IFile;

/**
 * Used for modified files that are not below a source root. In case such a file
 * is part of the index it needs to be updated, otherwise it shall be ignored.
 */
public class PotentialTranslationUnit extends TranslationUnit {
	public PotentialTranslationUnit(ICElement parent, IFile file) {
		super(parent, file, CCorePlugin.CONTENT_TYPE_CHEADER);
	}
}
