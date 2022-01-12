/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;

public class Include extends SourceManipulation implements IInclude {
	private String fullPath;
	private final boolean standard;
	private boolean fIsResolved = true;

	public Include(ICElement parent, String name, boolean isStandard) {
		super(parent, name, ICElement.C_INCLUDE);
		standard = isStandard;
	}

	@Override
	public String getIncludeName() {
		return getElementName();
	}

	@Override
	public boolean isStandard() {
		return standard;
	}

	@Override
	public String getFullFileName() {
		return fullPath;
	}

	@Override
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

	public void setResolved(boolean resolved) {
		fIsResolved = resolved;
	}

	@Override
	public boolean isResolved() {
		return fIsResolved;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IInclude && equals(this, (IInclude) other)) {
			return true;
		}
		return false;
	}

	public static boolean equals(IInclude lhs, IInclude rhs) {
		return CElement.equals(lhs, rhs) && lhs.isActive() == rhs.isActive() && lhs.isResolved() == rhs.isResolved()
				&& lhs.isLocal() == rhs.isLocal() && (lhs.getFullFileName() == rhs.getFullFileName()
						|| lhs.getFullFileName() != null && lhs.getFullFileName().equals(rhs.getFullFileName()));
	}
}
