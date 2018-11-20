/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import java.io.File;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class QMLHyperlink implements IHyperlink {

	private final IRegion region;
	private final ITextViewer viewer;
	private final ITextEditor editor;

	public QMLHyperlink(IRegion region, ITextViewer viewer, ITextEditor editor) {
		this.region = region;
		this.viewer = viewer;
		this.editor = editor;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return "Open Declaration";
	}

	@Override
	public void open() {
		IQMLAnalyzer analyzer = Activator.getService(IQMLAnalyzer.class);
		try {
			IDocument document = viewer.getDocument();
			String selected = document.get(region.getOffset(), region.getLength());
			IFileEditorInput fileInput = (IFileEditorInput) editor.getEditorInput();
			String fileName = new File(fileInput.getFile().getLocationURI()).getAbsolutePath().substring(1);
			List<Bindings> definitions = analyzer.getDefinition(selected, fileName, document.get(),
					region.getOffset() + region.getLength());
			if (!definitions.isEmpty()) {
				Bindings definition = definitions.iterator().next();
				Bindings start = (Bindings) definition.get("start"); //$NON-NLS-1$
				if (start == null) {
					return;
				}
				int startLine = (int) (double) start.get("line"); //$NON-NLS-1$
				int startChar = (int) (double) start.get("ch"); //$NON-NLS-1$
				int startOffset = document.getLineOffset(startLine) + startChar;
				Bindings end = (Bindings) definition.get("end"); //$NON-NLS-1$
				int endLine = (int) (double) end.get("line"); //$NON-NLS-1$
				int endChar = (int) (double) end.get("ch"); //$NON-NLS-1$
				int endOffset = document.getLineOffset(endLine) + endChar;
				String target = (String) definition.get("file"); //$NON-NLS-1$
				if (fileName.equals(target)) {
					editor.selectAndReveal(startOffset, endOffset - startOffset);
				} else {
					IFile[] targetFiles = ResourcesPlugin.getWorkspace().getRoot()
							.findFilesForLocationURI(new File("/" + target).toURI()); //$NON-NLS-1$
					if (targetFiles.length > 0) {
						IEditorPart part = IDE.openEditor(editor.getEditorSite().getPage(), targetFiles[0]);
						if (part instanceof ITextEditor) {
							((ITextEditor) part).selectAndReveal(startOffset, endOffset - startOffset);
						}
					}
				}
			}
		} catch (BadLocationException | NoSuchMethodException | ScriptException | PartInitException e) {
			Activator.log(e);
		}
	}

}
