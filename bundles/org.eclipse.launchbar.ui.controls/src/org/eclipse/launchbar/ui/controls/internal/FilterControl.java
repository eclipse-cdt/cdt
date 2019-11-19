/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A simple control that provides a text widget and controls a list viewer
 */
public class FilterControl extends Composite {
	/**
	 * The filter text widget to be used by this tree. This value may be <code>null</code> if there is no filter widget, or if the
	 * controls have not yet been created.
	 */
	protected Text filterText;
	/**
	 * The viewer for the filtered tree. This value should never be <code>null</code> after the widget creation methods are
	 * complete.
	 */
	protected LaunchBarListViewer listViewer;

	protected ViewerFilter patternFilter;
	/**
	 * The text to initially show in the filter text control.
	 */
	protected String initialText = ""; //$NON-NLS-1$
	protected String patternText = null;
	/**
	 * The job used to refresh the tree.
	 */
	private Job refreshJob;
	/**
	 * The parent composite this control.
	 */
	protected Composite parent;

	/**
	 * Creates a filter control, to be fully function attachListViewer must be called shortly after
	 * 
	 * @param parent
	 */
	public FilterControl(Composite parent) {
		super(parent, SWT.NONE);
		this.parent = parent;
		patternFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				String text = ((ILabelProvider) listViewer.getLabelProvider()).getText(element);
				if (patternText == null)
					return true;
				String trim = patternText.trim();
				if (trim.isEmpty())
					return true;
				if (text == null)
					return false;
				if (text.contains(trim))
					return true;
				if (text.toLowerCase().contains(trim.toLowerCase()))
					return true;
				return false;
			}
		};
		init();
	}

	/**
	 * Create the filtered list.
	 */
	protected void init() {
		createControl(this, SWT.NONE);
		createRefreshJob();
		setInitialText(Messages.FilterControl_0);
		setFont(parent.getFont());
	}

	/**
	 * Create the filtered tree's controls. Subclasses should override.
	 * 
	 * @param parent
	 * @param treeStyle
	 */
	protected void createControl(Composite parent, int treeStyle) {
		setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
		if (parent.getLayout() instanceof GridLayout) {
			setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		}
		Composite fc = createFilterControls(parent);
		fc.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}

	/**
	 * Create the filter controls. By default, a text and corresponding tool bar button that clears the contents of the text is
	 * created. Subclasses may override.
	 * 
	 * @param parent
	 *            parent <code>Composite</code> of the filter controls
	 * @return the <code>Composite</code> that contains the filter controls
	 */
	protected Composite createFilterControls(Composite parent) {
		createFilterText(parent);
		return parent;
	}

	public Control attachListViewer(LaunchBarListViewer listViewer) {
		this.listViewer = listViewer;
		// listViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		listViewer.getControl().addDisposeListener(e -> refreshJob.cancel());
		listViewer.addFilter(patternFilter);
		return listViewer.getControl();
	}

	/**
	 * Create the refresh job for the receiver.
	 * 
	 */
	private void createRefreshJob() {
		refreshJob = doCreateRefreshJob();
		refreshJob.setSystem(true);
	}

	@Override
	public void setVisible(boolean visible) {
		boolean oldVisible = getVisible();
		if (oldVisible == true && visible == false && listViewer != null && filterText.isFocusControl()) {
			listViewer.setFocus();
		}
		if (getLayoutData() instanceof GridData) {
			((GridData) getLayoutData()).heightHint = visible ? SWT.DEFAULT : 0;
		}
		super.setVisible(visible);
	}

	/**
	 * Creates a workbench job that will refresh the tree based on the current filter text. Subclasses may override.
	 * 
	 * @return a workbench job that can be scheduled to refresh the tree
	 * 
	 * @since 3.4
	 */
	protected WorkbenchJob doCreateRefreshJob() {
		return new WorkbenchJob("Refresh Filter") {//$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (listViewer == null)
					return Status.CANCEL_STATUS;
				if (listViewer.getControl().isDisposed()) {
					return Status.CANCEL_STATUS;
				}
				updatePatternText();
				if (patternText == null) {
					return Status.OK_STATUS;
				}
				Control redrawControl = listViewer.getControl();
				try {
					// don't want the user to see updates that will be made to
					// the tree
					// we are setting redraw(false) on the composite to avoid
					// dancing scrollbar
					redrawControl.setRedraw(false);
					listViewer.setHistorySupported(patternText == null || patternText.isEmpty());
					listViewer.refresh(true);
					updateListSelection(false);
					// re-focus filterText in case it lost the focus
					filterText.setFocus();
				} finally {
					redrawControl.setRedraw(true);
				}
				return Status.OK_STATUS;
			}
		};
	}

	/**
	 * Creates the filter text and adds listeners. This method calls {@link #doCreateFilterText(Composite)} to create the text
	 * control. Subclasses should override {@link #doCreateFilterText(Composite)} instead of overriding this method.
	 * 
	 * @param parent
	 *            <code>Composite</code> of the filter text
	 */
	protected void createFilterText(Composite parent) {
		filterText = doCreateFilterText(parent);
		filterText.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(AccessibleEvent e) {
						String filterTextString = filterText.getText();
						if (filterTextString.length() == 0
								|| filterTextString.equals(initialText)) {
							e.result = initialText;
						} else {
							e.result = NLS.bind(
									Messages.FilterControl_1,
									new String[] {
											filterTextString,
											String.valueOf(getFilteredItemsCount()) });
						}
					}

					/**
					 * Return the number of filtered items
					 * 
					 * @return int
					 */
					private int getFilteredItemsCount() {
						return listViewer.getItemCount();
					}
				});
		filterText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (filterText.getText().equals(initialText)) {
					setFilterText(""); //$NON-NLS-1$
				}
			}
		});
		filterText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (filterText.getText().equals(initialText)) {
					clearText();
				}
			}
		});
		// enter key set focus to tree
		filterText.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
				listViewer.setFocus();
				updateListSelection(true);
			} else if (e.detail == SWT.TRAVERSE_ARROW_NEXT) {
				listViewer.setFocus();
				updateListSelection(false);
			} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
				listViewer.setDefaultSelection(new StructuredSelection());
				e.doit = false;
			}
		});
		filterText.addModifyListener(e -> textChanged());
		// if we're using a field with built in cancel we need to listen for
		// default selection changes (which tell us the cancel button has been
		// pressed)
		if ((filterText.getStyle() & SWT.ICON_CANCEL) != 0) {
			filterText.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if (e.detail == SWT.ICON_CANCEL)
						clearText();
				}
			});
		}
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	protected void updateListSelection(boolean enter) {
		if (listViewer.getItemCount() == 0) {
			if (enter)
				Display.getCurrent().beep();
		} else {
			StructuredSelection sel;
			// if the initial filter text hasn't changed, do not try to match
			if (patternText != null && !patternText.trim().isEmpty()) {
				// select item with triggering event, it may close the popup if list used as combo
				sel = new StructuredSelection(listViewer.getTopFilteredElement());
			} else {
				sel = new StructuredSelection(listViewer.getTopElement());
			}
			if (enter)
				listViewer.setDefaultSelection(sel);
			else
				listViewer.setSelection(sel);
		}
	}

	protected Text doCreateFilterText(Composite parent) {
		return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH
				| SWT.ICON_CANCEL);
	}

	/**
	 * Update the receiver after the text has changed.
	 */
	protected void textChanged() {
		String old = patternText;
		updatePatternText();
		if (patternText != null && old == null && patternText.isEmpty())
			return;// we changing from initial selection to empty string
		if (patternText == old)
			return;
		// cancel currently running job first, to prevent unnecessary redraw
		refreshJob.cancel();
		refreshJob.schedule(getRefreshJobDelay());
	}

	/**
	 * Return the time delay that should be used when scheduling the filter refresh job. Subclasses may override.
	 * 
	 * @return a time delay in milliseconds before the job should run
	 * 
	 */
	protected long getRefreshJobDelay() {
		return 200;
	}

	/**
	 * Set the background for the widgets that support the filter text area.
	 * 
	 * @param background
	 *            background <code>Color</code> to set
	 */
	@Override
	public void setBackground(Color background) {
		super.setBackground(background);
		// listComposite.setBackground(background);
		// filterText.setBackground(background);
	}

	/**
	 * Clears the text in the filter text widget.
	 */
	protected void clearText() {
		setFilterText(""); //$NON-NLS-1$
		// textChanged();
	}

	/**
	 * Set the text in the filter control.
	 * 
	 * @param string
	 */
	protected void setFilterText(String string) {
		if (filterText != null) {
			filterText.setText(string);
			selectAll();
		}
	}

	public Text getFilterText() {
		return filterText;
	}

	/**
	 * Get the tree viewer of the receiver.
	 * 
	 * @return the tree viewer
	 */
	public LaunchBarListViewer getViewer() {
		return listViewer;
	}

	/**
	 * Get the filter text for the receiver, if it was created. Otherwise return <code>null</code>.
	 * 
	 * @return the filter Text, or null if it was not created
	 */
	public Text getFilterControl() {
		return filterText;
	}

	/**
	 * Convenience method to return the text of the filter control. If the text widget is not created, then null is returned.
	 * 
	 * @return String in the text, or null if the text does not exist
	 */
	protected String getFilterString() {
		return filterText != null ? filterText.getText() : null;
	}

	/**
	 * Set the text that will be shown until the first focus. A default value is provided, so this method only need be called if
	 * overriding the default initial text is desired.
	 * 
	 * @param text
	 *            initial text to appear in text field
	 */
	public void setInitialText(String text) {
		initialText = text;
		if (filterText != null) {
			filterText.setMessage(text);
			if (filterText.isFocusControl()) {
				setFilterText(initialText);
				textChanged();
			} else {
				getDisplay().asyncExec(() -> {
					if (!filterText.isDisposed() && filterText.isFocusControl()) {
						setFilterText(initialText);
						textChanged();
					}
				});
			}
		} else {
			setFilterText(initialText);
			textChanged();
		}
	}

	/**
	 * Select all text in the filter text field.
	 * 
	 */
	protected void selectAll() {
		if (filterText != null) {
			filterText.selectAll();
		}
	}

	/**
	 * Get the initial text for the receiver.
	 * 
	 * @return String
	 */
	protected String getInitialText() {
		return initialText;
	}

	private void updatePatternText() {
		String text = getFilterString();
		boolean initial = initialText != null
				&& initialText.equals(text);
		if (initial) {
			patternText = null;
		} else if (text != null) {
			patternText = text;
		}
	}
}
