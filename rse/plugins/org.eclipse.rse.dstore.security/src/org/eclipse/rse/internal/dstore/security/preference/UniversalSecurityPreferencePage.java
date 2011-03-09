/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
 * David Dykstal (IBM) - [232131] fix minor layout problems along with date formats
 * Martin Oberhuber (Wind River) - [235626] Convert dstore.security to MessageBundle format
 * David McKnight (IBM)          - [210099] SSL Preference Page Restore Defaults Doesn't do anything
 *******************************************************************************/

package org.eclipse.rse.internal.dstore.security.preference;


import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dstore.core.util.ssl.DStoreKeyStore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.internal.dstore.security.UniversalSecurityPlugin;
import org.eclipse.rse.internal.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.internal.dstore.security.util.GridUtil;
import org.eclipse.rse.internal.dstore.security.util.StringModifier;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UniversalSecurityPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, Listener, SelectionListener
{

	private TableViewer		_viewer;

	ArrayList				_tableItems	= new ArrayList();

	private Button			_addButton;

	private Button			_removeButton;

	private Button			_renameButton;

	private Button			_propertiesButton;

	KeyStore				_keyStore;

	public UniversalSecurityPreferencePage()
	{
		super();
		setPreferenceStore(UniversalSecurityPlugin.getDefault()
				.getPreferenceStore());
	}

	protected Control createContents(Composite parent)
	{
		Composite composite = SystemWidgetHelpers.createComposite(parent, 1);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 5;
		layout.verticalSpacing = 10;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(GridUtil.createFill());

		Text label = new Text(composite, SWT.READ_ONLY);
		label.setBackground(composite.getBackground());
		label.setText(UniversalSecurityProperties.RESID_SECURITY_PREF_SEC_DESCRIPTION);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		createTableViewer(composite);

		Composite buttons = new Composite(composite, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		buttons.setLayoutData(data);
		layout = new GridLayout();
		layout.numColumns = 1;
		buttons.setLayout(layout);

		createButtons(buttons);
		initializeValues();

		SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "ssls0000"); //$NON-NLS-1$
		return composite;
	}

	private void createTableViewer(Composite parent)
	{
		// Create the table viewer.
		_viewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE
				| SWT.FULL_SELECTION);

		// Create the table control.
		Table table = _viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = GridUtil.createFill();
		data.heightHint = 50;
		table.setLayoutData(data);

		TableLayout tableLayout = new TableLayout();


		TableColumn aliasColumn = new TableColumn(table, SWT.LEFT);
		aliasColumn.setText(UniversalSecurityProperties.RESID_SECURITY_PREF_ALIAS_NAME);
		tableLayout.addColumnData(new ColumnPixelData(100));

		TableColumn toColumn = new TableColumn(table, SWT.LEFT);
		toColumn.setText(UniversalSecurityProperties.RESID_SECURITY_PREF_ISSUED_TO);
		tableLayout.addColumnData(new ColumnPixelData(150));

		TableColumn frmColumn = new TableColumn(table, SWT.LEFT);
		frmColumn.setText(UniversalSecurityProperties.RESID_SECURITY_PREF_ISSUED_FROM);
		tableLayout.addColumnData(new ColumnPixelData(150));

		TableColumn expColumn = new TableColumn(table, SWT.LEFT);
		expColumn.setText(UniversalSecurityProperties.RESID_SECURITY_PREF_EXPIRES);
		tableLayout.addColumnData(new ColumnPixelData(150));
		table.setLayout(tableLayout);

		// Adjust the table viewer.
		String[] properties = new String[] {"STRING", "STRING", "STRING", "STRING"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		_viewer.setColumnProperties(properties);
		_viewer.setContentProvider(new CertTableContentProvider());
		_viewer.setLabelProvider(new CertTableLabelProvider());
		_viewer.getTable().addSelectionListener(this);

		CertTableSorter.setTableSorter(_viewer, 0, true);
	}

	private void createButtons(Composite parent)
	{
		Composite buttonComposite = SystemWidgetHelpers.createComposite(parent, 4);

		_addButton = SystemWidgetHelpers.createPushButton(buttonComposite, UniversalSecurityProperties.RESID_SECURITY_ADD_LBL, this);


		_removeButton = SystemWidgetHelpers.createPushButton(buttonComposite, UniversalSecurityProperties.RESID_SECURITY_REMOVE_LBL, this);
		_removeButton.setEnabled(false);

		_renameButton = SystemWidgetHelpers.createPushButton(buttonComposite, UniversalSecurityProperties.RESID_SECURITY_RENAME_LBL, this);
		_renameButton.setEnabled(false);

		_propertiesButton = SystemWidgetHelpers.createPushButton(buttonComposite, UniversalSecurityProperties.RESID_SECURITY_PROPERTIES_LBL, this);
		_propertiesButton.setEnabled(false);
	}


	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

	/**
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults()
	{
		super.performDefaults();

	}

	/**
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk()
	{

		String storePath = UniversalSecurityPlugin.getKeyStoreLocation();
		String passw = UniversalSecurityPlugin.getKeyStorePassword();
		try
		{

			DStoreKeyStore.persistKeyStore(_keyStore, storePath, passw);

		}
		catch (IOException e)
		{

			String text = UniversalSecurityProperties.RESID_SECURITY_IO_SAVE_ERROR_;
			text = StringModifier.change(text, "%1", storePath); //$NON-NLS-1$

			text = StringModifier.change(text, "%1", storePath); //$NON-NLS-1$
			String msg = UniversalSecurityProperties.RESID_SECURITY_KEYSTORE_SAVE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, e);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);
			return false;

		}
		catch (CertificateException exc)
		{

			String text = UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_STORE_ERROR_;
			text = StringModifier.change(text, "%1", storePath); //$NON-NLS-1$
			String msg = UniversalSecurityProperties.RESID_SECURITY_KEYSTORE_SAVE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc);
			ErrorDialog.openError(UniversalSecurityPlugin
					.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);
			return false;

		}
		catch (KeyStoreException exc)
		{
			String text = UniversalSecurityProperties.RESID_SECURITY_UNINIT_KEYSTORE_ERROR_;
			text = StringModifier.change(text, "%1", UniversalSecurityPlugin //$NON-NLS-1$
					.getKeyStoreLocation());
			String msg = UniversalSecurityProperties.RESID_SECURITY_KEYSTORE_SAVE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);
			return false;

		}
		catch (NoSuchAlgorithmException exc2)
		{
			String text = UniversalSecurityProperties.RESID_SECURITY_ALGORITHM_ERROR_;
			text = StringModifier.change(text, "%1", UniversalSecurityPlugin //$NON-NLS-1$
					.getKeyStoreLocation());
			String msg = UniversalSecurityProperties.RESID_SECURITY_KEYSTORE_SAVE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc2);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);
			return false;

		}
		return true;

	}

	/**
	 * Loads certificates from the key store.
	 */
	private void initializeValues()
	{
		noDefaultAndApplyButton(); // restore defaults makes no sense here

		String storePath = UniversalSecurityPlugin.getKeyStoreLocation();
		String passw = UniversalSecurityPlugin.getKeyStorePassword();
		// String passw = "dstore";

		try
		{
			_keyStore = DStoreKeyStore.getKeyStore(storePath, passw);

			Enumeration aliases = _keyStore.aliases();

			while (aliases.hasMoreElements())
			{
				String alias = (String) (aliases.nextElement());
				/* The alias may be either a key or a certificate */
				java.security.cert.Certificate cert = _keyStore
						.getCertificate(alias);
				if (cert != null)
				{
					if (cert instanceof X509Certificate)
					{
						X509CertificateElement elem = new X509CertificateElement(
								alias,
								UniversalSecurityProperties.RESID_SECURITY_TRUSTED_CERTIFICATE,
								(X509Certificate) cert);
						_tableItems.add(elem);
					}
				}
				else
				{
					try
					{
						Key key = _keyStore.getKey(alias, passw.toCharArray());
						KeyElement elem = new KeyElement(alias,
								UniversalSecurityProperties.RESID_SECURITY_KEY_ENTRY,
								key);
						_tableItems.add(elem);
					}
					catch (UnrecoverableKeyException e)
					{
						/* Probably ignore the key in this case */
					}
				}

			}
		}
		catch (IOException e)
		{

			String text = UniversalSecurityProperties.RESID_SECURITY_LOAD_IO_EXC_;
			text = StringModifier.change(text, "%1", storePath); //$NON-NLS-1$
			String msg = UniversalSecurityProperties.RESID_SECURITY_LOAD_KEYSTORE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, e);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);

		}
		catch (CertificateException exc)
		{

			String text = UniversalSecurityProperties.RESID_SECURITY_KEY_LOAD_ERROR_;
			text = StringModifier.change(text, "%1", storePath); //$NON-NLS-1$
			String msg = UniversalSecurityProperties.RESID_SECURITY_LOAD_KEYSTORE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);

		}
		catch (KeyStoreException exc)
		{

			String text = UniversalSecurityProperties.RESID_SECURITY_INITIALIZE_ERROR_;
			text = StringModifier.change(text, "%1", UniversalSecurityPlugin //$NON-NLS-1$
					.getKeyStoreLocation());
			String msg = UniversalSecurityProperties.RESID_SECURITY_LOAD_KEYSTORE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);

		}
		catch (NoSuchProviderException exc2)
		{

			String text = UniversalSecurityProperties.RESID_SECURITY_SECURITY_PROVIDER_ERROR_;
			String msg = UniversalSecurityProperties.RESID_SECURITY_INITIALIZE_ERROR_;
			msg = StringModifier.change(msg, "%1", UniversalSecurityPlugin //$NON-NLS-1$
					.getKeyStoreLocation());
			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc2);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);

		}
		catch (NoSuchAlgorithmException exc2)
		{
			String text = UniversalSecurityProperties.RESID_SECURITY_ALGORITHM_ERROR_;
			text = StringModifier.change(text, "%1", UniversalSecurityPlugin //$NON-NLS-1$
					.getKeyStoreLocation());
			String msg = UniversalSecurityProperties.RESID_SECURITY_LOAD_KEYSTORE_ERROR_;

			Status err = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
					IStatus.ERROR, text, exc2);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(), UniversalSecurityProperties.RESID_SECURITY_SEC_MSG, msg, err);

		}

		_viewer.setInput(_tableItems);
	}

	public void handleEvent(Event event)
	{
		// TODO Auto-generated method stub
		if (event.widget == _addButton)
		{
			NewCertDialog dlg = new NewCertDialog(this, getShell());
			dlg.open();

			if (dlg.getReturnCode() == Window.OK)
			{
				_viewer.refresh();
			}

		}
		else if (event.widget == _removeButton)
		{
			IStructuredSelection elem = (IStructuredSelection) _viewer.getSelection();
			if (elem.size() > 0)
			{
				Iterator i = elem.iterator();
				while (i.hasNext())
				{
					try
					{
						Element current = (Element) i.next();
						_keyStore.deleteEntry(current.getAlias());
						_tableItems.remove(current);
					}
					catch (KeyStoreException e)
					{
					}
				}

				_viewer.refresh();
			}

		}
		else if (event.widget == _renameButton)
		{
			IStructuredSelection elem = (IStructuredSelection) _viewer
					.getSelection();
			if (elem.size() == 1)
			{
				Element sel = (Element) elem.getFirstElement();
				RenameCertDialog dlg = new RenameCertDialog(this, getShell(),
						sel.getAlias());
				dlg.open();

				if (dlg.getReturnCode() == Window.OK)
				{
					try
					{
						DStoreKeyStore.addCertificateToKeyStore(
								UniversalSecurityPreferencePage.this._keyStore,
								(Certificate) sel.getCert(), dlg.getNewAlias());
						_keyStore.deleteEntry(sel.getAlias());
						sel.setAlias(dlg.getNewAlias());
						_viewer.refresh();
					}
					catch (KeyStoreException e)
					{
					}
				}
			}

		}
		else if (event.widget == _propertiesButton)
		{
			IStructuredSelection elem = (IStructuredSelection) _viewer
					.getSelection();
			if (elem.size() == 1)
			{
				Element sel = (Element) elem.getFirstElement();
				CertPropertiesDialog dlg = null;
				if (sel instanceof X509CertificateElement)
				{
					dlg = new X509CertificatePropertiesDialog(getShell(), (X509CertificateElement)sel);
				}
				else
				{
					dlg = new KeyPropertiesDialog(getShell(), (KeyElement)sel);
				}

				dlg.open();

			}

		}

		boolean sel = _viewer.getSelection().isEmpty();
		_renameButton.setEnabled(!sel);
		_removeButton.setEnabled(!sel);
		_propertiesButton.setEnabled(!sel);
	}

	public void widgetSelected(SelectionEvent e)
	{
		// TODO Auto-generated method stub
		boolean sel = _viewer.getSelection().isEmpty();
		_renameButton.setEnabled(!sel);
		_removeButton.setEnabled(!sel);
		_propertiesButton.setEnabled(!sel);
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

}
