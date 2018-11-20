/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumerator;

public class Enumerator extends SourceManipulation implements IEnumerator {

	String constantExpression = ""; //$NON-NLS-1$

	public Enumerator(ICElement parent, String name) {
		super(parent, name, ICElement.C_ENUMERATOR);
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new SourceManipulationInfo(this);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IEnumerator#getConstantExpression()
	 */
	@Override
	public String getConstantExpression() {
		return constantExpression;
	}

	/**
	 * Sets the constantExpression.
	 * @param constantExpression The constantExpression to set
	 */
	public void setConstantExpression(String constantExpression) {
		this.constantExpression = constantExpression;
	}

}
