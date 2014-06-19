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
package org.eclipse.cdt.launchbar.ui.internal.controls;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.launchbar.ui.IHoverProvider;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
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

public class CSelector extends Composite implements ISelectionProvider {

	private IStructuredContentProvider contentProvider;
	private ILabelProvider labelProvider;
	private IHoverProvider hoverProvider;
	private Comparator<Object> sorter;

	private Object input;

	private Composite buttonComposite;
	private String toolTipText;
	private boolean toolTipWasModified; // Used for the hover provider

	private static final int arrowMax = 2;
	private Transition arrowTransition;

	private IStructuredSelection selection;
	private List<ISelectionChangedListener> selectionChangedListeners = new LinkedList<>();

	protected final Color backgroundColor;
	protected final Color outlineColor;
	protected final Color highlightColor;
	protected final Color white;

	private boolean mouseOver;

	private Image editImage;
	private boolean inEditButton;
	private Image buttonImage;
	private Label currentIcon;
	private Label currentLabel;

	private Shell popup;
	private ListItem listItems[];
	private int selIndex;
	private ScrolledComposite listScrolled;
	private final int itemH = 30;
	private int scrollBucket;
	private final int maxScrollBucket = 7;

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
					if (hoverProvider.displayHover(CSelector.this.selection.getFirstElement())) {
						buttonComposite.setToolTipText("");
						if (currentLabel != null) {
							currentLabel.setToolTipText("");
						}
						if (currentIcon != null) {
							currentIcon.setToolTipText("");
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
				openPopup();
			} else {
				closePopup();
			}
		}
	};

	private Listener focusOutListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.FocusOut:
				Control focusControl = getDisplay().getFocusControl();
				if (focusControl != null && focusControl.getShell() == popup) {
					Point loc = getDisplay().getCursorLocation();
					if (!getBounds().contains(toControl(loc))) {
						// Don't do it if we're in the selector, we'll deal with that later
						closePopup();
					}
				}
				break;

			case SWT.MouseUp:
				if (popup != null && !popup.isDisposed()) {
					Point loc = getDisplay().getCursorLocation();
					if (!popup.getBounds().contains(loc) && !getBounds().contains(toControl(loc))) {
						closePopup();
					}
				}
				break;
			}
		}
		
	};

	public CSelector(Composite parent, int style) {
		super(parent, style);

		backgroundColor = new Color(getDisplay(), new RGB(249, 249, 249));
		outlineColor = new Color(getDisplay(), new RGB(189, 195, 200));
		highlightColor = new Color(getDisplay(), new RGB(223, 239, 241));
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

	@Override
	public void dispose() {
		super.dispose();
		backgroundColor.dispose();
		outlineColor.dispose();
		highlightColor.dispose();
		if (editImage != null)
			editImage.dispose();
		if (buttonImage != null)
			buttonImage.dispose();
		if (popup != null)
			popup.dispose();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return;
		IStructuredSelection newSelection = (IStructuredSelection) selection;
		if (this.selection != null && newSelection.getFirstElement() == this.selection.getFirstElement())
			// Already selected
			return;
		
		this.selection = newSelection;

		if (buttonComposite != null)
			buttonComposite.dispose();

		toolTipText = getToolTipText();

		boolean editable = false;
		int columns = 2;
		Object element = null;
		element = this.selection.getFirstElement();

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
			Control editButton = createEditButton(buttonComposite, element);
			editButton.setBackground(backgroundColor);

			editButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					// Need to run this after the current event storm
					// Or we get a disposed error.
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (CSelector.this.selection != null)
								handleEdit(CSelector.this.selection.getFirstElement());
						}
					});
				}
			});
		}

		layout();
		fireSelectionChanged();
	}

	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
	}

	@Override
	public IStructuredSelection getSelection() {
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
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		popup.setLayout(layout);

		listScrolled = new ScrolledComposite(popup, SWT.V_SCROLL | SWT.NO_BACKGROUND);
		listScrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listScrolled.setExpandHorizontal(true);
		Composite listComp = new Composite(listScrolled, SWT.NONE);
		listScrolled.setContent(listComp);
		layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		listComp.setLayout(layout);

		for (Control child : listComp.getChildren())
			child.dispose();

		Arrays.sort(elements, sorter);

		listItems = new ListItem[elements.length];

		int heightHint = 0;
		for (int i = 0; i < elements.length; ++i) {
			listItems[i] = new ListItem(listComp, SWT.NONE, elements[i], i);
			if (i < maxScrollBucket) { // this is how many visible by default
				listItems[i].lazyInit();
				if (i == 0) {
					heightHint = listItems[0].computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
				}
			} else {
				GridData gd = (GridData) listItems[i].getLayoutData();
				gd.heightHint = heightHint;
			}
		}

		Point listCompSize = listComp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		listComp.setSize(listCompSize);

		if (hasActionArea())
			createActionArea(popup);

		Rectangle buttonBounds = getBounds();
		Point popupLocation = popup.getDisplay().map(this, null, 0,
				buttonBounds.height);
		popup.setLocation(popupLocation.x, popupLocation.y + 5);

		Point size = popup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point buttonSize = getSize();
		size.x = Math.min(size.x + 16, buttonSize.x * 4 / 3);
		size.y = Math.min(size.y, 250);
		popup.setSize(size);

		popup.setVisible(true);
		popup.setFocus();
		getDisplay().addFilter(SWT.FocusOut, focusOutListener);
		getDisplay().addFilter(SWT.MouseUp, focusOutListener);

		popup.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getDisplay().removeFilter(SWT.FocusOut, focusOutListener);
				getDisplay().removeFilter(SWT.MouseUp, focusOutListener);
			}
		});
		selIndex = -1;
		scrollBucket = 0;
		if (hoverProvider != null) {
			hoverProvider.dismissHover(selection != null ? selection.getFirstElement() : null, true);
		}
	}

	private void closePopup() {
		arrowTransition.to(arrowMax);
		popup.setVisible(false);
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				popup.dispose();
			}
		});
	}

	TraverseListener listItemTraverseListener = new TraverseListener() {
		@Override
		public void keyTraversed(TraverseEvent e) {
			final ListItem currItem = selIndex >=0 ? listItems[selIndex] : null;
			if (currItem == null && e.keyCode != SWT.ARROW_DOWN) {
				return;
			}
			if (e.detail == SWT.TRAVERSE_ARROW_NEXT || e.detail == SWT.TRAVERSE_TAB_NEXT) {
				if (inEditButton || e.keyCode == SWT.ARROW_DOWN) {
					int maxIdx = listItems.length -1;
					if (selIndex < maxIdx) {
						inEditButton = false;
						if (currItem != null)
							currItem.setBackground(white);
						// move to next item
						selIndex++;
						if (scrollBucket < maxScrollBucket) {
							scrollBucket++;
						} else {
							// need to scroll the list up 1 item
							int sY = listScrolled.getOrigin().y;
							listScrolled.setOrigin(0, sY + itemH);
						}
						listItems[selIndex].setBackground(highlightColor);
					} else if (selIndex == maxIdx && maxIdx > maxScrollBucket) {
						// level the scroll for any offset at the bottom of the list
						listScrolled.setOrigin(0, itemH * (maxIdx - maxScrollBucket +1));
					}
				} else if (currItem.editButton != null) {
					// move focus on edit button
					inEditButton = true;
					currItem.editButton.redraw();
				}
			} else if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
				if (!inEditButton || e.keyCode == SWT.ARROW_UP) {
					if (selIndex > 0) {
						inEditButton = false;
						currItem.setBackground(white);
						// move to previous item
						selIndex--;
						if (scrollBucket > 0) {
							scrollBucket--;
						} else {
							// need to scroll the list down 1 item
							int sY = listScrolled.getOrigin().y;
							listScrolled.setOrigin(0, sY - itemH);
						}
						listItems[selIndex].setBackground(highlightColor);
					} else if (selIndex == 0) {
						// level any offset @ beginning
						listScrolled.setOrigin(0, 0);
					}
				} else if (currItem.editButton != null) {
					// remove focus from edit button
					inEditButton = false;
					currItem.editButton.redraw();
				}
			} else if (e.detail == SWT.TRAVERSE_RETURN) {
				if (inEditButton) {
					inEditButton = false;
					// edit button in list item was pressed
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (CSelector.this.selection != null)
								handleEdit(currItem.element);
						}
					});
				} else {
					// list item was pressed
					popup.dispose();
					setSelection(new StructuredSelection(currItem.element));
				}
			} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
				popup.dispose();
			}
		}
	};

	private class ListItem extends Composite {
		protected final Object element;
		private Label icon;
		private Label label;
		protected Control editButton;
		private int index;

		public ListItem(Composite parent, int style, Object _element, int index) {
			super(parent, style);
			this.element = _element;
			this.index = index;

			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {

					Point size = getSize();
					GC gc = e.gc;
					gc.setForeground(outlineColor);
					gc.drawLine(0, size.y - 1, size.x, size.y - 1);
					if (label == null)
						lazyInit();
				}
			});

			// lazyInit();
		}	// end ListItem(..)

		protected void lazyInit() {
	        Image image = labelProvider.getImage(element);
			boolean editable = isEditable(element);

			int columns = 1;
			if (image != null)
				columns++;
			if (editable)
				columns++;

			GridLayout layout = new GridLayout(columns, false);
			layout.marginWidth = layout.marginHeight = 7;
			setLayout(layout);

			MouseListener listItemMouseListener = new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					popup.dispose();
					setSelection(new StructuredSelection(element));
				}
			};

			MouseTrackListener listItemMouseTrackListener = new MouseTrackAdapter() {
				@Override
				public void mouseEnter(MouseEvent e) {
					setBackground(highlightColor);
					int idx = getIndex();
					if (idx != selIndex) {
						if (selIndex >= 0) {
							listItems[selIndex].setBackground(white);
							scrollBucket = Math.max(Math.min(scrollBucket + idx - selIndex, maxScrollBucket), 0);
						} else { // initially
							scrollBucket = Math.min(idx,  maxScrollBucket);
						}
					}
					selIndex = idx;
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setBackground(white);
				}
			};

			addMouseListener(listItemMouseListener);
			addMouseTrackListener(listItemMouseTrackListener);

			if (image != null) {
				icon = createImage(this, image);
				icon.addMouseListener(listItemMouseListener);
				icon.addMouseTrackListener(listItemMouseTrackListener);
			}

			label = createLabel(this, element);
			label.addMouseListener(listItemMouseListener);
			label.addMouseTrackListener(listItemMouseTrackListener);

			if (editable) {
				editButton = createEditButton(this, element);
				editButton.setBackground(white);
				editButton.addMouseTrackListener(listItemMouseTrackListener);

				editButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseUp(MouseEvent e) {
						// Need to run this after the current event storm
						// Or we get a disposed error.
						getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (CSelector.this.selection != null)
									handleEdit(element);
							}
						});
					}
				});

				editButton.addTraverseListener(listItemTraverseListener);
			} else {
				addTraverseListener(listItemTraverseListener);
			}

			setBackground(white);

			layout(true);
        }

		@Override
		public void setBackground(Color color) {
			super.setBackground(color);

			if (icon != null && !icon.isDisposed())
				icon.setBackground(color);

			if (!label.isDisposed())
				label.setBackground(color);

			if (editButton != null && !editButton.isDisposed())
				editButton.setBackground(color);
		}

		public void setImage(Image image) {
			if (icon != null && !icon.isDisposed())
				icon.setImage(image);
		}

		public void setText(String text) {
			if (!label.isDisposed())
				label.setText(text);
		}

		protected int getIndex() {
			return index;
		}
	}  // end ListItem class

	private Label createImage(Composite parent, Image image) {
		Rectangle bounds = image.getBounds();
		boolean disposeImage = false;
		if (bounds.height > 16 || bounds.width > 16) {
			buttonImage = new Image(getDisplay(), 16, 16);
			GC gc = new GC(buttonImage);
			gc.setAntialias(SWT.ON);
			gc.setInterpolation(SWT.HIGH);
			gc.drawImage(image, 0, 0, image.getBounds().width,
					image.getBounds().height, 0, 0, 16, 16);
			gc.dispose();
			image = buttonImage;
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

	private Control createEditButton(Composite parent, Object element) {
		if (editImage == null) {
			editImage = Activator.getImageDescriptor("icons/config_config.png").createImage();
		}

		final Canvas editButton = new Canvas(parent, SWT.NONE) {
			@Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
				Rectangle bounds = editImage.getBounds();
				return new Point(bounds.width, bounds.height);
			};
		};

		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
		editButton.setToolTipText("Edit");

		editButton.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setAlpha(inEditButton ? 255 : 64);
				gc.drawImage(editImage, 0, 0);
			}
		});

		editButton.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				inEditButton = true;
				editButton.redraw();
			}

			@Override
			public void mouseExit(MouseEvent e) {
				inEditButton = false;
				editButton.redraw();
			}
		});

		return editButton;
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

	public void setSorter(Comparator<Object> sorter) {
		this.sorter = sorter;
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
		if (selection.getFirstElement() == element) {
			if (currentIcon != null && !currentIcon.isDisposed()) {
				currentIcon.setImage(labelProvider.getImage(element));
			}

			if (currentLabel != null && !currentLabel.isDisposed()) {
				currentLabel.setText(labelProvider.getText(element));
			}
		}

		if (popup != null && !popup.isDisposed()) {
			Object[] elements = contentProvider.getElements(input);
			int i;
			for (i = 0; i < elements.length; ++i)
				if (element == elements[i])
					break;

			if (i != elements.length) {
				listItems[i].setImage(labelProvider.getImage(element));
				listItems[i].setText(labelProvider.getText(element));
			}
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
