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


package org.eclipse.rse.dstore.security.widgets;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import org.eclipse.dstore.core.util.ssl.DStoreKeyStore;
import org.eclipse.rse.dstore.security.UniversalSecurityPlugin;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.dstore.security.util.GridUtil;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class CertificateForm extends SystemBaseForm
{
	private Text  _pathField;
	private Text  _aliasField;
    private String _aliasStr;
    private String _pathStr;
    private ArrayList listenerList;
	
	private Button _browseButton;
	public Shell _shell;
	
	public CertificateForm(Shell shell, ISystemMessageLine msgLine)
	{
		super(shell, msgLine);
        listenerList = new ArrayList();			
		_shell = shell;
	}
	
	public Control getInitialFocusControl()
	{
		return _pathField;
	}
	
	public Control createContents(Composite c){ 

            GridData data;
			Composite nameGroup = new Composite(c, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			data = GridUtil.createFill();
			data.widthHint = 400;
			nameGroup.setLayoutData(data);
			nameGroup.setLayout(layout);
			
			Label lblPath = new Label(nameGroup, SWT.NONE);
			lblPath.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_FILE));
			_pathField = new Text(nameGroup, SWT.BORDER);				
			_pathField.setLayoutData(GridUtil.createHorizontalFill());
			_pathField.setText("");
			
			_browseButton = new Button(nameGroup, SWT.PUSH);
			_browseButton.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_BROWSE));
			
			_browseButton.addListener(SWT.Selection, this);

			Label lblName = new Label(nameGroup, SWT.NONE);
			lblName.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_ALIAS));
			_aliasField = new Text(nameGroup, SWT.BORDER);
			_aliasField.setText("");

			_aliasField.setLayoutData(GridUtil.createHorizontalFill());
						
			_aliasField.addListener(SWT.Modify, this);
			_pathField.addListener(SWT.Modify, this);
			
			return _pathField;

	}

	/**
	 *  Handle all events and enablements for widgets in this dialog
	 *
	 * @param event Event
	 */
	public void handleEvent(Event event) {

		if(event.widget == _browseButton){
			showFileDialog();
			NotifyListeners(event);
		}
		if(event.widget==_aliasField || event.widget==_pathField){
			//setButtonState();
			_pathStr = _pathField.getText();
			_aliasStr = _aliasField.getText();
			NotifyListeners(event);			
		}

	}
	
	public void NotifyListeners(Event event){
		for(int i=0;i<listenerList.size();i++){
			((Listener)listenerList.get(i)).handleEvent(event);
		}	
	}
	
	public boolean validateDialog(){
		return (_aliasField.getText().trim().length()>0 && _pathField.getText().trim().length()>0);
	}
	
	private void showFileDialog(){
		String currentSource = _pathField.getText();

		FileDialog dlg = new FileDialog(_shell, SWT.OPEN);
		
		dlg.setFileName(currentSource);
		dlg.setFilterExtensions(new String[]{"*.cer", "*.*"});
		
		String source = dlg.open();

		if(source!=null)
		{
			_pathField.setText(source);
			File f = new File(source);
			String alias = f.getName();
			int dotIndex = alias.indexOf('.');
			if (dotIndex > 0)
			{
				alias = alias.substring(0, dotIndex);
			}
			_aliasField.setText(alias);
		}
			
	}

	public Certificate loadCertificate(KeyStore ks) throws IOException, CertificateException, KeyStoreException {

			
		Certificate fCertificate = DStoreKeyStore.loadCertificate(getPath());
		DStoreKeyStore.addCertificateToKeyStore(ks, fCertificate, getAliasName());
		return fCertificate;

	}		

	public void registerListener(Listener listener){
		listenerList.add(listener);
		
	}
    public String getAliasName()
    {
    	return _aliasStr;
    }
    
    public String getPath()
    {
    	return _pathStr;
    }
 
}