/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.progress.UIJob;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;

public class RenameInformationPopup implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension {

	private class PopupVisibilityManager implements IPartListener2, ControlListener, MouseListener, KeyListener, ITextListener, IViewportListener {

		public void start() {
			fEditor.getSite().getWorkbenchWindow().getPartService().addPartListener(this);
			final ISourceViewer viewer= fEditor.getViewer();
			final StyledText textWidget= viewer.getTextWidget();
			textWidget.addControlListener(this);
			textWidget.addMouseListener(this);
			textWidget.addKeyListener(this);
			fEditor.getSite().getShell().addControlListener(this);
			viewer.addTextListener(this);
			viewer.addViewportListener(this);
			fPopup.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					fEditor.getSite().getWorkbenchWindow().getPartService().removePartListener(PopupVisibilityManager.this);
					if (!textWidget.isDisposed()) {
						textWidget.removeControlListener(PopupVisibilityManager.this);
						textWidget.removeMouseListener(PopupVisibilityManager.this);
						textWidget.removeKeyListener(PopupVisibilityManager.this);
					}
					fEditor.getSite().getShell().removeControlListener(PopupVisibilityManager.this);
					viewer.removeTextListener(PopupVisibilityManager.this);
					viewer.removeViewportListener(PopupVisibilityManager.this);
					if (fMenuImage != null) {
						fMenuImage.dispose();
						fMenuImage= null;
					}
					if (fMenuManager != null) {
						fMenuManager.dispose();
						fMenuManager= null;
					}
					fRenameLinkedMode.cancel();
				}
			});
		}

		public void partActivated(IWorkbenchPartReference partRef) {
			IWorkbenchPart fPart= fEditor.getEditorSite().getPart();
			if (partRef.getPart(false) == fPart) {
				updateVisibility();
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
			IWorkbenchPart fPart= fEditor.getEditorSite().getPart();
			if (fPopup != null && !fPopup.isDisposed() && partRef.getPart(false) == fPart) {
				fPopup.setVisible(false);
			}
		}

		public void partHidden(IWorkbenchPartReference partRef) {
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		public void partOpened(IWorkbenchPartReference partRef) {
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}

		public void controlMoved(ControlEvent e) {
			updatePopupLocation(true);
			updateVisibility(); //only for hiding outside editor area
		}

		public void controlResized(ControlEvent e) {
			updatePopupLocation(true);
			updateVisibility(); //only for hiding outside editor area
		}

		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
		}

		public void mouseUp(MouseEvent e) {
			updatePopupLocation(false);
			updateVisibility();
		}

		public void keyPressed(KeyEvent e) {
			updatePopupLocation(false);
			updateVisibility();
		}

		public void keyReleased(KeyEvent e) {
		}

		public void textChanged(TextEvent event) {
			if (!event.getViewerRedrawState())
				return;
			updatePopupLocation(false);
			updateVisibility(); //only for hiding outside editor area
		}

		public void viewportChanged(int verticalOffset) {
			updatePopupLocation(true);
			updateVisibility(); //only for hiding outside editor area
		}
	}

	/**
	 * Cached platform flag for dealing with platform-specific issue:
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=219326 : Shell with custom region and SWT.NO_TRIM still has border
	 */
	private static boolean MAC = Util.isMac();

	private static final int WIDGET_PRIORITY= 1000;

	private static final String DIALOG_SETTINGS_SECTION= "RenameInformationPopup"; //$NON-NLS-1$
	private static final String SNAP_POSITION_KEY= "snap_position"; //$NON-NLS-1$

	private static final int SNAP_POSITION_UNDER_RIGHT_FIELD= 0;
	private static final int SNAP_POSITION_OVER_RIGHT_FIELD= 1;
	private static final int SNAP_POSITION_UNDER_LEFT_FIELD= 2;
	private static final int SNAP_POSITION_OVER_LEFT_FIELD= 3;
	private static final int SNAP_POSITION_LOWER_RIGHT= 4;

	private static final int POPUP_VISIBILITY_DELAY= 300;

	/**
	 * Offset of info hover arrow from the left or right side.
	 */
	private static final int HAO= 10;

	/**
	 * Width of info hover arrow.
	 */
	private static final int HAW= 8;

	/**
	 * Height of info hover arrow.
	 */
	private static final int HAH= 10;

	/**
	 * Gap between linked position and popup.
	 */
	private static final int GAP= 2;

	private final CEditor fEditor;
	private final RenameLinkedMode fRenameLinkedMode;

	private int fSnapPosition;
	private boolean fSnapPositionChanged;
	private Shell fPopup;
	private GridLayout fPopupLayout;
	private Region fRegion;

	private Image fMenuImage;
	private MenuManager fMenuManager;
	private ToolBar fToolBar;
	private String fOpenDialogBinding= ""; //$NON-NLS-1$
	private boolean fIsMenuUp= false;

	private boolean fDelayJobFinished= false;

	public RenameInformationPopup(CEditor editor, RenameLinkedMode renameLinkedMode) {
		fEditor= editor;
		fRenameLinkedMode= renameLinkedMode;
		restoreSnapPosition();
	}

	private void restoreSnapPosition() {
		IDialogSettings settings= getDialogSettings();
		try {
			fSnapPosition= settings.getInt(SNAP_POSITION_KEY);
		} catch (NumberFormatException e) {
			// default:
			fSnapPosition= SNAP_POSITION_UNDER_LEFT_FIELD;
		}
		fSnapPositionChanged= true;
	}

	private IDialogSettings getDialogSettings() {
		return CUIPlugin.getDefault().getDialogSettingsSection(DIALOG_SETTINGS_SECTION);
	}

	public void open() {
		// Must cache here, since editor context is not available in menu from popup shell:
		fOpenDialogBinding= getOpenDialogBinding();

		Shell workbenchShell= fEditor.getSite().getShell();
		final Display display= workbenchShell.getDisplay();

		fPopup= new Shell(workbenchShell, SWT.ON_TOP | SWT.NO_TRIM | SWT.TOOL);
		fPopupLayout= new GridLayout(2, false);
		fPopupLayout.marginWidth= 1;
		fPopupLayout.marginHeight= 1;
		fPopupLayout.marginLeft= 4;
		fPopupLayout.horizontalSpacing= 0;
		fPopup.setLayout(fPopupLayout);

		createContent(fPopup);
		updatePopupLocation(true);
		new PopupVisibilityManager().start();

		// Leave linked mode when popup loses focus
		// (except when focus goes back to workbench window or menu is open):
		fPopup.addShellListener(new ShellAdapter() {
			@Override
			public void shellDeactivated(ShellEvent e) {
				if (fIsMenuUp)
					return;

				final Shell editorShell= fEditor.getSite().getShell();
				display.asyncExec(new Runnable() {
					// post to UI thread since editor shell only gets activated after popup has lost focus
					public void run() {
						Shell activeShell= display.getActiveShell();
						if (activeShell != editorShell) {
							fRenameLinkedMode.cancel();
						}
					}
				});
			}
		});

		if (!MAC) { // carbon and cocoa draw their own border...
			fPopup.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent pe) {
					pe.gc.drawPolygon(getPolygon(true));
				}
			});
		}

//		fPopup.moveBelow(null); // make sure hovers are on top of the info popup
// 		XXX workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=170774
//		fPopup.moveBelow(workbenchShell.getShells()[0]);

		UIJob delayJob= new UIJob(display, RenameMessages.RenameInformationPopup_delayJobName) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fDelayJobFinished= true;
				if (fPopup != null && !fPopup.isDisposed()) {
					updateVisibility();
				}
				return Status.OK_STATUS;
			}
		};
		delayJob.setSystem(true);
		delayJob.setPriority(Job.INTERACTIVE);
		delayJob.schedule(POPUP_VISIBILITY_DELAY);
	}

	public void close() {
		if (fPopup != null) {
			if (!fPopup.isDisposed()) {
				fPopup.close();
			}
			fPopup= null;
		}
		releaseWidgetToken();
		if (fRegion != null) {
			if (!fRegion.isDisposed()) {
				fRegion.dispose();
			}
		}
	}

	public Shell getShell() {
		return fPopup;
	}

	private void updatePopupLocation(boolean force) {
		if (!force && fSnapPosition == SNAP_POSITION_LOWER_RIGHT)
			return;

		packPopup();
		Point loc= computePopupLocation(fSnapPosition);
		if (loc != null && !loc.equals(fPopup.getLocation())) {
			fPopup.setLocation(loc);
			// XXX workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=170774
//			fPopup.moveBelow(fEditor.getSite().getShell().getShells()[0]);
		}
	}

	private void updateVisibility() {
		if (fPopup != null && !fPopup.isDisposed() && fDelayJobFinished) {
			boolean visible= false;
			//TODO: Check for visibility of linked position, not whether popup is outside of editor?
			if (fRenameLinkedMode.isCaretInLinkedPosition()) {
				StyledText textWidget= fEditor.getViewer().getTextWidget();
				Rectangle eArea= Geometry.toDisplay(textWidget, textWidget.getClientArea());
				Rectangle pBounds= fPopup.getBounds();
				pBounds.x -= GAP;
				pBounds.y -= GAP;
				pBounds.width += 2 * GAP;
				pBounds.height += 2 * GAP;
				if (eArea.intersects(pBounds)) {
					visible= true;
				}
			}
			if (visible && !fPopup.isVisible()) {
				ISourceViewer viewer= fEditor.getViewer();
				if (viewer instanceof IWidgetTokenOwnerExtension) {
					IWidgetTokenOwnerExtension widgetTokenOwnerExtension= (IWidgetTokenOwnerExtension) viewer;
					widgetTokenOwnerExtension.requestWidgetToken(this, WIDGET_PRIORITY);
				}
			} else if (!visible && fPopup.isVisible()) {
				releaseWidgetToken();
			}
			fPopup.setVisible(visible);
		}
	}

	private void releaseWidgetToken() {
		ISourceViewer viewer= fEditor.getViewer();
		if (viewer instanceof IWidgetTokenOwner) {
			IWidgetTokenOwner widgetTokenOwner= (IWidgetTokenOwner) viewer;
			widgetTokenOwner.releaseWidgetToken(this);
		}
	}

	/**
	 * @param snapPosition one of the SNAP_POSITION_* constants
	 * @return the location in display coordinates or <code>null</code> iff not visible
	 */
	private Point computePopupLocation(int snapPosition) {
		if (fPopup == null || fPopup.isDisposed())
			return null;

		switch (snapPosition) {
			case SNAP_POSITION_LOWER_RIGHT:
			{
				StyledText eWidget= fEditor.getViewer().getTextWidget();
				Rectangle eBounds= eWidget.getClientArea();
				Point eLowerRight= eWidget.toDisplay(eBounds.x + eBounds.width, eBounds.y + eBounds.height);
				Point pSize= getExtent();
				return new Point(eLowerRight.x - pSize.x - 5, eLowerRight.y - pSize.y - 5);
			}

			case SNAP_POSITION_UNDER_RIGHT_FIELD:
			case SNAP_POSITION_OVER_RIGHT_FIELD:
			{
				LinkedPosition position= fRenameLinkedMode.getCurrentLinkedPosition();
				if (position == null)
					return null;
				ISourceViewer viewer= fEditor.getViewer();
				ITextViewerExtension5 viewer5= (ITextViewerExtension5) viewer;
				int widgetOffset= viewer5.modelOffset2WidgetOffset(position.offset + position.length);

				StyledText textWidget= viewer.getTextWidget();
				Point pos= textWidget.getLocationAtOffset(widgetOffset);
				Point pSize= getExtent();
				if (snapPosition == SNAP_POSITION_OVER_RIGHT_FIELD) {
					pos.y-= pSize.y + GAP;
				} else {
					pos.y+= textWidget.getLineHeight(widgetOffset) + GAP;
				}
				pos.x+= GAP;
				Point dPos= textWidget.toDisplay(pos);
				Rectangle displayBounds= textWidget.getDisplay().getClientArea();
				Rectangle dPopupRect= Geometry.createRectangle(dPos, pSize);
				Geometry.moveInside(dPopupRect, displayBounds);
				return new Point(dPopupRect.x, dPopupRect.y);
			}

			case SNAP_POSITION_UNDER_LEFT_FIELD:
			case SNAP_POSITION_OVER_LEFT_FIELD:
			default: // same as SNAP_POSITION_UNDER_LEFT_FIELD
			{
				LinkedPosition position= fRenameLinkedMode.getCurrentLinkedPosition();
				if (position == null)
					return null;
				ISourceViewer viewer= fEditor.getViewer();
				ITextViewerExtension5 viewer5= (ITextViewerExtension5) viewer;
				int widgetOffset= viewer5.modelOffset2WidgetOffset(position.offset/* + position.length*/);

				StyledText textWidget= viewer.getTextWidget();
				Point pos= textWidget.getLocationAtOffset(widgetOffset);
				Point pSize= getExtent();
				pSize.y+= HAH + 1;
				pos.x-= HAO;
				if (snapPosition == SNAP_POSITION_OVER_LEFT_FIELD) {
					pos.y-= pSize.y;
				} else {
					pos.y+= textWidget.getLineHeight(widgetOffset);
				}
				Point dPos= textWidget.toDisplay(pos);
				Rectangle displayBounds= textWidget.getDisplay().getClientArea();
				Rectangle dPopupRect= Geometry.createRectangle(dPos, pSize);
				Geometry.moveInside(dPopupRect, displayBounds);
				return new Point(dPopupRect.x, dPopupRect.y);
			}
		}
	}

	private void addMoveSupport(final Shell popupShell, final Control movedControl) {
		movedControl.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(final MouseEvent downEvent) {
				if (downEvent.button != 1) {
					return;
				}

				final Point POPUP_SOURCE= popupShell.getLocation();
				final StyledText textWidget= fEditor.getViewer().getTextWidget();
				Point pSize= getExtent();
				int originalSnapPosition= fSnapPosition;

				/*
				 * Feature in Tracker: it is not possible to directly control the feedback,
				 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=121300
				 * and https://bugs.eclipse.org/bugs/show_bug.cgi?id=121298#c1 .
				 *
				 * Workaround is to have an offscreen rectangle for tracking mouse movement
				 * and a manually updated rectangle for the actual drop target.
				 */
				final Tracker tracker= new Tracker(textWidget, SWT.NONE);

				final Point[] LOCATIONS= {
						textWidget.toControl(computePopupLocation(SNAP_POSITION_UNDER_RIGHT_FIELD)),
						textWidget.toControl(computePopupLocation(SNAP_POSITION_OVER_RIGHT_FIELD)),
						textWidget.toControl(computePopupLocation(SNAP_POSITION_UNDER_LEFT_FIELD)),
						textWidget.toControl(computePopupLocation(SNAP_POSITION_OVER_LEFT_FIELD)),
						textWidget.toControl(computePopupLocation(SNAP_POSITION_LOWER_RIGHT))
				};

				final Rectangle[] DROP_TARGETS= {
					Geometry.createRectangle(LOCATIONS[0], pSize),
					Geometry.createRectangle(LOCATIONS[1], pSize),
					new Rectangle(LOCATIONS[2].x, LOCATIONS[2].y + HAH, pSize.x, pSize.y),
					Geometry.createRectangle(LOCATIONS[3], pSize),
					Geometry.createRectangle(LOCATIONS[4], pSize)
				};
				final Rectangle MOUSE_MOVE_SOURCE= new Rectangle(1000000, 0, 0, 0);
				tracker.setRectangles(new Rectangle[] { MOUSE_MOVE_SOURCE, DROP_TARGETS[fSnapPosition] });
				tracker.setStippled(true);

				ControlListener moveListener= new ControlAdapter() {
					/*
					 * @see org.eclipse.swt.events.ControlAdapter#controlMoved(org.eclipse.swt.events.ControlEvent)
					 */
					@Override
					public void controlMoved(ControlEvent moveEvent) {
						Rectangle[] currentRects= tracker.getRectangles();
						final Rectangle mouseMoveCurrent= currentRects[0];
						Point popupLoc= new Point(
								POPUP_SOURCE.x + mouseMoveCurrent.x - MOUSE_MOVE_SOURCE.x,
								POPUP_SOURCE.y + mouseMoveCurrent.y - MOUSE_MOVE_SOURCE.y);

						popupShell.setLocation(popupLoc);

						Point ePopupLoc= textWidget.toControl(popupLoc);
						int minDist= Integer.MAX_VALUE;
						for (int snapPos= 0; snapPos < DROP_TARGETS.length; snapPos++) {
							int dist= Geometry.distanceSquared(ePopupLoc, LOCATIONS[snapPos]);
							if (dist < minDist) {
								minDist= dist;
								fSnapPosition= snapPos;
								fSnapPositionChanged= true;
								currentRects[1]= DROP_TARGETS[snapPos];
							}
						}
						tracker.setRectangles(currentRects);
					}
				};
				tracker.addControlListener(moveListener);
				boolean committed= tracker.open();
				tracker.close();
				tracker.dispose();
				if (committed) {
					getDialogSettings().put(SNAP_POSITION_KEY, fSnapPosition);
				} else {
					fSnapPosition= originalSnapPosition;
					fSnapPositionChanged= true;
				}
				updatePopupLocation(true);
				activateEditor();
			}
		});
	}

	private void packPopup() {
		if (!fSnapPositionChanged) {
			return;
		}
		fSnapPositionChanged= false;
		
		boolean isUnderLeft= fSnapPosition == SNAP_POSITION_UNDER_LEFT_FIELD;
		boolean isOverLeft= fSnapPosition == SNAP_POSITION_OVER_LEFT_FIELD;
		fPopupLayout.marginTop= isUnderLeft ? HAH : 0;
		fPopupLayout.marginBottom= isOverLeft ? HAH + 1 : 0;
		fPopup.pack();

		Region oldRegion= fRegion;
		if (isUnderLeft || isOverLeft) {
			fRegion= new Region();
			fRegion.add(getPolygon(false));
			fPopup.setRegion(fRegion);
			Rectangle bounds= fRegion.getBounds();
			fPopup.setSize(bounds.width, bounds.height + 1);
		} else {
			fRegion= null;
			fPopup.setRegion(null);
		}

		if (oldRegion != null) {
			oldRegion.dispose();
		}
	}

	private Point getExtent() {
		Point e = fPopup.getSize();
		switch (fSnapPosition) {
			case SNAP_POSITION_UNDER_LEFT_FIELD:
				e.y -= HAH;
				break;
			case SNAP_POSITION_OVER_LEFT_FIELD:
				e.y -= HAH + 1;
				break;
		}
		return e;
	}

	private int[] getPolygon(boolean border) {
		Point e = getExtent();
		int b = border ? 1 : 0;
		boolean isRTL= (fPopup.getStyle() & SWT.RIGHT_TO_LEFT) != 0;
		int ha1= isRTL ? e.x - HAO :           HAO + HAW;
		int ha2= isRTL ? e.x - HAO - HAW / 2 : HAO + HAW / 2;
		int ha3= isRTL ? e.x - HAO - HAW :     HAO;
			int[] poly;
			switch (fSnapPosition) {
				case SNAP_POSITION_OVER_LEFT_FIELD:
					poly= new int[] {
							0, 0,
							e.x - b, 0,
							e.x - b, e.y - b,
							ha1, e.y - b,
							ha2, e.y + HAH - b,
							ha3, e.y - b,
							0, e.y - b,
							0, 0 };
					break;

				case SNAP_POSITION_UNDER_LEFT_FIELD:
					poly= new int[] {
							0, HAH,
							ha3 + b, HAH,
							ha2, b,
							ha1 - b, HAH,
							e.x - b, HAH,
							e.x - b, e.y + HAH - b,
							0, e.y + HAH - b,
							0, HAH };
					break;

				default:
					poly= new int[] {
							0, 0,
							e.x - b, 0,
							e.x - b, e.y - b,
							0, e.y - b,
							0, 0 };
					break;
			}
		return poly;
	}

	private void createContent(Composite parent) {
		Display display= parent.getDisplay();
		Color foreground= display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		Color background= display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		addMoveSupport(fPopup, parent);

		StyledText hint= new StyledText(fPopup, SWT.READ_ONLY | SWT.SINGLE);
		String enterKeyName= getEnterBinding();
		String hintTemplate= RenameMessages.RenameInformationPopup_EnterNewName;
		hint.setText(NLS.bind(hintTemplate, enterKeyName));
		hint.setForeground(foreground);
		hint.setStyleRange(new StyleRange(hintTemplate.indexOf("{0}"), enterKeyName.length(), null, null, SWT.BOLD)); //$NON-NLS-1$
		hint.setEnabled(false); // text must not be selectable
		addMoveSupport(fPopup, hint);

		addViewMenu(parent);

		recursiveSetBackgroundColor(parent, background);
	}

	private ToolBar addViewMenu(final Composite parent) {
		fToolBar= new ToolBar(parent, SWT.FLAT);
		final ToolItem menuButton = new ToolItem(fToolBar, SWT.PUSH, 0);
		fMenuImage= CPluginImages.DESC_ELCL_VIEW_MENU.createImage();
		menuButton.setImage(fMenuImage);
		menuButton.setToolTipText(RenameMessages.RenameInformationPopup_menu);
		fToolBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				showMenu(fToolBar);
			}
		});
		menuButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showMenu(fToolBar);
			}
		});
		fToolBar.pack();
		return fToolBar;
	}

	private void showMenu(ToolBar toolBar) {
		Menu menu= getMenuManager().createContextMenu(toolBar);
		menu.setLocation(toolBar.toDisplay(0, toolBar.getSize().y));
		fIsMenuUp= true;
		menu.setVisible(true);
	}

	private MenuManager getMenuManager() {
		if (fMenuManager != null) {
			return fMenuManager;
		}

		fMenuManager= new MenuManager();
		fMenuManager.setRemoveAllWhenShown(true);

		fMenuManager.addMenuListener(new IMenuListener2() {
			public void menuAboutToHide(IMenuManager manager) {
				fIsMenuUp= false;
			}
			public void menuAboutToShow(IMenuManager manager) {
				boolean canRefactor= !fRenameLinkedMode.isOriginalName();
				
				IAction refactorAction= new Action(RenameMessages.RenameInformationPopup_RenameInWorkspace) {
					@Override
					public void run() {
						activateEditor();
						fRenameLinkedMode.doRename(false);
					}
				};
				refactorAction.setAccelerator(SWT.CR);
				refactorAction.setEnabled(canRefactor);
				manager.add(refactorAction);

				IAction previewAction= new Action(RenameMessages.RenameInformationPopup_Preview) {
					@Override
					public void run() {
						activateEditor();
						fRenameLinkedMode.doRename(true);
					}
				};
				previewAction.setAccelerator(SWT.CTRL | SWT.CR);
				previewAction.setEnabled(canRefactor);
				manager.add(previewAction);

				IAction openDialogAction= new Action(RenameMessages.RenameInformationPopup_OpenDialog + '\t' + fOpenDialogBinding) {
					@Override
					public void run() {
						activateEditor();
						fRenameLinkedMode.startFullDialog();
					}
				};
				manager.add(openDialogAction);

				manager.add(new Separator());

				MenuManager subMenuManager= new MenuManager(RenameMessages.RenameInformationPopup_SnapTo);
				addMoveMenuItem(subMenuManager, SNAP_POSITION_UNDER_LEFT_FIELD, RenameMessages.RenameInformationPopup_snap_under_left);
				addMoveMenuItem(subMenuManager, SNAP_POSITION_UNDER_RIGHT_FIELD, RenameMessages.RenameInformationPopup_snap_under_right);
				addMoveMenuItem(subMenuManager, SNAP_POSITION_OVER_LEFT_FIELD, RenameMessages.RenameInformationPopup_snap_over_left);
				addMoveMenuItem(subMenuManager, SNAP_POSITION_OVER_RIGHT_FIELD, RenameMessages.RenameInformationPopup_snap_over_right);
				addMoveMenuItem(subMenuManager, SNAP_POSITION_LOWER_RIGHT, RenameMessages.RenameInformationPopup_snap_bottom_right);
				manager.add(subMenuManager);

				IAction prefsAction= new Action(RenameMessages.RenameInformationPopup_preferences) {
					@Override
					public void run() {
						fRenameLinkedMode.cancel();
						String linkedModePrefPageID= "org.eclipse.ui.editors.preferencePages.LinkedModePreferencePage"; //$NON-NLS-1$
						String refactoringPrefPageID= CPluginPreferencePage.C_BASE_PREF_PAGE_ID;
						PreferencesUtil.createPreferenceDialogOn(fEditor.getSite().getShell(), refactoringPrefPageID, new String[] { linkedModePrefPageID, refactoringPrefPageID }, null).open();
					}
				};
				manager.add(prefsAction);
			}
		});
		return fMenuManager;
	}

	private void addMoveMenuItem(IMenuManager manager, final int snapPosition, String text) {
		IAction action= new Action(text, IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				fSnapPosition= snapPosition;
				fSnapPositionChanged= true;
				getDialogSettings().put(SNAP_POSITION_KEY, fSnapPosition);
				updatePopupLocation(true);
				activateEditor();
			}
		};
		action.setChecked(fSnapPosition == snapPosition);
		manager.add(action);
	}

	private static String getEnterBinding() {
		return KeyStroke.getInstance(KeyLookupFactory.getDefault().formalKeyLookup(IKeyLookup.CR_NAME)).format();
	}

	/**
	 * WARNING: only works in workbench window context!
	 * @return the keybinding for Refactor &gt; Rename
	 */
	private static String getOpenDialogBinding() {
		IBindingService bindingService= (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		if (bindingService == null)
			return ""; //$NON-NLS-1$
		String binding= bindingService.getBestActiveBindingFormattedFor(ICEditorActionDefinitionIds.RENAME_ELEMENT);
		return binding == null ? "" : binding; //$NON-NLS-1$
	}

	private static void recursiveSetBackgroundColor(Control control, Color color) {
		control.setBackground(color);
		if (control instanceof Composite) {
			Control[] children= ((Composite) control).getChildren();
			for (int i= 0; i < children.length; i++) {
				recursiveSetBackgroundColor(children[i], color);
			}
		}
	}

	public boolean ownsFocusShell() {
		if (fIsMenuUp)
			return true;
		if (fPopup == null || fPopup.isDisposed())
			return false;
		Shell activeShell= fPopup.getDisplay().getActiveShell();
		if (fPopup == activeShell)
			return true;
		return false;
	}

	private void activateEditor() {
		fEditor.getSite().getShell().setActive();
	}

	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		return false;
	}

	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		return false;
	}

	public boolean setFocus(IWidgetTokenOwner owner) {
		if (fToolBar != null && !fToolBar.isDisposed())
			showMenu(fToolBar);
		return true;
	}
}
