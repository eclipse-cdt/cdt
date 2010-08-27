/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * Interface for internal c++ scopes
 */
public interface ICPPASTInternalScope extends IASTInternalScope, ICPPScope {
	/**
	 * Same as {@link IScope#getBindings(IASTName, boolean, boolean, IIndexFileSet)} with the
	 * possibility to disable checking the point of declaration. The method is used to resolve
	 * dependent bindings, where the points of declaration may be reversed. 
	 */
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, 
			IIndexFileSet acceptLocalBindings, boolean checkPointOfDecl);
}
