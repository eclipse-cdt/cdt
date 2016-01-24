/*******************************************************************************
 * Copyright (c) 2008, 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.pdom.dom.IInternalPDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for template parameters stored in the index.
 */
public interface IPDOMCPPTemplateParameter extends IInternalPDOMNode, ICPPTemplateParameter {
	IPDOMCPPTemplateParameter[] EMPTY_ARRAY = {};

	/**
	 * Default values are updated after the class template is stored, because we
	 * may have to refer to the other template parameters.
	 */
	void configure(ICPPTemplateParameter templateParameter);
	
	/**
	 * @see org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding#update(PDOMLinkage, IBinding, IASTNode)
	 */
	void update(PDOMLinkage linkage, IBinding newBinding, IASTNode point) throws CoreException;

	/**
	 * parameters of template template parameters need to be deleted.
	 * @throws CoreException 
	 */
	void forceDelete(PDOMLinkage pdom) throws CoreException;
}
