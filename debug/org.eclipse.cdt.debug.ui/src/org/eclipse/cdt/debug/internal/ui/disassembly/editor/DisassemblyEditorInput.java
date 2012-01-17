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

package org.eclipse.cdt.debug.internal.ui.disassembly.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class DisassemblyEditorInput implements IEditorInput {

    private Object fDebugContext;
    private Object fDisassemblyContext;

    public DisassemblyEditorInput( Object debugContext, Object disassemblyContext ) {
        fDisassemblyContext = disassemblyContext;
        fDebugContext = debugContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    @Override
	public boolean exists() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    @Override
	public ImageDescriptor getImageDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    @Override
	public String getName() {
        // TODO Auto-generated method stub
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    @Override
	public IPersistableElement getPersistable() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    @Override
	public String getToolTipText() {
        // TODO Auto-generated method stub
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
	@SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getDisassemblyContext() {
        return fDisassemblyContext;
    }

    public Object getDebugContext() {
        return fDebugContext;
    }
}
