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

import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * The disassembly viewer.
 */
public class DisassemblyViewer extends SourceViewer {

	/**
	 * The current input.
	 */
	private Object fInput;

	/**
	 * Constructor for DisassemblyViewer.
	 * 
	 * @param parent
	 * @param ruler
	 * @param styles
	 */
	public DisassemblyViewer( Composite parent, IVerticalRuler vertRuler, IOverviewRuler ovRuler ) {
		super( parent, vertRuler, ovRuler, true, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION );
		getTextWidget().setFont( JFaceResources.getFont( IInternalCDebugUIConstants.DISASSEMBLY_FONT ) );
		setEditable( false );
		GridData gd = new GridData( GridData.FILL_BOTH );
		getControl().setLayoutData( gd );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IInputProvider#getInput()
	 */
	public Object getInput() {
		return fInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setInput(java.lang.Object)
	 */
	public void setInput( Object input ) {
		fInput = input;
	}
}
