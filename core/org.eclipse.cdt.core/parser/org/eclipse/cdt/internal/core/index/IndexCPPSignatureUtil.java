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
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownMemberClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * Determines the signatures and signature hashes for bindings that can have
 * siblings with the same name.
 */
public class IndexCPPSignatureUtil {
	/**
	 * Returns the signature for the binding.  Returns an empty string if
	 * a signature is not required for the binding.
	 *
	 * @param binding the binding
	 * @return the signature or an empty string
	 */
	public static String getSignature(IBinding binding) throws CoreException, DOMException {
		StringBuilder buffer = new StringBuilder();
		if (binding instanceof ICPPDeferredClassInstance) {
			buffer.append(getSignature(((ICPPDeferredClassInstance) binding).getTemplateDefinition()));
		}
		if (binding instanceof ICPPTemplateInstance) {
			ICPPTemplateInstance inst = (ICPPTemplateInstance) binding;
			buffer.append(getTemplateArgString(inst.getTemplateArguments(), true));
		} else if (binding instanceof ICPPUnknownMemberClassInstance) {
			ICPPUnknownMemberClassInstance inst = (ICPPUnknownMemberClassInstance) binding;
			buffer.append(getTemplateArgString(inst.getArguments(), true));
		} else if (binding instanceof ICPPClassTemplatePartialSpecialization) {
			ICPPClassTemplatePartialSpecialization partial = (ICPPClassTemplatePartialSpecialization) binding;
			buffer.append(getTemplateArgString(partial.getTemplateArguments(), false));
		}

		if (binding instanceof ICPPFunction) {
			IFunction function = (ICPPFunction) binding;
			final IFunctionType ftype = function.getType();
			buffer.append(getFunctionParameterString(ftype));
			if (binding instanceof ICPPTemplateDefinition) {
				ICPPTemplateDefinition tdef = (ICPPTemplateDefinition) binding;
				appendTemplateParameters(tdef.getTemplateParameters(), buffer);
				ASTTypeUtil.appendType(ftype.getReturnType(), true, buffer);
			}
		}
		if (binding instanceof ICPPMethod && !(binding instanceof ICPPConstructor)) {
			ICPPFunctionType ft = ((ICPPMethod) binding).getType();
			if (ft.isConst())
				buffer.append('c');
			if (ft.isVolatile())
				buffer.append('v');
			if (ft.hasRefQualifier()) {
				buffer.append('&');
				if (ft.isRValueReference())
					buffer.append('&');
			}
		}

		return buffer.toString();
	}

	private static void appendTemplateParameters(ICPPTemplateParameter[] tpars, StringBuilder buffer) {
		buffer.append('<');
		for (ICPPTemplateParameter tpar : tpars) {
			appendTemplateParameter(tpar, buffer);
			buffer.append(',');
		}
		buffer.append('>');
	}

	private static void appendTemplateParameter(ICPPTemplateParameter tpar, StringBuilder buffer) {
		if (tpar instanceof ICPPTemplateNonTypeParameter) {
			ASTTypeUtil.appendType(((ICPPTemplateNonTypeParameter) tpar).getType(), true, buffer);
		} else if (tpar instanceof ICPPTemplateTypeParameter) {
			buffer.append('#');
		} else if (tpar instanceof ICPPTemplateTemplateParameter) {
			buffer.append('#');
			appendTemplateParameters(((ICPPTemplateTemplateParameter) tpar).getTemplateParameters(), buffer);
		}
		if (tpar.isParameterPack())
			buffer.append("..."); //$NON-NLS-1$
	}

	/**
	 * Constructs a string in the format:
	 *   <typeName1,typeName2,...>
	 */
	public static String getTemplateArgString(ICPPTemplateArgument[] args, boolean qualifyTemplateParameters)
			throws CoreException, DOMException {
		return ASTTypeUtil.getArgumentListString(args, true);
	}

	/**
	 * Constructs a string in the format:
	 *   (paramName1,paramName2,...)
	 */
	private static String getFunctionParameterString(IFunctionType functionType) throws DOMException {
		IType[] types = functionType.getParameterTypes();
		if (types.length == 1 && SemanticUtil.isVoidType(types[0])) {
			types = new IType[0];
		}
		StringBuilder result = new StringBuilder();
		result.append('(');
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				result.append(',');
			}
			ASTTypeUtil.appendType(types[i], true, result);
		}
		if (functionType instanceof ICPPFunctionType && ((ICPPFunctionType) functionType).takesVarArgs()) {
			if (types.length != 0) {
				result.append(',');
			}
			result.append("..."); //$NON-NLS-1$
		}
		result.append(')');
		return result.toString();
	}

	/**
	 * Returns the signature hash for the passed binding.
	 *
	 * @param binding the binding
	 * @return the hash code of the binding's signature string
	 */
	public static Integer getSignatureHash(IBinding binding) throws CoreException, DOMException {
		String sig = getSignature(binding);
		return sig.length() == 0 ? null : Integer.valueOf(sig.hashCode());
	}

	/**
	 * Compares signature hashes of the two given bindings.
	 *
	 * @param a the first binding
	 * @param b the second binding
	 * @return sgn(signature_hash(a) - signature_hash(b))
	 */
	public static int compareSignatures(IBinding a, IBinding b) {
		try {
			int siga = getSignature(a).hashCode();
			int sigb = getSignature(b).hashCode();
			return siga < sigb ? -1 : siga > sigb ? 1 : 0;
		} catch (CoreException | DOMException e) {
			CCorePlugin.log(e);
		}
		return 0;
	}
}
