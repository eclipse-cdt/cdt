/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.view;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.ui.ViewerPane;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tm.discovery.model.Service;
import org.eclipse.tm.discovery.protocol.IProtocol;
import org.eclipse.tm.discovery.protocol.ProtocolFactory;
import org.eclipse.tm.discovery.transport.ITransport;
import org.eclipse.tm.discovery.transport.TransportFactory;
import org.eclipse.tm.internal.discovery.engine.ServiceDiscoveryEngine;
import org.eclipse.tm.internal.discovery.model.provider.ModelItemProviderAdapterFactory;
import org.eclipse.tm.internal.discovery.wizard.ServiceDiscoveryWizardMainPage;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.part.ViewPart;

/**
 * View to display the Service Discovery model in a tree form.
 * 
 * @see TransportFactory
 * @see ServiceDiscoveryEngine
 * 
 */
public class ServiceDiscoveryView extends ViewPart {
	
	//Tree widgets
	private TreeViewer treeViewer;
	private ViewerPane viewerPaneTree;
	
	// Table widgets
	private TableViewer tableViewer;
	private ViewerPane viewerPaneTable;
		
	private String query = null; 
	
	private Table table;
	
	private Action discoveryAction;
	private Action refreshAction;
	private Action clearAction;

	private ITransport transport = null;
	private IProtocol protocol = null;
	
	private ServiceDiscoveryWizardMainPage serviceDiscoveryWizardMainPage;
	private Resource resource;

	private ServiceDiscoveryEngine serviceDiscoveryEngine = ServiceDiscoveryEngine.getInstance();
	
public void createPartControl(Composite parent) {
	
		resource = serviceDiscoveryEngine.getResource();
		
		List factories = new ArrayList();
		factories.add(new ResourceItemProviderAdapterFactory());
		factories.add(new ModelItemProviderAdapterFactory());
		factories.add(new ReflectiveItemProviderAdapterFactory());
		
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
				factories);
		
		Composite comp = new Composite(parent, SWT.NULL);
		
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        comp.setLayout(gridLayout);
        
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		
		SashForm sashForm = new SashForm(comp,SWT.NULL );
		sashForm.setOrientation(SWT.HORIZONTAL);
		
		sashForm.setLayoutData(data);
		
		// TREE
		
		viewerPaneTree = new ViewerPane(getSite().getPage(), ServiceDiscoveryView.this) {
			public Viewer createViewer(Composite composite) {
				Tree tree = new Tree(composite, SWT.NULL);
				ContainerCheckedTreeViewer treeViewer = new ContainerCheckedTreeViewer(tree);
		
				return treeViewer;
			}
		
			public void requestActivation() {
				super.requestActivation();
			}
		};
		
		Composite sashComposite = new Composite(sashForm,SWT.BORDER);
		sashComposite.setLayout(new FillLayout());
		
		viewerPaneTree.createControl(sashComposite);
		
		treeViewer = (TreeViewer) viewerPaneTree.getViewer();
		
		treeViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
		
		treeViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		
		viewerPaneTree.setTitle(Messages.ServiceDiscoveryView_ServicesTreeTitle, null);
		
		treeViewer.setInput(resource);
		
		new AdapterFactoryTreeEditor(treeViewer.getTree(), adapterFactory);
		
		((ContainerCheckedTreeViewer) viewerPaneTree.getViewer()).addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if(!event.getSelection().isEmpty())
				{
					EObject obj = (EObject)((IStructuredSelection) event.getSelection()).getFirstElement();
					
					if(obj instanceof Service)
						tableViewer.setInput(obj);
				}
			}
		});
		
		
		// TABLE
		
		viewerPaneTable =
			new ViewerPane(getSite().getPage(), ServiceDiscoveryView.this) {
				public Viewer createViewer(Composite composite) {
					return new TableViewer(composite);
				}
			};
		
		Composite c3 = new Composite(sashForm,SWT.BORDER);
		c3.setLayout(new FillLayout());
			
		viewerPaneTable.createControl(c3);
		tableViewer = (TableViewer)viewerPaneTable.getViewer();

		viewerPaneTable.setTitle(Messages.ServiceDiscoveryView_PropertiesTableTitle, null);
		
		
		table = tableViewer.getTable();
		
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn objectColumn = new TableColumn(table, SWT.NONE);
		layout.addColumnData(new ColumnWeightData(3, 100, true));
		objectColumn.setText(Messages.ServiceDiscoveryView_KeyColumnLabel);
		objectColumn.setResizable(true);

		TableColumn selfColumn = new TableColumn(table, SWT.NONE);
		layout.addColumnData(new ColumnWeightData(2, 100, true));
		selfColumn.setText(Messages.ServiceDiscoveryView_ValueColumnLabel);
		selfColumn.setResizable(true);
		
		tableViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
		
		tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		
		makeActions();

		getViewSite().getActionBars().getToolBarManager().add(discoveryAction);
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getToolBarManager().add(refreshAction);
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getToolBarManager().add(clearAction);
	
	}

	private void update(String query, String address, String transportName, String protocolName, int timeOut)
	{
		this.query = query;
		
		//instantiate protocol and transport from factories (extensions)
		
		try {
			protocol = ProtocolFactory.getProtocol(protocolName);
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), Messages.ServiceDiscoveryView_ProtocolErrorDialogTitle, Messages.ServiceDiscoveryView_ProtocolErrorDialogMessage+protocolName);
		}
		
		try {
			transport = TransportFactory.getTransport(transportName, address, timeOut);
		} catch (UnknownHostException e) {
			MessageDialog.openError(new Shell(), Messages.ServiceDiscoveryView_TransportNoAddressFoundDialogTitle, Messages.ServiceDiscoveryView_TransportNoAddressFoundDialogTransport+address);
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), Messages.ServiceDiscoveryView_TransportErrorDialogTitle, Messages.ServiceDiscoveryView_TransportErrorDialogMessage+transportName);
		}
		
		
		if(protocol != null && transport != null)
		{
			serviceDiscoveryEngine.doServiceDiscovery(query, protocol,transport);
		}
	}



	private void makeActions() {
		discoveryAction = new Action() {
			public void run() {
				
				Wizard wizard = new Wizard(){
					
					public boolean performFinish() {
						
						update(serviceDiscoveryWizardMainPage.getQuery(),
							   serviceDiscoveryWizardMainPage.getAddress(),	
							   serviceDiscoveryWizardMainPage.getTransport(), 
							   serviceDiscoveryWizardMainPage.getProtocol(), 
							   serviceDiscoveryWizardMainPage.getTimeOut());
					
						return true;
					}};
				
				serviceDiscoveryWizardMainPage = new ServiceDiscoveryWizardMainPage();
					
				wizard.addPage(serviceDiscoveryWizardMainPage);
				
				Shell shell = new Shell();
				WizardDialog dialog = new WizardDialog(shell, wizard);
				
				Rectangle absoluteRect = shell.getMonitor().getClientArea();
				Rectangle shellRect = shell.getBounds();
				shell.setLocation(((absoluteRect.width - shellRect.width) / 2), ((absoluteRect.height - shellRect.height) / 2));
				
				dialog.open();
			}
		};
		
		discoveryAction.setText(Messages.ServiceDiscoveryView_DiscoveryActionText);
		discoveryAction.setToolTipText(Messages.ServiceDiscoveryView_DiscoveryActionToolTipText);
		discoveryAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		refreshAction = new Action() {
			public void run() {
				if(query != null && transport != null && protocol != null)
				{
					serviceDiscoveryEngine.doServiceDiscovery(query, protocol,transport);
				}
			}
		};
		refreshAction.setText(Messages.ServiceDiscoveryView_RefreshActionText);
		refreshAction.setToolTipText(Messages.ServiceDiscoveryView_RefreshActionToolTipText);
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		
		
		clearAction = new Action() {
			public void run() {
				resource.getContents().clear();
				tableViewer.setInput(null);
			}
		};
		clearAction.setText(Messages.ServiceDiscoveryView_ClearActionText);
		clearAction.setToolTipText(Messages.ServiceDiscoveryView_ClearActionToolTipText);
		clearAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

	}

	public void setFocus() {
	}
	
}
