/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Apr 29, 2004
 */
package org.eclipse.cdt.core.parser.tests;

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
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
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
}
