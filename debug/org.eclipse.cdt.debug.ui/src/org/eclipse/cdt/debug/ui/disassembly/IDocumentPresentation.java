/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.disassembly;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;

/**
 * Virtual document specific presentation context.
 * <p>
 * Clients may implement and extend this interface to provide
 * special contexts. Implementations must subclass {@link PresentationContext}.
 * </p>
 * 
 * This interface is experimental.
 */
public interface IDocumentPresentation extends IPresentationContext {

    /**
     * Temporary attribute for testing.
     */
    public static final String ATTR_LINE_LABEL = "lineLabel"; //$NON-NLS-1$

}
