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

import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * Test the new AST.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends AST2BaseTest {

    public void testBasicFunction() throws Exception {
        StringBuffer buff = new StringBuffer();
        buff.append("int x;\n"); //$NON-NLS-1$
        buff.append("void f(int y) {\n"); //$NON-NLS-1$
        buff.append("   int z = x + y;\n"); //$NON-NLS-1$
        buff.append("}\n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
        IScope globalScope = tu.getScope();

        IASTDeclaration[] declarations = tu.getDeclarations();

        // int x
        IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) declarations[0];
        IASTSimpleDeclSpecifier declspec_x = (IASTSimpleDeclSpecifier) decl_x
                .getDeclSpecifier();
        assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_x.getType());
        IASTDeclarator declor_x = decl_x.getDeclarators()[0];
        IASTName name_x = declor_x.getName();
        assertEquals("x", name_x.toString()); //$NON-NLS-1$

        // function - void f()
        IASTFunctionDefinition funcdef_f = (IASTFunctionDefinition) declarations[1];
        IASTSimpleDeclSpecifier declspec_f = (IASTSimpleDeclSpecifier) funcdef_f
                .getDeclSpecifier();
        assertEquals(IASTSimpleDeclSpecifier.t_void, declspec_f.getType());
        IASTFunctionDeclarator declor_f = funcdef_f.getDeclarator();
        IASTName name_f = declor_f.getName();
        assertEquals("f", name_f.toString()); //$NON-NLS-1$

        // parameter - int y
        assertTrue(declor_f instanceof IASTStandardFunctionDeclarator);
        IASTParameterDeclaration decl_y = ((IASTStandardFunctionDeclarator) declor_f)
                .getParameters()[0];
        IASTSimpleDeclSpecifier declspec_y = (IASTSimpleDeclSpecifier) decl_y
                .getDeclSpecifier();
        assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_y.getType());
        IASTDeclarator declor_y = decl_y.getDeclarator();
        IASTName name_y = declor_y.getName();
        assertEquals("y", name_y.toString()); //$NON-NLS-1$

        // int z
        IASTCompoundStatement body_f = (IASTCompoundStatement) funcdef_f
                .getBody();
        IASTDeclarationStatement declstmt_z = (IASTDeclarationStatement) body_f
                .getStatements()[0];
        IASTSimpleDeclaration decl_z = (IASTSimpleDeclaration) declstmt_z
                .getDeclaration();
        IASTSimpleDeclSpecifier declspec_z = (IASTSimpleDeclSpecifier) decl_z
                .getDeclSpecifier();
        assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_z.getType());
        IASTDeclarator declor_z = decl_z.getDeclarators()[0];
        IASTName name_z = declor_z.getName();
        assertEquals("z", name_z.toString()); //$NON-NLS-1$

        // = x + y
        IASTInitializerExpression initializer = (IASTInitializerExpression) declor_z
                .getInitializer();
        IASTBinaryExpression init_z = (IASTBinaryExpression) initializer
                .getExpression();
        assertEquals(IASTBinaryExpression.op_plus, init_z.getOperator());
        IASTIdExpression ref_x = (IASTIdExpression) init_z.getOperand1();
        IASTName name_ref_x = ref_x.getName();
        assertEquals("x", name_ref_x.toString()); //$NON-NLS-1$

        IASTIdExpression ref_y = (IASTIdExpression) init_z.getOperand2();
        IASTName name_ref_y = ref_y.getName();
        assertEquals("y", name_ref_y.toString()); //$NON-NLS-1$

        // BINDINGS
        // resolve the binding to get the variable object
        IVariable var_x = (IVariable) name_x.resolveBinding();
        assertEquals(globalScope, var_x.getScope());
        IFunction func_f = (IFunction) name_f.resolveBinding();
        assertEquals(globalScope, func_f.getScope());
        IParameter var_y = (IParameter) name_y.resolveBinding();
        assertEquals(((IASTCompoundStatement) funcdef_f.getBody()).getScope(),
                var_y.getScope());

        IVariable var_z = (IVariable) name_z.resolveBinding();
        assertEquals(((ICFunctionScope) func_f.getFunctionScope())
                .getBodyScope(), var_z.getScope());

        // make sure the variable referenced is the same one we declared above
        assertEquals(var_x, name_ref_x.resolveBinding());
        assertEquals(var_y, name_ref_y.resolveBinding());

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_x.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);

        decls = tu.getDeclarations(name_f.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_f);

        decls = tu.getDeclarations(name_y.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_y);

        decls = tu.getDeclarations(name_z.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_z);

        decls = tu.getDeclarations(name_ref_x.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);

        decls = tu.getDeclarations(name_ref_y.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_y);

        // // test clearBindings
        // assertNotNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()));
        // //$NON-NLS-1$
        // assertNotNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()));
        // //$NON-NLS-1$
        // assertNotNull(((ICScope) body_f.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("z").toCharArray()));
        // //$NON-NLS-1$
        // assertNotNull(((ICScope) body_f.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("y").toCharArray()));
        // //$NON-NLS-1$
        // CVisitor.clearBindings(tu);
        // assertNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()));
        // //$NON-NLS-1$
        // assertNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()));
        // //$NON-NLS-1$
        // assertNull(((ICScope) body_f.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("z").toCharArray()));
        // //$NON-NLS-1$
        // assertNull(((ICScope) body_f.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("y").toCharArray()));
        // //$NON-NLS-1$
    }

    public void testSimpleStruct() throws Exception {
        StringBuffer buff = new StringBuffer();
        buff.append("typedef struct {\n"); //$NON-NLS-1$
        buff.append("    int x;\n"); //$NON-NLS-1$
        buff.append("} S;\n"); //$NON-NLS-1$

        buff.append("void f() {\n"); //$NON-NLS-1$
        buff.append("    S myS;\n"); //$NON-NLS-1$
        buff.append("    myS.x = 5;"); //$NON-NLS-1$
        buff.append("}"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier type = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();

        // it's a typedef
        assertEquals(IASTDeclSpecifier.sc_typedef, type.getStorageClass());
        // this an anonymous struct
        IASTName name_struct = type.getName();
        assertTrue(name_struct.isDeclaration());
        assertFalse(name_struct.isReference());
        assertEquals("", name_struct.toString()); //$NON-NLS-1$
        // member - x
        IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) type
                .getMembers()[0];
        IASTSimpleDeclSpecifier spec_x = (IASTSimpleDeclSpecifier) decl_x
                .getDeclSpecifier();
        // it's an int
        assertEquals(IASTSimpleDeclSpecifier.t_int, spec_x.getType());
        IASTDeclarator tor_x = decl_x.getDeclarators()[0];
        IASTName name_x = tor_x.getName();
        assertEquals("x", name_x.toString()); //$NON-NLS-1$

        // declarator S
        IASTDeclarator tor_S = decl.getDeclarators()[0];
        IASTName name_S = tor_S.getName();
        assertEquals("S", name_S.toString()); //$NON-NLS-1$

        // function f
        IASTFunctionDefinition def_f = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        // f's body
        IASTCompoundStatement body_f = (IASTCompoundStatement) def_f.getBody();
        // the declaration statement for myS
        IASTDeclarationStatement declstmt_myS = (IASTDeclarationStatement) body_f
                .getStatements()[0];
        // the declaration for myS
        IASTSimpleDeclaration decl_myS = (IASTSimpleDeclaration) declstmt_myS
                .getDeclaration();
        // the type specifier for myS
        IASTNamedTypeSpecifier type_spec_myS = (IASTNamedTypeSpecifier) decl_myS
                .getDeclSpecifier();
        // the type name for myS
        IASTName name_type_myS = type_spec_myS.getName();
        // the declarator for myS
        IASTDeclarator tor_myS = decl_myS.getDeclarators()[0];
        // the name for myS
        IASTName name_myS = tor_myS.getName();
        // the assignment expression statement
        IASTExpressionStatement exprstmt = (IASTExpressionStatement) body_f
                .getStatements()[1];
        // the assignment expression
        IASTBinaryExpression assexpr = (IASTBinaryExpression) exprstmt
                .getExpression();
        // the field reference to myS.x
        IASTFieldReference fieldref = (IASTFieldReference) assexpr
                .getOperand1();
        // the reference to myS
        IASTIdExpression ref_myS = (IASTIdExpression) fieldref.getFieldOwner();
        IASTLiteralExpression lit_5 = (IASTLiteralExpression) assexpr
                .getOperand2();
        assertEquals("5", lit_5.toString()); //$NON-NLS-1$

        // Logical Bindings In Test
        ICompositeType type_struct = (ICompositeType) name_struct
                .resolveBinding();
        ITypedef typedef_S = (ITypedef) name_S.resolveBinding();
        // make sure the typedef is hooked up correctly
        assertEquals(type_struct, typedef_S.getType());
        // the typedef S for myS
        ITypedef typedef_myS = (ITypedef) name_type_myS.resolveBinding();
        assertEquals(typedef_S, typedef_myS);
        // get the real type for S which is our anonymous struct
        ICompositeType type_myS = (ICompositeType) typedef_myS.getType();
        assertEquals(type_myS, type_struct);
        // the variable myS
        IVariable var_myS = (IVariable) name_myS.resolveBinding();
        assertEquals(typedef_S, var_myS.getType());
        assertEquals(var_myS, ref_myS.getName().resolveBinding());
        IField field_x = (IField) name_x.resolveBinding();
        assertEquals(field_x, fieldref.getFieldName().resolveBinding());

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_struct.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_struct);

        decls = tu.getDeclarations(name_x.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);

        decls = tu.getDeclarations(def_f.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], def_f.getDeclarator().getName());

        decls = tu.getDeclarations(name_S.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_S);

        decls = tu.getDeclarations(name_myS.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_myS);

        decls = tu.getDeclarations(ref_myS.getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_myS);

        decls = tu.getDeclarations(fieldref.getFieldName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);
    }

    public void testCExpressions() throws ParserException {
        validateSimpleUnaryExpressionC("++x", IASTUnaryExpression.op_prefixIncr); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("--x", IASTUnaryExpression.op_prefixDecr); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("+x", IASTUnaryExpression.op_plus); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("-x", IASTUnaryExpression.op_minus); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("!x", IASTUnaryExpression.op_not); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("~x", IASTUnaryExpression.op_tilde); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("*x", IASTUnaryExpression.op_star); //$NON-NLS-1$
        validateSimpleUnaryExpressionC("&x", IASTUnaryExpression.op_amper); //$NON-NLS-1$
        validateSimpleUnaryExpressionC(
                "sizeof x", IASTUnaryExpression.op_sizeof); //$NON-NLS-1$
        validateSimpleTypeIdExpressionC(
                "sizeof( int )", IASTTypeIdExpression.op_sizeof); //$NON-NLS-1$
        validateSimpleUnaryTypeIdExpression(
                "(int)x", IASTCastExpression.op_cast); //$NON-NLS-1$
        validateSimplePostfixInitializerExpressionC("(int) { 5 }"); //$NON-NLS-1$
        validateSimplePostfixInitializerExpressionC("(int) { 5, }"); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x=y", IASTBinaryExpression.op_assign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x*=y", IASTBinaryExpression.op_multiplyAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x/=y", IASTBinaryExpression.op_divideAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x%=y", IASTBinaryExpression.op_moduloAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x+=y", IASTBinaryExpression.op_plusAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x-=y", IASTBinaryExpression.op_minusAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x<<=y", IASTBinaryExpression.op_shiftLeftAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x>>=y", IASTBinaryExpression.op_shiftRightAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x&=y", IASTBinaryExpression.op_binaryAndAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x^=y", IASTBinaryExpression.op_binaryXorAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x|=y", IASTBinaryExpression.op_binaryOrAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x-y", IASTBinaryExpression.op_minus); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x+y", IASTBinaryExpression.op_plus); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x/y", IASTBinaryExpression.op_divide); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x*y", IASTBinaryExpression.op_multiply); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x%y", IASTBinaryExpression.op_modulo); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x<<y", IASTBinaryExpression.op_shiftLeft); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x>>y", IASTBinaryExpression.op_shiftRight); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x<y", IASTBinaryExpression.op_lessThan); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x>y", IASTBinaryExpression.op_greaterThan); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x<=y", IASTBinaryExpression.op_lessEqual); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x>=y", IASTBinaryExpression.op_greaterEqual); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x==y", IASTBinaryExpression.op_equals); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x!=y", IASTBinaryExpression.op_notequals); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x&y", IASTBinaryExpression.op_binaryAnd); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x^y", IASTBinaryExpression.op_binaryXor); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x|y", IASTBinaryExpression.op_binaryOr); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x&&y", IASTBinaryExpression.op_logicalAnd); //$NON-NLS-1$
        validateSimpleBinaryExpressionC(
                "x||y", IASTBinaryExpression.op_logicalOr); //$NON-NLS-1$
        validateConditionalExpressionC("x ? y : x"); //$NON-NLS-1$
    }

    public void testMultipleDeclarators() throws Exception {
        IASTTranslationUnit tu = parse("int r, s;", ParserLanguage.C); //$NON-NLS-1$
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTDeclarator[] declarators = decl.getDeclarators();
        assertEquals(2, declarators.length);

        IASTDeclarator dtor1 = declarators[0];
        IASTDeclarator dtor2 = declarators[1];

        IASTName name1 = dtor1.getName();
        IASTName name2 = dtor2.getName();

        assertEquals(name1.resolveBinding().getName(), "r"); //$NON-NLS-1$
        assertEquals(name2.resolveBinding().getName(), "s"); //$NON-NLS-1$

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name1);

        decls = tu.getDeclarations(name2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name2);
    }

    public void testStructureTagScoping_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;             \n"); //$NON-NLS-1$
        buffer.append("void f(){             \n"); //$NON-NLS-1$
        buffer.append("   struct A;          \n"); //$NON-NLS-1$
        buffer.append("   struct A * a;      \n"); //$NON-NLS-1$
        buffer.append("}                     \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // struct A;
        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTElaboratedTypeSpecifier compTypeSpec = (IASTElaboratedTypeSpecifier) decl1
                .getDeclSpecifier();
        assertEquals(0, decl1.getDeclarators().length);
        IASTName nameA1 = compTypeSpec.getName();

        // void f() {
        IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef
                .getBody();
        assertEquals(2, compoundStatement.getStatements().length);

        // struct A;
        IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compoundStatement
                .getStatements()[0];
        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) declStatement
                .getDeclaration();
        compTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
        assertEquals(0, decl2.getDeclarators().length);
        IASTName nameA2 = compTypeSpec.getName();

        // struct A * a;
        declStatement = (IASTDeclarationStatement) compoundStatement
                .getStatements()[1];
        IASTSimpleDeclaration decl3 = (IASTSimpleDeclaration) declStatement
                .getDeclaration();
        compTypeSpec = (IASTElaboratedTypeSpecifier) decl3.getDeclSpecifier();
        IASTName nameA3 = compTypeSpec.getName();
        IASTDeclarator dtor = decl3.getDeclarators()[0];
        IASTName namea = dtor.getName();
        assertEquals(1, dtor.getPointerOperators().length);
        assertTrue(dtor.getPointerOperators()[0] instanceof ICASTPointer);

        // bindings
        ICompositeType str1 = (ICompositeType) nameA1.resolveBinding();
        ICompositeType str2 = (ICompositeType) nameA2.resolveBinding();
        IVariable var = (IVariable) namea.resolveBinding();
        IType str3pointer = var.getType();
        assertTrue(str3pointer instanceof IPointerType);
        ICompositeType str3 = (ICompositeType) ((IPointerType) str3pointer)
                .getType();
        ICompositeType str4 = (ICompositeType) nameA3.resolveBinding();
        assertNotNull(str1);
        assertNotNull(str2);
        assertNotSame(str1, str2);
        assertSame(str2, str3);
        assertSame(str3, str4);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(nameA1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], nameA1);

        decls = tu.getDeclarations(fndef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fndef.getDeclarator().getName());

        decls = tu.getDeclarations(nameA2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], nameA2);

        decls = tu.getDeclarations(nameA3.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], nameA2);

        decls = tu.getDeclarations(namea.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], namea);
    }

    public void testStructureTagScoping_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;             \n"); //$NON-NLS-1$
        buffer.append("void f(){             \n"); //$NON-NLS-1$
        buffer.append("   struct A * a;      \n"); //$NON-NLS-1$
        buffer.append("}                     \r\n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // struct A;
        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTElaboratedTypeSpecifier compTypeSpec = (IASTElaboratedTypeSpecifier) decl1
                .getDeclSpecifier();
        assertEquals(0, decl1.getDeclarators().length);
        IASTName nameA1 = compTypeSpec.getName();

        // void f() {
        IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef
                .getBody();
        assertEquals(1, compoundStatement.getStatements().length);

        // struct A * a;
        IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compoundStatement
                .getStatements()[0];
        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) declStatement
                .getDeclaration();
        compTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
        IASTName nameA2 = compTypeSpec.getName();
        IASTDeclarator dtor = decl2.getDeclarators()[0];
        IASTName namea = dtor.getName();
        assertEquals(1, dtor.getPointerOperators().length);
        assertTrue(dtor.getPointerOperators()[0] instanceof ICASTPointer);

        // bindings
        ICompositeType str1 = (ICompositeType) nameA1.resolveBinding();
        ICompositeType str2 = (ICompositeType) nameA2.resolveBinding();
        IVariable var = (IVariable) namea.resolveBinding();
        IPointerType str3pointer = (IPointerType) var.getType();
        ICompositeType str3 = (ICompositeType) str3pointer.getType();
        assertNotNull(str1);
        assertSame(str1, str2);
        assertSame(str2, str3);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(nameA1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], nameA1);

        decls = tu.getDeclarations(fndef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fndef.getDeclarator().getName());

        decls = tu.getDeclarations(nameA2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], nameA1);

        decls = tu.getDeclarations(namea.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], namea);
    }

    public void testStructureDef() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;                \r\n"); //$NON-NLS-1$
        buffer.append("struct A * a;            \n"); //$NON-NLS-1$
        buffer.append("struct A { int i; };     \n"); //$NON-NLS-1$
        buffer.append("void f() {               \n"); //$NON-NLS-1$
        buffer.append("   a->i;                 \n"); //$NON-NLS-1$
        buffer.append("}                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // struct A;
        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) decl1
                .getDeclSpecifier();
        assertEquals(0, decl1.getDeclarators().length);
        IASTName name_A1 = elabTypeSpec.getName();

        // struct A * a;
        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) tu
                .getDeclarations()[1];
        elabTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
        IASTName name_A2 = elabTypeSpec.getName();
        IASTDeclarator dtor = decl2.getDeclarators()[0];
        IASTName name_a = dtor.getName();
        assertEquals(1, dtor.getPointerOperators().length);
        assertTrue(dtor.getPointerOperators()[0] instanceof ICASTPointer);

        // struct A {
        IASTSimpleDeclaration decl3 = (IASTSimpleDeclaration) tu
                .getDeclarations()[2];
        ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) decl3
                .getDeclSpecifier();
        IASTName name_Adef = compTypeSpec.getName();

        // int i;
        IASTSimpleDeclaration decl4 = (IASTSimpleDeclaration) compTypeSpec
                .getMembers()[0];
        dtor = decl4.getDeclarators()[0];
        IASTName name_i = dtor.getName();

        // void f() {
        IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu
                .getDeclarations()[3];
        IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef
                .getBody();
        assertEquals(1, compoundStatement.getStatements().length);

        // a->i;
        IASTExpressionStatement exprstmt = (IASTExpressionStatement) compoundStatement
                .getStatements()[0];
        IASTFieldReference fieldref = (IASTFieldReference) exprstmt
                .getExpression();
        IASTIdExpression id_a = (IASTIdExpression) fieldref.getFieldOwner();
        IASTName name_aref = id_a.getName();
        IASTName name_iref = fieldref.getFieldName();

        // bindings
        IVariable var_a1 = (IVariable) name_aref.resolveBinding();
        IVariable var_i1 = (IVariable) name_iref.resolveBinding();
        IPointerType structA_1pointer = (IPointerType) var_a1.getType();
        ICompositeType structA_1 = (ICompositeType) structA_1pointer.getType();
        ICompositeType structA_2 = (ICompositeType) name_A1.resolveBinding();
        ICompositeType structA_3 = (ICompositeType) name_A2.resolveBinding();
        ICompositeType structA_4 = (ICompositeType) name_Adef.resolveBinding();

        IVariable var_a2 = (IVariable) name_a.resolveBinding();
        IVariable var_i2 = (IVariable) name_i.resolveBinding();

        assertSame(var_a1, var_a2);
        assertSame(var_i1, var_i2);
        assertSame(structA_1, structA_2);
        assertSame(structA_2, structA_3);
        assertSame(structA_3, structA_4);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_A1.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_A1);
        assertEquals(decls[1], name_Adef);

        decls = tu.getDeclarations(name_A2.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_A1);
        assertEquals(decls[1], name_Adef);

        decls = tu.getDeclarations(name_a.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a);

        decls = tu.getDeclarations(name_Adef.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_A1);
        assertEquals(decls[1], name_Adef);

        decls = tu.getDeclarations(name_i.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);

        decls = tu.getDeclarations(fndef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fndef.getDeclarator().getName());

        decls = tu.getDeclarations(name_aref.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a);

        decls = tu.getDeclarations(name_iref.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);
    }

    public void testStructureNamespace() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct x {};        \n"); //$NON-NLS-1$
        buffer.append("void f( int x ) {   \n"); //$NON-NLS-1$
        buffer.append("   struct x i;      \n"); //$NON-NLS-1$
        buffer.append("}                   \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration declaration1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier typeSpec = (IASTCompositeTypeSpecifier) declaration1
                .getDeclSpecifier();
        IASTName x_1 = typeSpec.getName();

        IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        assertTrue(fdef.getDeclarator() instanceof IASTStandardFunctionDeclarator);
        IASTParameterDeclaration param = ((IASTStandardFunctionDeclarator) fdef
                .getDeclarator()).getParameters()[0];
        IASTName x_2 = param.getDeclarator().getName();

        IASTCompoundStatement compound = (IASTCompoundStatement) fdef.getBody();
        IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compound
                .getStatements()[0];
        IASTSimpleDeclaration declaration2 = (IASTSimpleDeclaration) declStatement
                .getDeclaration();
        IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier) declaration2
                .getDeclSpecifier();
        IASTName x_3 = elab.getName();

        ICompositeType x1 = (ICompositeType) x_1.resolveBinding();
        IVariable x2 = (IVariable) x_2.resolveBinding();
        ICompositeType x3 = (ICompositeType) x_3.resolveBinding();

        assertNotNull(x1);
        assertNotNull(x2);
        assertSame(x1, x3);
        assertNotSame(x2, x3);

        IASTDeclarator decl_i = declaration2.getDeclarators()[0];
        decl_i.getName().resolveBinding(); // add i's binding to the scope

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(x_1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], x_1);

        decls = tu.getDeclarations(fdef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fdef.getDeclarator().getName());

        decls = tu.getDeclarations(x_2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], x_2);

        decls = tu.getDeclarations(x_3.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], x_1);

        decls = tu.getDeclarations(declaration2.getDeclarators()[0].getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], declaration2.getDeclarators()[0].getName());

        // assertNotNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_TAG, new String("x").toCharArray()));
        // //$NON-NLS-1$
        // assertNotNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()));
        // //$NON-NLS-1$
        // assertNotNull(((ICScope) compound.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()));
        // //$NON-NLS-1$
        // assertNotNull(((ICScope) compound.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("i").toCharArray()));
        // //$NON-NLS-1$
        // CVisitor.clearBindings(tu);
        // assertNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_TAG, new String("x").toCharArray()));
        // //$NON-NLS-1$
        // assertNull(((ICScope) tu.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()));
        // //$NON-NLS-1$
        // assertNull(((ICScope) compound.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()));
        // //$NON-NLS-1$
        // assertNull(((ICScope) compound.getScope()).getBinding(
        // ICScope.NAMESPACE_TYPE_OTHER, new String("i").toCharArray()));
        // //$NON-NLS-1$
    }

    public void testFunctionParameters() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( int a );        \n"); //$NON-NLS-1$
        buffer.append("void f( int b ){        \n"); //$NON-NLS-1$
        buffer.append("   b;                   \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // void f(
        IASTSimpleDeclaration f_decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTStandardFunctionDeclarator dtor = (IASTStandardFunctionDeclarator) f_decl
                .getDeclarators()[0];
        IASTName f_name1 = dtor.getName();
        // int a );
        IASTParameterDeclaration param1 = dtor.getParameters()[0];
        IASTDeclarator paramDtor = param1.getDeclarator();
        IASTName name_param1 = paramDtor.getName();

        // void f(
        IASTFunctionDefinition f_defn = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        assertTrue(f_defn.getDeclarator() instanceof IASTStandardFunctionDeclarator);
        dtor = (IASTStandardFunctionDeclarator) f_defn.getDeclarator();
        IASTName f_name2 = dtor.getName();
        // int b );
        IASTParameterDeclaration param2 = dtor.getParameters()[0];
        paramDtor = param2.getDeclarator();
        IASTName name_param2 = paramDtor.getName();

        // b;
        IASTCompoundStatement compound = (IASTCompoundStatement) f_defn
                .getBody();
        IASTExpressionStatement expStatement = (IASTExpressionStatement) compound
                .getStatements()[0];
        IASTIdExpression idexp = (IASTIdExpression) expStatement
                .getExpression();
        IASTName name_param3 = idexp.getName();

        // bindings
        IParameter param_1 = (IParameter) name_param3.resolveBinding();
        IParameter param_2 = (IParameter) name_param2.resolveBinding();
        IParameter param_3 = (IParameter) name_param1.resolveBinding();
        IFunction f_1 = (IFunction) f_name1.resolveBinding();
        IFunction f_2 = (IFunction) f_name2.resolveBinding();

        assertNotNull(param_1);
        assertNotNull(f_1);
        assertSame(param_1, param_2);
        assertSame(param_2, param_3);
        assertSame(f_1, f_2);

        CVisitor.clearBindings(tu);
        param_1 = (IParameter) name_param1.resolveBinding();
        param_2 = (IParameter) name_param3.resolveBinding();
        param_3 = (IParameter) name_param2.resolveBinding();
        f_1 = (IFunction) f_name2.resolveBinding();
        f_2 = (IFunction) f_name1.resolveBinding();
        assertNotNull(param_1);
        assertNotNull(f_1);
        assertSame(param_1, param_2);
        assertSame(param_2, param_3);
        assertSame(f_1, f_2);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(f_name1.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], f_name1);
        assertEquals(decls[1], f_name2);

        decls = tu.getDeclarations(name_param1.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_param1);
        assertEquals(decls[1], name_param2);

        decls = tu.getDeclarations(f_name2.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], f_name1);
        assertEquals(decls[1], f_name2);

        decls = tu.getDeclarations(name_param2.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_param1);
        assertEquals(decls[1], name_param2);
    }

    public void testSimpleFunction() throws Exception {
        StringBuffer buffer = new StringBuffer("void f( int a, int b ) { }  \n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTFunctionDefinition fDef = (IASTFunctionDefinition) tu
                .getDeclarations()[0];
        assertTrue(fDef.getDeclarator() instanceof IASTStandardFunctionDeclarator);
        IASTStandardFunctionDeclarator fDtor = (IASTStandardFunctionDeclarator) fDef
                .getDeclarator();
        IASTName fName = fDtor.getName();

        IASTParameterDeclaration a = fDtor.getParameters()[0];
        IASTName name_a = a.getDeclarator().getName();

        IASTParameterDeclaration b = fDtor.getParameters()[1];
        IASTName name_b = b.getDeclarator().getName();

        IFunction function = (IFunction) fName.resolveBinding();
        IParameter param_a = (IParameter) name_a.resolveBinding();
        IParameter param_b = (IParameter) name_b.resolveBinding();

        assertEquals("f", function.getName()); //$NON-NLS-1$
        assertEquals("a", param_a.getName()); //$NON-NLS-1$
        assertEquals("b", param_b.getName()); //$NON-NLS-1$

        IParameter[] params = function.getParameters();
        assertEquals(2, params.length);
        assertSame(params[0], param_a);
        assertSame(params[1], param_b);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(fName.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fName);

        decls = tu.getDeclarations(name_a.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a);

        decls = tu.getDeclarations(name_b.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_b);
    }

    public void testSimpleFunctionCall() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f();              \n"); //$NON-NLS-1$
        buffer.append("void g() {             \n"); //$NON-NLS-1$
        buffer.append("   f();                \n"); //$NON-NLS-1$
        buffer.append("}                      \n"); //$NON-NLS-1$
        buffer.append("void f(){ }            \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // void f();
        IASTSimpleDeclaration fdecl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) fdecl
                .getDeclarators()[0];
        IASTName name_f = fdtor.getName();

        // void g() {
        IASTFunctionDefinition gdef = (IASTFunctionDefinition) tu
                .getDeclarations()[1];

        // f();
        IASTCompoundStatement compound = (IASTCompoundStatement) gdef.getBody();
        IASTExpressionStatement expStatement = (IASTExpressionStatement) compound
                .getStatements()[0];
        IASTFunctionCallExpression fcall = (IASTFunctionCallExpression) expStatement
                .getExpression();
        IASTIdExpression fcall_id = (IASTIdExpression) fcall
                .getFunctionNameExpression();
        IASTName name_fcall = fcall_id.getName();
        assertNull(fcall.getParameterExpression());

        // void f() {}
        IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu
                .getDeclarations()[2];
        assertTrue(fdef.getDeclarator() instanceof IASTStandardFunctionDeclarator);
        fdtor = (IASTStandardFunctionDeclarator) fdef.getDeclarator();
        IASTName name_fdef = fdtor.getName();

        // bindings
        IFunction function_1 = (IFunction) name_fcall.resolveBinding();
        IFunction function_2 = (IFunction) name_f.resolveBinding();
        IFunction function_3 = (IFunction) name_fdef.resolveBinding();

        assertNotNull(function_1);
        assertSame(function_1, function_2);
        assertSame(function_2, function_3);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_f.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_f);
        assertEquals(decls[1], name_fdef);

        decls = tu.getDeclarations(gdef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], gdef.getDeclarator().getName());

        decls = tu.getDeclarations(name_fcall.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_f);
        assertEquals(decls[1], name_fdef);

        decls = tu.getDeclarations(name_fdef.resolveBinding());
        assertEquals(decls.length, 2);
        assertEquals(decls[0], name_f);
        assertEquals(decls[1], name_fdef);
    }

    public void testForLoop() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                         \n"); //$NON-NLS-1$
        buffer.append("   for( int i = 0; i < 5; i++ ) {  \n"); //$NON-NLS-1$         
        buffer.append("      i;                           \n"); //$NON-NLS-1$
        buffer.append("   }                               \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // void f() {
        IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu
                .getDeclarations()[0];
        IASTCompoundStatement compound = (IASTCompoundStatement) fdef.getBody();

        // for(
        IASTForStatement for_stmt = (IASTForStatement) compound.getStatements()[0];
        // int i = 0;
        assertNull(for_stmt.getInitExpression());
        IASTSimpleDeclaration initDecl = (IASTSimpleDeclaration) for_stmt
                .getInitDeclaration();
        IASTDeclarator dtor = initDecl.getDeclarators()[0];
        IASTName name_i = dtor.getName();
        // i < 5;
        IASTBinaryExpression exp = (IASTBinaryExpression) for_stmt
                .getCondition();
        IASTIdExpression id_i = (IASTIdExpression) exp.getOperand1();
        IASTName name_i2 = id_i.getName();
        IASTLiteralExpression lit_5 = (IASTLiteralExpression) exp.getOperand2();
        assertEquals(IASTLiteralExpression.lk_integer_constant, lit_5.getKind());
        // i++ ) {
        IASTUnaryExpression un = (IASTUnaryExpression) for_stmt
                .getIterationExpression();
        IASTIdExpression id_i2 = (IASTIdExpression) un.getOperand();
        IASTName name_i3 = id_i2.getName();
        assertEquals(IASTUnaryExpression.op_postFixIncr, un.getOperator());

        // i;
        compound = (IASTCompoundStatement) for_stmt.getBody();
        IASTExpressionStatement exprSt = (IASTExpressionStatement) compound
                .getStatements()[0];
        IASTIdExpression id_i3 = (IASTIdExpression) exprSt.getExpression();
        IASTName name_i4 = id_i3.getName();

        // bindings
        IVariable var_1 = (IVariable) name_i4.resolveBinding();
        IVariable var_2 = (IVariable) name_i.resolveBinding();
        IVariable var_3 = (IVariable) name_i2.resolveBinding();
        IVariable var_4 = (IVariable) name_i3.resolveBinding();

        assertSame(var_1, var_2);
        assertSame(var_2, var_3);
        assertSame(var_3, var_4);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(fdef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fdef.getDeclarator().getName());

        decls = tu.getDeclarations(name_i.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);

        decls = tu.getDeclarations(name_i2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);

        decls = tu.getDeclarations(name_i3.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);

        decls = tu.getDeclarations(name_i4.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);
    }

    public void testExpressionFieldReference() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A { int x; };    \n"); //$NON-NLS-1$
        buffer.append("void f(){               \n"); //$NON-NLS-1$
        buffer.append("   ((struct A *) 1)->x; \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier compType = (IASTCompositeTypeSpecifier) simpleDecl
                .getDeclSpecifier();
        IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) compType
                .getMembers()[0];
        IASTName name_x1 = decl_x.getDeclarators()[0].getName();
        IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IASTCompoundStatement body = (IASTCompoundStatement) fdef.getBody();
        IASTExpressionStatement expStatement = (IASTExpressionStatement) body
                .getStatements()[0];
        IASTFieldReference fieldRef = (IASTFieldReference) expStatement
                .getExpression();
        IASTName name_x2 = fieldRef.getFieldName();

        IField x1 = (IField) name_x1.resolveBinding();
        IField x2 = (IField) name_x2.resolveBinding();

        assertNotNull(x1);
        assertSame(x1, x2);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(compType.getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], compType.getName());

        decls = tu.getDeclarations(name_x1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x1);

        decls = tu.getDeclarations(fdef.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fdef.getDeclarator().getName());

        IASTCastExpression castExpression = (IASTCastExpression) ((IASTUnaryExpression) ((IASTFieldReference) expStatement
                .getExpression()).getFieldOwner()).getOperand();
        IASTElaboratedTypeSpecifier elaboratedTypeSpecifier = ((IASTElaboratedTypeSpecifier) castExpression
                .getTypeId().getDeclSpecifier());
        decls = tu.getDeclarations(elaboratedTypeSpecifier.getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], compType.getName());

        decls = tu.getDeclarations(name_x2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x1);
    }

    public void testLabels() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {          \n"); //$NON-NLS-1$
        buffer.append("   while( 1 ) {     \n"); //$NON-NLS-1$
        buffer.append("      if( 1 )       \n"); //$NON-NLS-1$
        buffer.append("         goto end;  \n"); //$NON-NLS-1$
        buffer.append("   }                \n"); //$NON-NLS-1$
        buffer.append("   end: ;           \n"); //$NON-NLS-1$
        buffer.append("}                   \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        CNameCollector collector = new CNameCollector();
        tu.accept(collector);

        assertEquals(collector.size(), 3);
        IFunction function = (IFunction) collector.getName(0).resolveBinding();
        ILabel label_1 = (ILabel) collector.getName(1).resolveBinding();
        ILabel label_2 = (ILabel) collector.getName(2).resolveBinding();
        assertNotNull(function);
        assertNotNull(label_1);
        assertEquals(label_1, label_2);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(collector.getName(0)
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], collector.getName(0));

        decls = tu.getDeclarations(collector.getName(1).resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], collector.getName(2));

        decls = tu.getDeclarations(collector.getName(2).resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], collector.getName(2));
    }

    public void testAnonStruct() throws Exception {
        StringBuffer buffer = new StringBuffer("typedef struct { } X;\n"); //$NON-NLS-1$
        buffer.append("int f( X x );"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        // test tu.getDeclarations(IBinding)
        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) tu
                .getDeclarations()[1];
        IASTName name_X1 = decl1.getDeclarators()[0].getName();
        IASTName name_f = decl2.getDeclarators()[0].getName();
        IASTName name_X2 = ((IASTNamedTypeSpecifier) ((IASTStandardFunctionDeclarator) decl2
                .getDeclarators()[0]).getParameters()[0].getDeclSpecifier())
                .getName();
        IASTName name_x = ((IASTStandardFunctionDeclarator) decl2
                .getDeclarators()[0]).getParameters()[0].getDeclarator()
                .getName();

        IASTName[] decls = tu.getDeclarations(name_X1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_X1);

        decls = tu.getDeclarations(name_f.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_f);

        decls = tu.getDeclarations(name_X2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_X1);

        decls = tu.getDeclarations(name_x.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);
    }

    public void testLongLong() throws ParserException {
        IASTTranslationUnit tu = parse("long long x;\n", ParserLanguage.C); //$NON-NLS-1$

        // test tu.getDeclarations(IBinding)
        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTName name_x = decl1.getDeclarators()[0].getName();

        IASTName[] decls = tu.getDeclarations(name_x.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);
    }

    public void testEnumerations() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("enum hue { red, blue, green };     \n"); //$NON-NLS-1$
        buffer.append("enum hue col, *cp;                 \n"); //$NON-NLS-1$
        buffer.append("void f() {                         \n"); //$NON-NLS-1$
        buffer.append("   col = blue;                     \n"); //$NON-NLS-1$
        buffer.append("   cp = &col;                      \n"); //$NON-NLS-1$
        buffer.append("   if( *cp != red )                \n"); //$NON-NLS-1$
        buffer.append("      return;                      \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl1.getDeclarators().length, 0);
        ICASTEnumerationSpecifier enumSpec = (ICASTEnumerationSpecifier) decl1
                .getDeclSpecifier();
        IASTEnumerator e1 = enumSpec.getEnumerators()[0];
        IASTEnumerator e2 = enumSpec.getEnumerators()[1];
        IASTEnumerator e3 = enumSpec.getEnumerators()[2];
        IASTName name_hue = enumSpec.getName();

        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) tu
                .getDeclarations()[1];
        IASTDeclarator dtor = decl2.getDeclarators()[0];
        IASTName name_col = dtor.getName();
        dtor = decl2.getDeclarators()[1];
        IASTName name_cp = dtor.getName();
        IASTElaboratedTypeSpecifier spec = (IASTElaboratedTypeSpecifier) decl2
                .getDeclSpecifier();
        assertEquals(spec.getKind(), IASTElaboratedTypeSpecifier.k_enum);
        IASTName name_hue2 = spec.getName();

        IASTFunctionDefinition fn = (IASTFunctionDefinition) tu
                .getDeclarations()[2];
        IASTCompoundStatement compound = (IASTCompoundStatement) fn.getBody();
        IASTExpressionStatement expStatement1 = (IASTExpressionStatement) compound
                .getStatements()[0];
        IASTBinaryExpression exp = (IASTBinaryExpression) expStatement1
                .getExpression();
        assertEquals(exp.getOperator(), IASTBinaryExpression.op_assign);
        IASTIdExpression id1 = (IASTIdExpression) exp.getOperand1();
        IASTIdExpression id2 = (IASTIdExpression) exp.getOperand2();
        IASTName r_col = id1.getName();
        IASTName r_blue = id2.getName();

        IASTExpressionStatement expStatement2 = (IASTExpressionStatement) compound
                .getStatements()[1];
        exp = (IASTBinaryExpression) expStatement2.getExpression();
        assertEquals(exp.getOperator(), IASTBinaryExpression.op_assign);
        id1 = (IASTIdExpression) exp.getOperand1();
        IASTUnaryExpression ue = (IASTUnaryExpression) exp.getOperand2();
        id2 = (IASTIdExpression) ue.getOperand();
        IASTName r_cp = id1.getName();
        IASTName r_col2 = id2.getName();

        IASTIfStatement ifStatement = (IASTIfStatement) compound
                .getStatements()[2];
        exp = (IASTBinaryExpression) ifStatement.getConditionExpression();
        ue = (IASTUnaryExpression) exp.getOperand1();
        id1 = (IASTIdExpression) ue.getOperand();
        id2 = (IASTIdExpression) exp.getOperand2();

        IASTName r_cp2 = id1.getName();
        IASTName r_red = id2.getName();

        IEnumeration hue = (IEnumeration) name_hue.resolveBinding();
        IEnumerator red = (IEnumerator) e1.getName().resolveBinding();
        IEnumerator blue = (IEnumerator) e2.getName().resolveBinding();
        IEnumerator green = (IEnumerator) e3.getName().resolveBinding();
        IVariable col = (IVariable) name_col.resolveBinding();
        IVariable cp = (IVariable) name_cp.resolveBinding();
        IEnumeration hue_2 = (IEnumeration) name_hue2.resolveBinding();
        IVariable col2 = (IVariable) r_col.resolveBinding();
        IEnumerator blue2 = (IEnumerator) r_blue.resolveBinding();
        IVariable cp2 = (IVariable) r_cp.resolveBinding();
        IVariable col3 = (IVariable) r_col2.resolveBinding();
        IVariable cp3 = (IVariable) r_cp2.resolveBinding();
        IEnumerator red2 = (IEnumerator) r_red.resolveBinding();

        assertNotNull(hue);
        assertSame(hue, hue_2);
        assertNotNull(red);
        assertNotNull(green);
        assertNotNull(blue);
        assertNotNull(col);
        assertNotNull(cp);
        assertSame(col, col2);
        assertSame(blue, blue2);
        assertSame(cp, cp2);
        assertSame(col, col3);
        assertSame(cp, cp3);
        assertSame(red, red2);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_hue.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_hue);

        decls = tu.getDeclarations(e1.getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], e1.getName());

        decls = tu.getDeclarations(e2.getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], e2.getName());

        decls = tu.getDeclarations(e3.getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], e3.getName());

        decls = tu.getDeclarations(name_hue2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_hue);

        decls = tu.getDeclarations(name_col.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_col);

        decls = tu.getDeclarations(name_cp.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_cp);

        decls = tu.getDeclarations(fn.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], fn.getDeclarator().getName());

        decls = tu.getDeclarations(r_col.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_col);

        decls = tu.getDeclarations(r_blue.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], e2.getName());

        decls = tu.getDeclarations(r_cp.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_cp);

        decls = tu.getDeclarations(r_col2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_col);

        decls = tu.getDeclarations(r_cp2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_cp);

        decls = tu.getDeclarations(r_red.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], e1.getName());
    }

    public void testPointerToFunction() throws Exception {
        IASTTranslationUnit tu = parse("int (*pfi)();", ParserLanguage.C); //$NON-NLS-1$
        assertEquals(tu.getDeclarations().length, 1);
        IASTSimpleDeclaration d = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        assertEquals(d.getDeclarators().length, 1);
        IASTStandardFunctionDeclarator f = (IASTStandardFunctionDeclarator) d
                .getDeclarators()[0];
        assertEquals(f.getName().toString(), "");
        assertNotNull(f.getNestedDeclarator());
        assertEquals(f.getNestedDeclarator().getName().toString(), "pfi"); //$NON-NLS-1$
        assertTrue(f.getPointerOperators().length == 0);
        assertFalse(f.getNestedDeclarator().getPointerOperators().length == 0);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(f.getNestedDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], f.getNestedDeclarator().getName());
    }

    public void testBasicTypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int a;       \n"); //$NON-NLS-1$
        buffer.append("char * b;    \n"); //$NON-NLS-1$
        buffer.append("const int c; \n"); //$NON-NLS-1$
        buffer.append("const char * const d; \n"); //$NON-NLS-1$
        buffer.append("const char ** e; \n"); //$NON-NLS-1$
        buffer.append("const char * const * const volatile ** const * f; \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IVariable a = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IVariable b = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IVariable c = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable d = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[4];
        IVariable e = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[5];
        IVariable f = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();

        IType t_a_1 = a.getType();
        assertTrue(t_a_1 instanceof IBasicType);
        assertFalse(((IBasicType) t_a_1).isLong());
        assertFalse(((IBasicType) t_a_1).isShort());
        assertFalse(((IBasicType) t_a_1).isSigned());
        assertFalse(((IBasicType) t_a_1).isUnsigned());
        assertEquals(((IBasicType) t_a_1).getType(), IBasicType.t_int);

        IType t_b_1 = b.getType();
        assertTrue(t_b_1 instanceof IPointerType);
        IType t_b_2 = ((IPointerType) t_b_1).getType();
        assertTrue(t_b_2 instanceof IBasicType);
        assertEquals(((IBasicType) t_b_2).getType(), IBasicType.t_char);

        IType t_c_1 = c.getType();
        assertTrue(t_c_1 instanceof IQualifierType);
        assertTrue(((IQualifierType) t_c_1).isConst());
        IType t_c_2 = ((IQualifierType) t_c_1).getType();
        assertTrue(t_c_2 instanceof IBasicType);
        assertEquals(((IBasicType) t_c_2).getType(), IBasicType.t_int);

        IType t_d_1 = d.getType();
        assertTrue(t_d_1 instanceof IPointerType);
        assertTrue(((IPointerType) t_d_1).isConst());
        IType t_d_2 = ((IPointerType) t_d_1).getType();
        assertTrue(t_d_2 instanceof IQualifierType);
        assertTrue(((IQualifierType) t_d_2).isConst());
        IType t_d_3 = ((IQualifierType) t_d_2).getType();
        assertTrue(t_d_3 instanceof IBasicType);
        assertEquals(((IBasicType) t_d_3).getType(), IBasicType.t_char);

        IType t_e_1 = e.getType();
        assertTrue(t_e_1 instanceof IPointerType);
        assertFalse(((IPointerType) t_e_1).isConst());
        IType t_e_2 = ((IPointerType) t_e_1).getType();
        assertTrue(t_e_2 instanceof IPointerType);
        assertFalse(((IPointerType) t_e_2).isConst());
        IType t_e_3 = ((IPointerType) t_e_2).getType();
        assertTrue(t_e_3 instanceof IQualifierType);
        assertTrue(((IQualifierType) t_e_3).isConst());
        IType t_e_4 = ((IQualifierType) t_e_3).getType();
        assertTrue(t_e_4 instanceof IBasicType);
        assertEquals(((IBasicType) t_e_4).getType(), IBasicType.t_char);

        IType t_f_1 = f.getType();
        assertTrue(t_f_1 instanceof IPointerType);
        assertFalse(((IPointerType) t_f_1).isConst());
        assertFalse(((IPointerType) t_f_1).isVolatile());
        IType t_f_2 = ((IPointerType) t_f_1).getType();
        assertTrue(t_f_2 instanceof IPointerType);
        assertTrue(((IPointerType) t_f_2).isConst());
        assertFalse(((IPointerType) t_f_2).isVolatile());
        IType t_f_3 = ((IPointerType) t_f_2).getType();
        assertTrue(t_f_3 instanceof IPointerType);
        assertFalse(((IPointerType) t_f_3).isConst());
        assertFalse(((IPointerType) t_f_3).isVolatile());
        IType t_f_4 = ((IPointerType) t_f_3).getType();
        assertTrue(t_f_4 instanceof IPointerType);
        assertTrue(((IPointerType) t_f_4).isConst());
        assertTrue(((IPointerType) t_f_4).isVolatile());
        IType t_f_5 = ((IPointerType) t_f_4).getType();
        assertTrue(t_f_5 instanceof IPointerType);
        assertTrue(((IPointerType) t_f_5).isConst());
        assertFalse(((IPointerType) t_f_5).isVolatile());
        IType t_f_6 = ((IPointerType) t_f_5).getType();
        assertTrue(t_f_6 instanceof IQualifierType);
        assertTrue(((IQualifierType) t_f_6).isConst());
        IType t_f_7 = ((IQualifierType) t_f_6).getType();
        assertTrue(t_f_7 instanceof IBasicType);
        assertEquals(((IBasicType) t_f_7).getType(), IBasicType.t_char);
    }

    public void testCompositeTypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A {} a1;              \n"); //$NON-NLS-1$
        buffer.append("typedef struct A * AP;       \n"); //$NON-NLS-1$
        buffer.append("struct A * const a2;         \n"); //$NON-NLS-1$
        buffer.append("AP a3;                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        ICompositeType A = (ICompositeType) compSpec.getName().resolveBinding();
        IASTName name_a1 = decl.getDeclarators()[0].getName();
        IVariable a1 = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IASTName name_A2 = ((IASTElaboratedTypeSpecifier) decl
                .getDeclSpecifier()).getName();
        IASTName name_AP = decl.getDeclarators()[0].getName();
        ITypedef AP = (ITypedef) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IASTName name_A3 = ((IASTElaboratedTypeSpecifier) decl
                .getDeclSpecifier()).getName();
        IVariable a2 = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        IASTName name_a2 = decl.getDeclarators()[0].getName();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable a3 = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        IASTName name_a3 = decl.getDeclarators()[0].getName();
        IASTName name_AP2 = ((IASTNamedTypeSpecifier) decl.getDeclSpecifier())
                .getName();

        IType t_a1 = a1.getType();
        assertSame(t_a1, A);

        IType t_a2 = a2.getType();
        assertTrue(t_a2 instanceof IPointerType);
        assertTrue(((IPointerType) t_a2).isConst());
        assertSame(((IPointerType) t_a2).getType(), A);

        IType t_a3 = a3.getType();
        assertSame(t_a3, AP);
        IType t_AP = AP.getType();
        assertTrue(t_AP instanceof IPointerType);
        assertSame(((IPointerType) t_AP).getType(), A);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(compSpec.getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], compSpec.getName());

        decls = tu.getDeclarations(name_a1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a1);

        decls = tu.getDeclarations(name_A2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], compSpec.getName());

        decls = tu.getDeclarations(name_AP.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_AP);

        decls = tu.getDeclarations(name_A3.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], compSpec.getName());

        decls = tu.getDeclarations(name_a2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a2);

        decls = tu.getDeclarations(name_AP2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_AP);

        decls = tu.getDeclarations(name_a3.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a3);
    }

    public void testArrayTypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int a[restrict];       \n"); //$NON-NLS-1$
        buffer.append("char * b[][];    \n"); //$NON-NLS-1$
        buffer.append("const char * const c[][][]; \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTName name_a = decl.getDeclarators()[0].getName();
        IVariable a = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IASTName name_b = decl.getDeclarators()[0].getName();
        IVariable b = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IASTName name_c = decl.getDeclarators()[0].getName();
        IVariable c = (IVariable) decl.getDeclarators()[0].getName()
                .resolveBinding();

        IType t_a_1 = a.getType();
        assertTrue(t_a_1 instanceof ICArrayType);
        assertTrue(((ICArrayType) t_a_1).isRestrict());
        IType t_a_2 = ((IArrayType) t_a_1).getType();
        assertTrue(t_a_2 instanceof IBasicType);
        assertEquals(((IBasicType) t_a_2).getType(), IBasicType.t_int);

        IType t_b_1 = b.getType();
        assertTrue(t_b_1 instanceof IArrayType);
        IType t_b_2 = ((IArrayType) t_b_1).getType();
        assertTrue(t_b_2 instanceof IArrayType);
        IType t_b_3 = ((IArrayType) t_b_2).getType();
        assertTrue(t_b_3 instanceof IPointerType);
        IType t_b_4 = ((IPointerType) t_b_3).getType();
        assertTrue(t_b_4 instanceof IBasicType);
        assertEquals(((IBasicType) t_b_4).getType(), IBasicType.t_char);

        IType t_c_1 = c.getType();
        assertTrue(t_c_1 instanceof IArrayType);
        IType t_c_2 = ((IArrayType) t_c_1).getType();
        assertTrue(t_c_2 instanceof IArrayType);
        IType t_c_3 = ((IArrayType) t_c_2).getType();
        assertTrue(t_c_3 instanceof IArrayType);
        IType t_c_4 = ((IArrayType) t_c_3).getType();
        assertTrue(t_c_4 instanceof IPointerType);
        assertTrue(((IPointerType) t_c_4).isConst());
        IType t_c_5 = ((IPointerType) t_c_4).getType();
        assertTrue(t_c_5 instanceof IQualifierType);
        assertTrue(((IQualifierType) t_c_5).isConst());
        IType t_c_6 = ((IQualifierType) t_c_5).getType();
        assertTrue(t_c_6 instanceof IBasicType);
        assertEquals(((IBasicType) t_c_6).getType(), IBasicType.t_char);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_a.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_a);

        decls = tu.getDeclarations(name_b.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_b);

        decls = tu.getDeclarations(name_c.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_c);
    }

    public void testFunctionTypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;                           \n"); //$NON-NLS-1$
        buffer.append("int * f( int i, char c );           \n"); //$NON-NLS-1$
        buffer.append("void ( *g ) ( struct A * );         \n"); //$NON-NLS-1$
        buffer.append("void (* (*h)(struct A**) ) ( int d ); \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl
                .getDeclSpecifier();
        ICompositeType A = (ICompositeType) elabSpec.getName().resolveBinding();
        IASTName name_A1 = elabSpec.getName();
        assertTrue(name_A1.isDeclaration());

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IFunction f = (IFunction) decl.getDeclarators()[0].getName()
                .resolveBinding();
        IASTName name_f = decl.getDeclarators()[0].getName();
        IASTName name_i = ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0]).getParameters()[0].getDeclarator()
                .getName();
        IASTName name_c = ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0]).getParameters()[1].getDeclarator()
                .getName();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IVariable g = (IVariable) decl.getDeclarators()[0]
                .getNestedDeclarator().getName().resolveBinding();
        IASTName name_g = decl.getDeclarators()[0].getNestedDeclarator()
                .getName();
        IASTName name_A2 = ((IASTElaboratedTypeSpecifier) ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0]).getParameters()[0].getDeclSpecifier())
                .getName();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable h = (IVariable) decl.getDeclarators()[0]
                .getNestedDeclarator().getNestedDeclarator().getName()
                .resolveBinding();
        IASTName name_h = decl.getDeclarators()[0].getNestedDeclarator()
                .getNestedDeclarator().getName();
        IASTName name_A3 = ((IASTElaboratedTypeSpecifier) ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0].getNestedDeclarator()).getParameters()[0]
                .getDeclSpecifier()).getName();
        IASTName name_d = ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0]).getParameters()[0].getDeclarator()
                .getName();

        IFunctionType t_f = f.getType();
        IType t_f_return = t_f.getReturnType();
        assertTrue(t_f_return instanceof IPointerType);
        assertTrue(((IPointerType) t_f_return).getType() instanceof IBasicType);
        IType[] t_f_params = t_f.getParameterTypes();
        assertEquals(t_f_params.length, 2);
        assertTrue(t_f_params[0] instanceof IBasicType);
        assertTrue(t_f_params[1] instanceof IBasicType);

        // g is a pointer to a function that returns void and has 1 parameter
        // struct A *
        IType t_g = g.getType();
        assertTrue(t_g instanceof IPointerType);
        assertTrue(((IPointerType) t_g).getType() instanceof IFunctionType);
        IFunctionType t_g_func = (IFunctionType) ((IPointerType) t_g).getType();
        IType t_g_func_return = t_g_func.getReturnType();
        assertTrue(t_g_func_return instanceof IBasicType);
        IType[] t_g_func_params = t_g_func.getParameterTypes();
        assertEquals(t_g_func_params.length, 1);
        IType t_g_func_p1 = t_g_func_params[0];
        assertTrue(t_g_func_p1 instanceof IPointerType);
        assertSame(((IPointerType) t_g_func_p1).getType(), A);

        // h is a pointer to a function that returns a pointer to a function
        // the returned pointer to function returns void and takes 1 parameter
        // int
        // the *h function takes 1 parameter struct A**
        IType t_h = h.getType();
        assertTrue(t_h instanceof IPointerType);
        assertTrue(((IPointerType) t_h).getType() instanceof IFunctionType);
        IFunctionType t_h_func = (IFunctionType) ((IPointerType) t_h).getType();
        IType t_h_func_return = t_h_func.getReturnType();
        IType[] t_h_func_params = t_h_func.getParameterTypes();
        assertEquals(t_h_func_params.length, 1);
        IType t_h_func_p1 = t_h_func_params[0];
        assertTrue(t_h_func_p1 instanceof IPointerType);
        assertTrue(((IPointerType) t_h_func_p1).getType() instanceof IPointerType);
        assertSame(((IPointerType) ((IPointerType) t_h_func_p1).getType())
                .getType(), A);

        assertTrue(t_h_func_return instanceof IPointerType);
        IFunctionType h_return = (IFunctionType) ((IPointerType) t_h_func_return)
                .getType();
        IType h_r = h_return.getReturnType();
        IType[] h_ps = h_return.getParameterTypes();
        assertTrue(h_r instanceof IBasicType);
        assertEquals(h_ps.length, 1);
        assertTrue(h_ps[0] instanceof IBasicType);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_A1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_A1);

        decls = tu.getDeclarations(name_f.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_f);

        decls = tu.getDeclarations(name_i.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_i);

        decls = tu.getDeclarations(name_c.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_c);

        decls = tu.getDeclarations(name_g.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_g);

        decls = tu.getDeclarations(name_A2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_A1);

        decls = tu.getDeclarations(name_h.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_h);

        decls = tu.getDeclarations(name_A3.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_A1);

        assertNull(name_d.resolveBinding());
    }

    public void testDesignatedInitializers() throws ParserException {
        StringBuffer buffer = new StringBuffer("typedef struct {\n"); //$NON-NLS-1$
        buffer.append(" int x;\n"); //$NON-NLS-1$
        buffer.append(" int y;\n"); //$NON-NLS-1$
        buffer.append("} Coord;\n"); //$NON-NLS-1$
        buffer.append("typedef struct {\n"); //$NON-NLS-1$
        buffer.append("Coord *pos;\n"); //$NON-NLS-1$
        buffer.append("int width;\n"); //$NON-NLS-1$
        buffer.append("} Point;\n"); //$NON-NLS-1$
        buffer.append("int main(int argc, char *argv[])\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("Coord xy = {.y = 10, .x = 11};\n"); //$NON-NLS-1$
        buffer.append("Point point = {.width = 100, .pos = &xy};\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        assertNotNull(tu);
        IASTDeclaration[] declarations = tu.getDeclarations();
        IASTName name_Coord = ((IASTSimpleDeclaration) declarations[0])
                .getDeclarators()[0].getName();
        IASTName name_x = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) declarations[0])
                .getDeclSpecifier()).getMembers()[0]).getDeclarators()[0]
                .getName();
        IASTName name_y = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) declarations[0])
                .getDeclSpecifier()).getMembers()[1]).getDeclarators()[0]
                .getName();
        IASTName name_Point = ((IASTSimpleDeclaration) declarations[1])
                .getDeclarators()[0].getName();
        IASTName name_pos = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) declarations[1])
                .getDeclSpecifier()).getMembers()[0]).getDeclarators()[0]
                .getName();
        IASTName name_width = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) declarations[1])
                .getDeclSpecifier()).getMembers()[1]).getDeclarators()[0]
                .getName();
        IASTFunctionDefinition main = (IASTFunctionDefinition) declarations[2];
        IASTStatement[] statements = ((IASTCompoundStatement) main.getBody())
                .getStatements();

        IASTSimpleDeclaration xy = (IASTSimpleDeclaration) ((IASTDeclarationStatement) statements[0])
                .getDeclaration();
        IASTName name_Coord2 = ((IASTNamedTypeSpecifier) xy.getDeclSpecifier())
                .getName();
        IASTName name_xy = xy.getDeclarators()[0].getName();
        IASTDeclarator declarator_xy = xy.getDeclarators()[0];
        IASTInitializer[] initializers1 = ((IASTInitializerList) declarator_xy
                .getInitializer()).getInitializers();
        IASTName name_y2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) initializers1[0])
                .getDesignators()[0]).getName();

        // test bug 87649
        assertEquals(((ASTNode) (ICASTDesignatedInitializer) initializers1[0])
                .getLength(), 7);

        IASTName name_x2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) initializers1[1])
                .getDesignators()[0]).getName();

        IASTSimpleDeclaration point = (IASTSimpleDeclaration) ((IASTDeclarationStatement) statements[1])
                .getDeclaration();
        IASTName name_Point2 = ((IASTNamedTypeSpecifier) point
                .getDeclSpecifier()).getName();
        IASTName name_point = point.getDeclarators()[0].getName();
        IASTDeclarator declarator_point = point.getDeclarators()[0];
        IASTInitializer[] initializers2 = ((IASTInitializerList) declarator_point
                .getInitializer()).getInitializers();
        IASTName name_width2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) initializers2[0])
                .getDesignators()[0]).getName();
        IASTName name_pos2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) initializers2[1])
                .getDesignators()[0]).getName();
        IASTName name_xy2 = ((IASTIdExpression) ((IASTUnaryExpression) ((IASTInitializerExpression) ((ICASTDesignatedInitializer) initializers2[1])
                .getOperandInitializer()).getExpression()).getOperand())
                .getName();

        for (int i = 0; i < 2; ++i) {
            ICASTDesignatedInitializer designatedInitializer = (ICASTDesignatedInitializer) initializers1[i];
            assertEquals(designatedInitializer.getDesignators().length, 1);
            ICASTFieldDesignator fieldDesignator = (ICASTFieldDesignator) designatedInitializer
                    .getDesignators()[0];
            assertNotNull(fieldDesignator.getName().toString());
        }

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(name_Coord2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_Coord);

        decls = tu.getDeclarations(name_xy.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_xy);

        decls = tu.getDeclarations(name_y2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_y);

        decls = tu.getDeclarations(name_x2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_x);

        decls = tu.getDeclarations(name_Point2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_Point);

        decls = tu.getDeclarations(name_point.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_point);

        decls = tu.getDeclarations(name_width2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_width);

        decls = tu.getDeclarations(name_pos2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_pos);

        decls = tu.getDeclarations(name_xy2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_xy);
    }

    public void testMoreGetDeclarations1() throws Exception {
        StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
        buffer.append("struct S {\n"); //$NON-NLS-1$
        buffer.append(" int a;\n"); //$NON-NLS-1$
        buffer.append(" int b;\n"); //$NON-NLS-1$
        buffer.append("} s;\n"); //$NON-NLS-1$
        buffer.append("int f() {\n"); //$NON-NLS-1$
        buffer.append("struct S s = {.a=1,.b=2};\n}\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration S_decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTFunctionDefinition f_def = (IASTFunctionDefinition) tu
                .getDeclarations()[1];

        IASTName a1 = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) S_decl
                .getDeclSpecifier()).getMembers()[0]).getDeclarators()[0]
                .getName();
        IASTName b1 = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) S_decl
                .getDeclSpecifier()).getMembers()[1]).getDeclarators()[0]
                .getName();
        IASTName a2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) ((IASTInitializerList) ((IASTSimpleDeclaration) ((IASTDeclarationStatement) ((IASTCompoundStatement) f_def
                .getBody()).getStatements()[0]).getDeclaration())
                .getDeclarators()[0].getInitializer()).getInitializers()[0])
                .getDesignators()[0]).getName();
        IASTName b2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) ((IASTInitializerList) ((IASTSimpleDeclaration) ((IASTDeclarationStatement) ((IASTCompoundStatement) f_def
                .getBody()).getStatements()[0]).getDeclaration())
                .getDeclarators()[0].getInitializer()).getInitializers()[1])
                .getDesignators()[0]).getName();

        assertEquals(a1.resolveBinding(), a2.resolveBinding());
        assertEquals(b1.resolveBinding(), b2.resolveBinding());

        IASTName[] decls = tu.getDeclarations(a1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(a1, decls[0]);

        decls = tu.getDeclarations(b1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(b1, decls[0]);
    }

    public void testMoreGetDeclarations2() throws Exception {
        StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
        buffer.append(" struct S { \n"); //$NON-NLS-1$
        buffer.append(" int a; \n"); //$NON-NLS-1$
        buffer.append(" int b; \n"); //$NON-NLS-1$
        buffer.append("} s = {.a=1,.b=2};\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration S_decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];

        IASTName a1 = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) S_decl
                .getDeclSpecifier()).getMembers()[0]).getDeclarators()[0]
                .getName();
        IASTName b1 = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) S_decl
                .getDeclSpecifier()).getMembers()[1]).getDeclarators()[0]
                .getName();
        IASTName a2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) ((IASTInitializerList) S_decl
                .getDeclarators()[0].getInitializer()).getInitializers()[0])
                .getDesignators()[0]).getName();
        IASTName b2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) ((IASTInitializerList) S_decl
                .getDeclarators()[0].getInitializer()).getInitializers()[1])
                .getDesignators()[0]).getName();

        assertEquals(a1.resolveBinding(), a2.resolveBinding());
        assertEquals(b1.resolveBinding(), b2.resolveBinding());

        IASTName[] decls = tu.getDeclarations(a1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(a1, decls[0]);

        decls = tu.getDeclarations(b1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(b1, decls[0]);
    }

    public void testMoreGetDeclarations3() throws Exception {
        StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
        buffer.append(" typedef struct S { \n"); //$NON-NLS-1$
        buffer.append(" int a; \n"); //$NON-NLS-1$
        buffer.append(" int b; \n"); //$NON-NLS-1$
        buffer.append("} s;\n"); //$NON-NLS-1$
        buffer.append("typedef s t;\n"); //$NON-NLS-1$
        buffer.append("typedef t y;\n"); //$NON-NLS-1$
        buffer.append("y x = {.a=1,.b=2};\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration S_decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTSimpleDeclaration x_decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[3];

        IASTName a1 = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) S_decl
                .getDeclSpecifier()).getMembers()[0]).getDeclarators()[0]
                .getName();
        IASTName b1 = ((IASTSimpleDeclaration) ((IASTCompositeTypeSpecifier) S_decl
                .getDeclSpecifier()).getMembers()[1]).getDeclarators()[0]
                .getName();
        IASTName a2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) ((IASTInitializerList) x_decl
                .getDeclarators()[0].getInitializer()).getInitializers()[0])
                .getDesignators()[0]).getName();
        IASTName b2 = ((ICASTFieldDesignator) ((ICASTDesignatedInitializer) ((IASTInitializerList) x_decl
                .getDeclarators()[0].getInitializer()).getInitializers()[1])
                .getDesignators()[0]).getName();

        assertEquals(a1.resolveBinding(), a2.resolveBinding());
        assertEquals(b1.resolveBinding(), b2.resolveBinding());

        IASTName[] decls = tu.getDeclarations(a1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(a1, decls[0]);

        decls = tu.getDeclarations(b1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(b1, decls[0]);
    }

    public void testFnReturningPtrToFn() throws Exception {
        IASTTranslationUnit tu = parse(
                "void ( * f( int ) )(){}", ParserLanguage.C); //$NON-NLS-1$

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[0];
        IFunction f = (IFunction) def.getDeclarator().getNestedDeclarator()
                .getName().resolveBinding();

        IFunctionType ft = f.getType();
        assertTrue(ft.getReturnType() instanceof IPointerType);
        assertTrue(((IPointerType) ft.getReturnType()).getType() instanceof IFunctionType);
        assertEquals(ft.getParameterTypes().length, 1);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(def.getDeclarator()
                .getNestedDeclarator().getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], def.getDeclarator().getNestedDeclarator()
                .getName());
    }

    // test C99: 6.7.5.3-7 A declaration of a parameter as ‘‘array of type’’
    // shall be adjusted to ‘‘qualified pointer to
    // type’’, where the type qualifiers (if any) are those specified within the
    // [ and ] of the
    // array type derivation.
    public void testArrayTypeToQualifiedPointerTypeParm() throws Exception {
        IASTTranslationUnit tu = parse(
                "void f(int parm[const 3]);", ParserLanguage.C); //$NON-NLS-1$

        IASTSimpleDeclaration def = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IFunction f = (IFunction) def.getDeclarators()[0].getName()
                .resolveBinding();

        IFunctionType ft = f.getType();
        assertTrue(ft.getParameterTypes()[0] instanceof IPointerType);
        assertTrue(((IPointerType) ft.getParameterTypes()[0]).isConst());

        // test tu.getDeclarations(IBinding)
        IASTName name_parm = ((IASTStandardFunctionDeclarator) def
                .getDeclarators()[0]).getParameters()[0].getDeclarator()
                .getName();
        IASTName[] decls = tu.getDeclarations(name_parm.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_parm);
    }

    public void testFunctionDefTypes() throws Exception {
        StringBuffer buffer = new StringBuffer("int f() {}\n"); //$NON-NLS-1$
        buffer.append("int *f2() {}\n"); //$NON-NLS-1$
        buffer.append("int (* f3())() {}\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C); //$NON-NLS-1$

        IASTFunctionDefinition def1 = (IASTFunctionDefinition) tu
                .getDeclarations()[0];
        IFunction f = (IFunction) def1.getDeclarator().getName()
                .resolveBinding();
        IASTFunctionDefinition def2 = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IFunction f2 = (IFunction) def2.getDeclarator().getName()
                .resolveBinding();
        IASTFunctionDefinition def3 = (IASTFunctionDefinition) tu
                .getDeclarations()[2];
        IFunction f3 = (IFunction) def3.getDeclarator().getName()
                .resolveBinding();

        IFunctionType ft = f.getType();
        IFunctionType ft2 = f2.getType();
        IFunctionType ft3 = f3.getType();

        assertTrue(ft.getReturnType() instanceof IBasicType);
        assertTrue(ft2.getReturnType() instanceof IPointerType);
        assertTrue(((IPointerType) ft2.getReturnType()).getType() instanceof IBasicType);
        assertTrue(ft3.getReturnType() instanceof IPointerType);
        assertTrue(((IPointerType) ft3.getReturnType()).getType() instanceof IFunctionType);
        assertTrue(((IFunctionType) ((IPointerType) ft3.getReturnType())
                .getType()).getReturnType() instanceof IBasicType);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu.getDeclarations(def1.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], def1.getDeclarator().getName());

        decls = tu.getDeclarations(def2.getDeclarator().getName()
                .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], def2.getDeclarator().getName());

        decls = tu.getDeclarations(def3.getDeclarator().getNestedDeclarator()
                .getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], def3.getDeclarator().getNestedDeclarator()
                .getName());
    }

    // any parameter to type function returning T is adjusted to be pointer to
    // function returning T
    public void testParmToFunction() throws Exception {
        IASTTranslationUnit tu = parse(
                "int f(int g(void)) { return g();}", ParserLanguage.C); //$NON-NLS-1$

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[0];
        IFunction f = (IFunction) def.getDeclarator().getName()
                .resolveBinding();

        IType ft = ((CFunction) f).getType();
        assertTrue(ft instanceof IFunctionType);
        IType gt_1 = ((IFunctionType) ft).getParameterTypes()[0];
        assertTrue(gt_1 instanceof IPointerType);
        IType gt_2 = ((IPointerType) gt_1).getType();
        assertTrue(gt_2 instanceof IFunctionType);
        IType gt_ret = ((IFunctionType) gt_2).getReturnType();
        assertTrue(gt_ret instanceof IBasicType);
        assertEquals(((IBasicType) gt_ret).getType(), IBasicType.t_int);
        IType gt_parm = ((IFunctionType) gt_2).getParameterTypes()[0];
        assertTrue(gt_parm instanceof IBasicType);
        assertEquals(((IBasicType) gt_parm).getType(), IBasicType.t_void);

        // test tu.getDeclarations(IBinding)
        assertTrue(def.getDeclarator() instanceof IASTStandardFunctionDeclarator);
        IASTName name_g = ((IASTStandardFunctionDeclarator) def.getDeclarator())
                .getParameters()[0].getDeclarator().getName();
        IASTName name_g_call = ((IASTIdExpression) ((IASTFunctionCallExpression) ((IASTReturnStatement) ((IASTCompoundStatement) def
                .getBody()).getStatements()[0]).getReturnValue())
                .getFunctionNameExpression()).getName();
        IASTName[] decls = tu.getDeclarations(name_g_call.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_g);
    }

    public void testArrayPointerFunction() throws Exception {
        IASTTranslationUnit tu = parse(
                "int (*v[])(int *x, int *y);", ParserLanguage.C); //$NON-NLS-1$

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IVariable v = (IVariable) ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0]).getNestedDeclarator().getName()
                .resolveBinding();

        IType vt_1 = v.getType();
        assertTrue(vt_1 instanceof IArrayType);
        IType vt_2 = ((IArrayType) vt_1).getType();
        assertTrue(vt_2 instanceof IPointerType);
        IType vt_3 = ((IPointerType) vt_2).getType();
        assertTrue(vt_3 instanceof IFunctionType);
        IType vt_ret = ((IFunctionType) vt_3).getReturnType();
        assertTrue(vt_ret instanceof IBasicType);
        assertEquals(((IBasicType) vt_ret).getType(), IBasicType.t_int);
        assertEquals(((IFunctionType) vt_3).getParameterTypes().length, 2);
        IType vpt_1 = ((IFunctionType) vt_3).getParameterTypes()[0];
        assertTrue(vpt_1 instanceof IPointerType);
        IType vpt_1_2 = ((IPointerType) vpt_1).getType();
        assertTrue(vpt_1_2 instanceof IBasicType);
        assertEquals(((IBasicType) vpt_1_2).getType(), IBasicType.t_int);
        IType vpt_2 = ((IFunctionType) vt_3).getParameterTypes()[0];
        assertTrue(vpt_2 instanceof IPointerType);
        IType vpt_2_2 = ((IPointerType) vpt_1).getType();
        assertTrue(vpt_2_2 instanceof IBasicType);
        assertEquals(((IBasicType) vpt_2_2).getType(), IBasicType.t_int);

        // test tu.getDeclarations(IBinding)
        IASTName[] decls = tu
                .getDeclarations(((IASTStandardFunctionDeclarator) decl
                        .getDeclarators()[0]).getNestedDeclarator().getName()
                        .resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], ((IASTStandardFunctionDeclarator) decl
                .getDeclarators()[0]).getNestedDeclarator().getName());
    }

    public void testTypedefExample4a() throws Exception {
        StringBuffer buffer = new StringBuffer("typedef void DWORD;\n"); //$NON-NLS-1$
        buffer.append("typedef DWORD v;\n"); //$NON-NLS-1$
        buffer.append("v signal(int);\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        ITypedef dword = (ITypedef) decl1.getDeclarators()[0].getName()
                .resolveBinding();
        IType dword_t = dword.getType();
        assertTrue(dword_t instanceof IBasicType);
        assertEquals(((IBasicType) dword_t).getType(), IBasicType.t_void);

        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) tu
                .getDeclarations()[1];
        ITypedef v = (ITypedef) decl2.getDeclarators()[0].getName()
                .resolveBinding();
        IType v_t_1 = v.getType();
        assertTrue(v_t_1 instanceof ITypedef);
        IType v_t_2 = ((ITypedef) v_t_1).getType();
        assertTrue(v_t_2 instanceof IBasicType);
        assertEquals(((IBasicType) v_t_2).getType(), IBasicType.t_void);

        IASTSimpleDeclaration decl3 = (IASTSimpleDeclaration) tu
                .getDeclarations()[2];
        IFunction signal = (IFunction) decl3.getDeclarators()[0].getName()
                .resolveBinding();
        IFunctionType signal_t = signal.getType();
        IType signal_ret = signal_t.getReturnType();
        assertTrue(signal_ret instanceof ITypedef);
        IType signal_ret2 = ((ITypedef) signal_ret).getType();
        assertTrue(signal_ret2 instanceof ITypedef);
        IType signal_ret3 = ((ITypedef) signal_ret2).getType();
        assertTrue(signal_ret3 instanceof IBasicType);
        assertEquals(((IBasicType) signal_ret3).getType(), IBasicType.t_void);

        // test tu.getDeclarations(IBinding)
        IASTName name_DWORD = decl1.getDeclarators()[0].getName();
        IASTName name_v = decl2.getDeclarators()[0].getName();

        IASTName[] decls = tu.getDeclarations(name_DWORD.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_DWORD);

        decls = tu.getDeclarations(((IASTNamedTypeSpecifier) decl2
                .getDeclSpecifier()).getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_DWORD);

        decls = tu.getDeclarations(((IASTNamedTypeSpecifier) decl3
                .getDeclSpecifier()).getName().resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_v);
    }

    public void testTypedefExample4b() throws Exception {
        StringBuffer buffer = new StringBuffer("typedef void DWORD;\n"); //$NON-NLS-1$
        buffer.append("typedef DWORD (*pfv)(int);\n"); //$NON-NLS-1$
        buffer.append("pfv signal(int, pfv);\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        ITypedef dword = (ITypedef) decl1.getDeclarators()[0].getName()
                .resolveBinding();
        IType dword_t = dword.getType();
        assertTrue(dword_t instanceof IBasicType);
        assertEquals(((IBasicType) dword_t).getType(), IBasicType.t_void);

        IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) tu
                .getDeclarations()[1];
        ITypedef pfv = (ITypedef) decl2.getDeclarators()[0]
                .getNestedDeclarator().getName().resolveBinding();
        IType pfv_t_1 = pfv.getType();
        assertTrue(pfv_t_1 instanceof IPointerType);
        IType pfv_t_2 = ((IPointerType) pfv_t_1).getType();
        assertTrue(pfv_t_2 instanceof IFunctionType);
        IType pfv_t_2_ret_1 = ((IFunctionType) pfv_t_2).getReturnType();
        assertTrue(pfv_t_2_ret_1 instanceof ITypedef);
        IType pfv_t_2_ret_2 = ((ITypedef) pfv_t_2_ret_1).getType();
        assertTrue(pfv_t_2_ret_2 instanceof IBasicType);
        assertEquals(((IBasicType) pfv_t_2_ret_2).getType(), IBasicType.t_void);
        assertTrue(((ITypedef) pfv_t_2_ret_1).getName().equals("DWORD")); //$NON-NLS-1$
        IType pfv_t_2_parm = ((IFunctionType) pfv_t_2).getParameterTypes()[0];
        assertTrue(pfv_t_2_parm instanceof IBasicType);
        assertEquals(((IBasicType) pfv_t_2_parm).getType(), IBasicType.t_int);

        IASTSimpleDeclaration decl3 = (IASTSimpleDeclaration) tu
                .getDeclarations()[2];
        IFunction signal = (IFunction) decl3.getDeclarators()[0].getName()
                .resolveBinding();
        IFunctionType signal_t = signal.getType();
        IType signal_ret_1 = signal_t.getReturnType();
        assertTrue(signal_ret_1 instanceof ITypedef);
        IType signal_ret_2 = ((ITypedef) signal_ret_1).getType();
        assertTrue(signal_ret_2 instanceof IPointerType);
        IType signal_ret_3 = ((IPointerType) signal_ret_2).getType();
        assertTrue(signal_ret_3 instanceof IFunctionType);
        IType signal_ret_ret_1 = ((IFunctionType) signal_ret_3).getReturnType();
        assertTrue(signal_ret_ret_1 instanceof ITypedef);
        IType signal_ret_ret_2 = ((ITypedef) signal_ret_ret_1).getType();
        assertTrue(signal_ret_ret_2 instanceof IBasicType);
        assertEquals(((IBasicType) signal_ret_ret_2).getType(),
                IBasicType.t_void);
        assertTrue(((ITypedef) signal_ret_ret_1).getName().equals("DWORD")); //$NON-NLS-1$

        IType signal_parm_t1 = signal_t.getParameterTypes()[0];
        assertTrue(signal_parm_t1 instanceof IBasicType);
        assertEquals(((IBasicType) signal_parm_t1).getType(), IBasicType.t_int);
        IType signal_parm_t2 = signal_t.getParameterTypes()[1];
        assertTrue(signal_parm_t2 instanceof ITypedef);
        IType signal_parm_t2_1 = ((ITypedef) signal_parm_t2).getType();
        assertTrue(signal_parm_t2_1 instanceof IPointerType);
        IType signal_parm_t2_2 = ((IPointerType) signal_parm_t2_1).getType();
        assertTrue(signal_parm_t2_2 instanceof IFunctionType);
        IType signal_parm_t2_ret_1 = ((IFunctionType) signal_parm_t2_2)
                .getReturnType();
        assertTrue(signal_parm_t2_ret_1 instanceof ITypedef);
        IType signal_parm_t2_ret_2 = ((ITypedef) signal_parm_t2_ret_1)
                .getType();
        assertTrue(signal_parm_t2_ret_2 instanceof IBasicType);
        assertEquals(((IBasicType) signal_parm_t2_ret_2).getType(),
                IBasicType.t_void);
        assertTrue(((ITypedef) signal_parm_t2_ret_1).getName().equals("DWORD")); //$NON-NLS-1$

        // test tu.getDeclarations(IBinding)
        IASTName name_pfv = decl2.getDeclarators()[0].getNestedDeclarator()
                .getName();
        IASTName name_pfv1 = ((IASTNamedTypeSpecifier) decl3.getDeclSpecifier())
                .getName();
        IASTName name_pfv2 = ((IASTNamedTypeSpecifier) ((IASTStandardFunctionDeclarator) decl3
                .getDeclarators()[0]).getParameters()[1].getDeclSpecifier())
                .getName();

        IASTName[] decls = tu.getDeclarations(name_pfv1.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_pfv);

        decls = tu.getDeclarations(name_pfv2.resolveBinding());
        assertEquals(decls.length, 1);
        assertEquals(decls[0], name_pfv);
    }

    public void testTypedefExample4c() throws Exception {
        StringBuffer buffer = new StringBuffer(
                "typedef void fv(int), (*pfv)(int);\n"); //$NON-NLS-1$
        buffer.append("void (*signal1(int, void (*)(int)))(int);\n"); //$NON-NLS-1$
        buffer.append("fv *signal2(int, fv *);\n"); //$NON-NLS-1$    	
        buffer.append("pfv signal3(int, pfv);\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        ITypedef fv = (ITypedef) decl.getDeclarators()[0].getName()
                .resolveBinding();
        ITypedef pfv = (ITypedef) decl.getDeclarators()[1]
                .getNestedDeclarator().getName().resolveBinding();

        IType fv_t = fv.getType();
        assertEquals(((IBasicType) ((IFunctionType) fv_t).getReturnType())
                .getType(), IBasicType.t_void);
        assertEquals(
                ((IBasicType) ((IFunctionType) fv_t).getParameterTypes()[0])
                        .getType(), IBasicType.t_int);

        IType pfv_t = pfv.getType();
        assertEquals(((IBasicType) ((IFunctionType) ((IPointerType) pfv_t)
                .getType()).getReturnType()).getType(), IBasicType.t_void);
        assertEquals(((IBasicType) ((IFunctionType) ((IPointerType) pfv
                .getType()).getType()).getParameterTypes()[0]).getType(),
                IBasicType.t_int);

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IFunction signal1 = (IFunction) decl.getDeclarators()[0]
                .getNestedDeclarator().getName().resolveBinding();
        IType signal1_t = signal1.getType();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IFunction signal2 = (IFunction) decl.getDeclarators()[0].getName()
                .resolveBinding();
        IType signal2_t = signal2.getType();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IFunction signal3 = (IFunction) decl.getDeclarators()[0].getName()
                .resolveBinding();
        IType signal3_t = signal3.getType();

        assertEquals(
                ((IBasicType) ((IFunctionType) ((IPointerType) ((IFunctionType) signal1_t)
                        .getReturnType()).getType()).getReturnType()).getType(),
                IBasicType.t_void);
        assertEquals(((IBasicType) ((IFunctionType) signal1_t)
                .getParameterTypes()[0]).getType(), IBasicType.t_int);
        assertEquals(
                ((IBasicType) ((IFunctionType) ((IPointerType) ((IFunctionType) signal1_t)
                        .getParameterTypes()[1]).getType()).getReturnType())
                        .getType(), IBasicType.t_void);
        assertEquals(
                ((IBasicType) ((IFunctionType) ((IPointerType) ((IFunctionType) signal1_t)
                        .getParameterTypes()[1]).getType()).getParameterTypes()[0])
                        .getType(), IBasicType.t_int);

        assertEquals(
                ((IBasicType) ((IFunctionType) ((ITypedef) ((IPointerType) ((IFunctionType) signal2_t)
                        .getReturnType()).getType()).getType()).getReturnType())
                        .getType(), IBasicType.t_void);
        assertEquals(((IBasicType) ((IFunctionType) signal2_t)
                .getParameterTypes()[0]).getType(), IBasicType.t_int);
        assertEquals(
                ((IBasicType) ((IFunctionType) ((ITypedef) ((IPointerType) ((IFunctionType) signal2_t)
                        .getParameterTypes()[1]).getType()).getType())
                        .getReturnType()).getType(), IBasicType.t_void);
        assertEquals(
                ((IBasicType) ((IFunctionType) ((ITypedef) ((IPointerType) ((IFunctionType) signal2_t)
                        .getParameterTypes()[1]).getType()).getType())
                        .getParameterTypes()[0]).getType(), IBasicType.t_int);

        assertEquals(
                ((IBasicType) ((IFunctionType) ((IPointerType) ((ITypedef) ((IFunctionType) signal3_t)
                        .getReturnType()).getType()).getType()).getReturnType())
                        .getType(), IBasicType.t_void);
        assertEquals(((IBasicType) ((IFunctionType) signal3_t)
                .getParameterTypes()[0]).getType(), IBasicType.t_int);
        assertEquals(
                ((IBasicType) ((IFunctionType) ((IPointerType) ((ITypedef) ((IFunctionType) signal3_t)
                        .getParameterTypes()[1]).getType()).getType())
                        .getReturnType()).getType(), IBasicType.t_void);
        assertEquals(
                ((IBasicType) ((IFunctionType) ((IPointerType) ((ITypedef) ((IFunctionType) signal3_t)
                        .getParameterTypes()[1]).getType()).getType())
                        .getParameterTypes()[0]).getType(), IBasicType.t_int);

    }

    public void testBug80992() throws Exception {
        StringBuffer buffer = new StringBuffer("const int x = 10;\n"); //$NON-NLS-1$
        buffer.append("int y [ const static x ];"); //$NON-NLS-1$
        ICASTArrayModifier mod = (ICASTArrayModifier) ((IASTArrayDeclarator) ((IASTSimpleDeclaration) parse(
                buffer.toString(), ParserLanguage.C).getDeclarations()[1])
                .getDeclarators()[0]).getArrayModifiers()[0];
        assertTrue(mod.isConst());
        assertTrue(mod.isStatic());
        assertFalse(mod.isRestrict());
        assertFalse(mod.isVolatile());
        assertFalse(mod.isVariableSized());
    }

    public void testBug80978() throws Exception {
        StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
        buffer.append("int y ( int [ const *] );"); //$NON-NLS-1$
        ICASTArrayModifier mod = (ICASTArrayModifier) ((IASTArrayDeclarator) ((IASTStandardFunctionDeclarator) ((IASTSimpleDeclaration) parse(
                buffer.toString(), ParserLanguage.C).getDeclarations()[0])
                .getDeclarators()[0]).getParameters()[0].getDeclarator())
                .getArrayModifiers()[0];
        assertTrue(mod.isConst());
        assertTrue(mod.isVariableSized());
        assertFalse(mod.isStatic());
        assertFalse(mod.isRestrict());
        assertFalse(mod.isVolatile());
    }

    public void testExternalVariable() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {               \n"); //$NON-NLS-1$
        buffer.append("   if( a == 0 )          \n"); //$NON-NLS-1$
        buffer.append("      a = a + 3;         \n"); //$NON-NLS-1$
        buffer.append("}                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        IVariable a = (IVariable) col.getName(1).resolveBinding();
        assertNotNull(a);
        assertTrue(a instanceof ICExternalBinding);
        assertInstances(col, a, 3);
    }

    public void testExternalDefs() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {               \n"); //$NON-NLS-1$
        buffer.append("   if( a == 0 )          \n"); //$NON-NLS-1$
        buffer.append("      g( a );            \n"); //$NON-NLS-1$
        buffer.append("   if( a < 0 )           \n"); //$NON-NLS-1$
        buffer.append("      g( a >> 1 );       \n"); //$NON-NLS-1$
        buffer.append("   if( a > 0 )           \n"); //$NON-NLS-1$
        buffer.append("      g( *(&a + 2) );    \n"); //$NON-NLS-1$
        buffer.append("}                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        IVariable a = (IVariable) col.getName(1).resolveBinding();
        IFunction g = (IFunction) col.getName(2).resolveBinding();
        assertNotNull(a);
        assertNotNull(g);
        assertTrue(a instanceof ICExternalBinding);
        assertTrue(g instanceof ICExternalBinding);

        assertEquals(col.size(), 10);
        assertInstances(col, a, 6);
        assertInstances(col, g, 3);
    }

    public void testFieldDesignators() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("typedef struct { int x; int y; } Coord;  \n"); //$NON-NLS-1$
        buffer.append("int f() {                               \n"); //$NON-NLS-1$
        buffer.append("   Coord xy = { .x = 10, .y = 11 };     \n"); //$NON-NLS-1$
        buffer.append("}                                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 9);
        IField x = (IField) col.getName(1).resolveBinding();
        IField y = (IField) col.getName(2).resolveBinding();
        ITypedef Coord = (ITypedef) col.getName(3).resolveBinding();

        assertInstances(col, x, 2);
        assertInstances(col, y, 2);
        assertInstances(col, Coord, 2);
    }

    public void testArrayDesignator() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("enum { member_one, member_two };    \n"); //$NON-NLS-1$
        buffer.append("const char *nm[] = {                \n"); //$NON-NLS-1$
        buffer.append("   [member_one] = \"one\",          \n"); //$NON-NLS-1$
        buffer.append("   [member_two] = \"two\"           \n"); //$NON-NLS-1$
        buffer.append("};                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 6);
        IEnumerator one = (IEnumerator) col.getName(1).resolveBinding();
        IEnumerator two = (IEnumerator) col.getName(2).resolveBinding();

        assertInstances(col, one, 2);
        assertInstances(col, two, 2);
    }

    public void testBug83737() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {\n"); //$NON-NLS-1$
        buffer.append("if( a == 0 )\n"); //$NON-NLS-1$
        buffer.append("g( a );\n"); //$NON-NLS-1$
        buffer.append("else if( a < 0 )\n"); //$NON-NLS-1$
        buffer.append("g( a >> 1 );\n"); //$NON-NLS-1$
        buffer.append("else if( a > 0 )\n"); //$NON-NLS-1$
        buffer.append("g( *(&a + 2) );\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        IASTIfStatement if_statement = (IASTIfStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
                .getDeclarations()[0]).getBody()).getStatements()[0];
        assertEquals(((IASTBinaryExpression) if_statement
                .getConditionExpression()).getOperator(),
                IASTBinaryExpression.op_equals);
        IASTIfStatement second_if_statement = (IASTIfStatement) if_statement
                .getElseClause();
        assertEquals(((IASTBinaryExpression) second_if_statement
                .getConditionExpression()).getOperator(),
                IASTBinaryExpression.op_lessThan);
        IASTIfStatement third_if_statement = (IASTIfStatement) second_if_statement
                .getElseClause();
        assertEquals(((IASTBinaryExpression) third_if_statement
                .getConditionExpression()).getOperator(),
                IASTBinaryExpression.op_greaterThan);
    }

    public void testBug84090_LabelReferences() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                    \n"); //$NON-NLS-1$
        buffer.append("   while(1){                  \n"); //$NON-NLS-1$
        buffer.append("      if( 1 ) goto end;       \n"); //$NON-NLS-1$
        buffer.append("   }                          \n"); //$NON-NLS-1$
        buffer.append("   end: ;                     \n"); //$NON-NLS-1$
        buffer.append("}                             \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 3);
        ILabel end = (ILabel) col.getName(1).resolveBinding();

        IASTName[] refs = tu.getReferences(end);
        assertEquals(refs.length, 1);
        assertSame(refs[0].resolveBinding(), end);
    }

    public void testBug84092_EnumReferences() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("enum col { red, blue };    \n"); //$NON-NLS-1$
        buffer.append("enum col c;                \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector collector = new CNameCollector();
        tu.accept(collector);

        assertEquals(collector.size(), 5);
        IEnumeration col = (IEnumeration) collector.getName(0).resolveBinding();

        IASTName[] refs = tu.getReferences(col);
        assertEquals(refs.length, 1);
        assertSame(refs[0].resolveBinding(), col);
    }

    public void testBug84096_FieldDesignatorRef() throws Exception {
        IASTTranslationUnit tu = parse(
                "struct s { int a; } ss = { .a = 1 }; \n", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector collector = new CNameCollector();
        tu.accept(collector);

        assertEquals(collector.size(), 4);
        IField a = (IField) collector.getName(1).resolveBinding();

        IASTName[] refs = tu.getReferences(a);
        assertEquals(refs.length, 1);
        assertSame(refs[0].resolveBinding(), a);
    }

    public void testProblems() throws Exception {

        IASTTranslationUnit tu = parse(
                "    a += ;", ParserLanguage.C, true, false); //$NON-NLS-1$
        IASTProblem[] ps = CVisitor.getProblems(tu);
        assertEquals(ps.length, 1);
        ps[0].getMessage();
    }

    public void testEnumerationForwards() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("enum e;          \n;"); //$NON-NLS-1$
        buffer.append("enum e{ one };   \n;"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 3);
        IEnumeration e = (IEnumeration) col.getName(0).resolveBinding();
        IEnumerator[] etors = e.getEnumerators();
        assertTrue(etors.length == 1);
        assertFalse(etors[0] instanceof IProblemBinding);

        assertInstances(col, e, 2);
    }

    public void testBug84185() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                 \n"); //$NON-NLS-1$
        buffer.append("   int ( *p ) [2];         \n"); //$NON-NLS-1$
        buffer.append("   (&p)[0] = 1;            \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 3);
        IVariable p = (IVariable) col.getName(1).resolveBinding();
        assertTrue(p.getType() instanceof IPointerType);
        assertTrue(((IPointerType) p.getType()).getType() instanceof IArrayType);
        IArrayType at = (IArrayType) ((IPointerType) p.getType()).getType();
        assertTrue(at.getType() instanceof IBasicType);

        assertInstances(col, p, 2);
    }

    public void testBug84185_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                 \n"); //$NON-NLS-1$
        buffer.append("   int ( *p ) [2];         \n"); //$NON-NLS-1$
        buffer.append("   (&p)[0] = 1;            \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 3);

        IVariable p_ref = (IVariable) col.getName(2).resolveBinding();
        IVariable p_decl = (IVariable) col.getName(1).resolveBinding();

        assertSame(p_ref, p_decl);
    }

    public void testBug84176() throws Exception {
        StringBuffer buffer = new StringBuffer(
                "// example from: C99 6.5.2.5-16\n"); //$NON-NLS-1$
        buffer.append("struct s { int i; };\n"); //$NON-NLS-1$
        buffer.append("void f (void)\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("		 struct s *p = 0, *q;\n"); //$NON-NLS-1$
        buffer.append("int j = 0;\n"); //$NON-NLS-1$
        buffer.append("q = p;\n"); //$NON-NLS-1$
        buffer.append("p = &((struct s){ j++ }); \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        parse(buffer.toString(), ParserLanguage.C, false, true);
    }

    public void testBug84266() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct s { double i; } f(void);  \n"); //$NON-NLS-1$
        buffer.append("struct s f(void){}               \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 7);

        ICompositeType s_ref = (ICompositeType) col.getName(4).resolveBinding();
        ICompositeType s_decl = (ICompositeType) col.getName(0)
                .resolveBinding();

        assertSame(s_ref, s_decl);
        CVisitor.clearBindings(tu);

        s_decl = (ICompositeType) col.getName(0).resolveBinding();
        s_ref = (ICompositeType) col.getName(4).resolveBinding();

        assertSame(s_ref, s_decl);
    }

    public void testBug84266_2() throws Exception {
        IASTTranslationUnit tu = parse("struct s f(void);", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 3);

        ICompositeType s = (ICompositeType) col.getName(0).resolveBinding();
        assertNotNull(s);

        tu = parse("struct s f(void){}", ParserLanguage.C); //$NON-NLS-1$
        col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 3);

        s = (ICompositeType) col.getName(0).resolveBinding();
        assertNotNull(s);
    }

    public void testBug84250() throws Exception {
        assertTrue(((IASTDeclarationStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) parse(
                "void f() { int (*p) [2]; }", ParserLanguage.C).getDeclarations()[0]).getBody()).getStatements()[0]).getDeclaration() instanceof IASTSimpleDeclaration); //$NON-NLS-1$
    }

    public void testBug84186() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct s1 { struct s2 *s2p; /* ... */ }; // D1 \n"); //$NON-NLS-1$
        buffer.append("struct s2 { struct s1 *s1p; /* ... */ }; // D2 \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 6);

        ICompositeType s_ref = (ICompositeType) col.getName(1).resolveBinding();
        ICompositeType s_decl = (ICompositeType) col.getName(3)
                .resolveBinding();

        assertSame(s_ref, s_decl);
        CVisitor.clearBindings(tu);

        s_decl = (ICompositeType) col.getName(3).resolveBinding();
        s_ref = (ICompositeType) col.getName(1).resolveBinding();

        assertSame(s_ref, s_decl);
    }

    public void testBug84267() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("typedef struct { int a; } S;      \n"); //$NON-NLS-1$
        buffer.append("void g( S* (*funcp) (void) ) {    \n"); //$NON-NLS-1$
        buffer.append("   (*funcp)()->a;                 \n"); //$NON-NLS-1$
        buffer.append("   funcp()->a;                    \n"); //$NON-NLS-1$
        buffer.append("}                                 \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 11);

        ITypedef S = (ITypedef) col.getName(2).resolveBinding();
        IField a = (IField) col.getName(10).resolveBinding();
        IParameter funcp = (IParameter) col.getName(7).resolveBinding();
        assertNotNull(funcp);
        assertInstances(col, funcp, 3);
        assertInstances(col, a, 3);

        assertTrue(funcp.getType() instanceof IPointerType);
        IType t = ((IPointerType) funcp.getType()).getType();
        assertTrue(t instanceof IFunctionType);
        IFunctionType ft = (IFunctionType) t;
        assertTrue(ft.getReturnType() instanceof IPointerType);
        assertSame(((IPointerType) ft.getReturnType()).getType(), S);
    }

    public void testBug84228() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( int m, int c[m][m] );        \n"); //$NON-NLS-1$
        buffer.append("void f( int m, int c[m][m] ){        \n"); //$NON-NLS-1$
        buffer.append("   int x;                            \n"); //$NON-NLS-1$
        buffer.append("   { int x = x; }                    \n"); //$NON-NLS-1$
        buffer.append("}                                    \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        assertEquals(col.size(), 13);

        IParameter m = (IParameter) col.getName(3).resolveBinding();
        IVariable x3 = (IVariable) col.getName(12).resolveBinding();
        IVariable x2 = (IVariable) col.getName(11).resolveBinding();
        IVariable x1 = (IVariable) col.getName(10).resolveBinding();

        assertSame(x2, x3);
        assertNotSame(x1, x2);

        assertInstances(col, m, 6);
        assertInstances(col, x1, 1);
        assertInstances(col, x2, 2);

        IASTName[] ds = tu.getDeclarations(x2);
        assertEquals(ds.length, 1);
        assertSame(ds[0], col.getName(11));
    }

    public void testBug84236() throws Exception {
        String code = "double maximum(double a[ ][*]);"; //$NON-NLS-1$
        IASTSimpleDeclaration d = (IASTSimpleDeclaration) parse(code,
                ParserLanguage.C).getDeclarations()[0];
        IASTStandardFunctionDeclarator fd = (IASTStandardFunctionDeclarator) d
                .getDeclarators()[0];
        IASTParameterDeclaration p = fd.getParameters()[0];
        IASTArrayDeclarator a = (IASTArrayDeclarator) p.getDeclarator();
        ICASTArrayModifier star = (ICASTArrayModifier) a.getArrayModifiers()[1];
        assertTrue(star.isVariableSized());

    }

  

    public void testBug85049() throws Exception {
        StringBuffer buffer = new StringBuffer("typedef int B;\n"); //$NON-NLS-1$
        buffer.append("void g() {\n"); //$NON-NLS-1$
        buffer.append("B * bp;  //1\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        IASTTranslationUnit t = parse(buffer.toString(), ParserLanguage.C);
        IASTFunctionDefinition g = (IASTFunctionDefinition) t.getDeclarations()[1];
        IASTCompoundStatement body = (IASTCompoundStatement) g.getBody();
        final IASTStatement statement = body.getStatements()[0];
        assertTrue(statement instanceof IASTDeclarationStatement);
        IASTSimpleDeclaration bp = (IASTSimpleDeclaration) ((IASTDeclarationStatement) statement)
                .getDeclaration();
        assertTrue(bp.getDeclarators()[0].getName().resolveBinding() instanceof IVariable);

    }



    public void testBug86766() throws Exception {
        IASTTranslationUnit tu = parse(
                "char foo; void foo(){}", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        IVariable foo = (IVariable) col.getName(0).resolveBinding();
        IProblemBinding prob = (IProblemBinding) col.getName(1)
                .resolveBinding();
        assertEquals(prob.getID(), IProblemBinding.SEMANTIC_INVALID_OVERLOAD);
        assertNotNull(foo);
    }



    public void testBug88338_C() throws Exception {
        IASTTranslationUnit tu = parse(
                "struct A; struct A* a;", ParserLanguage.C); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);

        assertTrue(col.getName(0).isDeclaration());
        assertFalse(col.getName(0).isReference());
        assertTrue(col.getName(1).isReference());
        assertFalse(col.getName(1).isDeclaration());

        tu = parse("struct A* a; struct A;", ParserLanguage.C); //$NON-NLS-1$
        col = new CPPNameCollector();
        tu.accept(col);

        col.getName(2).resolveBinding();

        assertTrue(col.getName(0).isDeclaration());
        assertFalse(col.getName(0).isReference());

        assertTrue(col.getName(2).isDeclaration());
        assertFalse(col.getName(2).isReference());
    }

    public void test88460() throws Exception {
        IASTTranslationUnit tu = parse("void f();", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        IFunction f = (IFunction) col.getName(0).resolveBinding();
        assertFalse(f.isStatic());
    }

    public void testBug90253() throws Exception {
        IASTTranslationUnit tu = parse(
                "void f(int par) { int v1; };", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        IFunction f = (IFunction) col.getName(0).resolveBinding();
        IParameter p = (IParameter) col.getName(1).resolveBinding();
        IVariable v1 = (IVariable) col.getName(2).resolveBinding();

        IScope scope = f.getFunctionScope();

        IBinding[] bs = scope.find("par"); //$NON-NLS-1$
        assertEquals(bs.length, 1);
        assertSame(bs[0], p);

        bs = scope.find("v1"); //$NON-NLS-1$
        assertEquals(bs.length, 1);
        assertSame(bs[0], v1);
    }

    public void testFind() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S {};                \n"); //$NON-NLS-1$
        buffer.append("int S;                      \n"); //$NON-NLS-1$
        buffer.append("void f( ) {                 \n"); //$NON-NLS-1$
        buffer.append("   int S;                   \n"); //$NON-NLS-1$
        buffer.append("   {                        \n"); //$NON-NLS-1$
        buffer.append("      S :  ;                \n"); //$NON-NLS-1$
        buffer.append("   }                        \n"); //$NON-NLS-1$
        buffer.append("}                           \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
        CNameCollector col = new CNameCollector();
        tu.accept(col);

        ICompositeType S1 = (ICompositeType) col.getName(0).resolveBinding();
        IVariable S2 = (IVariable) col.getName(1).resolveBinding();
        IFunction f = (IFunction) col.getName(2).resolveBinding();
        IVariable S3 = (IVariable) col.getName(3).resolveBinding();
        ILabel S4 = (ILabel) col.getName(4).resolveBinding();

        IScope scope = f.getFunctionScope();

        IBinding[] bs = scope.find("S"); //$NON-NLS-1$

        assertNotNull(S2);
        assertEquals(bs.length, 3);
        assertSame(bs[0], S3);
        assertSame(bs[1], S1);
        assertSame(bs[2], S4);
    }

    public void test92791() throws Exception {
        IASTTranslationUnit tu = parse(
                "void f() { int x, y; x * y; }", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector col = new CNameCollector();
        tu.accept(col);
        for (int i = 0; i < col.size(); ++i)
            assertFalse(col.getName(i).resolveBinding() instanceof IProblemBinding);

        tu = parse(
                "void f() { typedef int x; int y; x * y; }", ParserLanguage.C); //$NON-NLS-1$
        col = new CNameCollector();
        tu.accept(col);
        for (int i = 0; i < col.size(); ++i)
            assertFalse(col.getName(i).resolveBinding() instanceof IProblemBinding);

    }

    public void testBug85786() throws Exception {
        IASTTranslationUnit tu = parse(
                "void f( int ); void foo () { void * p = &f; ( (void (*) (int)) p ) ( 1 ); }", ParserLanguage.C); //$NON-NLS-1$
        CNameCollector nameResolver = new CNameCollector();
        tu.accept(nameResolver);
        assertNoProblemBindings(nameResolver);
    }

    protected void assertNoProblemBindings(CNameCollector col) {
        Iterator i = col.nameList.iterator();
        while (i.hasNext()) {
            IASTName n = (IASTName) i.next();
            assertFalse(n.resolveBinding() instanceof IProblemBinding);
        }
    }

    protected void assertProblemBindings(CNameCollector col, int count) {
        Iterator i = col.nameList.iterator();
        int sum = 0;
        while (i.hasNext()) {
            IASTName n = (IASTName) i.next();
            if (n.getBinding() instanceof IProblemBinding)
                ++sum;
        }
        assertEquals(count, sum);
    }

    public void testBug94365() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#define ONE(a, ...) int x\n"); //$NON-NLS-1$
        buffer.append("#define TWO(b, args...) int y\n"); //$NON-NLS-1$
        buffer.append("int main()\n"); //$NON-NLS-1$
        buffer.append("{\n"); //$NON-NLS-1$
        buffer.append("ONE(\"string\"); /* err */\n"); //$NON-NLS-1$
        buffer.append("TWO(\"string\"); /* err */\n"); //$NON-NLS-1$
        buffer.append("return 0;	\n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$

        parse(buffer.toString(), ParserLanguage.C);
    }

    public void testBug95119() throws Exception {
        StringBuffer buff = new StringBuffer();
        buff.append("#define MACRO(a)\n"); //$NON-NLS-1$
        buff.append("void main() {\n"); //$NON-NLS-1$
        buff.append("MACRO(\'\"\');\n"); //$NON-NLS-1$
        buff.append("}\n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C);
        IASTDeclaration[] declarations = tu.getDeclarations();
        assertEquals(declarations.length, 1);
        assertNotNull(declarations[0]);
        assertTrue(declarations[0] instanceof IASTFunctionDefinition);
        assertEquals(((IASTFunctionDefinition) declarations[0]).getDeclarator()
                .getName().toString(), "main");
        assertTrue(((IASTCompoundStatement) ((IASTFunctionDefinition) declarations[0])
                .getBody()).getStatements()[0] instanceof IASTNullStatement);

        buff = new StringBuffer();
        buff.append("#define MACRO(a)\n"); //$NON-NLS-1$
        buff.append("void main() {\n"); //$NON-NLS-1$
        buff.append("MACRO(\'X\');\n"); //$NON-NLS-1$
        buff.append("}\n"); //$NON-NLS-1$

        tu = parse(buff.toString(), ParserLanguage.C);
        declarations = tu.getDeclarations();
        assertEquals(declarations.length, 1);
        assertNotNull(declarations[0]);
        assertTrue(declarations[0] instanceof IASTFunctionDefinition);
        assertEquals(((IASTFunctionDefinition) declarations[0]).getDeclarator()
                .getName().toString(), "main");
        assertTrue(((IASTCompoundStatement) ((IASTFunctionDefinition) declarations[0])
                .getBody()).getStatements()[0] instanceof IASTNullStatement);
    }

    public void testBug81739() throws Exception {
        StringBuffer buffer = new StringBuffer("typedef long _TYPE;\n"); //$NON-NLS-1$
        buffer.append("typedef _TYPE TYPE;\n"); //$NON-NLS-1$
        buffer.append("int function(TYPE (* pfv)(int parm));\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.C);
    }
}