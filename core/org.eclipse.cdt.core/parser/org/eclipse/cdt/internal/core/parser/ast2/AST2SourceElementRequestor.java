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
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.c.ICASTModifiedType;

/**
 * @author Doug Schaefer
 */
public class AST2SourceElementRequestor extends NullSourceElementRequestor implements IParserLogService {

    private ASTTranslationUnit translationUnit;
    private ASTScope currentScope;
    private ASTDeclaration currentDeclaration;
    
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
    private void linkDeclaration(ASTDeclaration declaration) {
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
		
		ASTVariableDeclaration varDecl = new ASTVariableDeclaration();
		varDecl.setName(name);

		ASTVariable var = new ASTVariable();
		
		varDecl.setVariable(var);
		linkDeclaration(varDecl);
		
		IASTType varType = null;
		
		IASTAbstractDeclaration abstractDecl = variable.getAbstractDeclaration();
		IASTTypeSpecifier typeSpecifier = abstractDecl.getTypeSpecifier();
		if (typeSpecifier instanceof IASTSimpleTypeSpecifier) {
			IASTSimpleTypeSpecifier realtype = (IASTSimpleTypeSpecifier)typeSpecifier;
			varType = currentScope.findType(new ASTIdentifier(realtype.getTypename()));
		}
		
		if (abstractDecl.isConst()) {
			ICASTModifiedType modType = new CASTModifiedType();
			modType.setType(varType);
			modType.setIsConst(true);
			varType = modType;
		}
		
		for (Iterator i = abstractDecl.getPointerOperators(); i.hasNext(); ) {
			ASTPointerOperator pointer = (ASTPointerOperator)i.next();
			ASTPointerType pointerType = new ASTPointerType();
			pointerType.setType(varType);
			varType = pointerType;
		}
		
		var.setType(varType);
	}

}
