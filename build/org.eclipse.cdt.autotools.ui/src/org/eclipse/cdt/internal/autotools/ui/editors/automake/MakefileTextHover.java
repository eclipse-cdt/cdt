/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;

/**
 * MakefileTextHover
 *
 */
public class MakefileTextHover implements ITextHover {

	private IEditorPart fEditor;

	/**
	 *
	 */
	public MakefileTextHover(IEditorPart editor) {
		fEditor = editor;
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				int len = hoverRegion.getLength();
				int offset = hoverRegion.getOffset();
				textViewer.getDocument().get(offset, len); // check off/len validity
				if (fEditor != null && len > -1) {
					IWorkingCopyManager fManager = AutomakeEditorFactory.getDefault().getWorkingCopyManager();
					IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
					if (makefile != null) {
						WordPartDetector wordPart = new WordPartDetector(textViewer, offset);
						String name = wordPart.toString();
						IMacroDefinition[] statements = null;
						if (WordPartDetector.inMacro(textViewer, offset)) {
							statements = makefile.getMacroDefinitions(name);
							if (statements == null || statements.length == 0) {
								statements = makefile.getBuiltinMacroDefinitions(name);
							}
						}

						if (statements == null) {
							statements = new IMacroDefinition[0];
						}
						// iterate over all the different categories
						StringBuilder buffer = new StringBuilder();
						for (int i = 0; i < statements.length; i++) {
							if (i > 0) {
								buffer.append("\n"); //$NON-NLS-1$
							}
							String infoString = statements[i].getValue().toString();
							buffer.append(name);
							buffer.append(" - "); //$NON-NLS-1$
							buffer.append(infoString);
						}
						return buffer.toString();
					}
				}
			} catch (BadLocationException e) {
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		return new Region(offset, 0);
	}

}
