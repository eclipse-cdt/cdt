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
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.InternalASTServiceProvider;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;

/**
 * @author jcamelon
 * 
 * This class serves as the manager of the AST/DOM mechanisms for the CDT.
 * It should be eventually added to CCorePlugin for startup.  
 */
public class CDOM {
    
    

    private CDOM() 
    {
    }
    
    private static CDOM instance = new CDOM();
    public static CDOM getInstance()
    {
        return instance;
    }
    private IASTServiceProvider [] services = { new InternalASTServiceProvider() };    

    public IASTServiceProvider[] getASTServices() {
        return services;
    }
    
    public IASTServiceProvider getDefaultASTService() {
        IASTServiceProvider [] factories = getASTServices();
        if( factories != null && factories.length > 0 )
            return factories[0];
        return null;
    }
    
    public IASTServiceProvider getASTServiceByName(String name) {
        IASTServiceProvider [] factories = getASTServices();
        if( factories == null || factories.length == 0 )
            return null;
        for( int i = 0; i < factories.length; ++i )
            if( factories[i] != null && factories[i].getName().equals( name ) )
                return factories[i];
        return null;
    }
    
    public IASTServiceProvider getASTServiceByDialect( String dialect )
    {
        IASTServiceProvider [] factories = getASTServices();
        if( factories == null || factories.length == 0 )
            return null;
        for( int i = 0; i < factories.length; ++i )
            if( factories[i] != null )
            {
                String [] dialects = factories[i].getSupportedDialects();
                if( dialects != null )
                    for( int j = 0; j < dialects.length; ++j )
                        if( dialects[j].equals( dialect ))
                            return factories[i];
            }
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
        	    return SavedCodeReaderFactory.getInstance();
        	case PARSE_WORKING_COPY_WITH_SAVED_INCLUSIONS:
        	    return null; //TODO
        	case PARSE_WORKING_COPY_WHENEVER_POSSIBLE:
        	    return null; //TODO
        }
        return null;
    }

}
