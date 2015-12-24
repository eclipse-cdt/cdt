/*******************************************************************************
 * Copyright (c) 2007, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * The specialization of a friend function template in the context of a class specialization.
 *
 * @author Bryan Wilkinson
 */
class PDOMCPPFunctionTemplateSpecialization extends	PDOMCPPFunctionSpecialization 
		implements ICPPFunctionTemplate, ICPPInstanceCache, IPDOMMemberOwner {
	
	public PDOMCPPFunctionTemplateSpecialization(PDOMCPPLinkage linkage, PDOMNode parent,
			ICPPFunctionTemplate template, PDOMBinding specialized, IASTNode point) throws CoreException {
		super(linkage, parent, template, specialized, point);
	}

	public PDOMCPPFunctionTemplateSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_TEMPLATE_SPECIALIZATION;
	}
	
	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		ICPPFunctionTemplate template = (ICPPFunctionTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
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
}
