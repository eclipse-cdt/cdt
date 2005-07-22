/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.core.runtime.IPath;

/**
 * The Element Info of a Translation Unit.
 */
class TranslationUnitInfo extends OpenableInfo {

	/** 
	 * Timestamp of original resource at the time this element
	 * was opened or last updated.
	 */
	protected long fTimestamp;

	protected TranslationUnitInfo (CElement element) {
		super(element);
	}

	/* Overide the SourceManipulation for the range.  */
	protected ISourceRange getSourceRange() {
		IPath location = ((TranslationUnit)getElement()).getLocation(); 		
		return new SourceRange(0, (int)location.toFile().length());
	}
}
