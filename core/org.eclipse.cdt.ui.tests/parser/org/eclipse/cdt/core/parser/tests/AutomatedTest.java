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
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.internal.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.IParserCallback;
import org.eclipse.cdt.internal.core.parser.NullParserCallback;
import org.eclipse.cdt.internal.core.parser.Parser;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AutomatedTest extends TestCase {
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
	
	public void reportFailed(){
		fail( "Unable to open " + outputFile + "for output of results." );
	}
	
	public void propertiesFailed(){
		fail( "Unable to load properties file." );
	}
	
	protected void runTest() throws Throwable {
		String name = getName();
		
		if( name.equals("propertiesFailed") )
			propertiesFailed();
		else if ( name.equals("reportFailed") )
			reportFailed();
		else
			doFile();
	}
		
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		
		try{
			loadProperties();
		} catch( Exception e ){
			suite.addTest( new AutomatedTest( "propertiesFailed") );
		}
		
		if( outputFile != null && !outputFile.equals("") ){
			try{
				
				File output = new File( outputFile );
				
				if( output.exists() ){
					output.delete();
				}
				
				output.createNewFile();
			
				report = new FileOutputStream( output );
			
			} catch( Exception e ) {
				suite.addTest( new AutomatedTest( "reportFailed" ) );
			}
		}
		
		Set keys = testSources.keySet();
		Iterator iter = keys.iterator();
		int size = keys.size();
		String item = null;
		for( int i = size; i > 0; i-- )
		{
			item = (String) iter.next();
			File file = new File( item );
			if( file.exists() ){
				defaultNature = (String) testSources.get( item );
				fillSuite( suite, file );		
			}
		}

		return suite;
	}
	private static void fillSuite( TestSuite suite, File path ){
		File files[] = null;
		if( path.isFile() ){
			files = new File[ 1 ];
			files[0] = path;
		}
		else
			files = path.listFiles();

		File file = null;
		String filePath = null;
		int i = 0;
		try{
			file = files[ i++ ];
			while( file != null )
			{
				if( file.isDirectory() )
					fillSuite( suite, file );
				else if( file.isFile() && nameFilter.accept( file.getParentFile(), file.getName() ) ){
					try{
						filePath = file.getCanonicalPath();
					} catch ( Exception e ){
						continue;
					}
					
					if(	filePath.endsWith(".cpp") || filePath.endsWith(".hpp") || 
						filePath.endsWith(".hxx") || filePath.endsWith(".hh") )
					{
						natures.put( filePath, "cpp" );
					} else if( filePath.endsWith(".c") ){ 
						natures.put( filePath, "c" );
					} else {
						natures.put( filePath, defaultNature );
					}
					
					fileList.add( file );
					suite.addTest( new AutomatedTest( file.getName() ) );
				}				
				file = files[ i++ ];
			}
		} catch( ArrayIndexOutOfBoundsException e ){
			//done
		}
	}
	
	protected void tearDown () throws Exception	{
		if( fileList != null && fileList.size() == 0 && report != null ){
			report.flush();
			report.close();
		}
	}
	
	static private void loadProperties() throws Exception{
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
	
	private static LinkedList fileList = new LinkedList();
	private static FilenameFilter nameFilter = new Filter();
	private static FileOutputStream report = null;
	private static IParserCallback nullCallback = new NullParserCallback();
	private static Properties properties = new Properties();
	private static String defaultNature;
	private static String outputFile = null;
	private static HashMap testSources = new HashMap();
	private static HashMap natures = new HashMap();
	
	static private class Filter implements FilenameFilter
	{
		public boolean accept(File dir, String name) {
			if( name.endsWith(".cpp") 	|| 
				name.endsWith(".c") 	|| 
				name.endsWith(".cc") 	|| 
				name.endsWith(".h") 	||
				name.endsWith(".hh")	||
				name.endsWith(".hxx"))
			{
				return true;
			}
			else
				return false;
		}
	}
}
