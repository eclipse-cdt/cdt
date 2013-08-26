/*******************************************************************************
 * Copyright (c) 2008,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui.dialogs;

import java.util.Vector;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.internal.remote.ui.DeferredFileStore;
import org.eclipse.internal.remote.ui.RemoteContentProvider;
import org.eclipse.internal.remote.ui.RemoteResourceComparator;
import org.eclipse.internal.remote.ui.RemoteUIImages;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.PendingUpdateAdapter;

/**
 * Generic file/directory browser for remote resources.
 * 
 * @author greg
 * 
 */
public class RemoteResourceBrowser extends Dialog {
	public final static String EMPTY_STRING = ""; //$NON-NLS-1$
	public final static int FILE_BROWSER = 0x01;
	public final static int DIRECTORY_BROWSER = 0x02;
	public static final int SINGLE = 0x01;
	public static final int MULTI = 0x02;

	private final static int widthHint = 300;
	private final static int heightHint = 300;

	private Tree tree = null;
	private TreeViewer treeViewer;
	private Text remotePathText;
	private Button okButton;
	private Button upButton;
	private Button newFolderButton;
	private RemoteConnectionWidget fRemoteConnectionWidget;

	private int browserType;
	private String dialogTitle;
	private String dialogLabel;

	private boolean showConnections = false;
	private boolean showHidden = false;
	private String remotePath = EMPTY_STRING;
	private String remotePaths[];
	private String fInitialPath;
	private IPath fRootPath;
	private IRemoteFileManager fFileMgr;
	private IRemoteConnection fConnection;
	private final IRemoteUIConnectionManager fUIConnMgr;
	private int optionFlags = SINGLE;

	public RemoteResourceBrowser(IRemoteServices services, IRemoteConnection conn, Shell parent, int flags) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());
		fConnection = conn;
		this.optionFlags = flags;
		if (conn == null) {
			showConnections = true;
		}
		fUIConnMgr = RemoteUIServices.getRemoteUIServices(services).getUIConnectionManager();
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		setType(FILE_BROWSER | DIRECTORY_BROWSER);
	}

	/**
	 * Change the viewers input. Called when a new connection is selected.
	 * 
	 * @param conn
	 *            new connection
	 * @return true if input successfully changed
	 */
	private boolean changeInput(final IRemoteConnection conn) {
		if (conn == null) {
			return false;
		}

		fUIConnMgr.openConnectionWithProgress(getShell(), null, conn);
		if (!conn.isOpen()) {
			return false;
		}

		fFileMgr = conn.getFileManager();
		if (fFileMgr != null) {
			/*
			 * Note: the call to findInitialPath must happen before the
			 * treeViewer input is set or the treeViewer fails. No idea why this
			 * is.
			 */
			String cwd = conn.getWorkingDirectory();
			IPath initial = findInitialPath(cwd, fInitialPath);

			// TODO: not platform independent - needs IRemotePath
			setRoot(initial.toString());

			fConnection = conn;
			return true;
		}

		return false;
	}

	/**
	 * When a new connection is selected, make sure it is open before using it.
	 */
	private void connectionSelected() {
		/*
		 * Make sure the connection is open before we try and read from the
		 * connection.
		 */
		final IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
		if (!changeInput(conn)) {
			/*
			 * Reset combo back to the previous selection
			 */
			fRemoteConnectionWidget.setConnection(fConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets
	 * .Composite, int, java.lang.String, boolean)
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			okButton = button;
		}
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(dialogTitle);
		remotePathText.setText(remotePath);
		if (!showConnections) {
			changeInput(fConnection);
		}
		return contents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);

		final Composite dialogComp = new Composite(main, SWT.NONE);
		dialogComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		dialogComp.setLayout(layout);

		if (showConnections) {
			fRemoteConnectionWidget = new RemoteConnectionWidget(dialogComp, SWT.NONE, null,
					RemoteConnectionWidget.FLAG_NO_LOCAL_SELECTION, null);
			fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
			fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					connectionSelected();
					updateDialog();
				}
			});
		}

		Label label = new Label(dialogComp, SWT.NONE);
		label.setText(dialogLabel);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		remotePathText = new Text(dialogComp, SWT.BORDER | SWT.SINGLE);
		remotePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				remotePath = remotePathText.getText();
				updateDialog();
			}
		});
		remotePathText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				getShell().setDefaultButton(null); // allow text widget to receive SWT.DefaultSelection event
			}

			public void focusLost(FocusEvent e) {
				getShell().setDefaultButton(okButton);
			}
		});
		remotePathText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				remotePathText.setSelection(remotePathText.getText().length());
				setRoot(remotePathText.getText());
			}

		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = widthHint;
		remotePathText.setLayoutData(gd);

		upButton = new Button(dialogComp, SWT.PUSH | SWT.FLAT);
		upButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_UP_NAV));
		upButton.setToolTipText(Messages.RemoteResourceBrowser_UpOneLevel);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!fRootPath.isRoot()) {
					setRoot(fRootPath.removeLastSegments(1).toOSString());
				}
			}
		});
		// new folder: See Bug 396334
		newFolderButton = new Button(dialogComp, SWT.PUSH | SWT.FLAT);
		newFolderButton.setImage(RemoteUIImages.get(RemoteUIImages.IMG_ELCL_NEW_FOLDER));
		newFolderButton.setToolTipText(Messages.RemoteResourceBrowser_NewFolder);
		newFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String pathText = remotePathText.getText();
				String newname = "/newfolder"; //$NON-NLS-1$  
				remotePathText.setText(pathText + newname);
				remotePathText.setSelection(pathText.length() + 1, pathText.length() + newname.length());
				remotePathText.setFocus();
			}
		});

		if ((optionFlags & MULTI) == MULTI) {
			tree = new Tree(main, SWT.MULTI | SWT.BORDER);
		} else {
			tree = new Tree(main, SWT.SINGLE | SWT.BORDER);
		}

		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 4;
		// see bug 158380
		gd.heightHint = Math.max(main.getParent().getSize().y, heightHint);
		tree.setLayoutData(gd);

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new RemoteContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					Object element = ss.getFirstElement();
					if (element instanceof DeferredFileStore) {
						DeferredFileStore dfs = (DeferredFileStore) element;
						remotePathText.setText(dfs.getFileStore().toURI().getPath());
					}
					Vector<String> selectedPaths = new Vector<String>(ss.size());
					for (Object currentSelection : ss.toArray()) {
						if (currentSelection instanceof DeferredFileStore) {
							selectedPaths.add(((DeferredFileStore) currentSelection).getFileStore().toURI().getPath());
						}
					}
					remotePaths = selectedPaths.toArray(new String[0]);
				}
			}
		});
		treeViewer.setComparator(new RemoteResourceComparator());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object o = s.getFirstElement();
				if (treeViewer.isExpandable(o)) {
					treeViewer.setExpandedState(o, !treeViewer.getExpandedState(o));
				}
			}

		});
		if (browserType == DIRECTORY_BROWSER) {
			treeViewer.addFilter(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if ((element instanceof DeferredFileStore)) {
						return ((DeferredFileStore) element).isContainer();
					}
					return element instanceof PendingUpdateAdapter;
				}
			});
		}

		final Button showHiddenButton = new Button(main, SWT.CHECK);
		showHiddenButton.setText(Messages.RemoteResourceBrowser_Show_hidden_files);
		showHiddenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showHidden = showHiddenButton.getSelection();
				setRoot(fRootPath.toString());
			}
		});

		updateDialog();

		return main;
	}

	/**
	 * Determine the initial path for the browser. If the initial path is not
	 * supplied or does not exist on the remote machine, then the initial path
	 * will be the cwd.
	 * 
	 * @param cwd
	 * @param initialPath
	 * @return initial path
	 */
	private IPath findInitialPath(String cwd, String initialPath) {
		if (initialPath != null) {
			IPath path = new Path(initialPath);
			if (!path.isAbsolute()) {
				path = new Path(cwd).append(path);
			}
			if (fFileMgr.getResource(path.toString()).fetchInfo().exists()) {
				return path;
			}
		}
		return new Path(cwd);
	}

	/**
	 * Get the connection that was selected
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		return fConnection;
	}

	/**
	 * Get the path that was selected.
	 * 
	 * @return selected path
	 */
	public String getPath() {
		if (remotePath.equals("")) { //$NON-NLS-1$
			return null;
		}
		return remotePath;
	}

	/**
	 * Get the paths that were selected.
	 * 
	 * @return selected paths
	 */
	public String[] getPaths() {
		return remotePaths;
	}

	/**
	 * Set the initial path to start browsing. This will be set in the browser
	 * text field, and in a future version should expand the browser to this
	 * location if it exists.
	 * 
	 * @param path
	 */
	public void setInitialPath(String path) {
		fInitialPath = path;
	}

	/**
	 * Set the root directory for the browser. This will also update the text
	 * field with the path.
	 * 
	 * @param path
	 *            path of root directory
	 */
	private void setRoot(String path) {
		if (fFileMgr != null) {
			IFileStore root = fFileMgr.getResource(path);
			treeViewer.setInput(new DeferredFileStore(root, !showHidden));
			remotePathText.setText(path);
			remotePathText.setSelection(remotePathText.getText().length());
			fRootPath = new Path(path);
		}
	}

	/**
	 * Set the dialogTitle of the dialog.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		dialogTitle = title;
		if (dialogTitle == null) {
			dialogTitle = ""; //$NON-NLS-1$
		}
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(dialogTitle);
		}
	}

	/**
	 * Set the type of browser. Can be either a file browser (allows selection
	 * of files) or a directory browser (allows selection of directories), or
	 * both.
	 */
	public void setType(int type) {
		browserType = type;
		if (type == FILE_BROWSER) {
			dialogLabel = Messages.RemoteResourceBrowser_fileLabel;
			setTitle(Messages.RemoteResourceBrowser_fileTitle);
		} else if (type == DIRECTORY_BROWSER) {
			dialogLabel = Messages.RemoteResourceBrowser_directoryLabel;
			setTitle(Messages.RemoteResourceBrowser_directoryTitle);
		} else {
			dialogLabel = Messages.RemoteResourceBrowser_resourceLabel;
			setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		}
	}

	/**
	 * Show available connections on browser if possible.
	 * 
	 * @param enable
	 */
	public void showConnections(boolean enable) {
		this.showConnections = enable;
	}

	private void updateDialog() {
		if (okButton != null && upButton != null && newFolderButton != null) {
			okButton.setEnabled(false);
			upButton.setEnabled(false);
			newFolderButton.setEnabled(false);

			if (fConnection != null) {
				if (remotePathText != null) {
					String pathText = remotePathText.getText();
					if (!pathText.equals(EMPTY_STRING)) {
						okButton.setEnabled(true);
						newFolderButton.setEnabled(true);
						IPath path = new Path(pathText);
						if (!path.isRoot()) {
							upButton.setEnabled(true);
						}
					}
				}
			}
		}
	}
}
