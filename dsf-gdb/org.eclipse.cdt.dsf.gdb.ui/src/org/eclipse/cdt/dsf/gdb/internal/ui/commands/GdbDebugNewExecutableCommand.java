/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.IDebugNewExecutableHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

public class GdbDebugNewExecutableCommand extends AbstractDebugCommand implements IDebugNewExecutableHandler {

	private class PromptJob extends UIJob {

		private DataRequestMonitor<PromptInfo> fRequestMonitor;
		private boolean fRemote = false;
	
		private PromptJob( boolean remote, DataRequestMonitor<PromptInfo> rm ) {
			super( "New Executable Prompt Job" ); //$NON-NLS-1$
			fRemote = remote;
			fRequestMonitor = rm;
		}

		@Override
		public IStatus runInUIThread( IProgressMonitor monitor ) {
			int flags = ( fRemote ) ? NewExecutableDialog.REMOTE : 0;
			NewExecutableDialog dialog = new NewExecutableDialog( GdbUIPlugin.getShell(), flags );
			final boolean canceled = dialog.open() == Window.CANCEL;
			final PromptInfo info = dialog.getInfo();
			fExecutor.execute( new DsfRunnable() {
				
				@Override
				public void run() {
					if ( canceled )
						fRequestMonitor.cancel();
					else
						fRequestMonitor.setData( info );
					fRequestMonitor.done();
				}
			} );
			return Status.OK_STATUS;
		}
	}

	private class NewExecutableDialog extends TitleAreaDialog {

		private static final int REMOTE = 0x1;

		private int fFlags = 0;
		private PromptInfo fInfo = null;
		
		private Text fHostBinaryText;
		private Text fTargetBinaryText;
		private Text fArgumentsText;

		private NewExecutableDialog( Shell parentShell, int flags ) {
			super( parentShell );
			setShellStyle( getShellStyle() | SWT.RESIZE );
			fFlags = flags;
		}

		@Override
		protected Control createContents( Composite parent ) {
			Control control = super.createContents( parent );
			validate();
			return control;
		}

		@Override
		protected Control createDialogArea( Composite parent ) {
			boolean remote = (fFlags & REMOTE) > 0;

			getShell().setText( Messages.GdbDebugNewExecutableCommand_Debug_New_Executable ); 
			setTitle( Messages.GdbDebugNewExecutableCommand_Select_Binary );
			String message = ( remote ) ? 
					Messages.GdbDebugNewExecutableCommand_Select_binaries_on_host_and_target :
					Messages.GdbDebugNewExecutableCommand_Select_binary_and_specify_arguments;
			setMessage( message );

			Composite control = (Composite)super.createDialogArea( parent );
			Composite comp = new Composite( control, SWT.NONE );
			GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
			GridLayout layout = new GridLayout( 3, false );
			comp.setLayout( layout );
			comp.setLayoutData( gd );
			
			new Label( comp, SWT.None ).setText( remote ? Messages.GdbDebugNewExecutableCommand_Binary_on_host : Messages.GdbDebugNewExecutableCommand_Binary );
			fHostBinaryText = new Text( comp, SWT.BORDER );
			fHostBinaryText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
			fHostBinaryText.addModifyListener( new ModifyListener() {
				
				@Override
				public void modifyText( ModifyEvent e ) {
					validate();
				}
			} );
			Button browseButton = new Button( comp, SWT.PUSH );
			browseButton.setText( Messages.GdbDebugNewExecutableCommand_Browse );
			browseButton.setFont( JFaceResources.getDialogFont() );
			setButtonLayoutData( browseButton );
			browseButton.addSelectionListener( new SelectionAdapter() {

				@Override
				public void widgetSelected( SelectionEvent e ) {
					FileDialog dialog = new FileDialog( getShell() );
					dialog.setFileName( fHostBinaryText.getText() );
					String result = dialog.open();
					if ( result != null ) {
						fHostBinaryText.setText( result );
					}
				}
			} );
			
			if ( remote ) {
				new Label( comp, SWT.None ).setText( Messages.GdbDebugNewExecutableCommand_Binary_on_target );
				fTargetBinaryText = new Text( comp, SWT.BORDER );
				fTargetBinaryText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
				fTargetBinaryText.addModifyListener( new ModifyListener() {
					
					@Override
					public void modifyText( ModifyEvent e ) {
						validate();
					}
				} );
			}

			new Label( comp, SWT.None ).setText( Messages.GdbDebugNewExecutableCommand_Arguments );
			fArgumentsText = new Text( comp, SWT.BORDER );
			fArgumentsText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

			return control;
		}

		@Override
		protected void okPressed() {
			String targetPath = ( fTargetBinaryText != null ) ? fTargetBinaryText.getText().trim() : null;
			String args = fArgumentsText.getText().trim();
			fInfo = new PromptInfo( fHostBinaryText.getText().trim(), targetPath, args );
			super.okPressed();
		}

		private PromptInfo getInfo() {
			return fInfo;
		}
		
		private void validate() {
			String message = null;
			String hostBinary = fHostBinaryText.getText().trim();
			File file = new File( hostBinary );
			if ( !file.exists() )
				message = Messages.GdbDebugNewExecutableCommand_Binary_file_does_not_exist;
			else if ( file.isDirectory() )
				message = Messages.GdbDebugNewExecutableCommand_Invalid_binary;
			if ( fTargetBinaryText != null ) {
				if ( fTargetBinaryText.getText().trim().length() == 0 )
					message = Messages.GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified;
			}
			setErrorMessage( message );
			getButton( IDialogConstants.OK_ID ).setEnabled( message == null );
		}
	}

	private class PromptInfo {
		private String fHostPath;
		private String fTargetPath;
		private String fArguments;

		private PromptInfo( String hostPath, String targetPath, String args ) {
			super();
			fHostPath = hostPath;
			fTargetPath = targetPath;
			fArguments = args;
		}
		
		String getHostPath() {
			return fHostPath;
		}
		
		String getTargetPath() {
			return fTargetPath;
		}
		
		String getArguments() {
			return fArguments;
		}
	}

	private final GdbLaunch fLaunch;
	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fTracker;

	public GdbDebugNewExecutableCommand( DsfSession session, GdbLaunch launch ) {
		super();
		fLaunch = launch;
		fExecutor = session.getExecutor();
		fTracker = new DsfServicesTracker( GdbUIPlugin.getBundleContext(), session.getId() );
	}

	public boolean canDebugNewExecutable() {
		
       	Query<Boolean> canDebugQuery = new Query<Boolean>() {
            @Override
			public void execute( DataRequestMonitor<Boolean> rm ) {
				IProcesses procService = fTracker.getService( IProcesses.class );
				IGDBBackend backend = fTracker.getService( IGDBBackend.class );
				ICommandControlService commandControl = fTracker.getService( ICommandControlService.class );

				if ( procService == null || commandControl == null || backend == null ) {
					rm.setData( false );
					rm.done();
					return;
				}
				procService.isDebugNewProcessSupported( commandControl.getContext(), rm );
			}            
       	};
		try {
			fExecutor.execute( canDebugQuery );
			return canDebugQuery.get();
		}
		catch( InterruptedException e ) {
		}
		catch( ExecutionException e ) {
		}
		catch( RejectedExecutionException e ) {
			// Can be thrown if the session is shutdown
		}
		return false;
	}

	public void debugNewExecutable( final RequestMonitor rm ) {
		IGDBBackend backend = fTracker.getService( IGDBBackend.class );
		final IProcesses procService = fTracker.getService( IProcesses.class );
		final ICommandControlService commandControl = fTracker.getService( ICommandControlService.class );
		if ( backend == null || procService == null || commandControl == null ) {
			rm.setStatus( new Status( IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Service is not available" ) ); //$NON-NLS-1$
			rm.done();
			return;
		}

		PromptJob job = new PromptJob( 
			backend.getSessionType() == SessionType.REMOTE,
			new DataRequestMonitor<PromptInfo>( fExecutor, rm ){
				
				@Override
				protected void handleCancel() {
					rm.cancel();
					rm.done();
				};

				@Override
				protected void handleSuccess() {
					try {
						@SuppressWarnings( "unchecked" )
						final Map<String, Object> attributes = 
								new HashMap<String, Object>( getLaunchConfiguration().getAttributes() );
						attributes.put( IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REMOTE_BINARY, getData().getTargetPath() );
						attributes.put( ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getData().getArguments() );
						procService.debugNewProcess(
								commandControl.getContext(), 
								getData().getHostPath(), 
								attributes, 
								new ImmediateDataRequestMonitor<IDMContext>( rm ) );
					}
					catch( CoreException e ) {
						rm.setStatus( e.getStatus() );
						rm.done();
					}
				};
			} );
		job.schedule();
	}

	@Override
	protected void doExecute( Object[] targets, IProgressMonitor monitor, IRequest request ) throws CoreException {
		Query<Boolean> query = new Query<Boolean>() {

			@Override
			protected void execute( DataRequestMonitor<Boolean> rm ) {
				debugNewExecutable( rm );
			}
		};
		try {
			fExecutor.execute( query );
			query.get();
		}
		catch( InterruptedException e ) {
		}
		catch( ExecutionException e ) {
		}
		catch( CancellationException e ) {
			// Nothing to do, just ignore the command since the user
			// cancelled it.
		}
		catch( RejectedExecutionException e ) {
			// Can be thrown if the session is shutdown
		}
	}

	@Override
	protected boolean isExecutable( Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request ) throws CoreException {
		return canDebugNewExecutable();
	}

	@Override
	protected Object getTarget( Object element ) {
		if ( element instanceof GdbLaunch || element instanceof IDMVMContext )
			return element;
		return null;
	}

	public void dispose() {
		fTracker.dispose();
	}

	private ILaunchConfiguration getLaunchConfiguration() {
		return fLaunch.getLaunchConfiguration();
	}
}
