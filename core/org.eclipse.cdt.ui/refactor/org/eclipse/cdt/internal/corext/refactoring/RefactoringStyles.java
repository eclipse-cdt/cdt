/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring;


public final class RefactoringStyles {
	
	public static final int NONE= 0;
	public static final int NEEDS_PREVIEW= 1 << 0;
	public static final int FORCE_PREVIEW= 1 << 1;
	public static final int NEEDS_PROGRESS= 1 << 2;
	
	private RefactoringStyles() {
		// no instance
	}
}
