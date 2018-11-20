/*******************************************************************************
 * Copyright (c) 2011, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Marc Khouzam (Ericsson) - Add support for multi-attach (Bug 293679)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.Arrays;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * Process prompter that allows the user to select one or more entries
 * in the top pane.  Those entries are displayed in the bottom pane.
 * No selection is allowed in the bottom pane.  The result returned
 * is the list of all selections of the top pane (shown in the bottom
 * pane).
 */
public class ProcessPrompterDialog extends TwoPaneElementSelector {

	private static final String DIALOG_SETTINGS_SECTION_ID = "processPrompterDialog"; //$NON-NLS-1$

	private static final String DIALOG_SETTINGS_FILTER_KEY = "filter"; //$NON-NLS-1$

	private final ILabelProvider elementRenderer;

	public ProcessPrompterDialog(Shell parent, ILabelProvider elementRenderer, ILabelProvider qualifierRenderer) {
		super(parent, elementRenderer, qualifierRenderer);
		this.elementRenderer = elementRenderer;
		setDialogBoundsSettings(getDialogBoundsSettings(), Dialog.DIALOG_PERSISTSIZE);
		setFilter(getFilterFromDialogSetting());
	}

	@Override
	protected FilteredList createFilteredList(Composite parent) {
		FilteredList list = super.createFilteredList(parent);

		list.setFilterMatcher(new FilteredList.FilterMatcher() {
			private SearchPattern matcher;

			@Override
			public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {

				if (pattern == null) {
					pattern = ""; //$NON-NLS-1$
				}

				if (!pattern.startsWith("*")) { //$NON-NLS-1$
					pattern = "*" + pattern; //$NON-NLS-1$
				}

				int rules = SearchPattern.RULE_BLANK_MATCH | SearchPattern.RULE_PREFIX_MATCH;
				if (!ignoreCase) {
					rules |= SearchPattern.RULE_CASE_SENSITIVE;
				}

				if (!ignoreWildCards) {
					rules |= SearchPattern.RULE_PATTERN_MATCH;
				}
				matcher = new SearchPattern(rules);
				matcher.setPattern(pattern);
			}

			@Override
			public boolean match(Object element) {
				return matcher.matches(elementRenderer.getText(element));
			}
		});

		return list;
	}

	/*
	 * The result should be every selected element.
	 */
	@Override
	protected void computeResult() {
		setResult(Arrays.asList(getSelectedElements()));
	}

	/*
	 * Disable the ability to select items in the bottom pane.
	 */
	@Override
	protected Table createLowerList(Composite parent) {
		final Table list = super.createLowerList(parent);

		// First remove listeners such as the double click.
		// We don't want the user to trigger the action by
		// double-clicking on the bottom pane.
		int[] events = { SWT.Selection, SWT.MouseDoubleClick };
		for (int event : events) {
			Listener[] selectionListeners = list.getListeners(event);
			for (Listener listener : selectionListeners) {
				list.removeListener(event, listener);
			}
		}

		// Now add a listener to prevent selection
		list.addListener(SWT.EraseItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) != 0) {
					event.detail &= ~SWT.SELECTED;
					// Removing the SELECTED event did not work properly.
					// The foreground text became invisible.
					// Let's simply deselect everything
					list.deselectAll();
				}
			}
		});
		return list;
	}

	/*
	 * Allow a double-click to work without any selection
	 * in the bottom list.
	 */
	@Override
	protected void handleDefaultSelected() {
		if (validateCurrentSelection()) {
			buttonPressed(IDialogConstants.OK_ID);
		}
	}

	/**
	 * Validate only upper selected elements. Lower list is always disabled.
	 *
	 * @see #createLowerList(Composite)
	 */
	@Override
	protected boolean validateCurrentSelection() {
		ISelectionStatusValidator validator = getValidator();
		Object[] elements = getSelectedElements();

		if (elements.length > 0) {
			IStatus status;
			if (validator != null) {
				status = validator.validate(elements);
			} else {
				status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
						null);
			}
			updateStatus(status);
			return status.isOK();
		}

		return super.validateCurrentSelection();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return getDialogSettings();
	}

	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = GdbUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION_ID);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION_ID);
		}
		return section;
	}

	private String getFilterFromDialogSetting() {
		String filter = getDialogSettings().get(DIALOG_SETTINGS_FILTER_KEY);
		return filter == null ? "" : filter; //$NON-NLS-1$
	}

	private void storeDialogSetting() {
		getDialogSettings().put(DIALOG_SETTINGS_FILTER_KEY, getFilter());
	}

	@Override
	public boolean close() {
		storeDialogSetting();
		return super.close();
	}
}
