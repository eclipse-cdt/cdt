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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.parser.IParserCallback;
import org.eclipse.cdt.internal.core.parser.NullParserCallback;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AutomatedFramework extends TestCase {

	public AutomatedFramework() {
		super();
	}

	public AutomatedFramework(String name) {
		super(name);
	}

	protected abstract AutomatedFramework newTest( String name );
	protected abstract void loadProperties() throws Exception;
	public	  abstract void doFile() throws Throwable;

	private void fillSuite( TestSuite suite, File path ){
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
						filePath.endsWith(".cc") || filePath.endsWith(".CC") ||
						filePath.endsWith(".C") ||
						filePath.endsWith(".hxx") || filePath.endsWith(".hh") )
					{
						AutomatedTest.natures.put( filePath, "cpp" );
					} else if( filePath.endsWith(".c") ){ 
						AutomatedTest.natures.put( filePath, "c" );
					} else {
						AutomatedTest.natures.put( filePath, AutomatedTest.defaultNature );
					}
					
					AutomatedTest.fileList.add( file );
					suite.addTest( newTest( file.getName().replace(',', '_') ) );
				}				
				file = files[ i++ ];
			}
		} catch( ArrayIndexOutOfBoundsException e ){
			//done
		}
	}

	public void reportFailed() {
		fail( "Unable to open " + outputFile + "for output of results." );
	}

	public void propertiesFailed() {
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

	public Test createSuite() {
		TestSuite suite = new TestSuite();
		
		try{
			loadProperties();
		} catch( Exception e ){
			suite.addTest( newTest( "propertiesFailed") );
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
				suite.addTest( newTest( "reportFailed" ) );
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

	protected static IParserCallback nullCallback = new NullParserCallback();
	protected static Properties properties = new Properties();
	protected static String defaultNature;
	protected static String outputFile = null;
	protected static HashMap testSources = new HashMap();
	protected static HashMap natures = new HashMap();
	protected static LinkedList fileList = new LinkedList();
	private static FilenameFilter nameFilter = new Filter();
	protected static FileOutputStream report = null;
	
	static private class Filter implements FilenameFilter
	{
		public boolean accept(File dir, String name) {
			if( name.endsWith(".cpp") 	|| 
				name.endsWith(".c") 	|| 
				name.endsWith(".cc") 	||
				name.endsWith(".CC") 	||
				name.endsWith(".C") 	|| 
				name.endsWith(".h") 	||
				name.endsWith(".hh")	||
				name.endsWith(".hpp")	||
				name.endsWith(".hxx"))
			{
				return true;
			}
			else
				return false;
		}
	}

}
