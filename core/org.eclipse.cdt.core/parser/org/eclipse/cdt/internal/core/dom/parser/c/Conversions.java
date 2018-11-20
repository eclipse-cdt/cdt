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

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;

/**
 * Routines related to conversions.
 */
public class Conversions {
	/**
	 * Perform array-to-pointer decay.
	 */
	public static IType arrayTypeToPointerType(ICArrayType type) {
		return new CPointerType(type.getType(),
				(type.isConst() ? CPointerType.IS_CONST : 0) | (type.isRestrict() ? CPointerType.IS_RESTRICT : 0)
						| (type.isVolatile() ? CPointerType.IS_VOLATILE : 0));
	}
}
