/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * <code>TableTextCellEditor</code> is a copy of TextCellEditor, with the
 * following changes:
 *
 * <ul>
 * <li> modify events are sent out as the text is changed, and not only after
 * editing is done </li>
 *
 * <li>a content assistant is supported</li>
 *
 * <li>the user can go to the next/previous row with up and down keys</li>
 * </ul>
 */
public class TableTextCellEditor extends CellEditor {
	public interface IActivationListener {
		public void activate();
	}

	private final TableViewer fTableViewer;
	private final int fColumn;
	private final String fProperty;
	/**
	 * The editor's value on activation. This value is reset to the
	 * cell when the editor is left via ESC key.
	 */
	String fOriginalValue;
	SubjectControlContentAssistant fContentAssistant;
	private IActivationListener fActivationListener;

    protected Text text;

    private boolean isSelection;
    private boolean isDeleteable;
    private boolean isSelectable;

    private static final int defaultStyle = SWT.SINGLE;
	private ModifyListener fModifyListener;

	public TableTextCellEditor(TableViewer tableViewer, int column) {
		super(tableViewer.getTable(), defaultStyle);
		fTableViewer= tableViewer;
		fColumn= column;
		fProperty= (String) tableViewer.getColumnProperties()[column];
	}

	@Override
	public void activate() {
		super.activate();
		if (fActivationListener != null)
			fActivationListener.activate();
		fOriginalValue= text.getText();
	}

	private void fireModifyEvent(Object newValue) {
		fTableViewer.getCellModifier().modify(
				((IStructuredSelection) fTableViewer.getSelection()).getFirstElement(),
				fProperty, newValue);
	}

	@Override
	protected void focusLost() {
		if (fContentAssistant != null && fContentAssistant.hasProposalPopupFocus()) {
			// skip focus lost if it went to the content assist popup
		} else {
			super.focusLost();
		}
	}

	public void setContentAssistant(SubjectControlContentAssistant assistant) {
		fContentAssistant= assistant;
	}

	public void setActivationListener(IActivationListener listener) {
		fActivationListener= listener;
	}

	public Text getText() {
		return text;
	}

    protected void checkDeleteable() {
        boolean oldIsDeleteable = isDeleteable;
        isDeleteable = isDeleteEnabled();
        if (oldIsDeleteable != isDeleteable) {
            fireEnablementChanged(DELETE);
        }
    }

    protected void checkSelectable() {
        boolean oldIsSelectable = isSelectable;
        isSelectable = isSelectAllEnabled();
        if (oldIsSelectable != isSelectable) {
            fireEnablementChanged(SELECT_ALL);
        }
    }

    protected void checkSelection() {
        boolean oldIsSelection = isSelection;
        isSelection = text.getSelectionCount() > 0;
        if (oldIsSelection != isSelection) {
            fireEnablementChanged(COPY);
            fireEnablementChanged(CUT);
        }
    }

	private ModifyListener getModifyListener() {
	    if (fModifyListener == null) {
	        fModifyListener = new ModifyListener() {
	            @Override
				public void modifyText(ModifyEvent e) {
	                editOccured(e);
	            }
	        };
	    }
	    return fModifyListener;
	}

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    @Override
	protected Control createControl(Composite parent) {
        text= new Text(parent, getStyle());
        text.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetDefaultSelected(SelectionEvent e) {
                handleDefaultSelection(e);
            }
        });
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// support switching rows while editing:
				if (e.stateMask == SWT.MOD1 || e.stateMask == SWT.MOD2) {
					if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
					    // allow starting multi-selection even if in edit mode
						deactivate();
						e.doit= false;
						return;
					}
				}

				if (e.stateMask != SWT.NONE)
					return;

				switch (e.keyCode) {
				case SWT.ARROW_DOWN:
					e.doit= false;
					int nextRow= fTableViewer.getTable().getSelectionIndex() + 1;
					if (nextRow >= fTableViewer.getTable().getItemCount())
						break;
					editRow(nextRow);
					break;

				case SWT.ARROW_UP:
					e.doit= false;
					int prevRow= fTableViewer.getTable().getSelectionIndex() - 1;
					if (prevRow < 0)
						break;
					editRow(prevRow);
					break;

				case SWT.F2:
					e.doit= false;
					deactivate();
					break;
				}
			}

			private void editRow(int row) {
				fTableViewer.getTable().setSelection(row);
				IStructuredSelection newSelection= (IStructuredSelection) fTableViewer.getSelection();
				if (newSelection.size() == 1)
					fTableViewer.editElement(newSelection.getFirstElement(), fColumn);
			}
		});
        text.addKeyListener(new KeyAdapter() {
            // hook key pressed - see PR 14201
            @Override
			public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);

                // as a result of processing the above call, clients may have
                // disposed this cell editor
                if ((getControl() == null) || getControl().isDisposed())
                    return;
                checkSelection(); // see explaination below
                checkDeleteable();
                checkSelectable();
            }
        });
        text.addTraverseListener(new TraverseListener() {
            @Override
			public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
        // We really want a selection listener but it is not supported so we
        // use a key listener and a mouse listener to know when selection changes
        // may have occurred
        text.addMouseListener(new MouseAdapter() {
            @Override
			public void mouseUp(MouseEvent e) {
                checkSelection();
                checkDeleteable();
                checkSelectable();
            }
        });
        text.addFocusListener(new FocusAdapter() {
            @Override
			public void focusLost(FocusEvent e) {
            	e.display.asyncExec(new Runnable() {
					@Override
					public void run() {
						// without the asyncExec, focus has not had a chance to go to the content assist proposals
						TableTextCellEditor.this.focusLost();
					}
				});
            }
        });
        text.setFont(parent.getFont());
        text.setBackground(parent.getBackground());
        text.setText("");//$NON-NLS-1$
        text.addModifyListener(getModifyListener());

		return text;
    }

    @Override
	protected void fireCancelEditor() {
		/* bug 58540: change signature refactoring interaction: validate as you type [refactoring] */
    	text.setText(fOriginalValue);
		super.fireApplyEditorValue();
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method returns
     * the text string.
     *
     * @return the text string
     */
    @Override
	protected Object doGetValue() {
        return text.getText();
    }

    @Override
	protected void doSetFocus() {
        if (text != null) {
            text.selectAll();
            text.setFocus();
            checkSelection();
            checkDeleteable();
            checkSelectable();
        }
    }

    /**
     * The <code>TextCellEditor2</code> implementation of
     * this <code>CellEditor</code> framework method accepts
     * a text string (type <code>String</code>).
     *
     * @param value a text string (type <code>String</code>)
     */
    @Override
	protected void doSetValue(Object value) {
        Assert.isTrue(text != null && (value instanceof String));
        text.removeModifyListener(getModifyListener());
        text.setText((String) value);
        text.addModifyListener(getModifyListener());
    }

    /**
     * Processes a modify event that occurred in this text cell editor.
     * This framework method performs validation and sets the error message
     * accordingly, and then reports a change via <code>fireEditorValueChanged</code>.
     * Subclasses should call this method at appropriate times. Subclasses
     * may extend or reimplement.
     *
     * @param e the SWT modify event
     */
    protected void editOccured(ModifyEvent e) {
        String value = text.getText();
        boolean oldValidState = isValueValid();
        boolean newValidState = isCorrect(value);
        if (!newValidState) {
            // Try to insert the current value into the error message.
            setErrorMessage(NLS.bind(getErrorMessage(), value));
        }
        valueChanged(oldValidState, newValidState);
		fireModifyEvent(text.getText()); // update model on-the-fly
    }

    @Override
	public LayoutData getLayoutData() {
        return new LayoutData();
    }

    protected void handleDefaultSelection(SelectionEvent event) {
        // same with enter-key handling code in keyReleaseOccured(e);
        fireApplyEditorValue();
        deactivate();
    }

    @Override
	public boolean isCopyEnabled() {
        if (text == null || text.isDisposed())
            return false;
        return text.getSelectionCount() > 0;
    }

    @Override
	public boolean isCutEnabled() {
        if (text == null || text.isDisposed())
            return false;
        return text.getSelectionCount() > 0;
    }

    @Override
	public boolean isDeleteEnabled() {
        if (text == null || text.isDisposed())
            return false;
        return text.getSelectionCount() > 0
                || text.getCaretPosition() < text.getCharCount();
    }

    @Override
	public boolean isPasteEnabled() {
        if (text == null || text.isDisposed())
            return false;
        return true;
    }

    @Override
	public boolean isSelectAllEnabled() {
        if (text == null || text.isDisposed())
            return false;
        return text.getCharCount() > 0;
    }

    @Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.character == '\r') { // Return key
            // Enter is handled in handleDefaultSelection.
            // Do not apply the editor value in response to an Enter key event
            // since this can be received from the IME when the intent is -not-
            // to apply the value.
            // See bug 39074 [CellEditors] [DBCS] canna input mode fires bogus event from Text Control
            //
            // An exception is made for Ctrl+Enter for multi-line texts, since
            // a default selection event is not sent in this case.
            if (text != null && !text.isDisposed() && (text.getStyle() & SWT.MULTI) != 0) {
                if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                    super.keyReleaseOccured(keyEvent);
                }
            }
            return;
        }
        super.keyReleaseOccured(keyEvent);
    }

    @Override
	public void performCopy() {
        text.copy();
    }

    @Override
	public void performCut() {
        text.cut();
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }

    @Override
	public void performDelete() {
        if (text.getSelectionCount() > 0) {
            // remove the contents of the current selection
            text.insert(""); //$NON-NLS-1$
        } else {
            // remove the next character
            int pos = text.getCaretPosition();
            if (pos < text.getCharCount()) {
                text.setSelection(pos, pos + 1);
                text.insert(""); //$NON-NLS-1$
            }
        }
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }

    @Override
	public void performPaste() {
        text.paste();
        checkSelection();
        checkDeleteable();
        checkSelectable();
    }

    @Override
	public void performSelectAll() {
        text.selectAll();
        checkSelection();
        checkDeleteable();
    }

	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}
}
