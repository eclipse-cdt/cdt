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
import java.io.IOException;
import java.io.StringWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import junit.framework.Test;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
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
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile(); //$NON-NLS-1$ //$NON-NLS-2$
		resourcePath += "resources/parser/AutomatedTest"; //$NON-NLS-1$
	
		try{
			FileInputStream propertiesIn = new FileInputStream( resourcePath + "/FractionalAutomatedTest.properties"); //$NON-NLS-1$
			properties.load( propertiesIn );
		
			outputFile = properties.getProperty( "outputFile", "" ); //$NON-NLS-1$ //$NON-NLS-2$
			String sourceInfo = properties.getProperty( "source", "" ); //$NON-NLS-1$ //$NON-NLS-2$

			stepSize = Integer.parseInt( properties.getProperty( "stepSize", "50" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			windowSize = Integer.parseInt( properties.getProperty( "windowSize", "200" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			timeOut = Integer.parseInt( properties.getProperty( "timeOut", "5000" )); //$NON-NLS-1$ //$NON-NLS-2$
			outputDir = properties.getProperty( "outDir", "" ); //$NON-NLS-1$ //$NON-NLS-2$
			
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
	
	public static Test suite()
	{
		AutomatedFramework frame = new FractionalAutomatedTest();
		
		return frame.createSuite();
	}
	
	static private String outputFile( String code ) {
		if( outputDir == null || outputDir.equals("") ) //$NON-NLS-1$
			return ""; //$NON-NLS-1$
			
		File output = new File( outputDir );
				
		try{
			if( output.exists() ){
				if( output.isFile() ){
					output.delete();
					output.createNewFile();
					FileOutputStream stream = new FileOutputStream( output );
					stream.write( code.getBytes() );
					stream.flush();
					stream.close();
					return outputDir;
				}
			} else {
				output.mkdir();
			}
			File file = new File( outputDir + "/" + failures++ + ".tmp" ); //$NON-NLS-1$ //$NON-NLS-2$
			if( file.exists() )
				file.delete();
			file.createNewFile();
			FileOutputStream stream = new FileOutputStream( file );
			stream.write( code.getBytes() );
			stream.flush();
			stream.close();
			
			return file.getCanonicalPath();
			
		} catch ( Exception e )
		{}
		return ""; //$NON-NLS-1$
	}
	
	static public void reportHang( String code, String file ){
		String output = outputFile( code.toString() );
		if( output.equals("") ) //$NON-NLS-1$
			output = "Parser hang while parsing " + file + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		else 
			output = "Parser hang while parsing " + output + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	 
		if( report != null ){
			try{
				report.write( output.getBytes() );
			} catch ( IOException e ) {}
		}

		fail( output );
	}
	
	static public void reportException( String code, String file, String exception ){
		String output = outputFile( code.toString() );

		if( output.equals("") ) //$NON-NLS-1$
			output = exception.getClass().toString() + " encountered in " + file + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		else 
			output = exception.getClass().toString() + " encountered in " + output + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
	 
		if( report != null ){
			try{
				report.write( output.getBytes() );
			} catch ( IOException e ) {}
		}
		
		fail( output );
	}
	
	public void doFile() throws Throwable {
		assertNotNull( fileList );
		
		File file = (File)fileList.removeFirst();
		FileInputStream stream = new FileInputStream( file );

		String filePath = file.getCanonicalPath();
		String nature = (String)natures.get( filePath );

		boolean cppNature = nature.equalsIgnoreCase("cpp"); //$NON-NLS-1$
		
		StringWriter code = new StringWriter(); 
		
		ParseThread thread = new ParseThread();
		
		byte b[] = new byte[stepSize]; 
		int n = stream.read( b );
		while( n != -1 ){
			code.write( new String( b ) );

			thread.code = code.toString();
			thread.cppNature = cppNature;
			thread.start();
			thread.join( timeOut );
			
			if( thread.isAlive() ){
				//Use deprecated Thread.stop() for now
				//alternative is to create a callback which could stop the parse on a flag
				//by throwing something, but that has the disadvantage of being unable to 
				//stop any loops that don't involve callbacks.
				thread.stop();
				reportHang( code.toString(), filePath );
			} else if( thread.result != null ) {
				reportException( code.toString(), filePath, thread.result );
			}
			
			n = stream.read( b );
		}
		
		String fullCode = code.toString();
		String windowedCode = null;
		int length = fullCode.length();
		int curPos = 0;
		
		while( curPos + windowSize < length){
			windowedCode = fullCode.substring( 0, curPos );
			windowedCode += "\n" + fullCode.substring( curPos + windowSize, length ); //$NON-NLS-1$
			
			thread.code = windowedCode;
			thread.cppNature = cppNature;
			thread.file = filePath;
			thread.start();
			thread.join( timeOut );

			if( thread.isAlive() )
			{
				thread.stop();
				reportHang( windowedCode, filePath );	
			} else if( thread.result != null ) {
				reportException( windowedCode, filePath, thread.result );
			}

			curPos += stepSize;
		}
	}

	static class ParseThread extends Thread{
		public String code;
		public boolean cppNature;
		public String file;
		public String result;
		
		public void run(){
			try{
				result = null;
				ParserLanguage language = cppNature ? ParserLanguage.CPP : ParserLanguage.C;
				IParser parser = ParserFactory.createParser( 
					ParserFactory.createScanner( new CodeReader( code.toCharArray() ), new ScannerInfo(), ParserMode.QUICK_PARSE, language, nullCallback,  new NullLogService(), null ), nullCallback, ParserMode.QUICK_PARSE, language, null );
				
				parser.parse();
			} catch ( Exception e ){
				result = e.getClass().toString();
			}
		}
	}
	
	static protected int stepSize = 50;
	static protected int windowSize = 200;
	static protected int timeOut = 5000;
	static protected String outputDir = null;
	static protected int failures = 0;
}
