/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * @since 5.0
 */
public class PDOMCPPClassSpecializationScope extends CPPClassSpecializationScope implements IIndexScope {

	private final PDOMCPPDeferredClassInstance fBinding;

	public PDOMCPPClassSpecializationScope(PDOMCPPDeferredClassInstance specialization) {
		super(specialization);
		fBinding= specialization;
	}

	public IIndexBinding getScopeBinding() {
		return fBinding;
	}
	
	@Override
	public IIndexName getScopeName() {
		return null;
	}
	
	@Override
	public IIndexScope getParent() {
		ICPPClassType cls = getOriginalClass();
		try {
			IScope scope = cls.getCompositeScope();
			if (scope != null) {
				scope= scope.getParent();
				if (scope instanceof IIndexScope) {
					return (IIndexScope) scope;
				}
			}
		} catch (DOMException e) {
		}
		return null;	
	}
}
