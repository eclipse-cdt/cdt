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
package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast2.ASTFactory;
import org.eclipse.cdt.core.parser.ast2.IASTBuiltinType;
import org.eclipse.cdt.core.parser.ast2.IASTPointerType;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTVariableDeclaration;
import org.eclipse.cdt.core.parser.ast2.c.ICASTModifiedType;
import org.eclipse.cdt.internal.core.parser.ast2.ASTIdentifier;

/**
 * Test the new AST.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends TestCase {

    private IASTTranslationUnit parse(String code) {
        return ASTFactory.parseString(code, new ScannerInfo());
    }

    public void testVariable() {
    	String code = "int x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	assertEquals(4, varDecl.getName().getOffset());
    	assertEquals(1, varDecl.getName().getLength());
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(varDecl.getName(), new ASTIdentifier("x"));
    	IASTType type = var.getType();
    	assertNotNull(type);
    	assertEquals(((IASTBuiltinType)type).getName(), new ASTIdentifier("int"));
    }

    public void testPointerVariable() {
    	String code = "int * x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(varDecl.getName(), new ASTIdentifier("x"));
    	IASTPointerType pointerType = (IASTPointerType)var.getType();
    	IASTType type = pointerType.getType();
    	assertEquals(((IASTBuiltinType)type).getName(), new ASTIdentifier("int"));
    }
    
    public void testConstPointerVar() {
    	String code = "const int * x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(varDecl.getName(), new ASTIdentifier("x"));
    	IASTPointerType pointerType = (IASTPointerType)var.getType();
    	ICASTModifiedType modType = (ICASTModifiedType)pointerType.getType();
    	assertTrue(modType.isConst());
    	IASTType type = modType.getType();
    	assertEquals(((IASTBuiltinType)type).getName(), new ASTIdentifier("int"));
    }

}
