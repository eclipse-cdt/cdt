/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICBreakpoint;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
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
			setErrorMessage( "Ignore count must be a positive integer" );
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

	private Text fConditionTextControl;
	private BreakpointStringFieldEditor fCondition;

	private Text fIgnoreCountTextControl;
	private BreakpointIntegerFieldEditor fIgnoreCount;
	
	private ICBreakpoint fBreakpoint;

	/**
	 * Constructor for CBreakpointPreferencePage.
	 * @param style
	 */
	public CBreakpointPreferencePage( ICBreakpoint breakpoint )
	{
		super( GRID );
		setBreakpoint( breakpoint );
	}

	/**
	 * Constructor for CBreakpointPreferencePage.
	 * @param title
	 * @param style
	 */
	public CBreakpointPreferencePage(String title, int style)
	{
		super(title, style);
	}

	/**
	 * Constructor for CBreakpointPreferencePage.
	 * @param title
	 * @param image
	 * @param style
	 */
	public CBreakpointPreferencePage(
		String title,
		ImageDescriptor image,
		int style)
	{
		super(title, image, style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors()
	{
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
