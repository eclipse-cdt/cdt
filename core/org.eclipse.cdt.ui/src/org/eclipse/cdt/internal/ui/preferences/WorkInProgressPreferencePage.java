/*
 * Created on Mar 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.search.CSearchPage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WorkInProgressPreferencePage extends PreferencePage
		implements
			IWorkbenchPreferencePage {
	
	private Combo fExternLinks;
	private Button fExternEnabled;
	
	protected OverlayPreferenceStore fOverlayStore;
	
	public WorkInProgressPreferencePage(){
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		fOverlayStore  = createOverlayStore();
	}

	private OverlayPreferenceStore createOverlayStore() {
		ArrayList overlayKeys = new ArrayList();		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, CSearchPage.EXTERNALMATCH_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CSearchPage.EXTERNALMATCH_VISIBLE));
		
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();
		
		initializeDialogUnits(parent);
		
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		result.setLayout(layout);
		
		Group group= new Group(result, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("External Search Links"); //$NON-NLS-1$

		fExternEnabled = createCheckButton(group, "Enable external search markers"); //$NON-NLS-1$
		fExternEnabled.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				boolean externLinkEnabled = false;
				fExternLinks.setEnabled(false);
				if (button.getSelection()){
					fExternLinks.setEnabled(true);
					externLinkEnabled = true;
				}
				
				fOverlayStore.setValue(CSearchPage.EXTERNALMATCH_ENABLED, externLinkEnabled);
			}
		});
	
		fExternLinks = createComboBox(group,"External Marker Link Type",new String[]{"Visible","Invisible"},"Visible"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		fExternLinks.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.widget;
				fOverlayStore.setValue(CSearchPage.EXTERNALMATCH_VISIBLE, combo.getSelectionIndex());
			}
		});
		
		initialize(); 
		
		return result;
	
	}
	
	private void initialize(){
		boolean extEnabled = fOverlayStore.getBoolean(CSearchPage.EXTERNALMATCH_ENABLED);
		fExternEnabled.setSelection(extEnabled);
		
		fExternLinks.select(fOverlayStore.getInt(CSearchPage.EXTERNALMATCH_VISIBLE));
		fExternLinks.setEnabled(extEnabled);
		

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Combo createComboBox( Composite parent, String label, String[] items, String selection )
	{
		ControlFactory.createLabel( parent, label );
		Combo combo = ControlFactory.createSelectCombo( parent, items, selection );
		combo.setLayoutData( new GridData() );
		return combo;
	}
	/**
	 * Creates a button with the given label and sets the default 
	 * configuration data.
	 */
	private Button createCheckButton( Composite parent, String label )
	{
		Button button = new Button( parent, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}
	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		fOverlayStore.propagate();
		return true;
	}
	
	/**
	 * @param store
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(CSearchPage.EXTERNALMATCH_ENABLED, false);
		store.setDefault(CSearchPage.EXTERNALMATCH_VISIBLE, 0);
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fOverlayStore.loadDefaults();
		initialize();
		super.performDefaults();
	}
}
