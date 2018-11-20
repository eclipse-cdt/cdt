/*******************************************************************************
 * Copyright (c) 2005, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

/**
 * Collects some basic functionality.
 */
public class CRefactoringUtils {

	public static boolean isIdentifierChar(char c) {
		return isLeadingIdentifierChar(c) || ('0' <= c && c <= '9');
	}

	public static boolean isLeadingIdentifierChar(char c) {
		return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '_';
	}

	public static boolean checkIdentifier(String id) {
		if (id.length() == 0) {
			return false;
		}
		if (!isLeadingIdentifierChar(id.charAt(0))) {
			return false;
		}
		for (int i = 1; i < id.length(); i++) {
			if (!isIdentifierChar(id.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
