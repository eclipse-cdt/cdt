package org.eclipse.launchbar.ui.internal.controls;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Widget;

public class LaunchBarListViewer extends StructuredViewer {
	private ScrolledComposite listScrolled;
	private Composite listComposite;
	private ListItem[] listItems;
	private int selIndex;
	private int itemH = 30;
	private int scrollBucket;
	private final int maxScrollBucket = 6;
	private int separatorIndex = -1;
	private boolean historySupported = true;
	private ICellModifier modifier;
	private ViewerComparator historyComparator;
	private boolean finalSelection = false;
	private FilterControl filterControl;
	private Sash sash;
	private String historyPref;

	private static class LaunchBarListViewerComparator extends ViewerComparator {
		public LaunchBarListViewerComparator(Comparator<?> comp) {
			super(comp);
		}

		// have to override it because standard ViewerComparator compares by labels only
		@SuppressWarnings("unchecked")
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return getComparator().compare(e1, e2);
		}
	}

	private TraverseListener listItemTraverseListener = new TraverseListener() {
		@Override
		public void keyTraversed(TraverseEvent e) {
			final ListItem currItem = selIndex >= 0 ? listItems[selIndex] : null;
			if (currItem == null && e.keyCode != SWT.ARROW_DOWN) {
				return;
			}
			if (e.detail == SWT.TRAVERSE_ARROW_NEXT || e.detail == SWT.TRAVERSE_TAB_NEXT) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					int maxIdx = listItems.length - 1;
					if (selIndex < maxIdx) {
						// move to next item
						listItems[selIndex + 1].setSelected(true);
						if (scrollBucket < maxScrollBucket) {
							scrollBucket++;
						} else {
							// need to scroll the list up 1 item
							int sY = listScrolled.getOrigin().y;
							listScrolled.setOrigin(0, sY + itemH);
						}
					} else if (selIndex == maxIdx && maxIdx > maxScrollBucket) {
						// level the scroll for any offset at the bottom of the list
						listScrolled.setOrigin(0, itemH * (maxIdx - maxScrollBucket + 1));
					}
				}
			} else if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
				if (e.keyCode == SWT.ARROW_UP) {
					if (selIndex > 0) {
						// move to previous item
						if (scrollBucket > 0) {
							scrollBucket--;
						} else {
							// need to scroll the list down 1 item
							int sY = listScrolled.getOrigin().y;
							listScrolled.setOrigin(0, sY - itemH);
						}
						listItems[selIndex - 1].setSelected(true);
					} else if (selIndex == 0) {
						// level any offset @ beginning
						listScrolled.setOrigin(0, 0);
					}
				} else if (currItem.editButton != null) {
					// remove focus from edit button
					currItem.editButton.setSelected(false);
					currItem.editButton.redraw();
				}
			} else if (e.detail == SWT.TRAVERSE_RETURN) {
				setDefaultSelection(new StructuredSelection(currItem.element));
			} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
				setDefaultSelection(new StructuredSelection());
			}
		}
	};

	private KeyListener lisItemKeyListener = new KeyListener() {
		@Override
		public void keyReleased(KeyEvent e) {
			// ignore
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.character != 0 && e.character >= 0x20 && !filterControl.isVisible()) {
				if (listItems.length <= 1)
					return; // no filter for 1 item!
				// enable filter control and send the character there
				filterControl.setVisible(true);
				filterControl.setFocus();
				filterControl.getParent().layout(true);
				filterControl.getFilterText().setText(e.character + ""); //$NON-NLS-1$
				filterControl.getFilterText().setSelection(1);
			} else if (e.character == SWT.ESC) {
				setDefaultSelection(new StructuredSelection());
			}
		}
	};


	private class ListItem extends Composite {
		protected final Object element;
		private Label icon;
		private Label label;
		protected EditButton editButton;
		private int index;
		private Color backgroundColor = getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		private Color outlineColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		private Color highlightColor = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		private ILabelProvider labelProvider;
		private ICellModifier modifer;

		@Override
		public String toString() {
			return "[" + index + "] " + labelProvider.getText(element); //$NON-NLS-1$ //$NON-NLS-2$
		}
		public ListItem(Composite parent, int style, Object element, int index, ILabelProvider labelProvider,
				ICellModifier modifier) {
			super(parent, style);
			this.element = element;
			this.index = index;
			this.labelProvider = labelProvider;
			this.modifer = modifier;
			setData(element);
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
		} // end ListItem(..)

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
					setDefaultSelection(new StructuredSelection(element));
				}
			};
			MouseTrackListener listItemMouseTrackListener = new MouseTrackAdapter() {
				@Override
				public void mouseEnter(MouseEvent e) {
					setSelected(true);
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setSelected(false);
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
				editButton = new EditButton(this, SWT.NONE);
				editButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Need to run this after the current event storm
						// Or we get a disposed error.
						getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (editButton.isSelected())
									handleEdit(element);
							}
						});
					}
				});
				editButton.setBackground(backgroundColor);
				editButton.addMouseTrackListener(listItemMouseTrackListener);
				editButton.addTraverseListener(listItemTraverseListener);
				editButton.addKeyListener(lisItemKeyListener);
			} else {
				// add traverse listnener to control which will have keyboard focus
				addTraverseListener(listItemTraverseListener);
				addKeyListener(lisItemKeyListener);
			}

			setBackground(backgroundColor);
			layout(true);
		}

		public void setSelected(boolean selected) {
			if (selected) {
				setBackground(highlightColor);
				int idx = getIndex();
				if (idx != selIndex) {
					if (selIndex >= 0) {
						listItems[selIndex].setBackground(backgroundColor);
						scrollBucket = Math.max(Math.min(scrollBucket + idx - selIndex, maxScrollBucket), 0);
					} else { // initially
						scrollBucket = Math.min(idx, maxScrollBucket);
					}
				}
				selIndex = idx;
			} else {
				setBackground(backgroundColor);
			}
			if (editButton != null) {
				editButton.setSelected(selected);
			}
		}

		protected boolean isEditable(Object element) {
			if (modifer != null) {
				return modifer.canModify(element, null);
			}
			return false;
		}

		protected void handleEdit(Object element) {
			if (modifer != null) {
				modifer.modify(element, null, null);
			}
		}

		@Override
		public void setBackground(Color color) {
			super.setBackground(color);
			if (icon != null && !icon.isDisposed())
				icon.setBackground(color);
			if (label != null && !label.isDisposed())
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

		@Override
		public boolean setFocus() {
			super.setFocus();
			return true;
		}

		protected int getIndex() {
			return index;
		}

		private Label createImage(Composite parent, Image image) {
			Rectangle bounds = image.getBounds();
			boolean disposeImage = false;
			if (bounds.height > 16 || bounds.width > 16) {
				Image buttonImage = new Image(getDisplay(), 16, 16);
				GC gc = new GC(buttonImage);
				gc.setAntialias(SWT.ON);
				gc.setInterpolation(SWT.HIGH);
				// resize to 16 pixels
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
			ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
			label.setText(labelProvider.getText(element));
			if (labelProvider instanceof IFontProvider) {
				label.setFont(((IFontProvider) labelProvider).getFont(element));
			}
			return label;
		}
	} // end ListItem class

	public LaunchBarListViewer(Composite parent) {
		filterControl = new FilterControl(parent);
		listScrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.NO_BACKGROUND);
		listScrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listScrolled.setExpandHorizontal(true);
		listComposite = new Composite(listScrolled, SWT.NONE);
		listScrolled.setContent(listComposite);
		listComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
		selIndex = -1;
		scrollBucket = 0;
		filterControl.attachListViewer(this);
		historySupported = false;
		setHistoryPreferenceName(getHistoryPreferenceName());
	}

	private void createSash(final Composite listComp) {
		if (separatorIndex < 0 || !historySupported)
			return;
		sash = new Sash(listComp, SWT.BORDER | SWT.HORIZONTAL);
		sash.setLayoutData(GridDataFactory.fillDefaults().create());
		if (separatorIndex < listItems.length)
			sash.moveAbove(listItems[separatorIndex]);
		else
			sash.moveBelow(null);
		sash.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				separatorIndex = (e.y + itemH / 2) / itemH;
			}
		});
		sash.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				setSeparatorIndex(separatorIndex); // call setter if it was overriden
				if (separatorIndex >= 0) {
					if (separatorIndex < listItems.length)
						sash.moveAbove(listItems[separatorIndex]);
					else
						sash.moveBelow(null);
					listComp.layout();
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				sash.moveAbove(null); // keep on top so user see it when moving
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// ignore
			}
		});
		sash.setToolTipText(Messages.LaunchBarListViewer_0);
	}

	@Override
	public Control getControl() {
		return listScrolled;
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		refreshAll();
	}

	protected void refreshAll() {
		selIndex = -1;
		Control[] children = listComposite.getChildren();
		for (Control control : children) {
			control.dispose();
		}
		Object[] origElements = getElements();
		Object[] elements = filterElements(origElements);
		listItems = new ListItem[elements.length];
		if (elements.length > 0) {
			listItems[0] = createListItem(elements, 0);
			itemH = Math.max(listItems[0].computeSize(SWT.DEFAULT, SWT.DEFAULT).y, 16);
			for (int i = 1; i < elements.length; ++i) {
				listItems[i] = createListItem(elements, i);
			}
			createSash(listComposite);
		}
		listComposite.pack(true);
		listComposite.layout(true, true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		if (elements.length > maxScrollBucket) {
			Rectangle bounds = listItems[maxScrollBucket].getBounds();
			gd.heightHint = Math.max(bounds.y + bounds.height, itemH * (maxScrollBucket + 1));
		}
		listScrolled.setLayoutData(gd);
		listScrolled.layout(true);
	}

	private ListItem createListItem(Object[] elements, int i) {
		ListItem item = new ListItem(listComposite, SWT.NONE, elements[i], i, (ILabelProvider) getLabelProvider(), modifier);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		item.setLayoutData(gd);
		if (i <= maxScrollBucket) { // this is how many visible by default
			item.lazyInit();
		} else {
			gd.heightHint = itemH;
		}
		return item;
	}

	@Override
	protected Widget doFindInputItem(Object element) {
		return doFindItem(element);
	}

	@Override
	protected Widget doFindItem(Object element) {
		if (listItems == null)
			return null;
		for (ListItem listItem : listItems) {
			if (listItem.element.equals(element))
				return listItem;
		}
		return null;
	}

	@Override
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		if (item instanceof ListItem) {
			((ListItem) item).lazyInit();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected List getSelectionFromWidget() {
		ArrayList<Object> arrayList = new ArrayList<>();
		if (selIndex >= 0)
			arrayList.add(listItems[selIndex].element);
		return arrayList;
	}

	@Override
	protected void internalRefresh(Object element) {
		if (element == null || element == getRoot()) {
			refreshAll();
			return;
		}
		ListItem item = (ListItem) doFindItem(element);
		ILabelProvider lp = (ILabelProvider) getLabelProvider();
		if (lp == null || item == null)
			return;
		item.setImage(lp.getImage(element));
		item.setText(lp.getText(element));
	}

	private Object[] filterElements(Object[] elements) {
		Object[] topElements = elements.clone();
		if (getComparator() != null)
			getComparator().sort(this, elements);
		if (getTopComparator() != null)
			getTopComparator().sort(this, topElements);
		// only bottom part will be filtered
		Object[] result = elements;
		if (getFilters() != null) {
			for (ViewerFilter f : getFilters()) {
				result = f.filter(this, (Object) null, result);
			}
		}
		if (separatorIndex <= 0 || !historySupported)
			return result;
		if (separatorIndex >= topElements.length) {
			return topElements; // all elements will fit in top elements
		}
		ILaunchDescriptor[] descsCopy = new ILaunchDescriptor[separatorIndex + result.length];
		System.arraycopy(topElements, 0, descsCopy, 0, separatorIndex); // copy first N elements
		System.arraycopy(result, 0, descsCopy, separatorIndex, result.length); // copy all into rest
		return descsCopy;
	}

	private Object[] getElements() {
		IStructuredContentProvider cp = (IStructuredContentProvider) getContentProvider();
		if (cp == null)
			return new Object[0];
		Object[] elements = cp.getElements(getInput());
		return elements;
	}

	@Override
	public void reveal(Object element) {
		// TODO Auto-generated method stub
	}

	public void setDefaultSelection(StructuredSelection selection) {
		finalSelection = true;
		setSelection(selection, true);
	}

	@Override
	protected void setSelectionToWidget(@SuppressWarnings("rawtypes") List l, boolean reveal) {
		if (l.size() == 0) {
			return;
		}
		Object sel = l.get(0);
		Widget wid = doFindItem(sel);
		if (wid instanceof ListItem) {
			ListItem listItem = (ListItem) wid;
			listItem.setSelected(true);
		}
	}

	public int getSeparatorIndex() {
		return separatorIndex;
	}

	public void setSeparatorIndex(int separatorIndex) {
		this.separatorIndex = separatorIndex;
		if (separatorIndex <= 0)
			return;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String prefName = getHistoryPreferenceName();
		if (prefName != null && store.getInt(prefName) != getSeparatorIndex()) {
			store.setValue(prefName, getSeparatorIndex());
		}
	}

	protected String getHistoryPreferenceName() {
		return historyPref;
	}

	public void setCellModifier(ICellModifier modifier) {
		this.modifier = modifier;
	}

	public int getItemCount() {
		return listItems.length;
	}

	/**
	 * Returns top element (provider element) in the begging on non-history list
	 * 
	 * @return
	 */
	public Object getTopFilteredElement() {
		if (listItems.length > 0) {
			if (separatorIndex <= 0 || separatorIndex >= listItems.length || !historySupported)
				return listItems[0].element;
			else
				return listItems[separatorIndex].element;
		}
		return null;
	}

	public Object getTopElement() {
		if (listItems.length > 0) {
			return listItems[0].element;
		}
		return null;
	}

	public ViewerComparator getTopComparator() {
		return historyComparator;
	}

	/**
	 * ViewerComparator comparator labels of elements by default
	 * 
	 * @param comp
	 */
	public void setHistoryComparator(ViewerComparator comp) {
		historyComparator = comp;
	}

	public void setHistoryComparator(Comparator<?> comp) {
		historyComparator = comp == null ? null : new LaunchBarListViewerComparator(comp);
	}

	public void setComparator(Comparator<?> comp) {
		setComparator(comp == null ? null : new LaunchBarListViewerComparator(comp));
	}

	public boolean isHistorySupported() {
		return historySupported;
	}

	public void setHistorySupported(boolean historySupported) {
		this.historySupported = historySupported;
	}

	public void setHistoryPreferenceName(String historyPreferenceName) {
		this.historyPref = historyPreferenceName;
		if (historyPreferenceName != null) {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			int separator = store.getInt(historyPreferenceName);
			if (separator <= 0)
				separator = 1;
			setSeparatorIndex(separator);
		}
	}

	/**
	 * final selection will be set to true when user made a final selection in a list for example when double click on entry or
	 * pressed enter key
	 */
	public boolean isFinalSelection() {
		return finalSelection;
	}

	public void setFinalSelection(boolean finalSelection) {
		this.finalSelection = finalSelection;
	}

	public void setFocus() {
		if (selIndex >= 0 && listItems != null && listItems.length < selIndex)
			listItems[selIndex].setFocus();
		else
			getControl().setFocus();
	}

	public void setFilterVisible(boolean vis) {
		filterControl.setVisible(vis);
	}
}
