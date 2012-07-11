/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Base interface for all template parameters (non-type, type and template).
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPTemplateParameter extends ICPPBinding {
	public static final ICPPTemplateParameter[] EMPTY_TEMPLATE_PARAMETER_ARRAY = {};

	/**
	 * Returns the zero-based position of this parameter within the template parameter list it belongs to. 
	 * @since 5.1
	 */
	short getParameterPosition();

	/**
	 * Returns the nesting-level of the template declaration this parameter belongs to.
	 * <p>
	 * The nesting level is determined by counting enclosing template declarations,
	 * for example:
	 * <pre>
	 * namespace ns {
	 *    template<typename T> class X {       // nesting level 0
	 *       template<typename U> class Y1 {   // nesting level 1
	 *       };
	 *       class Y2 {
	 *          template typename<V> class Z { // nesting level 1
	 *             void m();
	 *          };  
	 *       };
	 *    };
	 * }
	 * template<typename T>                    // nesting level 0
	 *    template <typename V>                // nesting level 1
	 *       void ns::X<T>::Y2::Z<V>::m() {}
	 * </pre>
	 * @since 5.1
	 */
	short getTemplateNestingLevel();
	
	/**
	 * Returns {@code (getTemplateNestingLevel() << 16) + getParameterPosition()}.
	 * @since 5.1
	 */
	int getParameterID();
	
	/**
	 * Returns the default value for this template parameter, or <code>null</code>.
	 * @since 5.1
	 */
	ICPPTemplateArgument getDefaultValue();
	
	/**
	 * Returns whether this template parameter is a parameter pack.
	 * @since 5.2
	 */
	boolean isParameterPack();
}
