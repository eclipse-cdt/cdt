/*******************************************************************************
 * Copyright (c) 2009,2016 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemElement;
import org.eclipse.cdt.codan.core.model.IProblemProfile;

/**
 * base class for category and problem
 */
public class CodanProblemElement implements IProblemElement {
	private IProblemProfile profile;
	private IProblemCategory parent;
	private boolean frozen = false;

	@Override
	public IProblemProfile getProfile() {
		return profile;
	}

	@Override
	public IProblemCategory getParentCategory() {
		return parent;
	}

	/**
	 * @param profile - parent profile
	 */
	public void setProfile(IProblemProfile profile) {
		checkSet();
		this.profile = profile;
	}

	/**
	 * @param parent - parent category
	 */
	public void setParentCategory(IProblemCategory parent) {
		checkSet();
		this.parent = parent;
	}

	@Override
	public Object clone() {
		try {
			CodanProblemElement prob = (CodanProblemElement) super.clone();
			return prob;
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}

	protected void checkSet() {
		if (frozen)
			throw new IllegalStateException("Object is unmodifieble"); //$NON-NLS-1$
	}

	/**
	 * @param b
	 */
	protected void setFrozen(boolean b) {
		this.frozen = b;
	}

	/**
	 * @return
	 */
	protected boolean isFrozen() {
		return frozen;
	}
}
