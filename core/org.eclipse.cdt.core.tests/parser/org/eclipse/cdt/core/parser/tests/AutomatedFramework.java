/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
 
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

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;

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
					
					if(	filePath.endsWith(".cpp") || filePath.endsWith(".hpp") ||  //$NON-NLS-1$ //$NON-NLS-2$
						filePath.endsWith(".cc") || filePath.endsWith(".CC") || //$NON-NLS-1$ //$NON-NLS-2$
						filePath.endsWith(".C") || //$NON-NLS-1$
						filePath.endsWith(".hxx") || filePath.endsWith(".hh") ) //$NON-NLS-1$ //$NON-NLS-2$
					{
						AutomatedTest.natures.put( filePath, "cpp" ); //$NON-NLS-1$
					} else if( filePath.endsWith(".c") ){  //$NON-NLS-1$
						AutomatedTest.natures.put( filePath, "c" ); //$NON-NLS-1$
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
		fail( "Unable to open " + outputFile + "for output of results." ); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void propertiesFailed() {
		fail( "Unable to load properties file." ); //$NON-NLS-1$
	}
	
	protected void runTest() throws Throwable {
		String name = getName();
		
		if( name.equals("propertiesFailed") ) //$NON-NLS-1$
			propertiesFailed();
		else if ( name.equals("reportFailed") ) //$NON-NLS-1$
			reportFailed();
		else
			doFile();
	}

	public Test createSuite() {
		TestSuite suite = new TestSuite();
		
		try{
			loadProperties();
		} catch( Exception e ){
			suite.addTest( newTest( "propertiesFailed") ); //$NON-NLS-1$
		}
		
		if( outputFile != null && !outputFile.equals("") ){ //$NON-NLS-1$
			try{
				
				File output = new File( outputFile );
				
				if( output.exists() ){
					output.delete();
				}
				
				output.createNewFile();
			
				report = new FileOutputStream( output );
			
			} catch( Exception e ) {
				suite.addTest( newTest( "reportFailed" ) ); //$NON-NLS-1$
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

	protected static ISourceElementRequestor nullCallback = new NullSourceElementRequestor();
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
			if( name.endsWith(".cpp") 	||  //$NON-NLS-1$
				name.endsWith(".c") 	||  //$NON-NLS-1$
				name.endsWith(".cc") 	|| //$NON-NLS-1$
				name.endsWith(".CC") 	|| //$NON-NLS-1$
				name.endsWith(".C") 	||  //$NON-NLS-1$
				name.endsWith(".h") 	|| //$NON-NLS-1$
				name.endsWith(".hh")	|| //$NON-NLS-1$
				name.endsWith(".hpp")	|| //$NON-NLS-1$
				name.endsWith(".hxx")) //$NON-NLS-1$
			{
				return true;
			}
			else
				return false;
		}
	}

}
