/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.base;


import org.eclipse.core.runtime.IAdaptable;

/**
 * A <code>Context<code> can be used to annotate a <code>RefactoringStatusEntry</code>with 
 * additional information presentable in the UI.
 */
public class Context {
	public IAdaptable getCorrespondingElement() {
		return null;
	}
}
