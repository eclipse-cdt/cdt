/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.internal.discovery.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.discovery.protocol.ProtocolFactory;
import org.eclipse.tm.discovery.transport.TransportFactory;
import org.eclipse.ui.PlatformUI;

/**
 * Main wizard page for the service discovery process.</br>
 * It provides a wizard page with text boxes and combo boxes to gather the following data:
 * <ul>
 * <li>Protocol
 * <li>Transport
 * <li>Query
 * <li>Timeout
 * </li>
 * 
 * @see WizardPage
 */

public class ServiceDiscoveryWizardMainPage extends WizardPage {
	
	// settings
	private int timeOut = 500; //ms
	
	// widgets
	private Combo queryCombo, transportCombo, protocolCombo;
	private Text addressText, timeOutText;
	private Button multicastButton;
	
	private String tempAddress;
	
	/**
	 * Wizard main page constructor
	 */
	public ServiceDiscoveryWizardMainPage() {
		super("wizardPage1"); //$NON-NLS-1$
		setTitle(Messages.getString("ServiceDiscoveryWizardMainPage.WizardPageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("ServiceDiscoveryWizardMainPage.WizardPageDescription")); //$NON-NLS-1$
		setErrorMessage(Messages.getString("ServiceDiscoveryWizardMainPage.ProvideAddressError")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		
		Composite comp = new Composite(parent,SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		comp.setLayout(layout);
		
		//GridData
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = SWT.BEGINNING;
		data.grabExcessVerticalSpace = false;
		
		comp.setLayoutData(data);
		
		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.AddressLabel")); //$NON-NLS-1$
		
		addressText = new Text(comp, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
		addressText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				if(((Text)e.getSource()).getText().equals("")) //$NON-NLS-1$
				{
					setErrorMessage(Messages.getString("ServiceDiscoveryWizardMainPage.ProvideAddressError")); //$NON-NLS-1$
					setPageComplete(false);
				}	
				else
				{
					setErrorMessage(null);
					setPageComplete(true);
				}
				
			}
		});
		
		addressText.setLayoutData(data);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(addressText,"org.eclipse.tm.discovery.wizard.address"); //$NON-NLS-1$
		
		Composite comp2 = new Composite(comp,SWT.NULL);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 2;
		comp2.setLayout(layout2);
		
		multicastButton = new Button(comp2,SWT.CHECK);
		
		multicastButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				
				 Object src = e.getSource();
				 if(((Button)src).getSelection())
				 {
					 String multicastAddress = null;
						
						try {
							multicastAddress = ProtocolFactory.getMulticastAddress(protocolCombo.getText(), transportCombo.getText());
						} catch (CoreException e1) {}
							
						if(multicastAddress!=null)
						{
							tempAddress = addressText.getText();
							addressText.setText(multicastAddress);
						}
						else
						{
							((Button)src).setSelection(false);
						}
				 }
				 else
				 {
					 if(tempAddress!=null)
						 addressText.setText(tempAddress);
				 }
			}
		});
		
		new Label(comp2,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.MuticastAddressLabel0")); //$NON-NLS-1$
		

		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.TransportLabel")); //$NON-NLS-1$
		
		transportCombo = new Combo(comp, SWT.READ_ONLY);
		transportCombo.setItems(TransportFactory.getTransportList());
		transportCombo.select(0);
		
		transportCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent event) {
				
				if(multicastButton.getSelection())
				{
					String multicastAddress = null;
					
					try {
						multicastAddress = ProtocolFactory.getMulticastAddress(protocolCombo.getText(), transportCombo.getText());
					} catch (CoreException e1) {}
						
					if(multicastAddress!=null)
					{
						tempAddress = addressText.getText();
						addressText.setText(multicastAddress);
					}
				}
			}
		});
		
		transportCombo.setLayoutData(data);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(transportCombo,"org.eclipse.tm.discovery.wizard.transport"); //$NON-NLS-1$
		
		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.ProtocolLabel")); //$NON-NLS-1$
		
		protocolCombo = new Combo(comp, SWT.READ_ONLY);
		protocolCombo.setItems(ProtocolFactory.getProtocolList());
		protocolCombo.select(0);
		
		protocolCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent event) {
				String selectedProtocol = ((Combo)event.getSource()).getText();
				
				String[] queries = new String[]{};
				try {
					queries = ProtocolFactory.getQueryList(selectedProtocol);
				} catch (CoreException e) {}
				queryCombo.removeAll();
				queryCombo.setItems(queries);
				queryCombo.select(0);
				
				if(multicastButton.getSelection())
				{
					String multicastAddress = null;
					
					try {
						multicastAddress = ProtocolFactory.getMulticastAddress(protocolCombo.getText(), transportCombo.getText());
					} catch (CoreException e1) {}
						
					if(multicastAddress!=null)
					{
						tempAddress = addressText.getText();
						addressText.setText(multicastAddress);
					}
				}
			}
		});
		
		protocolCombo.setLayoutData(data);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(protocolCombo,"org.eclipse.tm.discovery.wizard.protocol"); //$NON-NLS-1$
		
		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.DiscoveryQueryLabel")); //$NON-NLS-1$
		
		queryCombo = new Combo(comp, SWT.NONE);
		String[] queries = new String[]{};
		try {
			queries = ProtocolFactory.getQueryList(protocolCombo.getText());
		} catch (CoreException e) {}
		for (int i = 0; i < queries.length; i++) {
			queryCombo.add(queries[i]);
		}
		queryCombo.select(0);
		
		queryCombo.setLayoutData(data);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(queryCombo,"org.eclipse.tm.discovery.wizard.query"); //$NON-NLS-1$
		
		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.TimeOutLabel")); //$NON-NLS-1$
		
		timeOutText = new Text(comp, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
		timeOutText.setText(Messages.getString("ServiceDiscoveryWizardMainPage.TimeOutValue")); //$NON-NLS-1$
		timeOutText.redraw();
		
		timeOutText.setLayoutData(data);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(timeOutText,"org.eclipse.tm.discovery.wizard.timeout"); //$NON-NLS-1$
		
		setPageComplete(false);
		
		setControl(comp);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),	"org.eclipse.tm.discovery.wizard.general"); //$NON-NLS-1$
		
		
	}
	
	/**
	 * Gets the service discovery command
	 * @return command introduced in the settings window
	 */
	public String getQuery() {
		return queryCombo.getText();
	}

	/**
	 * Gets the service discovery address
	 * @return address introduced in the settings window
	 */
	public String getAddress() {
		return addressText.getText();
	}
	
	/**
	 * Gets the service discovery transport
	 * @return address introduced in the settings window
	 */
	public String getTransport() {
		return transportCombo.getText();
	}
	
	/**
	 * Gets the service discovery protocol
	 * @return address introduced in the settings window
	 */
	public String getProtocol() {
		return protocolCombo.getText();
	}

	/**
	 * Gets the service discovery timeout
	 * @return timeout introduced in the settings window
	 */
	public int getTimeOut() {
		
		try{
			timeOut = Integer.parseInt(timeOutText.getText().trim());
		}catch(NumberFormatException e){}
		
		return timeOut;
	}

	 /* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
	        return isPageComplete();
	    }

	
}
