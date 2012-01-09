/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.util;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;

import org.eclipse.cdt.internal.core.PositionTracker;

/**
 * A simple general purpose position tracker.
 *
 * @since 4.0
 */
public class SimplePositionTracker extends PositionTracker implements
		IPositionUpdater {

	private IDocument fDocument;

	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void update(DocumentEvent event) {
        String text = event.getText();
        int insertLen = text != null ? text.length() : 0;
        update(event.getOffset(), event.getLength(), insertLen);
    }

    private void update(int offset, int deleteLen, int insertLen) {
        if (insertLen > deleteLen) {
            insert(offset + deleteLen, insertLen - deleteLen);
        } else if (insertLen < deleteLen) {
            delete(offset+insertLen, deleteLen - insertLen);
        }
    }

    /**
     * Start tracking on the given document.
     * 
     * @param doc
     */
    public synchronized void startTracking(IDocument doc) {
        stopTracking();
        fDocument= doc;
        if (fDocument != null) {
        	fDocument.addPositionUpdater(this);
        }
    }

    /**
     * Stop tracking.
     */
    public synchronized void stopTracking() {
        if (fDocument != null) {
            fDocument.removePositionUpdater(this);
            fDocument= null;
        }
    }
    
    /**
     * Destroy the tracker.
     */
    public void dispose() {
        stopTracking();
    }

}
