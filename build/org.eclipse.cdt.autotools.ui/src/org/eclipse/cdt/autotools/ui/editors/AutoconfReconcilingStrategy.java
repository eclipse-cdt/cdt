/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.cdt.autotools.ui.editors.outline.AutoconfContentOutlinePage;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfParser;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class AutoconfReconcilingStrategy implements IReconcilingStrategy {

	AutoconfContentOutlinePage outline;
	//	int lastRegionOffset;
	AutoconfEditor editor;
	IDocumentProvider documentProvider;

	public AutoconfReconcilingStrategy(AutoconfEditor editor) {
		outline = editor.getOutlinePage();
		//		lastRegionOffset = Integer.MAX_VALUE;
		this.editor = editor;
		documentProvider = editor.getDocumentProvider();
	}

	@Override
	public void reconcile(IRegion partition) {
		try {
			AutoconfParser parser = editor.getAutoconfParser();
			((AutoconfErrorHandler) parser.getErrorHandler()).removeAllExistingMarkers();

			editor.setRootElement(parser.parse(documentProvider.getDocument(editor.getEditorInput())));
			outline.update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocument(IDocument document) {
		// TODO Auto-generated method stub

	}

}
