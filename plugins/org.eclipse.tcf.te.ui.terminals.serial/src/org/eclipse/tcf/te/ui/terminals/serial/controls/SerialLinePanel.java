/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.serial.controls;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanelContainer;
import org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel;
import org.eclipse.tcf.te.ui.terminals.serial.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.serial.interfaces.ITraceIds;
import org.eclipse.tcf.te.ui.terminals.serial.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Serial line terminal launcher configuration panel implementation.
 */
public class SerialLinePanel extends AbstractConfigurationPanel {
	public static final String fcDefaultTTYSpeed = "9600"; //$NON-NLS-1$
	public static final String fcDefaultTTYDeviceWin32 = "COM1"; //$NON-NLS-1$
	public static final String fcDefaultTTYDeviceSolaris = "/dev/cua/a"; //$NON-NLS-1$
	public static final String fcDefaultTTYDeviceLinux = "/dev/ttyS0"; //$NON-NLS-1$
	public static final String fcDefaultTTYDatabits = "8"; //$NON-NLS-1$
	public static final String fcDefaultTTYParity = "None"; //$NON-NLS-1$
	public static final String fcDefaultTTYStopbits = "1"; //$NON-NLS-1$
	public static final String fcDefaultTTYFlowControl = "None"; //$NON-NLS-1$
	public static final String fcDefaultTTYTimeout = "5"; //$NON-NLS-1$
	public static final String fcEditableTTYOther = "Other..."; //$NON-NLS-1$

	private static final String[] fcTTYSpeedRates = { "600", //$NON-NLS-1$
		"1200", //$NON-NLS-1$
		"2400", //$NON-NLS-1$
		"4800", //$NON-NLS-1$
		"9600", //$NON-NLS-1$
		"14400", //$NON-NLS-1$
		"19200", //$NON-NLS-1$
		"38400", //$NON-NLS-1$
		"57600", //$NON-NLS-1$
		"115200" //$NON-NLS-1$
	};

	private static final String[] fcTTYDatabits = {
		"8", "7" //$NON-NLS-1$ //$NON-NLS-2$
	};

	private static final String[] fcTTYParity = {
		"None", "Odd", "Even" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};

	private static final String[] fcTTYStopbits = {
		"1", "2" //$NON-NLS-1$ //$NON-NLS-2$
	};

	private static final String[] fcTTYFlowControl = {
		"None", "Hardware", "Software" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	};

	Label hostTTYDeviceLabel;
	Combo hostTTYDeviceCombo;
	Label hostTTYSpeedLabel;
	Combo hostTTYSpeedCombo;
	Label hostTTYBitsLabel;
	Combo hostTTYBitsCombo;
	Label hostTTYParityLabel;
	Combo hostTTYParityCombo;
	Label hostTTYStopbitsLabel;
	Combo hostTTYStopbitsCombo;
	Label hostTTYFlowControlLabel;
	Combo hostTTYFlowControlCombo;
	Label hostTTYTimeoutLabel;
	Text  hostTTYTimeoutText;

	// Keep the fInputValidator protected!
	protected IInputValidator inputValidatorBaud;

	int lastSelected = -1;
	int lastSelectedBaud = -1;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public SerialLinePanel(IConfigurationPanelContainer container) {
		super(container);
	}

	protected class CustomSerialBaudRateInputValidator implements IInputValidator {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
		 */
		@Override
        public String isValid(String newText) {
			if (newText != null && newText.trim().length() > 0) {
				if (!newText.matches("[0-9]*")) { //$NON-NLS-1$
					return Messages.SerialLinePanel_error_invalidCharactesBaudRate;
				}
			} else if (newText != null) {
				// Empty string is an error without message (see interface)!
				return ""; //$NON-NLS-1$
			}
			return null;
		}
	}

	/**
	 * Returns the input validator to be used for checking the custom serial
	 * baud rate for basic plausibility.
	 */
	protected IInputValidator getCustomSerialBaudRateInputValidator() {
		if (inputValidatorBaud == null) {
			inputValidatorBaud = new CustomSerialBaudRateInputValidator();
		}
		return inputValidatorBaud;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    public void setupPanel(Composite parent) {
		Assert.isNotNull(parent);

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		panel.setBackground(parent.getBackground());

		setControl(panel);

		final Composite client = new Composite(parent, SWT.NONE);
		Assert.isNotNull(client);
		client.setLayout(new GridLayout(2, false));
		client.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		client.setBackground(panel.getBackground());

		// Host TTY settings
		hostTTYDeviceLabel = new Label(client, SWT.NONE);
		hostTTYDeviceLabel.setText(Messages.SerialLinePanel_hostTTYDevice_label);

		hostTTYDeviceCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYDeviceCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYDeviceCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if the user selected the special editable device, show a dialog asking for the device name
				if (fcEditableTTYOther.equals(hostTTYDeviceCombo.getText())) {
					List<String> tty = new ArrayList<String>();
					List<String> tcp = new ArrayList<String>();
					String selected = hostTTYDeviceCombo.getItem(lastSelected);
					for (String device : hostTTYDeviceCombo.getItems()) {
						if (!device.equalsIgnoreCase(fcEditableTTYOther)) {
							if (device.toUpperCase().startsWith("TCP:")) { //$NON-NLS-1$
								tcp.add(device);
							}
							else {
								tty.add(device);
							}
						}
					}
					SerialPortAddressDialog dialog = new SerialPortAddressDialog(client.getShell(), selected, tty, tcp);
					if (dialog.open() == Window.OK) {
						// retrieve the custom serial device name and set it to the combobox drop
						String device = dialog.getData();
						if (device != null && device.trim().length() > 0) {
							hostTTYDeviceCombo.add(device.trim());
							hostTTYDeviceCombo.setText(device.trim());
						} else if (lastSelected != -1) {
							hostTTYDeviceCombo.setText(hostTTYDeviceCombo.getItem(lastSelected));
						}
					} else if (lastSelected != -1){
						hostTTYDeviceCombo.setText(hostTTYDeviceCombo.getItem(lastSelected));
					}
				}
				lastSelected = hostTTYDeviceCombo.getSelectionIndex();

				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});

		hostTTYSpeedLabel = new Label(client, SWT.NONE);
		hostTTYSpeedLabel.setText(Messages.SerialLinePanel_hostTTYSpeed_label);

		hostTTYSpeedCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYSpeedCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYSpeedCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if the user selected the special editable baud rate, show a dialog asking for the baud rate
				if (fcEditableTTYOther.equals(hostTTYSpeedCombo.getText())) {
					InputDialog dialog = new InputDialog(getControl().getShell(),
					                                     Messages.SerialLinePanel_customSerialBaudRate_title,
					                                     Messages.SerialLinePanel_customSerialBaudRate_message,
					                                     "", //$NON-NLS-1$
					                                     getCustomSerialBaudRateInputValidator());
					if (dialog.open() == Window.OK) {
						// retrieve the custom serial device name and set it to the combobox drop
						String device = dialog.getValue();
						if (device != null && device.trim().length() > 0) {
							int index = hostTTYSpeedCombo.indexOf(fcEditableTTYOther);
							if (index != -1 && index == hostTTYSpeedCombo.getItemCount() - 1) {
								hostTTYSpeedCombo.add(device.trim());
							} else if (index != -1) {
								hostTTYSpeedCombo.setItem(index + 1, device.trim());
							}
							hostTTYSpeedCombo.setText(device.trim());
						} else if (lastSelectedBaud != -1) {
							hostTTYSpeedCombo.setText(hostTTYSpeedCombo.getItem(lastSelectedBaud));
						}
					} else if (lastSelectedBaud != -1){
						hostTTYSpeedCombo.setText(hostTTYSpeedCombo.getItem(lastSelectedBaud));
					}
				}
				lastSelectedBaud = hostTTYSpeedCombo.getSelectionIndex();

				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});

		// Query the list of available serial port interfaces.
		UIPlugin.getTraceHandler().trace("SerialLinePanel: Start quering the available comm ports.", ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$

		// Query the serial devices now.
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			@Override
			public void run() {
				queryAvailableSerialDevices();
			}
		});

		// add a special device which is being the editable one if requested at the end of the list
		hostTTYDeviceCombo.add(fcEditableTTYOther);

		if (hostTTYDeviceCombo.indexOf(getDefaultHostTTYDevice()) != -1) {
			hostTTYDeviceCombo.setText(getDefaultHostTTYDevice());
		} else {
			if ("".equals(hostTTYDeviceCombo.getText()) && hostTTYDeviceCombo.getItemCount() > 0) { //$NON-NLS-1$
				// USI: For SWT-GTK we need the special empty entry as well. Otherwise we will have problems
				// getting the selection changed event!
				if (hostTTYDeviceCombo.getItemCount() == 1
					&& fcEditableTTYOther.equals(hostTTYDeviceCombo.getItem(0))) {
					hostTTYDeviceCombo.add("", 0); //$NON-NLS-1$
				}
				hostTTYDeviceCombo.setText(hostTTYDeviceCombo.getItem(0));
			}
		}

		if (hostTTYDeviceCombo.getItemCount() > 0) {
			hostTTYDeviceCombo.setEnabled(true);
		} else {
			hostTTYDeviceCombo.setEnabled(false);
		}
		lastSelected = hostTTYDeviceCombo.getSelectionIndex();

		for (String fcTTYSpeedRate : fcTTYSpeedRates) {
			hostTTYSpeedCombo.add(fcTTYSpeedRate);
		}
		hostTTYSpeedCombo.add(fcEditableTTYOther);

		hostTTYSpeedCombo.setText(fcDefaultTTYSpeed);
		lastSelectedBaud = hostTTYSpeedCombo.getSelectionIndex();

		// add the advanced serial options
		hostTTYBitsLabel = new Label(client, SWT.NONE);
		hostTTYBitsLabel.setText(Messages.SerialLinePanel_hostTTYDatabits_label);
		hostTTYBitsCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYBitsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});

		for (String fcTTYDatabit : fcTTYDatabits) {
			hostTTYBitsCombo.add(fcTTYDatabit);
		}
		hostTTYBitsCombo.setText(fcDefaultTTYDatabits);

		hostTTYParityLabel = new Label(client, SWT.NONE);
		hostTTYParityLabel.setText(Messages.SerialLinePanel_hostTTYParity_label);
		hostTTYParityCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYParityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYParityCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});

		for (String element : fcTTYParity) {
			hostTTYParityCombo.add(element);
		}
		hostTTYParityCombo.setText(fcDefaultTTYParity);

		hostTTYStopbitsLabel = new Label(client, SWT.NONE);
		hostTTYStopbitsLabel.setText(Messages.SerialLinePanel_hostTTYStopbits_label);
		hostTTYStopbitsCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYStopbitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYStopbitsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});

		for (String fcTTYStopbit : fcTTYStopbits) {
			hostTTYStopbitsCombo.add(fcTTYStopbit);
		}
		hostTTYStopbitsCombo.setText(fcDefaultTTYStopbits);

		hostTTYFlowControlLabel = new Label(client, SWT.NONE);
		hostTTYFlowControlLabel.setText(Messages.SerialLinePanel_hostTTYFlowControl_label);
		hostTTYFlowControlCombo = new Combo(client, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		hostTTYFlowControlCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYFlowControlCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});

		for (String element : fcTTYFlowControl) {
			hostTTYFlowControlCombo.add(element);
		}
		hostTTYFlowControlCombo.setText(fcDefaultTTYFlowControl);

		hostTTYTimeoutLabel = new Label(client, SWT.NONE);
		hostTTYTimeoutLabel.setText(Messages.SerialLinePanel_hostTTYTimeout_label);
		hostTTYTimeoutText = new Text(client, SWT.SINGLE | SWT.BORDER);
		hostTTYTimeoutText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hostTTYTimeoutText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				IConfigurationPanelContainer container = SerialLinePanel.this.getContainer();
				if (container != null) container.validate();
			}
		});
		hostTTYTimeoutText.setText(fcDefaultTTYTimeout);
	}

	/**
	 * Query the list of serial devices.
	 */
	protected void queryAvailableSerialDevices() {
		// Avoid printing the library version output to stdout if the platform
		// is not in debug mode.
		String prop = System.getProperty("gnu.io.rxtx.NoVersionOutput"); //$NON-NLS-1$
		if (prop == null && !Platform.inDebugMode()) {
			System.setProperty("gnu.io.rxtx.NoVersionOutput", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// java.lang.UnsatisfiedLinkError: ../plugins/gnu.io.rxtx.solaris.sparc_2.1.7.200702281917/os/solaris/sparc/librxtxSerial.so:
		//       Can't load Sparc 32-bit .so on a Sparc 32-bit platform
		// May happen in CommPortIdentifier static constructor!
		try {
            Enumeration<CommPortIdentifier> ttyPortIds = CommPortIdentifier.getPortIdentifiers();
			if (!ttyPortIds.hasMoreElements()) {
				UIPlugin.getTraceHandler().trace("SerialLinePanel: NO comm ports available at all!", ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$
			}
			final List<String> ports = new ArrayList<String>();
			while (ttyPortIds.hasMoreElements()) {
				CommPortIdentifier port = ttyPortIds.nextElement();
				String type = "unknown"; //$NON-NLS-1$
				if (port.getPortType() == CommPortIdentifier.PORT_PARALLEL) {
					type = "parallel"; //$NON-NLS-1$
				}
				if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					type = "serial"; //$NON-NLS-1$
				}
				UIPlugin.getTraceHandler().trace("SerialLinePanel: Found comm port: name='" + port.getName() + "', type='" + type, ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$ //$NON-NLS-2$
				// only add serial ports
				if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					UIPlugin.getTraceHandler().trace("SerialLinePanel: Adding found serial comm port to combo!", ITraceIds.TRACE_SERIAL_LINE_PANEL, this); //$NON-NLS-1$
					if (!ports.contains(port.getName())) {
						ports.add(port.getName());
					}
				}
			}
			if (!ports.isEmpty()) {
				Collections.sort(ports);
				// This method may executed in a separate thread. We must spawn back
				// into the UI thread to execute the adding of the ports to the control.
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					@Override
                    public void run() {
						for (String port : ports) {
							hostTTYDeviceCombo.add(port);
						}
					}
				});
			}
		} catch (UnsatisfiedLinkError e) {
			IStatus status = new Status(IStatus.WARNING, UIPlugin.getUniqueIdentifier(),
										Messages.SerialLinePanel_warning_FailedToLoadSerialPorts, e);
			UIPlugin.getDefault().getLog().log(status);
		} catch (NoClassDefFoundError e) {
			// The NoClassDefFoundError happens the second time if the load of the library
			// failed once! We do ignore this error completely!
		}
	}

	/**
	 * Enables or disables the configuration panels controls.
	 *
	 * @param enabled Specify <code>true</code> to enable the controls, <code>false</code> otherwise.
	 */
	@Override
    public void setEnabled(boolean enabled) {
		hostTTYDeviceLabel.setEnabled(enabled);
		hostTTYDeviceCombo.setEnabled(enabled);
		hostTTYSpeedLabel.setEnabled(enabled);
		hostTTYSpeedCombo.setEnabled(enabled);
		hostTTYBitsLabel.setEnabled(enabled);
		hostTTYBitsCombo.setEnabled(enabled);
		hostTTYParityLabel.setEnabled(enabled);
		hostTTYParityCombo.setEnabled(enabled);
		hostTTYStopbitsLabel.setEnabled(enabled);
		hostTTYStopbitsCombo.setEnabled(enabled);
		hostTTYFlowControlLabel.setEnabled(enabled);
		hostTTYFlowControlCombo.setEnabled(enabled);
	}

	/**
	 * The name of the serial ports differ between the host platforms, so we have to
	 * detect the default host TTY device based on the host platform.
	 */
	public String getDefaultHostTTYDevice() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		// Linux ?
		if (osName.equalsIgnoreCase("Linux")) { //$NON-NLS-1$
			return fcDefaultTTYDeviceLinux;
		}
		// Solaris ?
		if (osName.equalsIgnoreCase("SunOS")) { //$NON-NLS-1$
			return fcDefaultTTYDeviceSolaris;
		}
		// Windows ?
		if (osName.toLowerCase().startsWith("windows")) { //$NON-NLS-1$
			return fcDefaultTTYDeviceWin32;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the default value for the serial port speed setting in bit/s
	 */
	public String getDefaultHostTTYSpeed() {
		return fcDefaultTTYSpeed;
	}

	/**
	 * Returns the default value for the serial port data bits setting
	 */
	public String getDefaultHostTTYDatabits() {
		return fcDefaultTTYDatabits;
	}

	/**
	 * Returns the default value for the serial port parity setting
	 */
	public String getDefaultHostTTYParity() {
		return fcDefaultTTYParity;
	}

	/**
	 * Returns the default value for the serial port stop bits setting
	 */
	public String getDefaultHostTTYStopbits() {
		return fcDefaultTTYStopbits;
	}

	/**
	 * Returns the default value for the serial port flow control setting
	 */
	public String getDefaultHostTTYFlowControl() {
		return fcDefaultTTYFlowControl;
	}

	/**
	 * Returns the default value for the serial port timeout setting.
	 */
	public String getDefaultHostTTYTimeout() {
		return fcDefaultTTYTimeout;
	}

	/**
	 * Set the text to the combo if available as selectable option.
	 *
	 * @param combo The combo box control. Must not be <code>null</code>.
	 * @param value The value to set. Must not be <code>null</code>.
	 */
	protected void doSetTextInCombo(Combo combo, String value) {
		Assert.isNotNull(combo);
		Assert.isNotNull(value);
		if (combo.indexOf(value) != 1) {
			combo.setText(value);
		}
	}

	/**
	 * Select the given tty device if available.
	 *
	 * @param value The tty device to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYDevice(String value) {
		doSetTextInCombo(hostTTYDeviceCombo, value);
	}

	/**
	 * Select the given tty device if available. The method
	 * will do nothing if the specified index is invalid.
	 *
	 * @param index The index of the tty device to select.
	 */
	public void setSelectedTTYDevice(int index) {
		if (index >= 0 && index < hostTTYDeviceCombo.getItemCount()) {
			hostTTYDeviceCombo.setText(hostTTYDeviceCombo.getItem(index));
		}
	}

	/**
	 * Select the given tty device speed if available.
	 *
	 * @param value The tty device speed to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYSpeed(String value) {
		doSetTextInCombo(hostTTYSpeedCombo, value);
	}

	/**
	 * Select the given tty device data bit configuration if available.
	 *
	 * @param value The tty device data bit configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYDatabits(String value) {
		doSetTextInCombo(hostTTYBitsCombo, value);
	}

	/**
	 * Select the given tty device parity configuration if available.
	 *
	 * @param value The tty device parity configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYParity(String value) {
		doSetTextInCombo(hostTTYParityCombo, value);
	}

	/**
	 * Select the given tty device stop bit configuration if available.
	 *
	 * @param value The tty device stop bit configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYStopbits(String value) {
		doSetTextInCombo(hostTTYStopbitsCombo, value);
	}

	/**
	 * Select the given tty device flow control configuration if available.
	 *
	 * @param value The tty device flow control configuration to select. Must not be <code>null</code>.
	 */
	public void setSelectedTTYFlowControl(String value) {
		doSetTextInCombo(hostTTYFlowControlCombo, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
	public boolean isValid() {
		String selectedTTYDevice = hostTTYDeviceCombo.getText();
		if (selectedTTYDevice == null || selectedTTYDevice.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYDevice, IMessageProvider.ERROR);
			return false;
		}

		if (fcEditableTTYOther.equals(selectedTTYDevice)) {
			setMessage(Messages.SerialLinePanel_info_editableTTYDeviceSelected, IMessageProvider.INFORMATION);
			return false;
		}

		String selectedTTYSpeedRate = hostTTYSpeedCombo.getText();
		if (selectedTTYSpeedRate == null || selectedTTYSpeedRate.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYSpeedRate, IMessageProvider.ERROR);
			return false;
		}

		if (fcEditableTTYOther.equals(selectedTTYSpeedRate)) {
			setMessage(Messages.SerialLinePanel_info_editableTTYBaudRateSelected, IMessageProvider.INFORMATION);
			return false;
		}

		String option = hostTTYBitsCombo.getText();
		if (option == null || option.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYDatabits, IMessageProvider.ERROR);
			return false;
		}

		option = hostTTYParityCombo.getText();
		if (option == null || option.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYParity, IMessageProvider.ERROR);
			return false;
		}

		option = hostTTYStopbitsCombo.getText();
		if (option == null || option.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYStopbits, IMessageProvider.ERROR);
			return false;
		}

		option = hostTTYFlowControlCombo.getText();
		if (option == null || option.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYFlowControl, IMessageProvider.ERROR);
			return false;
		}

		option = hostTTYTimeoutText.getText();
		if (option == null || option.trim().length() == 0) {
			setMessage(Messages.SerialLinePanel_error_emptyHostTTYFlowControl, IMessageProvider.ERROR);
			return false;
		}

		return true;
	}

	private final String fcSelectedTTYDeviceSlotId = "SerialLinePanel.selectedTTYDevice." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYSpeedRateSlotId = "SerialLinePanel.selectedTTYSpeedRate." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYDatabitsSlotId = "SerialLinePanel.selectedTTYDatabits." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYParitySlotId = "SerialLinePanel.selectedTTYParity." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYStopbitsSlotId = "SerialLinePanel.selectedTTYStopbits." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYFlowControlSlotId = "SerialLinePanel.selectedTTYFlowControl." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$
	private final String fcSelectedTTYTimeoutSlotId = "SerialLinePanel.selectedTTYTimeout." + System.getProperty("os.name"); //$NON-NLS-1$ //$NON-NLS-2$

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		String selectedTTYDevice = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYDeviceSlotId, idPrefix));
		if (selectedTTYDevice != null && selectedTTYDevice.trim().length() > 0) {
			if (hostTTYDeviceCombo.indexOf(selectedTTYDevice) != -1) {
				hostTTYDeviceCombo.setText(selectedTTYDevice);
			}
		}

		String selectedTTYSpeedRate = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYSpeedRateSlotId, idPrefix));
		if (selectedTTYSpeedRate != null && selectedTTYSpeedRate.trim().length() > 0) {
			if (hostTTYSpeedCombo.indexOf(selectedTTYSpeedRate) != -1) {
				hostTTYSpeedCombo.setText(selectedTTYSpeedRate);
			}
		}

		String option = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYDatabitsSlotId, idPrefix));
		if (option != null && option.trim().length() > 0 && hostTTYBitsCombo.indexOf(option) != -1) {
			hostTTYBitsCombo.setText(option);
		}

		option = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYParitySlotId, idPrefix));
		if (option != null && option.trim().length() > 0 && hostTTYParityCombo.indexOf(option) != -1) {
			hostTTYParityCombo.setText(option);
		}

		option = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYStopbitsSlotId, idPrefix));
		if (option != null && option.trim().length() > 0 && hostTTYStopbitsCombo.indexOf(option) != -1) {
			hostTTYStopbitsCombo.setText(option);
		}

		option = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYFlowControlSlotId, idPrefix));
		if (option != null && option.trim().length() > 0 && hostTTYFlowControlCombo.indexOf(option) != -1) {
			hostTTYFlowControlCombo.setText(option);
		}

		option = settings.get(prefixDialogSettingsSlotId(fcSelectedTTYTimeoutSlotId, idPrefix));
		if (option != null && option.trim().length() > 0 && !option.equals(hostTTYTimeoutText.getText())) {
			hostTTYTimeoutText.setText(option);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYDeviceSlotId, idPrefix), hostTTYDeviceCombo.getText());
		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYSpeedRateSlotId, idPrefix), hostTTYSpeedCombo.getText());

		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYDatabitsSlotId, idPrefix), hostTTYBitsCombo.getText());
		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYParitySlotId, idPrefix), hostTTYParityCombo.getText());
		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYStopbitsSlotId, idPrefix), hostTTYStopbitsCombo.getText());
		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYFlowControlSlotId, idPrefix), hostTTYFlowControlCombo.getText());
		settings.put(prefixDialogSettingsSlotId(fcSelectedTTYTimeoutSlotId, idPrefix), hostTTYTimeoutText.getText());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#setupData(java.util.Map)
	 */
	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null) return;

		hostTTYDeviceCombo.setText((String)data.get(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE));
		hostTTYSpeedCombo.setText((String)data.get(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE));

		hostTTYBitsCombo.setText((String)data.get(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS));
		hostTTYParityCombo.setText((String)data.get(ITerminalsConnectorConstants.PROP_SERIAL_PARITY));
		hostTTYStopbitsCombo.setText((String)data.get(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS));
		hostTTYFlowControlCombo.setText((String)data.get(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL));

		Object value = data.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		if (value instanceof Integer) {
			hostTTYTimeoutText.setText(((Integer)value).toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel#extractData(java.util.Map)
	 */
	@Override
	public void extractData(Map<String, Object> data) {
		if (data == null) return;

			data.put(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE, hostTTYDeviceCombo.getText());
			data.put(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE, hostTTYSpeedCombo.getText());

			data.put(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS, hostTTYBitsCombo.getText());
			data.put(ITerminalsConnectorConstants.PROP_SERIAL_PARITY, hostTTYParityCombo.getText());
			data.put(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS, hostTTYStopbitsCombo.getText());
			data.put(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL, hostTTYFlowControlCombo.getText());

			if (hostTTYTimeoutText.getText() != null) {
				Integer timeout = null;
				try {
					timeout = Integer.decode(hostTTYTimeoutText.getText());
				} catch (NumberFormatException e) { /* ignored on purpose */ }
				if (timeout != null) data.put(ITerminalsConnectorConstants.PROP_TIMEOUT, timeout);
				else data.remove(ITerminalsConnectorConstants.PROP_TIMEOUT);
			}
	}
}
