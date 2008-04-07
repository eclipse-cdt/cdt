/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * David McKnight (IBM) [196632] make sure selected element is marked selected
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 *******************************************************************************/

package org.eclipse.rse.ui.widgets.services;


import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.view.SystemPropertySheetForm;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.widgets.GridUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;



public class ServicesForm extends SystemBaseForm implements ICheckStateListener
{
	private CheckboxTableViewer _factoryViewer;
	private TreeViewer _serviceViewer;
	private SystemPropertySheetForm _propertiesViewer;
	
	private String _configurationTooltip = SystemResources.RESID_SERVICESFORM_CONFIGURATION_TOOLTIP;
	private String _serviceTooltip = SystemResources.RESID_SERVICESFORM_SERVICES_TOOLTIP;
	private String _propertiesTooltip = SystemResources.RESID_SERVICESFORM_PROPERTIES_TOOLTIP;
	
	private Text _descriptionVerbiage;
	
	public ServicesForm(Shell shell, ISystemMessageLine msgLine)
	{
		super(shell, msgLine);
		//_factoryTooltip = 
	}
	
	public Control createContents(Composite parent)
	{
		
		SashForm sashCompositeParent = new SashForm(parent, SWT.HORIZONTAL);
	    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	    sashCompositeParent.setLayoutData(data);
	    
		SashForm sashCompositeLeft = new SashForm(sashCompositeParent, SWT.VERTICAL);
	    data = new GridData(SWT.FILL, SWT.FILL, true, true);
	    sashCompositeLeft.setLayoutData(data);
	    
	    // factory composite
	    Composite factoryViewerComposite = new Composite(sashCompositeLeft, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        factoryViewerComposite.setLayout(layout);
        factoryViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    Label factoryLabel = new Label(factoryViewerComposite, SWT.NONE);
	    factoryLabel.setText(SystemResources.RESID_PROPERTIES_FACTORIES_LABEL);	    
	    factoryLabel.setToolTipText(SystemResources.RESID_PROPERTIES_FACTORIES_TOOLTIP);
		createFactoryViewer(factoryViewerComposite);
		
		
		// service composite
		Composite serviceViewerComposite = new Composite(sashCompositeLeft, SWT.NONE);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        serviceViewerComposite.setLayout(layout);
        serviceViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label servicesLabel = new Label(serviceViewerComposite, SWT.NONE);
	    servicesLabel.setText(SystemResources.RESID_PROPERTIES_SERVICES_LABEL);	    
		createServiceViewer(serviceViewerComposite);

		sashCompositeLeft.setWeights(new int[] { 50, 50 });
        
		// properties composite
		Composite servicePropertiesComposite = new Composite(sashCompositeParent, SWT.NONE);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        servicePropertiesComposite.setLayout(layout);
        servicePropertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label propertiesLabel = new Label(servicePropertiesComposite, SWT.NONE);
	    propertiesLabel.setText(SystemResources.RESID_PROPERTIES_PROPERTIES_LABEL);	
		createPropertiesViewer(servicePropertiesComposite);
	    
		sashCompositeParent.setWeights(new int[] { 40, 60 });
		
		// description composite
		Composite descriptionComposite = new Composite(parent, SWT.NONE);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        descriptionComposite.setLayout(layout);
        descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
	    descriptionLabel.setText(SystemResources.RESID_PROPERTIES_DESCRIPTION_LABEL);
		_descriptionVerbiage = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
        _descriptionVerbiage.setText(getCurrentVerbiage());
        _descriptionVerbiage.setEditable(false);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 150;    
		_descriptionVerbiage.setLayoutData(gridData);
		
        _factoryViewer.addSelectionChangedListener(new ISelectionChangedListener() 
        {
            public void selectionChanged(SelectionChangedEvent event) 
            {
            	ISelection selection = event.getSelection();
            	if (selection instanceof StructuredSelection)
            	{
            		StructuredSelection ss = (StructuredSelection)selection;
            		
            		ServiceElement firstElement = (ServiceElement)ss.getFirstElement();
            		if (firstElement != null)
            		{            			
            			_factoryViewer.setChecked(firstElement, true);            		
            			unCheckOthers(firstElement);
            			if (!firstElement.isSelected())
            				firstElement.setSelected(true);
            			_serviceViewer.setInput(firstElement);
            			_descriptionVerbiage.setText(getCurrentVerbiage());
            		}
            	}
            }
         });
		
        _serviceViewer.addSelectionChangedListener(new ISelectionChangedListener() 
                {
                    public void selectionChanged(SelectionChangedEvent event) 
                    {
                    	ISelection selection = event.getSelection();
                    	_propertiesViewer.selectionChanged(selection);
                		_descriptionVerbiage.setText(getCurrentVerbiage());
                    }
                 });
        
		return _factoryViewer.getControl();
	}
	

	
	private void createFactoryViewer(Composite parent)
	{
		// Create the table viewer.
		_factoryViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.READ_ONLY);
		_factoryViewer.addCheckStateListener(this);
		
		// Create the table control.
		Table table = _factoryViewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		GridData data = GridUtil.createFill();
		data.heightHint = 20;
		data.widthHint = 30;
		table.setLayoutData(data);
		table.setToolTipText(_configurationTooltip);


	
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
	
	
		_factoryViewer.setContentProvider(new ServiceTableContentProvider());
		_factoryViewer.setLabelProvider(new ServiceTableLabelProvider());
	}
	
	private void createServiceViewer(Composite parent)
	{
		// Create the table viewer.
		_serviceViewer = new TreeViewer(parent, SWT.BORDER);

		
		// Create the table control.
		Tree tableTree = _serviceViewer.getTree();
		GridData data = GridUtil.createFill();
		data.heightHint = 20;
		data.widthHint = 30;
		tableTree.setLayoutData(data);
		tableTree.setLayout(new GridLayout());
		tableTree.setToolTipText(_serviceTooltip);


		_serviceViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		

				
		_serviceViewer.setContentProvider(new ServiceTableContentProvider());
		_serviceViewer.setLabelProvider(new ServiceTableLabelProvider());

	}
	
	private void createPropertiesViewer(Composite parent)
	{
		
		_propertiesViewer = new SystemPropertySheetForm(getShell(),parent, SWT.BORDER, getMessageLine());
		_propertiesViewer.setToolTipText(_propertiesTooltip);


	}
	
	public void init(ServiceElement root)
	{
		_factoryViewer.setInput(root);
		TableItem[] items = _factoryViewer.getTable().getItems();
		for (int i = 0; i < items.length; i++)
		{
			TableItem item = items[i];
			ServiceElement element = (ServiceElement)item.getData();
			if (element.isSelected())
			{
				item.setChecked(true);
				_factoryViewer.setSelection(new StructuredSelection(element));
			}
		}
	}
	

	public void checkStateChanged(CheckStateChangedEvent event)
	{
		ServiceElement element = (ServiceElement)event.getElement();
		element.setSelected(event.getChecked());
		_factoryViewer.setSelection(new StructuredSelection(element));
		
		unCheckOthers(element);
		_descriptionVerbiage.setText(getCurrentVerbiage());
	}
	
	protected void unCheckOthers(ServiceElement checkedElement)
	{
		// uncheck the others now
		Object[] checked = _factoryViewer.getCheckedElements();
		for (int i = 0; i < checked.length; i++)
		{
			ServiceElement oldChecked = (ServiceElement)checked[i];
			if (oldChecked != checkedElement)
			{
				oldChecked.setSelected(false);
				_factoryViewer.setChecked(oldChecked, false);
			}
		}		
	}
	
	public ServiceElement getSelectedService()
	{
		return (ServiceElement)_factoryViewer.getCheckedElements()[0];
	}
	
	public boolean verify()
	{
		return _factoryViewer.getCheckedElements().length > 0;
	}
	
	protected String getCurrentVerbiage()
	{
		String description = null;
		if (_serviceViewer != null)
		{
			IStructuredSelection serviceSelection = (IStructuredSelection)_serviceViewer.getSelection();
			if (serviceSelection != null && !serviceSelection.isEmpty())
			{
				description = ((ServiceElement)serviceSelection.getFirstElement()).getDescription();
			}
			else if (_factoryViewer != null)
			{
				IStructuredSelection factorySelection = (IStructuredSelection) _factoryViewer.getSelection();
				if (factorySelection != null && !factorySelection.isEmpty())
				{
					description = ((ServiceElement)factorySelection.getFirstElement()).getDescription(); 
				}
			}
		}
		return description != null ? description : ""; //$NON-NLS-1$
	}
	
}
