/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IEnumerator;

public class Enumerator extends SourceManipulation implements IEnumerator{

	String constantExpression = ""; //$NON-NLS-1$
	
	public Enumerator(ICElement parent, String name) {
		super(parent, name, ICElement.C_ENUMERATOR);
	}
	
	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
	
	/**
	 * @see org.eclipse.cdt.core.model.IEnumerator#getConstantExptrssion()
	 */
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
