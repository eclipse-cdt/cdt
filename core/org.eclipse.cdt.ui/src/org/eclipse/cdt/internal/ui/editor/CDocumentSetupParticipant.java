/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;

/**
 * CDocumentSetupParticipant
 */
public class CDocumentSetupParticipant implements IDocumentSetupParticipant {
	/**
	 * 
	 */
	public CDocumentSetupParticipant() {
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		if (Display.getCurrent() != null){
			CTextTools tools= CUIPlugin.getDefault().getTextTools();
			tools.setupCDocument(document);
		}
	}

}
