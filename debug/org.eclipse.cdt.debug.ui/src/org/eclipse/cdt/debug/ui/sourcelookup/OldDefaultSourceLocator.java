/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.ui.sourcelookup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.SourceLookupFactory;
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Old default source locator. We keep it for migration purposes.
 */
public class OldDefaultSourceLocator implements IPersistableSourceLocator, IAdaptable {

	public class SourceSelectionDialog extends ListDialog {

		private SelectionButtonDialogField fAlwaysUseThisFileButton = new SelectionButtonDialogField( SWT.CHECK );

		public SourceSelectionDialog( Shell parent ) {
			super( parent );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea( Composite parent ) {
			Composite comp = ControlFactory.createComposite( parent, 1 );
			super.createDialogArea( comp );
			Composite comp1 = ControlFactory.createComposite( comp, 1 );
			fAlwaysUseThisFileButton.setLabelText( SourceLookupMessages.getString( "OldDefaultSourceLocator.0" ) ); //$NON-NLS-1$
			fAlwaysUseThisFileButton.doFillIntoGrid( comp1, 1 );
			return comp;
		}

		public boolean alwaysMapToSelection() {
			return fAlwaysUseThisFileButton.isSelected();
		}
	}

	public class SourceElementLabelProvider extends LabelProvider {

		protected CDebugImageDescriptorRegistry fDebugImageRegistry = CDebugUIPlugin.getImageDescriptorRegistry();

		public SourceElementLabelProvider() {
			super();
		}

		public String getText( Object element ) {
			if ( element instanceof IFile )
				return ((IFile)element).getFullPath().toString();
			if ( element instanceof FileStorage )
				return ((FileStorage)element).getFullPath().toOSString();
			return super.getText( element );
		}

		public Image getImage( Object element ) {
			if ( element instanceof IFile )
				return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_WORKSPACE_SOURCE_FILE );
			if ( element instanceof FileStorage )
				return fDebugImageRegistry.get( CDebugImages.DESC_OBJS_EXTERNAL_SOURCE_FILE );
			return super.getImage( element );
		}
	}

	/**
	 * Identifier for the 'Default C/C++ Source Locator' extension (value <code>"org.eclipse.cdt.debug.ui.DefaultSourceLocator"</code>).
	 */
	public static final String ID_DEFAULT_SOURCE_LOCATOR = CDebugUIPlugin.getUniqueIdentifier() + ".DefaultSourceLocator"; //$NON-NLS-1$

	// to support old configurations
	public static final String ID_OLD_DEFAULT_SOURCE_LOCATOR = "org.eclipse.cdt.launch" + ".DefaultSourceLocator"; //$NON-NLS-1$ //$NON-NLS-2$

	protected static final String ELEMENT_NAME = "PromptingSourceLocator"; //$NON-NLS-1$

	private static final String ATTR_PROJECT = "project"; //$NON-NLS-1$

	private static final String ATTR_MEMENTO = "memento"; //$NON-NLS-1$

	/**
	 * Underlying source locator.
	 */
	private ICSourceLocator fSourceLocator;

	private HashMap fFramesToSource = null;

	private HashMap fNamesToSource = null;

	public OldDefaultSourceLocator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException {
		if ( getCSourceLocator() != null ) {
			Document document = null;
			Throwable ex = null;
			try {
				document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element element = document.createElement( ELEMENT_NAME );
				document.appendChild( element );
				element.setAttribute( ATTR_PROJECT, getCSourceLocator().getProject().getName() );
				IPersistableSourceLocator psl = getPersistableSourceLocator();
				if ( psl != null ) {
					element.setAttribute( ATTR_MEMENTO, psl.getMemento() );
				}
				return CDebugUtils.serializeDocument( document );
			}
			catch( ParserConfigurationException e ) {
				ex = e;
			}
			catch( IOException e ) {
				ex = e;
			}
			catch( TransformerException e ) {
				ex = e;
			}
			abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.1" ), ex ); //$NON-NLS-1$
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento( String memento ) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();
			if ( !root.getNodeName().equalsIgnoreCase( ELEMENT_NAME ) ) {
				abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.2" ), null ); //$NON-NLS-1$
			}
			String projectName = root.getAttribute( ATTR_PROJECT );
			String data = root.getAttribute( ATTR_MEMENTO );
			if ( isEmpty( projectName ) ) {
				abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.3" ), null ); //$NON-NLS-1$
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( getCSourceLocator() == null )
				setCSourceLocator( SourceLookupFactory.createSourceLocator( project ) );
			if ( getCSourceLocator().getProject() != null && !getCSourceLocator().getProject().equals( project ) )
				return;
			if ( project == null || !project.exists() || !project.isOpen() )
				abort( MessageFormat.format( SourceLookupMessages.getString( "OldDefaultSourceLocator.4" ), new String[]{ projectName } ), null ); //$NON-NLS-1$
			IPersistableSourceLocator psl = getPersistableSourceLocator();
			if ( psl != null )
				psl.initializeFromMemento( data );
			else
				abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.5" ), null );  //$NON-NLS-1$
			return;
		}
		catch( ParserConfigurationException e ) {
			ex = e;
		}
		catch( SAXException e ) {
			ex = e;
		}
		catch( IOException e ) {
			ex = e;
		}
		abort( SourceLookupMessages.getString( "OldDefaultSourceLocator.6" ), ex ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults( ILaunchConfiguration configuration ) throws CoreException {
		setCSourceLocator( SourceLookupFactory.createSourceLocator( getProject( configuration ) ) );
		String memento = configuration.getAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, "" ); //$NON-NLS-1$
		if ( !isEmpty( memento ) )
			initializeFromMemento( memento );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( getCSourceLocator() instanceof IAdaptable ) {
			if ( adapter.equals( ICSourceLocator.class ) ) {
				return ((IAdaptable)getCSourceLocator()).getAdapter( adapter );
			}
			if ( adapter.equals( IResourceChangeListener.class ) ) {
				return ((IAdaptable)getCSourceLocator()).getAdapter( adapter );
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(org.eclipse.debug.core.model.IStackFrame)
	 */
	public Object getSourceElement( IStackFrame stackFrame ) {
		Object res = cacheLookup( stackFrame );
		if ( res == null ) {
			res = getCSourceLocator().getSourceElement( stackFrame );
			if ( res instanceof List ) {
				List list = (List)res;
				if ( list.size() != 0 ) {
					SourceSelectionDialog dialog = createSourceSelectionDialog( list );
					dialog.open();
					Object[] objs = dialog.getResult();
					res = (objs != null && objs.length > 0) ? objs[0] : null;
					if ( res != null )
						cacheSourceElement( stackFrame, res, dialog.alwaysMapToSelection() );
				}
				else
					res = null;
			}
		}
		if ( res == null ) {
			if ( stackFrame instanceof ICStackFrame && !isEmpty( ((ICStackFrame)stackFrame).getFile() ) ) {
				res = new FileNotFoundElement( stackFrame );
			}
			else // don't show in editor
			{
				res = new NoSymbolOrSourceElement( stackFrame );
			}
		}
		return res;
	}

	protected void saveChanges( ILaunchConfiguration configuration, IPersistableSourceLocator locator ) {
		try {
			ILaunchConfigurationWorkingCopy copy = configuration.copy( configuration.getName() );
			copy.setAttribute( ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, locator.getMemento() );
			copy.doSave();
		}
		catch( CoreException e ) {
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
		}
	}

	private SourceSelectionDialog createSourceSelectionDialog( List list ) {
		SourceSelectionDialog dialog = new SourceSelectionDialog( CDebugUIPlugin.getActiveWorkbenchShell() );
		dialog.setInput( list.toArray() );
		dialog.setContentProvider( new ArrayContentProvider() );
		dialog.setLabelProvider( new SourceElementLabelProvider() );
		dialog.setTitle( SourceLookupMessages.getString( "OldDefaultSourceLocator.7" ) ); //$NON-NLS-1$
		dialog.setMessage( SourceLookupMessages.getString( "OldDefaultSourceLocator.8" ) ); //$NON-NLS-1$
		dialog.setInitialSelections( new Object[]{ list.get( 0 ) } );
		return dialog;
	}

	private void cacheSourceElement( IStackFrame frame, Object sourceElement, boolean alwaysMapToSelection ) {
		if ( alwaysMapToSelection ) {
			String name = getFileName( frame );
			if ( name != null ) {
				if ( fNamesToSource == null )
					fNamesToSource = new HashMap();
				fNamesToSource.put( name, sourceElement );
			}
		}
		else {
			if ( fFramesToSource == null )
				fFramesToSource = new HashMap();
			fFramesToSource.put( frame, sourceElement );
		}
	}

	private Object cacheLookup( IStackFrame frame ) {
		String name = getFileName( frame );
		if ( name != null && fNamesToSource != null ) {
			Object result = fNamesToSource.get( name );
			if ( result != null )
				return result;
		}
		return (fFramesToSource != null) ? fFramesToSource.get( frame ) : null;
	}

	private String getFileName( IStackFrame frame ) {
		if ( frame instanceof ICStackFrame ) {
			String name = ((ICStackFrame)frame).getFile();
			if ( !isEmpty( name ) )
				return name.trim();
		}
		return null;
	}

	private ICSourceLocator getCSourceLocator() {
		return fSourceLocator;
	}

	private void setCSourceLocator( ICSourceLocator locator ) {
		fSourceLocator = locator;
	}

	private IPersistableSourceLocator getPersistableSourceLocator() {
		ICSourceLocator sl = getCSourceLocator();
		return (sl instanceof IPersistableSourceLocator) ? (IPersistableSourceLocator)sl : null;
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException {
		IStatus s = new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), 0, message, e );
		throw new CoreException( s );
	}

	private boolean isEmpty( String string ) {
		return string == null || string.trim().length() == 0;
	}

	private IProject getProject( ILaunchConfiguration configuration ) throws CoreException {
		String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
		if ( !isEmpty( projectName ) ) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project.exists() ) {
				return project;
			}
		}
		abort( MessageFormat.format( SourceLookupMessages.getString( "OldDefaultSourceLocator.9" ), new String[]{ projectName } ), null ); //$NON-NLS-1$
		return null;
	}
}
