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

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTTypeDeclaration;

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
		ASTIdentifier name = new ASTIdentifier();
		name.setName(variable.getNameCharArray());
		
		ASTVariableDeclaration varDecl = new ASTVariableDeclaration();
		varDecl.setName(name);

		ASTVariable var = new ASTVariable();
		
		varDecl.setVariable(var);
		linkDeclaration(varDecl);
		
		IASTTypeSpecifier typeSpecifier = variable.getAbstractDeclaration().getTypeSpecifier();
		if (typeSpecifier instanceof IASTSimpleTypeSpecifier) {
			IASTSimpleTypeSpecifier realtype = (IASTSimpleTypeSpecifier)typeSpecifier;
			
			IASTDeclaration decl = currentScope.findDeclaration(realtype.getTypename().toCharArray());
			if (decl != null && decl instanceof IASTTypeDeclaration)
				var.setType(((IASTTypeDeclaration)decl).getType());
		}
	}

}
