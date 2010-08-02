/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

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
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;


/**
 * 
 * Generates source code of declarator nodes. The actual string operations are delegated
 * to the <code>Scribe</code> class.
 * 
 * @see Scribe
 * @see IASTDeclarator
 * @author Emanuel Graf IFS
 * 
 */
public class DeclaratorWriter extends NodeWriter {

	private static final String AMPERSAND_SPACE = "& "; //$NON-NLS-1$
	private static final String STAR_SPACE = "* "; //$NON-NLS-1$
	private static final String PURE_VIRTUAL = " =0"; //$NON-NLS-1$
	
	public DeclaratorWriter(Scribe scribe, CPPASTVisitor visitor, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
	}
	
	protected void writeDeclarator(IASTDeclarator declarator) {
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			writeFunctionDeclarator((IASTStandardFunctionDeclarator) declarator);
		}else if (declarator instanceof IASTArrayDeclarator) {
			writeArrayDeclarator((IASTArrayDeclarator) declarator);
		}else if (declarator instanceof IASTFieldDeclarator) {
			writeFieldDeclarator((IASTFieldDeclarator) declarator);
		}else if (declarator instanceof ICASTKnRFunctionDeclarator) {
			writeCKnRFunctionDeclarator((ICASTKnRFunctionDeclarator) declarator);
		}else{
			writeDefaultDeclarator(declarator);
		}
		
		if(hasTrailingComments(declarator)) {
			writeTrailingComments(declarator, false);			
		}	
	}

	protected void writeDefaultDeclarator(IASTDeclarator declarator) {
		IASTPointerOperator[] pointOps = declarator.getPointerOperators();
		writePointerOperators(declarator, pointOps);
		IASTName name = declarator.getName();
		name.accept(visitor);
		writeNestedDeclarator(declarator);
		IASTInitializer init = getInitializer(declarator);
		if(init!= null) {
			init.accept(visitor);
		}
	}

	protected void writePointerOperators(IASTDeclarator declarator, IASTPointerOperator[] pointOps) {
		for (IASTPointerOperator operator : pointOps) {
			writePointerOp(operator);
		}
	}

	private void writeFunctionDeclarator(IASTStandardFunctionDeclarator funcDec) {
		IASTPointerOperator[] pointOps = funcDec.getPointerOperators();
		writePointerOperators(funcDec, pointOps);
		funcDec.getName().accept(visitor);
		writeNestedDeclarator(funcDec);
		writeParameters(funcDec);
		writeInitializer(funcDec);
		if (funcDec instanceof ICPPASTFunctionDeclarator) {
			writeCppFunctionDeclarator((ICPPASTFunctionDeclarator) funcDec);
		}
	}

	private void writeInitializer(IASTStandardFunctionDeclarator funcDec) {
		IASTInitializer init = getInitializer(funcDec);
		if(init != null) {
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
		if(nestedDeclarator != null) {
			scribe.print('(');
			nestedDeclarator.accept(visitor);
			scribe.print(')');
		}
	}

	private void writeCppFunctionDeclarator(ICPPASTFunctionDeclarator funcDec) {
		if (funcDec.isConst()) {
			scribe.printSpace();
			scribe.print(CONST);
		}
		if (funcDec.isVolatile()) {
			scribe.printSpace();
			scribe.print(VOLATILE);
		}
		if(funcDec.isPureVirtual()) {
			scribe.print(PURE_VIRTUAL);
		}
		writeExceptionSpecification(funcDec, funcDec.getExceptionSpecification());
	}

	protected void writeExceptionSpecification(ICPPASTFunctionDeclarator funcDec, IASTTypeId[] exceptions) {
		if (exceptions != ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
			scribe.printSpace();
			scribe.print(THROW);
			scribe.print('(');
			writeNodeList(exceptions);
			scribe.print(')');
		}
	}

	protected void writeParameterDeclarations(IASTStandardFunctionDeclarator funcDec, IASTParameterDeclaration[] paraDecls) {
		writeNodeList(paraDecls);
		if(funcDec.takesVarArgs()){
			if(paraDecls.length > 0){
				scribe.print(COMMA_SPACE);
			}
			scribe.print(VAR_ARGS);
		}
	}
	
	private void writePointer(IASTPointer operator) {
		if (operator instanceof ICPPASTPointerToMember) {
			ICPPASTPointerToMember pointerToMemberOp = (ICPPASTPointerToMember) operator;
			if(pointerToMemberOp.getName() != null){
				pointerToMemberOp.getName().accept(visitor);
				scribe.print(STAR_SPACE);
			}
		} else {
			scribe.print('*');
		}
		
		
		if (operator.isConst()) {
			scribe.printStringSpace(CONST);

		}
		if (operator.isVolatile()) {
			scribe.printStringSpace(VOLATILE);
		}
		if (operator instanceof ICASTPointer) {
			ICASTPointer cPoint = (ICASTPointer) operator;
			if(cPoint.isRestrict()) {
				scribe.print(RESTRICT);
			}
		}
		if (operator instanceof IGPPASTPointer) {
			IGPPASTPointer gppPoint = (IGPPASTPointer) operator;
			if(gppPoint.isRestrict()) {
				scribe.print(RESTRICT);
			}
		}
	}

	private void writePointerOp(IASTPointerOperator operator) {
		if (operator instanceof IASTPointer) {
			IASTPointer pointOp = (IASTPointer) operator;
			writePointer(pointOp);
		}else if (operator instanceof ICPPASTReferenceOperator) {
			scribe.print(AMPERSAND_SPACE);
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
		if(initializer != null) {
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
		IASTExpression ex= modifier.getConstantExpression();
		if (ex != null) {
			ex.accept(visitor);
		}
		scribe.print(']');
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
		if(initializer != null) {
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

	protected void writeKnRParameterDeclarations(
			ICASTKnRFunctionDeclarator knrFunct, IASTDeclaration[] knrDeclarations) {
		for (int i = 0; i < knrDeclarations.length;  ++i) {
			scribe.noNewLines();
			knrDeclarations[i].accept(visitor);
			scribe.newLines();
			if(i + 1 < knrDeclarations.length) {
				scribe.newLine();
			}
		}
	}

	protected void writeKnRParameterNames(ICASTKnRFunctionDeclarator knrFunct, IASTName[] parameterNames) {
		writeNodeList(parameterNames);
	}
}
