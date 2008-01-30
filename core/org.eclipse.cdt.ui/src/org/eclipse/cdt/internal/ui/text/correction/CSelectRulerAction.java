/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Perryman (IPL Information Processing Limited)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class CSelectRulerAction extends AbstractRulerActionDelegate {

    /*
     * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
     */
    protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
        return new CSelectAnnotationRulerAction(CEditorMessages.getResourceBundle(), "CSelectAnnotationRulerAction.", editor, rulerInfo); //$NON-NLS-1$
    }
}
