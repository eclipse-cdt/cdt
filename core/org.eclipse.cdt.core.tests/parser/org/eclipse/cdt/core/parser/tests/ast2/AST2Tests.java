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
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast2.ASTFactory;
import org.eclipse.cdt.core.parser.ast2.IASTFunction;
import org.eclipse.cdt.core.parser.ast2.IASTFunctionDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTPointerType;
import org.eclipse.cdt.core.parser.ast2.IASTScope;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTVariableDeclaration;
import org.eclipse.cdt.core.parser.ast2.c.ICASTModifiedType;

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
    	IASTScope global = (IASTScope)tu;
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)global.getFirstDeclaration();
    	//assertEquals(4, varDecl.getName().getOffset());
    	//assertEquals(1, varDecl.getName().getLength());
    	IASTType type = varDecl.getType();
    	assertNotNull(type);
    	assertEquals("int", type.getDeclaration().getName().toString());
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(var.getDeclaration(), varDecl);
    }

    public void testPointerVariable() {
    	String code = "int * x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTScope global = (IASTScope)tu;
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)global.getFirstDeclaration();
    	assertEquals("x", varDecl.getName().toString());
    	IASTPointerType pointerType = (IASTPointerType)varDecl.getType();
    	IASTType type = pointerType.getType();
    	assertEquals("int", type.getDeclaration().getName().toString());
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(var.getDeclaration(), varDecl);
    }
    
    public void testConstPointerVar() {
    	String code = "const int * x;";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTScope global = (IASTScope)tu;
    	IASTVariableDeclaration varDecl = (IASTVariableDeclaration)global.getFirstDeclaration();
    	assertEquals("x", varDecl.getName().toString());
    	IASTPointerType pointerType = (IASTPointerType)varDecl.getType();
    	ICASTModifiedType modType = (ICASTModifiedType)pointerType.getType();
    	assertTrue(modType.isConst());
    	IASTType type = modType.getType();
    	assertEquals("int", type.getDeclaration().getName().toString());
    	IASTVariable var = varDecl.getVariable();
    	assertEquals(var.getDeclaration(), varDecl);
    }

    public void testEmptyFunction() {
    	String code = "void f() { }";
    	IASTTranslationUnit tu = (IASTTranslationUnit)parse(code);
    	IASTScope global = (IASTScope)tu;
    	IASTFunctionDeclaration funcDecl = (IASTFunctionDeclaration)global.getFirstDeclaration();
    	assertEquals("f", funcDecl.getName().toString());
    	IASTType returnType = funcDecl.getReturnType();
    	assertEquals("void", returnType.getDeclaration().getName().toString());
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
    	IASTScope global = (IASTScope)tu;
    	// int x;
    	IASTVariableDeclaration xDecl = (IASTVariableDeclaration)global.getFirstDeclaration();
    	assertEquals("x", xDecl.getName().toString());
    	assertEquals("int", xDecl.getType().getDeclaration().getName().toString());
    	assertEquals(xDecl, xDecl.getVariable().getDeclaration());
    	// void f()
    	IASTFunctionDeclaration fDecl = (IASTFunctionDeclaration)xDecl.getNextDeclaration();
    	assertEquals("void", fDecl.getReturnType().getDeclaration().getName().toString());
    	assertEquals("f", fDecl.getName().toString());
    	// int y
    	IASTParameterDeclaration yDecl = fDecl.getFirstParameterDeclaration();
    	assertEquals("y", yDecl.getName().toString());
    	assertEquals("int", yDecl.getType().getDeclaration().getName().toString());

    	IASTFunction func = fDecl.getFunction();
    	assertEquals(fDecl, func.getDeclaration());
    }

}
