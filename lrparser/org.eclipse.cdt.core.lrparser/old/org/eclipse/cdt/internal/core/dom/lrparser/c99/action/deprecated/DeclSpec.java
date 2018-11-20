/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.action.deprecated;

import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK__Bool;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK__Complex;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK__Imaginary;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_auto;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_char;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_const;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_double;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_extern;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_float;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_inline;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_int;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_long;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_register;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_restrict;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_short;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_signed;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_static;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_unsigned;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_void;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_volatile;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99BasicType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Function;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99QualifierType;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Variable;

/**
 * Keeps track of declaration specifiers during the parse.
 * Used to compute types and determine if a declarator is a typedef.
 *
 * @author Mike Kucera
 */
class DeclSpec {

	// maps token kinds to the number of occurrences of that kind
	private Map<Integer, Integer> tokenKindMap = new HashMap<>();

	private IType type = null;

	public void add(int kind) {
		tokenKindMap.put(kind, count(kind) + 1);
	}

	public void remove(final int kind) {
		Integer count = tokenKindMap.get(kind);
		if (count == null)
			return;

		if (count <= 1)
			tokenKindMap.remove(kind);
		else
			tokenKindMap.put(kind, count - 1);
	}

	public boolean contains(int kind) {
		return tokenKindMap.containsKey(kind);
	}

	public boolean isTypedef() {
		return contains(C99Parsersym.TK_typedef);
	}

	/**
	 * Need to keep track of how many times a particular
	 * declaration specifier appears in order to support
	 * long long.
	 */
	public int count(int kind) {
		Integer count = tokenKindMap.get(kind);
		return count == null ? 0 : count;
	}

	/**
	 * Set if the type should be a structure.
	 */
	public void setType(IType type) {
		this.type = type;
	}

	public IType getType() {
		if (type != null)
			return type;
		if (tokenKindMap.isEmpty()) // there are no type tokens, so it must be implicit int
			return new C99BasicType(IBasicType.t_int);

		C99BasicType basicType = new C99BasicType();

		for (int kind : tokenKindMap.keySet()) {
			switch (kind) {
			case TK_void:
				basicType.setType(IBasicType.t_void);
				break;
			case TK_char:
				basicType.setType(IBasicType.t_char);
				break;
			case TK_int:
				basicType.setType(IBasicType.t_int);
				break;
			case TK_float:
				basicType.setType(IBasicType.t_float);
				break;
			case TK_double:
				basicType.setType(IBasicType.t_double);
				break;
			case TK_long:
				boolean isLongLong = count(TK_long) > 1;
				basicType.setLongLong(isLongLong);
				basicType.setLong(!isLongLong);
				break;
			case TK_signed:
				basicType.setSigned(true);
				break;
			case TK_unsigned:
				basicType.setUnsigned(true);
				break;
			case TK_short:
				basicType.setShort(true);
				break;
			case TK__Bool:
				basicType.setType(ICBasicType.t_Bool);
				break;
			case TK__Complex:
				basicType.setComplex(true);
				break;
			case TK__Imaginary:
				basicType.setImaginary(true);
				break;
			}
		}

		boolean isConst = contains(TK_const);
		boolean isRestrict = contains(TK_restrict);
		boolean isVolatile = contains(TK_volatile);

		if (isConst || isRestrict || isVolatile)
			return new C99QualifierType(basicType, isConst, isVolatile, isRestrict);
		return basicType;
	}

	public void modifyBinding(C99Variable var) {
		if (!var.isAuto())
			var.setAuto(contains(TK_auto));
		if (!var.isExtern())
			var.setExtern(contains(TK_extern));
		if (!var.isRegister())
			var.setRegister(contains(TK_register));
		if (!var.isStatic())
			var.setStatic(contains(TK_static));
	}

	public void modifyBinding(C99Function function) {
		if (!function.isAuto())
			function.setAuto(contains(TK_auto));
		if (!function.isExtern())
			function.setExtern(contains(TK_extern));
		if (!function.isInline())
			function.setInline(contains(TK_inline));
		if (!function.isRegister())
			function.setRegister(contains(TK_register));
		if (!function.isStatic())
			function.setStatic(contains(TK_static));
	}
}
