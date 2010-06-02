/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

//import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractPropertyDialog extends Dialog {
	private final static String WSP_BEG = "${workspace_loc:";  //$NON-NLS-1$
	private final static String WSP_END = "}"; //$NON-NLS-1$
	protected final static String EMPTY_STR = ""; //$NON-NLS-1$
	
	protected Shell shell;
	public boolean result = false;
	
	public Object data = null;
	public boolean check1 = false;
	public boolean check2 = false;
	public boolean check3 = false;
	public String text1;
	public String text2;
	private Shell parent;
	
	public AbstractPropertyDialog(Shell _parent, String title) {
		super(_parent, 0);
		this.getStyle();
		
		parent = _parent;
		this.setText(title);
	}

	abstract public void buttonPressed(SelectionEvent e);
	abstract protected Control createDialogArea(Composite c);
	
	protected Button setupButton(Composite c, String text) {
		Button b = new Button(c, SWT.PUSH);
		b.setText(text);
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});
		return b;
	}
	
	public boolean open () {
	 		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
	 		shell.setText(getText());
	 		createDialogArea(shell);
	 		
	 		// center window
	 		Rectangle r1 = parent.getBounds();
	 		Rectangle r2 = shell.getBounds();
	 		int x = r1.x + (r1.width - r2.width) / 2;
	 		int y = r1.y + (r1.height - r2.height) / 2;
	 		shell.setBounds(x, y, r2.width, r2.height);
	 		
		 	shell.open();
		 	Display display = parent.getDisplay();
		 	while (!shell.isDisposed()) {
		 		if (!display.readAndDispatch()) display.sleep();
		 	}
			return result;
	}
	protected static String strip_wsp(String s) {
		s = s.trim();
		if (s.startsWith(WSP_BEG) && s.endsWith(WSP_END)) {
			int x = s.length() - WSP_END.length(); 
			s = s.substring(WSP_BEG.length(), x);
		}
		return s;
	}
}
