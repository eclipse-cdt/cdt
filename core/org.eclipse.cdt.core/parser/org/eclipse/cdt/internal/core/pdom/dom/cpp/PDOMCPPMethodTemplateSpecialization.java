/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPMethodTemplateSpecialization extends
		PDOMCPPFunctionTemplateSpecialization implements ICPPMethod {
	
	public PDOMCPPMethodTemplateSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPMethod method, PDOMBinding specialized)
			throws CoreException {
		super(linkage, parent, (ICPPFunctionTemplate) method, specialized);
	}

	public PDOMCPPMethodTemplateSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_METHOD_TEMPLATE_SPECIALIZATION;
	}
	
	@Override
	public boolean isDestructor() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isDestructor();
		}
		return false;
	}

	@Override
	public boolean isImplicit() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isImplicit();
		}
		return false;
	}
	
	@Override
	public boolean isExplicit() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isExplicit();
		}
		return false;
	}

	@Override
	public boolean isVirtual() {
		IBinding spec = getSpecializedBinding();
		if (spec instanceof ICPPMethod) {
			((ICPPMethod)spec).isVirtual();
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
			((ICPPMethod)spec).getVisibility();
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
}
