/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.dialogs;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.StringComparePatternMatcher;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableView;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;


/**
 * @author dmcknigh
 */
public class SystemFilterTableDialog extends SystemPromptDialog implements KeyListener, IDoubleClickListener
{
    class InitialInputRunnable implements Runnable
    {
        public void run()
        {
            initInput();
        }
    }
    
	private SystemTableView _viewer;
	private Table table;
	private List _inputs;
	private IAdaptable  _currentInput;
	private String _lastFilter;
	private String _lastType;
	
	private String[]   _viewFilterStrings;
	private String[]   _typeFilterStrings;
	
	private ISubSystem _subSystem;
	
	private Combo _inputText;
	private Button _browseButton;
	
	private Combo _typeCombo;
	private Combo _filterCombo;
	
	private String selected = null;
	private boolean _allowInputChange = true;
	

	public SystemFilterTableDialog(Shell shell, String title, ISubSystem subSystem, String input, String[] viewFilterStrings, String[] typeFilterStrings, boolean allowInputChange)
	{
		super(shell, title);
		_subSystem = subSystem;
		setNeedsProgressMonitor(true);
		_inputs = new ArrayList();
		_inputs.add(input);
		_viewFilterStrings = viewFilterStrings;
		_typeFilterStrings = typeFilterStrings;
		_allowInputChange = allowInputChange;
	}
	
	
	public SystemFilterTableDialog(Shell shell, String title, ISubSystem subSystem, List inputs, String[] viewFilterStrings, String[] typeFilterStrings, boolean allowInputChange)
	{
		super(shell, title);
		_subSystem = subSystem;
		setNeedsProgressMonitor(true);
		_inputs = inputs;

		_viewFilterStrings = viewFilterStrings;
		_typeFilterStrings = typeFilterStrings;
		_allowInputChange = allowInputChange;
	}
	
	
	protected ISystemViewElementAdapter getAdatperFor(IAdaptable obj)
	{
	    return (ISystemViewElementAdapter)obj.getAdapter(ISystemViewElementAdapter.class);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.dialogs.SystemPromptDialog#createInner(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createInner(Composite parent) 
	{
			Composite c = new Composite(parent, SWT.NONE);

			GridLayout layout = new GridLayout();
			c.setLayout(layout);
			layout.numColumns =1;
			GridData gd = new GridData(GridData.FILL_BOTH);
			c.setLayoutData(gd);
			
			Composite inputC = new Composite(c, SWT.NONE);
			GridLayout ilayout = new GridLayout();
			inputC.setLayout(ilayout);
			
			if (_allowInputChange)
			{
				ilayout.numColumns =4;
			}
			else
			{
				ilayout.numColumns = 3;
			}
			
			GridData igd = new GridData(GridData.FILL_BOTH);
			inputC.setLayoutData(igd);
			
			// input
			Label objFilterLabel= SystemWidgetHelpers.createLabel(inputC, "Input");			
			_inputText = new Combo(inputC, SWT.DROP_DOWN | SWT.READ_ONLY);
			_inputText.addListener(SWT.Selection, this);		

			
			for (int i = 0; i < _inputs.size(); i++)
			{
			    String input = (String)_inputs.get(i);
			    if (input != null)
			    {
			        _inputText.add(input);
			    }
			}
			_inputText.select(0);

			if (_allowInputChange)
			{
				_browseButton = SystemWidgetHelpers.createPushButton(inputC, SystemResources.BUTTON_BROWSE, this);	
			}
			
			Composite filterC = new Composite(c, SWT.NONE);
			GridLayout flayout = new GridLayout();
			filterC.setLayout(flayout);
			flayout.numColumns =4;
			
			GridData fgd = new GridData(GridData.FILL_BOTH);
			filterC.setLayoutData(fgd);
			
			// type filter strings
			Label typeFilterLabel= SystemWidgetHelpers.createLabel(filterC, SystemPropertyResources.RESID_PROPERTY_TYPE_LABEL);			
			_typeCombo = new Combo(filterC, SWT.DROP_DOWN | SWT.READ_ONLY);
			for (int i = 0; i < _typeFilterStrings.length; i++)
			{
				if (null != _typeFilterStrings[i])
				{
					_typeCombo.add(_typeFilterStrings[i]);
				}
			}
			_typeCombo.select(0);
			_typeCombo.addKeyListener(this);
			_typeCombo.addListener(SWT.Selection, this);
			
			// view filter strings
			Label viewFilterLabel= SystemWidgetHelpers.createLabel(filterC, SystemResources.RESID_FILTERSTRING_STRING_LABEL);			
			_filterCombo = SystemWidgetHelpers.createCombo(filterC, this);
			_filterCombo.setText(_viewFilterStrings[0]);
			for (int i = 0; i < _viewFilterStrings.length; i++)
			{
				if (null != _viewFilterStrings[i])
				{
					_filterCombo.add(_viewFilterStrings[i]);
				}
			}
			_filterCombo.addKeyListener(this);
			
			// table
			table = new Table(c, SWT.BORDER);
			_viewer = new SystemTableView(table, this);
			_viewer.showColumns(false);
			_viewer.addDoubleClickListener(this);

			TableLayout tlayout = new TableLayout();
			table.setLayout(tlayout);
			table.setHeaderVisible(false);
			table.setLinesVisible(false);
			
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
			gridData.heightHint = 200;
			gridData.widthHint = 200;
			table.setLayoutData(gridData);

			


			enableOkButton(false);
			
			return c;
	}


	protected void initInput()
	{
	    if (_currentInput == null)
	    {
	        String input = (String)_inputs.get(0);
	        try
	        {
	        _currentInput = (IAdaptable)_subSystem.getObjectWithAbsoluteName(input);
	        	        
	        ISystemViewElementAdapter adapter = getAdatperFor(_currentInput);
	        if (adapter != null)
	        { 	
				applyViewFilter(false);
		    	_viewer.setInput(_currentInput);	
		    	_viewer.refresh();
	        }
	        }
	        catch (Exception e)
	        {
	            
	        }
	    }
	}		
	
	protected void applyViewFilter(boolean refresh)
	{
		String[] vfilters = new String[1];
		
		String typeFilter = _typeCombo.getText().toUpperCase();
		
		vfilters[0] = _filterCombo.getText().toUpperCase();	
		if (!vfilters[0].endsWith("*"))
			vfilters[0] += "*";
		
		if (_lastFilter != vfilters[0])
		{
			StringComparePatternMatcher matcher = new StringComparePatternMatcher(_lastFilter != null ?_lastFilter.toUpperCase() : null);
			if (_lastFilter == null || !matcher.stringMatches(vfilters[0]))
			{	
				_lastFilter = vfilters[0];
				_lastType = typeFilter;
				if (_currentInput != null)
				{				    
				    getAdatperFor(_currentInput).setFilterString(_lastFilter);
				}
		
			}
			else
			{
				_lastFilter = vfilters[0];
				_lastType = typeFilter;
			}			
			((SystemTableViewProvider)_viewer.getContentProvider()).flushCache();						
			String[] tfilters = new String[1];
			tfilters[0] = _lastFilter + typeFilter;
			_viewer.setViewFilters(tfilters);
		}
		else if (_lastType != typeFilter)
		{
		    _lastType = typeFilter;
			String[] tfilters = new String[1];
			tfilters[0] = _lastFilter + typeFilter;
			_viewer.setViewFilters(tfilters);
		}
		
	}
	


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.dialogs.SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
	    Display.getCurrent().asyncExec(new InitialInputRunnable());
	    //initInput();
		return _filterCombo;
	}

	public void handleEvent(Event e)
	{
	    Widget source = e.widget;

	    if (source == _typeCombo)
	    {
	        applyViewFilter(true);
	    }
	    else if (source == _filterCombo)
	    {
	    	if (_lastFilter == null || !_lastFilter.equals(_filterCombo.getText() + "*"))
			{
				applyViewFilter(true);
			}		
	    }
	    else if (source == _browseButton)
	    {
	    	SystemSelectAnythingDialog dlg = new SystemSelectAnythingDialog(getShell(), SystemResources.ACTION_SELECT_INPUT_DLG);
			dlg.setInputObject(_currentInput);   	
			if (dlg.open() == Window.OK)
			{
				_currentInput = (IAdaptable)dlg.getSelectedObject();
				ISystemViewElementAdapter adapter = getAdatperFor(_currentInput);
				String objName = adapter.getAbsoluteName(_currentInput);
				if (!_inputs.contains(objName))
				{
				    _inputs.add(0, objName);
				}
				
				_inputText.setText(objName);
				applyViewFilter(false);
				_viewer.setInput(_currentInput);
				_viewer.refresh();
			}
	    }
	    else if (source == _inputText)
	    {
	        int selected = _inputText.getSelectionIndex();
	        String inputStr = (String)_inputs.get(selected);
	        try
	        {
		        IAdaptable input = (IAdaptable)_subSystem.getObjectWithAbsoluteName(inputStr);
		        if (input != _currentInput)
		        {	            
		            _currentInput = input;
		        	ISystemViewElementAdapter adapter = getAdatperFor(_currentInput);
					_inputText.setText(inputStr);
					applyViewFilter(false);
					_viewer.setInput(_currentInput);
					_viewer.refresh();
		        }
	        }
	        catch (Exception e2)
	        {	            
	        }
	    }
	}

	public void keyPressed(KeyEvent e)
	{
	}
	
	public void keyReleased(KeyEvent e)
	{
		if (e.widget == _filterCombo)
		{
			String vfilter = _filterCombo.getText();	
			if (!vfilter.endsWith("*"))
				vfilter += "*";
			if (_lastFilter == null || !_lastFilter.equals(vfilter))
			{
			    //System.out.println("handling event");
			   // System.out.println("\tchar ="+e.character);
				applyViewFilter(true);
				_filterCombo.clearSelection();
				_filterCombo.setFocus();
			}
		}
	}
	public void doubleClick(DoubleClickEvent event)
	{
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (element == null)
			return;
		processOK();
		close();
	}

	
	protected boolean processOK() 
	{
		TableItem[] thisRow = table.getSelection();
		if (null != thisRow && thisRow.length == 1)
		{
			selected = thisRow[0].getText(0);
		}
		return true;
	}
	
	public String getSelected()
	{
		return selected;
	}
	

	

	
}