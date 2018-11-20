/*******************************************************************************
 * Copyright (c) 2007, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPMethodTemplateSpecialization extends PDOMCPPFunctionTemplateSpecialization implements ICPPMethod {
	private static final int TEMPLATE_PARAMS = PDOMCPPFunctionTemplateSpecialization.RECORD_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TEMPLATE_PARAMS + Database.PTR_SIZE;

	private volatile IPDOMCPPTemplateParameter[] fTemplateParameters;

	public PDOMCPPMethodTemplateSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPMethod method,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPFunctionTemplate) method, specialized);
		computeTemplateParameters((ICPPFunctionTemplate) method); // Sets fTemplateParameters
		final Database db = getDB();
		long rec = PDOMTemplateParameterArray.putArray(db, fTemplateParameters);
		db.putRecPtr(record + TEMPLATE_PARAMS, rec);
		linkage.new ConfigureTemplateParameters(((ICPPFunctionTemplate) method).getTemplateParameters(),
				fTemplateParameters);
	}

	public PDOMCPPMethodTemplateSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (fTemplateParameters == null) {
			try {
				long rec = getDB().getRecPtr(record + TEMPLATE_PARAMS);
				if (rec == 0) {
					fTemplateParameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					fTemplateParameters = PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fTemplateParameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return fTemplateParameters;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_METHOD_TEMPLATE_SPECIALIZATION;
	}

	@Override
	public boolean isDestructor() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod) spec).isDestructor();
		}
		return false;
	}

	@Override
	public boolean isImplicit() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod) spec).isImplicit();
		}
		return false;
	}

	@Override
	public boolean isExplicit() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod) spec).isExplicit();
		}
		return false;
	}

	@Override
	public boolean isVirtual() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod) spec).isVirtual();
		}
		return false;
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getVisibility() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod) spec).getVisibility();
		}
		return 0;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isPureVirtual() {
		return false;
	}

	@Override
	public boolean isOverride() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	private void computeTemplateParameters(ICPPFunctionTemplate originalMethodTemplate) {
		try {
			fTemplateParameters = PDOMTemplateParameterArray.createPDOMTemplateParameters(getLinkage(), this,
					originalMethodTemplate.getTemplateParameters());
		} catch (DOMException e) {
			CCorePlugin.log(e);
			fTemplateParameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			fTemplateParameters = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
		}
	}
}
