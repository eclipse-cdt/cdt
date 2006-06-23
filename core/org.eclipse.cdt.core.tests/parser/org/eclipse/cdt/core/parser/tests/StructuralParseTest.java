/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Apr 29, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.StructuralParseCallback;

/**
 * @author aniefer
 */
public class StructuralParseTest extends TestCase {
	
    public StructuralParseTest()
    {
        super();
    }
    
    protected StructuralParseCallback callback;
    
    protected IASTCompilationUnit parse( String code ) throws ParserException, ParserFactoryError
    {
    	return parse( code, true, ParserLanguage.CPP );
    }
    
    protected IASTCompilationUnit parse( String code, boolean throwOnError ) throws ParserException, ParserFactoryError
    {
    	return parse( code, throwOnError, ParserLanguage.CPP );
    }
    
    protected IASTCompilationUnit parse(String code, boolean throwOnError, ParserLanguage language) throws ParserException, ParserFactoryError
    {
    	callback = new StructuralParseCallback(); 
    	IParser parser = ParserFactory.createParser( 
    		ParserFactory.createScanner( new CodeReader( code.toCharArray() ), new ScannerInfo(), //$NON-NLS-1$
    			                         ParserMode.STRUCTURAL_PARSE, language, callback, new NullLogService(), null ), 
			callback, ParserMode.STRUCTURAL_PARSE, language, null 	
    	);
    	if( ! parser.parse() && throwOnError ) throw new ParserException( "FAILURE"); //$NON-NLS-1$
        return callback.getCompilationUnit();
    }
    
    public void testBug60149() throws Exception
	{
    	IASTCompilationUnit cu = parse( "extern \"C\" { int v; } " ); //$NON-NLS-1$
    	
    	Iterator i = cu.getDeclarations();
    	
    	IASTLinkageSpecification ls = (IASTLinkageSpecification) i.next();
    	assertFalse( i.hasNext() );
    	
    	i = ls.getDeclarations();
    	IASTVariable v = (IASTVariable) i.next();
    	assertEquals( v.getName(), "v" ); //$NON-NLS-1$
    	assertFalse( i.hasNext() );
	}
    
    public void testBug60480() throws Exception
	{
    	IASTCompilationUnit cu = parse( "template < int > void foo();" ); //$NON-NLS-1$
    	Iterator i = cu.getDeclarations();
    	
    	IASTTemplateDeclaration template = (IASTTemplateDeclaration) i.next();
    	assertFalse( i.hasNext() );
    	
    	IASTFunction foo = (IASTFunction) template.getOwnedDeclaration();
    	assertEquals( foo.getName(), "foo" ); //$NON-NLS-1$
	}
    
    public void testBug77010() throws Exception
    {
        Writer writer = new StringWriter();
        writer.write(" struct Example{                                \n"); //$NON-NLS-1$
        writer.write("    int                *deref();                \n"); //$NON-NLS-1$
        writer.write("    int const          *deref() const;          \n"); //$NON-NLS-1$
        writer.write("    int       volatile *deref()       volatile; \n"); //$NON-NLS-1$
        writer.write("    int const volatile *deref() const volatile; \n"); //$NON-NLS-1$
        writer.write(" };                                             \n"); //$NON-NLS-1$
        
        Iterator i = parse( writer.toString() ).getDeclarations();
        IASTClassSpecifier Ex = (IASTClassSpecifier) ((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier();
        
        i = Ex.getDeclarations();
        IASTMethod deref = (IASTMethod) i.next();
        assertFalse( deref.getReturnType().isConst() );
        assertFalse( deref.getReturnType().isVolatile() );
        assertFalse( deref.isConst() );
        assertFalse( deref.isVolatile() );
        
        deref = (IASTMethod) i.next();
        assertTrue( deref.getReturnType().isConst() );
        assertFalse( deref.getReturnType().isVolatile() );
        assertTrue( deref.isConst() );
        assertFalse( deref.isVolatile() );
        
        deref = (IASTMethod) i.next();
        assertFalse( deref.getReturnType().isConst() );
        assertTrue( deref.getReturnType().isVolatile() );
        assertFalse( deref.isConst() );
        assertTrue( deref.isVolatile() );
        
        deref = (IASTMethod) i.next();
        assertTrue( deref.getReturnType().isConst() );
        assertTrue( deref.getReturnType().isVolatile() );
        assertTrue( deref.isConst() );
        assertTrue( deref.isVolatile() );
        assertFalse( i.hasNext() );
    }
}
