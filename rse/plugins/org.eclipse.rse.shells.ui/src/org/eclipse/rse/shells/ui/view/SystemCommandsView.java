/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.shells.ui.view;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.ISystemThemeConstants;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;

public class SystemCommandsView extends SystemTableView implements ISystemThemeConstants, IPropertyChangeListener
{
	public class CommandsViewFilter extends ViewerFilter
	{
		public boolean select(Viewer viewer, Object parent, Object element)
		{
			if (element instanceof IRemoteOutput)
			{
				IRemoteOutput remoteOutput = (IRemoteOutput) element;
				if (remoteOutput.getText().indexOf("BEGIN-END-TAG:") > -1)
				{
					return false;
				}
			}
			return true;
		}
	}

	private static int MAX_BUFFER = 20000;
	private Color _errColor;
	private Color _outColor;
	private Color _infColor;
	private Color _warColor;
	private Color _prmColor;
	private int _maxCharWidth = 256;

	public SystemCommandsView(Table table, ISystemMessageLine msgLine)
	{
		super(table, msgLine);
		addFilter(new CommandsViewFilter());
		updateTheme();
		int[] colWidths = new int[1];
		colWidths[0] = 1000;
		setLastColumnWidths(colWidths);
	}

	// overridden to produce custom provider
	protected SystemTableViewProvider getProvider()
	{
		if (_provider == null)
		{
			_provider = new SystemCommandsViewProvider();
		}
		return _provider;
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		// for now always update
		updateTheme();
	}

	public void updateTheme()
	{
		Table table = getTable();
		if (table != null)
		{
			IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
			Color bg = mgr.getCurrentTheme().getColorRegistry().get(REMOTE_COMMANDS_VIEW_BG_COLOR);
			Color fg = mgr.getCurrentTheme().getColorRegistry().get(REMOTE_COMMANDS_VIEW_FG_COLOR);
			Font fFont = mgr.getCurrentTheme().getFontRegistry().get(REMOTE_COMMANDS_VIEW_FONT);
			table.setBackground(bg);
			table.setForeground(fg);
			table.setFont(fFont);
			if (_errColor == null)
			{
				mgr.addPropertyChangeListener(this);
			}
			_errColor = mgr.getCurrentTheme().getColorRegistry().get(MESSAGE_ERROR_COLOR);
			_outColor = mgr.getCurrentTheme().getColorRegistry().get(REMOTE_COMMANDS_VIEW_FG_COLOR);
			_infColor = mgr.getCurrentTheme().getColorRegistry().get(MESSAGE_INFORMATION_COLOR);
			_warColor = mgr.getCurrentTheme().getColorRegistry().get(MESSAGE_WARNING_COLOR);
			_prmColor = mgr.getCurrentTheme().getColorRegistry().get(REMOTE_COMMANDS_VIEW_PROMPT_COLOR);
		}
	}

	public void refresh()
	{
		super.refresh();
		Table table = getTable();
		if (table != null && table.getItemCount() > 0)
		{
			TableItem lastItem = table.getItem(table.getItemCount() - 1);
			table.showItem(lastItem);
		}
	}

	public synchronized void updateChildren()
	{
		// updateTheme();
		Object input = getInput();
		if (input instanceof IRemoteCommandShell)
		{
			SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
			Table table = getTable();
			Object[] children = provider.getChildren(input);
			if (children != null && children.length > 0)
			{
				boolean needsLayout = false;
				TableItem[] tableItems = table.getItems();
				if (tableItems == null || tableItems.length == 0)
				{
					needsLayout = true;
				} else
				{
					int widest = provider.getMaxCharsInColumnZero();
					if (widest > _maxCharWidth)
					{
						needsLayout = true;
						_maxCharWidth = widest;
					}
				}
				synchronized (children)
				{
					int index = table.getItemCount();
					if (index > MAX_BUFFER)
					{
						table.setRedraw(false);
						clearFirstItems(children, index - (MAX_BUFFER / 2));
						table.setRedraw(true);
						provider.flushCache();
						internalRefresh(input);
					} else
					{
						// update previous item
						/*
						 if (index > 0 && (children.length > index - 1))
						 {
						 Object lastObject = children[index - 1];

						 TableItem lastItem = table.getItem(index - 1);
						 if (lastObject != null && lastItem != null)
						 {
						 colorItem(lastItem, lastObject);
						 updateItem(lastItem, lastObject);
						 }
						 }*/
						for (int i = index; i < children.length; i++)
						{
							Object child = children[i];
							if (child != null)
							{
								boolean isVisible = true;
								if (isVisible)
								{
									TableItem newItem = (TableItem) newItem(table, SWT.NONE, index);
									colorItem(newItem, child);
									updateItem(newItem, child);
									index++;
								}
							}
						}
						if (index > 0)
						{
							table.setTopIndex(index - 1);
						}
					}
				}
				if (needsLayout)
				{
					computeLayout(true);
				}
			}
		}
	}

	private void colorItem(TableItem newItem, Object child)
	{
		if (child instanceof IRemoteOutput)
		{
			IRemoteOutput rmtOutput = (IRemoteOutput) child;
			String type = rmtOutput.getType();
			if (type.equals("stderr") || type.equals("error"))
			{
				newItem.setForeground(_errColor);
			} else if (type.equals("warning"))
			{
				newItem.setForeground(_warColor);
			} else if (type.equals("informational"))
			{
				newItem.setForeground(_infColor);
			} else if (type.equals("prompt"))
			{
				newItem.setForeground(_prmColor);
			} else
			{
				newItem.setForeground(_outColor);
			}
		}
	}

	public void clearAllItems()
	{
		Object input = getInput();
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
		Object[] children = provider.getChildren(input);
		clearFirstItems(children, children.length);
	}

	public void dispose()
	{
		IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
		mgr.removePropertyChangeListener(this);
	}

	private void clearFirstItems(Object[] children, int items)
	{
		Table table = getTable();
		table.setRedraw(false);
		synchronized (table)
		{
			if (items > 0)
			{
				int count = table.getItemCount();
				if (count >= items)
				{
					table.remove(0, items - 1);
					IRemoteCommandShell input = (IRemoteCommandShell) getInput();
					// remove items from command
					for (int i = 0; i < items && i < children.length; i++)
					{
						Object item = children[i];
						input.removeOutput(item);
					}
				}
			}
		}
		table.setRedraw(true);
	}

	protected Item newItem(Widget parent, int flags, int ix)
	{
		if (parent instanceof Table)
		{
			return new TableItem((Table) parent, flags);
		}
		return null;
	}

	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		Object child = event.getSource();
		if (event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH)
		{
			if (child == getInput())
			{
				SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
				if (provider != null)
				{
					provider.flushCache();
					updateChildren();
					return;
				}
			}
		} else if (event.getType() == ISystemResourceChangeEvents.EVENT_ICON_CHANGE)
		{
			try
			{
				Widget w = findItem(child);
				if (w != null)
				{
					updateItem(w, child);
				}
			} catch (Exception e)
			{
			}
		}
		//super.systemResourceChanged(event);
	}

	protected Object getParentForContent(Object element)
	{
		return getAdapter(element).getParent(element);
	}
}