/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.Messages;


public abstract class ModifyDialogTabPage implements IModifyDialogTabPage {	
	/**
	 * This is the default listener for any of the Preference
	 * classes. It is added by the respective factory methods and
	 * updates the page's preview on each change. 
	 */
	protected final Observer fUpdater= new Observer() {
		public void update(Observable o, Object arg) {
			doUpdatePreview();
			notifyValuesModified();
		}
	};
	
	/**
	 * The base class of all Preference classes. A preference class provides a wrapper
	 * around one or more SWT widgets and handles the input of values for some key.
	 * On each change, the new value is written to the map and the listeners are notified.
	 */
	protected abstract class Preference extends Observable {
	    private final Map<String, String> fPreferences;
	    private boolean fEnabled;
	    private String fKey;
	    
	    /**
	     * Create a new Preference.
	     * @param preferences The map where the value is written.
	     * @param key The key for which a value is managed.
	     */
	    public Preference(Map<String, String> preferences, String key) {
	        fPreferences= preferences;
	        fEnabled= true;
	        fKey= key;
	    }
	    /**
	     * @return Gets the map of this Preference.
	     */
	    protected final Map<String, String> getPreferences() {
	        return fPreferences;
	    }

	    /**
	     * Set the enabled state of all SWT widgets of this preference. 
	     * @param enabled new value
	     */
	    public final void setEnabled(boolean enabled) {
	        fEnabled= enabled;
	        updateWidget();
	    }
	    
	    /**
	     * @return Gets the enabled state of all SWT widgets of this Preference.
	     */
	    public final boolean getEnabled() {
	        return fEnabled;
	    }
	    
	    /**
	     * Set the key which is used to store the value.
	     * @param key New value
	     */
	    public final void setKey(String key) {
	        if (key == null || !fKey.equals(key)) {
	            fKey= key;
	            updateWidget();
	        }
	    }
	    /**
	     * @return Gets the currently used key which is used to store the value.
	     */	    
	    public final String getKey() {
	        return fKey;
	    }
	    
	    /**
	     * Returns the main control of a preference, which is mainly used to 
	     * manage the focus. This may be <code>null</code> if the preference doesn't
	     * have a control which is able to have the focus. 
	     * @return The main control
	     */
	    public abstract Control getControl();
	    
	    /**
	     * To be implemented in subclasses. Update the SWT widgets when the state 
	     * of this object has changed (enabled, key, ...).
	     */
	    protected abstract void updateWidget();
	}
	
	/**
	 * Wrapper around a checkbox and a label. 
	 */	
	protected class ButtonPreference extends Preference {
		private final String[] fValues;
		private final Button fCheckbox;
		
		/**
		 * Create a new CheckboxPreference.
		 * @param composite The composite on which the SWT widgets are added.
		 * @param numColumns The number of columns in the composite's GridLayout.
		 * @param preferences The map to store the values.
		 * @param key The key to store the values.
		 * @param values An array of two elements indicating the values to store on unchecked/checked.
		 * @param text The label text for this Preference.
		 * @param style SWT style flag for the button
		 */
		public ButtonPreference(Composite composite, int numColumns,
								  Map<String, String> preferences, String key, 
								  String [] values, String text, int style) {
		    super(preferences, key);
		    if (values == null || text == null) 
		        throw new IllegalArgumentException(FormatterMessages.ModifyDialogTabPage_error_msg_values_text_unassigned); 
			fValues= values;

			fCheckbox= new Button(composite, style);
			fCheckbox.setText(text);
			fCheckbox.setLayoutData(createGridData(numColumns, GridData.FILL_HORIZONTAL, SWT.DEFAULT));
			fCheckbox.setFont(composite.getFont());
			
			updateWidget();

			fCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					checkboxChecked(((Button)e.widget).getSelection());
				}
			});
		}
		
		protected void checkboxChecked(boolean state) {
			getPreferences().put(getKey(), state ? fValues[1] : fValues[0]);
			setChanged();
			notifyObservers();
		}
		
		@Override
		protected void updateWidget() {
			if (getKey() != null) {
				fCheckbox.setEnabled(getEnabled());
				fCheckbox.setSelection(getChecked());
			} else {
				fCheckbox.setSelection(false);
				fCheckbox.setEnabled(false);
			}
		}
		
		public boolean getChecked() {
			return fValues[1].equals(getPreferences().get(getKey()));
		}
		
		public void setChecked(boolean checked) {
			getPreferences().put(getKey(), checked ? fValues[1] : fValues[0]);
			updateWidget();
			checkboxChecked(checked);	
		}
		
		@Override
		public Control getControl() {
			return fCheckbox;
		}
	}
	
	protected final class CheckboxPreference extends ButtonPreference {
		public CheckboxPreference(Composite composite, int numColumns, Map<String,String> preferences, String key, String[] values, String text) {
	        super(composite, numColumns, preferences, key, values, text, SWT.CHECK);
        }
	}
	
	protected final class RadioPreference extends ButtonPreference {
		public RadioPreference(Composite composite, int numColumns, Map<String,String> preferences, String key, String[] values, String text) {
	        super(composite, numColumns, preferences, key, values, text, SWT.RADIO);
        }	
	}
	
	/**
	 * Wrapper around a Combo box.
	 */
	protected final class ComboPreference extends Preference {
		private final String [] fItems;
		private final String[] fValues;
		private final Combo fCombo;
		
		/**
		 * Create a new ComboPreference.
		 * @param composite The composite on which the SWT widgets are added.
		 * @param numColumns The number of columns in the composite's GridLayout.
		 * @param preferences The map to store the values.
		 * @param key The key to store the values.
		 * @param values An array of n elements indicating the values to store for each selection.
		 * @param text The label text for this Preference.
		 * @param items An array of n elements indicating the text to be written in the combo box.
		 */
		public ComboPreference(Composite composite, int numColumns,
								  Map<String, String> preferences, String key, 
								  String [] values, String text, String [] items) {
		    super(preferences, key);
		    if (values == null || items == null || text == null) 
		        throw new IllegalArgumentException(FormatterMessages.ModifyDialogTabPage_error_msg_values_items_text_unassigned); 
			fValues= values;
			fItems= items;
			createLabel(numColumns - 1, composite, text);
			fCombo= new Combo(composite, SWT.SINGLE | SWT.READ_ONLY);
			fCombo.setFont(composite.getFont());
			fCombo.setItems(items);
			
			int max= 0;
			for (String item : items)
				if (item.length() > max) max= item.length();
			
			fCombo.setLayoutData(createGridData(1, GridData.HORIZONTAL_ALIGN_FILL, fCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x));			

			updateWidget();

			fCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					comboSelected(((Combo)e.widget).getSelectionIndex());
				}
			});
		}
		
		protected void comboSelected(int index) {
			getPreferences().put(getKey(), fValues[index]);
			setChanged();
			notifyObservers(fValues[index]);
		}
		
		@Override
		protected void updateWidget() {
			if (getKey() != null) {
				fCombo.setEnabled(getEnabled());
				fCombo.setText(getSelectedItem());
			} else {
				fCombo.setText(""); //$NON-NLS-1$
				fCombo.setEnabled(false);
			}
		}
		
		public String getSelectedItem() {
			final String selected= getPreferences().get(getKey());
			for (int i= 0; i < fValues.length; i++) {
				if (fValues[i].equals(selected)) {
					return fItems[i];
				}
			}
			return ""; //$NON-NLS-1$
		}
		
		public boolean hasValue(String value) {
			return value.equals(getPreferences().get(getKey()));
		}
		
		@Override
		public Control getControl() {
			return fCombo;
		}
	}
	
	/**
	 * Wrapper around a textfied which requests an integer input of a given range.
	 */
	protected final class NumberPreference extends Preference {
		
		private final int fMinValue, fMaxValue;
		private final Label fNumberLabel;
		private final Text fNumberText;

		protected int fSelected;
        protected int fOldSelected;
        
        
		/**
		 * Create a new NumberPreference.
		 * @param composite The composite on which the SWT widgets are added.
		 * @param numColumns The number of columns in the composite's GridLayout.
		 * @param preferences The map to store the values.
		 * @param key The key to store the values.
		 * @param minValue The minimum value which is valid input.
		 * @param maxValue The maximum value which is valid input.
		 * @param text The label text for this Preference.
		 */
		public NumberPreference(Composite composite, int numColumns,
							   Map<String, String> preferences, String key, 
							   int minValue, int maxValue, String text) {
		    super(preferences, key);
		    
			fNumberLabel= createLabel(numColumns - 1, composite, text, GridData.FILL_HORIZONTAL);
			fNumberText= new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
			fNumberText.setFont(composite.getFont());

			final int length= Integer.toString(maxValue).length() + 3; 
			fNumberText.setLayoutData(createGridData(1, GridData.HORIZONTAL_ALIGN_END, fPixelConverter.convertWidthInCharsToPixels(length)));
			
			fMinValue= minValue;
			fMaxValue= maxValue;
			
			updateWidget();
			
			fNumberText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				    NumberPreference.this.focusGained();
				}
                public void focusLost(FocusEvent e) {
				    NumberPreference.this.focusLost();
				}
			});
			
			fNumberText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					fieldModified();
				}
			});
		}
		
		private IStatus createErrorStatus() {
		    return new Status(IStatus.ERROR, CUIPlugin.getPluginId(), 0, Messages.format(FormatterMessages.ModifyDialogTabPage_NumberPreference_error_invalid_value, Integer.toString(fMinValue), Integer.toString(fMaxValue)), null); 
		}

		protected void focusGained() {
		    fOldSelected= fSelected;
		    fNumberText.setSelection(0, fNumberText.getCharCount());
		}
		
		protected void focusLost() {
		    updateStatus(null);
		    final String input= fNumberText.getText();
		    if (!validInput(input))
		        fSelected= fOldSelected;
		    else
		        fSelected= Integer.parseInt(input);
		    if (fSelected != fOldSelected) {
		    	saveSelected();
		    	fNumberText.setText(Integer.toString(fSelected));
		    }
		}
		
		
		protected void fieldModified() {
		    final String trimInput= fNumberText.getText().trim();
		    final boolean valid= validInput(trimInput);
		    
		    updateStatus(valid ? null : createErrorStatus());

		    if (valid) {
		        final int number= Integer.parseInt(trimInput);
		        if (fSelected != number) {
		            fSelected= number;
		            saveSelected();
		        }
		    }
		}
		
		private boolean validInput(String trimInput) {
		    int number;
		    
		    try {
		        number= Integer.parseInt(trimInput);
		    } catch (NumberFormatException x) {
		        return false;
		    }
		    
		    if (number < fMinValue) return false;
		    if (number > fMaxValue) return false;
		    return true;
		}
		
		private void saveSelected() {
			getPreferences().put(getKey(), Integer.toString(fSelected));
			setChanged();
			notifyObservers();
		}
		
		@Override
		protected void updateWidget() {
		    final boolean hasKey= getKey() != null;

		    fNumberLabel.setEnabled(hasKey && getEnabled());
			fNumberText.setEnabled(hasKey && getEnabled());

			if (hasKey) {
			    String s= getPreferences().get(getKey());
			    try {
			        fSelected= Integer.parseInt(s);
			    } catch (NumberFormatException e) {
			        final String message= Messages.format(FormatterMessages.ModifyDialogTabPage_NumberPreference_error_invalid_key, getKey()); 
			        CUIPlugin.log(new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.OK, message, e));
			        s= ""; //$NON-NLS-1$
			    }
			    fNumberText.setText(s);
			} else {
			    fNumberText.setText(""); //$NON-NLS-1$
			}
		}
		
		@Override
		public Control getControl() {
			return fNumberText;
		}
	}

	
	/**
	 * This class provides the default way to preserve and re-establish the focus
	 * over multiple modify sessions. Each ModifyDialogTabPage has its own instance,
	 * and it should add all relevant controls upon creation, always in the same sequence.
	 * This established a mapping of controls to indexes, which allows to restore the focus
	 * in a later session. 
	 * The index is saved in the dialog settings, and there is only one common preference for 
	 * all tab pages. It is always the currently active tab page which stores its focus
	 * index. 
	 */
	protected final static class DefaultFocusManager extends FocusAdapter {
		
		private final static String PREF_LAST_FOCUS_INDEX= "formatter_page.modify_dialog_tab_page.last_focus_index"; //$NON-NLS-1$ 
		
		private final IDialogSettings fDialogSettings;
		
		private final Map<Control, Integer> fItemMap;
		private final List<Control> fItemList;
		
		private int fIndex;
		
		public DefaultFocusManager() {
			fDialogSettings= CUIPlugin.getDefault().getDialogSettings();
			fItemMap= new HashMap<Control, Integer>();
			fItemList= new ArrayList<Control>();
			fIndex= 0;
		}

		@Override
		public void focusGained(FocusEvent e) {
			fDialogSettings.put(PREF_LAST_FOCUS_INDEX, fItemMap.get(e.widget).intValue());
		}
		
		public void add(Control control) {
			control.addFocusListener(this);
			fItemList.add(fIndex, control);
			fItemMap.put(control, new Integer(fIndex++));
		}
		
		public void add(Preference preference) {
			final Control control= preference.getControl();
			if (control != null) 
				add(control);
		}
		
		public boolean isUsed() {
			return fIndex != 0;
		}
		
		public void restoreFocus() {
			int index= 0;
			try {
				index= fDialogSettings.getInt(PREF_LAST_FOCUS_INDEX);
				// make sure the value is within the range
				if ((index >= 0) && (index <= fItemList.size() - 1)) {
					fItemList.get(index).setFocus();
				}
			} catch (NumberFormatException ex) {
				// this is the first time
			}
		}
		
		public void resetFocus() {
			fDialogSettings.put(PREF_LAST_FOCUS_INDEX, -1);
		}
	}
	
	/**
	 * Layout used for the settings part. Makes sure to show scrollbars
	 * if necessary. The settings part needs to be layouted on resize.
	 */
	private static class PageLayout extends Layout {
		
		private final ScrolledComposite fContainer;
		private final int fMinimalWidth;
		private final int fMinimalHight;

		private PageLayout(ScrolledComposite container, int minimalWidth, int minimalHight) {
			fContainer= container;
			fMinimalWidth= minimalWidth;
			fMinimalHight= minimalHight;
		}
		
		@Override
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
			
			int x = fMinimalWidth;
			int y = fMinimalHight;
			Control[] children = composite.getChildren();
			for (Control element : children) {
				Point size = element.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}
			
			Rectangle area= fContainer.getClientArea();
			if (area.width > x) {
				fContainer.setExpandHorizontal(true);
			} else {
				fContainer.setExpandHorizontal(false);
			}
			
			if (area.height > y) {
				fContainer.setExpandVertical(true);
			} else {
				fContainer.setExpandVertical(false);
			}
			
			if (wHint != SWT.DEFAULT) {
				x = wHint;
			}
			if (hHint != SWT.DEFAULT) {
				y = hHint;
			}
			
			return new Point(x, y);
		}

		@Override
		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			for (Control element : children) {
				element.setSize(rect.width, rect.height);
			}
		}
	}

	/**
	 * The default focus manager. This widget knows all widgets which can have the focus
	 * and listens for focusGained events, on which it stores the index of the current
	 * focus holder. When the dialog is restarted, <code>restoreFocus()</code> sets the 
	 * focus to the last control which had it.
	 * 
	 * The standard Preference object are managed by this focus manager if they are created
	 * using the respective factory methods. Other SWT widgets can be added in subclasses 
	 * when they are created.
	 */
	protected final DefaultFocusManager fDefaultFocusManager;

	/**
	 * A pixel converter for layout calculations
	 */
	protected PixelConverter fPixelConverter;
	
	
	/**
	 * The map where the current settings are stored.
	 */
	protected Map<String,String> fWorkingValues;
	
	/**
	 * The modify dialog where we can display status messages.
	 */
	private IModifyDialogTabPage.IModificationListener fModifyListener;


	/*
	 * Create a new <code>ModifyDialogTabPage</code>
	 */
	public ModifyDialogTabPage(IModifyDialogTabPage.IModificationListener modifyListener, Map<String,String> workingValues) {
		fWorkingValues= workingValues;
		fModifyListener= modifyListener;
		fDefaultFocusManager= new DefaultFocusManager();
	}
	
	public ModifyDialogTabPage() {
		fDefaultFocusManager= new DefaultFocusManager();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setWorkingValues(Map<String,String> workingValues) {
		fWorkingValues= workingValues;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setModifyListener(IModifyDialogTabPage.IModificationListener modifyListener) {
		fModifyListener= modifyListener;
	}
	
	/**
	 * Create the contents of this tab page. Subclasses cannot override this, 
	 * instead they must implement <code>doCreatePreferences</code>. <code>doCreatePreview</code> may also
	 * be overridden as necessary.
	 * @param parent The parent composite
	 * @return Created content control
	 */
	public final Composite createContents(Composite parent) {
		final int numColumns= 4;
		
		if (fPixelConverter == null) {
		    fPixelConverter= new PixelConverter(parent);
		}
		
		final SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setFont(parent.getFont());
		
		Composite scrollContainer = new Composite(sashForm, SWT.NONE);
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		scrollContainer.setLayoutData(gridData);
				
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		scrollContainer.setLayout(layout);
		
		ScrolledComposite scroll= new ScrolledComposite(scrollContainer, SWT.V_SCROLL | SWT.H_SCROLL);
		scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);

		final Composite settingsContainer= new Composite(scroll, SWT.NONE);
		settingsContainer.setFont(sashForm.getFont());
		
		scroll.setContent(settingsContainer);
		
		settingsContainer.setLayout(new PageLayout(scroll, 400, 400));
		settingsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite settingsPane= new Composite(settingsContainer, SWT.NONE);
		settingsPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		layout= new GridLayout(numColumns, false);
		layout.verticalSpacing= (int)(1.5 * fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING));
		layout.horizontalSpacing= fPixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.marginHeight= fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= fPixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		settingsPane.setLayout(layout);
		doCreatePreferences(settingsPane, numColumns);
		
		settingsContainer.setSize(settingsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));	
		
		scroll.addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				settingsContainer.setSize(settingsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));	
			}
		});
		
		Label sashHandle = new Label(scrollContainer, SWT.SEPARATOR | SWT.VERTICAL);
		gridData= new GridData(SWT.RIGHT, SWT.FILL, false, true);
		sashHandle.setLayoutData(gridData);

		final Composite previewPane= new Composite(sashForm, SWT.NONE);
		previewPane.setLayout(createGridLayout(numColumns, true));
		previewPane.setFont(sashForm.getFont());
		doCreatePreviewPane(previewPane, numColumns);

		initializePage();
	
		sashForm.setWeights(new int [] {3, 3});
		return sashForm;
	}
	
	/**
	 * This method is called after all controls have been allocated, including the preview. 
	 * It can be used to set the preview text and to create listeners.
	 *
	 */
	protected abstract void initializePage();
	

	/**
	 * Create the left side of the modify dialog. This is meant to be implemented by subclasses. 
	 * @param composite Composite to create in
	 * @param numColumns Number of columns to use
	 */
	protected abstract void doCreatePreferences(Composite composite, int numColumns);
	

	/**
	 * Create the right side of the modify dialog. By default, the preview is displayed there.
	 * Subclasses can override this method in order to customize the right-hand side of the 
	 * dialog.
	 * @param composite Composite to create in
	 * @param numColumns Number of columns to use
	 * @return Created composite
	 */
	protected Composite doCreatePreviewPane(Composite composite, int numColumns) {
		createLabel(numColumns, composite, FormatterMessages.ModifyDialogTabPage_preview_label_text);  
		
		final CPreview preview= doCreateCPreview(composite);
		fDefaultFocusManager.add(preview.getControl());
		
		final GridData gd= createGridData(numColumns, GridData.FILL_BOTH, 0);
		gd.widthHint= 0;
		gd.heightHint=0;
		preview.getControl().setLayoutData(gd);
		
		return composite;
	}

	/**
	 * To be implemented by subclasses. This method should return an instance of CPreview.
	 * Currently, the choice is between CompilationUnitPreview which contains a valid compilation
	 * unit, or a SnippetPreview which formats several independent code snippets and displays them 
	 * in the same window.
	 * @param parent Parent composite
	 * @return Created preview
	 */
	protected abstract CPreview doCreateCPreview(Composite parent);
	
	/**
	 * {@inheritDoc}
	 */
	final public void makeVisible() {
		fDefaultFocusManager.resetFocus();
		doUpdatePreview();
	}
	
	/**
	 * Update the preview. To be implemented by subclasses.
	 */
	protected abstract void doUpdatePreview();
	
	protected void notifyValuesModified() {
		fModifyListener.valuesModified();
	}
    /**
	 * {@inheritDoc}
	 */
    public void setInitialFocus() {
		if (fDefaultFocusManager.isUsed()) {
			fDefaultFocusManager.restoreFocus();
		}
	}
	

    /**
     * Set the status field on the dialog. This can be used by tab pages to report 
     * inconsistent input. The OK button is disabled if the kind is IStatus.ERROR. 
     * @param status Status describing the current page error state
     */
	protected void updateStatus(IStatus status) {
		fModifyListener.updateStatus(status);
	}
	
	/*
	 * Factory methods to make GUI construction easier
	 */
	
	/*
	 * Create a GridLayout with the default margin and spacing settings, as
	 * well as the specified number of columns.
	 */
	protected GridLayout createGridLayout(int numColumns, boolean margins) {
		final GridLayout layout= new GridLayout(numColumns, false);
		layout.verticalSpacing= fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing= fPixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		if (margins) {
			layout.marginHeight= fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth= fPixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		} else {
			layout.marginHeight= 0;
			layout.marginWidth= 0;
		}
		return layout;
	}

	/*
	 * Convenience method to create a GridData.
	 */
	protected static GridData createGridData(int numColumns, int style, int widthHint) {
		final GridData gd= new GridData(style);
		gd.horizontalSpan= numColumns;
		gd.widthHint= widthHint;
		return gd;		
	}


	/* 
	 * Convenience method to create a label.  
	 */
	protected static Label createLabel(int numColumns, Composite parent, String text) {
		return createLabel(numColumns, parent, text, GridData.FILL_HORIZONTAL);
	}
	
	/*
	 * Convenience method to create a label
	 */
	protected static Label createLabel(int numColumns, Composite parent, String text, int gridDataStyle) {
		final Label label= new Label(parent, SWT.WRAP);
		label.setFont(parent.getFont());
		label.setText(text);
		
		PixelConverter pixelConverter= new PixelConverter(parent);
		label.setLayoutData(createGridData(numColumns, gridDataStyle, pixelConverter.convertHorizontalDLUsToPixels(150)));
		return label;
	}

	/*
	 * Convenience method to create a group
	 */
	protected Group createGroup(int numColumns, Composite parent, String text ) {
		final Group group= new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setLayoutData(createGridData(numColumns, GridData.FILL_HORIZONTAL, SWT.DEFAULT));
		
		final GridLayout layout= new GridLayout(numColumns, false);
		layout.verticalSpacing=  fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing= fPixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.marginHeight=  fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		//layout.marginHeight= fPixelConverter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		//layout.marginWidth= fPixelConverter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		
		group.setLayout(layout);//createGridLayout(numColumns, true));
		group.setText(text);
		return group;
	}
	
	/*
	 * Convenience method to create a NumberPreference. The widget is registered as 
	 * a potential focus holder, and the default updater is added.
	 */
	protected NumberPreference createNumberPref(Composite composite, int numColumns, String name, String key,
												int minValue, int maxValue) {
		final NumberPreference pref= new NumberPreference(composite, numColumns, fWorkingValues, 
			key, minValue, maxValue, name);
		fDefaultFocusManager.add(pref);
		pref.addObserver(fUpdater);
		return pref;
	}
	
	/*
	 * Convenience method to create a ComboPreference. The widget is registered as 
	 * a potential focus holder, and the default updater is added.
	 */
	protected ComboPreference createComboPref(Composite composite, int numColumns, String name, 
											  String key, String [] values, String [] items) {
		final ComboPreference pref= new ComboPreference(composite, numColumns, 
			fWorkingValues, key, values, name, items);
		fDefaultFocusManager.add(pref);
		pref.addObserver(fUpdater);
		return pref;
	}

	/*
	 * Convenience method to create a CheckboxPreference. The widget is registered as 
	 * a potential focus holder, and the default updater is added.
	 */
	protected CheckboxPreference createCheckboxPref(Composite composite, int numColumns, String name, String key,
													String [] values) {
		final CheckboxPreference pref= new CheckboxPreference(composite, numColumns, 
			fWorkingValues, key, values, name);
		fDefaultFocusManager.add(pref);
		pref.addObserver(fUpdater);
		return pref;
	}
	
	protected RadioPreference createRadioPref(Composite composite, int numColumns, String name, String key,
			String [] values) {
		final RadioPreference pref= new RadioPreference(composite, numColumns, 
				fWorkingValues, key, values, name);
		fDefaultFocusManager.add(pref);
		pref.addObserver(fUpdater);
		return pref;
	}
	

	/*
	 * Create a nice javadoc comment for some string.
	 */
	protected static String createPreviewHeader(String title) {
		return "/*\n* " + title + "\n*/\n"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
