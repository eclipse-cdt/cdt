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
import org.eclipse.cdt.core.parser.ast2.IASTFunction;
import org.eclipse.cdt.core.parser.ast2.IASTFunctionDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTParameterDeclaration;
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
    	IASTType type = varDecl.getType();
    	assertNotNull(type);
    	assertEquals(type.getDeclaration().getName(), new ASTIdentifier("int"));
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(var.getDeclaration(), varDecl);
    }

    public void testPointerVariable() {
    	String code = "int * x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	assertEquals(varDecl.getName(), new ASTIdentifier("x"));
    	IASTPointerType pointerType = (IASTPointerType)varDecl.getType();
    	IASTType type = pointerType.getType();
    	assertEquals(type.getDeclaration().getName(), new ASTIdentifier("int"));
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(var.getDeclaration(), varDecl);
    }
    
    public void testConstPointerVar() {
    	String code = "const int * x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	assertEquals(varDecl.getName(), new ASTIdentifier("x"));
    	IASTPointerType pointerType = (IASTPointerType)varDecl.getType();
    	ICASTModifiedType modType = (ICASTModifiedType)pointerType.getType();
    	assertTrue(modType.isConst());
    	IASTType type = modType.getType();
    	assertEquals(type.getDeclaration().getName(), new ASTIdentifier("int"));
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(var.getDeclaration(), varDecl);
    }

    public void testEmptyFunction() {
    	String code = "void f() { }";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTFunctionDeclaration funcDecl = (IASTFunctionDeclaration)tu.getFirstDeclaration();
    	assertEquals(funcDecl.getName(), new ASTIdentifier("f"));
    	IASTType returnType = funcDecl.getReturnType();
    	assertEquals(returnType.getDeclaration().getName(), new ASTIdentifier("void"));
    	IASTFunction function = funcDecl.getFunction();
    	assertEquals(function.getDeclaration(), funcDecl);
    	assertNull(function.getBody());
    }

    public void testFunctWithRefs() {
    	String code
			= "int x;\n" 
    		+ "void f(int y) {\n"
			+ "  int z = x + y\n"
			+ "}";
    	// <variableDeclaration name="x">
    	//   <variable/>
    	// </variableDeclaration>
    	// <functionDeclaration name="f">
    	//   <parameters>
    	//     <parameterDeclaration name="y">
    	//       <parameter/>
    	//     </parameterDeclaration>
    	//   </parameters>
    	//   <body>
    	//     <declarationStatement>
    	//       <variableDeclaration name="z">
    	//         <variable>
    	//           <initExpression>
    	//             <additiveExpression>
    	//               <variableReference - link back to var x/>
    	//               <variableReference - link back to var y/>
    	//             </additiveExpression>
    	//           </initExpression>
    	//         </variable>
    	//       </variableDeclaration>
    	//     </declarationStatement>
    	//   </body>
    	// <functionDeclaration>
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	// int x;
    	IASTVariableDeclaration xDecl = (IASTVariableDeclaration)tu.getFirstDeclaration();
    	assertEquals(new ASTIdentifier("x"), xDecl.getName());
    	assertEquals(new ASTIdentifier("int"), xDecl.getType().getDeclaration().getName());
    	assertEquals(xDecl, xDecl.getVariable().getDeclaration());
    	// void f()
    	IASTFunctionDeclaration fDecl = (IASTFunctionDeclaration)xDecl.getNextDeclaration();
    	assertEquals(new ASTIdentifier("void"), fDecl.getReturnType().getDeclaration().getName());
    	assertEquals(new ASTIdentifier("f"), fDecl.getName());
    	// int y
    	IASTParameterDeclaration yDecl = fDecl.getFirstParameterDeclaration();
    	assertEquals(new ASTIdentifier("y"), yDecl.getName());
    	assertEquals(new ASTIdentifier("int"), yDecl.getType().getDeclaration().getName());
    }

}
