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

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class DOMLocationTests extends AST2BaseTest {

   private static final String _TEXT_ = "<text>"; //$NON-NLS-1$
   public void testBaseCase() throws ParserException {
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse("int x;", p); //$NON-NLS-1$
         IASTDeclaration declaration = tu.getDeclarations()[0];
         IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
         assertNotNull(nodeLocations);
         assertEquals(nodeLocations.length, 1);
         assertTrue(nodeLocations[0] instanceof IASTFileLocation);
         IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
         assertEquals(fileLocation.getFileName(), _TEXT_); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), 0);
         assertEquals(fileLocation.getNodeLength(), 6);
         IASTNodeLocation[] tuLocations = tu.getNodeLocations();
         assertEquals(tuLocations.length, nodeLocations.length);
         assertEquals(fileLocation.getFileName(),
               ((IASTFileLocation) tuLocations[0]).getFileName()); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), tuLocations[0]
               .getNodeOffset());
         assertEquals(fileLocation.getNodeLength(), tuLocations[0]
               .getNodeLength());
      }
   }

   public void testSimpleDeclaration() throws ParserException {
      String code ="int xLen5, * yLength8, zLength16( int );"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 1);
         IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarations[0];
         IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
         assertNotNull(nodeLocations);
         assertEquals(nodeLocations.length, 1);
         assertTrue(nodeLocations[0] instanceof IASTFileLocation);
         IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
         assertEquals(fileLocation.getFileName(), _TEXT_); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), 0);
         assertEquals(fileLocation.getNodeLength(), code.indexOf( ";") + 1); //$NON-NLS-1$
         IASTDeclarator[] declarators = declaration.getDeclarators();
         assertEquals( declarators.length, 3 );
         for( int i = 0; i < 3; ++i )
         {
            IASTDeclarator declarator = declarators[i];
            switch( i )
            {
               case 0:
                  assertSoleLocation( declarator, code.indexOf( "xLen5"), "xLen5".length() ); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
               case 1:
                  assertSoleLocation( declarator, code.indexOf( "* yLength8"), "* yLength8".length()); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
               case 2:
                  assertSoleLocation( declarator, code.indexOf( "zLength16( int )"), "zLength16( int )".length() ); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
            }
         }
         
      }
   }

   
   public void testSimpleObjectStyleMacroDefinition() throws Exception {
      String code ="/* hi */\n#define FOOT 0x01\n\n"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 0);
         IASTPreprocessorMacroDefinition [] macros = tu.getMacroDefinitions();
         assertNotNull( macros );
         assertEquals( macros.length, 1 );
         assertSoleLocation( macros[0], code.indexOf( "#"), code.indexOf( "0x01") + 4 - code.indexOf( "#")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         assertTrue( macros[0] instanceof IASTPreprocessorObjectStyleMacroDefinition );
         assertEquals( macros[0].getName().toString(), "FOOT" ); //$NON-NLS-1$
         assertEquals( macros[0].getExpansion(), "0x01"); //$NON-NLS-1$
      }
   }
   

   public void testSimpleFunctionStyleMacroDefinition() throws Exception {
      String code = "#define FOOBAH( WOOBAH ) JOHN##WOOBAH\n\n"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 0);
         IASTPreprocessorMacroDefinition [] macros = tu.getMacroDefinitions();
         assertNotNull( macros );
         assertEquals( macros.length, 1 );
         assertTrue( macros[0] instanceof IASTPreprocessorFunctionStyleMacroDefinition );
         assertSoleLocation( macros[0], code.indexOf( "#define"), code.indexOf( "##WOOBAH") + 8 - code.indexOf( "#define")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$s
         assertEquals( macros[0].getName().toString(), "FOOBAH" ); //$NON-NLS-1$
         assertEquals( macros[0].getExpansion(), "JOHN##WOOBAH"); //$NON-NLS-1$
         IASTFunctionStyleMacroParameter [] parms = ((IASTPreprocessorFunctionStyleMacroDefinition)macros[0]).getParameters();
         assertNotNull( parms );
         assertEquals( parms.length, 1 );
         assertEquals( parms[0].getParameter(), "WOOBAH" ); //$NON-NLS-1$
      }
      
   }
   
   /**
    * @param declarator
    * @param offset
    * @param length
    */
   private void assertSoleLocation(IASTNode n, int offset, int length) {
      IASTNodeLocation [] locations = n.getNodeLocations();
      assertEquals( 1, locations.length );
      IASTNodeLocation nodeLocation = locations[0];
      assertEquals( offset, nodeLocation.getNodeOffset() );
      assertEquals( length, nodeLocation.getNodeLength() );
   }
   
   public void testBug83664() throws Exception {
       String code = "int foo(x) int x; {\n 	return x;\n   }\n"; //$NON-NLS-1$
       IASTTranslationUnit tu = parse( code, ParserLanguage.C );
       IASTDeclaration [] declarations = tu.getDeclarations();
       assertEquals( declarations.length, 1 );
       IASTFunctionDefinition definition = (IASTFunctionDefinition) declarations[0];
       IASTFunctionDeclarator declarator = definition.getDeclarator();
       assertSoleLocation( declarator, code.indexOf( "foo" ), code.indexOf( "int x;") + 6 - code.indexOf( "foo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
       IASTCompoundStatement body = (IASTCompoundStatement) definition.getBody();
       assertEquals( body.getStatements().length,  1 );
       IASTReturnStatement returnStatement= (IASTReturnStatement) body.getStatements()[0];
       IASTIdExpression expression = (IASTIdExpression) returnStatement.getReturnValue();
       assertSoleLocation( expression, code.indexOf( "return ") + "return ".length(), 1 ); //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   public void testBug84343() throws Exception {
      String code = "class A {}; int f() {\nA * b = 0;\nreturn b;}"; //$NON-NLS-1$
      IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
      IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[1];
      IASTDeclarationStatement ds = (IASTDeclarationStatement) ((IASTCompoundStatement)f.getBody()).getStatements()[0];
      IASTSimpleDeclaration b = (IASTSimpleDeclaration) ds.getDeclaration();
      ICPPASTNamedTypeSpecifier namedTypeSpec = (ICPPASTNamedTypeSpecifier) b.getDeclSpecifier();
      assertSoleLocation( namedTypeSpec, code.indexOf( "\nA") + 1, 1 ); //$NON-NLS-1$
   }
   
   public void testBug84366() throws Exception {
      String code = "enum hue { red, blue, green };"; //$NON-NLS-1$
      IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
      IASTSimpleDeclaration d = (IASTSimpleDeclaration) tu.getDeclarations()[0];
      IASTEnumerationSpecifier enum = (IASTEnumerationSpecifier) d.getDeclSpecifier();
      IASTEnumerationSpecifier.IASTEnumerator enumerator = enum.getEnumerators()[0];
      assertSoleLocation( enumerator, code.indexOf( "red"), "red".length() ); //$NON-NLS-1$ //$NON-NLS-2$
   }

   public void testBug84375() throws Exception {
      String code = "class D { public: int x; };\nclass C : public virtual D {};"; //$NON-NLS-1$
      IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
      IASTSimpleDeclaration d2 = (IASTSimpleDeclaration) tu.getDeclarations()[1];
      ICPPASTCompositeTypeSpecifier classSpec = (ICPPASTCompositeTypeSpecifier) d2.getDeclSpecifier();
      ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier [] bases = classSpec.getBaseSpecifiers();
      assertSoleLocation( bases[0], code.indexOf( "public virtual D"), "public virtual D".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      
      
   }
   
   public void testBug84357() throws Exception {
      String code = "class X {	int a;\n};\nint X::  * pmi = &X::a;"; //$NON-NLS-1$
      IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
      IASTSimpleDeclaration pmi = (IASTSimpleDeclaration) tu.getDeclarations()[1];
      IASTDeclarator d = pmi.getDeclarators()[0];
      IASTPointerOperator p = d.getPointerOperators()[0];
      assertSoleLocation( p, code.indexOf( "X::  *") , "X::  *".length()); //$NON-NLS-1$ //$NON-NLS-2$
   }
   public void testBug84367() throws Exception {
      String code = "void foo(   int   );"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p);
         IASTSimpleDeclaration definition = (IASTSimpleDeclaration) tu.getDeclarations()[0];
         IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) definition.getDeclarators()[0];
         IASTParameterDeclaration parameter = declarator.getParameters()[0];
         assertSoleLocation( parameter, code.indexOf( "int" ), 3 ); //$NON-NLS-1$
         
      }      
   }
   public void testElaboratedTypeSpecifier() throws ParserException {
      String code = "/* blah */ struct A anA; /* blah */"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
         IASTElaboratedTypeSpecifier elabType = (IASTElaboratedTypeSpecifier) declaration.getDeclSpecifier();
         assertSoleLocation( elabType, code.indexOf( "struct"), "struct A".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
   }

   
   public void testBug83852() throws Exception {
      String code = "/* blah */ typedef short jc;  int x = 4;  jc myJc = (jc)x; "; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration [] declarations = tu.getDeclarations();
         assertEquals( 3, declarations.length );
         for( int i = 0; i < 3; ++i )
         {
            IASTSimpleDeclaration decl = (IASTSimpleDeclaration) declarations[i];
            int start = 0, length = 0;
            switch( i )
            {
               case 0:
                  start = code.indexOf( "typedef"); //$NON-NLS-1$
                  length = "typedef short jc;".length(); //$NON-NLS-1$
                  break;
               case 1:
                  start = code.indexOf( "int x = 4;"); //$NON-NLS-1$
                  length = "int x = 4;".length(); //$NON-NLS-1$
                  break;
               case 2:
                  start = code.indexOf( "jc myJc = (jc)x;"); //$NON-NLS-1$
                  length = "jc myJc = (jc)x;".length(); //$NON-NLS-1$
                  break;
            }
            assertSoleLocation( decl, start, length );
         }
         IASTInitializerExpression initializer = (IASTInitializerExpression) ((IASTSimpleDeclaration)declarations[2]).getDeclarators()[0].getInitializer();
         IASTCastExpression castExpression = (IASTCastExpression) initializer.getExpression();
         IASTTypeId typeId = castExpression.getTypeId();
         assertSoleLocation( typeId, code.indexOf( "(jc)") + 1, "jc".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
   }
   
   public void testBug83853() throws ParserException {
      String code = "int f() {return (1?0:1);	}"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p);
         IASTFunctionDefinition definition = (IASTFunctionDefinition) tu.getDeclarations()[0];
         IASTCompoundStatement statement = (IASTCompoundStatement) definition.getBody();
         IASTReturnStatement returnStatement = (IASTReturnStatement) statement.getStatements()[0];
         IASTUnaryExpression unaryExpression = (IASTUnaryExpression) returnStatement.getReturnValue();
         assertEquals( unaryExpression.getOperator(), IASTUnaryExpression.op_bracketedPrimary );
         IASTConditionalExpression conditional = (IASTConditionalExpression) unaryExpression.getOperand();
         assertSoleLocation( conditional,code.indexOf( "1?0:1"), "1?0:1".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      }      
   }
   
   public void testBug84374() throws Exception
   {
      String code = "class P1 { public: int x; };\nclass P2 { public: int x; };\nclass B : public P1, public P2 {};\nvoid main() {\nB * b = new B();\n}"; //$NON-NLS-1$
      IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
      IASTFunctionDefinition main = (IASTFunctionDefinition) tu.getDeclarations()[3];
      IASTCompoundStatement statement = (IASTCompoundStatement) main.getBody();
      IASTDeclarationStatement decl = (IASTDeclarationStatement) statement.getStatements()[0];
      IASTSimpleDeclaration b = (IASTSimpleDeclaration) decl.getDeclaration();
      IASTInitializerExpression initializerExpression = (IASTInitializerExpression) b.getDeclarators()[0].getInitializer();
      assertSoleLocation( initializerExpression, code.indexOf( "new B()"), "new B()".length() ); //$NON-NLS-1$ //$NON-NLS-2$
      ICPPASTNewExpression newExpression = (ICPPASTNewExpression) initializerExpression.getExpression();
      assertSoleLocation( newExpression, code.indexOf( "new B()"), "new B()".length() ); //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   public void testBug83737() throws Exception {
      String code = "void f() {  if( a == 0 ) g( a ); else if( a < 0 ) g( a >> 1 ); else if( a > 0 ) g( *(&a + 2) ); }"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p);
         IASTFunctionDefinition definition = (IASTFunctionDefinition) tu.getDeclarations()[0];
         IASTCompoundStatement statement = (IASTCompoundStatement) definition.getBody();
         IASTIfStatement first_if = (IASTIfStatement) statement.getStatements()[0];
         IASTIfStatement second_if = (IASTIfStatement) first_if.getElseClause();
         IASTIfStatement third_if = (IASTIfStatement) second_if.getElseClause();
         assertNull( third_if.getElseClause() );
         int first_if_start = code.indexOf( "if( a == 0 )" ); //$NON-NLS-1$
         int total_if_length = "if( a == 0 ) g( a ); else if( a < 0 ) g( a >> 1 ); else if( a > 0 ) g( *(&a + 2) );".length(); //$NON-NLS-1$
         int total_if_end = first_if_start + total_if_length;
         int second_if_start = code.indexOf( "if( a < 0 )"); //$NON-NLS-1$
         int third_if_start = code.indexOf( "if( a > 0 )"); //$NON-NLS-1$
         assertSoleLocation( first_if, first_if_start, total_if_length );
         assertSoleLocation( second_if, second_if_start, total_if_end - second_if_start );
         assertSoleLocation( third_if, third_if_start, total_if_end - third_if_start );
      }      
   }
   
   public void testBug84467() throws Exception
   {
   		String code = "class D { };\n D d1;\n const D d2;\n void foo() {\n typeid(d1) == typeid(d2);\n }"; //$NON-NLS-1$
   	  	IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
   	  	IASTBinaryExpression bexp = (IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)((IASTFunctionDefinition)tu.getDeclarations()[3]).getBody()).getStatements()[0]).getExpression();
   	  	IASTTypeIdExpression exp = (IASTTypeIdExpression)((IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)((IASTFunctionDefinition)tu.getDeclarations()[3]).getBody()).getStatements()[0]).getExpression()).getOperand1();
   	  	
   	  	assertSoleLocation( bexp, code.indexOf( "typeid(d1) == typeid(d2)"), "typeid(d1) == typeid(d2)".length() ); //$NON-NLS-1$ //$NON-NLS-2$
   	  	assertSoleLocation( exp, code.indexOf( "typeid(d1)"), "typeid(d1)".length() ); //$NON-NLS-1$ //$NON-NLS-2$
   	  	exp = (IASTTypeIdExpression)((IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)((IASTFunctionDefinition)tu.getDeclarations()[3]).getBody()).getStatements()[0]).getExpression()).getOperand2();
   	  	assertSoleLocation( exp, code.indexOf( "typeid(d2)"), "typeid(d2)".length() ); //$NON-NLS-1$ //$NON-NLS-2$
   }
   
   public void testBug84576() throws Exception
   {
   		String code = "namespace A {\n extern \"C\" int g();\n }"; //$NON-NLS-1$
   	  	IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
   	  	ICPPASTLinkageSpecification spec = (ICPPASTLinkageSpecification)((ICPPASTNamespaceDefinition)tu.getDeclarations()[0]).getDeclarations()[0];
   	  	assertSoleLocation( spec, code.indexOf( "extern \"C\""), "extern \"C\"".length() ); //$NON-NLS-1$ //$NON-NLS-2$
   }

}