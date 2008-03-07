/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
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
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.DeclaratorWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.Scribe;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;


public class ModifiedASTDeclaratorWriter extends DeclaratorWriter {

	private final ASTModificationHelper modificationHelper;

	public ModifiedASTDeclaratorWriter(Scribe scribe, CPPASTVisitor visitor, ASTModificationStore modificationStore, NodeCommentMap commentMap) {
		super(scribe, visitor, commentMap);
		this.modificationHelper = new ASTModificationHelper(modificationStore);
	}

	@Override
	protected void writeParameterDeclarations(
			IASTStandardFunctionDeclarator funcDec,
			IASTParameterDeclaration[] paraDecls) {	
		IASTParameterDeclaration[] modifiedParameters = modificationHelper.createModifiedChildArray(funcDec, paraDecls);			
		super.writeParameterDeclarations(funcDec, modifiedParameters);
		
	}

	@Override
	protected void writePointerOperators(IASTDeclarator declarator,IASTPointerOperator[] unmodifiedPointerOperations) {
		IASTPointerOperator[] modifiedPointer = modificationHelper.createModifiedChildArray(declarator, unmodifiedPointerOperations);
		super.writePointerOperators(declarator, modifiedPointer);
	}
	
	

	@Override
	protected void writeCtorChainInitializer(ICPPASTFunctionDeclarator funcDec,
			ICPPASTConstructorChainInitializer[] ctorInitChain) {
		ICPPASTConstructorChainInitializer[] modifiedChainInitializer = modificationHelper.createModifiedChildArray(funcDec, ctorInitChain);
		super.writeCtorChainInitializer(funcDec, modifiedChainInitializer);
	}

	@Override
	protected void writeArrayModifiers(IASTArrayDeclarator arrDecl,
			IASTArrayModifier[] arrMods) {
		IASTArrayModifier[] modifiedModifiers = modificationHelper.createModifiedChildArray(arrDecl, arrMods);
		super.writeArrayModifiers(arrDecl, modifiedModifiers);
	}

	@Override
	protected void writeExceptionSpecification(ICPPASTFunctionDeclarator funcDec, IASTTypeId[] exceptions ) {	
		IASTTypeId[] modifiedExceptions = modificationHelper.createModifiedChildArray(funcDec, exceptions);
		super.writeExceptionSpecification(funcDec, modifiedExceptions);
	}
	
	
	
	@Override
	protected void writeKnRParameterDeclarations(
			ICASTKnRFunctionDeclarator knrFunct,
			IASTDeclaration[] knrDeclarations) {
		IASTDeclaration[] modifiedDeclarations = modificationHelper.createModifiedChildArray(knrFunct, knrDeclarations);
		
		super.writeKnRParameterDeclarations(knrFunct, modifiedDeclarations);
	}

	@Override
	protected void writeKnRParameterNames(
			ICASTKnRFunctionDeclarator knrFunct, IASTName[] parameterNames) {
		IASTName[] modifiedNames = modificationHelper.createModifiedChildArray(knrFunct, parameterNames);
		super.writeKnRParameterNames(knrFunct, modifiedNames);
	}

	@Override
	protected IASTInitializer getInitializer(IASTDeclarator decl) {
		return modificationHelper.getInitializer(decl);
	}
}
