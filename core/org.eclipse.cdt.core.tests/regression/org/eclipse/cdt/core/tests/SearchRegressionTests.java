/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Oct 4, 2004
 */
package org.eclipse.cdt.core.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * @author aniefer
 */
public class SearchRegressionTests extends BaseTestFramework implements ICSearchConstants, IIndexChangeListener{
    static protected ICSearchScope 			scope;
    static protected SearchEngine			searchEngine;
    static protected BasicSearchResultCollector	resultCollector;
    static private boolean indexChanged = false;
    
    {
        scope = SearchEngine.createWorkspaceScope();
		resultCollector = new BasicSearchResultCollector();
		searchEngine = new SearchEngine();
    }
    public SearchRegressionTests()
    {
        super();
    }
    /**
     * @param name
     */
    public SearchRegressionTests(String name)
    {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
		try{
		    project.setSessionProperty( IndexManager.activationKey, new Boolean( true ) );
		} catch ( CoreException e ) { //boo
		}
        IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
        indexManager.addIndexChangeListener( this );
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
    
        IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
        indexManager.removeIndexChangeListener( this );
		try{
		    project.setSessionProperty( IndexManager.activationKey, new Boolean( false ) );
		} catch ( CoreException e ) { //boo
		}
        super.tearDown();
	}
    
    protected Set search( ICSearchPattern pattern ) {
		try {
			searchEngine.search( workspace, pattern, scope, resultCollector, false );
		} catch (InterruptedException e) {
		    //boo
		}
		
		return resultCollector.getSearchResults();
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.index.IIndexChangeListener#indexChanged(org.eclipse.cdt.core.index.IndexChangeEvent)
     */
    public void indexChanged( IndexChangeEvent event ) {
        indexChanged = true;
    }
    
    protected IFile importFile(String fileName, String contents ) throws Exception{
        indexChanged = false;
        IFile file = super.importFile( fileName, contents );
	
        int loops = 0;
        while( !indexChanged && loops++ < 20){
            Thread.sleep( 100 );
        }
        if( loops >= 20 )
            fail("Timeout waiting for file \"" + fileName + "\" to index." );  //$NON-NLS-1$//$NON-NLS-2$
        
		return file;
	}
    
    public void assertMatch( Set matches, IFile file, int offset ) throws Exception {
        Iterator i = matches.iterator();
        while( i.hasNext() ){
            IMatch match = (IMatch) i.next();
            if( match.getStartOffset() == offset && match.getLocation().equals( file.getLocation() ) )
                return; //match
        }
        fail( "Match at offset " + offset + " in \"" + file.getLocation() + "\" not found." );    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
    }
 
    public static Test suite(){
        return suite( true );
    }
    public static Test suite( boolean cleanup ) {
        TestSuite suite = new TestSuite("SearchRegressionTests"); //$NON-NLS-1$
        suite.addTest( new SearchRegressionTests("testClassDeclarationReference") ); //$NON-NLS-1$
        
        if( cleanup )
            suite.addTest( new SearchRegressionTests( "cleanupProject" ) ); //$NON-NLS-1$
        
	    return suite;
    }
    
    public void testClassDeclarationReference() throws Exception {
        Writer writer = new StringWriter();
        writer.write(" class A {               \n" ); //$NON-NLS-1$
        writer.write("    int foo();           \n" ); //$NON-NLS-1$
        writer.write(" };                      \n" ); //$NON-NLS-1$
        writer.write(" int A::foo() {          \n" ); //$NON-NLS-1$
        writer.write(" }                       \n" ); //$NON-NLS-1$
        
        String code = writer.toString();
        IFile f = importFile( "f.cpp", code ); //$NON-NLS-1$
        
		ICSearchPattern pattern = SearchEngine.createSearchPattern( "A", CLASS, ALL_OCCURRENCES, true ); //$NON-NLS-1$
		Set matches = search( pattern );
		
		assertEquals( matches.size(), 2 );
		assertMatch( matches, f, code.indexOf( "A {" )  ); //$NON-NLS-1$ //$NON-NLS-2$
		assertMatch( matches, f, code.indexOf( "A::" ) ); //$NON-NLS-1$ //$NON-NLS-2$
   	}


}
