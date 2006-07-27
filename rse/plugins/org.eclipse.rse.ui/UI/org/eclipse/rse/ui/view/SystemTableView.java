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

package org.eclipse.rse.ui.view;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPopupMenuActionContributorManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvent;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemRemoteChangeListener;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.services.clientserver.StringCompare;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.ui.actions.SystemCommonSelectAllAction;
import org.eclipse.rse.ui.actions.SystemOpenExplorerPerspectiveAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.actions.SystemRemotePropertiesAction;
import org.eclipse.rse.ui.actions.SystemShowInTableAction;
import org.eclipse.rse.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;


/**
 * This subclass of the standard JFace table viewer is used to
 * show a generic table view of the selected object in the Systems view
 * <p>
 * 
 * TableViewer comes from com.ibm.jface.viewer
 */
public class SystemTableView
	extends TableViewer
	implements IMenuListener, ISystemDeleteTarget, ISystemRenameTarget, ISystemSelectAllTarget, ISystemResourceChangeListener, ISystemRemoteChangeListener, ISelectionChangedListener, ISelectionProvider
{


	// inner class to support cell editing	
	private ICellModifier cellModifier = new ICellModifier()
	{
		public Object getValue(Object element, String property)
		{
			ISystemViewElementAdapter adapter = getAdapter(element);
			adapter.setPropertySourceInput(element);
			Object value = adapter.getPropertyValue(property);
			if (value == null)
			{
				value = "";
			}
			return value;
		}

		public boolean canModify(Object element, String property)
		{
			boolean modifiable = true;
			return modifiable;
		}

		public void modify(Object element, String property, Object value)
		{
			if (element instanceof TableItem && value != null)
			{
				Object obj = ((TableItem) element).getData();
				ISystemViewElementAdapter adapter = getAdapter(obj);
				if (adapter != null)
				{
					adapter.setPropertyValue(property, value);

					SelectionChangedEvent event = new SelectionChangedEvent(SystemTableView.this, getSelection());

					// fire the event
					fireSelectionChanged(event);
				}
			}
		}
	};

	private class HeaderSelectionListener extends SelectionAdapter
	{
	
	    public HeaderSelectionListener()
	    {
	        _upI = RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_MOVEUP_ID);
	        _downI = RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_MOVEDOWN_ID);
	    }
	  
	    
		/**
		 * Handles the case of user selecting the
		 * header area.
		 * <p>If the column has not been selected previously,
		 * it will set the sorter of that column to be
		 * the current table view sorter. Repeated
		 * presses on the same column header will
		 * toggle sorting order (ascending/descending).
		 */
		public void widgetSelected(SelectionEvent e)
		{
			Table table = getTable();
			if (!table.isDisposed())
			{
				// column selected - need to sort
			    TableColumn tcolumn = (TableColumn)e.widget;
				int column = table.indexOf(tcolumn);
				SystemTableViewSorter oldSorter = (SystemTableViewSorter) getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber())
				{
					oldSorter.setReversed(!oldSorter.isReversed());
					if (tcolumn.getImage() == _upI)
					{
					    tcolumn.setImage(_downI);
					}
					else
					{
					    tcolumn.setImage(_upI);
					}
				} 
				else
				{
					setSorter(new SystemTableViewSorter(column, SystemTableView.this, _columnManager));
					tcolumn.setImage(_downI);
				}
				
				// unset image of other columns
				TableColumn[] allColumns = table.getColumns();
				for (int i = 0; i < allColumns.length; i++)
				{
				    if (i != column)
				    {
				        if (allColumns[i].getImage() != null)
				        {
				            allColumns[i].setImage(null);
				        }
				    }
				}
				refresh();
			}
		}
	}

	private Object _objectInput;
	private TableLayout _layout;
	protected SystemTableViewProvider _provider;
	private HeaderSelectionListener _columnSelectionListener;
	private MenuManager _menuManager;
	private SystemTableViewFilter _filter;
	private IPropertyDescriptor[] _uniqueDescriptors;
	private SystemTableViewColumnManager _columnManager;

	// these variables were copied from SystemView to allow for limited support
	// of actions.  I say limited because somethings don't yet work properly.
	protected SystemRefreshAction _refreshAction;
	protected PropertyDialogAction _propertyDialogAction;
	protected SystemRemotePropertiesAction _remotePropertyDialogAction;
	protected SystemOpenExplorerPerspectiveAction _openToPerspectiveAction;
	protected SystemShowInTableAction _showInTableAction;

	// global actions
	// Note the Edit menu actions are set in SystemViewPart. Here we use these
	//   actions from our own popup menu actions.
	protected SystemCommonDeleteAction _deleteAction;
	// for global delete menu item	
	protected SystemCommonRenameAction _renameAction;
	// for common rename menu item	
	protected SystemCommonSelectAllAction _selectAllAction;
	// for common Ctrl+A select-all

	protected boolean _selectionShowRefreshAction;
	protected boolean _selectionShowOpenViewActions;
	protected boolean _selectionShowDeleteAction;
	protected boolean _selectionShowRenameAction;
	protected boolean _selectionEnableDeleteAction;
	protected boolean _selectionEnableRenameAction;

	protected boolean _selectionIsRemoteObject = true;
	protected boolean _selectionFlagsUpdated = false;

	private IWorkbenchPart _workbenchPart = null;
	private ISystemMessageLine _messageLine;

	private int[] _lastWidths = null;
	private int   _charWidth = 3;
	
	private boolean _showColumns = true;

	 private  Image _upI;
	 private  Image _downI;

	protected boolean     menuListenerAdded = false;
	
    private static final int LEFT_BUTTON = 1;
    private int mouseButtonPressed = LEFT_BUTTON;   
	/**
	 * Constructor for the table view
	 * 
	 */
	public SystemTableView(Table table, ISystemMessageLine msgLine)
	{
		super(table);
		_layout = new TableLayout();
		_messageLine = msgLine;
		
		_columnManager = new SystemTableViewColumnManager(this);
		_provider = getProvider();
		_columnSelectionListener = new HeaderSelectionListener();

		setContentProvider(_provider);
		
		setLabelProvider(new SystemDecoratingLabelProvider(_provider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));	
		//setLabelProvider(_provider);

		_filter = new SystemTableViewFilter();
		addFilter(_filter);
		
		_charWidth = table.getFont().getFontData()[0].getHeight() / 2;
		computeLayout();

		_menuManager = new MenuManager("#PopupMenu");
		_menuManager.setRemoveAllWhenShown(true);
		_menuManager.addMenuListener(this);
		Menu menu = _menuManager.createContextMenu(table);
		table.setMenu(menu);

		addSelectionChangedListener(this);

		RSEUIPlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);
		RSEUIPlugin.getTheSystemRegistry().addSystemRemoteChangeListener(this);

		initDragAndDrop();
	
		table.setVisible(false);
		
		// key listening for delete press
		getControl().addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				handleKeyPressed(e);
			}
		});
		getControl().addMouseListener(new MouseAdapter() 
				{
				   public void mouseDown(MouseEvent e) 
				   {
				   	    mouseButtonPressed =  e.button;   		  //d40615 	    
				   }
				});		
		
		
        _upI = RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_ARROW_UP_ID);
        _downI = RSEUIPlugin.getDefault().getImage(ISystemIconConstants.ICON_SYSTEM_ARROW_DOWN_ID);
	}
	
	protected SystemTableViewProvider getProvider()
	{
		if (_provider == null)
		{
			_provider = new SystemTableViewProvider(_columnManager);
		}
		return _provider;
	}
	
	public void showColumns(boolean flag)
	{
		_showColumns = flag;
	}
	

	public Layout getLayout()
	{
		return _layout;
	}

	public void setWorkbenchPart(IWorkbenchPart part)
	{
		_workbenchPart = part;
	}

	public void setViewFilters(String[] filter)
	{
		if (_filter.getFilters() != filter)
		{
			_filter.setFilters(filter);
			refresh();
		}
	}

	public String[] getViewFilters()
	{
		return _filter.getFilters();
	}

	/**
	 * Return the popup menu for the table
	 */
	public Menu getContextMenu()
	{
		return getTable().getMenu();
	}
	/**
	 * Return the popup menu for the table
	 */
	public MenuManager getContextMenuManager()
	{
		return _menuManager;
	}

	/**
	 * Called whenever the input for the view changes
	 */
	public void inputChanged(Object newObject, Object oldObject)
	{
		if (newObject instanceof IAdaptable)
		{
			getTable().setVisible(true);
			_objectInput = newObject;
			computeLayout();

			// reset the filter
			//setViewFilters(null);

			super.inputChanged(newObject, oldObject);

		}
		else if (newObject == null)
		{
			getTable().setVisible(false);
			_objectInput = null;
			computeLayout();

			setViewFilters(null);
		}
	}

	public Object getInput()
	{
		return _objectInput;
	}

	/**
	 * Convenience method for retrieving the view adapter for an object 
	 */
	protected ISystemViewElementAdapter getAdapter(Object obj)
	{
		ISystemViewElementAdapter adapter = SystemAdapterHelpers.getAdapter(obj, this);
		if (adapter != null)
			adapter.setPropertySourceInput(obj);
		return adapter;
	}

	/**
	 * Convenience method for retrieving the view adapter for an object's children 
	 */
	public ISystemViewElementAdapter getAdapterForContents()
	{
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
		Object[] children = provider.getChildren(getInput());
		if (children != null && children.length > 0)
		{
			IAdaptable child = (IAdaptable) children[0];
			return getAdapter(child);
		}
		return null;
	}

	/**
	 * Used to determine what the columns should be on the table.
	 */
	public IPropertyDescriptor[] getVisibleDescriptors(Object object)
		{
			SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
			Object[] children = provider.getChildren(object);
			return getVisibleDescriptors(children);
		}
		
	private IPropertyDescriptor[] getVisibleDescriptors(Object[] children)
		{			
			if (children != null && children.length > 0)
			{
				IAdaptable child = (IAdaptable) children[0];
				return getCustomDescriptors(getAdapter(child));
			}

			return new IPropertyDescriptor[0];
	}

	public SystemTableViewColumnManager getColumnManager()
	{
	    return _columnManager;
	}
	
	public IPropertyDescriptor getNameDescriptor(Object object)
	{
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
		Object[] children = provider.getChildren(object);
		return getNameDescriptor(children);
	}
	
	private IPropertyDescriptor getNameDescriptor(Object[] children)
		{
			if (children != null && children.length > 0)
			{
				IAdaptable child = (IAdaptable) children[0];
				return getAdapter(child).getPropertyDescriptors()[0];
			}

			return null;
		}

	/**
	 * Used to determine the formats of each descriptor.
	 */
	private ArrayList getFormatsIn()
	{
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
		Object[] children = provider.getChildren(_objectInput);
		return getFormatsIn(children);
	}
	
	private IPropertyDescriptor[] getCustomDescriptors(ISystemViewElementAdapter adapter)
	{
	    return _columnManager.getVisibleDescriptors(adapter);
	}

	private ArrayList getFormatsIn(Object[] children)
	{
		ArrayList results = new ArrayList();

		if (children != null && children.length > 0)
		{
			IAdaptable child = (IAdaptable) children[0];

			Object adapter = child.getAdapter(ISystemViewElementAdapter.class);
			if (adapter instanceof ISystemViewElementAdapter)
			{
				ISystemViewElementAdapter ad = (ISystemViewElementAdapter) adapter;
				ad.setPropertySourceInput(child);
				
				IPropertyDescriptor[] descriptors = getCustomDescriptors(ad);
				for (int i = 0; i < descriptors.length; i++)
				{
					IPropertyDescriptor descriptor = descriptors[i];

					try
					{
						Object key = descriptor.getId();

						Object propertyValue = ad.getPropertyValue(key, false);
						results.add(propertyValue.getClass());
					}
					catch (Exception e)
					{
						results.add(String.class);
					}

				}
			}
		}

		return results;
	}		

	public void computeLayout()
	{
		computeLayout(false);
	}

	private CellEditor getCellEditor(Table parent, IPropertyDescriptor descriptor)
	{
		CellEditor editor = descriptor.createPropertyEditor(parent);
		if (editor instanceof SystemInheritableTextCellEditor)
		{
			((SystemInheritableTextCellEditor) editor).getInheritableEntryField().setAllowEditingOfInheritedText(true);
		}

		return editor;
	}
	
	private boolean sameDescriptors(IPropertyDescriptor[] descriptors1, IPropertyDescriptor[] descriptors2)
	{
		if (descriptors1 == null || descriptors2 == null)
		{
			return false;
		}
		if (descriptors1.length == descriptors2.length)
		{
			boolean same = true;
			for (int i = 0; i < descriptors1.length && same; i++)
			{
				same = descriptors1[i] == descriptors2[i];
			}
			return same;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Determines what columns should be shown in this view. The columns may change
	 * anytime the view input changes.  The columns in the control are modified and
	 * columns may be added or deleted as necessary to make it conform to the 
	 * new data.
	 */
	public void computeLayout(boolean force)
	{
		if (_showColumns == false)
			return;
		if (_objectInput == null)
			return;
			
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
		Object[] children = provider.getChildren(_objectInput);

		// if no children, don't update
		if (children == null || children.length == 0)
		{
			return;
		}
		
		IPropertyDescriptor[] descriptors = getVisibleDescriptors(children);
		IPropertyDescriptor nameDescriptor = getNameDescriptor(children);
		
		int n = descriptors.length; // number of columns we need (name column + other columns)
		if (nameDescriptor != null)
			n += 1;
		if (n == 0)
			return; // there is nothing to lay out!

		
		if (sameDescriptors(descriptors,_uniqueDescriptors) && !force)
		{
			setLastColumnWidths(getCurrentColumnWidths());
			return;
		}
		_uniqueDescriptors = descriptors;
		Table table = getTable();
		if (table == null || table.isDisposed())
			return;

		// set column attributes, create new columns if necessary
		TableColumn[] columns = table.getColumns();
		int numColumns = columns.length; // number of columns in the control
		CellEditor editors[] = new CellEditor[n];
		String headings[] = new String[n];
		String propertyIds[] = new String[n];
		ArrayList formats = getFormatsIn();


		_layout = new TableLayout();
		for (int i = 0; i < n; i++)
		{ // for each column
			String name = null;
			String propertyId = null;
			CellEditor editor = null;
			int alignment = SWT.LEFT;
			int weight = 100;
			if (i == 0)
			{ 
				// this is the first column -- treat it special
				name = SystemPropertyResources.RESID_PROPERTY_NAME_LABEL;
				propertyId = (String) nameDescriptor.getId();
				editor = getCellEditor(table, nameDescriptor);
				weight = 200;
			}
			else
			{ // these columns come from the regular descriptors
				IPropertyDescriptor descriptor = descriptors[i - 1];

				Class format = (Class) formats.get(i - 1);
				name = descriptor.getDisplayName();
				propertyId = (String) descriptor.getId();
				editor = getCellEditor(table, descriptor);
				if (format != String.class)
					alignment = SWT.RIGHT;
			}
			TableColumn tc = null;
			if (i >= numColumns)
			{
				tc = new TableColumn(table, alignment, i);
				tc.setMoveable(true);
				tc.addSelectionListener(_columnSelectionListener);
			}
			else
			{
				tc = columns[i];
				tc.setAlignment(alignment);
			}
			_layout.addColumnData(new ColumnWeightData(weight));
			tc.setText(name);
			if (i == 0)
			{
			 //   tc.setImage(_downI);
			}
			headings[i] = name;
			editors[i] = editor;
			propertyIds[i] = propertyId;
		}
		setColumnProperties(propertyIds);
		setCellEditors(editors);
		setCellModifier(cellModifier);

		// dispose of any extra columns the table control may have
		for (int i = n; i < numColumns; i++)
		{
			columns[i].dispose();
			columns[i] = null;
		}

		// compute column widths
		columns = table.getColumns();
		numColumns = columns.length;
		Rectangle clientA = table.getClientArea();
		int totalWidth = clientA.width - 5;
		if (totalWidth <= 0)
		{
		    // find a default
		    totalWidth = 500;
		}
		

		int[] lastWidths = getLastColumnWidths();
		if (numColumns > 1)
		{
			// check if previous widths can be used	
			if (lastWidths != null && lastWidths.length == numColumns)
			{
				
				// use previously established widths
				setCurrentColumnWidths(lastWidths);
			}
			else
			{
			    if (totalWidth > 0)
			    {
					// no previous widths or number of columns has changed - need to calculate
					int averageWidth = totalWidth / numColumns;
					int firstWidth = Math.max(averageWidth, 150);
					averageWidth = (totalWidth - firstWidth) / (numColumns - 1);
					averageWidth = Math.max(averageWidth, 80);
					columns[0].setWidth(firstWidth);
					for (int i = 1; i < numColumns; i++)
					{
						
						columns[i].setWidth(averageWidth);
					}
					setLastColumnWidths(getCurrentColumnWidths());
			    }
			}
			table.setHeaderVisible(true);
		}
		else
		{ 
			
		    if (numColumns == 1) 
		    {	
		    	int width = totalWidth;
		    	if (lastWidths != null && lastWidths.length == 1)
		    	{
		    		width = (totalWidth > lastWidths[0]) ? totalWidth : lastWidths[0];
		    	}
		    	
		    	
		    	int maxWidth = provider.getMaxCharsInColumnZero() * _charWidth;
		    	if (maxWidth > width)
		    	{
		    		width = maxWidth;
		    	}
		    	
		        if (width > 0)
		        {
		            columns[0].setWidth(width);
		        }
		        table.setHeaderVisible(false);
		    }
		}
	}

	public int[] getCurrentColumnWidths()
	{
		Table table = getTable();
		if (table != null && !table.isDisposed())
		{
			int[] widths = new int[table.getColumnCount()];
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++)
			{
				widths[i] = columns[i].getWidth();
			}
			return widths;
		}

		return new int[0];
	}

	public void setCurrentColumnWidths(int[] widths)
	{
		Table table = getTable();
		if (table != null && !table.isDisposed())
		{
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < columns.length && i < widths.length; i++)
			{
				columns[i].setWidth(widths[i]);
			}
		}
	}

	public int[] getLastColumnWidths()
	{
		return _lastWidths;
	}

	public void setLastColumnWidths(int[] widths)
	{
		_lastWidths = widths;
	}

	protected void initDragAndDrop()
	{
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { PluginTransfer.getInstance(), TextTransfer.getInstance(), EditorInputTransfer.getInstance(), FileTransfer.getInstance()};

		addDragSupport(ops, transfers, new SystemViewDataDragAdapter((ISelectionProvider) this));
		addDropSupport(ops | DND.DROP_DEFAULT, transfers, new SystemViewDataDropAdapter(this));
	}

	/**
	 * Used to asynchronously update the view whenever properties change.
	 */
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{

		boolean madeChange = false;
		Object parent = event.getParent();
		Object child = event.getSource();
		int eventType = event.getType();
		switch (eventType)
		{
		 	case ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE:
		   	case ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE:
		   	case ISystemResourceChangeEvents.EVENT_CHANGE_FILTERSTRING_REFERENCE:
		    	{
		   	      if (_objectInput instanceof ISystemFilterReference)
		   	      {
		   	          if (child == ((ISystemFilterReference)_objectInput).getReferencedFilter())
		   	          {
			   	       	SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();
	
						if (provider != null)
						{
							if (!madeChange)
							{
								provider.flushCache();
								madeChange = true;
							}
	
							computeLayout();
							try
							{
								internalRefresh(_objectInput);
							}
							catch (Exception e)
							{
								SystemBasePlugin.logError(e.getMessage());
							}
						}

		   	          }
		   	      }		   	      
		   	    }
		   	    break;
			case ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE :
			case ISystemResourceChangeEvents.EVENT_PROPERTYSHEET_UPDATE :
			case ISystemResourceChangeEvents.EVENT_ICON_CHANGE:
				{
					try
					{
						Widget w = findItem(child);
						if (w != null)
						{					    
							updateItem(w, child);
						}
					}
					catch (Exception e)
					{
						
					}
				}
				return;
				//break;
			 
			case ISystemResourceChangeEvents.EVENT_DELETE:   	    	  
  	    	 case ISystemResourceChangeEvents.EVENT_DELETE_MANY:
  	    	  	{
  	    	      if (child instanceof ISystemFilterReference)
  	    	      {
  	    	          Widget w = findItem(child);
  	    	          if (w != null)
  	    	          {
  	    	              remove(child);
  	    	          }	
	   	    	  }
  	    	  	}
  	    	      break;  
			
  	    	 case ISystemResourceChangeEvents.EVENT_ADD :
			case ISystemResourceChangeEvents.EVENT_ADD_RELATIVE :
				{
					boolean addingConnection = (child instanceof IHost);
					if (_objectInput instanceof ISystemRegistry && addingConnection)
					{
						SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();

						if (provider != null)
						{
							if (!madeChange)
							{
								provider.flushCache();
								madeChange = true;
							}

							computeLayout();
							internalRefresh(_objectInput);
						}
					}
				}
				break;
				
			case ISystemResourceChangeEvents.EVENT_REFRESH:
			{
				  if (child == RSEUIPlugin.getTheSystemRegistry())
				  {
					  // treat this as refresh all
					  child = _objectInput;
				  }
			}
			default :
				break;

		}

		if (child == _objectInput || parent == _objectInput)
		{
			SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();

			if (provider != null)
			{
				if (!madeChange)
				{
					provider.flushCache();
					madeChange = true;
				}

				computeLayout();
				try
				{
					internalRefresh(_objectInput);
				}
				catch (Exception e)
				{
					SystemBasePlugin.logError(e.getMessage());
				}
			}
		}
	}

	/**
	 * This is the method in your class that will be called when a remote resource
	 *  changes. You will be called after the resource is changed.
	 * @see org.eclipse.rse.model.ISystemRemoteChangeEvent
	 */
	public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event)
	{
		boolean madeChange = false;
		int eventType = event.getEventType();
		Object remoteResourceParent = event.getResourceParent();
		Object remoteResource = event.getResource();
		boolean originatedHere = (event.getOriginatingViewer() == this);
		Vector remoteResourceNames = null;
		if (remoteResource instanceof Vector)
		{
			remoteResourceNames = (Vector) remoteResource;
			remoteResource = remoteResourceNames.elementAt(0);
		}
		String remoteResourceParentName = getRemoteResourceAbsoluteName(remoteResourceParent);
		String remoteResourceName = getRemoteResourceAbsoluteName(remoteResource);
		if (remoteResourceName == null)
			return;
		SystemTableViewProvider provider = (SystemTableViewProvider) getContentProvider();

		if (_objectInput instanceof ISystemContainer && ((ISystemContainer)_objectInput).isStale())
		{
		    provider.flushCache();
		    refresh();
		    return;
		}
		
		switch (eventType)
		{
			// --------------------------
			// REMOTE RESOURCE CHANGED...
			// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED :
				{
					if (remoteResourceParent == getInput())
					{
						Widget w = findItem(remoteResource);
						if (w != null)
						{
							updateItem(w, remoteResource);
						}

					}
				}
				break;

				// --------------------------
				// REMOTE RESOURCE CREATED...
				// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED :
				{
					String inputResourceName = getRemoteResourceAbsoluteName(getInput());
					if (remoteResourceParentName != null && remoteResourceParentName.equals(inputResourceName))
					{
						if (provider == null)
						{
							return;
						}
						if (!madeChange)
						{
							provider.flushCache();
							madeChange = true;
						}

						refresh();
					}
				}
				break;

				// --------------------------
				// REMOTE RESOURCE DELETED...
				// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED :
				{
					{
						Object dchild = remoteResource;

						ISystemViewElementAdapter dadapt = getAdapter(dchild);
						if (dadapt != null)
						{
							ISubSystem dSubSystem = dadapt.getSubSystem(dchild);
							String dkey = dadapt.getAbsoluteName(dchild);

							if (provider != null)
							{
								Object[] children = provider.getChildren(_objectInput);
								for (int i = 0; i < children.length; i++)
								{
									Object existingChild = children[i];
									if (existingChild != null)
									{
										ISystemViewElementAdapter eadapt = getAdapter(existingChild);
										ISubSystem eSubSystem = eadapt.getSubSystem(existingChild);

										if (dSubSystem == eSubSystem)
										{
											String ekey = eadapt.getAbsoluteName(existingChild);
											if (ekey.equals(dkey))
											{
												if (!madeChange)
												{
													provider.flushCache();
													madeChange = true;

													// do a full refresh
													refresh();
												}
											}
										}

									}
								}
							}
						}
					}

				}
				break;

				// --------------------------
				// REMOTE RESOURCE RENAMED...
				// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED :
				{
					String oldName = event.getOldName();
					Object child = event.getResource();

					if (provider != null)
					{
						Object[] previousResults = provider.getCache();
						if (previousResults != null)
						{
							for (int i = 0; i < previousResults.length; i++)
							{
								Object previousResult = previousResults[i];

								if (previousResult == child)
								{
									Widget widget = findItem(previousResult);
									if (widget != null)
									{
										widget.setData(child);
										updateItem(widget, child);
										return;
									}
								}
								else
								{
									String previousName = getAdapter(previousResult).getAbsoluteName(previousResult);

									if (previousName != null && previousName.equals(oldName))
									{
										provider.flushCache();
										internalRefresh(_objectInput);
										return;
									}
								}
							}

						}
					}
				}

				break;

				/*
				// --------------------------
				// REMOTE RESOURCE RENAMED...
				// --------------------------
				case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED :
				{
				if (remoteResourceParent == getInput())
				{
					if (provider != null)
					{
						provider.flushCache();
					}
				
					refresh();
				}
				
				}
				break;
				*/
		}
	}

	/**
	 * Turn a given remote object reference into a fully qualified absolute name
	 */
	private String getRemoteResourceAbsoluteName(Object remoteResource)
	{
		if (remoteResource == null)
			return null;
		String remoteResourceName = null;
		if (remoteResource instanceof String)
			remoteResourceName = (String) remoteResource;
		else
		{
			ISystemRemoteElementAdapter ra = getRemoteAdapter(remoteResource);
			if (ra == null)
				return null;
			remoteResourceName = ra.getAbsoluteName(remoteResource);
		}
		return remoteResourceName;
	}

	public void selectionChanged(SelectionChangedEvent event)
	{
	    IStructuredSelection sel = (IStructuredSelection)event.getSelection();		
		Object firstSelection = sel.getFirstElement();
		if (firstSelection == null)
		  return;
		
		_selectionFlagsUpdated = false;
		ISystemViewElementAdapter adapter = getAdapter(firstSelection);
		if (adapter != null)
		{
		   displayMessage(adapter.getStatusLineText(firstSelection));
		   if ((mouseButtonPressed == LEFT_BUTTON))   
		      adapter.selectionChanged(firstSelection);	
		}  
		else
		  clearMessage();
	}

	public void dispose()
	{
		removeSelectionChangedListener(this);
		RSEUIPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
		RSEUIPlugin.getTheSystemRegistry().removeSystemRemoteChangeListener(this);
		_menuManager.removeAll();

		Table table = getTable();

		if (!table.isDisposed())
		{
			table.removeAll();
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++)
			{
				TableColumn column = columns[i];
				if (column != null && !column.isDisposed())
				{
					column.removeSelectionListener(_columnSelectionListener);
					column.dispose();
					column = null;
				}
			}

			table.dispose();
		}
	}

	/*
	 * Everything below is basically stuff copied and pasted from SystemsView
	 * -There needs to be cleaning up of the below code as some of this stuff
	 * is broken for the table view
	 * 
	 *
	public void createStandardGroups(IMenuManager menu)
	{
		if (!menu.isEmpty())
			return;
		// simply sets partitions in the menu, into which actions can be directed.
		// Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
		// Deleted groups are not used yet.
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW));
		// new->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO));
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_EXPANDTO));
		// expand to->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPAND));
		// expand, collapse
		// goto into, go->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH));
		// open with->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BROWSEWITH));
		// browse with ->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN));
		// open xxx
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH));
		// work with->
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_SHOW));         // show->type hierarchy, in-navigator
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD));
		// build, rebuild, refresh
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE));
		// update, change
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE));
		// rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER));
		// move up, move down		
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE));
		// getters/setters, etc. Typically in editor
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH));
		// search
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION));
		// connection-related actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT));
		// get or put actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS));
		// actions queried from adapters
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS));
		// user or BP/ISV additions
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_VIEWER_SETUP)); // ? Probably View->by xxx, yyy
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_TEAM));
		// Team
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_COMPAREWITH));
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_REPLACEWITH));
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES));
		// Properties
	}

	/**
	  * Rather than pre-defining this common action we wait until it is first needed,
	  *  for performance reasons.
	  */
	protected PropertyDialogAction getPropertyDialogAction()
	{
		if (_propertyDialogAction == null)
		{
			_propertyDialogAction = new PropertyDialogAction(new SameShellProvider(getShell()), this);
			//propertyDialogAction.setToolTipText(" "); 
		}
		_propertyDialogAction.selectionChanged(getSelection());
		return _propertyDialogAction;
	}
	/**
	 * Rather than pre-defining this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	protected SystemRemotePropertiesAction getRemotePropertyDialogAction()
	{
		if (_remotePropertyDialogAction == null)
		{
			_remotePropertyDialogAction = new SystemRemotePropertiesAction(getShell());
		}
		_remotePropertyDialogAction.setSelection(getSelection());
		return _remotePropertyDialogAction;
	}
	/**
	 * Return the select All action
	 */
	protected IAction getSelectAllAction()
	{
		if (_selectAllAction == null)
			_selectAllAction = new SystemCommonSelectAllAction(getShell(), this, this);
		return _selectAllAction;
	}

	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	protected IAction getRenameAction()
	{
		if (_renameAction == null)
			_renameAction = new SystemCommonRenameAction(getShell(), this);
		return _renameAction;
	}
	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	protected IAction getDeleteAction()
	{
		if (_deleteAction == null)
			_deleteAction = new SystemCommonDeleteAction(getShell(), this);
		return _deleteAction;
	}

	/**
	* Return the refresh action
	*/
	protected IAction getRefreshAction()
	{
		if (_refreshAction == null)
			_refreshAction = new SystemRefreshAction(getShell());
		return _refreshAction;
	}
	/*
	 * Get the common "Open to->" action for opening a new Remote Systems Explorer view,
	 *  scoped to the currently selected object.
	 *
	protected SystemCascadingOpenToAction getOpenToAction()
	{
		if (openToAction == null)
		  openToAction = new SystemCascadingOpenToAction(getShell(),getWorkbenchWindow());
		return openToAction;
	} NOT USED YET */
	/**
	 * Get the common "Open to->" action for opening a new Remote Systems Explorer view,
	 *  scoped to the currently selected object.
	 */
	protected SystemOpenExplorerPerspectiveAction getOpenToPerspectiveAction()
	{
		if (_openToPerspectiveAction == null)
		{
			IWorkbench desktop = PlatformUI.getWorkbench();
			IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

			_openToPerspectiveAction = new SystemOpenExplorerPerspectiveAction(getShell(), win);
		}
		//getWorkbenchWindow());
		return _openToPerspectiveAction;
	}

	protected SystemShowInTableAction getShowInTableAction()
	{
		if (_showInTableAction == null)
		{
			_showInTableAction = new SystemShowInTableAction(getShell());
		}
		//getWorkbenchWindow());
		return _showInTableAction;
	}

	public Shell getShell()
	{
		return getTable().getShell();
	}

	/**
	 * Required method from ISystemDeleteTarget.
	 * Decides whether to even show the delete menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean showDelete()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionShowDeleteAction;
	}
	/**
	 * Required method from ISystemDeleteTarget
	 * Decides whether to enable the delete menu item. 
	 * Assumes scanSelections() has already been called
	 */
	public boolean canDelete()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionEnableDeleteAction;
	}

	/*
	 * Required method from ISystemDeleteTarget
	 */
	public boolean doDelete(IProgressMonitor monitor)
	{
		ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		//int selectedCount = selection.size();
		//Object multiSource[] = new Object[selectedCount];
		//int idx = 0;
		Object element = null;
		//Object parentElement = getSelectedParent();
		ISystemViewElementAdapter adapter = null;
		boolean ok = true;
		boolean anyOk = false;
		Vector deletedVector = new Vector();
		try
		{
			while (ok && elements.hasNext())
			{
				element = elements.next();
				//multiSource[idx++] = element;
				adapter = getAdapter(element);
				ok = adapter.doDelete(getShell(), element, monitor);
				if (ok)
				{
					anyOk = true;
					deletedVector.addElement(element);
				}
			}
		}
		catch (SystemMessageException exc)
		{
			SystemMessageDialog.displayErrorMessage(getShell(), exc.getSystemMessage());
			ok = false;
		}
		catch (Exception exc)
		{
			String msg = exc.getMessage();
			if ((msg == null) || (exc instanceof ClassCastException))
				msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_DELETING).makeSubstitution(element, msg));
			ok = false;
		}
		if (anyOk)
		{
			Object[] deleted = new Object[deletedVector.size()];
			for (int idx = 0; idx < deleted.length; idx++)
				deleted[idx] = deletedVector.elementAt(idx);
			if (_selectionIsRemoteObject)
				//sr.fireEvent(new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(deleted, ISystemResourceChangeEvent.EVENT_DELETE_REMOTE_MANY, null));
				sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, deletedVector, null, null, null, this);
			else
				sr.fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(deleted, ISystemResourceChangeEvents.EVENT_DELETE_MANY, getInput()));
		}
		return ok;
	}

	// ---------------------------
	// ISYSTEMRENAMETARGET METHODS
	// ---------------------------

	/**
	 * Required method from ISystemRenameTarget.
	 * Decides whether to even show the rename menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean showRename()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionShowRenameAction;
	}
	/**
	 * Required method from ISystemRenameTarget
	 * Decides whether to enable the rename menu item. 
	 * Assumes scanSelections() has already been called
	 */
	public boolean canRename()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionEnableRenameAction;
	}

	// default implementation
	// in default table, parent is input 
	protected Object getParentForContent(Object element)
	{
		return _objectInput;
	}

	/**
	* Required method from ISystemRenameTarget
	*/
	public boolean doRename(String[] newNames)
	{
		ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		int selectedCount = selection.size();
		Object element = null;

		ISystemViewElementAdapter adapter = null;
		ISystemRemoteElementAdapter remoteAdapter = null;
		String oldFullName = null;
		boolean ok = true;
		try
		{
			int nameIdx = 0;
			while (ok && elements.hasNext())
			{
				element = elements.next();
				adapter = getAdapter(element);
				Object parentElement = getParentForContent(element);

				remoteAdapter = getRemoteAdapter(element);
				if (remoteAdapter != null)
					oldFullName = remoteAdapter.getAbsoluteName(element);
				// pre-rename
				ok = adapter.doRename(getShell(), element, newNames[nameIdx++]);
				if (ok)
				{
					if (remoteAdapter != null)
					{
						// do rename here
						Widget widget = findItem(element);
						if (widget != null)
						{
							updateItem(widget, element);
						}

						//sr.fireEvent(new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(element, ISystemResourceChangeEvent.EVENT_RENAME_REMOTE, oldFullName));
						sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED, element, parentElement, remoteAdapter.getSubSystem(element), oldFullName, this);

					}
					else
						sr.fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(element, ISystemResourceChangeEvents.EVENT_RENAME, parentElement));
				}
			}
		}
		catch (SystemMessageException exc)
		{
			SystemMessageDialog.displayErrorMessage(getShell(), exc.getSystemMessage());
			ok = false;
		}
		catch (Exception exc)
		{
			//String msg = exc.getMessage();
			//if ((msg == null) || (exc instanceof ClassCastException))
			//  msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_RENAMING).makeSubstitution(element, exc),
			//msg),
			exc);
			ok = false;
		}
		return ok;
	}

	/**
	 * Returns the implementation of ISystemRemoteElement for the given
	 * object.  Returns null if this object does not adaptable to this.
	 */
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object o)
	{
		ISystemRemoteElementAdapter adapter = null;
		if (!(o instanceof IAdaptable))
			adapter = (ISystemRemoteElementAdapter) Platform.getAdapterManager().getAdapter(o, ISystemRemoteElementAdapter.class);
		else
			adapter = (ISystemRemoteElementAdapter) ((IAdaptable) o).getAdapter(ISystemRemoteElementAdapter.class);
		if ((adapter != null) && (adapter instanceof ISystemViewElementAdapter))
			 ((ISystemViewElementAdapter) adapter).setViewer(this);
		return adapter;
	}

	/**
	* Return true if select all should be enabled for the given object.
	* For a tree view, you should return true if and only if the selected object has children.
	* You can use the passed in selection or ignore it and query your own selection.
	*/
	public boolean enableSelectAll(IStructuredSelection selection)
	{
		return true;
	}
	/**
	 * When this action is run via Edit->Select All or via Ctrl+A, perform the
	 * select all action. For a tree view, this should select all the children 
	 * of the given selected object. You can use the passed in selected object
	 * or ignore it and query the selected object yourself. 
	 */
	public void doSelectAll(IStructuredSelection selection)
	{
		Table table = getTable();
		TableItem[] items = table.getItems();

		table.setSelection(items);
		Object[] objects = new Object[items.length];
		for (int idx = 0; idx < items.length; idx++)
			objects[idx] = items[idx].getData();
		fireSelectionChanged(new SelectionChangedEvent(this, new StructuredSelection(objects)));
	}

	public void menuAboutToShow(IMenuManager manager)
	{
		SystemView.createStandardGroups(manager);
	   	  
		fillContextMenu(manager);
		
		  if (!menuListenerAdded)
	   	    {
	   	      if (manager instanceof MenuManager)
	   	      {
	   	      	Menu m = ((MenuManager)manager).getMenu();
	   	      	if (m != null)
	   	      	{
	   	      		menuListenerAdded = true;
	   	      		SystemViewMenuListener ml = new SystemViewMenuListener();
	   	      		if (_messageLine != null)
	   	      		  ml.setShowToolTipText(true, _messageLine);
	   	      		m.addMenuListener(ml);
	   	      	}
	   	      }
	   	    }

	}

	public ISelection getSelection()
	{
		ISelection selection = super.getSelection();
		if (selection == null || selection.isEmpty())
		{
			// make the selection the parent
			ArrayList list = new ArrayList();
			if (_objectInput != null)
			{
				list.add(_objectInput);
				selection = new StructuredSelection(list);
			}
		}

		return selection;
	}

	public void fillContextMenu(IMenuManager menu)
	{
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		int selectionCount = selection.size();

		{

			// ADD COMMON ACTIONS...
			// no need for refresh of object in table
			//menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getRefreshAction());

			// COMMON RENAME ACTION...
			if (canRename())
			{
				if (showRename())
					menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getRenameAction());
			}

			// ADAPTER SPECIFIC ACTIONS   	      
			SystemMenuManager ourMenu = new SystemMenuManager(menu);

			Iterator elements = selection.iterator();
			Hashtable adapters = new Hashtable();
			while (elements.hasNext())
			{
				Object element = elements.next();
				ISystemViewElementAdapter adapter = getAdapter(element);
				adapters.put(adapter, element); // want only unique adapters
			}
			Enumeration uniqueAdapters = adapters.keys();
			Shell shell = getShell();
			while (uniqueAdapters.hasMoreElements())
			{
				ISystemViewElementAdapter nextAdapter = (ISystemViewElementAdapter) uniqueAdapters.nextElement();
				nextAdapter.addActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);

				if (nextAdapter instanceof AbstractSystemViewAdapter)
				{
						AbstractSystemViewAdapter aVA = (AbstractSystemViewAdapter)nextAdapter;
						// add remote actions
						aVA.addCommonRemoteActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);
						
						// add dynamic menu popups
						aVA.addDynamicPopupMenuActions(ourMenu, selection, shell,  ISystemContextMenuConstants.GROUP_ADDITIONS);
				}
			}

			// wail through all actions, updating shell and selection
			IContributionItem[] items = menu.getItems();
			for (int idx = 0; idx < items.length; idx++)
			{
				if ((items[idx] instanceof ActionContributionItem) && (((ActionContributionItem) items[idx]).getAction() instanceof ISystemAction))
				{
					ISystemAction item = (ISystemAction) (((ActionContributionItem) items[idx]).getAction());
					//item.setShell(getShell());	      	   
					//item.setSelection(selection);
					//item.setViewer(this);
					item.setInputs(getShell(), this, selection);
				}
				else if (items[idx] instanceof SystemSubMenuManager)
				{
					SystemSubMenuManager item = (SystemSubMenuManager) items[idx];
					//item.setShell(getShell());
					//item.setSelection(selection); 	
					//item.setViewer(this); 	
					item.setInputs(getShell(), this, selection);
				}
			}

			// COMMON DELETE ACTION...
			if (canDelete() && showDelete())
			{
				//menu.add(getDeleteAction());
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getDeleteAction());
				((ISystemAction) getDeleteAction()).setInputs(getShell(), this, selection);
				menu.add(new Separator());
			}

			// PROPERTIES ACTION...
			// This is supplied by the system, so we pretty much get it for free. It finds the
			// registered propertyPages extension points registered for the selected object's class type.
			//propertyDialogAction.selectionChanged(selection);		  

			if (!_selectionIsRemoteObject) // is not a remote object
			{
				PropertyDialogAction pdAction = getPropertyDialogAction();
				if (pdAction.isApplicableForSelection())
				{

					menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
				}
				// OPEN IN NEW PERSPECTIVE ACTION... if (fromSystemViewPart && showOpenViewActions())
				{
					//SystemCascadingOpenToAction openToAction = getOpenToAction();
					SystemOpenExplorerPerspectiveAction openToPerspectiveAction = getOpenToPerspectiveAction();
					SystemShowInTableAction showInTableAction = getShowInTableAction();
					openToPerspectiveAction.setSelection(selection);
					showInTableAction.setSelection(selection);
					//menu.appendToGroup(ISystemContextMenuConstants.GROUP_OPEN, openToAction.getSubMenu());
					menu.appendToGroup(ISystemContextMenuConstants.GROUP_OPEN, openToPerspectiveAction);
					menu.appendToGroup(ISystemContextMenuConstants.GROUP_OPEN, showInTableAction);

				}
			}
			else // is a remote object
				{
				//Object firstSelection = selection.getFirstElement();
				//ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(firstSelection);
				//logMyDebugMessage(this.getClass().getName(), ": there is a remote adapter");
				SystemRemotePropertiesAction pdAction = getRemotePropertyDialogAction();
				if (pdAction.isApplicableForSelection())
					menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
				//else
				//logMyDebugMessage(this.getClass().getName(), ": but it is not applicable for selection");          	
				// --------------------------------------------------------------------------------------------------------------------
				// look for and add any popup menu actions registered via our org.eclipse.rse.core.popupMenus extension point...
				// --------------------------------------------------------------------------------------------------------------------
				if (_workbenchPart != null)
				{
					SystemPopupMenuActionContributorManager.getManager().contributeObjectActions(_workbenchPart, ourMenu, this, null);
				}
			}

		}
	}

	/**
	 * --------------------------------------------------------------------------------
	 * For many actions we have to walk the selection list and examine each selected
	 *  object to decide if a given common action is supported or not.
	 * <p>
	 * Walking this list multiple times while building the popup menu is a performance
	 *  hit, so we have this common method that does it only once, setting instance
	 *  variables for all of the decisions we are in interested in.
	 * --------------------------------------------------------------------------------
	 */
	protected void scanSelections()
	{
		// initial these variables to true. Then if set to false even once, leave as false always...
		_selectionShowRefreshAction = true;
		_selectionShowOpenViewActions = true;
		_selectionShowDeleteAction = true;
		_selectionShowRenameAction = true;
		_selectionEnableDeleteAction = true;
		_selectionEnableRenameAction = true;
		_selectionIsRemoteObject = true;
		_selectionFlagsUpdated = true;

		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		while (elements.hasNext())
		{
			Object element = elements.next();
			ISystemViewElementAdapter adapter = getAdapter(element);

			if (_selectionShowRefreshAction)
				_selectionShowRefreshAction = adapter.showRefresh(element);

			if (_selectionShowOpenViewActions)
				_selectionShowOpenViewActions = adapter.showOpenViewActions(element);

			if (_selectionShowDeleteAction)
				_selectionShowDeleteAction = adapter.showDelete(element);

			if (_selectionShowRenameAction)
				_selectionShowRenameAction = adapter.showRename(element);

			if (_selectionEnableDeleteAction)
				_selectionEnableDeleteAction = _selectionShowDeleteAction && adapter.canDelete(element);
			//System.out.println("ENABLE DELETE SET TO " + selectionEnableDeleteAction);

			if (_selectionEnableRenameAction)
				_selectionEnableRenameAction = _selectionShowRenameAction && adapter.canRename(element);

			if (_selectionIsRemoteObject)
				_selectionIsRemoteObject = (getRemoteAdapter(element) != null);
		}

	}

	public void positionTo(String name)
	{
		ArrayList selectedItems = new ArrayList();
		Table table = getTable();
		int topIndex = 0;
		for (int i = 0; i < table.getItemCount(); i++)
		{
			TableItem item = table.getItem(i);
			Object data = item.getData();
			if (data instanceof IAdaptable)
			{
				ISystemViewElementAdapter adapter = getAdapter(data);
				String itemName = adapter.getName(data);

				if (StringCompare.compare(name, itemName, false))
				{
					if (topIndex == 0)
					{
						topIndex = i;
					}
					selectedItems.add(item);
				}
			}
		}

		if (selectedItems.size() > 0)
		{
			TableItem[] tItems = new TableItem[selectedItems.size()];
			for (int i = 0; i < selectedItems.size(); i++)
			{
				tItems[i] = (TableItem) selectedItems.get(i);
			}

			table.setSelection(tItems);
			table.setTopIndex(topIndex);
			setSelection(getSelection(), true);
		}
	}

	void handleKeyPressed(KeyEvent event)
	{
		//System.out.println("Key Pressed");
		//System.out.println("...event character : " + event.character + ", "+(int)event.character);
		//System.out.println("...event state mask: " + event.stateMask);
		//System.out.println("...CTRL            : " + SWT.CTRL);
		if ((event.character == SWT.DEL) && (event.stateMask == 0) && (((IStructuredSelection) getSelection()).size() > 0))
		{
			scanSelections();
			/*  DKM - 53694
			if (showDelete() && canDelete())
			{
				SystemCommonDeleteAction dltAction = (SystemCommonDeleteAction) getDeleteAction();
				dltAction.setShell(getShell());
				dltAction.setSelection(getSelection());
				dltAction.setViewer(this);
				dltAction.run();
			}
			*/
		}
	}
	
	/**
	 * Display a message/status on the message/status line
	 */
	public void displayMessage(String msg)
	{
		if (_messageLine != null)
		  _messageLine.setMessage(msg);
	}
	/**
	 * Clear message/status shown on the message/status line
	 */
	public void clearMessage()
	{
		if (_messageLine != null)
		  _messageLine.clearMessage();
	}
	
}