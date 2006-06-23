/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.cdt.debug.internal.ui.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Viewer configuration for disassembly.
 */
public class DisassemblyViewerConfiguration extends SourceViewerConfiguration {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover( ISourceViewer sourceViewer ) {
		return new DisassemblyAnnotationHover();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationControlCreator(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IInformationControlCreator getInformationControlCreator( ISourceViewer sourceViewer ) {
		return getInformationControlCreator( sourceViewer, true );
	}

	public IInformationControlCreator getInformationControlCreator( ISourceViewer sourceViewer, final boolean cutDown ) {
		return new IInformationControlCreator() {

			public IInformationControl createInformationControl( Shell parent ) {
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl( parent, style, new HTMLTextPresenter( cutDown ) );
				// return new HoverBrowserControl(parent);
			}
		};
	}
}
