/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.controls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.terminal.view.ui.nls.Messages;

/**
 * A helper class to create a composite with a highlighted note
 * entry and a message text.
 */
public class NoteCompositeHelper {

	/**
	 * The common label text to show on a note. Defaults to &quot;Note:&quot;.
	 */
	public static final String NOTE_LABEL = Messages.NoteCompositeHelper_note_label;

	private static class NoteComposite extends Composite {

		public NoteComposite(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			for (Control child : getChildren()) {
				child.setEnabled(enabled);
			}
		}
	}

	/**
	 * Creates a composite with a highlighted Note entry and a message text.
	 * This is designed to take up the full width of the page.
	 *
	 * @see PreferencePage#createNoteComposite, this is a plain copy of that!
	 * @param font
	 *            the font to use
	 * @param composite
	 *            the parent composite
	 * @param title
	 *            the title of the note
	 * @param message
	 *            the message for the note
	 *
	 * @return the composite for the note
	 */
	public static Composite createNoteComposite(Font font, Composite composite, String title, String message) {
		return createNoteComposite(font, composite, title, message, SWT.DEFAULT);
	}

	/**
	 * Creates a composite with a highlighted Note entry and a message text.
	 * This is designed to take up the full width of the page.
	 *
	 * @see PreferencePage#createNoteComposite, this is a plain copy of that!
	 * @param font
	 *            the font to use
	 * @param composite
	 *            the parent composite
	 * @param title
	 *            the title of the note
	 * @param message
	 *            the message for the note
	 * @param minCharsPerLine
	 *            the minimum number of characters per line. Defaults to '65' if less than '20'.
	 *
	 * @return the composite for the note
	 */
	public static Composite createNoteComposite(Font font, Composite composite, String title, String message, int minCharsPerLine) {
		final GC gc = new GC(composite);
		gc.setFont(font);

		Composite messageComposite = new NoteComposite(composite, SWT.NONE);

		GridLayout messageLayout = new GridLayout();
		messageLayout.numColumns = 2;
		messageLayout.marginWidth = 0;
		messageLayout.marginHeight = 0;
		messageComposite.setLayout(messageLayout);

		GridData layoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		if (composite.getLayout() instanceof GridLayout) {
			layoutData.horizontalSpan = ((GridLayout) composite.getLayout()).numColumns;
		}
		messageComposite.setLayoutData(layoutData);
		messageComposite.setFont(font);

		final Label noteLabel = new Label(messageComposite, SWT.BOLD);
		noteLabel.setText(title);
		noteLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		noteLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				// Note: This is actually wrong but the same as in platforms
				// PreferencePage
				if (JFaceResources.BANNER_FONT.equals(event.getProperty())) {
					noteLabel.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
				}
			}
		};
		JFaceResources.getFontRegistry().addListener(fontListener);
		noteLabel.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});

		Label messageLabel = new Label(messageComposite, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.setFont(font);

		/**
		 * Set the controls style to FILL_HORIZONTAL making it multi-line if
		 * needed
		 */
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), minCharsPerLine >= 20 ? minCharsPerLine : 65);
		messageLabel.setLayoutData(layoutData);

		gc.dispose();

		return messageComposite;
	}

	/**
	 * change the text of the second label
	 *
	 * @param messageComposite
	 *            the NoteComposite that gets returned from createNoteComposite
	 * @param msg
	 *            the new text
	 */
	public static void setMessage(Composite messageComposite, String msg) {
		if (messageComposite instanceof NoteComposite) {
			Control[] children = messageComposite.getChildren();
			if (children.length == 2) {
				Control c = children[1];
				if (c instanceof Label) {
					((Label) c).setText(msg);
					messageComposite.pack();
				}
			}
		}
	}
}
