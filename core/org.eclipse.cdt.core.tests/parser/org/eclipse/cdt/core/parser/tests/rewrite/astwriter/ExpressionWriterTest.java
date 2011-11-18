/*******************************************************************************
 * Copyright (c) 2010 University of Applied Sciences Rapperswil (HSR).
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Pascal Kesseli (HSR) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCapture;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLambdaExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExpressionWriterTest extends TestCase {
    private static final String BR = System.getProperty("line.separator");
    private static CPPASTSimpleDeclSpecifier INT = new CPPASTSimpleDeclSpecifier();
    private static IASTName NO_NAME = new CPPASTName(new char[] {});
    private NodeCommentMap commentMap = new NodeCommentMap();
	private ASTVisitor visitor;

    static {
        INT.setType(IASTSimpleDeclSpecifier.t_int); 
    }

	@Override
    @Before
	protected void setUp() throws Exception {
		visitor = new ASTWriterVisitor(commentMap);
	}

    @Test
    public void testWriteLambdaExpressionEmptyIntroducerNoDeclarator() throws Exception {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        lambda.accept(visitor);
        String expected = "[] {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(expected, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionDefaultCaptureByCopyNoDeclarator() {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        lambda.setCaptureDefault(ICPPASTLambdaExpression.CaptureDefault.BY_COPY);
        lambda.accept(visitor);
        String expected = "[=] {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(expected, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionDefaultCaptureByReferenceNoDeclarator() {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        lambda.setCaptureDefault(ICPPASTLambdaExpression.CaptureDefault.BY_REFERENCE);
        lambda.accept(visitor);
        String expected = "[&] {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(expected, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionThisCaptureNoDeclarator() {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        lambda.addCapture(new CPPASTCapture());
        lambda.accept(visitor);
        String expected = "[this] {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(expected, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionMixedCaptureNoDeclarator() {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        lambda.setCaptureDefault(ICPPASTLambdaExpression.CaptureDefault.BY_COPY);
        lambda.addCapture(new CPPASTCapture());
        ICPPASTCapture x = new CPPASTCapture(), y = new CPPASTCapture();
        x.setIdentifier(new CPPASTName(new char[] { 'x' }));
        x.setIsByReference(true);
        y.setIdentifier(new CPPASTName(new char[] { 'y' }));
        lambda.addCapture(x);
        lambda.addCapture(y);
        lambda.accept(visitor);
        String r = "[=, this, &x, y] {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(r, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionEmptyIntroducerSimpleDeclarator() throws Exception {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        lambda.setDeclarator(getSimpleFunctionDeclarator());
        lambda.accept(visitor);
        String r = "[](int i, int j) {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(r, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionEmptyIntroducerMutableDeclarator() throws Exception {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        ICPPASTFunctionDeclarator f = getSimpleFunctionDeclarator();
        f.setMutable(true);
        lambda.setDeclarator(f);
        lambda.accept(visitor);
        String r = "[](int i, int j) mutable {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(r, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionEmptyIntroducerExceptionSpecificationDeclarator() throws Exception {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        ICPPASTFunctionDeclarator f = getSimpleFunctionDeclarator();
        f.addExceptionSpecificationTypeId(new CPPASTTypeId(INT, new CPPASTDeclarator(NO_NAME)));
        lambda.setDeclarator(f);
        lambda.accept(visitor);
        String r = "[](int i, int j) throw (int) {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(r, visitor.toString());
    }

    @Test
    public void testWriteLambdaExpressionEmptyIntroducerTrailingReturnTypeDeclarator() throws Exception {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        ICPPASTFunctionDeclarator f = getSimpleFunctionDeclarator();
        f.setTrailingReturnType(new CPPASTTypeId(INT, new CPPASTDeclarator(NO_NAME)));
        lambda.setDeclarator(f);
        lambda.accept(visitor);
        String r = "[](int i, int j) -> int {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(r, visitor.toString());
    }

    @Test
    public void testWriteAllEmbracingLambdaExpression() {
        ICPPASTLambdaExpression lambda = getEmptyLambdaExpression();
        ICPPASTFunctionDeclarator f = getSimpleFunctionDeclarator();
        lambda.setCaptureDefault(ICPPASTLambdaExpression.CaptureDefault.BY_REFERENCE);
        ICPPASTCapture x = new CPPASTCapture(), y = new CPPASTCapture();
        x.setIdentifier(new CPPASTName(new char[] { 'x' }));
        y.setIdentifier(new CPPASTName(new char[] { 'y' }));
        y.setIsByReference(true);
        lambda.addCapture(x);
        lambda.addCapture(y);
        lambda.addCapture(new CPPASTCapture());
        f.setMutable(true);
        f.setTrailingReturnType(new CPPASTTypeId(INT, new CPPASTDeclarator(NO_NAME)));
        f.addExceptionSpecificationTypeId(new CPPASTTypeId(INT, new CPPASTDeclarator(NO_NAME)));
        lambda.setDeclarator(f);
        lambda.accept(visitor);
        String r = "[&, x, &y, this](int i, int j) mutable throw (int) -> int {" + BR + "    return 7;" + BR + "}" + BR;
        Assert.assertEquals(r, visitor.toString());
    }

    private static ICPPASTFunctionDeclarator getSimpleFunctionDeclarator() {
        ICPPASTFunctionDeclarator f = new CPPASTFunctionDeclarator(new CPPASTName());
        IASTName name = new CPPASTName(new char[] { 'i' });
        IASTDeclarator d = new CPPASTDeclarator(name);
        f.addParameterDeclaration(new CPPASTParameterDeclaration(INT, d));
        name = new CPPASTName(new char[] { 'j' });
        d = new CPPASTDeclarator(name);
        f.addParameterDeclaration(new CPPASTParameterDeclaration(INT, d));
        return f;
    }

    private static ICPPASTLambdaExpression getEmptyLambdaExpression() {
        ICPPASTLambdaExpression lambda = new CPPASTLambdaExpression();
        CPPASTCompoundStatement stmt = new CPPASTCompoundStatement();
        stmt.addStatement(new CPPASTReturnStatement(new CPPASTLiteralExpression(
        		IASTLiteralExpression.lk_integer_constant, new char[] { '7' })));
        lambda.setBody(stmt);
        return lambda;
    }
}
