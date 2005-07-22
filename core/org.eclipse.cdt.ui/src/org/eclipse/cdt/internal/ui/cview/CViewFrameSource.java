/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

public class CViewFrameSource extends TreeViewerFrameSource {
        private CView cview;

        protected TreeFrame createFrame(Object input) {
                TreeFrame frame = super.createFrame(input);
                frame.setToolTipText(cview.getToolTipText(input));
                return frame;
        }
        /**
         * Also updates the title of the packages explorer
         */
        protected void frameChanged(TreeFrame frame) {
                super.frameChanged(frame);
                cview.updateTitle();
        }
        public CViewFrameSource(CView cview) {
                super(cview.getViewer());
                this.cview = cview;
        }
}

