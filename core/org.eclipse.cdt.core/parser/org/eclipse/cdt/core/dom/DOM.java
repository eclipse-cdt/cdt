/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom;

/**
 * @author jcamelon
 * 
 * This class serves as the manager of the AST/DOM mechanisms for the CDT.
 * It should be eventually added to CCorePlugin for startup.  
 */
public class DOM {

    public IASTServiceProvider[] getASTFactories() {
        //TODO stub
        return null;
    }
    
    public IASTServiceProvider getDefaultASTFactory() {
        IASTServiceProvider [] factories = getASTFactories();
        if( factories != null && factories.length > 0 )
            return factories[0];
        return null;
    }
    
    public IASTServiceProvider getASTFactoryByName(String name) {
        IASTServiceProvider [] factories = getASTFactories();
        if( factories == null || factories.length == 0 )
            return null;
        for( int i = 0; i < factories.length; ++i )
            if( factories[i] != null && factories[i].getName().equals( name ) )
                return factories[i];
        return null;
    }

    public static final int PARSE_SAVED_RESOURCES = 0; 
    public static final int PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS = 1;
    public static final int PARSE_WORKING_COPY_WHENEVER_POSSIBLE = 2;
    
    public ICodeReaderFactory getCodeReaderFactory( int key )
    {
        switch( key )
        {
        	case PARSE_SAVED_RESOURCES: 
        	    return null; //TODO
        	case PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS:
        	    return null; //TODO
        	case PARSE_WORKING_COPY_WHENEVER_POSSIBLE:
        	    return null; //TODO
        }
        return null;
    }

}
