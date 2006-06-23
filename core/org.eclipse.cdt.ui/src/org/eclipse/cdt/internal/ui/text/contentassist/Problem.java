/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

public class Problem implements IProblem {
	String message = null;
	Problem(String message){
		this.message = message;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.IProblem#getMessage()
	 */
	public String getMessage() {
		return message;
	}
}
