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
package org.eclipse.cdt.debug.internal.ui.disassembly.viewer;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Notifies the associated document when the viewer is refreshed, scrolled or resized.
 */
public class VirtualSourceViewer extends SourceViewer {

    private SelectionListener fScrollSelectionListener;

    public VirtualSourceViewer( Composite parent, IVerticalRuler ruler, int styles ) {
        this( parent, ruler, null, false, styles );
    }

    public VirtualSourceViewer( Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles ) {
        super( parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles );
        initScrollBarListener();
        initControlListener();
    }

    public VirtualDocument getVirtualDocument() {
        return (VirtualDocument)getDocument();
    }

    private void initControlListener() {
        getTextWidget().addControlListener( new ControlListener() {

            @Override
			public void controlMoved( ControlEvent e ) {
            }
            
            @Override
			public void controlResized( ControlEvent e ) {
                handleControlResized();
            }
        } );
    }

    private void initScrollBarListener() {
        ScrollBar scroll = getTextWidget().getVerticalBar();
        fScrollSelectionListener = new SelectionAdapter() {
            
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected( SelectionEvent e ) {
                handleScrollBarSelection( e );
            }
        };
        scroll.addSelectionListener( fScrollSelectionListener );
    }

    public int getNumberOfVisibleLines() {
        StyledText widget = getTextWidget();
        if ( widget != null ) {
            Rectangle clArea = widget.getClientArea();
            if ( !clArea.isEmpty() ) {
                return (clArea.height / widget.getLineHeight()) + 1;
            }
        }
        return 0;
    }
    
    protected void handleScrollBarSelection( SelectionEvent e ) {
        int offset = getVirtualDocument().getCurrentOffset();
        int lines = getNumberOfVisibleLines();
        if ( e.detail == SWT.ARROW_UP ) {
            --offset;
        }
        else if ( e.detail == SWT.ARROW_DOWN ) {
            ++offset;
        }
        else if ( e.detail == SWT.PAGE_UP ) {
            offset -= lines;
        }
        else if ( e.detail == SWT.PAGE_DOWN ) {
            offset += lines;
        }
        else if ( e.detail == SWT.HOME ) {
        }
        else if ( e.detail == SWT.END ) {
        }
        getVirtualDocument().updateContent( lines, offset, false );
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.TextViewer#refresh()
     */
    @Override
    public void refresh() {
        refresh( false );
    }

    public void refresh( boolean revealInput ) {
        VirtualDocument document = getVirtualDocument();
        document.updateContent( getNumberOfVisibleLines(), getVirtualDocument().getCurrentOffset(), revealInput );
    }

    protected void handleControlResized() {
        getVirtualDocument().updateContent( getNumberOfVisibleLines(), getVirtualDocument().getCurrentOffset(), false );
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewer#handleDispose()
     */
    @Override
    protected void handleDispose() {
        getVirtualDocument().dispose();
        super.handleDispose();
    }
}
