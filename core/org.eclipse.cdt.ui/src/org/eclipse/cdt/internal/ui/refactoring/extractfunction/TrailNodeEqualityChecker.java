/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.ui.refactoring.Container;
import org.eclipse.cdt.internal.ui.refactoring.EqualityChecker;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;

public class TrailNodeEqualityChecker implements EqualityChecker<IASTNode> {
	private final Map<String, Integer> names;
	private final Container<Integer> namesCounter;
	private final IIndex index;

	public TrailNodeEqualityChecker(Map<String, Integer> names, Container<Integer> namesCounter, IIndex index) {
		super();
		this.names = names;
		this.namesCounter = namesCounter;
		this.index = index;
	}

	@Override
	public boolean isEqual(IASTNode trailNode, IASTNode node) {
		if ((trailNode instanceof TrailName && node instanceof IASTName)
				|| Arrays.equals(getInterfaces(node), getInterfaces(trailNode))) {
			// Is same type
			if (node instanceof IASTExpression) {
				return isExpressionEqual(trailNode, node);
			} else if (node instanceof IASTStatement) {
				return isStatementEqual(trailNode, node);
			} else if (node instanceof IASTPointerOperator) {
				return isPointerOperatorEqual(trailNode, node);
			} else if (node instanceof IASTDeclaration) {
				return isDeclarationEqual(trailNode, node);
			} else if (node instanceof IASTDeclarator) {
				return isDeclaratorEqual(trailNode, node);
			} else if (node instanceof IASTInitializer) {
				// No special case, the same type means equality
				return true;
			} else if (node instanceof IASTDeclSpecifier) {
				return isDeclSpecifierEqual(trailNode, node);
			} else if (node instanceof ICPPASTTypeId) {
				return idTypeIdEqual((ICPPASTTypeId) trailNode, (ICPPASTTypeId) node);
			} else if (node instanceof IASTName) {
				return isNameEqual(trailNode, node);
			} else {
				CUIPlugin.logError("Unexpected node type " + node.getClass().getSimpleName()); //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	private boolean isNameEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof ICPPASTConversionName) {
			return true;
		} else if (trailNode instanceof ICPPASTOperatorName) {
			ICPPASTOperatorName trailName = (ICPPASTOperatorName) trailNode;
			ICPPASTOperatorName name = (ICPPASTOperatorName) node;
			return trailName.equals(name);
		} else if (trailNode instanceof TrailName && node instanceof IASTName) {
			TrailName trailName = (TrailName) trailNode;
			IASTName name = (IASTName) node;
			return isNameEqual(trailName, name);
		} else {
			return true;
		}
	}

	private boolean isDeclSpecifierEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier trailDecl = (IASTSimpleDeclSpecifier) trailNode;
			IASTSimpleDeclSpecifier decl = (IASTSimpleDeclSpecifier) node;
			return isSimpleDeclSpecifierEqual(trailDecl, decl);
		} else if (trailNode instanceof ICPPASTNamedTypeSpecifier) {
			ICPPASTNamedTypeSpecifier trailDecl = (ICPPASTNamedTypeSpecifier) trailNode;
			ICPPASTNamedTypeSpecifier decl = (ICPPASTNamedTypeSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl) && isSameNamedTypeSpecifierName(trailDecl, decl)
					&& trailDecl.isConstexpr() == decl.isConstexpr() && trailDecl.isExplicit() == decl.isExplicit()
					&& trailDecl.isFriend() == decl.isFriend() && trailDecl.isThreadLocal() == decl.isThreadLocal()
					&& trailDecl.isTypename() == decl.isTypename() && trailDecl.isVirtual() == decl.isVirtual();
		} else if (trailNode instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier trailDecl = (IASTNamedTypeSpecifier) trailNode;
			IASTNamedTypeSpecifier decl = (IASTNamedTypeSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl) && isSameNamedTypeSpecifierName(trailDecl, decl);
		} else if (trailNode instanceof IASTElaboratedTypeSpecifier) {
			IASTElaboratedTypeSpecifier trailDecl = (IASTElaboratedTypeSpecifier) trailNode;
			IASTElaboratedTypeSpecifier decl = (IASTElaboratedTypeSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl) && trailDecl.getKind() == decl.getKind();
		} else if (trailNode instanceof IASTCompositeTypeSpecifier) {
			IASTCompositeTypeSpecifier trailDecl = (IASTCompositeTypeSpecifier) trailNode;
			IASTCompositeTypeSpecifier decl = (IASTCompositeTypeSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl) && trailDecl.getKey() == decl.getKey();
		} else if (trailNode instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier trailDecl = (ICPPASTDeclSpecifier) trailNode;
			ICPPASTDeclSpecifier decl = (ICPPASTDeclSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl) && trailDecl.isConstexpr() == decl.isConstexpr()
					&& trailDecl.isExplicit() == decl.isExplicit() && trailDecl.isFriend() == decl.isFriend()
					&& trailDecl.isThreadLocal() == decl.isThreadLocal() && trailDecl.isVirtual() == decl.isVirtual();
		} else if (trailNode instanceof ICASTDeclSpecifier) {
			ICASTDeclSpecifier trailDecl = (ICASTDeclSpecifier) trailNode;
			ICASTDeclSpecifier decl = (ICASTDeclSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl) && trailDecl.isRestrict() == decl.isRestrict();
		} else if (trailNode instanceof IASTDeclSpecifier) {
			IASTDeclSpecifier trailDecl = (IASTDeclSpecifier) trailNode;
			IASTDeclSpecifier decl = (IASTDeclSpecifier) node;
			return isDeclSpecifierEqual(trailDecl, decl);
		} else {
			// The same.
			return true;
		}
	}

	private boolean isDeclaratorEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTStandardFunctionDeclarator) {
			IASTStandardFunctionDeclarator trailFunc = (IASTStandardFunctionDeclarator) trailNode;
			IASTStandardFunctionDeclarator func = (IASTStandardFunctionDeclarator) node;
			return trailFunc.takesVarArgs() == func.takesVarArgs();
		} else if (trailNode instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator trailFunc = (ICPPASTFunctionDeclarator) trailNode;
			ICPPASTFunctionDeclarator func = (ICPPASTFunctionDeclarator) node;
			return trailFunc.isConst() == func.isConst() && trailFunc.isPureVirtual() == func.isPureVirtual()
					&& trailFunc.isVolatile() == func.isVolatile();
		} else {
			//same type
			return true;
		}
	}

	private boolean isDeclarationEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTASMDeclaration) {
			IASTASMDeclaration trailASMDecl = (IASTASMDeclaration) trailNode;
			IASTASMDeclaration asmDecl = (IASTASMDeclaration) node;
			return trailASMDecl.getAssembly().equals(asmDecl.getAssembly());
		} else if (trailNode instanceof ICPPASTExplicitTemplateInstantiation) {
			ICPPASTExplicitTemplateInstantiation trailTempl = (ICPPASTExplicitTemplateInstantiation) trailNode;
			ICPPASTExplicitTemplateInstantiation templ = (ICPPASTExplicitTemplateInstantiation) node;
			return trailTempl.getModifier() == templ.getModifier();
		} else if (trailNode instanceof ICPPASTLinkageSpecification) {
			ICPPASTLinkageSpecification trailLink = (ICPPASTLinkageSpecification) trailNode;
			ICPPASTLinkageSpecification link = (ICPPASTLinkageSpecification) node;
			return trailLink.getLiteral().equals(link.getLiteral());
		} else if (trailNode instanceof ICPPASTTemplateDeclaration) {
			ICPPASTTemplateDeclaration trailTempl = (ICPPASTTemplateDeclaration) trailNode;
			ICPPASTTemplateDeclaration templ = (ICPPASTTemplateDeclaration) node;
			return trailTempl.isExported() == templ.isExported();
		} else if (trailNode instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration trailUsing = (ICPPASTUsingDeclaration) trailNode;
			ICPPASTUsingDeclaration using = (ICPPASTUsingDeclaration) node;
			return trailUsing.isTypename() == using.isTypename();
		} else if (trailNode instanceof ICPPASTVisibilityLabel) {
			ICPPASTVisibilityLabel trailVisibility = (ICPPASTVisibilityLabel) trailNode;
			ICPPASTVisibilityLabel visibility = (ICPPASTVisibilityLabel) node;
			return trailVisibility.getVisibility() == visibility.getVisibility();
		} else {
			//same type
			return true;
		}
	}

	private boolean isPointerOperatorEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTPointer) {
			IASTPointer trailGPointer = (IASTPointer) trailNode;
			IASTPointer gPointer = (IASTPointer) node;
			return trailGPointer.isConst() == gPointer.isConst() && trailGPointer.isRestrict() == gPointer.isRestrict()
					&& trailGPointer.isVolatile() == gPointer.isVolatile();
		} else {
			//same type
			return true;
		}
	}

	private boolean isStatementEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof ICPPASTCatchHandler) {
			ICPPASTCatchHandler trailCatch = (ICPPASTCatchHandler) trailNode;
			ICPPASTCatchHandler nodeCatch = (ICPPASTCatchHandler) node;
			return trailCatch.isCatchAll() == nodeCatch.isCatchAll();
		}
		//same type
		return true;
	}

	private boolean idTypeIdEqual(ICPPASTTypeId trailNode, ICPPASTTypeId node) {
		return trailNode.isPackExpansion() == node.isPackExpansion();
	}

	private boolean isExpressionEqual(IASTNode trailNode, IASTNode node) {
		if (trailNode instanceof IASTBinaryExpression) {
			IASTBinaryExpression trailExpr = (IASTBinaryExpression) trailNode;
			IASTBinaryExpression expr = (IASTBinaryExpression) node;
			return trailExpr.getOperator() == expr.getOperator();
		} else if (trailNode instanceof ICPPASTFieldReference) {
			ICPPASTFieldReference trailFieldRef = (ICPPASTFieldReference) trailNode;
			ICPPASTFieldReference fieldRef = (ICPPASTFieldReference) node;
			return trailFieldRef.isPointerDereference() == fieldRef.isPointerDereference()
					&& trailFieldRef.isTemplate() == fieldRef.isTemplate();
		} else if (trailNode instanceof IASTFieldReference) {
			IASTFieldReference trailFieldRef = (IASTFieldReference) trailNode;
			IASTFieldReference fieldRef = (IASTFieldReference) node;
			return trailFieldRef.isPointerDereference() == fieldRef.isPointerDereference();
		} else if (trailNode instanceof IASTLiteralExpression) {
			IASTLiteralExpression trailLiteral = (IASTLiteralExpression) trailNode;
			IASTLiteralExpression literal = (IASTLiteralExpression) node;
			return trailLiteral.getKind() == literal.getKind()
					&& trailLiteral.getRawSignature().equals(literal.getRawSignature());
		} else if (trailNode instanceof IASTUnaryExpression) {
			IASTUnaryExpression trailExpr = (IASTUnaryExpression) trailNode;
			IASTUnaryExpression expr = (IASTUnaryExpression) node;
			return trailExpr.getOperator() == expr.getOperator();
		} else if (trailNode instanceof IASTTypeIdExpression) {
			IASTTypeIdExpression trailIdExpr = (IASTTypeIdExpression) trailNode;
			IASTTypeIdExpression idExpr = (IASTTypeIdExpression) node;
			return trailIdExpr.getTypeId() == idExpr.getTypeId();
		} else if (trailNode instanceof ICPPASTDeleteExpression) {
			ICPPASTDeleteExpression trailDelete = (ICPPASTDeleteExpression) trailNode;
			ICPPASTDeleteExpression delete = (ICPPASTDeleteExpression) node;
			return trailDelete.isGlobal() == delete.isGlobal() && trailDelete.isVectored() == delete.isVectored();
		} else if (trailNode instanceof ICPPASTNewExpression) {
			ICPPASTNewExpression trailNew = (ICPPASTNewExpression) trailNode;
			ICPPASTNewExpression nodeNew = (ICPPASTNewExpression) node;
			return trailNew.isGlobal() == nodeNew.isGlobal() && trailNew.isNewTypeId() == nodeNew.isNewTypeId();
		} else if (trailNode instanceof ICPPASTSimpleTypeConstructorExpression) {
			ICPPASTSimpleTypeConstructorExpression trailConsExpr = (ICPPASTSimpleTypeConstructorExpression) trailNode;
			ICPPASTSimpleTypeConstructorExpression consExpr = (ICPPASTSimpleTypeConstructorExpression) node;
			return isDeclSpecifierEqual(trailConsExpr.getDeclSpecifier(), consExpr.getDeclSpecifier());
		} else {
			//			same type
			return true;
		}
	}

	private boolean isSameNamedTypeSpecifierName(IASTNamedTypeSpecifier trailDecl, IASTNamedTypeSpecifier decl) {
		return trailDecl.getName().getRawSignature().equals(decl.getName().getRawSignature());
	}

	private Class<?>[] getInterfaces(IASTNode node) {
		Class<?>[] interfaces = node.getClass().getInterfaces();
		List<Class<?>> interfaceList = Arrays.asList(interfaces);
		Class<?>[] returnArray = new Class[interfaceList.size()];
		return interfaceList.toArray(returnArray);
	}

	private boolean isDeclSpecifierEqual(IASTDeclSpecifier trailDeclSpeci, IASTDeclSpecifier declSpeci) {
		if (trailDeclSpeci instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier trailCppDecl = (ICPPASTDeclSpecifier) trailDeclSpeci;
			ICPPASTDeclSpecifier cppDecl = (ICPPASTDeclSpecifier) declSpeci;
			if (trailCppDecl.isConstexpr() != cppDecl.isConstexpr() || trailCppDecl.isExplicit() != cppDecl.isExplicit()
					|| trailCppDecl.isFriend() != cppDecl.isFriend()
					|| trailCppDecl.isThreadLocal() != cppDecl.isThreadLocal()
					|| trailCppDecl.isVirtual() != cppDecl.isVirtual()) {
				return false;
			}
		}
		return trailDeclSpeci.isConst() == declSpeci.isConst() && trailDeclSpeci.isInline() == declSpeci.isInline()
				&& trailDeclSpeci.isVolatile() == declSpeci.isVolatile()
				&& trailDeclSpeci.isRestrict() == declSpeci.isRestrict()
				&& trailDeclSpeci.getStorageClass() == declSpeci.getStorageClass();
	}

	private boolean isSimpleDeclSpecifierEqual(IASTSimpleDeclSpecifier trailDeclSpeci,
			IASTSimpleDeclSpecifier declSpeci) {
		return isDeclSpecifierEqual(trailDeclSpeci, declSpeci) && trailDeclSpeci.isLong() == declSpeci.isLong()
				&& trailDeclSpeci.isShort() == declSpeci.isShort() && trailDeclSpeci.isSigned() == declSpeci.isSigned()
				&& trailDeclSpeci.isUnsigned() == declSpeci.isUnsigned()
				&& trailDeclSpeci.getType() == declSpeci.getType()
				&& trailDeclSpeci.isComplex() == declSpeci.isComplex()
				&& trailDeclSpeci.isImaginary() == declSpeci.isImaginary()
				&& trailDeclSpeci.isLongLong() == declSpeci.isLongLong();
	}

	private boolean isNameEqual(TrailName trailName, IASTName name) {
		int actCount = namesCounter.getObject().intValue();
		if (names.containsKey(name.getRawSignature())) {
			Integer nameId = names.get(name.getRawSignature());
			actCount = nameId.intValue();
		} else {
			++actCount;
			namesCounter.setObject(Integer.valueOf(actCount));
			names.put(name.getRawSignature(), namesCounter.getObject());
		}

		if (actCount != trailName.getNameNumber()) {
			return false;
		}

		IASTName realName = trailName.getRealName();
		IBinding realBind = realName.resolveBinding();
		IBinding nameBind = name.resolveBinding();
		if (trailName.isGloballyQualified()) {
			IIndexName[] realDecs;
			IIndexName[] nameDecs;
			try {
				realDecs = index.findDeclarations(realBind);
				nameDecs = index.findDeclarations(nameBind);
			} catch (CoreException e) {
				CUIPlugin.log(e);
				return false;
			}

			if (realDecs.length != nameDecs.length)
				return false;
			for (int i = 0; i < realDecs.length; ++i) {
				IASTFileLocation rfl = realDecs[i].getFileLocation();
				IASTFileLocation nfl = nameDecs[i].getFileLocation();
				if (rfl.getNodeOffset() != nfl.getNodeOffset() || !rfl.getFileName().equals(nfl.getFileName()))
					return false;
			}
			return true;
		} else {
			if (realBind instanceof ILabel && nameBind instanceof ILabel) {
				if (realName.getRawSignature().equals(name.getRawSignature())) {
					return true;
				}
			} else {
				IType oType = getType(realBind);
				IType nType = getType(nameBind);
				if (oType == null || nType == null)
					return false;

				if (oType.isSameType(nType))
					return true;
			}
		}
		return false;
	}

	private IType getType(IBinding binding) {
		if (binding instanceof ICPPVariable) {
			ICPPVariable var = (ICPPVariable) binding;
			return var.getType();
		}
		return null;
	}
}
