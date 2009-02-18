/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight   (IBM)        - [165680] "Show in Remote Shell View" does not work
 * Yu-Fen Kuo       (MontaVista) - Adapted from CommandsViewWorkbook
 * Anna Dushistova  (MontaVista) - Adapted from CommandsViewWorkbook
 * Yu-Fen Kuo       (MontaVista) - [227572] RSE Terminal doesn't reset the "connected" state when the shell exits
 * Martin Oberhuber (Wind River) - [227571] RSE Terminal should honor Encoding set on the IHost
 * Michael Scharf   (Wind River) - [236203] [rseterminal] Potentially UI blocking code in TerminalViewTab.createTabItem
 * Anna Dushistova  (MontaVista) - [244437] [rseterminal] Possible race condition when multiple Terminals are launched after each other                             
 * Martin Oberhuber (Wind River) - [247700] Terminal uses ugly fonts in JEE package
 ********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.views;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.terminals.ui.TerminalServiceHelper;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionClearAll;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCopy;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCut;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionSelectAll;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;

/**
 * This is the desktop view wrapper of the System View viewer.
 */
public class TerminalViewTab extends Composite {

	public static String DATA_KEY_CONTROL = "$_control_$"; //$NON-NLS-1$

	private final CTabFolder tabFolder;

	private IPropertyChangeListener propertyChangeListener;

	private Menu menu;

	private boolean fMenuAboutToShow;

	private TerminalActionCopy fActionEditCopy;

	private TerminalActionCut fActionEditCut;

	private TerminalActionPaste fActionEditPaste;

	private TerminalActionClearAll fActionEditClearAll;

	private TerminalActionSelectAll fActionEditSelectAll;

	protected class TerminalContextMenuHandler implements MenuListener,
			IMenuListener {
		public void menuHidden(MenuEvent event) {
			fMenuAboutToShow = false;
			fActionEditCopy.updateAction(fMenuAboutToShow);
		}

		public void menuShown(MenuEvent e) {

		}

		public void menuAboutToShow(IMenuManager menuMgr) {
			fMenuAboutToShow = true;
			fActionEditCopy.updateAction(fMenuAboutToShow);
			fActionEditCut.updateAction(fMenuAboutToShow);
			fActionEditSelectAll.updateAction(fMenuAboutToShow);
			fActionEditPaste.updateAction(fMenuAboutToShow);
			fActionEditClearAll.updateAction(fMenuAboutToShow);
		}
	}

	public TerminalViewTab(final Composite parent, TerminalViewer viewer) {
		super(parent, SWT.NONE);
		tabFolder = new CTabFolder(this, SWT.NONE);
		tabFolder.setLayout(new FillLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		setLayout(new FillLayout());
		tabFolder.setBackground(parent.getBackground());
		tabFolder.setSimple(false);
		tabFolder.setUnselectedImageVisible(false);
		tabFolder.setUnselectedCloseVisible(false);

		tabFolder.setMinimizeVisible(false);
		tabFolder.setMaximizeVisible(false);
		setupActions();
	}

	public void dispose() {
		if (propertyChangeListener != null) {
			IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
			mgr.removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
		}
		if (!tabFolder.isDisposed()) {
			tabFolder.dispose();
		}
		super.dispose();
	}

	public CTabFolder getFolder() {
		return tabFolder;
	}

	public void remove(Object root) {

	}

	public CTabItem getSelectedTab() {
		if (tabFolder.getItemCount() > 0) {
			int index = tabFolder.getSelectionIndex();
			CTabItem item = tabFolder.getItem(index);
			return item;
		}

		return null;
	}

	public void showCurrentPage() {
		tabFolder.setFocus();
	}

	public void showPageFor(Object root) {
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			CTabItem item = tabFolder.getItem(i);
			if (item.getData() == root) {
				tabFolder.setSelection(item);
			}

		}
	}

	public void showPageFor(String tabName) {
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			CTabItem item = tabFolder.getItem(i);
			if (item.getText().equals(tabName)) {
				tabFolder.setSelection(item);
				return;
			}

		}
	}

	public void disposePageFor(String tabName) {
		for (int i = 0; i < tabFolder.getItemCount(); i++) {
			CTabItem item = tabFolder.getItem(i);
			if (item.getText().equals(tabName)) {
				item.dispose();
				return;
			}

		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		// for now always update
		if (tabFolder!=null) {
			CTabItem[] items = tabFolder.getItems();
			for (int i=0; i<items.length; i++) {
				Object control = items[i].getData(DATA_KEY_CONTROL);
				if (control instanceof ITerminalViewControl) {
					updateTheme((ITerminalViewControl) control);
				}
			}
		}
	}

	public void updateTheme(final ITerminalViewControl control) {
		if (control != null) {
			IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
			Font font;
			FontRegistry fr = mgr.getCurrentTheme().getFontRegistry();
			if (fr.hasValueFor("terminal.views.view.font.definition")) { //$NON-NLS-1$
				//Terminal View font if available
				font = fr.get("terminal.views.view.font.definition"); //$NON-NLS-1$
			} else if (fr.hasValueFor("REMOTE_COMMANDS_VIEW_FONT")) { //$NON-NLS-1$
				//fallback: "Remote Shell Font"
				font = fr.get("REMOTE_COMMANDS_VIEW_FONT"); //$NON-NLS-1$
			} else {
				//fallback: "Basic Text Font"
				font = fr.get("org.eclipse.jface.textfont"); //$NON-NLS-1$
			}
			control.setFont(font);
			if (propertyChangeListener == null) {
				final TerminalViewTab myself = this;
				propertyChangeListener = new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						myself.propertyChange(event);
					}
				};
				mgr.addPropertyChangeListener(propertyChangeListener);
			}
		}
	}

	public CTabItem createTabItem(IAdaptable root,
			final String initialWorkingDirCmd) {
		final CTabItem item = new CTabItem(tabFolder, SWT.CLOSE);
		setTabTitle(root, item);

		item.setData(root);
		Composite c = new Composite(tabFolder, SWT.NONE);
		c.setLayout(new FillLayout());

		tabFolder.getParent().layout(true);
		if (root instanceof IHost) {
			final IHost host = (IHost) root;

			ITerminalConnector connector = new RSETerminalConnector(host);
			ITerminalViewControl terminalControl = TerminalViewControlFactory
					.makeControl(new ITerminalListener() {

						public void setState(final TerminalState state) {
							if (state == TerminalState.CLOSED
									|| state == TerminalState.CONNECTED) {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										if (!item.isDisposed()) {
											final ITerminalServiceSubSystem terminalServiceSubSystem = TerminalServiceHelper
													.getTerminalSubSystem(host);

											if (state == TerminalState.CONNECTED)
												TerminalServiceHelper
														.updateTerminalShellForTerminalElement(item);

											setTabImage(host, item);
											ISystemRegistry registry = RSECorePlugin
													.getTheSystemRegistry();
											registry
													.fireEvent(new SystemResourceChangeEvent(
															terminalServiceSubSystem,
															ISystemResourceChangeEvents.EVENT_REFRESH,
															terminalServiceSubSystem));
										}
										if (state == TerminalState.CONNECTED) {

											if (initialWorkingDirCmd != null) {
												Object data = item
														.getData(DATA_KEY_CONTROL);
												if (data instanceof ITerminalViewControl)
													((ITerminalViewControl) data)
															.pasteString(initialWorkingDirCmd);
											}
										}
									}
								});
							}

						}

						public void setTerminalTitle(String title) {

						}
					}, c, new ITerminalConnector[] { connector });
			// Specify Encoding for Terminal
			try {
				terminalControl.setEncoding(host.getDefaultEncoding(true));
			} catch (UnsupportedEncodingException e) {
				/* ignore and allow fallback to default encoding */
			}
			terminalControl.setConnector(connector);
			item.setData(DATA_KEY_CONTROL, terminalControl);
			updateTheme(terminalControl);
			terminalControl.connectTerminal();
		}
		item.setControl(c);
		tabFolder.setSelection(item);
		item.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				Object source = e.getSource();
				if (source instanceof CTabItem) {
					CTabItem currentItem = (CTabItem) source;
					Object data = currentItem.getData(DATA_KEY_CONTROL);
					if (data instanceof ITerminalViewControl) {
						((ITerminalViewControl) data).disposeTerminal();
					}
					data = currentItem.getData();
					if (data instanceof IHost) {
						TerminalServiceHelper.removeTerminalElementFromHost(
								currentItem, (IHost) data);
					}
				}

			}

		});

		setupContextMenus();
		return item;

	}

	protected void setupActions() {
		fActionEditCopy = new TerminalActionCopy() {
			protected ITerminalViewControl getTarget() {
				return getCurrentTerminalViewControl();
			}
		};
		fActionEditCut = new TerminalActionCut() {
			protected ITerminalViewControl getTarget() {
				return getCurrentTerminalViewControl();
			}
		};
		fActionEditPaste = new TerminalActionPaste() {
			protected ITerminalViewControl getTarget() {
				return getCurrentTerminalViewControl();
			}
		};
		fActionEditClearAll = new TerminalActionClearAll() {
			protected ITerminalViewControl getTarget() {
				return getCurrentTerminalViewControl();
			}
		};
		fActionEditSelectAll = new TerminalActionSelectAll() {
			protected ITerminalViewControl getTarget() {
				return getCurrentTerminalViewControl();
			}
		};
	}

	protected void setupContextMenus() {
		ITerminalViewControl terminalViewControl = getCurrentTerminalViewControl();
		if (terminalViewControl == null)
			return;

		if (menu == null) {
			MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			menu = menuMgr.createContextMenu(tabFolder);
			loadContextMenus(menuMgr);
			TerminalContextMenuHandler contextMenuHandler = new TerminalContextMenuHandler();
			menuMgr.addMenuListener(contextMenuHandler);
			menu.addMenuListener(contextMenuHandler);
		}
		Control ctlText = terminalViewControl.getControl();
		ctlText.setMenu(menu);
	}

	protected void loadContextMenus(IMenuManager menuMgr) {
		menuMgr.add(fActionEditCopy);
		menuMgr.add(fActionEditPaste);
		menuMgr.add(new Separator());
		menuMgr.add(fActionEditClearAll);
		menuMgr.add(fActionEditSelectAll);
		menuMgr.add(new Separator());

		// Other plug-ins can contribute there actions here
		menuMgr.add(new Separator("Additions")); //$NON-NLS-1$
	}

	private void setTabTitle(IAdaptable root, CTabItem titem) {
		ISystemViewElementAdapter va = (ISystemViewElementAdapter) root
				.getAdapter(ISystemViewElementAdapter.class);
		if (va != null) {
			updateWithUniqueTitle(va.getName(root), titem);
			setTabImage(root, titem);
		}
	}

	private void setTabImage(IAdaptable root, CTabItem titem) {
		ISystemViewElementAdapter va = (ISystemViewElementAdapter) root
				.getAdapter(ISystemViewElementAdapter.class);
		if (va != null) {
			if (root instanceof IHost) {
				ITerminalServiceSubSystem terminalServiceSubSystem = TerminalServiceHelper
						.getTerminalSubSystem((IHost) root);
				TerminalElement element = terminalServiceSubSystem
						.getChild(titem.getText());
				if (element != null) {
					va = (ISystemViewElementAdapter) element
							.getAdapter(ISystemViewElementAdapter.class);
					titem
							.setImage(va.getImageDescriptor(element)
									.createImage());
					return;
				}
			}

			titem.setImage(va.getImageDescriptor(root).createImage());
		}
	}

	private void updateWithUniqueTitle(String title, CTabItem currentItem) {
		CTabItem[] items = tabFolder.getItems();
		int increment = 1;
		String temp = title;
		for (int i = 0; i < items.length; i++) {
			if (items[i] != currentItem) {
				String name = items[i].getText();
				if (name != null) {
					if (name.equals(temp)) {
						temp = title + " " + increment++; //$NON-NLS-1$
					}
				}

			}
		}
		currentItem.setText(temp);
	}

	private ITerminalViewControl getCurrentTerminalViewControl() {
		if (tabFolder != null && !tabFolder.isDisposed()) {
			CTabItem item = tabFolder.getSelection();
			if (item != null && !item.isDisposed()) {
				Object data = item.getData(DATA_KEY_CONTROL);
				if (data instanceof ITerminalViewControl)
					return ((ITerminalViewControl) data);
			}
		}
		return null;
	}
}
