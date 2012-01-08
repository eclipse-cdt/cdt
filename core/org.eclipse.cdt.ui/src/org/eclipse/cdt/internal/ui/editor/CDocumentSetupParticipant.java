/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      QNX Software Systems - Initial API and implementation
 *      Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.text.CTextTools;


/**
 * CDocumentSetupParticipant
 */
public class CDocumentSetupParticipant implements IDocumentSetupParticipant, IDocumentSetupParticipantExtension {
	/**
	 * 
	 */
	public CDocumentSetupParticipant() {
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void setup(IDocument document) {
		setup(document, null, null);
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension#setup(org.eclipse.jface.text.IDocument, org.eclipse.core.runtime.IPath, org.eclipse.core.filebuffers.LocationKind)
	 */
	@Override
	public void setup(IDocument document, IPath location, LocationKind locationKind) {
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		tools.setupCDocument(document, location, locationKind);
	}
}
