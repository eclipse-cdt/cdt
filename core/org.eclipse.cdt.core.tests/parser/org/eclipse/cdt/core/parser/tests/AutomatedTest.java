/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.core.parser.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.cdt.core.parser.ILineOffsetReconciler;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.Path;



/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AutomatedTest extends AutomatedFramework {

	public AutomatedTest() {
	}
	public AutomatedTest(String name){
		super(name);
	}
	
	public void doFile() throws Throwable {
		assertNotNull( fileList );
		
		File file = null;
		IParser parser = null;
		ILineOffsetReconciler mapping = null; 
		
		try{
			file = (File)fileList.removeFirst();
			FileInputStream stream = new FileInputStream( file );

			String filePath = file.getCanonicalPath();
			ParserLanguage language = ((String)natures.get( filePath )).equalsIgnoreCase("cpp") ? ParserLanguage.CPP : ParserLanguage.C; //$NON-NLS-1$
			parser = ParserFactory.createParser( ParserFactory.createScanner(filePath, new ScannerInfo(), ParserMode.QUICK_PARSE, language, nullCallback, null, null ), nullCallback, ParserMode.QUICK_PARSE, language, null);
						
			mapping = ParserFactory.createLineOffsetReconciler( new InputStreamReader( stream ) );
			
			assertTrue( parser.parse() );
		} 
		catch( Throwable e )
		{
			String output = null;
			if( e instanceof AssertionFailedError ){
				output = file.getCanonicalPath() + ": Parse failed on line "; //$NON-NLS-1$
				output += mapping.getLineNumberForOffset(parser.getLastErrorOffset()) + "\n"; //$NON-NLS-1$
			} else {
				output = file.getCanonicalPath() + ": " + e.getClass().toString(); //$NON-NLS-1$
				output += " on line " + mapping.getLineNumberForOffset(parser.getLastErrorOffset()) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if( report != null ){
				report.write( output.getBytes() );
			}

			fail( output );
		}
	}
	
	protected AutomatedFramework newTest( String name ){
		return new AutomatedTest( name );
	}
	
	public static Test suite()
	{
		AutomatedFramework frame = new AutomatedTest();
		
		return frame.createSuite();
	}

	protected void tearDown () throws Exception	{
		if( fileList != null && fileList.size() == 0 && report != null ){
			report.flush();
			report.close();
		}
	}
	
	protected void loadProperties() throws Exception{
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile(); //$NON-NLS-1$ //$NON-NLS-2$
		resourcePath += "resources/parser/AutomatedTest"; //$NON-NLS-1$
		
		try{
			FileInputStream propertiesIn = new FileInputStream( resourcePath + "/AutomatedTest.properties"); //$NON-NLS-1$
			properties.load( propertiesIn );
			
			outputFile = properties.getProperty( "outputFile", "" ); //$NON-NLS-1$ //$NON-NLS-2$
			String sourceInfo = properties.getProperty( "source", "" ); //$NON-NLS-1$ //$NON-NLS-2$
			if( sourceInfo.equals("") ) //$NON-NLS-1$
				throw new FileNotFoundException();
			else{
				StringTokenizer tokenizer = new StringTokenizer( sourceInfo, "," ); //$NON-NLS-1$
				String str = null, val = null;
				try{
					while( tokenizer.hasMoreTokens() ){
						str = tokenizer.nextToken().trim();
						val = tokenizer.nextToken().trim();
						
						testSources.put( str, val );
					}
				} catch ( NoSuchElementException e ){
					//only way to get here is to have a missing val, assume cpp for that str
					testSources.put( str, "cpp" ); //$NON-NLS-1$
				}
				
			}
		} catch ( FileNotFoundException e ){
			testSources.put( resourcePath + "/defaultCpp", "cpp" ); //$NON-NLS-1$ //$NON-NLS-2$
			testSources.put( resourcePath + "/defaultC", "c" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
