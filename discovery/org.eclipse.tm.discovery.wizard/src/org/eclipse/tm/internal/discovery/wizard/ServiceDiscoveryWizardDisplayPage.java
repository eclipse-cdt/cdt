/********************************************************************************
 * Copyright (c) 2006, 2008 Symbian Software Ltd. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Javier Montalvo Orus (Symbian) - initial API and implementation
 * Javier Montalvo Orus (Symbian) - [plan] Improve Discovery and Autodetect in RSE
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186523] Move subsystemConfigurations from UI to core
 * Javier Montalvo Orus (Symbian) - [186652] Next button should not be enabled on final page of discovery wizard
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.wizard;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.ui.ViewerPane;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tm.discovery.model.Device;
import org.eclipse.tm.discovery.model.Pair;
import org.eclipse.tm.discovery.model.Service;
import org.eclipse.tm.discovery.model.ServiceType;
import org.eclipse.tm.discovery.protocol.IProtocol;
import org.eclipse.tm.discovery.protocol.ProtocolFactory;
import org.eclipse.tm.discovery.transport.ITransport;
import org.eclipse.tm.discovery.transport.TransportFactory;
import org.eclipse.tm.internal.discovery.engine.ServiceDiscoveryEngine;
import org.eclipse.tm.internal.discovery.model.provider.ModelItemProviderAdapterFactory;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

/**
 * Wizard page providing the list of discovered services
 */

public class ServiceDiscoveryWizardDisplayPage extends WizardPage {

	//tree widgets
	private TreeViewer treeViewer;
	private ViewerPane viewerPaneTree;
	private ViewerFilter filter;

	//table widgets
	private TableViewer tableViewer;
	private ViewerPane viewerPaneTable;
	private Table table;
	private TableEditor editor;

	private ComposedAdapterFactory adapterFactory;

	//button widgets
	private Button showAllButton;

	//static service discovery engine
	private final ServiceDiscoveryEngine serviceDiscoveryEngine = ServiceDiscoveryEngine.getInstance();

	//service discovery settings
	private String query = null;
	private String address = null;
	private String transportName = null;
	private String protocolName = null;
	private int timeOut = 500;

	//format of serviceType attribute list of names and transports
	//of extension point org.eclipse.core.subsystemConfigurations
	private final Pattern serviceTypeFormat = Pattern.compile("_(.+)\\._(.+)"); //$NON-NLS-1$

	private Service lastSelectedService = null;

	private Hashtable supportedServicesType = new Hashtable();

	/**
	 * Constructor for the wizard page performing and displayin the results of
	 * the service discovery
	 * 
	 * @param query Query for the service discovery action
	 * @param address Address of the target device
	 * @param transportName Name of the transport implementation to be used
	 * @param protocolName Name of the protocol implementation to be used
	 * @param timeOut Timeout to be used in the transport
	 */

	public ServiceDiscoveryWizardDisplayPage(String query, String address, String transportName, String protocolName, int timeOut) {
		super("wizardPage2"); //$NON-NLS-1$
		setTitle(Messages.getString("ServiceDiscoveryWizardDisplayPage.WizardPageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("ServiceDiscoveryWizardDisplayPage.WizardPageDescription")); //$NON-NLS-1$

		//load all service id's from the extension point registry
		//this id will be used to filter the supported sytem types

		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.rse.core","subsystemConfigurations"); //$NON-NLS-1$ //$NON-NLS-2$
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String type = ce[i].getAttribute("serviceType"); //$NON-NLS-1$

			if(type!=null)
			{
				String[] variants = type.split(";"); //$NON-NLS-1$

				for (int j = 0; j < variants.length; j++) {
					Matcher match = serviceTypeFormat.matcher(variants[j]);
					if(match.matches())
					{
						String name = match.group(1);
						String transport = match.group(2);
						if(supportedServicesType.containsKey(name))
						{
							//insert new transport
							((Vector)supportedServicesType.get(name)).add(transport);
						}
						else
						{
							//create vector with new transport
							Vector transports = new Vector();
							transports.add(transport);
							supportedServicesType.put(name,transports);
						}
					}
				}
			}
		}

		this.query = query;
		this.address = address;
		this.transportName = transportName;
		this.protocolName = protocolName;
		this.timeOut = timeOut;

	}

	/**
	 * Refresh the contents of the service discovery model
	 * 
	 * @param query Query for the service discovery action
	 * @param address Address of the target device
	 * @param transportName Name of the transport implementation to be used
	 * @param protocolName Name of the protocol implementation to be used
	 * @param timeOut Timeout to be used in the transport
	 */
	public void update(String query, String address, String transportName, String protocolName, int timeOut)
	{
		//update settings
		this.query = query;
		this.address = address;
		this.transportName = transportName;
		this.protocolName = protocolName;
		this.timeOut = timeOut;

		//instantiate protocol and transport from factories (extensions)
		//and perform the service discovery action

		IProtocol protocol = null;
		ITransport transport = null;

		try {
			protocol = ProtocolFactory.getProtocol(protocolName);
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), Messages.getString("ServiceDiscoveryWizardDisplayPage.ProtocolErrorTitle"), Messages.getString("ServiceDiscoveryWizardDisplayPage.ProtocolErrorMessage")+protocolName); //$NON-NLS-1$ //$NON-NLS-2$
		}

		try {
			transport = TransportFactory.getTransport(transportName, address, timeOut);
		} catch (UnknownHostException e) {
			MessageDialog.openError(new Shell(), Messages.getString("ServiceDiscoveryWizardDisplayPage.TransportAddressNotFoundTitle"), Messages.getString("ServiceDiscoveryWizardDisplayPage.TransportAddressNotFoundMessage")+address); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), Messages.getString("ServiceDiscoveryWizardDisplayPage.TransportErrorTitle"), Messages.getString("ServiceDiscoveryWizardDisplayPage.TransportErrorMessage")+transportName); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if(protocol != null && transport != null)
		{
			try{
				serviceDiscoveryEngine.doServiceDiscovery(query, protocol,transport);
			}catch(Exception e){}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		List factories = new ArrayList();
		factories.add(new ResourceItemProviderAdapterFactory());
		factories.add(new ModelItemProviderAdapterFactory());
		factories.add(new ReflectiveItemProviderAdapterFactory());

		adapterFactory = new ComposedAdapterFactory(factories);

		Composite comp = new Composite(parent, SWT.NULL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		comp.setLayout(gridLayout);

		//TOOLBAR

		createToolBar(comp);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;

		SashForm sashForm = new SashForm(comp,SWT.NULL );
		sashForm.setOrientation(SWT.HORIZONTAL);

		sashForm.setLayoutData(data);

		// TREE

		Composite sashComposite = new Composite(sashForm,SWT.BORDER);
		sashComposite.setLayout(new FillLayout());

		createTree(sashComposite);

		// TABLE

		Composite innerComposite = new Composite(sashForm,SWT.BORDER);
		innerComposite.setLayout(new FillLayout());

		createTable(innerComposite);


		// SHOW ALL SERVICES BUTTON

		createShowAllButton(comp);


		update(query, address, transportName, protocolName, timeOut);

		setPageComplete(false);

		setControl(comp);

	}

	/*
	 * ToolBar of the wizard page
	 */
	private void createToolBar(Composite comp)
	{
		ToolBar toolBar = new ToolBar(comp,SWT.HORIZONTAL | SWT.FLAT | SWT.LEFT | SWT.WRAP );

		ToolItem refreshButton = new ToolItem(toolBar, SWT.NULL);
		refreshButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				update(query, address, transportName, protocolName, timeOut);
			}
		});

		refreshButton.setToolTipText(Messages.getString("ServiceDiscoveryWizardDisplayPage.RefreshButtonToolTipText")); //$NON-NLS-1$
		refreshButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO).createImage());

		ToolItem cleanButton = new ToolItem(toolBar, SWT.NULL);
		cleanButton.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				serviceDiscoveryEngine.getResource().getContents().clear();
				tableViewer.setInput(null);
			}
		});

		cleanButton.setToolTipText(Messages.getString("ServiceDiscoveryWizardDisplayPage.ClearButtonToolTipText")); //$NON-NLS-1$
		cleanButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE).createImage());

	}

	/*
	 * Tree of the wizard page
	 */
	private void createTree(Composite comp)
	{

		viewerPaneTree = new ViewerPane(  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()) {
			public Viewer createViewer(Composite composite) {
				Tree tree = new Tree(composite, SWT.CHECK);
				ContainerCheckedTreeViewer treeViewer = new ContainerCheckedTreeViewer(tree);

				return treeViewer;
			}

			public void requestActivation() {
				super.requestActivation();
			}
		};

		viewerPaneTree.createControl(comp);

		treeViewer = (TreeViewer) viewerPaneTree.getViewer();

		treeViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));

		treeViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));

		viewerPaneTree.setTitle(Messages.getString("ServiceDiscoveryWizardDisplayPage.ServicesTreeTitle"), null); //$NON-NLS-1$

		treeViewer.setInput(serviceDiscoveryEngine.getResource());

		new AdapterFactoryTreeEditor(treeViewer.getTree(), adapterFactory);

		((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if(!event.getSelection().isEmpty())
				{
					EObject obj = (EObject)((IStructuredSelection) event.getSelection()).getFirstElement();

					if(obj instanceof Service)
					{
						tableViewer.setInput(obj);
						lastSelectedService = (Service)obj;
					}
				}
			}
		});


		((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {

				ContainerCheckedTreeViewer treeViewer = (ContainerCheckedTreeViewer) event.getSource();

				if(treeViewer.getCheckedElements().length > 0)
					setPageComplete(true);
				else
					setPageComplete(false);

			}
		});

		filter = new ViewerFilter(){

			public boolean select(Viewer viewer, Object parentElement, Object element) {
				boolean supported = true;

				if(element instanceof ServiceType) {

					//check if the service type is in the supported list
					String serviceTypeName = ((ServiceType)element).getName();
					if(!supportedServicesType.containsKey(serviceTypeName))
					{
						supported = false;
					}
				}

				if(element instanceof Service) {

					//if the discovered transport value is not contained in the list of supported transports filter this service
					supported = false;

					String serviceTypeName = ((ServiceType)((Service)element).eContainer()).getName();

					//check if the transport service is supported
					Vector transports = (Vector)supportedServicesType.get(serviceTypeName);
					Iterator it = ((Service)element).getPair().iterator();
					while(it.hasNext())
					{
						Pair pair = (Pair)it.next();
						if(pair.getKey().equalsIgnoreCase("transport")) //$NON-NLS-1$
						{
							String transport = pair.getValue();

							for (int i = 0; i < transports.size(); i++) {
								if(((String)transports.elementAt(i)).equalsIgnoreCase(transport))
								{
									//found a supported transport
									supported = true;
								}
							}
						}

					}

				}
				return supported;

			}};



			((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).addFilter(filter);

	}

	/*
	 * Table of the wizard page
	 */
	private void createTable(Composite comp)
	{
		viewerPaneTable =
			new ViewerPane(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()) {
			public Viewer createViewer(Composite composite) {
				return new TableViewer(composite);
			}
		};

		viewerPaneTable.createControl(comp);
		tableViewer = (TableViewer)viewerPaneTable.getViewer();

		viewerPaneTable.setTitle(Messages.getString("ServiceDiscoveryWizardDisplayPage.PropertiesTableTitle"), null); //$NON-NLS-1$


		table = tableViewer.getTable();

		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn objectColumn = new TableColumn(table, SWT.NONE);
		layout.addColumnData(new ColumnWeightData(3, 100, true));
		objectColumn.setText(Messages.getString("ServiceDiscoveryWizardDisplayPage.KeyColumnLabel")); //$NON-NLS-1$
		objectColumn.setResizable(true);

		TableColumn selfColumn = new TableColumn(table, SWT.NONE);
		layout.addColumnData(new ColumnWeightData(2, 100, true));
		selfColumn.setText(Messages.getString("ServiceDiscoveryWizardDisplayPage.ValueColumnLabel")); //$NON-NLS-1$
		selfColumn.setResizable(true);

		tableViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));

		tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));

		editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		table.addListener (SWT.MouseDown, new Listener () {
			public void handleEvent (Event event) {
				Rectangle clientArea = table.getClientArea ();
				Point pt = new Point (event.x, event.y);
				int index = table.getTopIndex ();
				while (index < table.getItemCount ()) {
					boolean visible = false;
					final TableItem item = table.getItem (index);
					for (int i=table.getColumnCount()-1; i<table.getColumnCount(); i++) {
						Rectangle rect = item.getBounds (i);
						if (rect.contains (pt)) {
							final int column = i;
							final Text text = new Text (table, SWT.NONE);
							Listener textListener = new Listener () {
								public void handleEvent (final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										item.setText (column, text.getText ());
										text.dispose ();

										//update model when focus out
										updatePairs(item.getText(0),item.getText(1));
										break;

									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText (column, text.getText ());

											//update model when pressing return
											updatePairs(item.getText(0),item.getText(1));
											text.dispose ();
											e.doit = false;
											break;
										case SWT.TRAVERSE_ESCAPE:
											text.dispose ();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener (SWT.FocusOut, textListener);
							text.addListener (SWT.Traverse, textListener);
							editor.setEditor (text, item, i);
							text.setText (item.getText (i));
							text.selectAll ();
							text.setFocus ();
							return;
						}
						if (!visible && rect.intersects (clientArea)) {
							visible = true;
						}
					}
					if (!visible) return;
					index++;
				}

			}
		});

	}

	/*
	 * Check box to show all services
	 */
	private void createShowAllButton(Composite comp)
	{
		showAllButton = new Button(comp,SWT.CHECK);
		showAllButton.setText(Messages.getString("ServiceDiscoveryWizardDisplayPage.ShowAllServicesButtonText")); //$NON-NLS-1$

		showAllButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {

				Object src = e.getSource();
				if(((Button)src).getSelection())
				{
					((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).removeFilter(filter);
				}
				else
				{
					((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).addFilter(filter);
				}
			}
		});
	}

	/*
	 * Update the service pairs information in the model
	 */
	private void updatePairs(String key, String value)
	{
		Iterator pairsIterator = lastSelectedService.getPair().iterator();
		while(pairsIterator.hasNext())
		{
			Pair pair = (Pair)pairsIterator.next();
			if(pair.getKey().equals(key))
			{
				pair.setValue(value);
			}
		}
	}

	/**
	 * Get the
	 * <code>Service<code> objects selected in the tree view from the specified host
	 * 
	 * @param address Address of the host which services are queried
	 * @return Vector containing the <code>Service<code> selected
	 * 
	 * @see Service
	 */
	public Vector getSelectedServices(String address)
	{
		Vector services = new Vector();

		Object[] checkedElements = ((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).getCheckedElements();
		for(int i=0; i<checkedElements.length; i++)
		{
			if(checkedElements[i] instanceof Service)
			{
				Service service = (Service)checkedElements[i];
				if(((Device)service.eContainer().eContainer()).getAddress().equalsIgnoreCase(address))
				{
					services.add(service);
				}
			}
		}

		return services;
	}

	/**
	 * Get the addresses of the discovered hosts that have at least one service selected
	 * 
	 * @return
	 * String[] containing the addresses of the selected hosts
	 */
	public String[] getAddresses()
	{
		Vector addressVector = new Vector();

		Object[] checkedElements = ((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).getCheckedElements();
		for(int i=0; i<checkedElements.length; i++)
		{
			if(checkedElements[i] instanceof Device)
			{
				addressVector.add(((Device)checkedElements[i]).getAddress());
			}
		}

		String[] addresses = new String[addressVector.size()];
		addressVector.copyInto(addresses);

		return addresses;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return false;
	}

}
