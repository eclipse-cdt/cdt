package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
                super(cview.getResourceViewer());
                this.cview = cview;
        }
}

