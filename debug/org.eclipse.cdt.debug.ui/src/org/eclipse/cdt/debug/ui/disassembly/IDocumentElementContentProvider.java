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

/**
 * Provides a content for a virtual source viewer.
 * 
 * This interface is experimental.
 */
public interface IDocumentElementContentProvider {

    /**
     * Updates the base element of the source viewer.
     * This method is called when the viewer's input is changed.
     *
     * @param update the new input.
     */
    public void updateInput( IDocumentBaseChangeUpdate update );

    /**
     * Updates the source content as requested by the given update.
     * This method is called when the viewer requires to update it's content.
     * 
     * @param update specifies the lines to update and stores result
     */ 
    public void updateContent( IDocumentElementContentUpdate update );
}
