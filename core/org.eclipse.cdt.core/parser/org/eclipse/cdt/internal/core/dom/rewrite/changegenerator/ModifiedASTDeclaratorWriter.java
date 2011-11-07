/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.DeclaratorWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.Scribe;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

public class ModifiedASTDeclaratorWriter extends DeclaratorWriter {
	private final ASTModificationHelper modificationHelper;

	public ModifiedASTDeclaratorWriter(Scribe scribe, ASTWriterVisitor visitor, ModificationScopeStack stack, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		this.modificationHelper = new ASTModificationHelper(stack);
	}

	@Override
	protected void writeParameterDeclarations(IASTStandardFunctionDeclarator funcDec,
			IASTParameterDeclaration[] paraDecls) {	
		IASTParameterDeclaration[] modifiedParameters =	modificationHelper.createModifiedChildArray(
				funcDec, paraDecls, IASTParameterDeclaration.class, commentMap);
		super.writeParameterDeclarations(funcDec, modifiedParameters);
	}

	@Override
	protected void writePointerOperators(IASTDeclarator declarator,
			IASTPointerOperator[] unmodifiedPointerOperations) {
		IASTPointerOperator[] modifiedPointer = modificationHelper.createModifiedChildArray(
				declarator, unmodifiedPointerOperations, IASTPointerOperator.class, commentMap);
		super.writePointerOperators(declarator, modifiedPointer);
	}
	
	@Override
	protected void writeArrayModifiers(IASTArrayDeclarator arrDecl, IASTArrayModifier[] arrMods) {
		IASTArrayModifier[] modifiedModifiers = modificationHelper.createModifiedChildArray(arrDecl,
				arrMods, IASTArrayModifier.class, commentMap);
		super.writeArrayModifiers(arrDecl, modifiedModifiers);
	}

	@Override
	protected void writeExceptionSpecification(ICPPASTFunctionDeclarator funcDec,
			IASTTypeId[] exceptions) {	
		IASTTypeId[] modifiedExceptions = modificationHelper.createModifiedChildArray(funcDec,
				exceptions, IASTTypeId.class, commentMap);
		// it makes a difference whether the exception array is identical to 
		// ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION
		if (modifiedExceptions.length == 0 &&
				exceptions == ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
			modifiedExceptions= ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION;
		}
		
		super.writeExceptionSpecification(funcDec, modifiedExceptions);
	}
	
	@Override
	protected void writeKnRParameterDeclarations(ICASTKnRFunctionDeclarator knrFunct,
			IASTDeclaration[] knrDeclarations) {
		IASTDeclaration[] modifiedDeclarations = modificationHelper.createModifiedChildArray(
				knrFunct, knrDeclarations, IASTDeclaration.class, commentMap);
		super.writeKnRParameterDeclarations(knrFunct, modifiedDeclarations);
	}

	@Override
	protected void writeKnRParameterNames(ICASTKnRFunctionDeclarator knrFunct,
			IASTName[] parameterNames) {
		IASTName[] modifiedNames = modificationHelper.createModifiedChildArray(knrFunct,
				parameterNames, IASTName.class, commentMap);
		super.writeKnRParameterNames(knrFunct, modifiedNames);
	}

	@Override
	protected IASTInitializer getInitializer(IASTDeclarator decl) {
		return modificationHelper.getInitializer(decl);
	}
}
