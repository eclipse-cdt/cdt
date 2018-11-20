/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.core.resources.IProject;

@SuppressWarnings("restriction")
public class ASTUtil {

	/**
	 * A convenience method to find the project that contains the given node.  Returns null if
	 * the project cannot be found.
	 */
	public static IProject getProject(IASTNode node) {
		IASTTranslationUnit astTU = node.getTranslationUnit();
		if (astTU == null)
			return null;

		ITranslationUnit tu = astTU.getOriginatingTranslationUnit();
		if (tu == null)
			return null;

		ICProject cProject = tu.getCProject();
		if (cProject == null)
			return null;

		return cProject.getProject();
	}

	/**
	 * Return the fully qualified name of the binding for the given name.  Returns null
	 * if the name has no binding.  Tries to resolve the binding if needed.
	 */
	public static String getFullyQualifiedName(IASTName name) {
		return getFullyQualifiedName(name.resolveBinding());
	}

	/**
	 * Return the fully qualified name of the given binding.  Returns null if there
	 * is no binding.
	 */
	public static String getFullyQualifiedName(IBinding binding) {
		if (binding == null)
			return null;
		if (binding instanceof ICPPBinding)
			try {
				return getFullyQualifiedName(((ICPPBinding) binding).getQualifiedName());
			} catch (DOMException e) {
				Activator.log(e);
				return null;
			}

		String ownerName = getFullyQualifiedName(binding.getOwner());
		return (ownerName == null ? "" : ownerName) + "::" + binding.getName();
	}

	/**
	 * Create and return a string representation of the fully qualified name in the
	 * input array's elements.
	 */
	public static String getFullyQualifiedName(String[] qualName) {
		boolean first = true;
		StringBuilder str = new StringBuilder();
		for (String name : qualName) {
			if (first)
				first = false;
			else
				str.append("::");
			str.append(name);
		}
		return str.toString();
	}

	// NOTE: This expression allows embedded line terminators (?s) for cases where the code looks like:
	// QObject::connect( &a, SIGNAL(
	//					sig1(
	//						int
	//					), ...
	// The regex trims leading and trailing whitespace within the expansion parameter.  This is needed
	// so that the start of the capture group provides the proper offset into the expansion.
	public static final Pattern Regex_MacroExpansion = Pattern
			.compile("(?s)([_a-zA-Z]\\w*)\\s*\\(\\s*(.*?)\\s*\\)\\s*");

	public static IType getBaseType(IType type) {
		while (type instanceof ITypeContainer)
			type = ((ITypeContainer) type).getType();
		return type;
	}

	public static IType getBaseType(IASTNode node) {
		if (node instanceof IASTIdExpression)
			return getBaseType((IASTIdExpression) node);
		if (node instanceof IASTFunctionCallExpression)
			return getBaseType((IASTFunctionCallExpression) node);
		if (node instanceof IASTExpression)
			return getBaseType(((IASTExpression) node).getExpressionType());

		return null;
	}

	public static IType getBaseType(IASTInitializerClause init) {
		if (!(init instanceof ICPPASTInitializerClause))
			return null;

		ICPPASTInitializerClause cppInit = (ICPPASTInitializerClause) init;
		ICPPEvaluation eval = cppInit.getEvaluation();
		try {
			CPPSemantics.pushLookupPoint(cppInit);
			return eval == null ? null : getBaseType(eval.getType());
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	public static ICPPClassType getReceiverType(IASTFunctionCallExpression fncall) {
		// If the expression is calling a member function then find the type of the
		// receiver.
		IASTExpression fnName = fncall.getFunctionNameExpression();
		if (fnName instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) fnName;
			ICPPASTExpression receiver = fieldRef.getFieldOwner();

			IType recvType = getBaseType(receiver);
			if (recvType instanceof ICPPClassType)
				return (ICPPClassType) recvType;
		}

		// Otherwise check for a call to implicit 'this'.  See details in the thread that
		// starts at http://dev.eclipse.org/mhonarc/lists/cdt-dev/msg26972.html
		try {
			for (IScope scope = CPPVisitor.getContainingScope(fncall); scope != null; scope = scope.getParent())
				if (scope instanceof ICPPClassScope)
					return ((ICPPClassScope) scope).getClassType();
		} catch (DOMException e) {
			Activator.log(e);
		}

		return null;
	}

	/**
	 * Does not return null.
	 */
	public static Collection<IQMethod> findMethods(IQObject qobj, QtMethodReference ref) {
		Set<IQMethod> bindings = new LinkedHashSet<>();

		Iterable<IQMethod> methods = null;
		switch (ref.getType()) {
		case Signal:
			methods = qobj.getSignals().withoutOverrides();
			break;
		case Slot:
			methods = qobj.getSlots().withoutOverrides();
			break;
		}

		if (methods != null) {
			String qtNormalizedSig = QtMethodUtil.getQtNormalizedMethodSignature(ref.getRawSignature());
			if (qtNormalizedSig == null)
				return bindings;

			for (IQMethod method : methods)
				for (String signature : method.getSignatures())
					if (signature.equals(qtNormalizedSig))
						bindings.add(method);
		}
		return bindings;
	}

	public static <T extends IBinding> T resolveFunctionBinding(Class<T> cls, IASTFunctionCallExpression fnCall) {
		IASTName fnName = null;
		IASTExpression fnNameExpr = fnCall.getFunctionNameExpression();
		if (fnNameExpr instanceof IASTIdExpression)
			fnName = ((IASTIdExpression) fnNameExpr).getName();
		else if (fnNameExpr instanceof ICPPASTFieldReference)
			fnName = ((ICPPASTFieldReference) fnNameExpr).getFieldName();

		IBinding binding = fnName == null ? null : fnName.resolveBinding();
		if (binding == null)
			return null;

		return cls.isAssignableFrom(binding.getClass()) ? cls.cast(binding) : null;
	}

	public static ICPPASTVisibilityLabel findVisibilityLabel(ICPPMethod method, IASTNode ast) {
		// the visibility cannot be found without an ast
		if (ast == null)
			return null;

		// We need to get the CompTypeSpec in order to see the token that created the method's
		// visibility specifier.  The ast parameter will be either the method definition or a
		// declaration.  If it happens to be a declaration, then the CompTypeSpec is a parent of
		// the AST and it can be accessed through public API.  However, if the ast parameter happens
		// to be a definition, then there isn't any public API (that I've found) to get to the
		// CompTypeSpec.  Instead, we cheat and use the InternalBinding.

		MethodSpec methodSpec = new MethodSpec(ast);
		if (methodSpec.clsSpec == null && method instanceof ICPPInternalBinding) {
			ICPPInternalBinding internalBinding = (ICPPInternalBinding) method;
			IASTNode[] decls = internalBinding.getDeclarations();
			for (int i = 0; methodSpec.clsSpec == null && i < decls.length; ++i)
				methodSpec = new MethodSpec(decls[i]);
		}

		if (methodSpec.clsSpec == null)
			return null;

		ICPPASTVisibilityLabel lastLabel = null;
		for (IASTDeclaration decl : methodSpec.clsSpec.getMembers()) {
			if (decl instanceof ICPPASTVisibilityLabel)
				lastLabel = (ICPPASTVisibilityLabel) decl;
			else if (decl == methodSpec.methodDecl)
				return lastLabel;
		}

		return null;
	}

	private static class MethodSpec {
		public final ICPPASTCompositeTypeSpecifier clsSpec;
		public final IASTNode methodDecl;

		public MethodSpec(IASTNode node) {
			ICPPASTCompositeTypeSpecifier cls = null;
			IASTNode mth = node;
			while (mth != null && cls == null) {
				IASTNode parent = mth.getParent();
				if (parent instanceof ICPPASTCompositeTypeSpecifier)
					cls = (ICPPASTCompositeTypeSpecifier) parent;
				else
					mth = parent;
			}

			clsSpec = cls;
			methodDecl = mth;
		}
	}
}
