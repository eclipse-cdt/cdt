/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexInternalTemplateParameter;
import org.eclipse.core.runtime.CoreException;

/**
 * Determines the signatures and signature mementos for bindings that can have
 * siblings with the same name.
 * 
 * @author Bryan Wilkinson
 */
public class PDOMCPPOverloaderUtil {
	
	/**
	 * Returns the signature for the binding.  Returns an empty string if a
	 * signature is not required for the binding.
	 * 
	 * @param binding
	 * @return the signature or an empty string
	 * @throws CoreException
	 * @throws DOMException
	 */
	public static String getSignature(IBinding binding) throws CoreException, DOMException {
		StringBuffer buffer = new StringBuffer();
		if (binding instanceof ICPPTemplateInstance) {
			ICPPTemplateInstance inst = (ICPPTemplateInstance) binding;
			buffer.append(getTemplateArgString(inst.getArguments(), true));
		} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
			ICPPClassTemplatePartialSpecialization partial = (ICPPClassTemplatePartialSpecialization) binding;
			buffer.append(getTemplateArgString(partial.getArguments(), false));
		} else if (binding instanceof ICPPSpecialization) {
			ICPPSpecialization spec = (ICPPSpecialization) binding;
			ObjectMap argMap = spec.getArgumentMap();
			IType[] args = new IType[argMap.size()];
			for (int i = 0; i < argMap.size(); i++) {
				args[i] = (IType) argMap.getAt(i);
			}
			buffer.append(getTemplateArgString(args, false));
		} 
		
		if (binding instanceof IFunction) {
			IFunction function = (IFunction) binding;
			buffer.append(getFunctionParameterString((function.getType())));
		}
		
		return buffer.toString();
	}
	
	/**
	 * Constructs a string in the format:
	 *   <typeName1,typeName2,...>
	 * 
	 * @param types
	 * @param qualifyTemplateParameters
	 * @return
	 * @throws CoreException
	 * @throws DOMException
	 */
	private static String getTemplateArgString(IType[] types, boolean qualifyTemplateParameters) throws CoreException, DOMException {
		StringBuffer buffer = new StringBuffer();
		buffer.append('<');
		for (int i = 0; i < types.length; i++) {
			if (i>0) {
				buffer.append(',');
			}
			if (qualifyTemplateParameters && types[i] instanceof ICPPTemplateParameter) {
				ICPPBinding parent = null;
				if (types[i] instanceof IIndexInternalTemplateParameter) {
					parent = ((IIndexInternalTemplateParameter)types[i]).getParameterOwner();
				} else {
					IName parentName = ((ICPPTemplateParameter)types[i]).getScope().getScopeName();
					if (parentName instanceof IASTName) {
						parent = (ICPPBinding)((IASTName)parentName).resolveBinding();
					}
				}
				//identical template parameters from different templates must have unique signatures
				if (parent != null) {
					buffer.append(CPPVisitor.renderQualifiedName(parent.getQualifiedName()));
					String sig = getSignature(parent);
					if (sig != null)
						buffer.append(sig);
					buffer.append("::"); //$NON-NLS-1$
				}
				buffer.append(((ICPPTemplateParameter)types[i]).getName());
			} else {
				buffer.append(ASTTypeUtil.getType(types[i]));
			}
		}
		buffer.append('>');
		return buffer.toString();
	}
	
	/**
	 * Constructs a string in the format:
	 *   (paramName1,paramName2,...)
	 * 
	 * @param fType
	 * @return
	 * @throws DOMException
	 */
	private static String getFunctionParameterString(IFunctionType fType) throws DOMException {
		IType[] types = fType.getParameterTypes();
		if(types.length==1) {
			if(types[0] instanceof IBasicType) {
				if(((IBasicType)types[0]).getType()==IBasicType.t_void) {
					types = new IType[0];
				}
			}
		}
		StringBuffer result = new StringBuffer();
		result.append('(');
		for(int i=0; i<types.length; i++) {
			if (i>0) {
				result.append(',');
			}
			result.append(ASTTypeUtil.getType(types[i]));
		}
		result.append(')');
		return result.toString();
	}
	
	/**
	 * Gets the signature memento for the passed binding.
	 * 
	 * @param binding
	 * @return the hash code of the binding's signature string
	 * @throws CoreException
	 * @throws DOMException
	 */
	public static Integer getSignatureMemento(IBinding binding) throws CoreException, DOMException {
		String sig = getSignature(binding);
		return sig.length() == 0 ? null : new Integer(sig.hashCode());
	}
}
