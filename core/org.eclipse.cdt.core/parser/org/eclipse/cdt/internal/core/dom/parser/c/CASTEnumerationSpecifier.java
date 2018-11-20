/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalEnumerationSpecifier;

/**
 * AST node for enumeration specifiers.
 */
public class CASTEnumerationSpecifier extends CASTBaseDeclSpecifier
		implements IASTInternalEnumerationSpecifier, ICASTEnumerationSpecifier {
	private IASTName fName;
	private Boolean fValuesComputed;
	private IASTEnumerator[] fEnumerators = IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
	private int fNumEnumerators;

	public CASTEnumerationSpecifier() {
	}

	public CASTEnumerationSpecifier(IASTName name) {
		setName(name);
	}

	@Override
	public CASTEnumerationSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTEnumerationSpecifier copy(CopyStyle style) {
		CASTEnumerationSpecifier copy = new CASTEnumerationSpecifier();
		return copy(copy, style);
	}

	protected <T extends CASTEnumerationSpecifier> T copy(T copy, CopyStyle style) {
		copy.setName(fName == null ? null : fName.copy(style));
		for (IASTEnumerator enumerator : getEnumerators()) {
			copy.addEnumerator(enumerator == null ? null : enumerator.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public boolean startValueComputation() {
		if (fValuesComputed != null)
			return false;

		fValuesComputed = Boolean.FALSE;
		return true;
	}

	@Override
	public void finishValueComputation() {
		fValuesComputed = Boolean.TRUE;
	}

	@Override
	public boolean isValueComputationInProgress() {
		return fValuesComputed != null && !fValuesComputed;
	}

	@Override
	public void addEnumerator(IASTEnumerator enumerator) {
		assertNotFrozen();
		if (enumerator != null) {
			enumerator.setParent(this);
			enumerator.setPropertyInParent(ENUMERATOR);
			fEnumerators = ArrayUtil.appendAt(fEnumerators, fNumEnumerators++, enumerator);
		}
	}

	@Override
	public IASTEnumerator[] getEnumerators() {
		fEnumerators = ArrayUtil.trim(fEnumerators, fNumEnumerators);
		return fEnumerators;
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
		this.fName = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATION_NAME);
		}
	}

	@Override
	public IASTName getName() {
		return fName;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (!visitAlignmentSpecifiers(action)) {
			return false;
		}
		if (fName != null && !fName.accept(action))
			return false;
		IASTEnumerator[] etors = getEnumerators();
		for (int i = 0; i < etors.length; i++) {
			if (!etors[i].accept(action))
				return false;
		}
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public int getRoleForName(IASTName n) {
		if (this.fName == n)
			return r_definition;
		return r_unclear;
	}
}
