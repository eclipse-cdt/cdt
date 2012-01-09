/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.ui.texteditor.ITextEditor;

public class THInformationProvider implements IInformationProviderExtension, IInformationProvider {

	private ITextEditor fEditor;

	public THInformationProvider(ITextEditor editor) {
		fEditor= editor;
	}

	@Override
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		return fEditor == null ? null : TypeHierarchyUI.getInput(fEditor, subject);
	}

	@Override
	public String getInformation(ITextViewer textViewer, IRegion subject) {
		return null;
	}

	@Override
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}
}
