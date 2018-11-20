/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Array declarator for c++.
 */
public class CPPASTArrayDeclarator extends CPPASTDeclarator implements ICPPASTArrayDeclarator {
	private IASTArrayModifier[] arrayMods = null;
	private int arrayModsPos = -1;

	public CPPASTArrayDeclarator(IASTName name, IASTInitializer initializer) {
		super(name, initializer);
	}

	public CPPASTArrayDeclarator(IASTName name) {
		super(name);
	}

	public CPPASTArrayDeclarator() {
	}

	@Override
	public CPPASTArrayDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTArrayDeclarator copy(CopyStyle style) {
		CPPASTArrayDeclarator copy = new CPPASTArrayDeclarator();
		for (IASTArrayModifier modifier : getArrayModifiers()) {
			copy.addArrayModifier(modifier == null ? null : modifier.copy(style));
		}
		return copy(copy, style);
	}

	@Override
	public IASTArrayModifier[] getArrayModifiers() {
		if (arrayMods == null)
			return IASTArrayModifier.EMPTY_ARRAY;
		arrayMods = ArrayUtil.trimAt(IASTArrayModifier.class, arrayMods, arrayModsPos);
		return arrayMods;
	}

	@Override
	public void addArrayModifier(IASTArrayModifier arrayModifier) {
		assertNotFrozen();
		if (arrayModifier != null) {
			arrayMods = ArrayUtil.appendAt(IASTArrayModifier.class, arrayMods, ++arrayModsPos, arrayModifier);
			arrayModifier.setParent(this);
			arrayModifier.setPropertyInParent(ARRAY_MODIFIER);
		}
	}

	@Override
	protected boolean postAccept(ASTVisitor action) {
		IASTArrayModifier[] mods = getArrayModifiers();
		for (int i = 0; i < mods.length; i++) {
			if (!mods[i].accept(action))
				return false;
		}
		return super.postAccept(action);
	}
}
