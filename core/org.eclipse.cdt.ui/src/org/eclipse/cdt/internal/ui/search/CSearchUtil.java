/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (Rational Software) - initial implementation
 *     Torbjörn Svensson (STMicroelectronics) - bug #533379
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.ui.IWorkingSet;

public class CSearchUtil {
	public static int LRU_WORKINGSET_LIST_SIZE = 3;
	private static LRUWorkingSets workingSetsCache;

	public CSearchUtil() {
		super();
	}

	public static void updateLRUWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null || workingSets.length < 1)
			return;

		CSearchUtil.getLRUWorkingSets().add(workingSets);
	}

	public static LRUWorkingSets getLRUWorkingSets() {
		if (CSearchUtil.workingSetsCache == null) {
			CSearchUtil.workingSetsCache = new LRUWorkingSets(CSearchUtil.LRU_WORKINGSET_LIST_SIZE);
		}
		return CSearchUtil.workingSetsCache;
	}

	public static String toString(IWorkingSet[] workingSets) {
		if (workingSets != null && workingSets.length > 0) {
			String string = ""; //$NON-NLS-1$
			for (int i = 0; i < workingSets.length; i++) {
				if (i > 0)
					string += ", "; //$NON-NLS-1$
				string += workingSets[i].getName();
			}

			return string;
		}

		return null;
	}

	public static boolean isWriteOccurrence(IASTName node, IBinding binding) {
		boolean isWrite;
		if (binding instanceof ICPPVariable) {
			isWrite = ((CPPVariableReadWriteFlags.getReadWriteFlags(node) & PDOMName.WRITE_ACCESS) != 0);
		} else {
			isWrite = ((CVariableReadWriteFlags.getReadWriteFlags(node) & PDOMName.WRITE_ACCESS) != 0);
		}
		return isWrite;
	}

	/**
	 * Returns true whether 'ch' could the first character of an overloadable C++ operator.
	 */
	private static boolean isOperatorChar(char ch) {
		switch (ch) {
		case '&':
		case '|':
		case '+':
		case '-':
		case '!':
		case '=':
		case '>':
		case '<':
		case '%':
		case '^':
		case '(':
		case ')':
		case '[':
		case '~':
			return true;
		default:
			return false;
		}
	}

	/**
	 * If 'searchStr' contains the name of an overloadable operator with no
	 * space between 'operator' and the operator (e.g. 'operator<'), insert
	 * a space (yielding e.g. 'operator <'). This is necessary because the
	 * binding names for overloaded operators in the index contain such a
	 * space, and the search wouldn't find them otherwise.
	 */
	public static String adjustSearchStringForOperators(String searchStr) {
		int operatorIndex = searchStr.indexOf("operator"); //$NON-NLS-1$
		if (operatorIndex >= 0) { // Only do this if string actually contains "operator"
			int operatorCharIndex = operatorIndex + 8; // "operator" is 8 characters
			if (operatorCharIndex < searchStr.length() && isOperatorChar(searchStr.charAt(operatorCharIndex))) {
				searchStr = searchStr.substring(0, operatorCharIndex) + ' ' + searchStr.substring(operatorCharIndex);
			}
		}
		return searchStr;
	}
}
