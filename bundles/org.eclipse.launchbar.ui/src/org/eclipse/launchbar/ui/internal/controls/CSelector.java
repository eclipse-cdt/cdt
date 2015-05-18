/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.controls;

import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.launchbar.ui.IHoverProvider;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public abstract class CSelector extends Composite {
	private IStructuredContentProvider contentProvider;
	private ILabelProvider labelProvider;
	private IHoverProvider hoverProvider;
	private Comparator<?> sorter;
	private Comparator<?> sorterTop;
	private Object input;
	private Composite buttonComposite;
	private String toolTipText;
	private boolean toolTipWasModified; // Used for the hover provider
	private static final int arrowMax = 2;
	private Transition arrowTransition;
	private Object selection;
	protected final Color backgroundColor;
	protected final Color outlineColor;
	protected final Color highlightColor;
	protected final Color white;
	private boolean mouseOver;
	private Label currentIcon;
	private Label currentLabel;
	private Shell popup;
	private LaunchBarListViewer listViewer;
	private Job delayJob;
	private MouseTrackListener mouseTrackListener = new MouseTrackListener() {
		@Override
		public void mouseEnter(MouseEvent e) {
			if (!mouseOver) {
				mouseOver = true;
				redraw();
				if (toolTipWasModified) {
					buttonComposite.setToolTipText(toolTipText);
					if (currentLabel != null) {
						currentLabel.setToolTipText(toolTipText);
					}
					if (currentIcon != null) {
						currentIcon.setToolTipText(toolTipText);
					}
				}
			}
		}

		@Override
		public void mouseHover(MouseEvent e) {
			if (hoverProvider != null && (popup == null || popup.isDisposed())) {
				final Object eventSource = e.getSource();
				if ((eventSource == currentLabel || eventSource == buttonComposite || eventSource == currentIcon)) {
					if (hoverProvider.displayHover(selection)) {
						buttonComposite.setToolTipText(""); //$NON-NLS-1$
						if (currentLabel != null) {
							currentLabel.setToolTipText(""); //$NON-NLS-1$
						}
						if (currentIcon != null) {
							currentIcon.setToolTipText(""); //$NON-NLS-1$
						}
						toolTipWasModified = true;
					}
				}
			}
		}

		@Override
		public void mouseExit(MouseEvent e) {
			if (mouseOver) {
				mouseOver = false;
				redraw();
			}
		}
	};
	private MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mouseUp(MouseEvent event) {
			if (popup == null || popup.isDisposed()) {
				setFocus();
				openPopup();
			} else {
				closePopup();
			}
		}
	};

	protected boolean myIsFocusAncestor(Control control) {
		while (control != null && control != this && !(control instanceof Shell)) {
			control = control.getParent();
		}
		return control == this;
	}

	private Listener focusOutListener = new Listener() {
		private Job closingJob;

		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.FocusIn:
				if (closingJob != null)
					closingJob.cancel();
				if (event.widget instanceof Control && myIsFocusAncestor((Control) event.widget)) {
					break; // not closing
				}
				if (!isPopUpInFocus()) {
					closePopup();
				}
				break;
			case SWT.FocusOut:
				if (isPopUpInFocus()) {
					// we about to loose focus from popup children, but it may go
					// to another child, lets schedule a job to wait before we close
					if (closingJob != null)
						closingJob.cancel();
					closingJob = new Job(Messages.CSelector_0) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;

							closePopup();
							closingJob = null;
							return Status.OK_STATUS;
						}
					};
					closingJob.schedule(300);
				}
				break;
			case SWT.MouseUp: {
				if (popup != null && !popup.isDisposed()) {
					Point loc = getDisplay().getCursorLocation();
					if (!popup.getBounds().contains(loc) && !getBounds().contains(getParent().toControl(loc))) {
						closePopup();
					}
				}
				break;
			}
			}
		}

	};

	public CSelector(Composite parent, int style) {
		super(parent, style);
		backgroundColor = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		outlineColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		highlightColor = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		white = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		GridLayout mainButtonLayout = new GridLayout();
		setLayout(mainButtonLayout);
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setBackground(backgroundColor);
				gc.setForeground(outlineColor);
				Point size = getSize();
				final int arc = 3;
				gc.fillRoundRectangle(0, 0, size.x - 1, size.y - 1, arc, arc);
				gc.drawRoundRectangle(0, 0, size.x - 1, size.y - 1, arc, arc);
			}
		});
		addMouseListener(mouseListener);
		addMouseTrackListener(mouseTrackListener);
	}

	private boolean isPopUpInFocus() {
		Control focusControl = getDisplay().getFocusControl();
		if (focusControl != null && focusControl.getShell() == popup) {
			return true;
		}
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (popup != null)
			popup.dispose();
	}

	public void setDelayedSelection(final Object element, long millis) {
		if (delayJob != null)
			delayJob.cancel();
		delayJob = new Job(Messages.CSelector_1) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (isDisposed())
					return Status.CANCEL_STATUS;
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (monitor.isCanceled())
							return;
						setSelection(element);
					}
				});
				return Status.OK_STATUS;
			}
		};
		delayJob.schedule(millis);
	}

	public void setSelection(Object element) {
		if (isDisposed())
			return;
		this.selection = element;
		if (buttonComposite != null)
			buttonComposite.dispose();
		toolTipText = getToolTipText();
		boolean editable = false;
		int columns = 2;
		Image image = labelProvider.getImage(element);
		if (image != null)
			columns++;
		editable = isEditable(element);
		if (editable)
			columns++;
		buttonComposite = new Composite(this, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(columns, false);
		buttonLayout.marginHeight = buttonLayout.marginWidth = 0;
		buttonComposite.setLayout(buttonLayout);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		buttonComposite.setBackground(backgroundColor);
		buttonComposite.addMouseListener(mouseListener);
		buttonComposite.addMouseTrackListener(mouseTrackListener);
		buttonComposite.setToolTipText(toolTipText);
		if (element != null) {
			if (image != null) {
				Label icon = createImage(buttonComposite, image);
				icon.addMouseListener(mouseListener);
				icon.addMouseTrackListener(mouseTrackListener);
				currentIcon = icon;
				currentIcon.setToolTipText(toolTipText);
			}
			Label label = createLabel(buttonComposite, element);
			label.addMouseListener(mouseListener);
			label.addMouseTrackListener(mouseTrackListener);
			currentLabel = label;
			currentLabel.setToolTipText(toolTipText);
		} else {
			Composite blank = new Composite(buttonComposite, SWT.NONE);
			blank.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			currentIcon = null;
			currentLabel = null;
		}
		final Canvas arrow = new Canvas(buttonComposite, SWT.NONE) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return new Point(12, 16);
			}
		};
		arrow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		arrow.setBackground(backgroundColor);
		arrowTransition = new Transition(arrow, arrowMax, 80);
		arrow.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				final int hPadding = 2;
				GC gc = e.gc;
				LineAttributes attributes = new LineAttributes(2);
				attributes.cap = SWT.CAP_ROUND;
				gc.setLineAttributes(attributes);
				gc.setAlpha(mouseOver ? 255 : 100);
				Rectangle bounds = arrow.getBounds();
				int arrowWidth = bounds.width - hPadding * 2;
				int current = arrowTransition.getCurrent();
				gc.drawPolyline(new int[] { hPadding,
						bounds.height / 2 - current,
						hPadding + (arrowWidth / 2),
						bounds.height / 2 + current, hPadding + arrowWidth,
						bounds.height / 2 - current });
			}
		});
		arrow.addMouseListener(mouseListener);
		arrow.addMouseTrackListener(mouseTrackListener);
		if (editable) {
			final EditButton editButton = new EditButton(buttonComposite, SWT.NONE);
			editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
			editButton.setBackground(backgroundColor);
			editButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Need to run this after the current event storm
					// Or we get a disposed error.
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (CSelector.this.selection != null)
								handleEdit(selection);
						}
					});
				}
			});
		}
		layout();
	}

	protected abstract void fireSelectionChanged();

	public Object getSelection() {
		return selection;
	}

	public MouseListener getMouseListener() {
		return mouseListener;
	}

	public MouseTrackListener getMouseTrackListener() {
		return mouseTrackListener;
	}

	protected void openPopup() {
		Object[] elements = contentProvider.getElements(input);
		if (elements.length == 0 && !hasActionArea())
			return;
		arrowTransition.to(-arrowMax);
		if (popup != null && !popup.isDisposed()) {
			popup.dispose();
		}
		popup = new Shell(getShell(), SWT.TOOL | SWT.ON_TOP);
		popup.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());


		listViewer = new LaunchBarListViewer(popup);
		initializeListViewer(listViewer);
		listViewer.setFilterVisible(elements.length > 7);
		listViewer.setInput(input);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!listViewer.isFinalSelection())
					return;
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				if (!ss.isEmpty()) {
					setSelection(ss.getFirstElement());
					fireSelectionChanged();
				}
				closePopup();
			}
		});
		if (hasActionArea())
			createActionArea(popup);
		Rectangle buttonBounds = getBounds();
		Point popupLocation = popup.getDisplay().map(this, null, 0,
				buttonBounds.height);
		popup.setLocation(popupLocation.x, popupLocation.y + 5);
		Point size = popup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point buttonSize = getSize();
		size.x = Math.max(size.x, buttonSize.x);
		size.y = Math.min(size.y, 300);
		popup.setSize(size);
		popup.setVisible(true);
		popup.setFocus();
		getDisplay().addFilter(SWT.FocusIn, focusOutListener);
		getDisplay().addFilter(SWT.FocusOut, focusOutListener);
		getDisplay().addFilter(SWT.MouseUp, focusOutListener);
		popup.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getDisplay().removeFilter(SWT.FocusIn, focusOutListener);
				getDisplay().removeFilter(SWT.FocusOut, focusOutListener);
				getDisplay().removeFilter(SWT.MouseUp, focusOutListener);
			}
		});
		if (hoverProvider != null) {
			hoverProvider.dismissHover(selection != null ? selection : null, true);
		}
	}

	protected void initializeListViewer(LaunchBarListViewer listViewer) {
		listViewer.setContentProvider(contentProvider);
		listViewer.setLabelProvider(labelProvider);
		listViewer.setComparator(sorter);
		listViewer.setHistoryComparator(sorterTop);
	}

	private void closePopup() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (popup == null || popup.isDisposed())
					return;
				arrowTransition.to(arrowMax);
				popup.setVisible(false);
				popup.dispose();
			}
		});
	}

	private Label createImage(Composite parent, Image image) {
		Rectangle bounds = image.getBounds();
		boolean disposeImage = false;
		if (bounds.height > 16 || bounds.width > 16) {
			Image buttonImage = new Image(getDisplay(), 16, 16);
			GC gc = new GC(buttonImage);
			gc.setAntialias(SWT.ON);
			gc.setInterpolation(SWT.HIGH);
			gc.drawImage(image, 0, 0, image.getBounds().width,
					image.getBounds().height, 0, 0, 16, 16);
			gc.dispose();
			image = buttonImage;
			disposeImage = true;
		}
		Label icon = new Label(parent, SWT.NONE);
		icon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		icon.setImage(image);
		if (disposeImage) {
			final Image disposableImage = image;
			icon.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					disposableImage.dispose();
				}
			});
		}
		return icon;
	}

	private Label createLabel(Composite parent, Object element) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		label.setText(labelProvider.getText(element));
		label.setFont(getDisplay().getSystemFont());
		return label;
	}

	public void setContentProvider(IStructuredContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	public IStructuredContentProvider getContentProvider() {
		return contentProvider;
	}

	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	public void setHoverProvider(IHoverProvider hoverProvider) {
		this.hoverProvider = hoverProvider;
	}

	public IHoverProvider getHoverProvider() {
		return hoverProvider;
	}

	/**
	 * Set sorter for the bottom part of the selector
	 * 
	 * @param sorter
	 */
	public void setSorter(Comparator<?> sorter) {
		this.sorter = sorter;
	}

	/**
	 * Set sorter for the "history" part of the selector
	 * 
	 * @param sorter
	 */
	public void setHistorySortComparator(Comparator<?> sorter) {
		this.sorterTop = sorter;
	}

	public void setInput(Object input) {
		this.input = input;
	}

	public Object getInput() {
		return input;
	}

	public void refresh() {
		// TODO add any new ones to the popup if it's open
	}

	public void update(Object element) {
		if (selection == element) {
			if (currentIcon != null && !currentIcon.isDisposed()) {
				currentIcon.setImage(labelProvider.getImage(element));
			}
			if (currentLabel != null && !currentLabel.isDisposed()) {
				currentLabel.setText(labelProvider.getText(element));
			}
		}
		if (popup != null && !popup.isDisposed()) {
			listViewer.update(element, null);
		}
	}

	protected boolean hasActionArea() {
		return false;
	}

	protected void createActionArea(Composite parent) {
		// empty
	}

	protected boolean isEditable(Object element) {
		return false;
	}

	protected void handleEdit(Object element) {
		// nothing to do here
	}

}
