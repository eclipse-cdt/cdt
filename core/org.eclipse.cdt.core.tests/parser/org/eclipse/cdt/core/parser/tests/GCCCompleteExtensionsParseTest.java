/**********************************************************************
 * Copyright (c) 2004 IBM Canada Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class GCCCompleteExtensionsParseTest extends CompleteParseBaseTest {

	/**
	 * 
	 */
	public GCCCompleteExtensionsParseTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public GCCCompleteExtensionsParseTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

    public void testBug39695() throws Exception
    {
        Iterator i = parse("int a = __alignof__ (int);").getDeclarations(); //$NON-NLS-1$
        IASTVariable a = (IASTVariable) i.next();
        assertFalse( i.hasNext() );
        IASTExpression exp = a.getInitializerClause().getAssigmentExpression();
        assertEquals( exp.getExpressionKind(), IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID );
        assertEquals( exp.toString(), "__alignof__(int)"); //$NON-NLS-1$
    }
    
    public void testBug39684() throws Exception
    {
    	IASTFunction bar = (IASTFunction) parse("typeof(foo(1)) bar () { return foo(1); }").getDeclarations().next(); //$NON-NLS-1$
    	
    	IASTSimpleTypeSpecifier simpleTypeSpec = ((IASTSimpleTypeSpecifier)bar.getReturnType().getTypeSpecifier());
		assertEquals( simpleTypeSpec.getType(), IASTGCCSimpleTypeSpecifier.Type.TYPEOF );
    }

    public void testBug39698A() throws Exception
    {
        Iterator i = parse("int c = a <? b;").getDeclarations(); //$NON-NLS-1$
        IASTVariable c = (IASTVariable) i.next();
        IASTExpression exp = c.getInitializerClause().getAssigmentExpression();
        assertEquals( ASTUtil.getExpressionString( exp ), "a <? b" ); //$NON-NLS-1$
    }
    public void testBug39698B() throws Exception
    {
    	Iterator i = parse("int c = a >? b;").getDeclarations(); //$NON-NLS-1$
    	IASTVariable c = (IASTVariable) i.next();
        IASTExpression exp = c.getInitializerClause().getAssigmentExpression();
        assertEquals( ASTUtil.getExpressionString( exp ), "a >? b" ); //$NON-NLS-1$
    }

}
