/*******************************************************************************
 * Copyright (c) 2008, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IInternalPDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for template parameters stored in the index.
 */
public interface IPDOMCPPTemplateParameter extends IInternalPDOMNode, ICPPTemplateParameter, ICPPUnknownBinding {
	IPDOMCPPTemplateParameter[] EMPTY_ARRAY = {};

	/**
	 * Default values are updated after the class template is stored, because we
	 * may have to refer to the other template parameters.
	 */
	void configure(ICPPTemplateParameter templateParameter);

	/**
	 * @see org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding#update(PDOMLinkage, IBinding)
	 */
	void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException;

	/**
	 * parameters of template template parameters need to be deleted.
	 * @throws CoreException
	 */
	void forceDelete(PDOMLinkage pdom) throws CoreException;
}
