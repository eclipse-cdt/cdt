/*******************************************************************************
 * Copyright (c) 2007, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a function template, base class for method/constructor templates.
 */
class PDOMCPPFunctionTemplate extends PDOMCPPFunction 
		implements ICPPFunctionTemplate, ICPPInstanceCache, IPDOMMemberOwner, IPDOMCPPTemplateParameterOwner {

	private static final int TEMPLATE_PARAMS = PDOMCPPFunction.RECORD_SIZE;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = TEMPLATE_PARAMS + Database.PTR_SIZE;
	
	private volatile IPDOMCPPTemplateParameter[] params;  // Cached template parameters.

	public PDOMCPPFunctionTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFunctionTemplate template)
			throws CoreException, DOMException {
		super(linkage, parent, template, false);
		final ICPPTemplateParameter[] origParams= template.getTemplateParameters();
		params = PDOMTemplateParameterArray.createPDOMTemplateParameters(linkage, this, origParams);
		final Database db = getDB();
		long rec= PDOMTemplateParameterArray.putArray(db, params);
		db.putRecPtr(record + TEMPLATE_PARAMS, rec);
		linkage.new ConfigureFunctionTemplate(template, this);
	}

	public PDOMCPPFunctionTemplate(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding name) {
		// no support for updating templates, yet.
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_TEMPLATE;
	}

	@Override
	public IPDOMCPPTemplateParameter[] getTemplateParameters() {
		if (params == null) {
			try {
				long rec= getDB().getRecPtr(record + TEMPLATE_PARAMS);
				if (rec == 0) {
					params= IPDOMCPPTemplateParameter.EMPTY_ARRAY;
				} else {
					params= PDOMTemplateParameterArray.getArray(this, rec);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				params = IPDOMCPPTemplateParameter.EMPTY_ARRAY;
			}
		}
		return params;
	}
	
	@Override
	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return PDOMInstanceCache.getCache(this).getInstance(arguments);	
	}

	@Override
	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		PDOMInstanceCache.getCache(this).addInstance(arguments, instance);	
	}
	
	@Override
	public ICPPTemplateInstance[] getAllInstances() {
		return PDOMInstanceCache.getCache(this).getAllInstances();	
	}

	@Override
	public ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param) {
		// Template parameters are identified by their position in the parameter list.
		int pos = param.getParameterPosition();
		ICPPTemplateParameter[] pars = getTemplateParameters();
		
		if (pars == null || pos >= pars.length)
			return null;
		
		ICPPTemplateParameter result= pars[pos];
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
