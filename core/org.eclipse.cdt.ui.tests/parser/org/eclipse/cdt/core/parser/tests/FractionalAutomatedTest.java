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
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.cdt.internal.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.core.runtime.Path;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FractionalAutomatedTest extends AutomatedFramework {

	public FractionalAutomatedTest() {
		super();
	}

	public FractionalAutomatedTest(String name) {
		super(name);
	}
	
	protected AutomatedFramework newTest( String name ){
		return new FractionalAutomatedTest( name );
	}
	protected void loadProperties() throws Exception{
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
		resourcePath += "/parser/org/eclipse/cdt/core/parser/resources";
	
		try{
			FileInputStream propertiesIn = new FileInputStream( resourcePath + "/FractionalAutomatedTest.properties");
			properties.load( propertiesIn );
		
			outputFile = properties.getProperty( "outputFile", "" );
			String sourceInfo = properties.getProperty( "source", "" );

			stepSize = Integer.parseInt( properties.getProperty( "stepSize", "50" ) );
			windowSize = Integer.parseInt( properties.getProperty( "windowSize", "200" ) );
			
			tempFile = properties.getProperty( "tempFile", "" );
			
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
	
	public static Test suite()
	{
		AutomatedFramework frame = new FractionalAutomatedTest();
		
		return frame.createSuite();
	}
	
	static private void outputTempFile( String code ) {
		if( tempFile == null || tempFile.equals("") )
			return;
			
		File output = new File( tempFile );
				
		try{
			if( output.exists() ){
				output.delete();
			}
			
			output.createNewFile();
			FileOutputStream stream = new FileOutputStream( output );
			stream.write( code.getBytes() );
			stream.flush();
			stream.close();
		} catch ( Exception e )
		{}
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
			
			StringWriter code = new StringWriter(); 
			
			byte b[] = new byte[stepSize]; 
			int n = stream.read( b );
			while( n != -1 ){
				code.write( new String( b ) );
				parser = new Parser( code.toString(), nullCallback, true);
				parser.setCppNature( cppNature );
				parser.mapLineNumbers(true);
			
				outputTempFile( code.toString() );
				parser.parse();
				
				n = stream.read( b );
			}
			
			String fullCode = code.toString();
			String windowedCode = null;
			int length = fullCode.length();
			int curPos = 0;
			
			while( curPos + windowSize < length){
				windowedCode = fullCode.substring( 0, curPos );
				windowedCode += "\n" + fullCode.substring( curPos + windowSize, length );
				
				parser = new Parser( windowedCode, nullCallback, true );
				parser.setCppNature( cppNature );
				parser.mapLineNumbers(true);

				outputTempFile( windowedCode );
				parser.parse();
				
				curPos += stepSize;
			}
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

	static private int stepSize = 50;
	static private int windowSize = 200;
	static private String tempFile = null;
}
