/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Randy Rohrbach (Wind River Systems, Inc.) - extended implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Common function for actions that operate on a text viewer.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.0
 */
public class TextViewerAction extends Action implements IUpdate {

    private int fOperationCode= -1;
    private ITextOperationTarget fOperationTarget;

    /**
     * Constructs a new action in the given text viewer with
     * the specified operation code.
     * 
     * @param viewer
     * @param operationCode
     */
    public TextViewerAction(ITextViewer viewer, int operationCode) {
        fOperationCode= operationCode;
        fOperationTarget= viewer.getTextOperationTarget();
        update();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     * 
     * Updates the enabled state of the action.
     * Fires a property change if the enabled state changes.
     * 
     * @see org.eclipse.jface.action.Action#firePropertyChange(String, Object, Object)
     */
    public void update() {

        boolean wasEnabled= isEnabled();
        boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
        setEnabled(isEnabled);

        if (wasEnabled != isEnabled) {
            firePropertyChange(ENABLED, wasEnabled ? Boolean.TRUE : Boolean.FALSE, isEnabled ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run() 
     */
    @Override
    public void run() {
        if (fOperationCode != -1 && fOperationTarget != null) {
            fOperationTarget.doOperation(fOperationCode);
        }
    }
    
    /**
     * Configures this action with a label, tool tip, and description.
     * 
     * @param text action label
     * @param toolTipText action tool tip
     * @param description action description
     */
    public void configureAction(String text, String toolTipText, String description) {
        setText(text);
        setToolTipText(toolTipText);
        setDescription(description);
    }
}

