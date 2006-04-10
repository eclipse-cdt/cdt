/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.shells.ui.view;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



public class EncodingForm extends SystemBaseForm 
{
	// State for encoding group
	private String _encoding;
	private String _defaultEncoding;
	
	private Button _defaultEncodingButton;
	private Button _otherEncodingButton;
	private Combo _encodingCombo;

	
	public EncodingForm(Shell shell, ISystemMessageLine line)
	{
		super(shell, line);
		_defaultEncoding = ShellResources.RESID_SHELL_PROPERTYPAGE_DEFAULT_ENCODING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.SystemBaseForm#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite group) 
	{

		SelectionAdapter buttonListener = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) {
				updateEncodingState(_defaultEncodingButton.getSelection());
				updateValidState();
			}
		};

		_defaultEncodingButton = new Button(group, SWT.RADIO);
		_defaultEncodingButton.setText(ShellResources.RESID_SHELL_PROPERTYPAGE_HOST_ENCODING); 
		GridData data = new GridData();
		Font font = group.getFont();
		
		data.horizontalSpan = 2;
		_defaultEncodingButton.setLayoutData(data);
		_defaultEncodingButton.addSelectionListener(buttonListener);
		_defaultEncodingButton.setFont(font);

		_otherEncodingButton = new Button(group, SWT.RADIO);
		_otherEncodingButton.setText(ShellResources.RESID_SHELL_PROPERTYPAGE_OTHER_ENCODING); 
		_otherEncodingButton.addSelectionListener(buttonListener);
		_otherEncodingButton.setFont(font);

		_encodingCombo = new Combo(group, SWT.NONE);
		data = new GridData();
		_encodingCombo.setFont(font);
		_encodingCombo.setLayoutData(data);
		_encodingCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateValidState();
			}
		});

		return _encodingCombo;
	}
	
	public void initialize(List defaults, String encoding)
	{
		_encoding = encoding;
		
		boolean isDefault = _encoding == null || _encoding.length() == 0 || encoding.equals(_defaultEncoding);

		if (!isDefault && !defaults.contains(_encoding)) 
		{
			defaults.add(_encoding);
		}
		Collections.sort(defaults);
		for (int i = 0; i < defaults.size(); ++i) {
			_encodingCombo.add((String) defaults.get(i));
		}

		_encodingCombo.setText(isDefault ? _defaultEncoding : _encoding);
		updateEncodingState(isDefault);
	}

	public Control getDefaultControl()
	{
		return _encodingCombo;
	}
	
	public boolean usingDefault()
	{
		return _defaultEncodingButton.getSelection();
	}
	
	public String getEncoding()
	{		
		if (usingDefault())
		{
			return "";
		}
		return _encodingCombo.getText();
	}
	
	protected void updateValidState() 
	{
		if (!isEncodingValid()) 
		{
			getMessageLine().setErrorMessage(ShellResources.RESID_UNSUPPORTED_ENCODING);
		}
		else
		{
			getMessageLine().clearErrorMessage();
		}
	}

	private boolean isEncodingValid() 
	{
		return _defaultEncodingButton.getSelection()
				|| isValidEncoding(_encodingCombo.getText());
	}

	private boolean isValidEncoding(String enc) 
	{
		try {
			new String(new byte[0], enc);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

	private void updateEncodingState(boolean useDefault) 
	{
		_defaultEncodingButton.setSelection(useDefault);
		_otherEncodingButton.setSelection(!useDefault);
		_encodingCombo.setEnabled(!useDefault);
		updateValidState();
	}

}