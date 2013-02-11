/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.core.runtime.CoreException;

public abstract class CPPEvaluation implements ICPPEvaluation {

	CPPEvaluation() {
	}

	@Override
	public char[] getSignature() {
		SignatureBuilder buf = new SignatureBuilder();
		try {
			marshal(buf, true);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { '?' };
		}
		return buf.getSignature();
	}
	
	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		return getTypeOrFunctionSet(new LookupContext(point, null));
	}
	
	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return getValueCategory(new LookupContext(point, null));
	}

	protected static IBinding resolveUnknown(ICPPUnknownBinding unknown, ICPPTemplateParameterMap tpMap,
			int packOffset, ICPPClassSpecialization within, LookupContext context) {
		try {
			return CPPTemplates.resolveUnknown(unknown, tpMap, packOffset, within, context);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return unknown;
	}

	protected static ICPPTemplateArgument[] instantiateArguments(ICPPTemplateArgument[] args,
			ICPPTemplateParameterMap tpMap, int packOffset, ICPPClassSpecialization within, LookupContext context) {
		try {
			return CPPTemplates.instantiateArguments(args, tpMap, packOffset, within, context, false);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return args;
	}

	protected static IBinding instantiateBinding(IBinding binding, ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPClassSpecialization within, int maxdepth, IASTNode point) {
		try {
			return CPPTemplates.instantiateBinding(binding, tpMap, packOffset, within, maxdepth, point);
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return binding;
	}
}