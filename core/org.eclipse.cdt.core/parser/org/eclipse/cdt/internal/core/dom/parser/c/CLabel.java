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
 * Created on Nov 17, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class CLabel extends PlatformObject implements ILabel {
    
    public static class CLabelProblem extends ProblemBinding implements ILabel {
        public CLabelProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

        public IASTLabelStatement getLabelStatement() throws DOMException{
            throw new DOMException( this );
        }
    }
    
    private final IASTName labelStatement;
    
    public CLabel( IASTName statement ){
        labelStatement = statement;
        statement.setBinding( this );
    }
    public IASTNode getPhysicalNode(){
        return labelStatement;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ILabel#getLabelStatement()
     */
    public IASTLabelStatement getLabelStatement() {
        return (IASTLabelStatement) labelStatement.getParent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return labelStatement.toString();
    }
    public char[] getNameCharArray(){
        return labelStatement.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CVisitor.getContainingScope( labelStatement.getParent() );
    }

}
