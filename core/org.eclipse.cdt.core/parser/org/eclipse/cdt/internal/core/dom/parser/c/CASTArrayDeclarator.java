/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTArrayDeclarator extends CASTDeclarator implements IASTArrayDeclarator {
	private IASTArrayModifier[] arrayMods;
	private int arrayModsPos = -1;

	public CASTArrayDeclarator() {
	}

	public CASTArrayDeclarator(IASTName name, IASTInitializer initializer) {
		super(name, initializer);
	}

	public CASTArrayDeclarator(IASTName name) {
		super(name);
	}

	@Override
	public CASTArrayDeclarator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTArrayDeclarator copy(CopyStyle style) {
		CASTArrayDeclarator copy = new CASTArrayDeclarator();
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
			arrayModifier.setParent(this);
			arrayModifier.setPropertyInParent(ARRAY_MODIFIER);
			arrayMods = ArrayUtil.appendAt(IASTArrayModifier.class, arrayMods, ++arrayModsPos, arrayModifier);
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
