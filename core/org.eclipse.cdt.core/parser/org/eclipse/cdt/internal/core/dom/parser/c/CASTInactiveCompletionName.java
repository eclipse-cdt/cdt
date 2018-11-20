/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInactiveCompletionName;

public class CASTInactiveCompletionName extends CASTName implements IASTInactiveCompletionName {
	private IASTTranslationUnit fAst;

	public CASTInactiveCompletionName(char[] name, IASTTranslationUnit ast) {
		super(name);
		fAst = ast;
	}

	@Override
	public IASTCompletionContext getCompletionContext() {
		return this;
	}

	@Override
	public IBinding[] findBindings(IASTName name, boolean isPrefix) {
		// 'name' (which is the same as 'this') is not hooked up to the AST, but it
		// does have a location (offset and length) which we use to compute the
		// containing scope.
		IASTNodeSelector sel = fAst.getNodeSelector(null);
		IASTNode node = sel.findEnclosingNode(getOffset(), getLength());
		IScope lookupScope = CVisitor.getContainingScope(node);
		if (lookupScope == null) {
			lookupScope = fAst.getScope();
		}
		IBinding[] result = null;
		try {
			if (isPrefix) {
				result = CVisitor.lookupPrefix(lookupScope, name);
			} else {
				result = new IBinding[] { CVisitor.lookup(lookupScope, name) };
			}
		} catch (DOMException e) {
		}
		return ArrayUtil.trim(IBinding.class, result);
	}
}
