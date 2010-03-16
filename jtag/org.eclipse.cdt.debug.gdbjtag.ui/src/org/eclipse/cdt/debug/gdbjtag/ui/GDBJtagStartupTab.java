/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     Andy Jin - Added DSF debugging, bug 248593
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class GDBJtagStartupTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "Startup";
	private static final String TAB_ID = "org.eclipse.cdt.debug.gdbjtag.ui.startuptab";
	
	Text initCommands;
	Text delay;
	Button doReset;
	Button doHalt;
	
	Button loadImage;
	Text imageFileName;
	Button imageFileBrowseWs;
	Button imageFileBrowse;
	Text imageOffset;
	
	Button loadSymbols;
	Text symbolsFileName;

	Button symbolsFileBrowseWs;
	Button symbolsFileBrowse;
	Text symbolsOffset;
	
	Button setPcRegister;
	Text pcRegister;
	
	Button setStopAt;
	Text stopAt;
	
	Button setResume;
	boolean resume = false;
	
	Text runCommands;

	public String getName() {
		return TAB_NAME;
	}

	public Image getImage() {
		return GDBJtagImages.getStartupTabImage();
	}
	
	public void createControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);

		Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);

		createInitGroup(comp);
		createLoadGroup(comp);
		createRunOptionGroup(comp);
		createRunGroup(comp);
		
		sc.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void browseButtonSelected(String title, Text text) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);
		String str = text.getText().trim();
		int lastSeparatorIndex = str.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(str.substring(0, lastSeparatorIndex));
		str = dialog.open();
		if (str != null)
			text.setText(str);
	}
	
	private void browseWsButtonSelected(String title, Text text) {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
        dialog.setTitle(title); 
        dialog.setMessage(Messages.getString("GDBJtagStartupTab.FileBrowseWs_Message")); 
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
        if (dialog.open() == IDialogConstants.OK_ID) {
            IResource resource = (IResource) dialog.getFirstResult();
            String arg = resource.getFullPath().toOSString();
            String fileLoc = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
            text.setText(fileLoc);
        }
	}
	
	public void createInitGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagStartupTab.initGroup_Text"));
		
		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		comp.setLayout(layout);
		
		doReset = new Button(comp, SWT.CHECK);
		doReset.setText(Messages.getString("GDBJtagStartupTab.doReset_Text"));
		doReset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doResetChanged();
				updateLaunchConfigurationDialog();
			}
		});
		delay = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 60;
		delay.setLayoutData(gd);
		delay.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = (Character.isDigit(e.character) || Character.isISOControl(e.character));
			}
		});
		delay.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		comp.setLayout(layout);
		
		doHalt = new Button(comp, SWT.CHECK);
		doHalt.setText(Messages.getString("GDBJtagStartupTab.doHalt_Text"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		doHalt.setLayoutData(gd);
		doHalt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		initCommands = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 60;
		initCommands.setLayoutData(gd);
		initCommands.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
	}
	
	private void createLoadGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		layout.numColumns = 4;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagStartupTab.loadGroup_Text"));
		
		loadImage = new Button(group, SWT.CHECK);
		loadImage.setText(Messages.getString("GDBJtagStartupTab.loadImage_Text"));
		gd = new GridData();
		gd.horizontalSpan = 4;
		loadImage.setLayoutData(gd);
		loadImage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadImageChanged();
				updateLaunchConfigurationDialog();
			}
		});
		
		Composite comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 4;
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comp.setLayout(layout);
		
		Label imageLabel = new Label(comp, SWT.NONE);
		imageLabel.setText(Messages.getString("GDBJtagStartupTab.imageLabel_Text"));	
		imageFileName = new Text(comp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		imageFileName.setLayoutData(gd);
		imageFileName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
        imageFileBrowseWs = createPushButton(comp, Messages.getString("GDBJtagStartupTab.FileBrowseWs_Label"), null); 
        imageFileBrowseWs.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	browseWsButtonSelected(Messages.getString("GDBJtagStartupTab.imageFileBrowseWs_Title"), imageFileName);
            }
        });
        
		imageFileBrowse = createPushButton(comp, Messages.getString("GDBJtagStartupTab.FileBrowse_Label"), null);
		imageFileBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("GDBJtagStartupTab.imageFileBrowse_Title"), imageFileName);
			}
		});	
		
		Label imageOffsetLabel = new Label(comp, SWT.NONE);
		imageOffsetLabel.setText(Messages.getString("GDBJtagStartupTab.imageOffsetLabel_Text"));
		imageOffset = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 100;
		imageOffset.setLayoutData(gd);
		imageOffset.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = (Character.isDigit(e.character) || Character.isISOControl(e.character) || "abcdef".contains(String.valueOf(e.character).toLowerCase()));
			}
		});
		imageOffset.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		loadSymbols = new Button(group, SWT.CHECK);
		loadSymbols.setText(Messages.getString("GDBJtagStartupTab.loadSymbols_Text"));
		gd = new GridData();
		gd.horizontalSpan = 4;
		loadSymbols.setLayoutData(gd);
		loadSymbols.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadSymbolsChanged();
				updateLaunchConfigurationDialog();
			}
		});
		
		comp = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 4;
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comp.setLayout(layout);
		
		Label symbolLabel = new Label(comp, SWT.NONE);
		symbolLabel.setText(Messages.getString("GDBJtagStartupTab.symbolsLabel_Text"));	
		symbolsFileName = new Text(comp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		symbolsFileName.setLayoutData(gd);
		symbolsFileName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});	
		
        symbolsFileBrowseWs = createPushButton(comp, Messages.getString("GDBJtagStartupTab.FileBrowseWs_Label"), null); 
        symbolsFileBrowseWs.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	browseWsButtonSelected(Messages.getString("GDBJtagStartupTab.symbolsFileBrowseWs_Title"), symbolsFileName);
            }
        });
        
		symbolsFileBrowse = createPushButton(comp, Messages.getString("GDBJtagStartupTab.FileBrowse_Label"), null);
		symbolsFileBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseButtonSelected(Messages.getString("GDBJtagStartupTab.symbolsFileBrowse_Title"), symbolsFileName);
			}
		});
		
		Label symbolsOffsetLabel = new Label(comp, SWT.NONE);
		symbolsOffsetLabel.setText(Messages.getString("GDBJtagStartupTab.symbolsOffsetLabel_Text"));
		symbolsOffset = new Text(comp, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 100;
		symbolsOffset.setLayoutData(gd);
		symbolsOffset.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = (Character.isDigit(e.character) || Character.isISOControl(e.character) || "abcdef".contains(String.valueOf(e.character).toLowerCase()));
			}
		});
		symbolsOffset.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
	}
	
	public void createRunOptionGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		layout.numColumns = 2;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagStartupTab.runOptionGroup_Text"));
		
		setPcRegister = new Button(group, SWT.CHECK);
		setPcRegister.setText(Messages.getString("GDBJtagStartupTab.setPcRegister_Text"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		setPcRegister.setLayoutData(gd);
		setPcRegister.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pcRegisterChanged();
				updateLaunchConfigurationDialog();
			}
		});	

		pcRegister = new Text(group, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 100;
		pcRegister.setLayoutData(gd);
		pcRegister.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = (Character.isDigit(e.character) || Character.isISOControl(e.character) || "abcdef".contains(String.valueOf(e.character).toLowerCase()));
			}
		});
		pcRegister.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		setStopAt = new Button(group, SWT.CHECK);
		setStopAt.setText(Messages.getString("GDBJtagStartupTab.setStopAt_Text"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		setStopAt.setLayoutData(gd);
		setStopAt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stopAtChanged();
				updateLaunchConfigurationDialog();
			}
		});
		
		stopAt = new Text(group, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 100;
		stopAt.setLayoutData(gd);
		stopAt.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		setResume = new Button(group, SWT.CHECK);
		setResume.setText(Messages.getString("GDBJtagStartupTab.setResume_Text"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		setResume.setLayoutData(gd);
		setResume.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resumeChanged();
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void doResetChanged() {
		delay.setEnabled(doReset.getSelection());
	}

	private void loadImageChanged() {
		boolean enabled = loadImage.getSelection();
		imageFileName.setEnabled(enabled);
		imageFileBrowseWs.setEnabled(enabled);
		imageFileBrowse.setEnabled(enabled);
		imageOffset.setEnabled(enabled);
	}
	
	private void loadSymbolsChanged() {
		boolean enabled = loadSymbols.getSelection();
		symbolsFileName.setEnabled(enabled);
		symbolsFileBrowseWs.setEnabled(enabled);
		symbolsFileBrowse.setEnabled(enabled);
		symbolsOffset.setEnabled(enabled);
	}
	
	private void pcRegisterChanged() {
		pcRegister.setEnabled(setPcRegister.getSelection());
	}
	
	private void stopAtChanged() {
		stopAt.setEnabled(setStopAt.getSelection());
	}
	
	private void resumeChanged() {
		resume = setResume.getSelection();
	}
	
	public void createRunGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText(Messages.getString("GDBJtagStartupTab.runGroup_Text"));

		runCommands = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 60;
		runCommands.setLayoutData(gd);
		runCommands.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (!super.isValid(launchConfig))
			return false;
		setErrorMessage(null);
		setMessage(null);

		if (loadImage.getSelection()) {
			if (imageFileName.getText().trim().length() == 0) {
				setErrorMessage(Messages.getString("GDBJtagStartupTab.imageFileName_not_specified"));
				return false;
			}

			String path;
			try {
				path = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(imageFileName.getText().trim());
				IPath filePath = new Path(path);
				if (!filePath.toFile().exists()) {
					setErrorMessage(Messages.getString("GDBJtagStartupTab.imageFileName_does_not_exist"));
					return false;
				}
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(e.getStatus());
			}
		} else {
			setErrorMessage(null);
		}
		if (loadSymbols.getSelection()) {
			if (symbolsFileName.getText().trim().length() == 0) {
				setErrorMessage(Messages.getString("GDBJtagStartupTab.symbolsFileName_not_specified"));
				return false;
			}
			String path;
			try {
				path = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(symbolsFileName.getText().trim());
				IPath filePath = new Path(path);
				if (!filePath.toFile().exists()) {
					setErrorMessage(Messages.getString("GDBJtagStartupTab.symbolsFileName_does_not_exist"));
					return false;
				}
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(e.getStatus());
			}
		} else {
			setErrorMessage(null);
		}
		
		if (setPcRegister.getSelection()) {
			if (pcRegister.getText().trim().length() == 0) {
				setErrorMessage(Messages.getString("GDBJtagStartupTab.pcRegister_not_specified"));
				return false;
			}
		} else {
			setErrorMessage(null);
		}
		if (setStopAt.getSelection()) {
			if (stopAt.getText().trim().length() == 0) {
				setErrorMessage(Messages.getString("GDBJtagStartupTab.stopAt_not_specified"));
			}
		} else {
			setErrorMessage(null);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
//	protected void updateLaunchConfigurationDialog() {
//		super.updateLaunchConfigurationDialog();
//		isValid(getLaunchConfigurationDialog());
//	}
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			initCommands.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, "")); //$NON-NLS-1$
			doReset.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_DO_RESET, IGDBJtagConstants.DEFAULT_DO_RESET));
			doResetChanged();
			doHalt.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_DO_HALT, IGDBJtagConstants.DEFAULT_DO_HALT));
			delay.setText(String.valueOf(configuration.getAttribute(IGDBJtagConstants.ATTR_DELAY, IGDBJtagConstants.DEFAULT_DELAY)));
			loadImage.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE));
			loadImageChanged();
			String defaultImageFileName = configuration.getAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, ""); //$NON-NLS-1$
			if (defaultImageFileName.equals("")) {
				defaultImageFileName = configuration.getWorkingCopy().getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, ""); //$NON-NLS-1$
			}
			imageFileName.setText(defaultImageFileName);
			imageOffset.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, "")); //$NON-NLS-1$
			loadSymbols.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS));
			loadSymbolsChanged();
			symbolsFileName.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, "")); //$NON-NLS-1$
			symbolsOffset.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, "")); //$NON-NLS-1$
			setPcRegister.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_PC_REGISTER, IGDBJtagConstants.DEFAULT_SET_PC_REGISTER));
			pcRegisterChanged();
			pcRegister.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_PC_REGISTER, "")); //$NON-NLS-1$
			setStopAt.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, IGDBJtagConstants.DEFAULT_SET_STOP_AT));
			stopAtChanged();
			stopAt.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_STOP_AT, "")); //$NON-NLS-1$
			setResume.setSelection(configuration.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME));
			resumeChanged();
			runCommands.setText(configuration.getAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, "")); //$NON-NLS-1$)
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, initCommands.getText());
		configuration.setAttribute(IGDBJtagConstants.ATTR_DELAY, Integer.parseInt(delay.getText()));
		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_RESET, doReset.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_HALT, doHalt.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, loadImage.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, imageFileName.getText().trim());
		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, imageOffset.getText());
		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, loadSymbols.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, symbolsOffset.getText());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, symbolsFileName.getText().trim());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_PC_REGISTER, setPcRegister.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_PC_REGISTER, pcRegister.getText());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, setStopAt.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_STOP_AT, stopAt.getText());
		configuration.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, setResume.getSelection());
		configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, runCommands.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, ""); //$NON-NLS-1$
		configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IGDBJtagConstants.DEFAULT_LOAD_IMAGE);
		configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, ""); //$NON-NLS-1$
		configuration.setAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, ""); //$NON-NLS-1$
		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_RESET, true);
		configuration.setAttribute(IGDBJtagConstants.ATTR_DO_HALT, true);
	}

}
