/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * @author AChapiro
 */
public class CFormattingStrategy extends ContextBasedFormattingStrategy {
	private static class WorkItem {
		final IDocument document;
		final TypedPosition partition;

		WorkItem(IDocument document, TypedPosition partition) {
			this.document = document;
			this.partition = partition;
		}
	}

	private final Deque<WorkItem> fWorkItems = new ArrayDeque<>();

	/**
	 * Creates a new formatting strategy.
	 */
	public CFormattingStrategy() {
		super();
	}

	@Override
	public void format() {
		super.format();

		WorkItem workItem = fWorkItems.getFirst();
		IDocument document = workItem.document;
		TypedPosition partition = workItem.partition;

		if (document == null || partition == null)
			return;

		Map<String, String> preferences = getPreferences();

		try {
			TextEdit edit = CodeFormatterUtil.format(CodeFormatter.K_TRANSLATION_UNIT, document.get(),
					partition.getOffset(), partition.getLength(), 0, TextUtilities.getDefaultLineDelimiter(document),
					preferences);

			if (edit != null)
				edit.apply(document);
		} catch (MalformedTreeException e) {
			CUIPlugin.log(e);
		} catch (BadLocationException e) {
			// Can only happen on concurrent document modification - log and bail out.
			CUIPlugin.log(e);
		}
	}

	@Override
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);

		TypedPosition partition = (TypedPosition) context.getProperty(FormattingContextProperties.CONTEXT_PARTITION);
		IDocument document = (IDocument) context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
		fWorkItems.addLast(new WorkItem(document, partition));
	}

	@Override
	public void formatterStops() {
		super.formatterStops();

		fWorkItems.clear();
	}
}
