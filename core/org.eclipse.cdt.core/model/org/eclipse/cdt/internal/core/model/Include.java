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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;

public class Include extends SourceManipulation implements IInclude {
	
	private final boolean standard;
	private String fullPath;
	
	public Include(ICElement parent, String name, boolean isStandard) {
		super(parent, name, ICElement.C_INCLUDE);
		standard = isStandard;
	}

	public String getIncludeName() {
		return getElementName();
	}

	public boolean isStandard() {
		return standard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IInclude#getFullFileName()
	 */
	public String getFullFileName() {
		return fullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IInclude#isLocal()
	 */
	public boolean isLocal() {
		return !isStandard();
	}

	/*
	 * This is not yet populated properly by the parse;
	 * however, it might be in the near future.
	 */
	public void setFullPathName(String fullPath) {
		this.fullPath = fullPath;
	}

}
