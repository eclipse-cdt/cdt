/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * The preference page that is used to present the properties of a breakpoint as 
 * preferences.  A CBreakpointPreferenceStore is used to interface between this
 * page and the breakpoint.
 * 
 * @since Aug 27, 2002
 */
public class CBreakpointPreferencePage extends FieldEditorPreferencePage
{
	class BreakpointIntegerFieldEditor extends IntegerFieldEditor
	{
		public BreakpointIntegerFieldEditor( String name, String labelText, Composite parent )
		{
			super( name, labelText, parent );
			setErrorMessage( CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Ignore_count_must_be_positive_integer") ); //$NON-NLS-1$
		}

		/**
		 * @see IntegerFieldEditor#checkState()
		 */
		protected boolean checkState()
		{
			Text control = getTextControl();
			if ( !control.isEnabled() )
			{
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		/**
		 * Overrode here to be package visible.
		 */
		protected void refreshValidState()
		{
			super.refreshValidState();
		}

		/**
		 * Only store if the text control is enabled
		 * @see FieldEditor#doStore()
		 */
		protected void doStore()
		{
			Text text = getTextControl();
			if ( text.isEnabled() )
			{
				super.doStore();
			}
		}
		/**
			 * Clears the error message from the message line if the error
			 * message is the error message from this field editor.
		 */
		protected void clearErrorMessage()
		{
			if ( getPreferencePage() != null )
			{
				String message = getPreferencePage().getErrorMessage();
				if ( message != null )
				{
					if ( getErrorMessage().equals( message ) )
					{
						super.clearErrorMessage();
					}
				}
				else
				{
					super.clearErrorMessage();
				}
			}
		}
	}

	class BreakpointStringFieldEditor extends StringFieldEditor
	{
		public BreakpointStringFieldEditor( String name, String labelText, Composite parent )
		{
			super( name, labelText, parent );
		}

		/**
		 * @see StringFieldEditor#checkState()
		 */
		protected boolean checkState()
		{
			Text control = getTextControl();
			if ( !control.isEnabled() )
			{
				clearErrorMessage();
				return true;
			}
			return super.checkState();
		}

		protected void doStore()
		{
			Text text = getTextControl();
			if ( text.isEnabled() )
			{
				super.doStore();
			}
		}

		/**
		 * @see FieldEditor#refreshValidState()
		 */
		protected void refreshValidState()
		{
			super.refreshValidState();
		}

		/**
			 * Clears the error message from the message line if the error
			 * message is the error message from this field editor.
		 */
		protected void clearErrorMessage()
		{
			if ( getPreferencePage() != null )
			{
				String message = getPreferencePage().getErrorMessage();
				if ( message != null )
				{
					if ( getErrorMessage().equals( message ) )
					{
						super.clearErrorMessage();
					}

				}
				else
				{
					super.clearErrorMessage();
				}
			}
		}
	}

	class LabelFieldEditor extends FieldEditor 
	{
		private Label fTitleLabel;
		private Label fValueLabel;
		private Composite fBasicComposite;
		private String fValue;
		private String fTitle;

		public LabelFieldEditor( Composite parent, String title, String value )
		{
			fValue = value;
			fTitle = title;
			this.createControl( parent );
		}

		protected void adjustForNumColumns( int numColumns )
		{
			((GridData)fBasicComposite.getLayoutData()).horizontalSpan = numColumns;
		}

		protected void doFillIntoGrid( Composite parent, int numColumns )
		{
			fBasicComposite = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.numColumns = 2;
			fBasicComposite.setLayout( layout );
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			fBasicComposite.setLayoutData( data );

			fTitleLabel = new Label( fBasicComposite, SWT.NONE );
			fTitleLabel.setText( fTitle );
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			fTitleLabel.setLayoutData( gd );

			fValueLabel = new Label( fBasicComposite, SWT.WRAP );
			fValueLabel.setText( fValue );
			gd = new GridData();
			fValueLabel.setLayoutData( gd );
		}

		public int getNumberOfControls()
		{
			return 1;
		}

		/**
		 * The label field editor is only used to present a text label
		 * on a preference page.
		 */
		protected void doLoad()
		{
		}

		protected void doLoadDefault()
		{
		}

		protected void doStore()
		{
		}
	}

	private BreakpointStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;
	private BreakpointIntegerFieldEditor fIgnoreCount;
	
	private ICBreakpoint fBreakpoint;

	/**
	 * Constructor for CBreakpointPreferencePage.
	 * @param breakpoint
	 */
	public CBreakpointPreferencePage( ICBreakpoint breakpoint )
	{
		super( GRID );
		setBreakpoint( breakpoint );
		noDefaultAndApplyButton();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors()
	{
		ICBreakpoint breakpoint = getBreakpoint();

		createTypeSpecificLabelFieldEditors( breakpoint );

		IPreferenceStore store = getPreferenceStore();

		try
		{
			String condition= breakpoint.getCondition();
			if ( condition == null ) 
			{
				condition = ""; //$NON-NLS-1$
			}
			store.setValue( CBreakpointPreferenceStore.CONDITION, condition );

			createConditionEditor( getFieldEditorParent() );

			store.setValue( CBreakpointPreferenceStore.ENABLED, breakpoint.isEnabled() );
			int ignoreCount = breakpoint.getIgnoreCount();
			store.setValue( CBreakpointPreferenceStore.IGNORE_COUNT, ( ignoreCount >= 0 ) ? ignoreCount : 0 );

			createIgnoreCountEditor( getFieldEditorParent() );
		}
		catch( CoreException ce )
		{
			CDebugUIPlugin.log( ce );
		}
	}

	/**
	 * Method createTypeSpecificLabelFieldEditors.
	 * @param breakpoint
	 */
	private void createTypeSpecificLabelFieldEditors( ICBreakpoint breakpoint )
	{
		if ( breakpoint instanceof ICFunctionBreakpoint )
		{
			ICFunctionBreakpoint fbrkpt = (ICFunctionBreakpoint)breakpoint;
			String function = CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Not_available"); //$NON-NLS-1$
			try
			{
				function = fbrkpt.getFunction();
			}
			catch( CoreException e )
			{
			}
			catch( NumberFormatException e )
			{
			}
			if ( function != null )
			{
				addField( createLabelEditor( getFieldEditorParent(), CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Function_name"), function ) ); //$NON-NLS-1$
			}
			setTitle( CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Function_Breakpoint_Properties") ); //$NON-NLS-1$
		}
		else if ( breakpoint instanceof ICAddressBreakpoint )
		{
			ICAddressBreakpoint abrkpt = (ICAddressBreakpoint)breakpoint;
			String address = "Not available";
			try
			{
				address = CDebugUtils.toHexAddressString( Long.parseLong( abrkpt.getAddress() ) );
			}
			catch( CoreException e )
			{
			}
			catch( NumberFormatException e )
			{
			}
			if ( address != null )
			{
				addField( createLabelEditor( getFieldEditorParent(), CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Address"), address ) ); //$NON-NLS-1$
			}
			setTitle( CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Address_Breakpoint_Properties") ); //$NON-NLS-1$
		}
		else if ( breakpoint instanceof ILineBreakpoint )
		{
			String fileName = breakpoint.getMarker().getResource().getLocation().toOSString();
			if ( fileName != null )
			{
				addField( createLabelEditor( getFieldEditorParent(), CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.File"), fileName ) ); //$NON-NLS-1$
			}
			setTitle( CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Line_Breakpoint_Properties") ); //$NON-NLS-1$
			ILineBreakpoint lBreakpoint = (ILineBreakpoint)breakpoint;
			StringBuffer lineNumber = new StringBuffer( 4 );
			try
			{
				int lNumber = lBreakpoint.getLineNumber();
				if ( lNumber > 0 )
				{
					lineNumber.append( lNumber );
				}
			}
			catch( CoreException ce )
			{
				CDebugUIPlugin.log( ce );
			}
			if ( lineNumber.length() > 0 )
			{
				addField( createLabelEditor( getFieldEditorParent(), CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Line_Number"), lineNumber.toString() ) ); //$NON-NLS-1$
			}
		}
		else if ( breakpoint instanceof ICWatchpoint )
		{
			String projectName = breakpoint.getMarker().getResource().getLocation().toOSString();
			if ( projectName != null )
			{
				addField( createLabelEditor( getFieldEditorParent(), CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Project"), projectName ) ); //$NON-NLS-1$
			}
			ICWatchpoint watchpoint = (ICWatchpoint)breakpoint;
			String title = ""; //$NON-NLS-1$
			String expression = ""; //$NON-NLS-1$
			try
			{
				if ( watchpoint.isReadType() && !watchpoint.isWriteType() )
					title = CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Read_Watchpoint_Properties"); //$NON-NLS-1$
				else if ( !watchpoint.isReadType() && watchpoint.isWriteType() )
					title = CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Watchpoint_Properties"); //$NON-NLS-1$
				else
					title = CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Access_Watchpoint_Properties"); //$NON-NLS-1$
				expression = watchpoint.getExpression();
			}
			catch( CoreException ce )
			{
				CDebugUIPlugin.log( ce );
			}
			setTitle( title );
			addField( createLabelEditor( getFieldEditorParent(), CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Expression_To_Watch"), expression ) ); //$NON-NLS-1$
		}
	}

	protected void createConditionEditor( Composite parent )
	{
		fCondition = new BreakpointStringFieldEditor( CBreakpointPreferenceStore.CONDITION, CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Condition"), parent ); //$NON-NLS-1$
		fCondition.setEmptyStringAllowed( true );
		fCondition.setErrorMessage( CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Invalid_condition") ); //$NON-NLS-1$
		addField( fCondition );
	}

	protected void createIgnoreCountEditor( Composite parent )
	{
		fIgnoreCount = new BreakpointIntegerFieldEditor( CBreakpointPreferenceStore.IGNORE_COUNT, CDebugUIPlugin.getResourceString("internal.ui.actions.CBreakpointPreferencePage.Ignore_Count"), parent ); //$NON-NLS-1$
		fIgnoreCount.setValidRange( 0, Integer.MAX_VALUE );
		fIgnoreCountTextControl = fIgnoreCount.getTextControl( parent );
		try
		{
			fIgnoreCountTextControl.setEnabled( getBreakpoint().getIgnoreCount() >= 0 );
		}
		catch (CoreException ce)
		{
			CDebugUIPlugin.log( ce );
		}
		addField( fIgnoreCount );
	}

	protected FieldEditor createLabelEditor( Composite parent, String title, String value )
	{
		return new LabelFieldEditor( parent, title, value );
	}

	protected ICBreakpoint getBreakpoint() 
	{
		return fBreakpoint;
	}

	protected void setBreakpoint( ICBreakpoint breakpoint ) 
	{
		fBreakpoint = breakpoint;
	}
}
