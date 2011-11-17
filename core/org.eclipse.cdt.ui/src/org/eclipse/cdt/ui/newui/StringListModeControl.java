/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * Local UI control for multiple configurations string list mode redirecting to
 * Preference page "Multiple Configurations Edit".
 *
 * @since 5.3
 */
public class StringListModeControl {
	private static final String STRING_LIST_MODE_PREFERENCE_PAGE = "org.eclipse.cdt.managedbuilder.ui.preferences.PrefPage_MultiConfig"; //$NON-NLS-1$
	private ICPropertyProvider page;
	private Link linkStringListMode;
	private List<Listener> listeners = new ArrayList<Listener>();

	/**
	 * Constructor.
	 *
	 * @param page - preference page.
	 * @param parent - parent {@code Composite} control.
	 * @param span - horizontal span for the control
	 */
	public StringListModeControl(ICPropertyProvider page, final Composite parent, int span) {
		this.page = page;
		linkStringListMode = new Link(parent, SWT.NONE);
		updateStringListModeLink(linkStringListMode);
		linkStringListMode.setToolTipText(Messages.AbstractLangsListTab_MultiConfigStringListModeLinkHint);

		linkStringListMode.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// Use event.text to tell which link was used
				int result = PreferencesUtil.createPreferenceDialogOn(parent.getShell(), STRING_LIST_MODE_PREFERENCE_PAGE, null, null).open();
				if (result!=Window.CANCEL) {
					updateStringListModeControl();
					for (Listener listener : listeners) {
						listener.handleEvent(event);
					}
				}
			}
		});

		GridData gridData = new GridData(SWT.RIGHT, SWT.NONE, true, false);
		gridData.horizontalSpan = span;
		linkStringListMode.setLayoutData(gridData);
	}

	/**
	 * Add a listener suitable for {@link org.eclipse.swt.widgets.Widget#addListener(int, Listener)}.
	 *
	 * @param eventType - the type of event to listen for, currently not used.
	 * @param listener - the listener which should be notified when the event occurs.
	 */
	public void addListener(int eventType, final Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener from the collection of listeners.
	 *
	 * @param eventType the type of event to listen for, currently not used.
	 * @param listener the listener which should no longer be notified.
	 */
	protected void removeListener (int eventType, Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * Use to update the control when one of the string list modes gets modified .
	 */
	public void updateStringListModeControl() {
		updateStringListModeLink(linkStringListMode);
	}

	/**
	 * Updates the message of the link presented to the user.
	 *
	 * @param link - {@code Link} object to update.
	 */
	private void updateStringListModeLink(Link link) {
		boolean isMultiCfg = page.isMultiCfg();
		linkStringListMode.setVisible(isMultiCfg);
		if (isMultiCfg) {
			String modeUnknown = Messages.AbstractLangsListTab_UnknownMode;
			String modeDisplay = modeUnknown;
			switch (CDTPrefUtil.getMultiCfgStringListDisplayMode()) {
			case CDTPrefUtil.DMODE_CONJUNCTION:
				modeDisplay = Messages.AbstractLangsListTab_Conjunction;
				break;
			case CDTPrefUtil.DMODE_DISJUNCTION:
				modeDisplay = Messages.AbstractLangsListTab_Disjunction;
				break;
			}

			String modeWrite = modeUnknown;
			switch (CDTPrefUtil.getMultiCfgStringListWriteMode()) {
			case CDTPrefUtil.WMODE_MODIFY:
				modeWrite = Messages.AbstractLangsListTab_Modify;
				break;
			case CDTPrefUtil.WMODE_REPLACE:
				modeWrite = Messages.AbstractLangsListTab_Replace;
				break;
			}

			linkStringListMode.setText(Messages.AbstractLangsListTab_StringListMode +
					" <a href=\"workspace-settings\">"+modeDisplay+"</a> + <a href=\"workspace-settings\">"+modeWrite+"</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		linkStringListMode.getParent().layout();
	}

}
