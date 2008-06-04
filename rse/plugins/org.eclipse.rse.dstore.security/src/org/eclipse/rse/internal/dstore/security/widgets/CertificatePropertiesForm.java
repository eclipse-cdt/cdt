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
 * David Dykstal (IBM) - [231913] increasing width of form to accommodate translation
 * David Dykstal (IBM) - [232131] using ICU for DateFormat
 * Martin Oberhuber (Wind River) - [235626] Convert dstore.security to MessageBundle format
 *******************************************************************************/

package org.eclipse.rse.internal.dstore.security.widgets;

import java.security.Key;
import java.security.cert.X509Certificate;

import org.eclipse.rse.internal.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.internal.dstore.security.util.GridUtil;
import org.eclipse.rse.internal.dstore.security.util.StringModifier;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.DateFormat;

public class CertificatePropertiesForm extends SystemBaseForm
{
	private Object _certificate;
	private String      _alias;
	private boolean _advanced;

	public CertificatePropertiesForm(Shell shell, Object certificate, String alias)
	{
		this(shell, certificate, alias, false);
	}

	public CertificatePropertiesForm(Shell shell, Object certificate)
	{
		this(shell, certificate, false);
	}

	public CertificatePropertiesForm(Shell shell, Object certificate, String alias, boolean advanced)
	{
		super(shell, null);
		_certificate = certificate;
		_alias = alias;
		_advanced = advanced;
	}

	public CertificatePropertiesForm(Shell shell, Object certificate, boolean advanced)
	{
		this(shell, certificate, null, advanced);
	}


	public Control createContents(Composite parent)
	{
		if (_advanced)
		{
			return createAdvancedContents(parent);
		}
		else
		{
			return createSimpleContents(parent);
		}
	}

	public Control createSimpleContents(Composite parent)
	{
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		GridData data = GridUtil.createFill();
		data.widthHint = 450;
		//data.heightHint = 300;
		layout.numColumns = 2;
		content.setLayout(layout);
		content.setLayoutData(data);

		if (_alias != null)
		{
			Label lblAlias = new Label(content, SWT.NONE);
			lblAlias.setText(UniversalSecurityProperties.RESID_SECURITY_PROP_ALIAS_LBL);
			data = new GridData();
			data.horizontalIndent = 5;
			lblAlias.setLayoutData(data);

			Label lblAliasValue = new Label(content, SWT.NONE);
			lblAliasValue.setText(_alias);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalIndent = 5;
			lblAliasValue.setLayoutData(data);
		}

		Label lblVersion = new Label(content, SWT.NONE);
		lblVersion.setText(UniversalSecurityProperties.RESID_SECURITY_CERTIF_VERSION_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		lblVersion.setLayoutData(data);

		Label lblVersionValue = new Label(content, SWT.NONE);

		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		lblVersionValue.setLayoutData(data);

		Label lblIssuedTo = new Label(content, SWT.NONE);
		lblIssuedTo.setText(UniversalSecurityProperties.RESID_SECURITY_ISSUED_TO_LBL);
		data = new GridData();
		data.verticalAlignment = GridData.BEGINNING;
		data.horizontalIndent = 5;
		lblIssuedTo.setLayoutData(data);

		Text lblIssuedToValue = new Text(content, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP) ;
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 60;
		data.horizontalIndent = 5;
		lblIssuedToValue.setLayoutData(data);

		Label lblIssuedBy = new Label(content, SWT.NONE);
		lblIssuedBy.setText(UniversalSecurityProperties.RESID_SECURITY_ISSUED_BY_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		data.verticalAlignment = GridData.BEGINNING;
		lblIssuedBy.setLayoutData(data);

		Text lblIssuedByValue = new Text(content, SWT.BORDER |SWT.READ_ONLY | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		data.heightHint = 60;
		lblIssuedByValue.setLayoutData(data);

		Label lblValidity = new Label(content, SWT.NONE);
		lblValidity.setText(UniversalSecurityProperties.RESID_SECURITY_VALIDITY_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		lblValidity.setLayoutData(data);

		Label lblValidityValue = new Label(content, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		lblValidityValue.setLayoutData(data);

		Label lblAlgorithm = new Label(content, SWT.NONE);
		lblAlgorithm.setText(UniversalSecurityProperties.RESID_SECURITY_ALGORITHM_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		lblAlgorithm.setLayoutData(data);

		Label lblAlgorithmValue = new Label(content, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		lblAlgorithmValue.setLayoutData(data);

		if(_certificate instanceof X509Certificate){
			lblVersionValue.setText(((X509Certificate)_certificate).getType() + " V."+((X509Certificate)_certificate).getVersion()); //$NON-NLS-1$
			lblIssuedToValue.setText(((X509Certificate)_certificate).getSubjectDN().getName());
			lblIssuedByValue.setText(((X509Certificate)_certificate).getIssuerDN().getName());
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

			String validity = UniversalSecurityProperties.RESID_SECURITY_VALIDITY_PERIOD;
			validity = StringModifier.change(validity,"%1", df.format(((X509Certificate)_certificate).getNotBefore())); //$NON-NLS-1$
			validity = StringModifier.change(validity,"%2", df.format(((X509Certificate)_certificate).getNotAfter())); //$NON-NLS-1$

			lblValidityValue.setText(validity);
			lblAlgorithmValue.setText(((X509Certificate)_certificate).getSigAlgName());
		}else if(_certificate instanceof Key){
			lblVersionValue.setText(((Key)_certificate).getFormat());
			lblAlgorithmValue.setText(((Key)_certificate).getAlgorithm());
		}

		return content;
	}


	public Control createAdvancedContents(Composite parent)
	{
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		GridData data = GridUtil.createFill();
		data.widthHint = 600;
		//data.heightHint = 300;
		layout.numColumns = 2;
		content.setLayout(layout);
		content.setLayoutData(data);

		if (_alias != null)
		{
			Label lblAlias = new Label(content, SWT.NONE);
			lblAlias.setText(UniversalSecurityProperties.RESID_SECURITY_PROP_ALIAS_LBL);
			data = new GridData();
			data.horizontalIndent = 5;
			lblAlias.setLayoutData(data);

			Label lblAliasValue = new Label(content, SWT.NONE);
			lblAliasValue.setText(_alias);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalIndent = 5;
			lblAliasValue.setLayoutData(data);
		}

		Label lblVersion = new Label(content, SWT.NONE);
		lblVersion.setText(UniversalSecurityProperties.RESID_SECURITY_CERTIF_VERSION_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		lblVersion.setLayoutData(data);

		Label lblVersionValue = new Label(content, SWT.NONE);

		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		lblVersionValue.setLayoutData(data);

		Label lblIssuedTo = new Label(content, SWT.NONE);
		lblIssuedTo.setText(UniversalSecurityProperties.RESID_SECURITY_ISSUED_TO_LBL);
		data = new GridData();
		data.verticalAlignment = GridData.BEGINNING;
		data.horizontalIndent = 5;
		lblIssuedTo.setLayoutData(data);

		Text lblIssuedToValue = new Text(content, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP) ;
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 60;
		data.horizontalIndent = 5;
		lblIssuedToValue.setLayoutData(data);

		Label lblIssuedBy = new Label(content, SWT.NONE);
		lblIssuedBy.setText(UniversalSecurityProperties.RESID_SECURITY_ISSUED_BY_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		data.verticalAlignment = GridData.BEGINNING;
		lblIssuedBy.setLayoutData(data);

		Text lblIssuedByValue = new Text(content, SWT.BORDER |SWT.READ_ONLY | SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		data.heightHint = 60;
		lblIssuedByValue.setLayoutData(data);

		Label lblValidity = new Label(content, SWT.NONE);
		lblValidity.setText(UniversalSecurityProperties.RESID_SECURITY_VALIDITY_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		lblValidity.setLayoutData(data);

		Label lblValidityValue = new Label(content, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		lblValidityValue.setLayoutData(data);

		Label lblAlgorithm = new Label(content, SWT.NONE);
		lblAlgorithm.setText(UniversalSecurityProperties.RESID_SECURITY_ALGORITHM_LBL);
		data = new GridData();
		data.horizontalIndent = 5;
		lblAlgorithm.setLayoutData(data);

		Label lblAlgorithmValue = new Label(content, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		lblAlgorithmValue.setLayoutData(data);

		if(_certificate instanceof X509Certificate){
			lblVersionValue.setText(((X509Certificate)_certificate).getType() + " V."+((X509Certificate)_certificate).getVersion()); //$NON-NLS-1$
			lblIssuedToValue.setText(((X509Certificate)_certificate).getSubjectDN().getName());
			lblIssuedByValue.setText(((X509Certificate)_certificate).getIssuerDN().getName());
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

			String validity = UniversalSecurityProperties.RESID_SECURITY_VALIDITY_PERIOD;
			validity = StringModifier.change(validity,"%1", df.format(((X509Certificate)_certificate).getNotBefore())); //$NON-NLS-1$
			validity = StringModifier.change(validity,"%2", df.format(((X509Certificate)_certificate).getNotAfter())); //$NON-NLS-1$

			lblValidityValue.setText(validity);
			lblAlgorithmValue.setText(((X509Certificate)_certificate).getSigAlgName());
		}else if(_certificate instanceof Key){
			lblVersionValue.setText(((Key)_certificate).getFormat());
			lblAlgorithmValue.setText(((Key)_certificate).getAlgorithm());
		}

		return content;
	}
}
