/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
/*
 * COutlineInformationControl.java 2004-12-14 / 08:17:41

 * $Revision: 1.4 $ $Date: 2005/01/24 17:24:14 $
 *
 * @author P.Tomaszewski
 */
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.editor.CContentOutlinerProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.StandardCElementLabelProvider;
import org.eclipse.cdt.ui.CElementGrouping;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Control which shows outline information in C/C++ editor. Based on
 * AbstracInformationContol/JavaOutlineInformationControl from JDT.
 * 
 * TODO: Sorting.
 * 
 * @author P.Tomaszewski
 */
public class COutlineInformationControl implements IInformationControl,
        IInformationControlExtension, IInformationControlExtension3 {

    /** If this option is set, location is not restored. */
    private static final String STORE_RESTORE_SIZE= "ENABLE_RESTORE_SIZE"; //$NON-NLS-1$
    /** If this option is set, size is not restore. */
    private static final String STORE_RESTORE_LOCATION= "ENABLE_RESTORE_LOCATION"; //$NON-NLS-1$
    /** If this option is set, sort is done. */
    private static final String STORE_SORT_ENABLED = "ENABLE_SORT"; //$NON-NLS-1$

    /** Border thickness in pixels. */
    private static final int BORDER = 1;
    /** Right margin in pixels. */
    private static final int RIGHT_MARGIN = 3;
    /** Minimum width set by setSizeConstrains to tree viewer. */
    private static final int MIN_WIDTH = 300;

    /** Source viewer which shows this control. */
    CEditor fEditor;
    /** Shell for this control. */
    Shell fShell;
    /** Control's composite. */
    Composite fComposite;
    /** Tree viewer used to display outline. */
    TreeViewer fTreeViewer;
    /** Text control for filter. */
    Text fFilterText;
    /** Content provider for tree control. */
    IContentProvider fTreeContentProvider;

    /** Sorter for tree viewer. */
    OutlineSorter fSorter;

    /** Control bounds. */
    Rectangle fBounds;
    /** Control trim. */
    Rectangle fTrim;

    /** This prevents to notify listener when it is adding. */
    boolean fIsDeactivationActive;

    /** Deactivation adapter. */
    private Listener fDeactivateListener;
    /** Shell adapter, used for control deactivation. */
    private ShellListener fShellListener;
    /** Control adapter for shell, used in resize action. */
    private ControlListener fControlListener;

    /** Should outline be sorted. */
    boolean fSort = true;

    /** Tool bar displayed on the top of the outline. */ 
    ToolBar fToolBar;
    /** Composite for tool bar. */
    private Composite fToolbarComposite;
    /** Menu manager for options menu. */
    private MenuManager fViewMenuManager;

    /**
     * Creates new outline control.
     * 
     * @param editor
     *            CEditor editor which uses this control.
     * @param parent
     *            Shell parent.
     * @param shellStyle
     *            Style of new shell.
     * @param treeStyle
     *            Style of the tree viewer.
     */
    public COutlineInformationControl(CEditor editor, Shell parent,
            int shellStyle, int treeStyle) {
        super();
        this.fEditor = editor;
        createShell(parent, shellStyle);
        createComposite();
        createToolbar();
        createHorizontalSeparator();
        createTreeeViewer(treeStyle);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setInformation(java.lang.String)
     */
    public void setInformation(String information) {
        // Does not need implementation.
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setSizeConstraints(int,
     *      int)
     */
    public void setSizeConstraints(int maxWidth, int maxHeight) {
        // Copied from AbstractInformationContol.
        if (maxWidth > -1 && maxHeight > -1) {
            final GridData gd = new GridData(GridData.FILL_BOTH);
            if (maxWidth > -1) {
                if (maxWidth < MIN_WIDTH) {
                    gd.widthHint = MIN_WIDTH;
                } else {
                    gd.widthHint = maxWidth;
                }
            }
            if (maxHeight > -1) {
                gd.heightHint = maxHeight;
            }

            fTreeViewer.getTree().setLayoutData(gd);
        }
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
     */
    public Point computeSizeHint() {
        return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        if (visible || fIsDeactivationActive) {
            fShell.setVisible(visible);
        }
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setSize(int, int)
     */
    public void setSize(int width, int height) {
        fShell.setSize(width, height);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setLocation(org.eclipse.swt.graphics.Point)
     */
    public void setLocation(Point location) {
        fTrim = fShell.computeTrim(0, 0, 0, 0);
        Point compositeLocation = fComposite.getLocation();
        location.x += fTrim.x - compositeLocation.x;
        location.y += fTrim.y - compositeLocation.y;
        fShell.setLocation(location);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#dispose()
     */
    public void dispose() {
        if (fShell != null && !fShell.isDisposed()) {
            fShell.removeShellListener(fShellListener);
            fShell.removeListener(SWT.Deactivate, fDeactivateListener);
            fShell.dispose();
            fFilterText.dispose();
            fComposite.dispose();
        } else {
            fShell = null;
            fComposite = null;
            fTreeViewer = null;
            fFilterText = null;
            fDeactivateListener = null;
            fShellListener = null;
        }
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#addDisposeListener(org.eclipse.swt.events.DisposeListener)
     */
    public void addDisposeListener(DisposeListener listener) {
        fShell.addDisposeListener(listener);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
     */
    public void removeDisposeListener(DisposeListener listener) {
        fShell.removeDisposeListener(listener);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setForegroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setForegroundColor(Color foreground) {
        fTreeViewer.getTree().setForeground(foreground);
        fFilterText.setForeground(foreground);
        fComposite.setForeground(foreground);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setBackgroundColor(org.eclipse.swt.graphics.Color)
     */
    public void setBackgroundColor(Color background) {
        fTreeViewer.getTree().setBackground(background);
        fFilterText.setBackground(background);
        fComposite.setBackground(background);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#isFocusControl()
     */
    public boolean isFocusControl() {
        return fTreeViewer.getControl().isFocusControl()
                || fFilterText.isFocusControl();
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#setFocus()
     */
    public void setFocus() {
        fShell.forceFocus();
        fFilterText.setFocus();
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#addFocusListener(org.eclipse.swt.events.FocusListener)
     */
    public void addFocusListener(FocusListener listener) {
        fShell.addFocusListener(listener);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControl#removeFocusListener(org.eclipse.swt.events.FocusListener)
     */
    public void removeFocusListener(FocusListener listener) {
        fShell.removeFocusListener(listener);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
     */
    public boolean hasContents() {
        return fTreeViewer != null
                && ((Tree) fTreeViewer.getControl()).getItemCount() > 0;
    }

    /**
     * @see org.eclipse.jface.text.IInformationControlExtension3#getBounds()
     */
    public Rectangle getBounds() {
        return fBounds;
    }

    /**
     * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
     */
    public Rectangle computeTrim() {
        // Copied from AbstractInformationControl.
        if (fTrim != null) {
            return fTrim;
        }
        return new Rectangle(0, 0, 0, 0);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControlExtension3#restoresLocation()
     */
    public boolean restoresLocation() {
        return getSettings().getBoolean(STORE_RESTORE_LOCATION);
    }

    /**
     * @see org.eclipse.jface.text.IInformationControlExtension3#restoresSize()
     */
    public boolean restoresSize() {
        return getSettings().getBoolean(STORE_RESTORE_SIZE);
    }

    /**
     * Returns view manager instance. If instance does not exit it is created.
     * @return View manager.
     */
    MenuManager getViewMenuManager() {
        if (fViewMenuManager == null) {
            fViewMenuManager= new MenuManager();
            fViewMenuManager.add(new SortAction());
            fViewMenuManager.add(new Separator());
            fViewMenuManager.add(new RememberBoundsAction());
            fViewMenuManager.add(new MoveAction());
        }
        return fViewMenuManager;
    }

    /**
     * Creates shell for outline control.
     * 
     * @param parent
     *            Parent shell.
     * @param shellStyle
     *            Shell style.
     */
    private void createShell(Shell parent, int shellStyle) {
        fShell = new Shell(parent, shellStyle);
        final Display display = fShell.getDisplay();
        fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        final int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
        fShell.setLayout(new BorderFillLayout(border));
        createDeactivationListener();
        fShell.addListener(SWT.Deactivate, fDeactivateListener);
        fIsDeactivationActive = true;
        createShellListener();
        fShell.addShellListener(fShellListener);
        createControlListener();
        fShell.addControlListener(fControlListener);
    }

    /**
     * Creates composite of the outline control.
     * 
     */
    private void createComposite() {
        fComposite = new Composite(fShell, SWT.RESIZE);
        GridLayout layout = new GridLayout(1, false);
        fComposite.setLayout(layout);
        fComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Creates tree viewer control.
     * @param treeStyle Tree style.
     */
    private void createTreeeViewer(int treeStyle) {
        final IWorkingCopyManager manager = CUIPlugin.getDefault()
                .getWorkingCopyManager();
        fTreeViewer = new ProblemTreeViewer(fComposite, treeStyle);
        final Tree tree = fTreeViewer.getTree();
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        fTreeContentProvider = new CContentOutlinerProvider(fTreeViewer);
        fSorter = new OutlineSorter();
        fTreeViewer.setContentProvider(fTreeContentProvider);
        if (getSettings().getBoolean(STORE_SORT_ENABLED)) {
            fTreeViewer.setSorter(fSorter);
        }
        fTreeViewer.setLabelProvider(new DecoratingCLabelProvider(
                new StandardCElementLabelProvider(), true));
        fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        fTreeViewer.setInput(manager.getWorkingCopy(fEditor.getEditorInput()));
        tree.addKeyListener(createKeyListenerForTreeViewer());
        tree.addSelectionListener(createSelectionListenerForTreeViewer());
        tree.addMouseMoveListener(createMouseMoveListenerForTreeViewer());
        tree.addMouseListener(createMouseListenerForTreeViewer());
    }

    /**
     * Creates horizontal separator between filter text and outline.
     */
    private void createHorizontalSeparator() {
        Label separator = new Label(fComposite, SWT.SEPARATOR | SWT.HORIZONTAL
                | SWT.LINE_DOT);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * Creates toolbar.
     */
    private void createToolbar() {
        fToolbarComposite = new Composite(fComposite, SWT.NONE);
        final GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        fToolbarComposite.setLayout(layout);
        fToolbarComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createFilterText();
        createViewMenu();
    }
    
    /**
     * Creates control for filter text.
     */
    private void createFilterText() {
        fFilterText = new Text(fToolbarComposite, SWT.NONE);
        final GridData data = new GridData(GridData.FILL_HORIZONTAL);
        final GC gc = new GC(fComposite);
        gc.setFont(fComposite.getFont());
        final FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.CENTER;
        fFilterText.setLayoutData(data);

        fFilterText.addKeyListener(createKeyListenerForFilterContol());
        fFilterText.addModifyListener(createModifyListenerForFilterControl());
    }

    /**
     * Creates view menu for toolbar. In this menu options will be displayed.
     */
    private void createViewMenu() {
        fToolBar= new ToolBar(fToolbarComposite, SWT.FLAT);
        final ToolItem viewMenuButton= new ToolItem(fToolBar, SWT.PUSH, 0);

        final GridData data= new GridData();
        data.horizontalAlignment= GridData.END;
        data.verticalAlignment= GridData.BEGINNING;
        fToolBar.setLayoutData(data);

        viewMenuButton.setImage(CPluginImages.get(CPluginImages.IMG_VIEW_MENU));
        createSelectionListenerForOptions(viewMenuButton);
    }

    /**
     * Creates selection listener for options.
     * @param viewMenuButton Button to create selection adapter.
     */
    private void createSelectionListenerForOptions(ToolItem viewMenuButton) {
        viewMenuButton.addSelectionListener(new SelectionAdapter() {
            /**
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                fIsDeactivationActive = false;

                final Menu menu = getViewMenuManager().createContextMenu(fShell);
                final Rectangle bounds = fToolBar.getBounds();
                final Point topLeft = fShell.toDisplay(new Point(bounds.x, bounds.y + bounds.height));
                menu.setLocation(topLeft.x, topLeft.y);

                menu.setVisible(true);        
            }
        });
    }
    
    /**
     * Creates mouse listener for tree viewer.
     * 
     * @return Created mouse listener.
     */
    private MouseListener createMouseListenerForTreeViewer() {
        final MouseListener mouseListener = new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                final Tree tree = fTreeViewer.getTree();
                if (tree.getSelectionCount() < 1) {
                    return;
                }
                if (e.button != 1) {
                    return;
                }

                if (tree.equals(e.getSource())) {
                    Object o = tree.getItem(new Point(e.x, e.y));
                    final TreeItem selection = tree.getSelection()[0];
                    if (selection.equals(o)) {
                        CElement selectedElement = (CElement) selection
                                .getData();
                        fEditor.setSelection(selectedElement);
                        dispose();
                    }
                    if (fComposite != null && !fComposite.isDisposed())
                    {
                        fBounds = fComposite.getBounds();
                    }
                }
            }
        };
        return mouseListener;
    }

    /**
     * Creates mouse move listener for tree viewer.
     * 
     * @return Mouse move listener.
     */
    private MouseMoveListener createMouseMoveListenerForTreeViewer() {
        // Copied from AbstractInformationControl.
        final MouseMoveListener moveListener = new MouseMoveListener() {
            TreeItem fLastItem = null;

            public void mouseMove(MouseEvent e) {
                final Tree tree = fTreeViewer.getTree();
                if (tree.equals(e.getSource())) {
                    Object o = tree.getItem(new Point(e.x, e.y));
                    if (o instanceof TreeItem) {
                        if (!o.equals(fLastItem)) {
                            fLastItem = (TreeItem) o;
                            tree.setSelection(new TreeItem[] { fLastItem });
                        } else if (e.y < tree.getItemHeight() / 4) {
                            // Scroll up
                            Point p = tree.toDisplay(e.x, e.y);
                            Item item = fTreeViewer.scrollUp(p.x, p.y);
                            if (item instanceof TreeItem) {
                                fLastItem = (TreeItem) item;
                                tree.setSelection(new TreeItem[] { fLastItem });
                            }
                        } else if (e.y > tree.getBounds().height
                                - tree.getItemHeight() / 4) {
                            // Scroll down
                            Point p = tree.toDisplay(e.x, e.y);
                            Item item = fTreeViewer.scrollDown(p.x, p.y);
                            if (item instanceof TreeItem) {
                                fLastItem = (TreeItem) item;
                                tree.setSelection(new TreeItem[] { fLastItem });
                            }
                        }
                    }
                }
            }
        };
        return moveListener;
    }

    /**
     * Creates selection listener for tree viewer.
     * 
     * @return Created selection listener.
     */
    private SelectionListener createSelectionListenerForTreeViewer() {
        final SelectionListener selectionListener = new SelectionListener() {
            /**
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                // Does not need implementation.
            }

            /**
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e) {
                final TreeItem[] selection = ((Tree) fTreeViewer.getControl())
                        .getSelection();
                if (selection.length > 0) {
                    CElement selectedElement = (CElement) selection[0]
                            .getData();
                    fEditor.setSelection(selectedElement);
                    dispose();
                }
            }
        };
        return selectionListener;
    }

    /**
     * Creates key listener for tree viewer.
     * 
     * @return Created listener.
     */
    private KeyListener createKeyListenerForTreeViewer() {
        final KeyListener listener = new KeyListener() {
            /**
             * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
             */
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 0x1B) // ESC
                {
                    dispose();
                }
            }

            /**
             * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            public void keyReleased(KeyEvent e) {
                // Does not need implementation.
            }
        };
        return listener;
    }

    /**
     * Creates modify listener for filter text control.
     * 
     * @return Modify listener.
     */
    private ModifyListener createModifyListenerForFilterControl() {
        final ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String text = ((Text) e.widget).getText();
                final int length = text.length();
                if (length > 0 && text.charAt(length - 1) != '*') {
                    text = text + '*';
                }
                ((CContentOutlinerProvider) fTreeContentProvider).updateFilter(text);
            }
        };
        return modifyListener;
    }

    /**
     * Creates key listener for filter text control.
     * 
     * @return Key listener.
     */
    private KeyListener createKeyListenerForFilterContol() {
        final KeyListener keyListener = new KeyListener() {
            /**
             * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
             */
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 0x0D) {
                    // Should select entered element.
                }
                if (e.keyCode == SWT.ARROW_DOWN) {
                    fTreeViewer.getTree().setFocus();
                }
                if (e.keyCode == SWT.ARROW_UP) {
                    fTreeViewer.getTree().setFocus();
                }
                if (e.character == 0x1B) // ESC
                {
                    dispose();
                }
            }

            /**
             * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
             */
            public void keyReleased(KeyEvent e) {
                // Does not need implementation.
            }
        };
        return keyListener;
    }

    /**
     * Creates control listener for shell.
     */
    private void createControlListener() {
        // Copied from AbstractInformationControl.
        fControlListener = new ControlAdapter() {
            /**
             * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
             */
            public void controlMoved(ControlEvent e) {
                fBounds = fShell.getBounds();
                if (fTrim != null) {
                    final Point location = fComposite.getLocation();
                    fBounds.x = fBounds.x - fTrim.x + location.x;
                    fBounds.y = fBounds.y - fTrim.y + location.y;
                }

            }

            /**
             * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
             */
            public void controlResized(ControlEvent e) {
                fBounds = fShell.getBounds();
                if (fTrim != null) {
                    final Point location = fComposite.getLocation();
                    fBounds.x = fBounds.x - fTrim.x + location.x;
                    fBounds.y = fBounds.y - fTrim.y + location.y;
                }
            }
        };
    }

    /**
     * Creates deactivation listener. It closes the control is shell becomes
     * active.
     */
    private void createDeactivationListener() {
        fDeactivateListener = new Listener() {
            /**
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                if (fIsDeactivationActive) {
                    dispose();
                }
            }
        };
    }

    /**
     * Creates shell listener for management deactivation state.
     */
    private void createShellListener() {
        fShellListener = new ShellAdapter() {
            /**
             * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
             */
            public void shellActivated(ShellEvent e) {
                if (e.widget == fShell && fShell.getShells().length == 0) {
                    fIsDeactivationActive = true;
                }
            }
        };
    }

    /**
     * Returns setting for this control.
     * @return Settings.
     */
    IDialogSettings getSettings() {
        final String sectionName = "org.eclipse.jdt.internal.ui.text.QuickOutline"; //$NON-NLS-1$
        IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null)
        {
            settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }

        return settings;
    }

    /**
     * 
     * Border fill layout. Copied from AbstractInformationControl.
     * 
     * @author P.Tomaszewski
     */
    private static class BorderFillLayout extends Layout {

        /** The border widths. */
        final int fBorderSize;

        /**
         * Creates a fill layout with a border.
         * 
         * @param borderSize
         *            the border size
         */
        public BorderFillLayout(int borderSize) {
            if (borderSize < 0)
                throw new IllegalArgumentException();
            fBorderSize = borderSize;
        }

        /**
         * Returns the border size.
         * 
         * @return the border size
         */
        public int getBorderSize() {
            return fBorderSize;
        }

        /**
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
         *      int, int, boolean)
         */
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {

            Control[] children = composite.getChildren();
            Point minSize = new Point(0, 0);

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    Point size = children[i].computeSize(wHint, hHint,
                            flushCache);
                    minSize.x = Math.max(minSize.x, size.x);
                    minSize.y = Math.max(minSize.y, size.y);
                }
            }

            minSize.x += fBorderSize * 2 + RIGHT_MARGIN;
            minSize.y += fBorderSize * 2;

            return minSize;
        }

        /**
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
         */
        protected void layout(Composite composite, boolean flushCache) {

            final Control[] children = composite.getChildren();
            final Point minSize = new Point(composite.getClientArea().width,
                    composite.getClientArea().height);

            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    final Control child = children[i];
                    child.setSize(minSize.x - fBorderSize * 2, minSize.y
                            - fBorderSize * 2);
                    child.setLocation(fBorderSize, fBorderSize);
                }
            }
        }
    }

    /**
     * A-Z Sorter for outline.
     * 
     * @author P.Tomaszewski
     */
    private class OutlineSorter extends ViewerSorter {
        /**
         * @see org.eclipse.jface.viewers.ViewerSorter#sort(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object[])
         */
        public void sort(Viewer viewer, Object[] elements) {
            if (fSort) {
                super.sort(viewer, elements);
            }
        }

        /**
         * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public int compare(Viewer viewer, Object e1, Object e2) {
            int result = 0;
            if (e1 instanceof CElementGrouping && e2 instanceof CElement) {
                result = -1;
            } else if (e1 instanceof CElement && e2 instanceof CElementGrouping) {
                result = 1;
            } else {
                int elType1;
                int elType2;
                String elName1;
                String elName2;
                if (e1 instanceof CElement && e2 instanceof CElement) {
                    CElement cel1 = (CElement) e1;
                    CElement cel2 = (CElement) e2;
                    elType1 = cel1.getElementType();
                    elType2 = cel2.getElementType();
                    elName1 = cel1.getElementName();
                    elName2 = cel2.getElementName();

                } else {
                    CElementGrouping cel1 = (CElementGrouping) e1;
                    CElementGrouping cel2 = (CElementGrouping) e2;
                    elType1 = cel1.getType();
                    elType2 = cel2.getType();
                    elName1 = cel1.toString();
                    elName2 = cel2.toString();
                }
                if (elType1 == elType2) {
                    result = elName1.compareTo(elName2);
                } else {
                    if (elType1 > elType2) {
                        result = -1;
                    } else if (elType1 < elType2) {
                        result = 1;
                    } else {
                        result = 0;
                    }
                }
            }
            return result;
        }
    }

    /**
     * The view menu's Remember Size and Location action.
     */
    private class RememberBoundsAction extends Action {

        /**
         * Creates new action.
        */
        RememberBoundsAction() {
            super(ActionMessages.getString("COutlineInformationControl.viewMenu.remember.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
            setChecked(getSettings().getBoolean(STORE_RESTORE_LOCATION));
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            final IDialogSettings settings = getSettings();

            final boolean newValue = isChecked();
            
            // store new value
            settings.put(STORE_RESTORE_LOCATION, newValue);
            settings.put(STORE_RESTORE_SIZE, newValue);

            fIsDeactivationActive = true;
        }
    }

    /**
     * 
     * The view menu's Move action.
     *
     * @author P.Tomaszewski
     */
    private class MoveAction extends Action {

        /**
         * Creates new action.
         */
        MoveAction() {
            super(ActionMessages.getString("COutlineInformationControl.viewMenu.move.label"), IAction.AS_PUSH_BUTTON); //$NON-NLS-1$
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            final Tracker tracker= new Tracker(fShell.getDisplay(), SWT.NONE);
            tracker.setStippled(true);
            final Rectangle[] r= new Rectangle[] { fFilterText.getShell().getBounds() };
            tracker.setRectangles(r);
            if (tracker.open()) {
                fShell.setBounds(tracker.getRectangles()[0]);
            }
        }
    }

    /**
     * 
     * The view menu's Sort action.
     *
     * @author P.Tomaszewski
     */
    private class SortAction extends Action {

        /**
         * Creates new action.
         */
        SortAction() {
            super(ActionMessages.getString("COutlineInformationControl.viewMenu.sort.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
            setChecked(getSettings().getBoolean(STORE_SORT_ENABLED));
        }

        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        public void run() {
            final boolean newValue = isChecked();
            if (newValue) {
                fTreeViewer.setSorter(fSorter);
            } else {
                fTreeViewer.setSorter(null);
            }
            getSettings().put(STORE_SORT_ENABLED, newValue);
            fIsDeactivationActive = true;
        }
    }
    
}
