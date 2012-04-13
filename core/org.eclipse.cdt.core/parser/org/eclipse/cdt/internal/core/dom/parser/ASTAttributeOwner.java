/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Classes that implement IASTAttributeOwner interface may extend this class.
 */
public abstract class ASTAttributeOwner extends ASTNode implements IASTAttributeOwner {
	private IASTAttribute[] attributes = IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;

	@Override
	public IASTAttribute[] getAttributes() {
	    attributes = ArrayUtil.trim(attributes);
	    return attributes;
	}

	@Override
	public void addAttribute(IASTAttribute attribute) {
	    assertNotFrozen();
		if (attribute != null) {
			attribute.setParent(this);
			attribute.setPropertyInParent(ATTRIBUTE);
			attributes = ArrayUtil.append(attributes, attribute);
		}
	}

	protected <T extends ASTAttributeOwner> T copy(T copy, CopyStyle style) {
		for (IASTAttribute attribute : getAttributes()) {
			copy.addAttribute(attribute.copy(style));
		}
		return super.copy(copy, style);
	}

	protected boolean acceptByAttributes(ASTVisitor action) {
    	for (IASTAttribute attribute : attributes) {
    		if (attribute == null)
    			break;
            if (!attribute.accept(action))
            	return false;
    	}
    	return true;
	}
}