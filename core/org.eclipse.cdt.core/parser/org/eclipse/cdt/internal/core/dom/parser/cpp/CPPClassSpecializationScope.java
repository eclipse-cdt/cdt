/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Bryan Wilkinson (QNX)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * @author aniefer
 */
public class CPPClassSpecializationScope extends AbstractCPPClassSpecializationScope implements IASTInternalScope {

	public CPPClassSpecializationScope(ICPPClassSpecialization specialization) {
		super(specialization);
	}
		

	public boolean isFullyCached() throws DOMException {
		ICPPScope origScope = (ICPPScope) getOriginalClassType().getCompositeScope();
		if (!ASTInternal.isFullyCached(origScope)) {
			try {
				CPPSemantics.lookupInScope(null, origScope, null);
			} catch (DOMException e) {
			}
		}
		return true;
	}
	
	// This scope does not cache its own names
	public void setFullyCached(boolean b) {}
	public void flushCache() {}
	public void addName(IASTName name) {}
	public IASTNode getPhysicalNode() { return null; }
	public void removeBinding(IBinding binding) {}
	public void addBinding(IBinding binding) {}
}
