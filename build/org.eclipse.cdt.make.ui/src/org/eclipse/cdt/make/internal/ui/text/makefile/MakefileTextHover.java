/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.text.WordPartDetector;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				int len = hoverRegion.getLength();
				int offset = hoverRegion.getOffset();
				textViewer.getDocument().get(offset, len); // check off/len validity
				if (fEditor != null && len > -1) {
					IWorkingCopyManager fManager = MakeUIPlugin.getDefault().getWorkingCopyManager();
					IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
					IMacroDefinition[] statements;
					if (WordPartDetector.inMacro(textViewer, offset)) {
						IMacroDefinition[] m1 = makefile.getMacroDefinitions();
						IMacroDefinition[] m2 = makefile.getBuiltinMacroDefinitions();
						statements = new IMacroDefinition[m1.length + m2.length];
						System.arraycopy(m1, 0, statements, 0, m1.length);
						System.arraycopy(m2, 0, statements, m1.length, m2.length);
					} else {
						statements = new IMacroDefinition[0];
					}
					// iterate over all the different categories
					WordPartDetector wordPart = new WordPartDetector(textViewer, offset);
					StringBuffer buffer = new StringBuffer();
					for (int i = 0; i < statements.length; i++) {
						String name = statements[i].getName();
						String infoString = statements[i].getValue().toString();
						if (name != null && name.equals(wordPart.toString())) {
							buffer.append(name);
							buffer.append(" - "); //$NON-NLS-1$
							buffer.append(infoString);
							break;						
						}
					}
					return buffer.toString();
				}
			} catch (BadLocationException e) {
			}
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		return new Region(offset, 0);
	}

}
