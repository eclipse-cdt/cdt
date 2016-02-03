/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents the mapping between goto statements and the label statements
 * the go to.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ILabel extends IBinding {
    /** @since 5.12 */
    public static final ILabel[] EMPTY_ARRAY = {};
    /**
     * @since 5.4
     * @deprecated use {@link #EMPTY_ARRAY} instead
     */
	@Deprecated
    public static final IBinding[] EMPTY_LABEL_ARRAY = {};
	
    /**
	 * Returns the label statement for this label.
	 */
	public IASTLabelStatement getLabelStatement();
}
