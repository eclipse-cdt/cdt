/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 * IBM Corporation - ongoing maintenance
 * Martin Oberhuber (Wind River) - copied and adapted from team.cvs.ui
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * A dialog for keyboad-interactive authentication for the ssh2 connection.
 */
public class KeyboardInteractiveDialog extends TrayDialog {
  // widgets
  private Text[] texts;

  protected String domain;
  protected String destination;
  protected String name;
  protected String instruction;
  protected String lang;
  protected String[] prompt;
  protected boolean[] echo;
  private String message;
  private String[] result;

  /**
   * Creates a nwe KeyboardInteractiveDialog.
   *
   * @param parentShell the parent shell
   * @param connectionId an id for the connection
   * @param destination the location
   * @param name the name
   * @param instruction the instruction
   * @param prompt the titles for textfields
   * @param echo '*' should be used or not
   */
  public KeyboardInteractiveDialog(Shell parentShell,
				   String connectionId,
				   String destination,
				   String name,
				   String instruction,
				   String[] prompt,
				   boolean[] echo){
    super(parentShell);
    this.domain=connectionId;
    this.destination=destination;
    this.name=name;
    this.instruction=instruction;
    this.prompt=prompt;
    this.echo=echo;
    this.message=NLS.bind(SshMessages.KeyboardInteractiveDialog_message, new String[] { destination+(name!=null && name.length()>0 ? ": "+name : "") }); //NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
  }
  /**
   * @see Window#configureShell
   */
  @Override
protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(message);
  }
  /**
   * @see Window#create
   */
  @Override
public void create() {
    super.create();
    if(texts.length>0){
      texts[0].setFocus();
    }
  }
  /**
   * @see Dialog#createDialogArea
   */
  @Override
protected Control createDialogArea(Composite parent) {
    Composite main=new Composite(parent, SWT.NONE);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    main.setLayout(layout);
    main.setLayoutData(new GridData(GridData.FILL_BOTH));

	// set F1 help
    PlatformUI.getWorkbench().getHelpSystem().setHelp(main, ISshConstants.HELP_KEYBOARD_INTERACTIVE_DIALOG);

    if (message!=null) {
      Label messageLabel=new Label(main, SWT.WRAP);
      messageLabel.setText(message);
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan=3;
      messageLabel.setLayoutData(data);
    }
    if(domain!=null){
      Label label = new Label(main, SWT.WRAP);
      label.setText(NLS.bind(SshMessages.KeyboardInteractiveDialog_labelConnection, new String[] { domain }));
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan=3;
      label.setLayoutData(data);
    }
    if (instruction!=null && instruction.length()>0) {
      Label messageLabel=new Label(main, SWT.WRAP);
      messageLabel.setText(instruction);
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan=3;
      messageLabel.setLayoutData(data);
    }
    createPasswordFields(main);
    return main;
  }
  /**
   * Creates the widgets that represent the entry area.
   *
   * @param parent  the parent of the widgets
   */
  protected void createPasswordFields(Composite parent) {
    texts=new Text[prompt.length];

    for(int i=0; i<prompt.length; i++){
      new Label(parent, SWT.NONE).setText(prompt[i]);
      texts[i]=new Text(parent, SWT.BORDER);
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.widthHint=convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
      texts[i].setLayoutData(data);

      if(!echo[i]){
	texts[i].setEchoChar('*');
      }
      new Label(parent, SWT.NONE);
    }

  }
  /**
   * Returns the entered values, or null
   * if the user cancelled.
   *
   * @return the entered values
   */
  public String[] getResult() {
    return result;
  }
  /**
   * Notifies that the ok button of this dialog has been pressed.
   * <p>
   * The default implementation of this framework method sets
   * this dialog's return code to <code>Window.OK</code>
   * and closes the dialog. Subclasses may override.
   * </p>
   */
  @Override
protected void okPressed() {
    result=new String[prompt.length];
    for(int i=0; i<texts.length; i++){
      result[i]=texts[i].getText();
    }
    super.okPressed();
  }
  /**
   * Notifies that the cancel button of this dialog has been pressed.
   * <p>
   * The default implementation of this framework method sets
   * this dialog's return code to <code>Window.CANCEL</code>
   * and closes the dialog. Subclasses may override.
   * </p>
   */
  @Override
protected void cancelPressed() {
    result=null;
    super.cancelPressed();
  }
}
