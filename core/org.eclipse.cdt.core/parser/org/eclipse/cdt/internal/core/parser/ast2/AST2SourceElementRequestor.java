/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast2;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTFunctionDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;
import org.eclipse.cdt.core.parser.ast2.IASTParameter;
import org.eclipse.cdt.core.parser.ast2.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTPointerType;
import org.eclipse.cdt.core.parser.ast2.IASTScope;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTTypeDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTVariableDeclaration;
import org.eclipse.cdt.core.parser.ast2.c.ICASTModifiedType;

/**
 * @author Doug Schaefer
 */
public class AST2SourceElementRequestor extends NullSourceElementRequestor implements IParserLogService {

    private IASTTranslationUnit translationUnit;
    private IASTScope currentScope;
    private IASTDeclaration currentDeclaration;
    
    // Log service functions
    public boolean isTracing() {
        return false;
    }
    
    public void traceLog(String message) {
    }

    public IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    // Utilities
    private void linkDeclaration(IASTDeclaration declaration) {
    	if (currentDeclaration != null)
    		currentDeclaration.setNextDeclaration(declaration);
    	else
    		currentScope.setFirstDeclaration(declaration);

		currentDeclaration = declaration;
    }
    
    // Callback functions
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
		translationUnit = new ASTTranslationUnit();
		currentScope = translationUnit;
		currentDeclaration = null;
	}

	public void acceptVariable(IASTVariable variable) {
		IASTIdentifier name = new ASTIdentifier(variable.getNameCharArray());
		name.setOffset(variable.getNameOffset());
		name.setLength(variable.getNameEndOffset() - variable.getNameOffset());
		
		IASTVariableDeclaration varDecl = new ASTVariableDeclaration();
		varDecl.setName(name);
		linkDeclaration(varDecl);

		org.eclipse.cdt.core.parser.ast2.IASTVariable var = new ASTVariable();
		varDecl.setVariable(var);
		var.setDeclaration(varDecl);
		
		IASTType varType = buildType(variable.getAbstractDeclaration());
		varDecl.setType(varType);
	}

	public void enterFunctionBody(IASTFunction function) {
		IASTIdentifier name = new ASTIdentifier(function.getNameCharArray());

		IASTFunctionDeclaration funcDecl = new ASTFunctionDeclaration();
		funcDecl.setName(name);
		linkDeclaration(funcDecl);

		org.eclipse.cdt.core.parser.ast2.IASTFunction func = new ASTFunction();
		funcDecl.setFunction(func);
		func.setDeclaration(funcDecl);
		
		IASTType returnType = buildType(function.getReturnType());
		funcDecl.setReturnType(returnType);

		IASTParameterDeclaration currentParam = null;
		for (Iterator i = function.getParameters(); i.hasNext(); ) {
			org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration realparam
				= (org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration)i.next();
			IASTParameterDeclaration paramDecl = new ASTParameterDeclaration();
			paramDecl.setName(new ASTIdentifier(realparam.getName()));
			paramDecl.setType(buildType(realparam));
			IASTParameter param = new ASTParameter();
			paramDecl.setVariable(param);
			param.setDeclaration(paramDecl);
			
			if (currentParam == null)
				funcDecl.setFirstParameterDeclaration(paramDecl);
			else
				currentParam.setNextDeclaration(paramDecl);
			
			currentParam = paramDecl;
		}
	}

	private IASTType buildType(IASTAbstractDeclaration abstractDecl) {
		
		IASTType type = null;
		
		IASTTypeSpecifier typeSpecifier = abstractDecl.getTypeSpecifier();
		if (typeSpecifier instanceof IASTSimpleTypeSpecifier) {
			IASTSimpleTypeSpecifier realtype = (IASTSimpleTypeSpecifier)typeSpecifier;
			IASTTypeDeclaration typeDecl
				= (IASTTypeDeclaration)currentScope.findDeclaration(
						new ASTIdentifier(realtype.getTypename()));
			type = typeDecl.getType();
		}
		
		if (abstractDecl.isConst()) {
			ICASTModifiedType modType = new CASTModifiedType();
			modType.setType(type);
			modType.setIsConst(true);
			type = modType;
		}
		
		for (Iterator i = abstractDecl.getPointerOperators(); i.hasNext(); ) {
			ASTPointerOperator pointer = (ASTPointerOperator)i.next();
			IASTPointerType pointerType = new ASTPointerType();
			pointerType.setType(type);
			type = pointerType;
		}
		
		return type;
	}
}
