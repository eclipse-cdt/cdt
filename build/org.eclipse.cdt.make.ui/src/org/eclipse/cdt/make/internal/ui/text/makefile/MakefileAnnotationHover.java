/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.text.makefile;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;

/**
 * MakefileAnnotationHover
 *
 */
public class MakefileAnnotationHover implements IAnnotationHover {

	private IEditorPart fEditor;

	/**
	 *
	 */
	public MakefileAnnotationHover(IEditorPart editor) {
		fEditor = editor;
	}

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IDocument document = sourceViewer.getDocument();
		try {
			IRegion info = document.getLineInformation(lineNumber);
			String line = document.get(info.getOffset(), info.getLength());
			int numberOfLines = document.getNumberOfLines();
			while (line != null && line.endsWith("\\")) { //$NON-NLS-1$
				line = line.substring(0, line.length() - 1);
				lineNumber++;
				if (lineNumber < numberOfLines) {
					info = document.getLineInformation(lineNumber);
					String l = document.get(info.getOffset(), info.getLength());
					line += "\n" + l; //$NON-NLS-1$
				}
			}
			if (line != null && line.indexOf('$') != -1 && line.length() > 1) {
				IWorkingCopyManager fManager = MakeUIPlugin.getDefault().getWorkingCopyManager();
				IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
				line = makefile.expandString(line);
				return line;
			}
			return line;
		} catch (BadLocationException x) {
		}
		return null;
	}

}
