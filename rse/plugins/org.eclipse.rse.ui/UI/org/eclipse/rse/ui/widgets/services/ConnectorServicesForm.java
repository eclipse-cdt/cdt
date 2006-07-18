/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.widgets.services;


import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.SystemPropertySheetForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;



public class ConnectorServicesForm extends SystemBaseForm
{
	private TreeViewer _serviceViewer;
	private SystemPropertySheetForm _propertiesViewer;
	private Text _descriptionVerbage;
	
	private String _serviceTooltip = SystemResources.RESID_SERVICESFORM_CONNECTORSERVICES_TOOLTIP;
	private String _propertiesTooltip = SystemResources.RESID_SERVICESFORM_PROPERTIES_TOOLTIP;

	
	public ConnectorServicesForm(ISystemMessageLine msgLine)
	{
		super(msgLine);
	}
	
	public Control createContents(Composite parent)
	{
		
		SashForm sashComposite = new SashForm(parent, SWT.VERTICAL);
	    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	    sashComposite.setLayoutData(data); 
	    
	    					
		// connector service composite
		Composite serviceViewerComposite = new Composite(sashComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
        layout.marginWidth = 0;
        serviceViewerComposite.setLayout(layout);
        serviceViewerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //serviceViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
		Label servicesLabel = new Label(serviceViewerComposite, SWT.NONE);
	    servicesLabel.setText(SystemResources.RESID_PROPERTIES_SERVICES_LABEL);	    
		createServiceViewer(serviceViewerComposite);

		// properties composite
		Composite servicePropertiesComposite = new Composite(sashComposite, SWT.NONE);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        servicePropertiesComposite.setLayout(layout);
        servicePropertiesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label propertiesLabel = new Label(servicePropertiesComposite, SWT.NONE);
	    propertiesLabel.setText(SystemResources.RESID_PROPERTIES_PROPERTIES_LABEL);	
		createPropertiesViewer(servicePropertiesComposite);
	    
		// description composite
		Composite descriptionComposite = new Composite(sashComposite, SWT.NONE);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        descriptionComposite.setLayout(layout);
        descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
	    descriptionLabel.setText(SystemResources.RESID_PROPERTIES_DESCRIPTION_LABEL);
		_descriptionVerbage = new Text(descriptionComposite, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
        _descriptionVerbage.setText(getCurrentVerbage());
        _descriptionVerbage.setEditable(false);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 150;    
		gridData.verticalAlignment = GridData.BEGINNING;
		_descriptionVerbage.setLayoutData(data);

		sashComposite.setWeights(new int[] { 20, 30, 10 });
		
		
        _serviceViewer.addSelectionChangedListener(new ISelectionChangedListener() 
                {
                    public void selectionChanged(SelectionChangedEvent event) 
                    {
                    	ISelection selection = event.getSelection();
                    	_propertiesViewer.selectionChanged(selection);
                		_descriptionVerbage.setText(getCurrentVerbage());
                    }
                 });
        
		return _serviceViewer.getControl();
	}
	
	
	private void createServiceViewer(Composite parent)
	{
		// Create the table viewer.
		_serviceViewer = new TreeViewer(parent, SWT.BORDER);
		// Create the table control.
		Tree tableTree = _serviceViewer.getTree();

		tableTree.setLayout(new FillLayout());
		tableTree.setToolTipText(_serviceTooltip);
		_serviceViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		_serviceViewer.setContentProvider(new ServiceTableContentProvider());
		_serviceViewer.setLabelProvider(new ServiceTableLabelProvider());

	}
	
	private void createPropertiesViewer(Composite parent)
	{		
		_propertiesViewer = new SystemPropertySheetForm(getShell(),parent, SWT.BORDER, getMessageLine(), 1, 1);
		_propertiesViewer.setToolTipText(_propertiesTooltip);
	}
	
	public void init(ServiceElement root)
	{
		_serviceViewer.setInput(root);
	}

	public boolean performOk()
	{
		return true;
	}
	
	protected String getCurrentVerbage()
	{
		if (_serviceViewer == null)
			return ""; //$NON-NLS-1$
		else
		{
			IStructuredSelection serviceSelection = (IStructuredSelection)_serviceViewer.getSelection();
			if (serviceSelection == null || serviceSelection.isEmpty())
			{
				return ""; //$NON-NLS-1$
			}
			else
			{
				String description = ((ServiceElement)serviceSelection.getFirstElement()).getDescription(); 
				return description!=null ? description : ""; //$NON-NLS-1$  
			}
		}
	}
}