/*******************************************************************************
 * Copyright (c) 2005, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionPrototypeScope;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.services.IDisposable;

/**
 * Used for refactoring to cache the IASTTranslationUnits.
 * Contains a collection of methods operating on ASTNodes.
 * The object has to be disposed of after use.
 */
public class ASTManager implements IDisposable {
	private static final int PARSE_MODE = ITranslationUnit.AST_SKIP_ALL_HEADERS
			| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
			| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
			| ITranslationUnit.AST_PARSE_INACTIVE_CODE;

	public final static int TRUE = 1;
	public final static int FALSE = 0;
	public final static int UNKNOWN = -1;

	// TODO(sprigogin): Replace fSharedAST and fTranslationUnits with CRefactoringContext.
	private IASTTranslationUnit fSharedAST;
	private final Map<IFile, IASTTranslationUnit> fTranslationUnits = new HashMap<>();
	private final Set<String> fProblemUnits = new HashSet<>();
	private final CRefactoringArgument fArgument;
	private IBinding[] fValidBindings;
	private String fRenameTo;
	private HashMap<IBinding, Integer> fKnownBindings;
	private HashSet<IBinding> fConflictingBinding;
	private boolean fDisposed;

	public static String nth_of_m(int n, int m) {
		StringBuilder nofm = new StringBuilder();
		append_nth_of_m(n, m, nofm);
		return nofm.toString();
	}

	static void append_nth_of_m(int n, int m, StringBuilder buf) {
		buf.append(n);
		switch (n) {
		case 1:
			buf.append("st"); //$NON-NLS-1$
			break;
		case 2:
			buf.append("nd"); //$NON-NLS-1$
			break;
		case 3:
			buf.append("rd"); //$NON-NLS-1$
			break;
		default:
			buf.append("th"); //$NON-NLS-1$
			break;
		}
		buf.append(" of "); //$NON-NLS-1$
		buf.append(m);
	}

	public static IASTFileLocation getLocationInTranslationUnit(IASTNode node) {
		return node.getFileLocation();
	}

	public static IASTName getSimpleName(IASTName name) {
		if (name instanceof ICPPASTQualifiedName) {
			if (name.getLastName() != null) {
				name = name.getLastName();
			}
		}
		return name;
	}

	/**
	 * Returns TRUE, FALSE or UNKNOWN.
	 * @throws DOMException
	 */
	public static int isSameBinding(IIndex index, IBinding b1, IBinding b2) throws DOMException {
		if (b1 == null || b2 == null) {
			return UNKNOWN;
		}
		if (b1.equals(b2)) {
			return TRUE;
		}
		if (b1 instanceof IIndexBinding || b2 instanceof IIndexBinding) {
			if (index != null) {
				IIndexBinding b11 = index.adaptBinding(b1);
				if (b11 != null)
					b1 = b11;
				IIndexBinding b21 = index.adaptBinding(b2);
				if (b21 != null)
					b2 = b21;
				if (b1.equals(b2))
					return TRUE;
			}
		}

		String n1 = b1.getName();
		String n2 = b2.getName();
		if (n1 == null || n2 == null) {
			return UNKNOWN;
		}
		if (!n1.equals(n2)) {
			return FALSE;
		}
		if (b1 instanceof ICompositeType) {
			if (!(b2 instanceof ICompositeType)) {
				return FALSE;
			}
			ICompositeType c1 = (ICompositeType) b1;
			ICompositeType c2 = (ICompositeType) b2;
			if (c1.getKey() != c2.getKey()) {
				return FALSE;
			}
			IScope s1 = c1.getCompositeScope();
			if (s1 != null)
				s1 = s1.getParent();
			IScope s2 = c2.getCompositeScope();
			if (s2 != null)
				s2 = s2.getParent();
			return isSameScope(s1, s2, false);
		}

		if (b1 instanceof IFunction) {
			if (!(b2 instanceof IFunction)) {
				return FALSE;
			}
			boolean isStatic = false;
			boolean checkSig = true;
			IFunction c1 = (IFunction) b1;
			IFunction c2 = (IFunction) b2;
			if (b1 instanceof ICPPMethod) {
				if (!(b2 instanceof ICPPMethod)) {
					return FALSE;
				}
			} else {
				if (b2 instanceof ICPPMethod) {
					return FALSE;
				}
				isStatic = c1.isStatic() || c2.isStatic();
				if ((!(b1 instanceof ICPPFunction) || ((ICPPFunction) b1).isExternC())
						&& (!(b2 instanceof ICPPFunction) || ((ICPPFunction) b2).isExternC())) {
					checkSig = false;
				}
			}

			int r1 = isSameScope(b1.getScope(), b2.getScope(), isStatic);
			if (r1 == FALSE) {
				return FALSE;
			}

			int r2 = checkSig ? hasSameSignature(c1, c2) : TRUE;
			if (r2 == FALSE) {
				return FALSE;
			}
			if (r1 != r2) {
				return UNKNOWN;
			}
			return r1;
		}

		if (b1 instanceof IVariable) {
			boolean fileStatic = false;
			if (!(b2 instanceof IVariable)) {
				return FALSE;
			}

			IVariable c1 = (IVariable) b1;
			IVariable c2 = (IVariable) b2;
			if (b1 instanceof IField) {
				if (!(b2 instanceof IField)) {
					return FALSE;
				}
			} else if (b1 instanceof IParameter) {
				if (!(b2 instanceof IParameter)) {
					return FALSE;
				}
			} else {
				if (b2 instanceof IField || b2 instanceof IParameter) {
					return FALSE;
				}
				fileStatic = c1.isStatic() || c2.isStatic();
			}
			int result = isSameScope(c1.getScope(), c2.getScope(), fileStatic);
			return result == UNKNOWN ? TRUE : result;
		}

		if (b1 instanceof IEnumerator) {
			if (!(b2 instanceof IEnumerator)) {
				return FALSE;
			}
			return isSameScope(b1.getScope(), b2.getScope(), false);
		}

		if (b1 instanceof ITypedef) {
			if (!(b2 instanceof ITypedef)) {
				return FALSE;
			}
			return isSameScope(b1.getScope(), b2.getScope(), false);
		}

		if (b1 instanceof IMacroBinding) {
			if (!(b2 instanceof IMacroBinding)) {
				return FALSE;
			}
			return TRUE;
		}
		if (b1 instanceof IEnumeration) {
			if (!(b2 instanceof IEnumeration)) {
				return FALSE;
			}
			return isSameScope(b1.getScope(), b2.getScope(), false);
		}
		int scopeCmp = isSameScope(b1.getScope(), b2.getScope(), false);
		if (scopeCmp != TRUE) {
			return scopeCmp;
		}

		if (b1.getClass().equals(b2.getClass())) {
			return TRUE;
		}
		return UNKNOWN;
	}

	public static int isSameScope(IScope s1, IScope s2, boolean fileStatic) throws DOMException {
		if (s1 == s2) {
			return TRUE;
		}
		IASTNode node1 = ASTInternal.getPhysicalNodeOfScope(s1);
		IASTNode node2 = ASTInternal.getPhysicalNodeOfScope(s2);

		// Forward declarations do not have parent scopes.
		if (s1 == null) {
			if (!fileStatic && node2 instanceof IASTTranslationUnit) {
				return TRUE;
			}
			return UNKNOWN;
		}
		if (s2 == null) {
			if (!fileStatic && node1 instanceof IASTTranslationUnit) {
				return TRUE;
			}
			return UNKNOWN;
		}

		if (s1.equals(s2)) {
			return TRUE;
		}

		if (node1 instanceof IASTTranslationUnit && node2 instanceof IASTTranslationUnit) {
			return hasSameLocation(node1, node2, fileStatic);
		}

		if (s1.getKind() == EScopeKind.eGlobal && s2.getKind() == EScopeKind.eGlobal)
			return TRUE;

		if (s1 instanceof ICPPBlockScope) {
			if (s2 instanceof ICPPBlockScope) {
				return hasSameLocation(node1, node2, fileStatic);
			}
			return FALSE;
		}

		String name1 = getName(s1);
		String name2 = getName(s2);
		if (s1 instanceof ICPPNamespaceScope) {
			if (s2 instanceof ICPPNamespaceScope) {
				ICPPNamespaceScope n1 = (ICPPNamespaceScope) s1;
				ICPPNamespaceScope n2 = (ICPPNamespaceScope) s2;
				int r1 = hasSameLocation(node1, node2, fileStatic);
				if (r1 == TRUE) {
					return r1;
				}
				if (name1 == null || name2 == null || !name1.equals(name2)) {
					return FALSE;
				}
				return isSameScope(n1.getParent(), n2.getParent(), fileStatic);
			}
			return FALSE;
		}

		if (name1 != null && name2 != null && !name1.equals(name2)) {
			return FALSE;
		}

		// Classes.
		if (s1 instanceof ICPPClassScope || s1 instanceof ICCompositeTypeScope) {
			if (s2 instanceof ICPPClassScope || s2 instanceof ICCompositeTypeScope) {
				return isSameScope(s1.getParent(), s2.getParent(), fileStatic);
			}
			return FALSE;
		}
		// Functions.
		if (s1 instanceof ICPPFunctionScope) {
			if (s2 instanceof ICPPFunctionScope) {
				return hasSameLocation(node1, node2, true);
			}
			return FALSE;
		}
		if (s1 instanceof ICFunctionScope || s1 instanceof ICFunctionPrototypeScope || s1 instanceof ICScope) {
			if (s2 instanceof ICFunctionScope || s2 instanceof ICFunctionPrototypeScope || s2 instanceof ICScope) {
				return hasSameLocation(node1, node2, true);
			}
			return FALSE;
		}

		return isSameScope(s1.getParent(), s2.getParent(), fileStatic);
	}

	public static String getName(IScope scope) {
		String name = null;
		if (scope instanceof IIndexScope) {
			IIndexScope indexScope = (IIndexScope) scope;
			final IIndexName scopeName = indexScope.getScopeName();
			if (scopeName != null) {
				name = scopeName.toString();
			}
		} else {
			name = getNameOrNull(ASTInternal.getPhysicalNodeOfScope(scope));
		}
		return name;
	}

	public static int hasSameSignature(IFunction f1, IFunction f2) throws DOMException {
		if (f1.takesVarArgs() != f2.takesVarArgs())
			return FALSE;

		if (f1 instanceof ICPPMethod != f2 instanceof ICPPMethod)
			return FALSE;

		return hasSameSignature(f1.getType(), f2.getType());
	}

	public static int hasSameSignature(IFunctionType t1, IFunctionType t2) throws DOMException {
		if (t1 instanceof ICPPFunctionType && t2 instanceof ICPPFunctionType) {
			ICPPFunctionType cppt1 = (ICPPFunctionType) t1;
			ICPPFunctionType cppt2 = (ICPPFunctionType) t2;
			if (cppt1.isConst() != cppt2.isConst())
				return FALSE;
			if (cppt1.isVolatile() != cppt2.isVolatile())
				return FALSE;
		}
		return isSameParameterList(t1.getParameterTypes(), t2.getParameterTypes());
	}

	private static int isSameParameterList(IType[] p1, IType[] p2) throws DOMException {
		if (p1 == p2) {
			return TRUE;
		}
		if (p1 == null || p2 == null) {
			return UNKNOWN;
		}
		if (p1.length != p2.length) {
			return FALSE;
		}
		int retval = TRUE;
		for (int i = 0; i < p2.length; i++) {
			switch (isSameType(p1[i], p2[i])) {
			case FALSE:
				return FALSE;
			case UNKNOWN:
				retval = UNKNOWN;
				break;
			}
		}

		return retval;
	}

	private static int isSameType(IType t1, IType t2) throws DOMException {
		if (t1 != null && t2 != null && t1.isSameType(t2)) {
			return TRUE;
		}
		t1 = getRealType(t1);
		t2 = getRealType(t2);
		if (t1 == t2) {
			return TRUE;
		}
		if (t1 == null || t2 == null || t1 instanceof ISemanticProblem || t2 instanceof ISemanticProblem) {
			return UNKNOWN;
		}

		if (t1 instanceof IArrayType) {
			if (t2 instanceof IArrayType) {
				IArrayType a1 = (IArrayType) t1;
				IArrayType a2 = (IArrayType) t2;
				return isSameType(a1.getType(), a2.getType());
			}
			return FALSE;
		}

		if (t1 instanceof IBasicType) {
			if (t2 instanceof IBasicType) {
				IBasicType a1 = (IBasicType) t1;
				IBasicType a2 = (IBasicType) t2;
				if (a1.getKind() != a2.getKind()) {
					return FALSE;
				}
				if (getSigned(a1) != getSigned(a2) || a1.isUnsigned() != a2.isUnsigned()) {
					return FALSE;
				}
				if (a1.isLong() != a2.isLong() || a1.isShort() != a2.isShort()) {
					return FALSE;
				}
				return TRUE;
			}
			return FALSE;
		}

		if (t1 instanceof ICompositeType) {
			if (t2 instanceof ICompositeType) {
				ICompositeType a1 = (ICompositeType) t1;
				ICompositeType a2 = (ICompositeType) t2;
				if (a1.getKey() != a2.getKey()) {
					return FALSE;
				}
				return isSameScope(a1.getCompositeScope(), a2.getCompositeScope(), false);
			}
			return FALSE;
		}

		if (t1 instanceof ICPPReferenceType) {
			if (t2 instanceof ICPPReferenceType) {
				ICPPReferenceType a1 = (ICPPReferenceType) t1;
				ICPPReferenceType a2 = (ICPPReferenceType) t2;
				return isSameType(a1.getType(), a2.getType());
			}
			return FALSE;
		}

		if (t1 instanceof ICPPTemplateTypeParameter) {
			if (t2 instanceof ICPPTemplateTypeParameter) {
				return TRUE;
			}
			return FALSE;
		}

		if (t1 instanceof IEnumeration) {
			if (t2 instanceof IEnumeration) {
				IEnumeration a1 = (IEnumeration) t1;
				IEnumeration a2 = (IEnumeration) t2;

				return isSameScope(a1.getScope(), a2.getScope(), false);
			}
			return FALSE;
		}

		if (t1 instanceof IFunctionType) {
			if (t2 instanceof IFunctionType) {
				IFunctionType a1 = (IFunctionType) t1;
				IFunctionType a2 = (IFunctionType) t2;
				return hasSameSignature(a1, a2);
			}
			return FALSE;
		}

		if (t1 instanceof IPointerType) {
			if (t2 instanceof IPointerType) {
				IPointerType a1 = (IPointerType) t1;
				IPointerType a2 = (IPointerType) t2;
				if (a1.isConst() != a2.isConst() || a1.isVolatile() != a2.isVolatile()
						|| a1.isRestrict() != a2.isRestrict()) {
					return FALSE;
				}
				return isSameType(a1.getType(), a2.getType());
			}
			return FALSE;
		}

		if (t1 instanceof IQualifierType) {
			if (t2 instanceof IQualifierType) {
				IQualifierType a1 = (IQualifierType) t1;
				IQualifierType a2 = (IQualifierType) t2;
				if (a1.isConst() != a2.isConst() || a1.isVolatile() != a2.isVolatile()) {
					return FALSE;
				}
				return isSameType(a1.getType(), a2.getType());
			}
			return FALSE;
		}

		return UNKNOWN;
	}

	private static boolean getSigned(IBasicType a2) {
		if (a2.isSigned()) {
			return true;
		}
		if (a2.isUnsigned()) {
			return false;
		}
		switch (a2.getKind()) {
		case eInt:
		case eUnspecified:
			return true;
		default:
			break;
		}
		return false;
	}

	private static IType getRealType(IType t) {
		while (t instanceof ITypedef) {
			t = ((ITypedef) t).getType();
		}
		return t;
	}

	private static String getNameOrNull(IASTNode node) {
		if (node instanceof IASTDeclarator) {
			return getSimpleName(((IASTDeclarator) node).getName()).toString();
		}
		if (node instanceof IASTNamedTypeSpecifier) {
			return getSimpleName(((IASTNamedTypeSpecifier) node).getName()).toString();
		}
		if (node instanceof IASTCompositeTypeSpecifier) {
			return getSimpleName(((IASTCompositeTypeSpecifier) node).getName()).toString();
		}
		if (node instanceof ICPPASTNamespaceDefinition) {
			return getSimpleName(((ICPPASTNamespaceDefinition) node).getName()).toString();
		}
		if (node instanceof IASTTranslationUnit) {
			return ((IASTTranslationUnit) node).getFilePath();
		}
		return null;
	}

	private static int hasSameLocation(IASTNode node1, IASTNode node2, boolean fileStatic) {
		if (node1 == null || node2 == null) {
			return UNKNOWN;
		}
		if (!fileStatic && node1 instanceof IASTTranslationUnit && node2 instanceof IASTTranslationUnit) {
			return TRUE;
		}

		IASTFileLocation l1 = node1.getNodeLocations()[0].asFileLocation();
		IASTFileLocation l2 = node2.getNodeLocations()[0].asFileLocation();
		if (l1 == null || l2 == null) {
			return UNKNOWN;
		}
		if (!l1.getFileName().equals(l2.getFileName())) {
			return FALSE;
		}
		if (l1.getNodeOffset() != l2.getNodeOffset()) {
			return FALSE;
		}
		if (l1.getNodeLength() != l2.getNodeLength()) {
			return FALSE;
		}
		return TRUE;
	}

	private static IScope getContainingScope(IASTName name) {
		IASTTranslationUnit tu = name.getTranslationUnit();
		if (tu == null) {
			return null;
		}
		if (tu instanceof ICPPASTTranslationUnit) {
			return CPPVisitor.getContainingScope(name);
		}
		return CVisitor.getContainingScope(name);
	}

	public static boolean isLocalVariable(IVariable v, IScope scope) {
		if (v instanceof IParameter) {
			return false;
		}
		while (scope != null) {
			if (scope instanceof ICPPFunctionScope || scope instanceof ICPPBlockScope
					|| scope instanceof ICFunctionScope) {
				return true;
			}
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
				scope = null;
			}
		}
		return false;
	}

	public static boolean isLocalVariable(IVariable v) {
		try {
			return isLocalVariable(v, v.getScope());
		} catch (DOMException e) {
			return false;
		}
	}

	public static IBinding[] findInScope(final IScope scope, String name, IASTTranslationUnit tu,
			boolean removeGlobalsWhenClassScope) throws DOMException {
		IBinding[] result = null;
		result = scope.find(name, tu);
		if (result == null || result.length == 0) {
			return result;
		}

		// eliminate global bindings when looking up in a class type
		if (removeGlobalsWhenClassScope && (scope instanceof ICPPClassScope || scope instanceof ICCompositeTypeScope)) {
			int count = 0;
			for (int i = 0; i < result.length; i++) {
				IBinding binding = result[i];
				IScope bscope = binding.getScope();
				if (!(bscope instanceof ICPPClassScope || bscope instanceof ICCompositeTypeScope)) {
					result[i] = null;
				} else {
					count++;
				}
			}
			if (count < result.length) {
				IBinding[] copy = new IBinding[count];
				int i = 0;
				for (IBinding b : result) {
					if (b != null) {
						copy[i++] = b;
					}
				}
				result = copy;
			}
		}

		// Try to find constructors.
		if (scope instanceof ICPPBlockScope) {
			for (int i = 0; i < result.length; i++) {
				IBinding binding = result[i];
				if (binding instanceof ICPPClassType) {
					ICPPClassType classType = (ICPPClassType) binding;
					if (classType.getKey() == ICPPClassType.k_class) {
						IBinding[] cons = classType.getConstructors();
						if (cons.length > 0 && !(cons[0] instanceof IProblemBinding)) {
							result[i] = cons[0];
						}
					}
				}
			}
		}

		return result;
	}

	public ASTManager(CRefactoringArgument arg) {
		fArgument = arg;
	}

	@Override
	public void dispose() {
		Assert.isTrue(!fDisposed, "ASTManager.dispose() called more than once"); //$NON-NLS-1$
		fDisposed = true;
		if (fSharedAST != null) {
			ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (!fDisposed)
			CUIPlugin.logError("ASTManager was not disposed"); //$NON-NLS-1$
		super.finalize();
	}

	void analyzeArgument(IIndex index, IProgressMonitor pm, RefactoringStatus status) {
		if (fArgument == null) {
			return;
		}

		if (fArgument.getArgumentKind() != CRefactory.ARGUMENT_UNKNOWN) {
			return;
		}

		if (fArgument.getSourceFile() == null)
			return;

		pm.beginTask(RenameMessages.ASTManager_task_analyze, 2);
		IASTTranslationUnit tu = getAST(index, fArgument.getSourceFile(), true, status);
		pm.worked(1);
		if (tu != null) {
			final IASTNodeSelector nodeSelector = tu.getNodeSelector(tu.getFilePath());
			final int offset = fArgument.getOffset();
			final int length = fArgument.getLength();
			IASTName name = nodeSelector.findEnclosingName(offset, length);
			if (name != null) {
				name = name.getLastName();
			} else {
				IASTNode node = nodeSelector.findEnclosingNode(offset, length);
				if (node instanceof IASTPreprocessorMacroDefinition || node instanceof IASTPreprocessorElifStatement
						|| node instanceof IASTPreprocessorIfdefStatement
						|| node instanceof IASTPreprocessorIfndefStatement
						|| node instanceof IASTPreprocessorIfStatement) {
					final IASTFileLocation fileLocation = node.getFileLocation();
					if (fileLocation != null) {
						final String ident = extractIdentifier(node.getRawSignature(),
								offset - fileLocation.getNodeOffset(), length);
						if (ident != null) {
							IASTPreprocessorMacroDefinition[] mdefs = tu.getMacroDefinitions();
							for (IASTPreprocessorMacroDefinition mdef : mdefs) {
								IASTName n = mdef.getName();
								if (ident.equals(n.toString())) {
									name = n;
									break;
								}
							}
						}
					}
				}
			}
			if (name != null) {
				fArgument.setName(name);
				IBinding binding = name.resolveBinding();
				if (binding != null) {
					IScope scope = null;
					try {
						scope = binding.getScope();
					} catch (DOMException e) {
						handleDOMException(tu, e, status);
					}
					fArgument.setBinding(name.getTranslationUnit(), binding, scope);
				}
			}
		}
		pm.worked(1);
		pm.done();
	}

	private String extractIdentifier(String rawSignature, int offset, int length) {
		char[] sig = rawSignature.toCharArray();
		int end = offset + length;
		if (offset < 0 || end > sig.length)
			return null;

		for (int i = offset; i < end; i++) {
			if (!Character.isJavaIdentifierPart(sig[i]))
				return null;
		}
		while (offset > 0) {
			if (!Character.isJavaIdentifierPart(sig[offset - 1]))
				break;
			offset--;
		}
		while (end < sig.length) {
			if (!Character.isJavaIdentifierPart(sig[end]))
				break;
			end++;
		}
		return rawSignature.substring(offset, end);
	}

	/**
	 * Returns an AST for the given file.
	 *
	 * @param index the index to use for the AST
	 * @param sourceFile the source file to obtain an AST for
	 * @param astStyle the style to pass to {@link ITranslationUnit#getAST(IIndex, int)} method.
	 *     If a previously cached AST is returned, the style is not guaranteed to match
	 *     the requested one.
	 * @param cacheIt if {@code true}, the AST will be cached for later reuse
	 * @return the requested AST or {@code null}
	 * @throws CoreException
	 */
	public synchronized IASTTranslationUnit getAST(IIndex index, IFile sourceFile, int astStyle, boolean cacheIt)
			throws CoreException {
		IASTTranslationUnit ast = fTranslationUnits.get(sourceFile);
		if (ast == null) {
			ICElement celem = CoreModel.getDefault().create(sourceFile);
			if (celem instanceof ITranslationUnit) {
				ITranslationUnit tu = CModelUtil.toWorkingCopy((ITranslationUnit) celem);
				if (fSharedAST != null && tu.equals(fSharedAST.getOriginatingTranslationUnit())) {
					ast = fSharedAST;
				} else {
					// Try to get a shared AST before creating our own.
					ast = ASTProvider.getASTProvider().acquireSharedAST(tu, index, ASTProvider.WAIT_ACTIVE_ONLY, null);
					if (ast != null) {
						if (fSharedAST != null) {
							ASTProvider.getASTProvider().releaseSharedAST(fSharedAST);
						}
						fSharedAST = ast;
					} else {
						ast = tu.getAST(index, astStyle);
						if (cacheIt) {
							fTranslationUnits.put(sourceFile, ast);
						}
					}
				}
			}
		}
		return ast;
	}

	private IASTTranslationUnit getAST(IIndex index, IFile sourceFile, boolean cacheIt, RefactoringStatus status) {
		try {
			return getAST(index, sourceFile, PARSE_MODE, cacheIt);
		} catch (CoreException e) {
			status.addError(e.getMessage());
			return null;
		}
	}

	public void analyzeTextMatches(IIndex index, Collection<CRefactoringMatch> matches, IProgressMonitor monitor,
			RefactoringStatus status) {
		CRefactoringMatchStore store = new CRefactoringMatchStore();
		for (CRefactoringMatch match : matches) {
			store.addMatch(match);
		}

		int count = store.getFileCount();
		String taskName = RenameMessages.ASTManager_task_generateAst;
		monitor.beginTask(taskName, 2 * count);
		monitor.setTaskName(taskName);

		List<IFile> files = store.getFileList();
		int cc = 0;
		long now = System.currentTimeMillis();
		long update = now;
		for (IFile file : files) {
			cc++;
			if (store.contains(file)) {
				if ((now = System.currentTimeMillis()) > update) {
					String nofm = nth_of_m(cc, count);
					String taskname = NLS.bind(RenameMessages.ASTManager_subtask_analyzing, nofm);
					monitor.subTask(taskname);
					update = now + 1000;
				}
				boolean doParse = false;
				Collection<CRefactoringMatch> fm = store.getMatchesForFile(file);
				for (Iterator<CRefactoringMatch> iterator = fm.iterator(); !doParse && iterator.hasNext();) {
					CRefactoringMatch match = iterator.next();
					switch (match.getLocation()) {
					case CRefactory.OPTION_IN_COMMENT:
					case CRefactory.OPTION_IN_INCLUDE_DIRECTIVE:
					case CRefactory.OPTION_IN_STRING_LITERAL:
						break;
					default:
						doParse = true;
					}
				}

				if (doParse) {
					IASTTranslationUnit tu = getAST(index, file, false, status);
					monitor.worked(1);
					analyzeTextMatchesOfTranslationUnit(tu, store, status);
					if (status.hasFatalError()) {
						return;
					}
					monitor.worked(1);
				} else {
					monitor.worked(2);
				}
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			} else {
				monitor.worked(2);
			}
		}
		monitor.done();
	}

	private void analyzeTextMatchesOfTranslationUnit(IASTTranslationUnit tu, final CRefactoringMatchStore store,
			final RefactoringStatus status) {
		fKnownBindings = new HashMap<>();
		fConflictingBinding = new HashSet<>();
		final Set<IPath> paths = new HashSet<>();
		boolean renamesMacro = fArgument.getArgumentKind() == CRefactory.ARGUMENT_MACRO;

		analyzeMacroMatches(tu, store, paths, status);
		if (status.hasFatalError())
			return;

		if (renamesMacro) {
			findConflictingBindingsWithNewName(tu, store, paths, status);
			if (status.hasFatalError())
				return;
		}

		analyzeLanguageMatches(tu, store, paths, status);
		if (status.hasFatalError())
			return;

		for (IPath path : paths) {
			if (path != null) {
				store.removePath(path);
			}
		}
		handleConflictingBindings(tu, status);
		fKnownBindings = null;
		fConflictingBinding = null;
	}

	private void analyzeLanguageMatches(IASTTranslationUnit tu, final CRefactoringMatchStore store,
			final Set<IPath> paths, final RefactoringStatus status) {
		ASTNameVisitor nv = new ASTSpecificNameVisitor(fArgument.getName()) {
			@Override
			protected int visitName(IASTName name, boolean isDestructor) {
				IPath path = analyzeAstMatch(name, store, isDestructor, status);
				paths.add(path);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};
		tu.accept(nv);
	}

	private void analyzeMacroMatches(IASTTranslationUnit tu, final CRefactoringMatchStore store,
			final Set<IPath> pathsVisited, final RefactoringStatus status) {
		String lookfor = fArgument.getName();
		IASTPreprocessorMacroDefinition[] mdefs = tu.getMacroDefinitions();
		for (IASTPreprocessorMacroDefinition mdef : mdefs) {
			IASTName macroName = mdef.getName();
			String macroNameStr = macroName.toString();
			if (fRenameTo.equals(macroNameStr)) {
				status.addFatalError(NLS.bind(RenameMessages.ASTManager_error_macro_name_conflict, fRenameTo));
				return;
			} else if (lookfor.equals(macroNameStr)) {
				IPath path = analyzeAstMatch(macroName, store, false, status);
				pathsVisited.add(path);
				IBinding macroBinding = macroName.resolveBinding();
				if (macroBinding != null) {
					IASTName[] refs = tu.getReferences(macroBinding);
					for (IASTName ref : refs) {
						path = analyzeAstMatch(ref, store, false, status);
						pathsVisited.add(path);
					}
				}
			}
			if (mdef instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
				boolean nameIsPar = false;
				IASTPreprocessorFunctionStyleMacroDefinition fm = (IASTPreprocessorFunctionStyleMacroDefinition) mdef;
				IASTFunctionStyleMacroParameter[] pars = fm.getParameters();
				if (pars != null) {
					for (int j = 0; !nameIsPar && j < pars.length; j++) {
						IASTFunctionStyleMacroParameter par = pars[j];
						String name = par.getParameter();
						if (lookfor.equals(name)) {
							nameIsPar = true;
						}
					}
					if (nameIsPar) {
						IASTFileLocation floc = mdef.getNodeLocations()[0].asFileLocation();
						int offset = floc.getNodeOffset();
						int end = offset + floc.getNodeLength();
						Collection<CRefactoringMatch> matches = store.findMatchesInRange(new Path(floc.getFileName()),
								offset, end);
						for (CRefactoringMatch match : matches) {
							match.setASTInformation(CRefactoringMatch.AST_REFERENCE_OTHER);
						}
					}
				}
			}
		}
	}

	//    private void markPreprocessorMatchesAsReference(
	//            IASTTranslationUnit tu, final CRefactoringMatchStore store,
	//            final Set pathsVisited, final RefactoringStatus status) {
	//        IASTPreprocessorStatement[] pdefs= tu.getAllPreprocessorStatements();
	//        for (int i = 0; i < pdefs.length; i++) {
	//            IASTPreprocessorStatement pdef = pdefs[i];
	//            if (pdef instanceof IASTPreprocessorIfdefStatement
	//                    || pdef instanceof IASTPreprocessorIfndefStatement
	//                    || pdef instanceof IASTPreprocessorIfStatement
	//                    || pdef instanceof IASTPreprocessorElifStatement
	////                  || pdef instanceof IASTPreprocessorElseStatement
	//                    || pdef instanceof IASTPreprocessorUndefStatement) {
	//        IPath path= new Path(tu.getContainingFilename());
	//                if (!store.getMatchesForPath(path).isEmpty()) {
	//                    IASTFileLocation floc= pdef.getNodeLocations()[0].asFileLocation();
	//                    int offset= floc.getNodeOffset();
	//                    int end= offset+ floc.getNodeLength();
	//                    Collection matches= store.findMatchesInRange(
	//                            new Path(floc.getFileName()), offset, end);
	//                    for (Iterator iter = matches.iterator(); iter.hasNext();) {
	//                        CRefactoringMatch match = (CRefactoringMatch) iter.next();
	//                        match.setASTInformation(CRefactoringMatch.AST_REFERENCE);
	//                    }
	//                }
	//            }
	//        }
	//    }

	private void findConflictingBindingsWithNewName(IASTTranslationUnit tu, CRefactoringMatchStore store,
			final Set<IPath> paths, final RefactoringStatus status) {
		ASTNameVisitor nv = new ASTSpecificNameVisitor(fRenameTo) {
			@Override
			protected int visitName(IASTName name, boolean isDestructor) {
				IPath path = addConflictingBindingForName(status, name);
				paths.add(path);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};
		tu.accept(nv);
	}

	protected IPath addConflictingBindingForName(final RefactoringStatus status, IASTName name) {
		IASTNodeLocation[] locations = name.getNodeLocations();
		IPath path = null;
		if (locations != null && locations.length == 1) {
			IASTNodeLocation loc = locations[0];
			IASTFileLocation floc = loc.asFileLocation();
			if (floc != null) {
				path = new Path(floc.getFileName());
				IBinding binding = name.resolveBinding();
				if (binding instanceof IProblemBinding) {
					handleProblemBinding(name.getTranslationUnit(), (IProblemBinding) binding, status);
				} else if (binding != null) {
					fConflictingBinding.add(binding);
				}
			}
		}
		return path;
	}

	protected IPath analyzeAstMatch(IASTName name, CRefactoringMatchStore store, boolean isDestructor,
			RefactoringStatus status) {
		IPath path = null;
		CRefactoringMatch match = null;

		IASTFileLocation loc = getImageFileLocation(name);
		if (loc != null) {
			path = new Path(loc.getFileName());
			match = store.findMatch(path, loc.getNodeOffset() + (isDestructor ? 1 : 0));
			if (match != null) {
				analyzeAstTextMatchPair(match, name, status);
			}
		}
		return path;
	}

	static IASTFileLocation getImageFileLocation(IASTName name) {
		return name.getImageLocation();
	}

	private void analyzeAstTextMatchPair(CRefactoringMatch match, IASTName name, RefactoringStatus status) {
		IBinding binding = name.resolveBinding();
		int cmp = FALSE;
		Integer cmpObj = fKnownBindings.get(binding);
		if (cmpObj != null) {
			cmp = cmpObj.intValue();
		} else if (binding instanceof IProblemBinding) {
			cmp = UNKNOWN;
			handleProblemBinding(name.getTranslationUnit(), (IProblemBinding) binding, status);
		} else {
			// Check whether a qualifier has a problem binding.
			boolean problemInQualifier = false;
			IASTNode parent = name.getParent();
			if (parent instanceof ICPPASTQualifiedName) {
				ICPPASTNameSpecifier[] qualifier = ((ICPPASTQualifiedName) parent).getQualifier();
				for (ICPPASTNameSpecifier n : qualifier) {
					if (n == name)
						break;
					final IBinding b = n.resolveBinding();
					if (b instanceof IProblemBinding) {
						handleProblemBinding(name.getTranslationUnit(), (IProblemBinding) b, status);
						problemInQualifier = true;
						break;
					}
				}
			}
			if (problemInQualifier) {
				cmp = UNKNOWN;
			} else {
				final IASTTranslationUnit tu = name.getTranslationUnit();
				final IIndex index = tu != null ? tu.getIndex() : null;
				IBinding[] bindings = binding instanceof ICPPUsingDeclaration
						? ((ICPPUsingDeclaration) binding).getDelegates()
						: new IBinding[] { binding };
				// When a 'using' declaration has multiple delegate bindings and only some of them
				// are being renamed, to preserve correctness of the code we would have to split
				// the 'using' declaration into two separate ones. We currently don't do that and
				// rename the 'using' declaration if at least one of its delegate bindings is being
				// renamed.
				outer: for (IBinding b : bindings) {
					for (IBinding renameBinding : fValidBindings) {
						try {
							int cmp0 = isSameBinding(index, b, renameBinding);
							if (cmp0 != FALSE) {
								cmp = cmp0;
							}
							if (cmp0 == TRUE) {
								break outer;
							}
						} catch (DOMException e) {
							handleDOMException(name.getTranslationUnit(), e, status);
							cmp = UNKNOWN;
						}
					}
				}
			}
			fKnownBindings.put(binding, Integer.valueOf(cmp));
		}
		switch (cmp) {
		case TRUE:
			match.setASTInformation(CRefactoringMatch.AST_REFERENCE);
			if (fRenameTo != null) {
				IScope scope = getContainingScope(name);
				if (scope != null) {
					IBinding[] conflicting = null;
					try {
						conflicting = findInScope(scope, fRenameTo, name.getTranslationUnit(), true);
					} catch (Exception e) {
						CUIPlugin.log(e);
					}
					if (conflicting != null && conflicting.length > 0) {
						fConflictingBinding.addAll(Arrays.asList(conflicting));
					}
				}
			}
			break;
		case FALSE:
			match.setASTInformation(CRefactoringMatch.AST_REFERENCE_OTHER);
			break;
		}
	}

	public void handleDOMException(IASTTranslationUnit tu, final DOMException e, RefactoringStatus status) {
		handleProblemBinding(tu, e.getProblem(), status);
	}

	public void handleProblemBinding(IASTTranslationUnit tu, final IProblemBinding pb, RefactoringStatus status) {
		if (tu != null) {
			String fpath = tu.getFilePath();
			if (fProblemUnits.add(fpath)) {
				String msg = pb.getMessage();
				if (msg != null && msg.length() > 0) {
					msg = NLS.bind(RenameMessages.ASTManager_warning_parsingError_detailed, msg);
				} else {
					msg = RenameMessages.ASTManager_warning_parsingError;
				}
				int line = pb.getLineNumber();
				if (line >= 1) {
					msg = NLS.bind(RenameMessages.ASTManager_warning_parsingError_withFileAndLine,
							new Object[] { msg, fpath, line });
				} else {
					msg = NLS.bind(RenameMessages.ASTManager_warning_parsingError_withFile, msg, fpath);
				}
				status.addWarning(msg);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleConflictingBindings(IASTTranslationUnit tu, RefactoringStatus status) {
		if (fConflictingBinding.isEmpty()) {
			return;
		}

		int argKind = fArgument.getArgumentKind();
		boolean isVarParEnumerator = false;
		boolean isLocalVarPar = false;
		boolean isFunction = false;
		boolean isContainer = false;
		boolean isMacro = false;

		switch (argKind) {
		case CRefactory.ARGUMENT_LOCAL_VAR:
		case CRefactory.ARGUMENT_PARAMETER:
			isLocalVarPar = true;
			isVarParEnumerator = true;
			break;
		case CRefactory.ARGUMENT_FILE_LOCAL_VAR:
		case CRefactory.ARGUMENT_GLOBAL_VAR:
		case CRefactory.ARGUMENT_FIELD:
		case CRefactory.ARGUMENT_ENUMERATOR:
			isVarParEnumerator = true;
			break;
		case CRefactory.ARGUMENT_FILE_LOCAL_FUNCTION:
		case CRefactory.ARGUMENT_GLOBAL_FUNCTION:
		case CRefactory.ARGUMENT_VIRTUAL_METHOD:
		case CRefactory.ARGUMENT_NON_VIRTUAL_METHOD:
			isFunction = true;
			break;
		case CRefactory.ARGUMENT_TYPE:
		case CRefactory.ARGUMENT_CLASS_TYPE:
		case CRefactory.ARGUMENT_NAMESPACE:
			isContainer = true;
			break;
		case CRefactory.ARGUMENT_MACRO:
			isMacro = true;
			break;
		case CRefactory.ARGUMENT_INCLUDE_DIRECTIVE:
			break;
		}

		Collection<IBinding>[] cflc = new Collection[] { new HashSet<IBinding>(), new ArrayList<IBinding>(),
				new ArrayList<IBinding>() };
		String[] errs = null;
		if (isMacro) {
			errs = new String[] { RenameMessages.CRenameLocalProcessor_error_conflict };
			cflc[0] = fConflictingBinding;
		} else {
			errs = new String[] { RenameMessages.CRenameLocalProcessor_error_shadow,
					RenameMessages.CRenameLocalProcessor_error_redeclare,
					RenameMessages.CRenameLocalProcessor_error_isShadowed,
					RenameMessages.CRenameLocalProcessor_error_overloads };
			classifyConflictingBindings(tu, (Set<IBinding>) cflc[0], (List<IBinding>) cflc[1], (List<IBinding>) cflc[2],
					status);
		}

		for (int i = 0; i < 3; i++) {
			Collection<?> coll = cflc[i];
			for (Object name : coll) {
				boolean warn = false;
				String msg = errs[i];
				IBinding conflict = (IBinding) name;
				String what = null;
				if (conflict instanceof IEnumerator) {
					if (isVarParEnumerator || isFunction || isMacro) {
						what = RenameMessages.CRenameLocalProcessor_enumerator;
					}
				} else if (conflict instanceof ICPPField) {
					if (isVarParEnumerator || isFunction || isMacro) {
						what = RenameMessages.CRenameLocalProcessor_field;
					}
				} else if (conflict instanceof IParameter) {
					if (isVarParEnumerator || isFunction || isMacro) {
						if (i == 1 && argKind == CRefactory.ARGUMENT_LOCAL_VAR) {
							msg = errs[0];
						}
						what = RenameMessages.CRenameLocalProcessor_parameter;
					}
				} else if (conflict instanceof IVariable) {
					if (isVarParEnumerator || isFunction || isMacro) {
						IVariable conflictingVar = (IVariable) conflict;
						what = RenameMessages.CRenameLocalProcessor_globalVariable;
						if (ASTManager.isLocalVariable(conflictingVar)) {
							if (i == 1 && argKind == CRefactory.ARGUMENT_PARAMETER) {
								msg = errs[2];
							}
							what = RenameMessages.CRenameLocalProcessor_localVariable;
						} else {
							if (conflictingVar.isStatic()) {
								what = RenameMessages.CRenameProcessorDelegate_fileStaticVariable;
							}
						}
					}
				} else if (conflict instanceof ICPPConstructor) {
					if (isVarParEnumerator || isFunction || isMacro) {
						what = RenameMessages.CRenameLocalProcessor_constructor;
					}
				} else if (conflict instanceof ICPPMethod) {
					if (isVarParEnumerator || isFunction || isMacro) {
						if (i == 1) {
							IBinding r = fArgument.getBinding();
							if (r instanceof ICPPMethod) {
								try {
									if (ASTManager.hasSameSignature((ICPPMethod) r,
											(ICPPMethod) conflict) == ASTManager.FALSE) {
										msg = errs[3];
										warn = true;
									}
								} catch (DOMException e) {
								}
							}
						}
						what = RenameMessages.CRenameLocalProcessor_method;
					}
				} else if (conflict instanceof IFunction) {
					if (isVarParEnumerator || isFunction || isMacro) {
						boolean ignore = false;
						if (isLocalVarPar) {
							IASTName[] refs = fArgument.getTranslationUnit().getReferences(conflict);
							if (refs == null || refs.length == 0) {
								ignore = true;
							}
						}
						if (!ignore) {
							IFunction conflictingFunction = (IFunction) conflict;
							if (i == 1 && conflict instanceof ICPPFunction) {
								IBinding r = fArgument.getBinding();
								if (r instanceof ICPPFunction) {
									try {
										if (ASTManager.hasSameSignature((ICPPFunction) r,
												conflictingFunction) == ASTManager.FALSE) {
											msg = errs[3];
											warn = true;
										}
									} catch (DOMException e) {
									}
								}
							}

							boolean isStatic = conflictingFunction.isStatic();
							if (isStatic) {
								what = RenameMessages.CRenameProcessorDelegate_fileStaticFunction;
							} else {
								what = RenameMessages.CRenameProcessorDelegate_globalFunction;
							}
						}
					}
				} else if (conflict instanceof ICompositeType || conflict instanceof IEnumeration
						|| conflict instanceof ITypedef) {
					if (isContainer || isMacro) {
						what = RenameMessages.CRenameProcessorDelegate_type;
					}
				} else if (conflict instanceof ICPPNamespace) {
					if (isContainer || isMacro) {
						what = RenameMessages.CRenameProcessorDelegate_namespace;
						if (argKind == CRefactory.ARGUMENT_NAMESPACE) {
							warn = true;
						}
					}
				}
				if (what != null) {
					String message = RenameMessages.CRenameLocalProcessor_error_message;
					String message1 = NLS.bind(RenameMessages.CRenameLocalProcessor_error_message1, msg);
					String message2 = NLS.bind(RenameMessages.CRenameLocalProcessor_error_message2, conflict.getName());
					String message3 = NLS.bind(RenameMessages.CRenameLocalProcessor_error_message3, what);
					String space = "  \n"; //$NON-NLS-1$
					String formatted = message + space + message1 + space + message2 + space + message3;
					RefactoringStatusEntry[] entries = status.getEntries();
					for (RefactoringStatusEntry entry : entries) {
						if (formatted.equals(entry.getMessage())) {
							formatted = null;
							break;
						}
					}
					if (formatted != null) {
						if (warn) {
							status.addWarning(formatted);
						} else {
							status.addError(formatted);
						}
					}
				}
			}
		}
	}

	protected void classifyConflictingBindings(IASTTranslationUnit tu, Set<IBinding> shadows,
			Collection<IBinding> redecl, Collection<IBinding> barriers, RefactoringStatus status) {
		// Collect bindings on higher or equal level.
		String name = fArgument.getName();
		IBinding[] newBindingsAboverOrEqual = null;
		IScope oldBindingsScope = null;
		for (Map.Entry<IBinding, Integer> entry : fKnownBindings.entrySet()) {
			IBinding oldBinding = entry.getKey();
			Integer value = entry.getValue();
			if (value.intValue() == TRUE && oldBinding.getName().equals(name)) {
				try {
					oldBindingsScope = oldBinding.getScope();
					if (oldBindingsScope != null) {
						newBindingsAboverOrEqual = ASTManager.findInScope(oldBindingsScope, fRenameTo, null, false);
					}
				} catch (DOMException e) {
					handleDOMException(tu, e, status);
				}
			}

			if (newBindingsAboverOrEqual != null && newBindingsAboverOrEqual.length > 0) {
				break;
			}
		}
		if (newBindingsAboverOrEqual == null) {
			newBindingsAboverOrEqual = IBinding.EMPTY_BINDING_ARRAY;
		}

		// Check conflicting bindings for being from above or equal level.
		for (IBinding conflictingBinding : fConflictingBinding) {
			if (conflictingBinding != null) {
				boolean isAboveOrEqual = false;
				for (int i = 0; !isAboveOrEqual && i < newBindingsAboverOrEqual.length; i++) {
					IBinding aboveBinding = newBindingsAboverOrEqual[i];
					try {
						if (isSameBinding(tu.getIndex(), aboveBinding, conflictingBinding) == TRUE) {
							isAboveOrEqual = true;
						}
					} catch (DOMException e) {
						handleDOMException(tu, e, status);
					}
				}
				if (!isAboveOrEqual) {
					barriers.add(conflictingBinding);
				}
			}
		}

		// Find bindings on same level.
		for (IBinding aboveBinding : newBindingsAboverOrEqual) {
			IScope aboveScope;
			try {
				aboveScope = aboveBinding.getScope();
				if (isSameScope(aboveScope, oldBindingsScope, false) == TRUE) {
					redecl.add(aboveBinding);
				} else {
					shadows.add(aboveBinding);
				}
			} catch (DOMException e) {
				handleDOMException(tu, e, status);
			}
		}
	}

	public void setValidBindings(IBinding[] validBindings) {
		fValidBindings = validBindings;
	}

	public void setRenameTo(String renameTo) {
		fRenameTo = renameTo;
	}
}
