/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CPPASTVisibilityLabel extends ASTNode implements ICPPASTVisibilityLabel {

    private int visibility;


    public CPPASTVisibilityLabel() {
	}

	public CPPASTVisibilityLabel(int visibility) {
		this.visibility = visibility;
	}

	@Override
	public CPPASTVisibilityLabel copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTVisibilityLabel copy(CopyStyle style) {
		CPPASTVisibilityLabel copy = new CPPASTVisibilityLabel(visibility);
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public int getVisibility() {
        return visibility;
    }

    @Override
	public void setVisibility(int visibility) {
        assertNotFrozen();
        this.visibility = visibility;
    }

    @Override
	public boolean accept( ASTVisitor action ){
    	if (action.shouldVisitDeclarations) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }

        if (action.shouldVisitDeclarations) {
            switch (action.leave(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
    }
}
