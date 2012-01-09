/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Implementation of field designators
 */
public class CASTFieldDesignator extends ASTNode implements ICASTFieldDesignator, IASTCompletionContext {

    private IASTName name;


    public CASTFieldDesignator() {
	}

	public CASTFieldDesignator(IASTName name) {
		setName(name);
	}
	
	@Override
	public CASTFieldDesignator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTFieldDesignator copy(CopyStyle style) {
		CASTFieldDesignator copy = new CASTFieldDesignator(name == null ? null : name.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTName getName() {
        return name;
    }

    @Override
	public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if (action.shouldVisitDesignators ) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if (name != null && !name.accept(action))
			return false;
		if (action.shouldVisitDesignators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		
        return true;
    }
    
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return CVisitor.findBindingsForContentAssist(n, isPrefix);
	}
}
