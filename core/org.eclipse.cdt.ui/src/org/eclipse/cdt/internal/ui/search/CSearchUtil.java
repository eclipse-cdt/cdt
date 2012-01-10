/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (Rational Software) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.ui.IWorkingSet;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

import org.eclipse.cdt.internal.core.dom.parser.c.CVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

public class CSearchUtil {
	public static int LRU_WORKINGSET_LIST_SIZE= 3;
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
			String string = new String();
			for (int i = 0; i < workingSets.length; i++) {
				if (i > 0)
					string += ", ";  //$NON-NLS-1$
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
}
