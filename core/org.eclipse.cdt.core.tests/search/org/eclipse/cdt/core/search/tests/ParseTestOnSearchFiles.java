/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.search.tests;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 *
 */
public class ParseTestOnSearchFiles extends TestCase
{
    private String name;
    private String fullPathName;
    /**
     * 
     */
    public ParseTestOnSearchFiles()
    {
        super();
    }
    /**
     * @param name
     */
    public ParseTestOnSearchFiles(String name)
    {
        super(name);        
    }
    
	protected void setUp() throws Exception {	
		name = "resources/search/classDecl.cpp";
		File f = CTestPlugin.getDefault().getFileInPlugin(new Path(name));
		fullPathName = f.getAbsolutePath();
	}
	
	public void testParseOfAndrewsFile() throws Exception
	{
		ISourceElementRequestor requestor = new NullSourceElementRequestor();
		IScanner scanner = ParserFactory.createScanner( fullPathName, new ScannerInfo(), ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, requestor, new NullLogService(), null );
		IParser parser = ParserFactory.createParser( scanner, requestor, ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, null );
		assertTrue( parser.parse() );
	}

}
