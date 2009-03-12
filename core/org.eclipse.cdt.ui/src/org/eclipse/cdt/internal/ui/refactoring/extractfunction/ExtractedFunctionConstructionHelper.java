/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;

import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;

/**
 * @author Mirko Stocker
 * 
 */
public abstract class ExtractedFunctionConstructionHelper {
	
	public static ExtractedFunctionConstructionHelper createFor (List<IASTNode> list) {
		if(list.get(0) instanceof IASTExpression) {
			return new ExtractExpression();
		}
		return new ExtractStatement();
	}
	
	public abstract void constructMethodBody(IASTCompoundStatement compound,
			List<IASTNode> list, ASTRewrite rewrite, TextEditGroup group);

	public abstract IASTDeclSpecifier determineReturnType(IASTNode extractedNode, NameInformation returnVariable);

	public abstract IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt, IASTExpression callExpression);
	
	protected boolean isReturnTypeAPointer(IASTNode node) {
		return false;
	}

	IASTStandardFunctionDeclarator createFunctionDeclarator(IASTName name, ICPPASTFunctionDeclarator functionDeclarator, NameInformation returnVariable, List<IASTNode> nodesToWrite, Collection<NameInformation> allUsedNames) {
		ICPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
		declarator.setName(name);
	
		if(functionDeclarator != null && functionDeclarator.isConst()) {
			declarator.setConst(true);
		}
		
		if(returnVariable != null) {
			IASTDeclarator decl = (IASTDeclarator) returnVariable.getDeclaration().getParent();
			IASTPointerOperator[] pointers = decl.getPointerOperators();
			for (IASTPointerOperator operator : pointers) {
				declarator.addPointerOperator(operator.copy());
			}
		}
	
		for (ICPPASTParameterDeclaration param : getParameterDeclarations(allUsedNames)) {
			declarator.addParameterDeclaration(param);
		}
		
		if(isReturnTypeAPointer(nodesToWrite.get(0))) {
			declarator.addPointerOperator(new CPPASTPointer());
		}
		
		return declarator;
	}
	
	public Collection<ICPPASTParameterDeclaration> getParameterDeclarations(Collection<NameInformation> allUsedNames) {
		Collection<ICPPASTParameterDeclaration> result = new ArrayList<ICPPASTParameterDeclaration>();		
		for (NameInformation name : allUsedNames) {
			if(!name.isDeclarationInScope()){
				result.add(name.getICPPASTParameterDeclaration(name.isUserSetIsReference()));
			}
		}
		return result;
	}
}
