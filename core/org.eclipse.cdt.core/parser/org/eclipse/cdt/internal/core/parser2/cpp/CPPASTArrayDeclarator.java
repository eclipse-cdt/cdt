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
package org.eclipse.cdt.internal.core.parser2.cpp;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;

/**
 * @author jcamelon
 */
public class CPPASTArrayDeclarator extends CPPASTDeclarator implements
        IASTArrayDeclarator {

    private int currentIndex = 0;
    private void removeNullArrayModifiers() {
        int nullCount = 0; 
        for( int i = 0; i < arrayMods.length; ++i )
            if( arrayMods[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTArrayModifier [] old = arrayMods;
        int newSize = old.length - nullCount;
        arrayMods = new IASTArrayModifier[ newSize ];
        for( int i = 0; i < newSize; ++i )
            arrayMods[i] = old[i];
        currentIndex = newSize;
    }

    
    private IASTArrayModifier [] arrayMods = null;
    private static final int DEFAULT_ARRAYMODS_LIST_SIZE = 4;


    public IASTArrayModifier[] getArrayModifiers() {
        if( arrayMods == null ) return IASTArrayModifier.EMPTY_ARRAY;
        removeNullArrayModifiers();
        return arrayMods;
 
    }

    public void addArrayModifier(IASTArrayModifier arrayModifier) {
        if( arrayMods == null )
        {
            arrayMods = new IASTArrayModifier[ DEFAULT_ARRAYMODS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( arrayMods.length == currentIndex )
        {
            IASTArrayModifier [] old = arrayMods;
            arrayMods = new IASTArrayModifier[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                arrayMods[i] = old[i];
        }
        arrayMods[ currentIndex++ ] = arrayModifier;
    }
}
