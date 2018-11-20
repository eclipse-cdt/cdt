/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     William Riley (Renesas) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension5;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * A control that can display a number of annotations. The control can decide how it layouts the
 * annotations to present them to the user.
 * <p>
 * This class was copied from org.eclipse.jdt.internal.ui.text.java.hover.AnnotationExpansionControl
 * </p>
 * <p>Each annotation can have its custom context menu and hover.</p>
 *
 * @since 6.1
 */
public class AnnotationExpansionControl implements IInformationControl, IInformationControlExtension,
		IInformationControlExtension2, IInformationControlExtension5 {

	public interface ICallback {
		void run(IInformationControlExtension2 control);
	}

	/**
	 * Input used by the control to display the annotations.
	 * TODO move to top-level class
	 * TODO encapsulate fields
	 *
	 * @since 6.1
	 */
	public static class AnnotationHoverInput {
		public Annotation[] fAnnotations;
		public ISourceViewer fViewer;
		public IVerticalRulerInfo fRulerInfo;
		public IVerticalRulerListener fAnnotationListener;
		public IDoubleClickListener fDoubleClickListener;
		public ICallback redoAction;
		public IAnnotationModel model;
	}

	private final class Item {
		Annotation fAnnotation;
		Canvas canvas;
		StyleRange[] oldStyles;

		public void selected() {
			Display disp = fShell.getDisplay();
			canvas.setCursor(getHandCursor(disp));
			// TODO: shade - for now: set grey background
			canvas.setBackground(getSelectionColor(disp));

			// highlight the viewer background at its position
			oldStyles = setViewerBackground(fAnnotation);

			// set the selection
			fSelection = this;

			if (fHoverManager == null) {
				fHoverManager = createHoverManager(fComposite, true);
			} else {
				fHoverManager.showInformation();
			}

			if (fInput.fAnnotationListener != null) {
				VerticalRulerEvent event = new VerticalRulerEvent(fAnnotation);
				fInput.fAnnotationListener.annotationSelected(event);
			}

		}

		public void defaultSelected(MouseEvent e) {
			if (fInput.fAnnotationListener != null) {
				Event swtEvent = new Event();
				swtEvent.type = SWT.MouseDown;
				swtEvent.display = e.display;
				swtEvent.widget = e.widget;
				swtEvent.time = e.time;
				swtEvent.data = e.data;
				swtEvent.x = e.x;
				swtEvent.y = e.y;
				swtEvent.button = e.button;
				swtEvent.stateMask = e.stateMask;
				swtEvent.count = e.count;
				VerticalRulerEvent event = new VerticalRulerEvent(fAnnotation, swtEvent);
				fInput.fAnnotationListener.annotationDefaultSelected(event);
			}

			dispose();
		}

		public void deselect() {
			// hide the popup
			//			fHoverManager.disposeInformationControl();

			// deselect
			fSelection = null;

			resetViewerBackground(oldStyles);
			oldStyles = null;

			Display disp = fShell.getDisplay();
			canvas.setCursor(null);
			// TODO: remove shading - for now: set standard background
			canvas.setBackground(disp.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		}

	}

	/**
	 * Disposes of an item
	 */
	private final static class MyDisposeListener implements DisposeListener {
		/*
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		@Override
		public void widgetDisposed(DisposeEvent e) {
			Item item = (Item) ((Widget) e.getSource()).getData();
			item.deselect();
			item.canvas = null;
			item.fAnnotation = null;
			item.oldStyles = null;

			((Widget) e.getSource()).setData(null);
		}
	}

	/**
	 * Listener on context menu invocation on the items
	 */
	private final class MyMenuDetectListener implements Listener {
		/*
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		@Override
		public void handleEvent(Event event) {
			if (event.type == SWT.MenuDetect) {
				// TODO: show per-item menu
				// for now: show ruler context menu
				if (fInput != null) {
					Control ruler = fInput.fRulerInfo.getControl();
					if (ruler != null && !ruler.isDisposed()) {
						Menu menu = ruler.getMenu();
						if (menu != null && !menu.isDisposed()) {
							menu.setLocation(event.x, event.y);
							menu.addMenuListener(new MenuListener() {

								@Override
								public void menuHidden(MenuEvent e) {
									dispose();
								}

								@Override
								public void menuShown(MenuEvent e) {
								}

							});
							menu.setVisible(true);
						}
					}
				}
			}
		}
	}

	/**
	 * Listener on mouse events on the items.
	 */
	private final class MyMouseListener extends MouseAdapter {
		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			Item item = (Item) ((Widget) e.getSource()).getData();
			if (e.button == 1 && item.fAnnotation == fInput.fAnnotations[0] && fInput.fDoubleClickListener != null) {
				fInput.fDoubleClickListener.doubleClick(null);
				// special code for JDT to renew the annotation set.
				if (fInput.redoAction != null)
					fInput.redoAction.run(AnnotationExpansionControl.this);
			}
			//			dispose();
			// TODO special action to invoke double-click action on the vertical ruler
			// how about
			//					Canvas can= (Canvas) e.getSource();
			//					Annotation a= (Annotation) can.getData();
			//					if (a != null) {
			//						a.getDoubleClickAction().run();
			//					}
		}

		/*
		 * JDT uses mouseDown here rather than mouseUp to fix a bug
		 * (details see https://bugs.eclipse.org/bugs/show_bug.cgi?id=165533)
		 *
		 * However this causes an issue where the top annotation is fired if the user
		 * click the 1st item in the expansion control. Due to mouseUp going to the ruler
		 * after the expansion is close.
		 *
		 * Bug as described in JDT does not seems to affect CDT so reverting to mouseUp.
		 *
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent e) {
			Item item = (Item) ((Widget) e.getSource()).getData();
			// TODO for now, to make double click work: disable single click on the first item
			// disable later when the annotationlistener selectively handles input
			if (item != null && e.button == 1) // && item.fAnnotation != fInput.fAnnotations[0])
				item.defaultSelected(e);
		}

	}

	/**
	 * Listener on mouse track events on the items.
	 */
	private final class MyMouseTrackListener implements MouseTrackListener {
		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseEnter(MouseEvent e) {
			Item item = (Item) ((Widget) e.getSource()).getData();
			if (item != null)
				item.selected();
		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseExit(MouseEvent e) {

			Item item = (Item) ((Widget) e.getSource()).getData();
			if (item != null)
				item.deselect();

			// if the event lies outside the entire popup, dispose
			org.eclipse.swt.graphics.Region region = fShell.getRegion();
			Canvas can = (Canvas) e.getSource();
			Point p = can.toDisplay(e.x, e.y);
			if (region == null) {
				Rectangle bounds = fShell.getBounds();
				//				p= fShell.toControl(p);
				if (!bounds.contains(p))
					dispose();
			} else {
				p = fShell.toControl(p);
				if (!region.contains(p))
					dispose();
			}

		}

		/*
		 * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseHover(MouseEvent e) {
			if (fHoverManager == null) {
				fHoverManager = createHoverManager(fComposite, true);
			}
		}
	}

	private HoverManager createHoverManager(Composite target, boolean show) {
		HoverManager hoverManager = new HoverManager();
		hoverManager.takesFocusWhenVisible(false);
		hoverManager.install(target);
		if (show) {
			hoverManager.showInformation();
		}
		return hoverManager;
	}

	/**
	 * @since 6.1
	 */
	public class LinearLayouter {

		private static final int ANNOTATION_SIZE = 14;
		private static final int BORDER_WIDTH = 2;

		public Layout getLayout(int itemCount) {
			// simple layout: a row of items
			GridLayout layout = new GridLayout(itemCount, true);
			layout.horizontalSpacing = 1;
			layout.verticalSpacing = 0;
			layout.marginHeight = 1;
			layout.marginWidth = 1;
			return layout;
		}

		public Object getLayoutData() {
			GridData gridData = new GridData(ANNOTATION_SIZE + 2 * BORDER_WIDTH, ANNOTATION_SIZE + 2 * BORDER_WIDTH);
			gridData.horizontalAlignment = GridData.CENTER;
			gridData.verticalAlignment = GridData.CENTER;
			return gridData;
		}

		public int getAnnotationSize() {
			return ANNOTATION_SIZE;
		}

		public int getBorderWidth() {
			return BORDER_WIDTH;
		}

		/**
		 * Gets the shell region for the given number of items.
		 *
		 * @param itemCount the item count
		 * @return the shell region
		 */
		public org.eclipse.swt.graphics.Region getShellRegion(int itemCount) {
			// no special region - set to null for default shell size
			return null;
		}

	}

	/**
	 * Listener on paint events on the items. Paints the annotation image on the given <code>GC</code>.
	 */
	private final class MyPaintListener implements PaintListener {
		/*
		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
		 */
		@Override
		public void paintControl(PaintEvent e) {
			Canvas can = (Canvas) e.getSource();
			Annotation a = ((Item) can.getData()).fAnnotation;
			if (a != null) {
				Rectangle rect = new Rectangle(fLayouter.getBorderWidth(), fLayouter.getBorderWidth(),
						fLayouter.getAnnotationSize(), fLayouter.getAnnotationSize());
				if (fAnnotationAccessExtension != null)
					fAnnotationAccessExtension.paint(a, e.gc, can, rect);
			}
		}
	}

	/**
	 * Our own private hover manager used to shop per-item pop-ups.
	 */
	private final class HoverManager extends AbstractInformationControlManager {

		/**
		 *
		 */
		public HoverManager() {
			super(new IInformationControlCreator() {
				@Override
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent);
				}
			});

			setMargins(5, 10);
			setAnchor(ANCHOR_BOTTOM);
			setFallbackAnchors(new Anchor[] { ANCHOR_BOTTOM, ANCHOR_LEFT, ANCHOR_RIGHT });
		}

		/*
		 * @see org.eclipse.jface.text.AbstractInformationControlManager#computeInformation()
		 */
		@Override
		protected void computeInformation() {
			if (fSelection != null) {
				Rectangle subjectArea = fSelection.canvas.getBounds();
				Annotation annotation = fSelection.fAnnotation;
				String msg;
				if (annotation != null)
					msg = annotation.getText();
				else
					msg = null;

				setInformation(msg, subjectArea);
			}
		}

	}

	/** Model data. */
	protected AnnotationHoverInput fInput;
	/** The control's shell */
	private Shell fShell;
	/** The composite combining all the items. */
	protected Composite fComposite;
	/** The currently selected item, or <code>null</code> if none is selected. */
	private Item fSelection;
	/** The hover manager for the per-item hovers. */
	private HoverManager fHoverManager;
	/** The annotation access extension. */
	private IAnnotationAccessExtension fAnnotationAccessExtension;

	/* listener legion */
	private final MyPaintListener fPaintListener;
	private final MyMouseTrackListener fMouseTrackListener;
	private final MyMouseListener fMouseListener;
	private final MyMenuDetectListener fMenuDetectListener;
	private final DisposeListener fDisposeListener;
	private final IViewportListener fViewportListener;

	private LinearLayouter fLayouter;

	/**
	 * Creates a new control.
	 *
	 * @param parent parent shell
	 * @param shellStyle additional style flags
	 * @param access the annotation access
	 */
	public AnnotationExpansionControl(Shell parent, int shellStyle, IAnnotationAccess access) {
		fPaintListener = new MyPaintListener();
		fMouseTrackListener = new MyMouseTrackListener();
		fMouseListener = new MyMouseListener();
		fMenuDetectListener = new MyMenuDetectListener();
		fDisposeListener = new MyDisposeListener();
		fViewportListener = new IViewportListener() {

			@Override
			public void viewportChanged(int verticalOffset) {
				dispose();
			}

		};
		fLayouter = new LinearLayouter();

		if (access instanceof IAnnotationAccessExtension)
			fAnnotationAccessExtension = (IAnnotationAccessExtension) access;

		fShell = new Shell(parent, shellStyle | SWT.NO_FOCUS | SWT.ON_TOP);
		Display display = fShell.getDisplay();
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		fComposite = new Composite(fShell, SWT.NO_FOCUS | SWT.NO_REDRAW_RESIZE | SWT.NO_TRIM);
		//		fComposite= new Composite(fShell, SWT.NO_FOCUS | SWT.NO_REDRAW_RESIZE | SWT.NO_TRIM | SWT.V_SCROLL);

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fShell.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = fLayouter.getAnnotationSize() + 2 * fLayouter.getBorderWidth() + 4;
		fComposite.setLayoutData(data);
		fComposite.addMouseTrackListener(new MouseTrackAdapter() {

			@Override
			public void mouseExit(MouseEvent e) {
				if (fComposite == null)
					return;
				Control[] children = fComposite.getChildren();
				Rectangle bounds = null;
				for (int i = 0; i < children.length; i++) {
					if (bounds == null)
						bounds = children[i].getBounds();
					else
						bounds.add(children[i].getBounds());
					if (bounds.contains(e.x, e.y))
						return;
				}

				// if none of the children contains the event, we leave the popup
				dispose();
			}

		});

		//		fComposite.getVerticalBar().addListener(SWT.Selection, new Listener() {
		//
		//			public void handleEvent(Event event) {
		//				Rectangle bounds= fShell.getBounds();
		//				int x= bounds.x - fLayouter.getAnnotationSize() - fLayouter.getBorderWidth();
		//				int y= bounds.y;
		//				fShell.setBounds(x, y, bounds.width, bounds.height);
		//			}
		//
		//		});

		Cursor handCursor = getHandCursor(display);
		fShell.setCursor(handCursor);
		fComposite.setCursor(handCursor);

		setInfoSystemColor();
	}

	private void setInfoSystemColor() {
		Display display = fShell.getDisplay();
		setForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		setBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
	 */
	@Override
	public void setInformation(String information) {
		setInput(null);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension2#setInput(java.lang.Object)
	 */
	@Override
	public void setInput(Object input) {
		if (fInput != null && fInput.fViewer != null)
			fInput.fViewer.removeViewportListener(fViewportListener);

		if (input instanceof AnnotationHoverInput)
			fInput = (AnnotationHoverInput) input;
		else
			fInput = null;

		inputChanged(fInput, null);
	}

	/**
	 * Internal hook method called when the input is
	 * initially set or subsequently changed.
	 *
	 * @param newInput the new input
	 * @param newSelection  the new selection
	 */
	protected void inputChanged(Object newInput, Object newSelection) {
		refresh();
	}

	protected void refresh() {
		adjustItemNumber();

		if (fInput == null)
			return;

		if (fInput.fAnnotations == null)
			return;

		if (fInput.fViewer != null)
			fInput.fViewer.addViewportListener(fViewportListener);

		fShell.setRegion(fLayouter.getShellRegion(fInput.fAnnotations.length));

		Layout layout = fLayouter.getLayout(fInput.fAnnotations.length);
		fComposite.setLayout(layout);

		Control[] children = fComposite.getChildren();
		for (int i = 0; i < fInput.fAnnotations.length; i++) {
			Canvas canvas = (Canvas) children[i];
			Item item = new Item();
			item.canvas = canvas;
			item.fAnnotation = fInput.fAnnotations[i];
			canvas.setData(item);
			canvas.redraw();
		}

	}

	protected void adjustItemNumber() {
		if (fComposite == null)
			return;

		Control[] children = fComposite.getChildren();
		int oldSize = children.length;
		int newSize = fInput == null ? 0 : fInput.fAnnotations.length;

		Display display = fShell.getDisplay();

		// add missing items
		for (int i = oldSize; i < newSize; i++) {
			Canvas canvas = new Canvas(fComposite, SWT.NONE);
			Object gridData = fLayouter.getLayoutData();
			canvas.setLayoutData(gridData);
			canvas.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			canvas.addPaintListener(fPaintListener);

			canvas.addMouseTrackListener(fMouseTrackListener);

			canvas.addMouseListener(fMouseListener);

			canvas.addListener(SWT.MenuDetect, fMenuDetectListener);

			canvas.addDisposeListener(fDisposeListener);
		}

		// dispose of exceeding resources
		for (int i = oldSize; i > newSize; i--) {
			Item item = (Item) children[i - 1].getData();
			item.deselect();
			children[i - 1].dispose();
		}

	}

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		fShell.setVisible(visible);
		if (visible) {
			/*
			 * Force 1st item to be selected when made visible
			 *
			 * This causes the tooltip to be displayed without additional
			 * delay.
			 */
			Control[] children = fComposite.getChildren();
			if (fHoverManager == null && children.length > 0) {
				Object data = children[0].getData();
				if (data instanceof Item) {
					((Item) data).selected();
				}
			}
		}
	}

	/*
	 * @see IInformationControl#dispose()
	 */
	@Override
	public void dispose() {
		if (fShell != null) {
			if (!fShell.isDisposed())
				fShell.dispose();
			fShell = null;
			fComposite = null;
			if (fHoverManager != null)
				fHoverManager.dispose();
			fHoverManager = null;
			fSelection = null;
		}
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
	 */
	@Override
	public boolean hasContents() {
		return fInput.fAnnotations != null && fInput.fAnnotations.length > 0;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControl#setSizeConstraints(int, int)
	 */
	@Override
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		//fMaxWidth= maxWidth;
		//fMaxHeight= maxHeight;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
	 */
	@Override
	public Point computeSizeHint() {
		return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	/*
	 * @see IInformationControl#setLocation(Point)
	 */
	@Override
	public void setLocation(Point location) {
		fShell.setLocation(location);
	}

	/*
	 * @see IInformationControl#setSize(int, int)
	 */
	@Override
	public void setSize(int width, int height) {
		fShell.setSize(width, height);
	}

	/*
	 * @see IInformationControl#addDisposeListener(DisposeListener)
	 */
	@Override
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#removeDisposeListener(DisposeListener)
	 */
	@Override
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	@Override
	public void setForegroundColor(Color foreground) {
		fComposite.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	@Override
	public void setBackgroundColor(Color background) {
		fComposite.setBackground(background);
	}

	/*
	 * @see IInformationControl#isFocusControl()
	 */
	@Override
	public boolean isFocusControl() {
		return fShell.getDisplay().getActiveShell() == fShell;
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	@Override
	public void setFocus() {
		fShell.forceFocus();
	}

	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	@Override
	public void addFocusListener(FocusListener listener) {
		fShell.addFocusListener(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	@Override
	public void removeFocusListener(FocusListener listener) {
		fShell.removeFocusListener(listener);
	}

	private StyleRange[] setViewerBackground(Annotation annotation) {
		StyledText text = fInput.fViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return null;

		Display disp = text.getDisplay();

		Position pos = fInput.model.getPosition(annotation);
		if (pos == null)
			return null;

		IRegion region = ((TextViewer) fInput.fViewer).modelRange2WidgetRange(new Region(pos.offset, pos.length));
		if (region == null)
			return null;

		StyleRange[] ranges = text.getStyleRanges(region.getOffset(), region.getLength());

		List<StyleRange> undoRanges = new ArrayList<>(ranges.length);
		for (int i = 0; i < ranges.length; i++) {
			undoRanges.add((StyleRange) ranges[i].clone());
		}

		int offset = region.getOffset();
		StyleRange current = undoRanges.size() > 0 ? undoRanges.get(0) : null;
		int curStart = current != null ? current.start : region.getOffset() + region.getLength();
		int curEnd = current != null ? current.start + current.length : -1;
		int index = 0;

		// fill no-style regions
		while (curEnd < region.getOffset() + region.getLength()) {
			// add empty range
			if (curStart > offset) {
				StyleRange undoRange = new StyleRange(offset, curStart - offset, null, null);
				undoRanges.add(index, undoRange);
				index++;
			}

			// step
			index++;
			if (index < undoRanges.size()) {
				offset = curEnd;
				current = undoRanges.get(index);
				curStart = current.start;
				curEnd = current.start + current.length;
			} else if (index == undoRanges.size()) {
				// last one
				offset = curEnd;
				current = null;
				curStart = region.getOffset() + region.getLength();
				curEnd = -1;
			} else
				curEnd = region.getOffset() + region.getLength();
		}

		// create modified styles (with background)
		List<StyleRange> shadedRanges = new ArrayList<>(undoRanges.size());
		for (Iterator<StyleRange> it = undoRanges.iterator(); it.hasNext();) {
			StyleRange range = (StyleRange) it.next().clone();
			shadedRanges.add(range);
			range.background = getHighlightColor(disp);
		}

		// set the ranges one by one
		for (Iterator<StyleRange> iter = shadedRanges.iterator(); iter.hasNext();) {
			text.setStyleRange(iter.next());

		}

		return undoRanges.toArray(undoRanges.toArray(new StyleRange[0]));
	}

	private void resetViewerBackground(StyleRange[] oldRanges) {

		if (oldRanges == null)
			return;

		if (fInput == null)
			return;

		StyledText text = fInput.fViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		// set the ranges one by one
		for (int i = 0; i < oldRanges.length; i++) {
			text.setStyleRange(oldRanges[i]);
		}
	}

	private Color getHighlightColor(Display disp) {
		return disp.getSystemColor(SWT.COLOR_GRAY);
	}

	private Color getSelectionColor(Display disp) {
		return disp.getSystemColor(SWT.COLOR_GRAY);
	}

	private Cursor getHandCursor(Display disp) {
		return disp.getSystemCursor(SWT.CURSOR_HAND);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#computeSizeConstraints(int, int)
	 * @since 3.4
	 */
	@Override
	public Point computeSizeConstraints(int widthInChars, int heightInChars) {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#containsControl(org.eclipse.swt.widgets.Control)
	 * @since 3.4
	 */
	@Override
	public boolean containsControl(Control control) {
		do {
			if (control == fShell)
				return true;
			if (control instanceof Shell)
				return false;
			control = control.getParent();
		} while (control != null);
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
	 * @since 3.4
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return fShell != null && !fShell.isDisposed() && fShell.isVisible();
	}

}
