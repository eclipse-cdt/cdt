/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional;

import java.util.Iterator;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.model.DisassemblyDocument;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * DisassemblyViewer
 */
public class DisassemblyViewer extends SourceViewer implements IPropertyChangeListener {

	class ResizeListener implements ControlListener {
		@Override
		public void controlResized(ControlEvent e) {
			updateViewportListeners(RESIZE);
		}

		@Override
		public void controlMoved(ControlEvent e) {
		}
	}

	private Color fForegroundColor;
	private Color fBackgroundColor;
	private boolean fUserTriggeredScrolling;
	private int fCachedLastTopPixel;
	private IPreferenceStore fPreferenceStore;
	private boolean fIsConfigured = false;

	/**
	 * Create a new DisassemblyViewer.
	 * @param parent
	 * @param ruler
	 * @param overviewRuler
	 * @param showsAnnotationOverview
	 * @param styles
	 */
	public DisassemblyViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler,
			boolean showsAnnotationOverview, int styles, IPreferenceStore store) {
		super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		// always readonly
		setEditable(false);
		setPreferenceStore(store);
	}

	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 *
	 * @param store the store to read from
	 * @param key the key used for the lookup in the preference store
	 * @param display the display used create the color
	 * @return the created color according to the specification in the preference store
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
		RGB rgb = null;

		if (store.contains(key)) {
			if (store.isDefault(key)) {
				rgb = PreferenceConverter.getDefaultColor(store, key);
			} else {
				rgb = PreferenceConverter.getColor(store, key);
			}

			if (rgb != null)
				return new Color(display, rgb);
		}

		return null;
	}

	protected void initializeViewerColors() {
		if (fPreferenceStore != null) {
			StyledText styledText = getTextWidget();

			// ----------- foreground color --------------------
			Color color = fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
					? null
					: createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,
							styledText.getDisplay());
			styledText.setForeground(color);

			if (fForegroundColor != null)
				fForegroundColor.dispose();

			fForegroundColor = color;

			// ---------- background color ----------------------
			color = fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT) ? null
					: createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
							styledText.getDisplay());
			styledText.setBackground(color);

			if (fBackgroundColor != null)
				fBackgroundColor.dispose();

			fBackgroundColor = color;
		}
	}

	public void setPreferenceStore(IPreferenceStore store) {
		if (fIsConfigured && fPreferenceStore != null)
			fPreferenceStore.removePropertyChangeListener(this);

		fPreferenceStore = store;

		if (fIsConfigured && fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
		}
	}

	@Override
	public void unconfigure() {
		if (fForegroundColor != null) {
			fForegroundColor.dispose();
			fForegroundColor = null;
		}
		if (fBackgroundColor != null) {
			fBackgroundColor.dispose();
			fBackgroundColor = null;
		}

		if (fPreferenceStore != null)
			fPreferenceStore.removePropertyChangeListener(this);

		super.unconfigure();

		fIsConfigured = false;
	}

	@Override
	public void configure(SourceViewerConfiguration configuration) {
		super.configure(configuration);
		if (fPreferenceStore != null) {
			fPreferenceStore.addPropertyChangeListener(this);
			initializeViewerColors();
		}
		fIsConfigured = true;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#createControl(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void createControl(Composite parent, int styles) {
		super.createControl(parent, styles);
		StyledText textWidget = getTextWidget();
		// extra resize listener to workaround bug 171018
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=171018
		textWidget.addControlListener(new ResizeListener());
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewer#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		switch (operation) {
		case COPY:
			StyledText textWidget = getTextWidget();
			if (textWidget == null || !redraws()) {
				return;
			}
			if (textWidget.getSelectionCount() == 0) {
				return;
			}
			String selectedText;
			try {
				selectedText = getSelectedText();
			} catch (BadLocationException e) {
				// should not happend
				DsfUIPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, e.getLocalizedMessage(), e));
				return;
			}
			Clipboard clipboard = new Clipboard(textWidget.getDisplay());
			clipboard.setContents(new Object[] { selectedText }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
			break;
		default:
			super.doOperation(operation);
		}
	}

	/**
	 * Get the selected text together with text displayed in visible
	 * ruler columns.
	 * @return  the selected text
	 * @throws BadLocationException
	 */
	public String getSelectedText() throws BadLocationException {
		StringBuilder text = new StringBuilder(200);
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		DisassemblyDocument doc = (DisassemblyDocument) getDocument();
		Point selection = getSelectedRange();
		int startOffset = selection.x;
		int length = selection.y;
		int endOffset = startOffset + length;
		int startLine = doc.getLineOfOffset(startOffset);
		int endLine = doc.getLineOfOffset(endOffset);
		int firstLineOffset = startOffset - doc.getLineOffset(startLine);
		if (firstLineOffset > 0) {
			// partial first line
			int lineLength = doc.getLineInformation(startLine).getLength();
			text.append(doc.get(startOffset, Math.min(lineLength - firstLineOffset, length)));
			++startLine;
			if (startLine <= endLine) {
				text.append(lineSeparator);
			}
		}
		for (int line = startLine; line < endLine; ++line) {
			String lineText = getLineText(line);
			text.append(lineText);
			text.append(lineSeparator);
		}
		if (doc.getLineOffset(endLine) < endOffset) {
			// partial last line
			if (startLine <= endLine) {
				int lineStart = doc.getLineOffset(endLine);
				text.append(getLinePrefix(endLine));
				text.append(doc.get(lineStart, endOffset - lineStart));
			}
		}
		return text.toString();
	}

	/**
	 * Return the content of the given line, excluding line separator.
	 * @param line  the line number
	 * @return the line content
	 * @throws BadLocationException
	 */
	public String getLineText(int line) throws BadLocationException {
		IDocument doc = getDocument();
		IRegion lineRegion = doc.getLineInformation(line);
		return getLinePrefix(line) + doc.get(lineRegion.getOffset(), lineRegion.getLength());
	}

	/**
	 * Get the line prefix by concatenating the text displayed by
	 * the visible ruler columns.
	 * @param line  the line number
	 * @return the prefix string with trailing blank or the empty string
	 */
	public String getLinePrefix(int line) {
		StringBuilder prefix = new StringBuilder(10);
		IVerticalRuler ruler = getVerticalRuler();
		if (ruler instanceof CompositeRuler) {
			for (Iterator<?> iter = ((CompositeRuler) ruler).getDecoratorIterator(); iter.hasNext();) {
				IVerticalRulerColumn column = (IVerticalRulerColumn) iter.next();
				if (column instanceof DisassemblyRulerColumn) {
					DisassemblyRulerColumn disassColumn = (DisassemblyRulerColumn) column;
					String columnText = disassColumn.createDisplayString(line);
					prefix.append(columnText);
					int columnWidth = disassColumn.computeNumberOfCharacters();
					columnWidth -= columnText.length();
					while (columnWidth-- > 0)
						prefix.append(' ');
					prefix.append(' ');
				}
			}
		}
		return prefix.toString();
	}

	/**
	 * Scroll the given position into the visible area if it is not yet visible.
	 * @param offset
	 * @see org.eclipse.jface.text.TextViewer#revealRange(int, int)
	 */
	public void revealOffset(int offset, boolean onTop) {
		try {
			IDocument doc = getVisibleDocument();

			int focusLine = doc.getLineOfOffset(offset);

			StyledText textWidget = getTextWidget();
			int top = textWidget.getTopIndex();
			if (top > -1) {

				// scroll vertically
				int lines = getEstimatedVisibleLinesInViewport();
				int bottom = top + lines;

				int bottomBuffer = Math.max(1, lines / 3);

				if (!onTop && focusLine >= top && focusLine <= bottom - bottomBuffer) {
					// do not scroll at all as it is already visible
				} else {
					if (!onTop && focusLine > bottom - bottomBuffer && focusLine <= bottom) {
						// focusLine is already in bottom bufferZone
						// scroll to top of bottom bufferzone - for smooth down-scrolling
						int scrollDelta = focusLine - (bottom - bottomBuffer);
						textWidget.setTopIndex(top + scrollDelta);
					} else {
						// scroll to top of visible area minus buffer zone
						int topBuffer = onTop ? 0 : lines / 3;
						textWidget.setTopIndex(Math.max(0, focusLine - topBuffer));
					}
					updateViewportListeners(INTERNAL);
				}
			}
		} catch (BadLocationException ble) {
			throw new IllegalArgumentException(ble.getLocalizedMessage());
		}
	}

	/**
	 * @return the number of visible lines in the viewport assuming a constant
	 *         line height.
	 */
	private int getEstimatedVisibleLinesInViewport() {
		StyledText textWidget = getTextWidget();
		if (textWidget != null) {
			Rectangle clArea = textWidget.getClientArea();
			if (!clArea.isEmpty())
				return clArea.height / textWidget.getLineHeight();
		}
		return -1;
	}

	public int getLastTopPixel() {
		return fCachedLastTopPixel;
	}

	public boolean isUserTriggeredScrolling() {
		return fUserTriggeredScrolling;
	}

	/*
	 * @see org.eclipse.jface.text.TextViewer#updateViewportListeners(int)
	 */
	@Override
	protected void updateViewportListeners(int origin) {
		fCachedLastTopPixel = fLastTopPixel;
		fUserTriggeredScrolling = origin != INTERNAL && origin != RESIZE;
		if (origin == RESIZE) {
			fLastTopPixel = -1;
		}
		super.updateViewportListeners(origin);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND.equals(property)
				|| AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)) {
			initializeViewerColors();
		}
	}
}
