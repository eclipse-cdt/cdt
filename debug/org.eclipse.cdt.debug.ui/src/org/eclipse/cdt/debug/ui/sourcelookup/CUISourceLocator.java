/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceMode;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLocator;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceManager;
import org.eclipse.cdt.debug.internal.ui.CDebugImageDescriptorRegistry;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.debug.internal.ui.editors.FileNotFoundElement;
import org.eclipse.cdt.debug.internal.ui.editors.NoSymbolOrSourceElement;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * 
 * A source locator that prompts the user to find source when source cannot
 * be found on the current source lookup path.
 * 
 * @since Sep 24, 2002
 */
public class CUISourceLocator implements IAdaptable
{
	public class SourceSelectionDialog extends ListDialog
	{
		private SelectionButtonDialogField fAlwaysUseThisFileButton = new SelectionButtonDialogField( SWT.CHECK );

		public SourceSelectionDialog( Shell parent )
		{
			super( parent );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent )
		{
			Composite comp = ControlFactory.createComposite( parent, 1 );
			super.createDialogArea( comp );
			Composite comp1 = ControlFactory.createComposite( comp, 1 );
			fAlwaysUseThisFileButton.setLabelText( "Always map to the selection" );
			fAlwaysUseThisFileButton.doFillIntoGrid( comp1, 1 );
			return comp;
		}

		public boolean alwaysMapToSelection()
		{
			return fAlwaysUseThisFileButton.isSelected();
		}
	}

	public class SourceElementLabelProvider extends LabelProvider
	{
		protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

		public SourceElementLabelProvider()
		{
			super();
		}

		public String getText(Object element)
		{
			if ( element instanceof IFile )
				return ((IFile)element).getFullPath().toString();
			if ( element instanceof FileStorage )
				return ((FileStorage)element).getFullPath().toOSString();
			return super.getText(element);
		}

		public Image getImage( Object element )
		{
			if ( element instanceof IFile )
				return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_WORKSPACE_SOURCE_FILE );
			if ( element instanceof FileStorage )
				return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_EXTERNAL_SOURCE_FILE );
			return super.getImage( element );
		}
	}

	/**
	 * The project being debugged.
	 */
	private IProject fProject = null; 
	
	/**
	 * Underlying source locator.
	 */
	private CSourceManager fSourceLocator;

	private HashMap fFramesToSource = null;
	private HashMap fNamesToSource = null;

	/**
	 * Constructor for CUISourceLocator.
	 */
	public CUISourceLocator( IProject project )
	{
		fProject = project;
		fSourceLocator = new CSourceManager( new CSourceLocator( project ) );
	}

	public Object getSourceElement( IStackFrame stackFrame )
	{
		Object res = cacheLookup( stackFrame );
		if ( res == null )
		{
			res = fSourceLocator.getSourceElement( stackFrame );
			if ( res instanceof List )
			{
				List list = (List)res;
				if ( list.size() != 0 )
				{
					SourceSelectionDialog dialog = createSourceSelectionDialog( list );
					dialog.open();
					Object[] objs = dialog.getResult();
					res = ( objs != null && objs.length > 0 ) ? objs[0] : null;
					if ( res != null )
						cacheSourceElement( stackFrame, res, dialog.alwaysMapToSelection() );
				}
				else
					res = null;
			}
		}
		if ( res == null )
		{
			IStackFrameInfo frameInfo = (IStackFrameInfo)stackFrame.getAdapter( IStackFrameInfo.class );
			if ( frameInfo != null && frameInfo.getFile() != null && frameInfo.getFile().length() > 0 )
			{
				res = new FileNotFoundElement( stackFrame );
			}
			else // don't show in editor
			{
				res = new NoSymbolOrSourceElement( stackFrame );
			}
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( fSourceLocator != null )
		{
			if ( adapter.equals( ICSourceLocator.class ) )
				return fSourceLocator;
			if ( adapter.equals( IResourceChangeListener.class ) && fSourceLocator instanceof IAdaptable )
				return ((IAdaptable)fSourceLocator).getAdapter( IResourceChangeListener.class );
			if ( adapter.equals( ISourceMode.class ) )
				return fSourceLocator;
		}
		return null;
	}

	public IProject getProject()
	{
		return fProject;
	}

	protected void saveChanges( ILaunchConfiguration configuration, IPersistableSourceLocator locator )
	{
		try
		{
			ILaunchConfigurationWorkingCopy copy = configuration.copy( configuration.getName() );
			copy.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			copy.doSave();
		}
		catch( CoreException e )
		{
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
		}
	}

	private SourceSelectionDialog createSourceSelectionDialog( List list )
	{
		SourceSelectionDialog dialog = new SourceSelectionDialog( CDebugUIPlugin.getActiveWorkbenchShell() );
		dialog.setInput( list.toArray() );
		dialog.setContentProvider( new ArrayContentProvider() );
		dialog.setLabelProvider( new SourceElementLabelProvider() );
		dialog.setTitle( "Selection needed" );
		dialog.setMessage( "Debugger has found multiple files with the same name.\nPlease select one associated with the selected stack frame." );
		dialog.setInitialSelections( new Object[] { list.get( 0 ) } );
		return dialog;
	}

	private void cacheSourceElement( IStackFrame frame, Object sourceElement, boolean alwaysMapToSelection )
	{
		if ( alwaysMapToSelection )
		{
			String name = getFileName( frame );
			if ( name != null )
			{
				if ( fNamesToSource == null ) 
					fNamesToSource = new HashMap();
				fNamesToSource.put( name, sourceElement );
			}
		}
		else
		{
			if ( fFramesToSource == null ) 
				fFramesToSource = new HashMap();
			fFramesToSource.put( frame, sourceElement );
		}
	}

	private Object cacheLookup( IStackFrame frame )
	{
		String name = getFileName( frame );
		if ( name != null && fNamesToSource != null )
		{
			Object result = fNamesToSource.get( name );
			if ( result != null )
				return result;
		}
		return ( fFramesToSource != null ) ? fFramesToSource.get( frame ) : null;
	}

	private String getFileName( IStackFrame frame )
	{
		IStackFrameInfo frameInfo = (IStackFrameInfo)frame.getAdapter( IStackFrameInfo.class );
		if ( frameInfo != null )
		{
			String name = frameInfo.getFile();
			if ( name != null && name.trim().length() > 0 )
				return name.trim();
		}
		return null;
	}
}
