/********************************************************************************
 * Copyright (c) 2006 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orús (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.discovery.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.discovery.protocol.ProtocolFactory;
import org.eclipse.tm.discovery.transport.TransportFactory;

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
		
		FillLayout layout = new FillLayout();
		layout.type = SWT.VERTICAL;
		
		Composite comp = new Composite(parent,SWT.NULL);
		comp.setLayout(layout);
		
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
		
		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.TransportLabel")); //$NON-NLS-1$
		
		transportCombo = new Combo(comp, SWT.READ_ONLY);
		transportCombo.setItems(TransportFactory.getTransportList());
		transportCombo.select(0);
		
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
			}
		});
		
		
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
		
		
		new Label(comp,SWT.NULL).setText(Messages.getString("ServiceDiscoveryWizardMainPage.TimeOutLabel")); //$NON-NLS-1$
		
		timeOutText = new Text(comp, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
		timeOutText.setText(Messages.getString("ServiceDiscoveryWizardMainPage.TimeOutValue")); //$NON-NLS-1$
		timeOutText.redraw();
		
		setPageComplete(false);
		
		setControl(comp);
		
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
