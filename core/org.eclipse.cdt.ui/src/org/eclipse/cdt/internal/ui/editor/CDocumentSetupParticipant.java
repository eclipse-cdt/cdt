/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      QNX Software Systems - Initial API and implementation
 *      Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

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
		CTextTools tools = CUIPlugin.getDefault().getTextTools();
		tools.setupCDocument(document, location, locationKind);
	}
}
