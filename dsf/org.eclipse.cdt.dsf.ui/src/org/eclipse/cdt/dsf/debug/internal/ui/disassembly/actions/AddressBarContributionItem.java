/*******************************************************************************
 * Copyright (c) 2009 Texas Instruments.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments - Initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug fix (329682)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class AddressBarContributionItem extends ContributionItem {
	private Combo addressBox;
	private IAction action;
	private ToolItem item;
	private int width;
	private String initialText;
	private String lastText;
	private Image warningImage = null;
	private Label warningLabel = null;
	private String warningText = null;

	/**
	 * Use this constructor to create an AddressBarContributionItem.
	 * 
	 * @param action
	 *            a contribution action.
	 */
	public AddressBarContributionItem(IAction action) {
		this.action = action;
	}

	/**
	 * After constructing this object, call this method to create an address
	 * box.
	 * 
	 * @param parent
	 *            a ToolBar object. Can be obtain with the getControl() method
	 *            in the ToolBarManager class.
	 * @param width
	 *            the width of the combo box.
	 * @param initialText
	 *            the initial text displayed in the combo box (e.g. 'Enter
	 *            address here')
	 * @param warningText
	 *            the tooltip of the warning label if it ever becomes visible.
	 *            May be null.
	 */
	public void createAddressBox(ToolBar parent, int width, String initialText,
			String warningText) {
		this.width = width;
		this.initialText = initialText;
		this.lastText = initialText;
		this.warningText = warningText;
		fill(parent, 0);

		parent.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (warningImage != null)
					warningImage.dispose();
			}
		});
	}

	/**
	 * Don't call this method from the client, use the createAddressBox method.
	 */
	@Override
	public void fill(ToolBar parent, int index) {
		item = new ToolItem(parent, SWT.SEPARATOR);
		Control box = internalCreateAddressBox(parent);
		item.setControl(box);
		item.setWidth(width);
		
		enableAddressBox(action.isEnabled());		
	}

	/**
	 * Set the address bar text
	 */
	public void setText(String text) {
		if (addressBox != null)
			addressBox.setText(text);
	}

	/**
	 * Get the address bar text
	 * 
	 * @return The text in the address bar.
	 */
	public String getText() {
		if (addressBox != null)
			return addressBox.getText();
		else
			return initialText;
	}

	/**
	 * Set the visibility of the warning icon. Should be set to true when there
	 * is a problem jumping to the specified address; false otherwise
	 * 
	 * @param visible
	 *            True for visible, false for hidden.
	 */
	public void setWarningIconVisible(boolean visible) {
		if (warningLabel == null)
			return;
		warningLabel.setVisible(visible);
	}

	/**
	 * Return whether the warning icon is visible or not.
	 * 
	 * @return True if visible, otherwise false.
	 */
	public boolean isWarningIconVisible() {
		if (warningLabel == null)
			return false;
		return warningLabel.isVisible();
	}

	/**
	 * Enable the address combo box.
	 * 
	 * @param enable
	 *            true to enable, else false.
	 */
	public void enableAddressBox(boolean enable) {
		if (addressBox != null) {
			item.setEnabled(enable);
			addressBox.setEnabled(enable);
		}
	}

	/**
	 * Creates the combo box and add it to the toolbar.
	 * 
	 * @param parent
	 *            the parent, toolbar.
	 * @return the combo box address control.
	 */
	private Control internalCreateAddressBox(Composite parent) {
		Composite top = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = layout.horizontalSpacing = 2;
		layout.numColumns = 3;
		top.setLayout(layout);

		warningLabel = new Label(top, SWT.NONE);

		warningImage = AbstractUIPlugin
				.imageDescriptorFromPlugin(
						DsfUIPlugin.PLUGIN_ID, "icons/address_warning.gif").createImage(); //$NON-NLS-1$
		warningLabel.setImage(warningImage);
		warningLabel.setToolTipText(warningText);
		setWarningIconVisible(false);

		addressBox = new Combo(top, SWT.DROP_DOWN);

		addressBox.setText(initialText);
		action.setText(initialText);

		addressBox.addFocusListener(new FocusListener() {
			// [nmehregani]: Support Ctrl+C in address bar
			KeyListener keyListener = new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					/* Not used */
				}

				@Override
				public void keyReleased(KeyEvent e) {
					if (e.stateMask == SWT.CTRL
							&& (((char) e.keyCode) == 'c' || ((char) e.keyCode) == 'C')) {
						String selection = null;

						Point selectionPoint = addressBox.getSelection();
						if (selectionPoint.x == selectionPoint.y)
							return;

						selection = addressBox.getText().substring(
								selectionPoint.x, selectionPoint.y);

						if ((selection != null)
								&& (!(selection.trim().length() == 0))) {
							Clipboard clipboard = null;
							try {
								clipboard = new Clipboard(addressBox
										.getDisplay());
								clipboard.setContents(
										new Object[] { selection },
										new Transfer[] { TextTransfer
												.getInstance() });
							} finally {
								if (clipboard != null)
									clipboard.dispose();
							}
						}
					}
				}

			};

			@Override
			public void focusGained(FocusEvent e) {
				// [nmehregani] bugzilla 297387: 'Home' shouldn't jump to PC address when focus is on location combo box
				if (action instanceof JumpToAddressAction) 
					((JumpToAddressAction)action).deactivateDisassemblyContext();				
				// end 297387
				
				lastText = addressBox.getText();
				
				// Erase the guide text when the focus is gained.
				if (lastText.trim().equals(initialText))
					addressBox.setText(""); //$NON-NLS-1$

				// [nmehregani]: Support Ctrl+C in address bar
				addressBox.addKeyListener(keyListener);
			}

			@Override
			public void focusLost(FocusEvent e) {
				// [nmehregani] bugzilla 297387: 'Home' shouldn't jump to PC address when focus is on location combo box
				if (action instanceof JumpToAddressAction) 
					((JumpToAddressAction)action).activateDisassemblyContext();				
				// end 297387
				
				// Re-insert the last text when the focus is lost and the text
				// field is empty.
				if (addressBox.getText().trim().length() == 0)
					addressBox.setText(lastText);

				// [nmehregani]: Support Ctrl+C in address bar
				addressBox.removeKeyListener(keyListener);
			}
		});

		addressBox.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					String addressBoxStr = addressBox.getText();

					// don't accept the initial text
					if (addressBoxStr.equals(initialText))
						return;

					Event event = new Event();
					event.data = addressBoxStr;
					action.runWithEvent(event);

					boolean bExist = false;
					for (int i = 0; i < addressBox.getItemCount(); ++i) {
						String itemText = addressBox.getItem(i);
						if (itemText.equals(addressBoxStr)) {
							bExist = true;
							break;
						}
					}

					if ((!bExist) && (addressBox.getText() != null)
							&& (!(addressBox.getText().trim().length() == 0)))
						addressBox.add(addressBox.getText());
				}
			}

		});

		addressBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				int selection = addressBox.getSelectionIndex();
				if (selection >= 0) {
					String addressBoxStr = addressBox.getItem(selection);
					Event event = new Event();
					event.data = addressBoxStr;
					action.runWithEvent(event);
				}
			}
		});

		addressBox.setLayoutData(new GridData(GridData.FILL,
				GridData.BEGINNING, true, false));
		return top;
	}

	// [nmehregani]: Support Ctrl+C in address bar
	public void clearSelection() {
		addressBox.clearSelection();
	}
}
