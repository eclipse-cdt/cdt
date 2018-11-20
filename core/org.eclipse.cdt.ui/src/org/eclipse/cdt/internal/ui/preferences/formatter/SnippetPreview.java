/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.TextEdit;

public class SnippetPreview extends CPreview {

	public final static class PreviewSnippet {
		public String header;
		public final String source;
		public final int kind;

		public PreviewSnippet(int kind, String source) {
			this.kind = kind;
			this.source = source;
		}
	}

	private ArrayList<PreviewSnippet> fSnippets;

	public SnippetPreview(Map<String, String> workingValues, Composite parent) {
		super(workingValues, parent);
		fSnippets = new ArrayList<>();
	}

	@Override
	protected void doFormatPreview() {
		if (fSnippets.isEmpty()) {
			fPreviewDocument.set(""); //$NON-NLS-1$
			return;
		}

		// This delimiter looks best for invisible characters
		final String delimiter = "\n"; //$NON-NLS-1$

		final StringBuilder buffer = new StringBuilder();
		for (PreviewSnippet snippet : fSnippets) {
			String formattedSource;
			try {
				TextEdit edit = CodeFormatterUtil.format(snippet.kind, snippet.source, 0, delimiter, fWorkingValues);
				if (edit == null) {
					formattedSource = snippet.source;
				} else {
					Document document = new Document(snippet.source);
					edit.apply(document, TextEdit.NONE);
					formattedSource = document.get();
				}
			} catch (Exception e) {
				final IStatus status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(),
						ICStatusConstants.INTERNAL_ERROR, FormatterMessages.CPreview_formatter_exception, e);
				CUIPlugin.log(status);
				continue;
			}
			buffer.append(delimiter);
			buffer.append(formattedSource);
			buffer.append(delimiter);
			buffer.append(delimiter);
		}
		fPreviewDocument.set(buffer.toString());
	}

	public void add(PreviewSnippet snippet) {
		fSnippets.add(snippet);
	}

	public void remove(PreviewSnippet snippet) {
		fSnippets.remove(snippet);
	}

	public void addAll(Collection<PreviewSnippet> snippets) {
		fSnippets.addAll(snippets);
	}

	public void clear() {
		fSnippets.clear();
	}
}
