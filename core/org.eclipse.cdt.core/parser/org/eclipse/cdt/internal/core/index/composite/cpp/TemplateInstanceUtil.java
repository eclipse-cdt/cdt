/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateNonTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

/**
 * For implementation re-use in the absence of multiple inheritance.
 */
public class TemplateInstanceUtil {

	public static ICPPTemplateParameterMap getTemplateParameterMap(ICompositesFactory cf,
			ICPPTemplateInstance rbinding) {
		ICPPTemplateParameterMap preresult = rbinding.getTemplateParameterMap();
		Integer[] keys = preresult.getAllParameterPositions();
		CPPTemplateParameterMap result = new CPPTemplateParameterMap(keys.length);

		try {
			for (Integer key : keys) {
				ICPPTemplateArgument arg = preresult.getArgument(key);
				if (arg != null) {
					result.put(key, convert(cf, arg));
				} else {
					ICPPTemplateArgument[] pack = preresult.getPackExpansion(key);
					result.put(key, convert(cf, pack));
				}
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return result;
	}

	public static ICPPTemplateArgument[] getTemplateArguments(ICompositesFactory cf, ICPPTemplateInstance rbinding) {
		return convert(cf, rbinding.getTemplateArguments());
	}

	public static ICPPTemplateArgument[] getTemplateArguments(ICompositesFactory cf,
			ICPPPartialSpecialization rbinding) {
		return convert(cf, rbinding.getTemplateArguments());
	}

	public static IBinding getSpecializedBinding(ICompositesFactory cf, IIndexBinding rbinding) {
		IBinding preresult = ((ICPPSpecialization) rbinding).getSpecializedBinding();
		return cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}

	public static ICPPTemplateDefinition getTemplateDefinition(ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPTemplateDefinition preresult = ((ICPPTemplateInstance) rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}

	public static ICPPTemplateArgument[] convert(ICompositesFactory cf, ICPPTemplateArgument[] arguments) {
		if (arguments == null)
			return null;
		try {
			ICPPTemplateArgument[] result = new ICPPTemplateArgument[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				result[i] = convert(cf, arguments[i]);
			}
			return result;
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return ICPPTemplateArgument.EMPTY_ARGUMENTS;
	}

	static ICPPTemplateArgument convert(ICompositesFactory cf, ICPPTemplateArgument arg) throws DOMException {
		if (arg == null)
			return null;
		if (arg.isTypeValue()) {
			final IType typeValue = arg.getTypeValue();
			IType t = cf.getCompositeType(typeValue);
			if (t != typeValue) {
				return new CPPTemplateTypeArgument(t);
			}
		} else {
			ICPPEvaluation eval = arg.getNonTypeEvaluation();
			ICPPEvaluation eval2 = ((CPPCompositesFactory) cf).getCompositeEvaluation(eval);
			if (eval2 != eval) {
				return new CPPTemplateNonTypeArgument(eval2);
			}
		}
		return arg;
	}

	public static ICPPTemplateParameter[] convert(ICompositesFactory cf, ICPPTemplateParameter[] preResult) {
		ICPPTemplateParameter[] result = new ICPPTemplateParameter[preResult.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (ICPPTemplateParameter) cf.getCompositeBinding((IIndexFragmentBinding) preResult[i]);
		}
		return result;
	}
}
