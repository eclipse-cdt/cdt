/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;


/**
 * Models the mapping of template parameters to values.
 * @since 5.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPTemplateParameterMap {

	/**
	 * Returns the value for the template parameter with the given id.
	 * @see ICPPTemplateParameter#getParameterID()
	 */
	public ICPPTemplateArgument getArgument(int paramID);

	/**
	 * Returns the value for the template parameter in the map, or <code>null</code> if 
	 * the parameter is not mapped.
	 */
	public ICPPTemplateArgument getArgument(ICPPTemplateParameter param);

	/**
	 * Returns the array of template parameter positions, for which a mapping exists.
	 */
	Integer[] getAllParameterPositions();
}
