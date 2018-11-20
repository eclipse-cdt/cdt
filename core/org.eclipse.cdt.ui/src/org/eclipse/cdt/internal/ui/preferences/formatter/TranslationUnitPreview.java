/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatterExtension;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.swt.widgets.Composite;

public class TranslationUnitPreview extends CPreview {
	private String fPreviewText;
	private int fPreviewTextOffset;
	private String fFormatterId;

	/**
	 * @param workingValues
	 * @param parent
	 */
	public TranslationUnitPreview(Map<String, String> workingValues, Composite parent) {
		super(workingValues, parent);
	}

	@Override
	protected void doFormatPreview() {
		if (fPreviewText == null) {
			fPreviewDocument.set(""); //$NON-NLS-1$
			return;
		}
		fPreviewDocument.set(fPreviewText);

		fSourceViewer.setVisibleRegion(fPreviewTextOffset, fPreviewText.length() - fPreviewTextOffset);
		fSourceViewer.setRedraw(false);
		final IFormattingContext context = new FormattingContext();
		try {
			final IContentFormatter formatter = fViewerConfiguration.getContentFormatter(fSourceViewer);
			if (formatter instanceof IContentFormatterExtension) {
				final IContentFormatterExtension extension = (IContentFormatterExtension) formatter;
				Map<String, String> prefs = fWorkingValues;
				if (fFormatterId != null) {
					prefs = new HashMap<>(fWorkingValues);
					prefs.put(CCorePreferenceConstants.CODE_FORMATTER, fFormatterId);
				}
				context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES, prefs);
				context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.valueOf(true));
				extension.format(fPreviewDocument, context);
			} else {
				formatter.format(fPreviewDocument, new Region(0, fPreviewDocument.getLength()));
			}
		} catch (Exception e) {
			final IStatus status = new Status(IStatus.ERROR, CUIPlugin.getPluginId(), ICStatusConstants.INTERNAL_ERROR,
					FormatterMessages.CPreview_formatter_exception, e);
			CUIPlugin.log(status);
		} finally {
			context.dispose();
			fSourceViewer.setRedraw(true);
		}
	}

	public void setPreviewText(String previewText) {
		setPreviewText(previewText, 0);
	}

	public void setPreviewText(String previewText, int previewTextOffset) {
		fPreviewText = previewText;
		fPreviewTextOffset = previewTextOffset;
		update();
	}

	/**
	 * @param formatterId
	 */
	public void setFormatterId(String formatterId) {
		fFormatterId = formatterId;
	}
}
