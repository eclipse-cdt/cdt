/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
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

	public static ICPPTemplateParameterMap getTemplateParameterMap(ICompositesFactory cf, ICPPTemplateInstance rbinding) {
		ICPPTemplateParameterMap preresult= rbinding.getTemplateParameterMap();
		Integer[] keys= preresult.getAllParameterPositions();
		CPPTemplateParameterMap result= new CPPTemplateParameterMap(keys.length);
		
		try {
			for (Integer key : keys) {
				ICPPTemplateArgument arg= preresult.getArgument(key);
				if (arg != null) {
					result.put(key, convert(cf, arg));
				} else {
					ICPPTemplateArgument[] pack= preresult.getPackExpansion(key);
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

	public static ICPPTemplateArgument[] getTemplateArguments(ICompositesFactory cf, ICPPClassTemplatePartialSpecialization rbinding) {
		return convert(cf, rbinding.getTemplateArguments());
	}

	public static IBinding getSpecializedBinding(ICompositesFactory cf, IIndexBinding rbinding) {
		IBinding preresult= ((ICPPSpecialization) rbinding).getSpecializedBinding();
		return cf.getCompositeBinding((IIndexFragmentBinding) preresult);
	}
		
	public static ICPPTemplateDefinition getTemplateDefinition(ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPTemplateDefinition preresult= ((ICPPTemplateInstance)rbinding).getTemplateDefinition();
		return (ICPPTemplateDefinition) cf.getCompositeBinding((IIndexFragmentBinding)preresult);
	}
	
	public static ICPPTemplateArgument[] convert(ICompositesFactory cf, ICPPTemplateArgument[] arguments) {
		if (arguments == null)
			return null;
		try {
			ICPPTemplateArgument[] result= new ICPPTemplateArgument[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				result[i]= convert(cf, arguments[i]);
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
			IType t= cf.getCompositeType(typeValue);
			if (t != typeValue) {
				return new CPPTemplateTypeArgument(t);
			}
		} else {
			ICPPEvaluation eval = arg.getNonTypeEvaluation();
			ICPPEvaluation eval2 = ((CPPCompositesFactory) cf).getCompositeEvaluation(eval);
			if (eval2 != eval) {
				return new CPPTemplateNonTypeArgument(eval2, null);
			}
		}
		return arg;
	}
	
	@Deprecated
	public static ObjectMap getArgumentMap(ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPSpecialization specn= (ICPPSpecialization) rbinding; 
		IBinding specd= ((CPPCompositesFactory) cf).findOneBinding(specn.getSpecializedBinding());
		if (specd == null)
			specd= specn.getSpecializedBinding();
		
		ObjectMap preresult= specn.getArgumentMap();
		ObjectMap result= new ObjectMap(preresult.size());
		Object[] keys= preresult.keyArray();
		Object[] keysToAdapt= keys;
		
		if (specd instanceof ICPPTemplateDefinition) {
			keysToAdapt= ((ICPPTemplateDefinition) specd).getTemplateParameters();
		}
		for (int i= 0; i < keys.length && i < keysToAdapt.length; i++) {
			IType type= (IType) preresult.get(keys[i]);
			result.put(
					cf.getCompositeBinding((IIndexFragmentBinding) keysToAdapt[i]), cf.getCompositeType(type));
		}
		
		return result;
	}

	@Deprecated
	public static IType[] getArguments(ICompositesFactory cf, ICPPTemplateInstance rbinding) {
		return getArguments(cf, rbinding.getArguments());
	}
	
	@Deprecated
	public static IType[] getArguments(ICompositesFactory cf, ICPPClassTemplatePartialSpecialization rbinding) {
		try {
			return getArguments(cf, rbinding.getArguments());
		} catch (DOMException e) {
			CCorePlugin.log(e);
			return IType.EMPTY_TYPE_ARRAY;
		}
	}
	
	@Deprecated
	private static IType[] getArguments(ICompositesFactory cf, IType[] result) {
		for (int i= 0; i < result.length; i++) {
			result[i] = cf.getCompositeType(result[i]);
		}
		return result;
	}

	public static ICPPTemplateParameter[] convert(ICompositesFactory cf, ICPPTemplateParameter[] preResult) {
		ICPPTemplateParameter[] result= new ICPPTemplateParameter[preResult.length];
		for (int i= 0; i < result.length; i++) {
			result[i]= (ICPPTemplateParameter) cf.getCompositeBinding((IIndexFragmentBinding) preResult[i]);
		}
		return result;
	}
}
