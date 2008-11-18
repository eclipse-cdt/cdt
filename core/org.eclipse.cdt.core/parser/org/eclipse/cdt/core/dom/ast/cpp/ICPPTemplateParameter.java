/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Base interface for all template parameters (non-type, type and template).
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPTemplateParameter extends ICPPBinding {
	public static final ICPPTemplateParameter[] EMPTY_TEMPLATE_PARAMETER_ARRAY = new ICPPTemplateParameter[0];
	
	/**
	 * The position of the template parameter is determined by the nesting level of the template 
	 * declaration and the offset of this parameter within the template parameter list. 
	 * In every context where a template parameter can be referenced (i.e. within a template declaration) 
	 * the parameter position is unique. 
	 * <p>
	 * The nesting level is determined by counting the template declarations that enclose the one that 
	 * declares this parameter. The position is then computed by 
	 * <code>(nesting-level << 16) + position-in-parameter-list</code>.
	 * <p> 
	 * Example: 
	 * <pre>
	 * namespace ns {
	 *    template<typename T> class X {       // parameter position: 0x00000000
	 *       template<typename U> class Y1 {   // parameter position: 0x00010000
	 *       };
	 *       class Y2 {
	 *          template typename<V> class Z { // parameter position: 0x00010000
	 *             void m();
	 *          };  
	 *       };
	 *    };
	 * }
	 * template<typename T>                    // parameter position 0x00000000
	 *    template <typename V>                // parameter position 0x00010000
	 *       void ns::X<T>::Y2::Z<V>::m() {}
	 * </pre>
	 * @since 5.1
	 */
	int getParameterPosition();

	/**
	 * Returns the default value for this template parameter, or <code>null</code>.
	 * @since 5.1
	 */
	ICPPTemplateArgument getDefaultValue();
}
