/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - https://bugs.eclipse.org/bugs/show_bug.cgi?id=145606
 * QNX Software Systems - Catchpoints support https://bugs.eclipse.org/bugs/show_bug.cgi?id=226689
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.propertypages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.CBreakpointUIContributionFactory;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution;
import org.eclipse.cdt.debug.ui.preferences.ReadOnlyFieldEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;

/**
 * The preference page used to present the properties of a breakpoint as preferences. A CBreakpointPreferenceStore is used to interface between this page and
 * the breakpoint.
 */
public class CBreakpointPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	class BreakpointIntegerFieldEditor extends IntegerFieldEditor {

		public BreakpointIntegerFieldEditor( String name, String labelText, Composite parent ) {
			super( name, labelText, parent );
			setErrorMessage( PropertyPageMessages.getString( "CBreakpointPropertyPage.0" ) ); //$NON-NLS-1$
		}

		/**
		 * @see IntegerFieldEditor#checkState()
		 */
		protected boolean checkState() {
			Text control = getTextControl();
			if ( !control.isEnabled() ) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		/**
		 * Overrode here to be package visible.
		 */
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/**
		 * Only store if the text control is enabled
		 * 
		 * @see FieldEditor#doStore()
		 */
		protected void doStore() {
			Text text = getTextControl();
			if ( text.isEnabled() ) {
				super.doStore();
			}
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		protected void clearErrorMessage() {
			if ( getPage() != null ) {
				String message = getPage().getErrorMessage();
				if ( message != null ) {
					if ( getErrorMessage().equals( message ) ) {
						super.clearErrorMessage();
					}
				}
				else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class BreakpointStringFieldEditor extends StringFieldEditor {

		public BreakpointStringFieldEditor( String name, String labelText, Composite parent ) {
			super( name, labelText, parent );
		}

		/**
		 * @see StringFieldEditor#checkState()
		 */
		protected boolean checkState() {
			Text control = getTextControl();
			if ( !control.isEnabled() ) {
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		protected void doStore() {
			Text text = getTextControl();
			if ( text.isEnabled() ) {
				super.doStore();
			}
		}

		/**
		 * @see FieldEditor#refreshValidState()
		 */
		protected void refreshValidState() {
			super.refreshValidState();
		}

		/**
		 * Clears the error message from the message line if the error message is the error message from this field editor.
		 */
		protected void clearErrorMessage() {
			if ( getPage() != null ) {
				String message = getPage().getErrorMessage();
				if ( message != null ) {
					if ( getErrorMessage().equals( message ) ) {
						super.clearErrorMessage();
					}
				}
				else {
					super.clearErrorMessage();
				}
			}
		}
	}

	class LabelFieldEditor extends ReadOnlyFieldEditor {
		private String fValue;

		public LabelFieldEditor( Composite parent, String title, String value ) {
			super(title, title, parent);
			fValue = value;
		}

		@Override
		protected void doLoad() {
			if (textField != null) {
				textField.setText(fValue);
			}
		}
		@Override
		protected void doLoadDefault() {
			// nothing
		}

	}

	private BooleanFieldEditor fEnabled;

	private BreakpointStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;

	private BreakpointIntegerFieldEditor fIgnoreCount;

	private IAdaptable fElement;

	/**
	 * The "fake" preference store used to interface between
	 * the breakpoint and the breakpoint preference page.
	 */
	private CBreakpointPreferenceStore fCBreakpointPreferenceStore;

	/**
	 * Constructor for CBreakpointPropertyPage.
	 * 
	 * @param breakpoint
	 */
	public CBreakpointPropertyPage() {
		super( GRID );
		noDefaultAndApplyButton();
		fCBreakpointPreferenceStore = new CBreakpointPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		ICBreakpoint breakpoint = getBreakpoint();
		createMainLabel(breakpoint);
		createContributetedFieldEditors(breakpoint);
		createTypeSpecificLabelFieldEditors( breakpoint );
		createEnabledField( getFieldEditorParent() );
		IPreferenceStore store = getPreferenceStore();
		try {
			String condition = breakpoint.getCondition();
			if ( condition == null ) {
				condition = ""; //$NON-NLS-1$
			}
			store.setValue( CBreakpointPreferenceStore.CONDITION, condition );
			createConditionEditor( getFieldEditorParent() );
			store.setValue( CBreakpointPreferenceStore.ENABLED, breakpoint.isEnabled() );
			int ignoreCount = breakpoint.getIgnoreCount();
			store.setValue( CBreakpointPreferenceStore.IGNORE_COUNT, (ignoreCount >= 0) ? ignoreCount : 0 );
			createIgnoreCountEditor( getFieldEditorParent() );
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.log( ce );
		}
	}

	private void createMainLabel(ICBreakpoint breakpoint) {
		addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.18" ), 
				getBreakpointMainLabel(breakpoint) ) );  //$NON-NLS-1$
	}

	/**
	 * Method createTypeSpecificLabelFieldEditors.
	 * 
	 * @param breakpoint
	 */
	private void createTypeSpecificLabelFieldEditors( ICBreakpoint breakpoint ) {

		if ( breakpoint instanceof ICFunctionBreakpoint ) {
			ICFunctionBreakpoint fbrkpt = (ICFunctionBreakpoint)breakpoint;
			String function = PropertyPageMessages.getString( "CBreakpointPropertyPage.1" ); //$NON-NLS-1$
			try {
				function = fbrkpt.getFunction();
			}
			catch( CoreException e ) {
			}
			catch( NumberFormatException e ) {
			}
			if ( function != null ) {
				addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.2" ), function ) ); //$NON-NLS-1$
			}
		}
		else if ( breakpoint instanceof ICAddressBreakpoint ) {
			ICAddressBreakpoint abrkpt = (ICAddressBreakpoint)breakpoint;
			String address = PropertyPageMessages.getString( "CBreakpointPropertyPage.4" ); //$NON-NLS-1$
			try {
				address = abrkpt.getAddress();
			}
			catch( CoreException e ) {
			}
			if ( address != null ) {
				addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.5" ), address ) ); //$NON-NLS-1$
			}
		}
		else if ( breakpoint instanceof ICWatchpoint ) {
			ICWatchpoint watchpoint = (ICWatchpoint)breakpoint;
			String expression = ""; //$NON-NLS-1$
			try {
				expression = watchpoint.getExpression();
			}
			catch( CoreException ce ) {
				CDebugUIPlugin.log( ce );
			}
			IProject project = breakpoint.getMarker().getResource().getProject();
			if ( project != null ) {
				addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.10" ), project.getName() ) ); //$NON-NLS-1$
			}
			IResource resource = breakpoint.getMarker().getResource();
			if ( resource instanceof IFile ) {
				String filename = resource.getLocation().toOSString();
				if ( filename != null ) {
					addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.20" ), filename ) ); //$NON-NLS-1$
				}
			}
			addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.14" ), expression ) ); //$NON-NLS-1$
		}
		else if ( breakpoint instanceof ILineBreakpoint ) {
			String fileName = null;
			try {
				fileName = breakpoint.getSourceHandle();
			}
			catch( CoreException e ) {
			}
			if ( fileName != null ) {
				addField( createLabelEditor( getFieldEditorParent(), PropertyPageMessages.getString( "CBreakpointPropertyPage.7" ), fileName ) ); //$NON-NLS-1$
			}
			ILineBreakpoint lBreakpoint = (ILineBreakpoint) breakpoint;

			int lNumber = 0;
			try {
				lNumber = lBreakpoint.getLineNumber();
			} catch (CoreException e) {
				CDebugUIPlugin.log(e);
			}

			if (lNumber > 0) {
				getPreferenceStore().setValue( CBreakpointPreferenceStore.LINE, lNumber);
				createLineNumberEditor(getFieldEditorParent());
			}
		}
	}

	private String getBreakpointMainLabel(ICBreakpoint breakpoint) {
		if (breakpoint instanceof ICFunctionBreakpoint)
			return PropertyPageMessages.getString("CBreakpointPropertyPage.3");
		if (breakpoint instanceof ICAddressBreakpoint)
			return PropertyPageMessages.getString("CBreakpointPropertyPage.4");
		if (breakpoint instanceof ICLineBreakpoint)
			return PropertyPageMessages.getString("CBreakpointPropertyPage.8");
		if (breakpoint instanceof ICEventBreakpoint)
			return PropertyPageMessages.getString("CBreakpointPropertyPage.21");
		if (breakpoint instanceof ICWatchpoint) {
			ICWatchpoint watchpoint = (ICWatchpoint) breakpoint;
			String type = ""; //$NON-NLS-1$
			try {
				if (watchpoint.isReadType() && !watchpoint.isWriteType())
					type = PropertyPageMessages.getString("CBreakpointPropertyPage.11"); //$NON-NLS-1$
				else if (!watchpoint.isReadType() && watchpoint.isWriteType())
					type = PropertyPageMessages.getString("CBreakpointPropertyPage.12"); //$NON-NLS-1$
				else
					type = PropertyPageMessages.getString("CBreakpointPropertyPage.13"); //$NON-NLS-1$

			} catch (CoreException ce) {
				CDebugUIPlugin.log(ce);
			}
			return type;
		}
	    // default main label is the label of marker type for the breakpoint
		String type = ""; //$NON-NLS-1$
		try {
			type = breakpoint.getMarker().getType(); // TODO: how to get label?
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}
		return type;
	}
	protected void createLineNumberEditor( Composite parent ) {
		 String title = PropertyPageMessages.getString( "CBreakpointPropertyPage.9" );
		 BreakpointIntegerFieldEditor labelFieldEditor =new BreakpointIntegerFieldEditor( CBreakpointPreferenceStore.LINE ,title, parent);
		 labelFieldEditor.setValidRange( 1, Integer.MAX_VALUE );
		 addField( labelFieldEditor );
	}
	

	protected void createEnabledField( Composite parent ) {
		fEnabled = new BooleanFieldEditor( CBreakpointPreferenceStore.ENABLED, PropertyPageMessages.getString( "CBreakpointPropertyPage.19" ), parent ); //$NON-NLS-1$
		addField( fEnabled );
	}

	protected void createConditionEditor( Composite parent ) {
		fCondition = new BreakpointStringFieldEditor( CBreakpointPreferenceStore.CONDITION, PropertyPageMessages.getString( "CBreakpointPropertyPage.15" ), parent ); //$NON-NLS-1$
		fCondition.setEmptyStringAllowed( true );
		fCondition.setErrorMessage( PropertyPageMessages.getString( "CBreakpointPropertyPage.16" ) ); //$NON-NLS-1$
		addField( fCondition );
	}

	protected void createIgnoreCountEditor( Composite parent ) {
		fIgnoreCount = new BreakpointIntegerFieldEditor( CBreakpointPreferenceStore.IGNORE_COUNT, PropertyPageMessages.getString( "CBreakpointPropertyPage.17" ), parent ); //$NON-NLS-1$
		fIgnoreCount.setValidRange( 0, Integer.MAX_VALUE );
		fIgnoreCountTextControl = fIgnoreCount.getTextControl( parent );
		try {
			fIgnoreCountTextControl.setEnabled( getBreakpoint().getIgnoreCount() >= 0 );
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.log( ce );
		}
		addField( fIgnoreCount );
	}

	protected FieldEditor createLabelEditor( Composite parent, String title, String value ) {
		return new LabelFieldEditor( parent, title, value );
	}

	protected ICBreakpoint getBreakpoint() {
		IAdaptable element = getElement();
		return ( element instanceof ICBreakpoint ) ? (ICBreakpoint)element : (ICBreakpoint)element.getAdapter(ICBreakpoint.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement( IAdaptable element ) {
		fElement = element;
	}

	public IPreferenceStore getPreferenceStore() {
		return fCBreakpointPreferenceStore;
	}

	public boolean performOk() {
		final List changedProperties = new ArrayList( 5 );
		getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener() {

			/**
			 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
			 */
			public void propertyChange( PropertyChangeEvent event ) {
				changedProperties.add( event.getProperty() );
			}
		} );
		boolean result = super.performOk();
		setBreakpointProperties( changedProperties );
		return result;
	}

	protected void setBreakpointProperties( final List changedProperties ) {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			public void run( IProgressMonitor monitor ) throws CoreException {
				ICBreakpoint breakpoint = getBreakpoint();
				Iterator changed = changedProperties.iterator();
				while( changed.hasNext() ) {
					String property = (String)changed.next();
					if ( property.equals( CBreakpointPreferenceStore.ENABLED ) ) {
						breakpoint.setEnabled( getPreferenceStore().getBoolean( CBreakpointPreferenceStore.ENABLED ) );
					}
					else if ( property.equals( CBreakpointPreferenceStore.IGNORE_COUNT ) ) {
						breakpoint.setIgnoreCount( getPreferenceStore().getInt( CBreakpointPreferenceStore.IGNORE_COUNT ) );
					}
					else if ( property.equals( CBreakpointPreferenceStore.CONDITION ) ) {
						breakpoint.setCondition( getPreferenceStore().getString( CBreakpointPreferenceStore.CONDITION ) );
					}
					else if ( property.equals( CBreakpointPreferenceStore.LINE ) ) {
						// already workspace runnable, setting markers are safe
						breakpoint.getMarker().setAttribute(IMarker.LINE_NUMBER, getPreferenceStore().getInt(CBreakpointPreferenceStore.LINE));
					} else {
					    // this allow set attributes contributed by other plugins
						String value = getPropertyAsString(property);
						breakpoint.getMarker().setAttribute(property, value);
					}
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run( wr, null );
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.log( ce );
		}
	}
	/**
	 * Creates field editors contributed using breakpointUIContribution extension point
	 * @param breakpoint
	 */
	private void createContributetedFieldEditors(ICBreakpoint breakpoint) {
		Composite parent = getFieldEditorParent();
		try {
			ICBreakpointsUIContribution cons[] = CBreakpointUIContributionFactory.getInstance()
					.getBreakpointUIContributions(breakpoint);
			for (ICBreakpointsUIContribution con : cons) {

				FieldEditor fieldEditor = con.getFieldEditor(con.getId(), con.getLabel()+":", parent);
				if (fieldEditor != null)
					addField(fieldEditor);
				Object o = breakpoint.getMarker().getAttribute(con.getId());
				String value = o==null?"":o.toString();
				getPreferenceStore().setValue(con.getId(), value);
			}
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}

	}

	/**
	 * Return string value of given property or null.
	 */
	protected String getPropertyAsString(String property) {
		// currently only supports String and Integer
		IPreferenceStore store = getPreferenceStore();
		
		if (store.contains(property)) {
			String value = store.getString(property);
			return value;
		} else return null;
	}
}
