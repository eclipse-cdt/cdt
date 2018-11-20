/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Collects methods to store an argument list in the database.
 */
public class PDOMTemplateParameterArray {
	/**
	 * Stores the given template arguments in the database.
	 * @return the record by which the arguments can be referenced.
	 */
	public static long putArray(final Database db, IPDOMCPPTemplateParameter[] params) throws CoreException {
		final short len = (short) Math.min(params.length, (Database.MAX_MALLOC_SIZE - 2) / 8);
		final long block = db.malloc(2 + 8 * len);
		long p = block;

		db.putShort(p, len);
		p += 2;
		for (int i = 0; i < len; i++, p += 4) {
			final IPDOMCPPTemplateParameter elem = params[i];
			db.putRecPtr(p, elem == null ? 0 : elem.getRecord());
		}
		return block;
	}

	/**
	 * Restores an array of template arguments from the database.
	 */
	public static IPDOMCPPTemplateParameter[] getArray(PDOMNode parent, long rec) throws CoreException {
		final PDOM pdom = parent.getPDOM();
		final Database db = pdom.getDB();
		final short len = db.getShort(rec);

		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE - 2) / 8);
		if (len == 0) {
			return IPDOMCPPTemplateParameter.EMPTY_ARRAY;
		}

		rec += 2;
		IPDOMCPPTemplateParameter[] result = new IPDOMCPPTemplateParameter[len];
		for (int i = 0; i < len; i++) {
			final long nodeRec = db.getRecPtr(rec);
			rec += 4;
			result[i] = nodeRec == 0 ? null : (IPDOMCPPTemplateParameter) PDOMNode.load(pdom, nodeRec);
		}
		return result;
	}

	/**
	 * Creates template parameters in the pdom
	 */
	public static IPDOMCPPTemplateParameter[] createPDOMTemplateParameters(PDOMLinkage linkage, PDOMNode parent,
			ICPPTemplateParameter[] origParams) throws CoreException, DOMException {
		IPDOMCPPTemplateParameter[] params = new IPDOMCPPTemplateParameter[origParams.length];
		for (int i = 0; i < origParams.length; i++) {
			params[i] = createPDOMTemplateParameter(linkage, parent, origParams[i]);
		}
		return params;
	}

	/**
	 * Creates a template parameter in the pdom
	 */
	public static IPDOMCPPTemplateParameter createPDOMTemplateParameter(PDOMLinkage linkage, PDOMNode parent,
			ICPPTemplateParameter origParam) throws CoreException, DOMException {
		IPDOMCPPTemplateParameter param = null;
		if (origParam instanceof ICPPTemplateNonTypeParameter) {
			param = new PDOMCPPTemplateNonTypeParameter(linkage, parent, (ICPPTemplateNonTypeParameter) origParam);
		} else if (origParam instanceof ICPPTemplateTypeParameter) {
			param = new PDOMCPPTemplateTypeParameter(linkage, parent, (ICPPTemplateTypeParameter) origParam);
		} else if (origParam instanceof ICPPTemplateTemplateParameter) {
			param = new PDOMCPPTemplateTemplateParameter(linkage, parent, (ICPPTemplateTemplateParameter) origParam);
		}
		return param;
	}
}
