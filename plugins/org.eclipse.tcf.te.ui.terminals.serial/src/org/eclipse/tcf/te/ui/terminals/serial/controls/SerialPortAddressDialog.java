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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.terminals.serial.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.serial.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Serial line port or address dialog.
 */
public class SerialPortAddressDialog extends TitleAreaDialog implements IMessageProvider {
	private String contextHelpId = null;

	private String message;
	private int messageType;
	private String errorMessage;
	private String title;

	// The default message is shown to the user if no other message is set
	private String defaultMessage;
	private int defaultMessageType;

	Button ttyControlSelector;
	Combo ttyControl;
	Button tcpControlSelector;
	Combo addressControl;
	Combo portControl;
	Label portLabel;

	List<String> ttyHistory;
	List<String> tcpHistory;

	String data = null;

	// regular expressions for validator
	/* default */ static final String IP_CHARACTERS_REGEX = "[0-9][0-9\\.]*"; //$NON-NLS-1$
	/* default */ static final String IP_FRAGMENT_REGEX = "([0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])"; //$NON-NLS-1$
	/* default */ static final String IP_REGEX = IP_FRAGMENT_REGEX + "(\\." + IP_FRAGMENT_REGEX + "){3}[ ]*"; //$NON-NLS-1$ //$NON-NLS-2$

	// RFC 1034 - ftp://ftp.rfc-editor.org/in-notes/std/std13.txt
	/* default */ static final String NAME_CHARACTERS_REGEX = "[a-zA-Z][0-9a-zA-Z\\-_\\.]*"; //$NON-NLS-1$
	// characters that can be set at the beginning
	/* default */ static final String NAME_START_REGEX = "[a-zA-Z]"; //$NON-NLS-1$
	// characters that can be set after the starting character
	/* default */ static final String NAME_FOLLOW_REGEX = "[a-zA-Z0-9-_]"; //$NON-NLS-1$
	// characters that can be set at the end
	/* default */ static final String NAME_END_REGEX = "[a-zA-Z0-9]"; //$NON-NLS-1$
	// single name fragment
	/* default */ static final String NAME_FRAGMENT_REGEX = "(" + NAME_START_REGEX + "(" + NAME_FOLLOW_REGEX + "*" + NAME_END_REGEX + ")?)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/* default */ static final String NAME_REGEX = NAME_FRAGMENT_REGEX + "(\\." + NAME_FRAGMENT_REGEX + ")*[ ]*"; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Constructor.
	 * @param parentShell
	 */
	public SerialPortAddressDialog(Shell parentShell, String selected, List<String> ttyHistory, List<String> tcpHistory) {
		super(parentShell);
		this.ttyHistory = ttyHistory;
		this.tcpHistory = tcpHistory;
		this.data = selected;

		this.contextHelpId = UIPlugin.getUniqueIdentifier() + ".SerialPortAddressDialog"; //$NON-NLS-1$
		setHelpAvailable(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();

		// If the dialog got set a message, make sure the message is really shown
		// to the user from the beginning.
		if (isMessageSet()) {
			if (errorMessage != null) {
				super.setErrorMessage(errorMessage);
			}
			else {
				super.setMessage(message, messageType);
			}
		} else if (defaultMessage != null) {
			// Default message set
			super.setMessage(defaultMessage, defaultMessageType);
		}

		// If the dialog got set a title, make sure the title is shown
		if (title != null) {
			super.setTitle(title);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected final Control createDialogArea(Composite parent) {
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextHelpId);
		}

		// Let the super implementation create the dialog area control
		Control control = super.createDialogArea(parent);
		// Setup the inner panel as scrollable composite
		if (control instanceof Composite) {
			ScrolledComposite sc = new ScrolledComposite((Composite)control, SWT.V_SCROLL);

			GridLayout layout = new GridLayout(1, true);
			layout.marginHeight = 0; layout.marginWidth = 0;
			layout.verticalSpacing = 0; layout.horizontalSpacing = 0;

			sc.setLayout(layout);
			sc.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);

			Composite composite = new Composite(sc, SWT.NONE);
			composite.setLayout(new GridLayout());

			// Setup the dialog area content
			createDialogAreaContent(composite);

			sc.setContent(composite);
			sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			// Return the scrolled composite as new dialog area control
			control = sc;
		}

		return control;
	}

	/**
	 * Creates the dialog area content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
    protected void createDialogAreaContent(Composite parent) {
    	Assert.isNotNull(parent);

		setDialogTitle(Messages.SerialPortAddressDialog_dialogtitle);
		setTitle(Messages.SerialPortAddressDialog_title);

		Composite ttyComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		ttyComp.setLayout(layout);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = 250;
		ttyComp.setLayoutData(layoutData);

		Composite panel = new Composite(ttyComp, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		panel.setLayoutData(layoutData);

		ttyControlSelector = new Button(panel, SWT.RADIO);
		ttyControlSelector.setText(Messages.SerialLinePanel_hostTTYDevice_label);
		layoutData = new GridData(SWT.LEAD, SWT.CENTER, false, false);
		ttyControlSelector.setLayoutData(layoutData);
		ttyControlSelector.setSelection(true);
		ttyControlSelector.addSelectionListener(new SelectionAdapter(){
			@Override
            public void widgetSelected(SelectionEvent e) {
				boolean selected = ttyControlSelector.getSelection();
				setTTYControlEnabled(selected);
				setTCPControlEnabled(!selected);
				onModify();
			}
		});

		ttyControl = new Combo(panel, SWT.DROP_DOWN);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		ttyControl.setLayoutData(layoutData);
		ttyControl.addModifyListener(new ModifyListener(){
			@Override
            public void modifyText(ModifyEvent e) {
				onModify();
			}
		});

		parent.getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				boolean enable = ttyHistory != null && ttyHistory.contains(data);
				setTTYControlEnabled(enable);
				setTCPControlEnabled(!enable);
				onModify();
			}
		});

		Composite tcpComp = new Composite(parent, SWT.NONE);
		layout = new GridLayout(2, true);
		tcpComp.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tcpComp.setLayoutData(layoutData);

		Composite tcpAddrComp = new Composite(tcpComp, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0; layout.marginHeight = 0;
		tcpAddrComp.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tcpAddrComp.setLayoutData(layoutData);

		tcpControlSelector = new Button(tcpAddrComp, SWT.RADIO);
		tcpControlSelector.setText(Messages.SerialPortAddressDialog_address);
		layoutData = new GridData(SWT.LEAD, SWT.CENTER, false, false);
		tcpControlSelector.setLayoutData(layoutData);
		tcpControlSelector.setSelection(false);
		tcpControlSelector.addSelectionListener(new SelectionAdapter(){
			@Override
            public void widgetSelected(SelectionEvent e) {
				boolean selected = tcpControlSelector.getSelection();
				setTTYControlEnabled(!selected);
				setTCPControlEnabled(selected);
				onModify();
			}
		});

		addressControl = new Combo(tcpAddrComp, SWT.DROP_DOWN);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		addressControl.setLayoutData(layoutData);
		addressControl.addModifyListener(new ModifyListener(){
			@Override
            public void modifyText(ModifyEvent e) {
				onModify();
			}
		});

		Composite tcpPortComp = new Composite(tcpComp, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0; layout.marginHeight = 0;
		tcpPortComp.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tcpPortComp.setLayoutData(layoutData);

		portLabel = new Label(tcpPortComp, SWT.HORIZONTAL);
		portLabel.setText(Messages.SerialPortAddressDialog_port);
		layoutData = new GridData(SWT.LEAD, SWT.CENTER, false, false);
		portLabel.setLayoutData(layoutData);

		portControl = new Combo(tcpPortComp, SWT.DROP_DOWN);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		portControl.setLayoutData(layoutData);
		portControl.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				StringBuilder buffer = new StringBuilder(((Combo)e.widget).getText());

				try {
					if (e.end > e.start) {
						buffer.replace(e.start, e.end, e.text);
					} else if (e.end >= 0) {
						buffer.insert(e.end, e.text);
					}
				} catch (StringIndexOutOfBoundsException exc) { /* ignored on purpose */ }

				String fulltext = buffer.toString();
				e.doit = fulltext.matches("([0-9]{0,5})|(0((x|X)[0-9a-fA-F]{0,4})?)"); //$NON-NLS-1$

				if (e.doit && fulltext.length() > 0 && !fulltext.equalsIgnoreCase("0x")) { //$NON-NLS-1$
					try {
						int value = Integer.decode(fulltext).intValue();
						if (value < 0 || value > 65535) {
							e.doit = false;
						}
					}
					catch (Exception ex) {
						e.doit = false;
					}
				}
			}
		});
		portControl.addModifyListener(new ModifyListener(){
			@Override
            public void modifyText(ModifyEvent e) {
				onModify();
			}
		});

		// Trigger the runnable after having created all controls!
		parent.getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				boolean enable = tcpHistory != null && tcpHistory.contains(data);
				setTTYControlEnabled(!enable);
				setTCPControlEnabled(enable);
				onModify();
			}
		});

		applyDialogFont(ttyComp);
		applyDialogFont(tcpComp);

		setupData();
	}

	private void setupData() {
		setTTYControlEnabled(true);
		setTCPControlEnabled(false);
		if (ttyHistory != null && !ttyHistory.isEmpty()) {
			for (String tty : ttyHistory) {
				if (tty != null && tty.trim().length() > 0 && ttyControl.indexOf(tty) == -1) {
					ttyControl.add(tty.trim());
				}
				if (tty != null && tty.equals(data)) {
					ttyControl.setText(tty.trim());
				}
			}
		}
		if (tcpHistory != null && !tcpHistory.isEmpty()) {
			for (String tcp : tcpHistory) {
				String[] data = tcp.split(":"); //$NON-NLS-1$
				if (data.length > 1) {
					if (data[1] != null && data[1].trim().length() > 0 && ttyControl.indexOf(data[1]) == -1) {
						addressControl.add(data[1].trim());
					}
				}
				if (data.length > 2) {
					if (data[2] != null && data[2].trim().length() > 0 && ttyControl.indexOf(data[2]) == -1) {
						addressControl.add(data[2].trim());
					}
				}
				if (tcp.equals(this.data)) {
					setTTYControlEnabled(false);
					setTCPControlEnabled(true);
					if (data.length > 1) {
						addressControl.setText(data[1]);
					}
					if (data.length > 2) {
						portControl.setText(data[2]);
					}
				}
			}
		}
		onModify();
	}

	void setTTYControlEnabled(boolean enable) {
		ttyControlSelector.setSelection(enable);
		ttyControl.setEnabled(enable);
	}

	void setTCPControlEnabled(boolean enable) {
		tcpControlSelector.setSelection(enable);
		addressControl.setEnabled(enable);
		portControl.setEnabled(enable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		setButtonEnabled(OK, false);
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.dialogs.CustomTitleAreaDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (ttyControlSelector.getSelection()) {
			data = ttyControl.getText();
		}
		else {
			data = "tcp:" + addressControl.getText() + ":" + portControl.getText(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		super.okPressed();
	}

	/**
	 * Called from the single controls if the content of the controls changed.
	 */
	protected void onModify() {
		setMessage(null);

		boolean valid = false;

		if (ttyControlSelector.getSelection()) {
			valid = isTtyControlValid();
		} else {
			valid = isAddressControlValid();
			valid &= isPortControlValid();
		}

		if (getMessage() == null) {
			setDefaultMessage(Messages.SerialPortAddressDialog_message, IMessageProvider.INFORMATION);
		}

		setButtonEnabled(OK, valid);
	}

	private static final Pattern validCharacters = Platform.OS_WIN32.equals(Platform.getOS()) ? Pattern.compile("[\\w]+") : Pattern.compile("[\\w/]+"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Validates the tty device control.
	 *
	 * @return <code>True</code> if the control is valid, <code>false</code> otherwise.
	 */
	protected boolean isTtyControlValid() {
		if (ttyControl == null || ttyControl.isDisposed()) return false;

		boolean valid = true;

		String m = null;
		int mt = IMessageProvider.NONE;

		String newText = ttyControl.getText();
		Assert.isNotNull(newText);
		if (newText.trim().length() > 0) {
			Matcher matcher = validCharacters.matcher(newText);
			if (!matcher.matches()) {
				m = Messages.SerialLinePanel_error_invalidCharactes;
				mt = IMessageProvider.ERROR;
			}
		}
		else {
			m = Messages.SerialLinePanel_error_emptyHostTTYDevice;
			mt = IMessageProvider.INFORMATION;
		}

		valid = mt != IMessageProvider.ERROR;
		if (mt > getMessageType()) setMessage(m, mt);

		return valid;
	}

	/**
	 * Validates the address control.
	 *
	 * @return <code>True</code> if the control is valid, <code>false</code> otherwise.
	 */
	protected boolean isAddressControlValid() {
		if (addressControl == null || addressControl.isDisposed()) return false;

		boolean valid = true;

		String m = null;
		int mt = IMessageProvider.NONE;

		String ipOrHostName = addressControl.getText();

		// info message when value is empty
		if (ipOrHostName == null || ipOrHostName.trim().length() == 0) {
			m = Messages.SerialPortAddressDialog_Information_MissingTargetNameAddress;
			mt = IMessageProvider.INFORMATION;
		} else {
			ipOrHostName = ipOrHostName.trim();
			// check IP address when only numeric values and '.' are entered
			if (ipOrHostName.matches(IP_CHARACTERS_REGEX)) {
				if (!ipOrHostName.matches(IP_REGEX)) {
					m = Messages.SerialPortAddressDialog_Error_InvalidTargetIpAddress;
					mt = IMessageProvider.ERROR;
				}
			}
			else if (ipOrHostName.matches(NAME_CHARACTERS_REGEX)) {
				if (!ipOrHostName.matches(NAME_REGEX)) {
					m = Messages.SerialPortAddressDialog_Error_InvalidTargetNameAddress;
					mt = IMessageProvider.ERROR;
				}
			}
			else {
				m = Messages.SerialPortAddressDialog_Error_InvalidTargetNameAddress;
				mt = IMessageProvider.ERROR;
			}
		}

		valid = mt != IMessageProvider.ERROR;
		if (mt > getMessageType()) setMessage(m, mt);

		return valid;
	}

	/**
	 * Validates the port control.
	 *
	 * @return <code>True</code> if the control is valid, <code>false</code> otherwise.
	 */
	protected boolean isPortControlValid() {
		if (portControl == null || portControl.isDisposed()) return false;

		boolean valid = true;

		String m = null;
		int mt = IMessageProvider.NONE;

		String newText = portControl.getText();
		Assert.isNotNull(newText);
		if (newText.trim().length() > 0) {
			if (!newText.matches("([0-9]{0,5})|(0((x|X)[0-9a-fA-F]{0,4})?)")) { //$NON-NLS-1$
				m = Messages.SerialPortAddressDialog_Error_InvalidPort;
				mt = IMessageProvider.ERROR;
			} else {
				try {
					int value = Integer.decode(newText).intValue();
					if (value < 0 || value > 65535) {
						m = Messages.SerialPortAddressDialog_Error_InvalidPortRange;
						mt = IMessageProvider.ERROR;
					}
				}
				catch (Exception ex) { /* ignored on purpose */ }
			}
		}
		else {
			m = Messages.SerialPortAddressDialog_Information_MissingPort;
			mt = IMessageProvider.INFORMATION;
		}

		valid = mt != IMessageProvider.ERROR;
		if (mt > getMessageType()) setMessage(m, mt);

		return valid;
	}

	/**
	 * Return the new name after OK was pressed.
	 * Unless OK was pressed, the old name is returned.
	 */
	public String getData() {
		return data;
	}

	/**
	 * Cleanup when dialog is closed.
	 */
	protected void dispose() {
		message = null;
		messageType = IMessageProvider.NONE;
		errorMessage = null;
		title = null;
		defaultMessage = null;
		defaultMessageType = IMessageProvider.NONE;
	}

	/**
	 * Cleanup the Dialog and close it.
	 */
	@Override
	public boolean close() {
		dispose();
		return super.close();
	}

	/**
	 * Set the enabled state of the dialog button specified by the given id (@see <code>IDialogConstants</code>)
	 * to the given state.
	 *
	 * @param buttonId The button id for the button to change the enabled state for.
	 * @param enabled The new enabled state to set for the button.
	 */
	public void setButtonEnabled(int buttonId, boolean enabled) {
		Button button = getButton(buttonId);
		if (button != null) {
			button.setEnabled(enabled);
		}
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title The title.
	 */
	public void setDialogTitle(String title) {
		if (getShell() != null && !getShell().isDisposed()) {
			getShell().setText(title);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String newTitle) {
		title = newTitle;
		super.setTitle(newTitle);
	}

	/**
	 * Set the default message. The default message is shown within the
	 * dialogs message area if no other message is set.
	 *
	 * @param message The default message or <code>null</code>.
	 * @param type The default message type. See {@link IMessageProvider}.
	 */
	public void setDefaultMessage(String message, int type) {
		defaultMessage = message;
		defaultMessageType = type;
		// Push the default message to the dialog if no other message is set
		if (!isMessageSet() && getContents() != null) {
			super.setMessage(defaultMessage, defaultMessageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setMessage(java.lang.String, int)
	 */
	@Override
	public void setMessage(String newMessage, int newType) {
		// To be able to implement IMessageProvider, we have to remember the
		// set message ourselfs. There is no access to these information by the
		// base class.
		message = newMessage; messageType = newType;
		// Only pass on to super implementation if the control has been created yet
		if (getContents() != null) {
			super.setMessage(message != null ? message : defaultMessage, message != null ? messageType : defaultMessageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setErrorMessage(java.lang.String)
	 */
	@Override
	public void setErrorMessage(String newErrorMessage) {
		// See setMessage(...)
		errorMessage = newErrorMessage;
		super.setErrorMessage(newErrorMessage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	@Override
	public String getMessage() {
		return errorMessage != null ? errorMessage : message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	@Override
	public int getMessageType() {
		return errorMessage != null ? IMessageProvider.ERROR : messageType;
	}

	/**
	 * Returns if or if not an message is set to the dialog.
	 *
	 * @return <code>True</code> if a message has been set, <code>false</code> otherwise.
	 */
	public boolean isMessageSet() {
		return errorMessage != null || message != null;
	}
}
