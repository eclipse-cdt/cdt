/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConceptDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConcept;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPConcept extends PDOMCPPBinding implements ICPPConcept, IPDOMCPPTemplateParameterOwner {
	/**
	 * Offset of RecPtr to record holding template parameters (relative to the beginning of the record).
	 * Field size: PDOMCPPBinding.RECORD_SIZE
	 */
	private static final int TEMPLATE_PARAMS = PDOMCPPBinding.RECORD_SIZE + 0;

	/**
	 * The size in bytes of a PDOMCPPConcept record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TEMPLATE_PARAMS + PDOMCPPBinding.RECORD_SIZE;

	private volatile IPDOMCPPTemplateParameter[] parameters; // Cached template parameters.

	public PDOMCPPConcept(PDOMCPPLinkage linkage, PDOMNode parent, ICPPConcept concept)
			throws CoreException, DOMException {
		super(linkage, parent, concept.getNameCharArray());

		final ICPPTemplateParameter[] origParams = concept.getTemplateParameters();
		IPDOMCPPTemplateParameter[] params = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this,
				origParams);
		final Database db = getDB();
		long rec = PDOMTemplateParameterArray.putArray(db, params);
		db.putRecPtr(record + TEMPLATE_PARAMS, rec);
		linkage.new ConfigureTemplateParameters(origParams, params);
	}

	public PDOMCPPConcept(PDOMCPPLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) {
		// no support for updating templates, yet.
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CONCEPT;
	}

	@Override
	public IPDOMCPPTemplateParameter[] getTemplateParameters() {
		if (parameters == null) {
			try {
				long rec = getDB().getRecPtr(record + TEMPLATE_PARAMS);
				if (rec == 0) {
					parameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					parameters = PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				parameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return parameters;
	}

	@Override
	public ICPPASTConceptDefinition getConceptDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param) {
		// Template parameters are identified by their position in the parameter list.
		int pos = param.getParameterPosition();
		ICPPTemplateParameter[] pars = getTemplateParameters();

		if (pars == null || pos >= pars.length)
			return null;

		ICPPTemplateParameter result = pars[pos];
		if (param instanceof ICPPTemplateTypeParameter) {
			if (result instanceof ICPPTemplateTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateNonTypeParameter) {
			if (result instanceof ICPPTemplateNonTypeParameter)
				return result;
		} else if (param instanceof ICPPTemplateTemplateParameter) {
			if (result instanceof ICPPTemplateTemplateParameter)
				return result;
		}
		return null;
	}
}
