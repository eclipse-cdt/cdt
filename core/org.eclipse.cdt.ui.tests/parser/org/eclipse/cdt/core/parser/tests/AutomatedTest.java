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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.internal.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.Parser;

import junit.framework.AssertionFailedError;
import junit.framework.Test;



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
		
		try{
			file = (File)fileList.removeFirst();
			FileInputStream stream = new FileInputStream( file );

			String filePath = file.getCanonicalPath();
			String nature = (String)natures.get( filePath );
			
			boolean cppNature = nature.equalsIgnoreCase("cpp");
			
			parser = new Parser( stream, nullCallback, true);
			parser.setCppNature( cppNature );
			parser.mapLineNumbers(true);
			
			assertTrue( parser.parse() );
		} 
		catch( Throwable e )
		{
			String output = null;
			if( e instanceof AssertionFailedError ){
				output = file.getCanonicalPath() + ": Parse failed on line ";
				output += parser.getLineNumberForOffset(parser.getLastErrorOffset()) + "\n";
			} else {
				output = file.getCanonicalPath() + ": " + e.getClass().toString();
				output += " on line " + parser.getLineNumberForOffset(parser.getLastErrorOffset()) + "\n";
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
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
		resourcePath += "/parser/org/eclipse/cdt/core/parser/resources";
		
		try{
			FileInputStream propertiesIn = new FileInputStream( resourcePath + "/AutomatedTest.properties");
			properties.load( propertiesIn );
			
			outputFile = properties.getProperty( "outputFile", "" );
			String sourceInfo = properties.getProperty( "source", "" );
			if( sourceInfo.equals("") )
				throw new FileNotFoundException();
			else{
				StringTokenizer tokenizer = new StringTokenizer( sourceInfo, "," );
				String str = null, val = null;
				try{
					while( tokenizer.hasMoreTokens() ){
						str = tokenizer.nextToken().trim();
						val = tokenizer.nextToken().trim();
						
						testSources.put( str, val );
					}
				} catch ( NoSuchElementException e ){
					//only way to get here is to have a missing val, assume cpp for that str
					testSources.put( str, "cpp" );
				}
				
			}
		} catch ( FileNotFoundException e ){
			testSources.put( resourcePath + "/cppFiles", "cpp" );
			testSources.put( resourcePath + "/cFiles", "c" );
		}
	}

}
