/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public class ExtendedTreeViewer extends TreeViewer {

    private boolean fPreservingSelection= false;

    public ExtendedTreeViewer(Composite parent) {
        super(parent);
    }
    
    public void refresh(final Object[] elements) {
        preservingSelection(new Runnable() {
            public void run() {
                for (int i = 0; i < elements.length; i++) {
                    refresh(elements[i]);
                }
            }
        });
    }

    protected void preservingSelection(Runnable updateCode) {
        if (fPreservingSelection) {
            updateCode.run();
        }
        else {
            fPreservingSelection= true;
            try {
                super.preservingSelection(updateCode);
            }
            finally {
                fPreservingSelection= false;
            }
        }
    }
}
