/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
public class StandardSymbolExtension extends AbstractSymbolExtension
{

    /**
     * @author jcamelon
     *
     */
    private class SimpleIterator implements Iterator
    {
    	boolean hasNext = true;
        /**
         * 
         */
        public SimpleIterator()
        {
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return hasNext;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next()
        {
        	if( hasNext )
        	{
        		hasNext = false;
        		return primaryDeclaration;
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
    /**
     * @param symbol
     * @param primaryDeclaration
     */
    public StandardSymbolExtension(ISymbol symbol, ASTSymbol primaryDeclaration)
    {
        super(symbol, primaryDeclaration);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#getAllDefinitions()
     */
    public Iterator getAllDefinitions()
    {
        return this.new SimpleIterator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension#addDefinition(org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol)
     */
    public void addDefinition(ASTSymbol definition) throws ExtensionException
    {
		throw new ExtensionException();
    }

}
