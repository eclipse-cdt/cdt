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

import org.eclipse.cdt.core.parser.ast2.ASTFactory;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTVariableDeclaration;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ast2.ASTBuiltinType;
import org.eclipse.cdt.internal.core.parser.ast2.ASTTranslationUnit;


/**
 * Test the new AST. Extends SpeedTest so we can get at its nice
 * scanner config stuff.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends SpeedTest {

    private IASTTranslationUnit parse(String code) {
        return ASTFactory.parseString(code, getScannerInfo(false));
    }

    // Also overrides the 'test' up in SpeedTest
    public void test() {
    	String code = "int x;";
    	ASTTranslationUnit tu = (ASTTranslationUnit)parse(code);
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(varDecl.getName().getName(), "x");
    	IASTType type = var.getType();
    	assertNotNull(type);
    	assertTrue(CharArrayUtils.equals(((ASTBuiltinType)type).getName(), "int".toCharArray()));
    }

}
