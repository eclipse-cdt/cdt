/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * The specialization of a friend function in the context of a class specialization,
 * also used as base class for function instances.
 */
public class CPPFunctionSpecialization extends CPPSpecialization
		implements ICPPFunctionSpecialization, ICPPInternalFunction {
	private final ICPPFunctionType fType;
	private ICPPParameter[] fParams;
	private final IType[] fExceptionSpecs;

	public CPPFunctionSpecialization(ICPPFunction orig, IBinding owner, ICPPTemplateParameterMap argMap,
			ICPPFunctionType type, IType[] exceptionSpecs) {
		super(orig, owner, argMap);
		fType = type;
		fExceptionSpecs = exceptionSpecs;
	}

	private ICPPFunction getFunction() {
		return (ICPPFunction) getSpecializedBinding();
	}

	public void setParameters(ICPPParameter[] params) {
		assert fParams == null;
		fParams = params;
	}

	@Override
	public ICPPParameter[] getParameters() {
		return fParams;
	}

	@Override
	public int getRequiredArgumentCount() {
		return CPPFunction.getRequiredArgumentCount(getParameters());
	}

	@Override
	public boolean hasParameterPack() {
		return CPPFunction.hasParameterPack(getParameters());
	}

	@Override
	public IScope getFunctionScope() {
		return null;
	}

	@Override
	public ICPPFunctionType getDeclaredType() {
		return fType;
	}

	@Override
	public ICPPFunctionType getType() {
		return fType;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isInline() {
		if (getDefinition() != null) {
			IASTNode def = getDefinition();
			while (!(def instanceof IASTFunctionDefinition))
				def = def.getParent();
			return ((IASTFunctionDefinition) def).getDeclSpecifier().isInline();
		}
		return getFunction().isInline();
	}

	@Override
	public boolean isExternC() {
		if (CPPVisitor.isExternC(getDefinition())) {
			return true;
		}
		return getFunction().isExternC();
	}

	@Override
	public boolean isStatic() {
		return isStatic(true);
	}

	@Override
	public boolean isStatic(boolean resolveAll) {
		//TODO resolveAll
		IBinding f = getSpecializedBinding();
		if (f instanceof ICPPInternalFunction)
			return ((ICPPInternalFunction) f).isStatic(resolveAll);
		if (f instanceof IIndexBinding && f instanceof ICPPFunction) {
			return ((ICPPFunction) f).isStatic();
		}
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_static);
	}

	@Override
	public boolean isExtern() {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isExtern();
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_extern);
	}

	@Override
	public boolean isAuto() {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isAuto();
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_auto);
	}

	@Override
	public boolean isConstexpr() {
		IASTNode def = getDefinition();
		if (def != null) {
			ICPPASTFunctionDefinition functionDefinition = CPPFunction.getFunctionDefinition(def);
			return ((ICPPASTDeclSpecifier) functionDefinition.getDeclSpecifier()).isConstexpr();
		}
		IBinding f = getSpecializedBinding();
		if (f instanceof ICPPFunction) {
			return ((ICPPFunction) f).isConstexpr();
		}
		return false;
	}

	@Override
	public boolean isDeleted() {
		IASTNode def = getDefinition();
		if (def != null)
			return CPPFunction.isDeletedDefinition(def);

		IBinding f = getSpecializedBinding();
		if (f instanceof ICPPFunction) {
			return ((ICPPFunction) f).isDeleted();
		}
		return false;
	}

	@Override
	public boolean isRegister() {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isRegister();
		return CPPFunction.hasStorageClass(this, IASTDeclSpecifier.sc_register);
	}

	@Override
	public boolean takesVarArgs() {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.takesVarArgs();

		ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) getDefinition();
		if (dtor != null) {
			return dtor.takesVarArgs();
		}
		ICPPASTFunctionDeclarator[] ds = (ICPPASTFunctionDeclarator[]) getDeclarations();
		if (ds != null && ds.length > 0) {
			return ds[0].takesVarArgs();
		}
		return false;
	}

	@Override
	public boolean isNoReturn() {
		ICPPFunction f = (ICPPFunction) getSpecializedBinding();
		if (f != null)
			return f.isNoReturn();
		return false;
	}

	@Override
	public IBinding resolveParameter(CPPParameter param) {
		int pos = param.getParameterPosition();

		final IASTNode[] decls = getDeclarations();
		int tdeclLen = decls == null ? 0 : decls.length;
		for (int i = -1; i < tdeclLen; i++) {
			ICPPASTFunctionDeclarator tdecl;
			if (i == -1) {
				tdecl = (ICPPASTFunctionDeclarator) getDefinition();
				if (tdecl == null)
					continue;
			} else if (decls != null) {
				tdecl = (ICPPASTFunctionDeclarator) decls[i];
				if (tdecl == null)
					break;
			} else {
				break;
			}

			IASTParameterDeclaration[] params = tdecl.getParameters();
			if (pos < params.length) {
				final IASTName oName = getParamName(params[pos]);
				return oName.resolvePreBinding();
			}
		}
		return param;
	}

	protected void updateFunctionParameterBindings(ICPPASTFunctionDeclarator fdtor) {
		IASTParameterDeclaration[] updateParams = fdtor.getParameters();

		int k = 0;
		final IASTNode[] decls = getDeclarations();
		int tdeclLen = decls == null ? 0 : decls.length;
		for (int i = -1; i < tdeclLen && k < updateParams.length; i++) {
			ICPPASTFunctionDeclarator tdecl;
			if (i == -1) {
				tdecl = (ICPPASTFunctionDeclarator) getDefinition();
				if (tdecl == null)
					continue;
			} else if (decls != null) {
				tdecl = (ICPPASTFunctionDeclarator) decls[i];
				if (tdecl == null)
					break;
			} else {
				break;
			}

			IASTParameterDeclaration[] params = tdecl.getParameters();
			int end = Math.min(params.length, updateParams.length);
			for (; k < end; k++) {
				final IASTName oName = getParamName(params[k]);
				IBinding b = oName.resolvePreBinding();
				IASTName n = getParamName(updateParams[k]);
				n.setBinding(b);
				ASTInternal.addDeclaration(b, n);
			}
		}
	}

	private IASTName getParamName(final IASTParameterDeclaration paramDecl) {
		return ASTQueries.findInnermostDeclarator(paramDecl.getDeclarator()).getName();
	}

	private ICPPASTFunctionDeclarator extractFunctionDtor(IASTNode node) {
		if (node instanceof IASTName)
			node = node.getParent();
		if (!(node instanceof IASTDeclarator))
			return null;
		node = ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) node);
		if (!(node instanceof ICPPASTFunctionDeclarator))
			return null;

		return (ICPPASTFunctionDeclarator) node;
	}

	@Override
	public void addDefinition(IASTNode node) {
		ICPPASTFunctionDeclarator dtor = extractFunctionDtor(node);
		if (dtor != null) {
			updateFunctionParameterBindings(dtor);
			super.addDefinition(dtor);
		}
	}

	@Override
	public void addDeclaration(IASTNode node) {
		ICPPASTFunctionDeclarator dtor = extractFunctionDtor(node);
		if (dtor != null) {
			updateFunctionParameterBindings(dtor);
			super.addDeclaration(dtor);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		IFunctionType t = getType();
		result.append(t != null ? ASTTypeUtil.getParameterTypeString(t) : "()"); //$NON-NLS-1$
		ICPPTemplateParameterMap tpmap = getTemplateParameterMap();
		if (tpmap != null) {
			result.append(" "); //$NON-NLS-1$
			result.append(tpmap.toString());
		}
		return result.toString();
	}

	@Override
	public IType[] getExceptionSpecification() {
		return fExceptionSpecs;
	}

	@Override
	public ICPPExecution getFunctionBodyExecution() {
		if (!isConstexpr()) {
			return null;
		}

		IASTNode def = getDefinition();
		if (def != null) {
			return CPPFunction.computeFunctionBodyExecution(def);
		}
		return CPPTemplates.instantiateFunctionBody(this);
	}
}
