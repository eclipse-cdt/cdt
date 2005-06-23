/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 25, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author aniefer
 */
public class CCompositeTypeScope extends CScope implements ICCompositeTypeScope {
    public CCompositeTypeScope( ICASTCompositeTypeSpecifier compTypeSpec ){
        super( compTypeSpec );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope#getBinding(char[])
     */
    public IBinding getBinding( char[] name ) {
        return super.getBinding( NAMESPACE_TYPE_OTHER, name );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) {
        CollectNamesAction action = new CollectNamesAction( name.toCharArray() );
        getPhysicalNode().accept( action );
        
        IASTName [] names = action.getNames();
        IBinding [] result = null;
        for( int i = 0; i < names.length; i++ ){
            IBinding b = names[i].resolveBinding();
            if( b == null ) continue;
            try {
                if( b.getScope() == this )
                    result = (IBinding[]) ArrayUtil.append( IBinding.class, result, b );
            } catch ( DOMException e ) {
            }
        }
            
        return (IBinding[]) ArrayUtil.trim( IBinding.class, result );
    }
}
