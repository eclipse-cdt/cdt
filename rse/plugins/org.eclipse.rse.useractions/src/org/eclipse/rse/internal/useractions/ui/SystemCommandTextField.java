/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Kevin Doyle	(IBM) - [239704] No Validation for Command textbox in Work with Compile and User Action dialogs
 * Kevin Doyle	(IBM) - [242041] Bring back Undo/Content Assist for User Actions/Compile Commands Command Field
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui;

import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.internal.useractions.ui.validators.ValidatorUserActionCommand;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.view.ISystemCommandTextModifyListener;
import org.eclipse.rse.shells.ui.view.SystemCommandEditor;
import org.eclipse.rse.ui.ISystemMassager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * This class encapsulates a prompt for a remote command that supports
 *  substitution variables. It is used in both compile and user action dialogs.
 */
public class SystemCommandTextField implements SelectionListener {
	protected SystemCommandEditor textCommand;
	protected ISystemMassager cmdMassager;
	protected Button insertVariableButton;
	protected Button editButton;
	protected boolean menuListenerAdded;
	protected SystemCommandViewerConfiguration sourceViewerConfiguration;
	//mri
	private String cmdFieldLabel = SystemUDAResources.RESID_UDA_COMMAND_LABEL;
	private String cmdFieldTooltip = SystemUDAResources.RESID_UDA_COMMAND_TOOLTIP;
	private String insertVarButtonLabel = SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_LABEL;
	private String insertVarButtonTooltip = SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_TOOLTIP;
	private String editButtonLabel = SystemUDAResources.RESID_UDA_EDIT_BUTTON_LABEL;
	private String editButtonTooltip = SystemUDAResources.RESID_UDA_EDIT_BUTTON_TOOLTIP;
	/**
	 * Maximum text length for command field: 512
	 */
	public static final int MAX_CMD_LENGTH = 512;

	/**
	 * Constructor .
	 * You must call setSubstitutionVariableList before 
	 *  calling createContents!
	 */
	public SystemCommandTextField(SystemCommandViewerConfiguration cmdAssistant) {
		super();
		this.sourceViewerConfiguration = cmdAssistant;
	}

	/**
	 * Reset what will be used to manage the content assist. A default is supplied.
	 */
	public void setCommandTextViewerConfiguration(SystemCommandViewerConfiguration cmdAssistant) {
		// defect 46404...
		if ((sourceViewerConfiguration != null) && (textCommand != null) && (sourceViewerConfiguration.getContentAssistant(textCommand) != null))
			sourceViewerConfiguration.getContentAssistant(textCommand).uninstall();
		this.sourceViewerConfiguration = cmdAssistant;
		if (textCommand != null) {
			textCommand.configure(sourceViewerConfiguration);
		}
	}

	/**
	 * Set the substitution variable list that Insert Variable will use.
	 */
	public void setSubstitutionVariableList(SystemCmdSubstVarList varList) {
		sourceViewerConfiguration.setSubstVarList(varList);
	}

	/**
	 * Set the action command massager. This is called to massage the contents
	 *  when getCommandText is called.
	 */
	public void setCommandMassager(ISystemMassager massager) {
		this.cmdMassager = massager;
	}

	/**
	 * Return the command massager as set by setCommandMassager(...)
	 */
	public ISystemMassager getCommandMassager() {
		return cmdMassager;
	}

	/**
	 * Return the edit widget. Will be null until createEditor is called
	 */
	public SourceViewer getEditor() {
		return textCommand;
	}

	/**
	 * Return the control widget for the command prompt
	 */
	public Control getCommandWidget() {
		return textCommand.getControl();
	}

	/**
	 * Return the text contents of the command widget
	 */
	public String getCommandText() {
		return textCommand.getCommandText();
	}

	/**
	 * Return the text contents of the command widget, after applying the massager.
	 * If the massager is null, this is the same as calling getCommandText().
	 */
	public String getMassagedCommandText() {
		if (cmdMassager == null)
			return getCommandText();
		else
			return cmdMassager.massage(getCommandText());
	}

	/**
	 * Set the text contents of the command widget
	 */
	public void setCommandText(String text) {
		textCommand.getDocument().set(text);
	}

	/**
	 * Enable/disable command widget
	 */
	public void enableCommandWidget(boolean enable) {
		if (textCommand != null) textCommand.getTextWidget().setEnabled(enable);
		if (insertVariableButton != null) insertVariableButton.setEnabled(enable);
		if (editButton != null) editButton.setEnabled(enable);
	}

	/**
	 * Turn on or off event ignoring flag
	 */
	public void setIgnoreChanges(boolean ignore) {
		if (textCommand != null) {
			textCommand.setIgnoreChanges(ignore);
		}
	}

	/**
	 * Method createContents.
	 * @param comp - the parent composite into which to place the prompt, field and insert-variable buttons
	 * @return Control
	 */
	public Control createContents(Composite comp, int nbrColumns, ISystemCommandTextAdditionalGUIProvider guiProvider) {
		Label labelCommand = SystemWidgetHelpers.createLabel(comp, cmdFieldLabel, cmdFieldTooltip);
		String s = SystemWidgetHelpers.appendColon(labelCommand.getText());
		labelCommand.setText(s);
		if ((guiProvider == null) || !guiProvider.createCommandLabelLineControls(comp, nbrColumns - 1)) ((GridData) labelCommand.getLayoutData()).horizontalSpan = nbrColumns;
		int cmdSpan = nbrColumns;
		textCommand = createEditor(comp, cmdSpan, sourceViewerConfiguration);
		textCommand.getControl().setToolTipText(cmdFieldTooltip);
		// Insert Variable... button
		insertVariableButton = SystemWidgetHelpers.createPushButton(comp, null, insertVarButtonLabel, insertVarButtonTooltip);
		// edit command button        
		editButton = SystemWidgetHelpers.createPushButton(comp, null, editButtonLabel, editButtonTooltip);
		// SUBCLASS-SUPPLIED BUTTONS
		if ((guiProvider == null) || !guiProvider.createExtraButtons(comp, nbrColumns - 1)) addFillerLine(comp, nbrColumns - 1);
		insertVariableButton.addSelectionListener(this);
		editButton.addSelectionListener(this);
		textCommand.getTextWidget().addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
	
				if (!e.doit)
					return;
					
				if (e.stateMask == SWT.CTRL)
				{
					switch (e.character)
					{
						case ' ' :
							textCommand.setInCodeAssist(true);
							textCommand.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
							break;
							// CTRL-Z
						case 'z' - 'a' + 1 :
							textCommand.doOperation(ITextOperationTarget.UNDO);
							//e.doit = false;
							break;
					}
				}

			}
		});
		return comp;
	}

	/**
	 * Set the information needed to get the command field's mri
	 */
	public void setMRI(String cmdFieldLabel, String cmdFieldTooltip, String insertVarButtonLabel, String insertVarButtonTooltip) {
		this.cmdFieldLabel = cmdFieldLabel;
		this.cmdFieldTooltip = cmdFieldTooltip;
		this.insertVarButtonLabel = insertVarButtonLabel;
		this.insertVarButtonTooltip = insertVarButtonTooltip;
	}

	/**
	 * SelectionListener Interface:
	 * For the checkboxes
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * SelectionListener Interface:
	 * For the checkboxes
	 */
	public void widgetSelected(SelectionEvent e) {
		Object source = e.getSource();
		if (source == insertVariableButton) {
			//sourceViewerConfiguration.getSubstVarList().printDisplayStrings();
			textCommand.getTextWidget().setFocus();
			textCommand.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		} else if (source == editButton) {
			// bring up dialog
			SystemCommandViewerConfiguration cfg = new SystemCommandViewerConfiguration();
			cfg.setSubstVarList(sourceViewerConfiguration.getSubstVarList());
			SystemEditCommandDialog dlg = new SystemEditCommandDialog(getCommandWidget().getShell(), getCommandText(), cfg, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			if (dlg.open() == Window.OK) {
				String str = dlg.getCommand();
				textCommand.getDocument().set(str);
			}
		}
	}

	/**
	 * Create the editor widget
	 */
	private SystemCommandEditor createEditor(Composite parent, int nbrColumns, SystemCommandViewerConfiguration sourceViewerConfiguration) {
		textCommand = new SystemCommandEditor(null, parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, nbrColumns, sourceViewerConfiguration,
				"", SystemUDAResources.RESID_UDA_INSERTVAR_BUTTON_LABEL); //$NON-NLS-1$
		textCommand.setCommandValidator(new ValidatorUserActionCommand());
		return textCommand;
	}

	/**
	 * Set the command validator to validate contents per keystroke
	 */
	public void setCommandValidator(ISystemValidator cmdValidator) {
		if (textCommand != null) textCommand.setCommandValidator(cmdValidator);
	}

	/**
	 * Validate command input
	 */
	public SystemMessage validateCommand() {
		if (textCommand != null)
			return textCommand.validateCommand();
		else
			return null;
	}

	/**
	 * Add a modify listener
	 */
	public void addModifyListener(ISystemCommandTextModifyListener listener) {
		if (textCommand != null) textCommand.addModifyListener(listener);
	}

	/**
	 * Remove a modify listener
	 */
	public void removeModifyListener(ISystemCommandTextModifyListener listener) {
		if (textCommand != null) textCommand.removeModifyListener(listener);
	}

	// -----------------------------
	// Helper methods...
	// -----------------------------
	/**
	 * Add a separator line. This is a physically visible line.
	 */
	protected Label addSeparatorLine(Composite parent, int nbrColumns) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		separator.setLayoutData(data);
		return separator;
	}

	/**
	 * Add a spacer line
	 */
	protected Label addFillerLine(Composite parent, int nbrColumns) {
		Label filler = new Label(parent, SWT.LEFT);
		GridData data = new GridData();
		data.horizontalSpan = nbrColumns;
		data.horizontalAlignment = GridData.FILL;
		filler.setLayoutData(data);
		return filler;
	}
}
