/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui;

/**
 * This interface extension allows the registration of content listeners.
 * Page implementors can use it to notify parents of changes in 
 * the page content which will force the parent tab to recalculate its size.
 * 
 * @since 7.0
 */
public interface ICDebuggerPageExtension extends ICDebuggerPage {

    /**
     * @since 7.0
     */
    public interface IContentChangeListener {

        void contentChanged();
    }

    /**
     * Adds a listener to this page. This method has no effect 
     * if the same listener is already registered.
     */
    void addContentChangeListener( IContentChangeListener listener );

    /**
     * Removes a listener from this list. Has no effect if 
     * the same listener was not already registered.
     */
    void removeContentChangeListener( IContentChangeListener listener );
}
