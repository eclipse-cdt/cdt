/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.vm;

import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProviderTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 2.2
 */
public class JFaceViewerFormattedValueTests extends FormattedValueTests {
    
    public JFaceViewerFormattedValueTests(String name) {
        super(name);
    }

    @Override
    protected ITreeModelContentProviderTarget createViewer(Display display, Shell shell) {
        return new TreeModelViewer(fShell, SWT.VIRTUAL, new PresentationContext("TestViewer"));
    }
    
    @Override
    protected int getTestModelDepth() {
        return 5;
    }
}
