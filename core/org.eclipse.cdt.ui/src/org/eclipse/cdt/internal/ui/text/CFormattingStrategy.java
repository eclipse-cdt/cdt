/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

/**
 * @author AChapiro
 */
public class CFormattingStrategy extends ContextBasedFormattingStrategy {
	/** Documents to be formatted by this strategy */
	private final Deque<IDocument> fDocuments= new ArrayDeque<>();
	/** Partitions to be formatted by this strategy */
	private final Deque<TypedPosition> fPartitions= new ArrayDeque<>();

	/**
	 * Creates a new formatting strategy.
 	 */
	public CFormattingStrategy() {
		super();
	}

	@Override
	public void format() {
		super.format();
		
		IDocument document= fDocuments.removeFirst();
		TypedPosition partition= fPartitions.removeFirst();
		
		if (document != null && partition != null) {
			try {
				@SuppressWarnings("unchecked")
				Map<String, String> preferences = getPreferences();
				TextEdit edit = CodeFormatterUtil.format(
						CodeFormatter.K_TRANSLATION_UNIT, document.get(),
						partition.getOffset(), partition.getLength(), 0,
						TextUtilities.getDefaultLineDelimiter(document),
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
 	}

	@Override
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);
		
		Object property = context.getProperty(FormattingContextProperties.CONTEXT_PARTITION);
		if (property instanceof TypedPosition) {
			fPartitions.addLast((TypedPosition) property);
		}
		property= context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
		if (property instanceof IDocument) {			
			fDocuments.addLast((IDocument) property);
		}
	}

	@Override
	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}
}
