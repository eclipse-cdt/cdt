/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.panels;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.core.terminals.TerminalContextPropertiesProviderFactory;
import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalContextPropertiesProvider;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.IContextPropertiesConstants;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanelContainer;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.WorkbenchEncoding;
import org.osgi.framework.Bundle;

/**
 * Abstract terminal configuration panel implementation.
 */
public abstract class AbstractExtendedConfigurationPanel extends AbstractConfigurationPanel {
	private static final String LAST_HOST_TAG = "lastHost";//$NON-NLS-1$
	private static final String HOSTS_TAG = "hosts";//$NON-NLS-1$
	private static final String ENCODINGS_TAG = "encodings"; //$NON-NLS-1$

	// The sub-controls
	/* default */ Combo hostCombo;
	private Button deleteHostButton;
	/* default */ Combo encodingCombo;

	// The last selected encoding
	/* default */ String lastSelectedEncoding;
	// The last entered custom encodings
	/* default */ final List<String> encodingHistory = new ArrayList<String>();

	// A map containing the settings per host
	protected final Map<String, Map<String, String>> hostSettingsMap = new HashMap<String, Map<String, String>>();

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public AbstractExtendedConfigurationPanel(IConfigurationPanelContainer container) {
		super(container);
	}

	/**
	 * Returns the host name or IP from the current selection.
	 *
	 * @return The host name or IP, or <code>null</code>.
	 */
	public String getSelectionHost() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			ITerminalContextPropertiesProvider provider = TerminalContextPropertiesProviderFactory.getProvider(element);
			if (provider != null) {
				Map<String, String> props = provider.getTargetAddress(element);
				if (props != null && props.containsKey(IContextPropertiesConstants.PROP_ADDRESS)) {
					return props.get(IContextPropertiesConstants.PROP_ADDRESS);
				}
			}
		}

		return null;
	}

	/**
	 * Returns the default encoding based on the current selection.
	 *
	 * @return The default encoding or <code>null</code>.
	 */
	public String getSelectionEncoding() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			ITerminalContextPropertiesProvider provider = TerminalContextPropertiesProviderFactory.getProvider(element);
			if (provider != null) {
				Object encoding = provider.getProperty(element, IContextPropertiesConstants.PROP_DEFAULT_ENCODING);
				if (encoding instanceof String) return ((String) encoding).trim();
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		String[] hosts = settings.getArray(HOSTS_TAG);
		if (hosts != null) {
			for (int i = 0; i < hosts.length; i++) {
				String hostEntry = hosts[i];
				String[] hostString = hostEntry.split("\\|");//$NON-NLS-1$
				String hostName = hostString[0];
				if (hostString.length == 2) {
					HashMap<String, String> attr = deSerialize(hostString[1]);
					hostSettingsMap.put(hostName, attr);
				}
				else {
					hostSettingsMap.put(hostName, new HashMap<String, String>());
				}
			}
		}

		if (!isWithoutSelection()) {
			String host = getSelectionHost();
			if (host != null) {
				fillSettingsForHost(host);
			}
		}
		else {
			if (hostCombo != null) {
				fillHostCombo();
				String lastHost = settings.get(LAST_HOST_TAG);
				if (lastHost != null) {
					int index = hostCombo.indexOf(lastHost);
					if (index != -1) {
						hostCombo.select(index);
					}
					else {
						hostCombo.select(0);
					}
				}
				else {
					hostCombo.select(0);
				}
				fillSettingsForHost(hostCombo.getText());
			}
		}

		encodingHistory.clear();
		String[] encodings = settings.getArray(ENCODINGS_TAG);
		if (encodings != null && encodings.length > 0) {
			encodingHistory.addAll(Arrays.asList(encodings));
			for (String encoding : encodingHistory) {
				encodingCombo.add(encoding, encodingCombo.getItemCount() - 1);
			}
		}
	}

	/**
	 * Restore the encodings widget values.
	 *
	 * @param settings The dialog settings. Must not be <code>null</code>.
	 * @param idPrefix The prefix or <code>null</code>.
	 */
	protected void doRestoreEncodingsWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		String encoding = settings.get(prefixDialogSettingsSlotId(ITerminalsConnectorConstants.PROP_ENCODING, idPrefix));
		if (encoding != null && encoding.trim().length() > 0) {
			setEncoding(encoding);
		}
	}

	/**
	 * Decode the host settings from the given string.
	 *
	 * @param hostString The encoded host settings. Must not be <code>null</code>.
	 * @return The decoded host settings.
	 */
	private HashMap<String, String> deSerialize(String hostString) {
		Assert.isNotNull(hostString);
		HashMap<String, String> attr = new HashMap<String, String>();

		if (hostString.length() != 0) {
			String[] hostAttrs = hostString.split("\\:");//$NON-NLS-1$
			for (int j = 0; j < hostAttrs.length-1; j = j + 2) {
				String key = hostAttrs[j];
				String value = hostAttrs[j + 1];
				attr.put(key, value);
			}
		}
		return attr;
	}

	/**
	 * Encode the host settings to a string.
	 *
	 * @param hostEntry The host settings. Must not be <code>null</code>.
	 * @param hostString The host string to encode to. Must not be <code>null</code>.
	 */
	private void serialize(Map<String, String> hostEntry, StringBuilder hostString) {
		Assert.isNotNull(hostEntry);
		Assert.isNotNull(hostString);

		if (hostEntry.keySet().size() != 0) {
			Iterator<Entry<String, String>> nextHostAttr = hostEntry.entrySet().iterator();
			while (nextHostAttr.hasNext()) {
				Entry<String, String> entry = nextHostAttr.next();
				String attrKey = entry.getKey();
				String attrValue = entry.getValue();
				hostString.append(attrKey + ":" + attrValue + ":");//$NON-NLS-1$ //$NON-NLS-2$
			}
			hostString.deleteCharAt(hostString.length() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Iterator<String> nextHost = hostSettingsMap.keySet().iterator();
		String[] hosts = new String[hostSettingsMap.keySet().size()];
		int i = 0;
		while (nextHost.hasNext()) {
			StringBuilder hostString = new StringBuilder();
			String host = nextHost.next();
			hostString.append(host + "|");//$NON-NLS-1$
			Map<String, String> hostEntry = hostSettingsMap.get(host);
			serialize(hostEntry, hostString);
			hosts[i] = hostString.toString();
			i = i + 1;
		}
		settings.put(HOSTS_TAG, hosts);
		if (isWithoutSelection()) {
			if (hostCombo != null) {
				String host = getHostFromSettings();
				if (host != null) settings.put(LAST_HOST_TAG, host);
			}
		}

		if (!encodingHistory.isEmpty()) {
			settings.put(ENCODINGS_TAG, encodingHistory.toArray(new String[encodingHistory.size()]));
		}
	}

	/**
	 * Save the encodings widget values.
	 *
	 * @param settings The dialog settings. Must not be <code>null</code>.
	 * @param idPrefix The prefix or <code>null</code>.
	 */
	protected void doSaveEncodingsWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		String encoding = getEncoding();
		if (encoding != null) {
			settings.put(prefixDialogSettingsSlotId(ITerminalsConnectorConstants.PROP_ENCODING, idPrefix), encoding);
		}
	}

	protected abstract void saveSettingsForHost(boolean add);

	protected abstract void fillSettingsForHost(String host);

	protected abstract String getHostFromSettings();

	protected void removeSecurePassword(String host) {
		// noop by default
	}

	/**
	 * Returns the selected host from the hosts combo widget.
	 *
	 * @return The selected host or <code>null</code>.
	 */
	protected final String getHostFromCombo() {
		return hostCombo != null && !hostCombo.isDisposed() ? hostCombo.getText() : null;
	}

	protected void removeSettingsForHost(String host) {
		if (hostSettingsMap.containsKey(host)) {
			hostSettingsMap.remove(host);
		}
	}

	/**
	 * Returns the list of host names of the persisted hosts.
	 *
	 * @return The list of host names.
	 */
	private List<String> getHostList() {
		List<String> hostList = new ArrayList<String>();
		hostList.addAll(hostSettingsMap.keySet());
		return hostList;
	}

	/**
	 * Fill the host combo with the stored per host setting names.
	 */
	protected void fillHostCombo() {
		if (hostCombo != null) {
			hostCombo.removeAll();
			List<String> hostList = getHostList();
			Collections.sort(hostList);
			Iterator<String> nextHost = hostList.iterator();
			while (nextHost.hasNext()) {
				String host = nextHost.next();
				hostCombo.add(host);
			}
			if (hostList.size() <= 1) {
				hostCombo.setEnabled(false);
			}
			else {
				hostCombo.setEnabled(true);

			}
			if (deleteHostButton != null) {
				if (hostList.size() == 0) {
					deleteHostButton.setEnabled(false);
				}
				else {
					deleteHostButton.setEnabled(true);
				}
			}
		}
	}

	public boolean isWithoutSelection() {
		ISelection selection = getSelection();
		if (selection == null) {
			return true;
		}
		if (selection instanceof IStructuredSelection && selection.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean isWithHostList() {
		return true;
	}

	/**
	 * Create the host selection combo.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param separator If <code>true</code>, a separator will be added after the controls.
	 */
	protected void createHostsUI(Composite parent, boolean separator) {
		Assert.isNotNull(parent);

		if (isWithoutSelection() && isWithHostList()) {
			Composite comboComposite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(3, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			comboComposite.setLayout(layout);
			comboComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label label = new Label(comboComposite, SWT.HORIZONTAL);
			label.setText(Messages.AbstractConfigurationPanel_hosts);

			hostCombo = new Combo(comboComposite, SWT.READ_ONLY);
			hostCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			hostCombo.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					String host = hostCombo.getText();
					fillSettingsForHost(host);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			deleteHostButton = new Button(comboComposite, SWT.NONE);
			// deleteHostButton.setText(Messages.AbstractConfigurationPanel_delete);

			ISharedImages workbenchImages = UIPlugin.getDefault().getWorkbench().getSharedImages();
			deleteHostButton.setImage(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE).createImage());

			deleteHostButton.setToolTipText(Messages.AbstractConfigurationPanel_deleteButtonTooltip);
			deleteHostButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					String host = getHostFromCombo();
					if (host != null && host.length() != 0) {
						removeSettingsForHost(host);
						removeSecurePassword(host);
						fillHostCombo();
						hostCombo.select(0);
						host = getHostFromCombo();
						if (host != null && host.length() != 0) {
							fillSettingsForHost(host);
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			if (separator) {
				Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
				sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}
		}
	}

	/**
	 * Create the encoding selection combo.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param separator If <code>true</code>, a separator will be added before the controls.
	 */
	protected void createEncodingUI(final Composite parent, boolean separator) {
		Assert.isNotNull(parent);

		if (separator) {
			Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label = new Label(panel, SWT.HORIZONTAL);
		label.setText(Messages.AbstractConfigurationPanel_encoding);

		encodingCombo = new Combo(panel, SWT.READ_ONLY);
		encodingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		encodingCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (Messages.AbstractConfigurationPanel_encoding_custom.equals(encodingCombo.getText())) {
					InputDialog dialog = new InputDialog(parent.getShell(),
														 Messages.AbstractConfigurationPanel_encoding_custom_title,
														 Messages.AbstractConfigurationPanel_encoding_custom_message,
														 null,
														 new IInputValidator() {
															@Override
															public String isValid(String newText) {
																boolean valid = false;
																try {
																	if (newText != null && !"".equals(newText)) { //$NON-NLS-1$
																		valid = Charset.isSupported(newText);
																	}
																} catch (IllegalCharsetNameException e) { /* ignored on purpose */ }

																if (!valid) {
																	return newText != null && !"".equals(newText) ? Messages.AbstractConfigurationPanel_encoding_custom_error : ""; //$NON-NLS-1$ //$NON-NLS-2$
																}
																return null;
															}
														});
					if (dialog.open() == Window.OK) {
						String encoding = dialog.getValue();
						encodingCombo.add(encoding, encodingCombo.getItemCount() - 1);
						encodingCombo.select(encodingCombo.indexOf(encoding));
						lastSelectedEncoding = encodingCombo.getText();

						// Remember the last 5 custom encodings entered
						if (!encodingHistory.contains(encoding)) {
							if (encodingHistory.size() == 5) encodingHistory.remove(4);
							encodingHistory.add(encoding);
						}

					} else {
						encodingCombo.select(encodingCombo.indexOf(lastSelectedEncoding));
					}
				}
			}
		});

		fillEncodingCombo();

		// Apply any default encoding derived from the current selection
		String defaultEncoding = getSelectionEncoding();
		if (defaultEncoding != null && !"".equals(defaultEncoding)) { //$NON-NLS-1$
			setEncoding(defaultEncoding);
		}
	}

	/**
	 * Fill the encoding combo.
	 */
	protected void fillEncodingCombo() {
		if (encodingCombo != null) {
			List<String> encodings = new ArrayList<String>();

			// Some hard-coded encodings
			encodings.add("Default (ISO-8859-1)"); //$NON-NLS-1$
			encodings.add("UTF-8"); //$NON-NLS-1$

			// The currently selected IDE encoding from the preferences
			String ideEncoding = getResourceEncoding();
			if (ideEncoding != null && !encodings.contains(ideEncoding)) encodings.add(ideEncoding);

			// The default Eclipse Workbench encoding (configured in the preferences)
			String eclipseEncoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
			if (eclipseEncoding != null && !encodings.contains(eclipseEncoding)) encodings.add(eclipseEncoding);

			// The default host (Java VM) encoding
			//
			// Note: We do not use Charset.defaultCharset().displayName() here as it returns the bit
			//       unusual name "windows-1252" on Windows. As there is no access to the "historical"
			//       name "Cp1252" stored in MS1252.class, stick to the older way of retrieving an encoding.
			String hostEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();
			if (!encodings.contains(hostEncoding)) encodings.add(hostEncoding);

			// The "Other..." encoding
			encodings.add(Messages.AbstractConfigurationPanel_encoding_custom);

			encodingCombo.setItems(encodings.toArray(new String[encodings.size()]));
			encodingCombo.select(0);

			lastSelectedEncoding = encodingCombo.getText();
		}
	}

	/**
	 * Get the current value of the encoding preference. If the value is not set
	 * return <code>null</code>.
	 * <p>
	 * <b>Note:</b> Copied from <code>org.eclipse.ui.ide.IDEEncoding</code>.
	 *
	 * @return String
	 */
	@SuppressWarnings("deprecation")
    private String getResourceEncoding() {
		String preference = null;
		Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			preference = org.eclipse.core.resources.ResourcesPlugin.getPlugin().getPluginPreferences().getString(org.eclipse.core.resources.ResourcesPlugin.PREF_ENCODING);
		}

		return preference != null && preference.length() > 0 ? preference : null;
	}

	/**
	 * Select the encoding.
	 *
	 * @param encoding The encoding. Must not be <code>null</code>.
	 */
	protected void setEncoding(String encoding) {
		Assert.isNotNull(encoding);

		if (encodingCombo != null && !encodingCombo.isDisposed()) {
			int index = encodingCombo.indexOf("ISO-8859-1".equals(encoding) ? "Default (ISO-8859-1)" : encoding); //$NON-NLS-1$ //$NON-NLS-2$
			if (index != -1) encodingCombo.select(index);
			else {
				encodingCombo.add(encoding, encodingCombo.getItemCount() - 1);
				encodingCombo.select(encodingCombo.indexOf(encoding));
			}

			lastSelectedEncoding = encodingCombo.getText();
		}
	}

	/**
	 * Returns the selected encoding.
	 *
	 * @return The selected encoding or <code>null</code>.
	 */
	protected String getEncoding() {
		String encoding = encodingCombo != null && !encodingCombo.isDisposed() ? encodingCombo.getText() : null;
		return encoding != null && encoding.startsWith("Default") ? null : encoding; //$NON-NLS-1$
	}

	/**
	 * Returns if or if not the selected encoding is supported.
	 *
	 * @return <code>True</code> if the selected encoding is supported.
	 */
	protected boolean isEncodingValid() {
		try {
			String encoding = getEncoding();
			return Charset.isSupported(encoding != null ? encoding : "ISO-8859-1"); //$NON-NLS-1$
		} catch (IllegalCharsetNameException e) {
			return false;
		}
	}
}
