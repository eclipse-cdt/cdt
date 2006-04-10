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

package org.eclipse.rse.dstore.security.wizards;

import java.security.cert.X509Certificate;
import java.util.List;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.dstore.security.UniversalSecurityPlugin;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.dstore.security.preference.CertTableContentProvider;
import org.eclipse.rse.dstore.security.preference.X509CertificateElement;
import org.eclipse.rse.dstore.security.preference.X509CertificatePropertiesDialog;
import org.eclipse.rse.dstore.security.util.GridUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class SystemImportCertWizardMainPage 
 	   extends AbstractSystemWizardPage
 	   implements  ISystemMessages, Listener        
{  


	protected SystemMessage errorMessage;
	protected ISystemValidator nameValidator;
	protected ISystemMessageLine msgLine;
	
    private List _certificates;
	private Button _propertiesButton;

	private TableViewer _viewer;
		  
	/**
	 * Constructor.
	 */
	public SystemImportCertWizardMainPage(Wizard wizard, List certs)
	{
		super(wizard, "NewCertificate", 
  		      UniversalSecurityProperties.RESID_SECURITY_TRUST_WIZ_CERTIFICATE_TITLE, 
		      UniversalSecurityProperties.RESID_SECURITY_TRUST_WIZ_CERTIFICATE_DESC);
		_certificates = certs;
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{

		Composite verbage = new Composite(parent, SWT.NULL);
		GridLayout vlayout = new GridLayout();
		GridData vdata = new GridData(GridData.FILL_BOTH);
		vlayout.numColumns = 1;
		verbage.setLayout(vlayout);
		verbage.setLayoutData(vdata);

		SystemWidgetHelpers.createLabel(verbage, UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_INFORMATION);
		createTableViewer(verbage);
		
		Composite b = new Composite(parent, SWT.NULL);
		GridLayout blayout = new GridLayout();
		GridData bdata = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.END);
		blayout.numColumns = 3;
		b.setLayout(blayout);
		b.setLayoutData(bdata);
		_propertiesButton = SystemWidgetHelpers.createPushButton(b, UniversalSecurityProperties.RESID_SECURITY_PROPERTIES_LBL, this);	
		
		return _propertiesButton;
	}
	

	private void createTableViewer(Composite parent)
	{
		// Create the table viewer.
		_viewer = new TableViewer(parent, SWT.BORDER | SWT.READ_ONLY);

		// Create the table control.
		Table table = _viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = GridUtil.createFill();
		data.heightHint = 30;
		data.widthHint = 80;
		table.setLayoutData(data);

		TableLayout tableLayout = new TableLayout();

		TableColumn toColumn = new TableColumn(table, SWT.LEFT);
		toColumn.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_PREF_ISSUED_TO));
		tableLayout.addColumnData(new ColumnPixelData(90));
		
		TableColumn frmColumn = new TableColumn(table, SWT.LEFT);
		frmColumn.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_PREF_ISSUED_FROM));
		tableLayout.addColumnData(new ColumnPixelData(90));
		
		TableColumn expColumn = new TableColumn(table, SWT.RIGHT);
		expColumn.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_PREF_EXPIRES));
		tableLayout.addColumnData(new ColumnPixelData(180));
		table.setLayout(tableLayout);

		// Adjust the table viewer.
		String[] properties = new String[] {"STRING", "STRING", "NUMBER"};
		_viewer.setColumnProperties(properties);
		_viewer.setContentProvider(new CertTableContentProvider());
		_viewer.setLabelProvider(new NewCertTableLabelProvider());
		
		for (int i = 0; i < _certificates.size(); i++)
		{
			_viewer.add(getElement(_certificates.get(i)));
		}
	}

	
	
	public void handleEvent(Event e)
	{
		if (e.widget == _propertiesButton)
		{
			IStructuredSelection sel = (IStructuredSelection)_viewer.getSelection();
			sel.getFirstElement();
			
			X509CertificatePropertiesDialog dlg = new X509CertificatePropertiesDialog(getShell(), (X509CertificateElement)sel.getFirstElement());
			dlg.open();
		}
	}
	
	public X509CertificateElement getElement(Object cert)
	{
		if (cert instanceof X509Certificate)
		{
			return new X509CertificateElement(null, 
					UniversalSecurityProperties.RESID_SECURITY_TRUSTED_CERTIFICATE, 
					(X509Certificate)cert);
		}
		return null;
	}
	
	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return _propertiesButton;
	}
	
	/**
	 * Init values using input data
	 */
	protected void initializeInput()
	{
	}
	
  
	
	/**
	 * Completes processing of the wizard. If this 
	 * method returns true, the wizard will close; 
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class. 
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish() 
	{
		
	    return true;
	}
    
  

	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete()
	{
		return true;
	}
	

}