/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author jcamelon
 *
 */
public class ForewardDeclaredSymbolExtension extends AbstractSymbolExtension
{
    /**
     * @author jcamelon
     *
     */
    private class DualIterator implements Iterator
    {
    	private int state = 0; 
        /**
         * 
         */
        public DualIterator()
        {
            super();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            if( state == 0 ) return true; 
            if( state == 1 && definitionSymbol != null ) return true;
            return false;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next()
        {
            switch( state )
            {
            	case 0: 
            		state = 1; 
            		return primaryDeclaration;
            	case 1: 
            		if( definitionSymbol != null )
            		{
  	            		state = 2; 
    	        		return definitionSymbol;
            		}
            		break;
            }
            throw new NoSuchElementException();
            
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
	protected ASTSymbol definitionSymbol = null; 
    /**
     * @param symbol
     * @param primaryDeclaration
     */
    public ForewardDeclaredSymbolExtension(
        ISymbol symbol,
        ASTSymbol primaryDeclaration)
    {
        super(symbol, primaryDeclaration);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#getAllDefinitions()
     */
    public Iterator getAllDefinitions()
    {
        return new DualIterator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#addDefinition(org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol)
     */
    public void addDefinition(ASTSymbol definition) throws ExtensionException
    {
        if( definitionSymbol != null )
        	throw new ExtensionException();
        definitionSymbol = definition;
    }
}
