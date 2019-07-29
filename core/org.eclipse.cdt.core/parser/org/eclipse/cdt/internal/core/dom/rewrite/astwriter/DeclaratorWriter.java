/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik
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
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import java.util.EnumSet;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVirtSpecifier.SpecifierKind;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Generates source code of declarator nodes. The actual string operations are delegated
 * to the {@link Scribe} class.
 *
 * @see IASTDeclarator
 * @author Emanuel Graf IFS
 */
public class DeclaratorWriter extends NodeWriter {
	private static final String AMPERSAND_AMPERSAND = "&&"; //$NON-NLS-1$
	private static final String PURE_VIRTUAL = " = 0"; //$NON-NLS-1$
	private static final String ARROW_OPERATOR = "->"; //$NON-NLS-1$

	public DeclaratorWriter(Scribe scribe, ASTWriterVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}

	protected void writeDeclarator(IASTDeclarator declarator) {
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			writeFunctionDeclarator((IASTStandardFunctionDeclarator) declarator);
		} else if (declarator instanceof IASTArrayDeclarator) {
			writeArrayDeclarator((IASTArrayDeclarator) declarator);
		} else if (declarator instanceof IASTFieldDeclarator) {
			writeFieldDeclarator((IASTFieldDeclarator) declarator);
		} else if (declarator instanceof ICASTKnRFunctionDeclarator) {
			writeCKnRFunctionDeclarator((ICASTKnRFunctionDeclarator) declarator);
		} else {
			writeDefaultDeclarator(declarator);
		}

		visitor.setSpaceNeededBeforeName(false);
		writeTrailingComments(declarator, false);
	}

	protected void writeDefaultDeclarator(IASTDeclarator declarator) {
		IASTPointerOperator[] pointOps = declarator.getPointerOperators();
		writePointerOperators(declarator, pointOps);
		writeParameterPack(declarator);
		IASTName name = declarator.getName();
		name.accept(visitor);
		writeNestedDeclarator(declarator);
		writeAttributes(declarator, EnumSet.of(SpaceLocation.BEFORE));
		IASTInitializer init = getInitializer(declarator);
		if (init != null) {
			init.accept(visitor);
		}
	}

	protected void writePointerOperators(IASTDeclarator declarator, IASTPointerOperator[] pointOps) {
		for (IASTPointerOperator operator : pointOps) {
			writeGCCAttributes(operator, EnumSet.noneOf(SpaceLocation.class));
			writePointerOperator(operator);
			writeCPPAttributes(operator, EnumSet.noneOf(SpaceLocation.class));
		}
	}

	private void writeParameterPack(IASTDeclarator declarator) {
		if (declarator instanceof ICPPASTDeclarator) {
			if (((ICPPASTDeclarator) declarator).declaresParameterPack()) {
				scribe.print(VAR_ARGS);
			}
		}
	}

	private void writeFunctionDeclarator(IASTStandardFunctionDeclarator funcDec) {
		IASTPointerOperator[] pointOps = funcDec.getPointerOperators();
		writePointerOperators(funcDec, pointOps);
		// Lambda declarators happen to have null names rather than empty ones when parsed.
		if (funcDec.getName() != null) {
			funcDec.getName().accept(visitor);
		}
		writeNestedDeclarator(funcDec);
		writeParameters(funcDec);
		writeInitializer(funcDec);
		if (funcDec instanceof ICPPASTFunctionDeclarator) {
			writeCppFunctionDeclarator((ICPPASTFunctionDeclarator) funcDec);
		}
	}

	private void writeInitializer(IASTStandardFunctionDeclarator funcDec) {
		IASTInitializer init = getInitializer(funcDec);
		if (init != null) {
			init.accept(visitor);
		}
	}

	private void writeParameters(IASTStandardFunctionDeclarator funcDec) {
		IASTParameterDeclaration[] paraDecls = funcDec.getParameters();
		scribe.print('(');
		writeParameterDeclarations(funcDec, paraDecls);
		scribe.print(')');
	}

	private void writeNestedDeclarator(IASTDeclarator funcDec) {
		IASTDeclarator nestedDeclarator = funcDec.getNestedDeclarator();
		if (nestedDeclarator != null) {
			if (visitor.isSpaceNeededBeforeName()) {
				scribe.printSpace();
				visitor.setSpaceNeededBeforeName(false);
			}
			scribe.print('(');
			nestedDeclarator.accept(visitor);
			scribe.print(')');
		}
	}

	private void writeCppFunctionDeclarator(ICPPASTFunctionDeclarator funcDec) {
		if (funcDec.isConst()) {
			scribe.printSpace();
			scribe.print(Keywords.CONST);
		}
		if (funcDec.isVolatile()) {
			scribe.printSpace();
			scribe.print(Keywords.VOLATILE);
		}
		RefQualifier refQualifier = funcDec.getRefQualifier();
		writeRefQualifier(refQualifier);
		if (funcDec.isMutable()) {
			scribe.printSpace();
			scribe.print(Keywords.MUTABLE);
		}
		writeExceptionSpecification(funcDec, funcDec.getExceptionSpecification(), funcDec.getNoexceptExpression());
		writeAttributes(funcDec, EnumSet.of(SpaceLocation.BEFORE));
		if (funcDec.getTrailingReturnType() != null) {
			scribe.printSpace();
			scribe.print(ARROW_OPERATOR);
			scribe.printSpace();
			funcDec.getTrailingReturnType().accept(visitor);
		}
		writeVirtualSpecifiers(funcDec);
		if (funcDec.isPureVirtual()) {
			scribe.print(PURE_VIRTUAL);
		}
	}

	public void writeVirtualSpecifiers(ICPPASTFunctionDeclarator funcDec) {
		for (ICPPASTVirtSpecifier virtSpecifier : funcDec.getVirtSpecifiers()) {
			scribe.printSpace();
			SpecifierKind specifierKind = virtSpecifier.getKind();
			if (specifierKind == SpecifierKind.Override) {
				scribe.print(Keywords.cOVERRIDE);
			}
			if (specifierKind == SpecifierKind.Final) {
				scribe.print(Keywords.cFINAL);
			}
		}
	}

	protected void writeExceptionSpecification(ICPPASTFunctionDeclarator funcDec, IASTTypeId[] exceptions,
			ICPPASTExpression noexceptExpression) {
		if (exceptions != ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
			scribe.printSpace();
			scribe.printStringSpace(Keywords.THROW);
			scribe.print('(');
			writeNodeList(exceptions);
			scribe.print(')');
		}
		if (noexceptExpression != null) {
			scribe.printSpace();
			scribe.print(Keywords.NOEXCEPT);
			if (noexceptExpression != ICPPASTFunctionDeclarator.NOEXCEPT_DEFAULT) {
				scribe.printSpace();
				scribe.print('(');
				noexceptExpression.accept(visitor);
				scribe.print(')');
			}
		}
	}

	protected void writeParameterDeclarations(IASTStandardFunctionDeclarator funcDec,
			IASTParameterDeclaration[] paramDecls) {
		writeNodeList(paramDecls);
		if (funcDec.takesVarArgs()) {
			if (paramDecls.length > 0) {
				scribe.print(COMMA_SPACE);
			}
			scribe.print(VAR_ARGS);
		}
	}

	private void writePointer(IASTPointer operator) {
		if (operator instanceof ICPPASTPointerToMember) {
			ICPPASTPointerToMember pointerToMemberOp = (ICPPASTPointerToMember) operator;
			if (pointerToMemberOp.getName() != null) {
				pointerToMemberOp.getName().accept(visitor);
				scribe.print('*');
			}
		} else {
			scribe.print('*');
		}

		if (operator.isConst()) {
			scribe.printStringSpace(Keywords.CONST);
		}
		if (operator.isVolatile()) {
			scribe.printStringSpace(Keywords.VOLATILE);
		}
		if (operator.isRestrict()) {
			scribe.printStringSpace(Keywords.RESTRICT);
		}
	}

	public void writePointerOperator(IASTPointerOperator operator) {
		if (operator instanceof IASTPointer) {
			IASTPointer pointOp = (IASTPointer) operator;
			writePointer(pointOp);
		} else if (operator instanceof ICPPASTReferenceOperator) {
			if (((ICPPASTReferenceOperator) operator).isRValueReference()) {
				scribe.print(AMPERSAND_AMPERSAND);
			} else {
				scribe.print('&');
			}
		}
	}

	private void writeArrayDeclarator(IASTArrayDeclarator arrDecl) {
		IASTPointerOperator[] pointOps = arrDecl.getPointerOperators();
		writePointerOperators(arrDecl, pointOps);
		IASTName name = arrDecl.getName();
		name.accept(visitor);

		writeNestedDeclarator(arrDecl);

		IASTArrayModifier[] arrMods = arrDecl.getArrayModifiers();
		writeArrayModifiers(arrDecl, arrMods);
		IASTInitializer initializer = getInitializer(arrDecl);
		if (initializer != null) {
			initializer.accept(visitor);
		}
	}

	protected IASTInitializer getInitializer(IASTDeclarator decl) {
		return decl.getInitializer();
	}

	protected void writeArrayModifiers(IASTArrayDeclarator arrDecl, IASTArrayModifier[] arrMods) {
		for (IASTArrayModifier modifier : arrMods) {
			writeArrayModifier(modifier);
		}
	}

	protected void writeArrayModifier(IASTArrayModifier modifier) {
		scribe.print('[');
		IASTExpression ex = modifier.getConstantExpression();
		if (ex != null) {
			ex.accept(visitor);
		}
		scribe.print(']');
		writeAttributes(modifier, EnumSet.noneOf(SpaceLocation.class));
	}

	private void writeFieldDeclarator(IASTFieldDeclarator fieldDecl) {
		IASTPointerOperator[] pointOps = fieldDecl.getPointerOperators();
		writePointerOperators(fieldDecl, pointOps);
		fieldDecl.getName().accept(visitor);
		scribe.printSpace();
		scribe.print(':');
		scribe.printSpace();
		fieldDecl.getBitFieldSize().accept(visitor);
		IASTInitializer initializer = getInitializer(fieldDecl);
		if (initializer != null) {
			initializer.accept(visitor);
		}
	}

	private void writeCKnRFunctionDeclarator(ICASTKnRFunctionDeclarator knrFunct) {
		knrFunct.getName().accept(visitor);
		scribe.print('(');
		writeKnRParameterNames(knrFunct, knrFunct.getParameterNames());
		scribe.print(')');
		scribe.newLine();
		writeKnRParameterDeclarations(knrFunct, knrFunct.getParameterDeclarations());
	}

	protected void writeKnRParameterDeclarations(ICASTKnRFunctionDeclarator knrFunct,
			IASTDeclaration[] knrDeclarations) {
		for (int i = 0; i < knrDeclarations.length; ++i) {
			scribe.noNewLines();
			knrDeclarations[i].accept(visitor);
			scribe.newLines();
			if (i + 1 < knrDeclarations.length) {
				scribe.newLine();
			}
		}
	}

	protected void writeKnRParameterNames(ICASTKnRFunctionDeclarator knrFunct, IASTName[] parameterNames) {
		writeNodeList(parameterNames);
	}
}
