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

package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;

public class CommonCTypes {
	public static IType pointerToVoid = pointerTo(CBasicType.VOID);
	public static IType pointerToConstVoid = pointerTo(constOf(CBasicType.VOID));
	public static IType pointerToInt = pointerTo(CBasicType.INT);
	public static IType pointerToConstInt = pointerTo(constOf(CBasicType.INT));
	public static IType pointerToVolatileInt = pointerTo(volatileOf(CBasicType.INT));
	public static IType pointerToConstVolatileInt = pointerTo(constVolatileOf(CBasicType.INT));

	private static IType pointerTo(IType type) {
		return new CPointerType(type, 0);
	}

	private static IType constOf(IType type) {
		return new CQualifierType(type, true, false, false);
	}

	private static IType volatileOf(IType type) {
		return new CQualifierType(type, false, true, false);
	}

	private static IType constVolatileOf(IType type) {
		return new CQualifierType(type, true, true, false);
	}
}