/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

/**
 * @since 2.0
 */
public interface IRegisterVMConstants {

    public static final String PROP_DESCRIPTION = "description";  //$NON-NLS-1$

    public static final String PROP_IS_FLOAT = "is_float";  //$NON-NLS-1$

    public static final String PROP_IS_READABLE = "is_readable";  //$NON-NLS-1$

    public static final String PROP_IS_READONCE = "is_readonce";  //$NON-NLS-1$

    public static final String PROP_IS_WRITEABLE = "is_writeable";  //$NON-NLS-1$

    public static final String PROP_IS_WRITEONCE = "is_writeonce";  //$NON-NLS-1$

    public static final String PROP_HAS_SIDE_EFFECTS = "has_side_effects";  //$NON-NLS-1$
    
    public static final String PROP_IS_ZERO_BASED_NUMBERING = "is_zero_based_numbering";  //$NON-NLS-1$
    
    public static final String PROP_IS_ZERO_BIT_LEFT_MOST = "is_zero_bit_left_most";  //$NON-NLS-1$
    
    public static final String PROP_CURRENT_MNEMONIC_LONG_NAME = "mnemonic_long_name";  //$NON-NLS-1$

    public static final String PROP_CURRENT_MNEMONIC_SHORT_NAME = "mnemonic_short_name";  //$NON-NLS-1$
}
