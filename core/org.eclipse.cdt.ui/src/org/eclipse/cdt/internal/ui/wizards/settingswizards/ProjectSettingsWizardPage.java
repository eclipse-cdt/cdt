/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.ListContentProvider;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.IProjectSettingsWizardPageStrategy.MessageType;

/**
 * @since 5.1
 */
abstract public class ProjectSettingsWizardPage extends WizardPage implements IProjectSettingsWizardPage {

	public static final String FILENAME_EXTENSION = "xml"; //$NON-NLS-1$
	
	
	private final IProjectSettingsWizardPageStrategy strategy;
	private final List<ISettingsProcessor> processors;
	
	private ICProject selectedProject;
	private ICConfigurationDescription selectedConfiguration;
	
	private Text filePathText;
	private CheckboxTableViewer settingsViewer;
	
	private IProject initialProject;
	
	/**
	 * 
	 * @param strategy
	 * @param initialProject the initial project to be selected, may be null
	 */
	private ProjectSettingsWizardPage(IProjectSettingsWizardPageStrategy strategy) {
		super(""); //$NON-NLS-1$
		this.strategy = strategy;
		
		// This could be replaced with an extension point
		this.processors = Arrays.<ISettingsProcessor>asList(
			new IncludePathsSettingsProcessor(),
			new MacroSettingsProcessor()
		);
		
	}
	
	
	
	
	protected abstract void layoutPage(Composite parent);
	
	
	public static ProjectSettingsWizardPage createExportWizardPage() {
		return new ProjectSettingsWizardPage(new ProjectSettingsExportStrategy()) {
			@Override
			public void layoutPage(Composite parent) {
			    createProjectSelectionGroup(parent);
			    createSettingsSelectionGroup(parent);
			    //createCheckboxSelectionGroup(parent);
			    createFileSelectionGroup(parent);
			}
		};
	}
	
	
	public static ProjectSettingsWizardPage createImportWizardPage() {
		return new ProjectSettingsWizardPage(new ProjectSettingsImportStrategy()) {
			@Override
			public void layoutPage(Composite parent) {
				createFileSelectionGroup(parent);
			    createProjectSelectionGroup(parent);
			    createSettingsSelectionGroup(parent);
			    //createCheckboxSelectionGroup(parent);
			}
		};
	}
	
	
	public boolean finish() {
		return strategy.finish(this);
	}
	
	
	public void setInitialProject(IProject project) {
		this.initialProject = project;
	}
	
	
	public List<ISettingsProcessor> getSettingsProcessors() {
		return Collections.unmodifiableList(processors);
	}
	
	
	public List<ISettingsProcessor> getSelectedSettingsProcessors() {
		List<ISettingsProcessor> selected = new ArrayList<ISettingsProcessor>();
		for(Object element : settingsViewer.getCheckedElements()) {
			selected.add((ISettingsProcessor)element);
		}
		return selected;
	}
	
	public String getDestinationFilePath() {
		return filePathText.getText();
	}
	
	
	public ICProject getSelectedProject() {
		return selectedProject;
	}
	
	
	public ICConfigurationDescription getSelectedConfiguration() {
		return selectedConfiguration;
	}
	
	public void setDisplayedSettingsProcessors(List<ISettingsProcessor> processors) {
		settingsViewer.setInput(processors);
		settingsViewer.refresh();
		settingsViewer.setAllChecked(true);
		updateWidgetEnablements();
	}
	
	
	public void showErrorDialog(String dialogTitle, String message) {
		Shell shell = Display.getCurrent().getActiveShell();
		Status status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, message, null);
		ErrorDialog.openError(shell, dialogTitle, null, status);
	}
	
	
	public void createControl(Composite parent) {
		setTitle(strategy.getMessage(MessageType.TITLE));
		setMessage(strategy.getMessage(MessageType.MESSAGE));
		    
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	    composite.setFont(parent.getFont());
	    
	    layoutPage(composite);
	    
		setControl(composite);
		
		strategy.pageCreated(this);
		
		updateWidgetEnablements();
	}
	
	
	private void updateWidgetEnablements() {
		boolean enableFinishButton = selectedProject != null 
		                          && selectedConfiguration != null
		                          && settingsViewer.getCheckedElements().length > 0 
		                          && filePathText.getText().length() > 0;
		
		// since this wizard has only one page we can toggle the finish button using the setPageComplete() method
		setPageComplete(enableFinishButton);
	}
	
	
	
	protected void createProjectSelectionGroup(Composite parent) {
		Composite projectSelectionGroup = new Composite(parent, SWT.NONE);
		projectSelectionGroup.setLayout(new GridLayout(2, true));
		projectSelectionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		projectSelectionGroup.setFont(parent.getFont());
		
		Label projectLabel = new Label(projectSelectionGroup, SWT.NONE);
		projectLabel.setText(Messages.ProjectSettingsWizardPage_selectProject);
		projectLabel.setLayoutData(new GridData());
		
		Label configLabel = new Label(projectSelectionGroup, SWT.NONE);
		configLabel.setText(Messages.ProjectSettingsWizardPage_selectConfiguration);
		configLabel.setLayoutData(new GridData());

		final Table projectTable = new Table(projectSelectionGroup, SWT.SINGLE | SWT.BORDER);
		projectTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableViewer projectViewer = new TableViewer(projectTable);
		projectViewer.setContentProvider(new ListContentProvider());
		projectViewer.setLabelProvider(new CElementLabelProvider());
		List<ICProject> openProjects = getAllOpenCProjects();
		Collections.sort(openProjects, new Comparator<ICProject>() {
			public int compare(ICProject o1, ICProject o2) {
				return o1.getProject().getName().compareTo(o2.getProject().getName());
			}
		});
		projectViewer.setInput(openProjects);
		
		final Table configTable = new Table(projectSelectionGroup, SWT.SINGLE | SWT.BORDER);
		configTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final TableViewer configViewer = new TableViewer(configTable);
		configViewer.setContentProvider(new ListContentProvider());
		configViewer.setLabelProvider(new LabelProvider() { 
			@Override public Image getImage(Object element) {
				return CPluginImages.get(CPluginImages.IMG_OBJS_CONFIG);
			}
			@Override public String getText(Object element) {
				ICConfigurationDescription config = (ICConfigurationDescription)element;
				String label = config.getName();
				if(config.isActive())
					label += " (" + Messages.ProjectSettingsWizardPage_active + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				return label;
			}
		});
		
		
		// TODO what if nothing is selected?
		projectTable.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				TableItem[] items = projectTable.getSelection();
				selectedProject = (ICProject)items[0].getData(); // its a single select so this is ok
				configViewer.setInput(getConfigurations(selectedProject));
				configViewer.refresh();
				configTable.select(0);
				configTable.notifyListeners(SWT.Selection, new Event());
			}
		});
		
		
		configTable.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				TableItem[] items = configTable.getSelection();
				selectedConfiguration = (ICConfigurationDescription)items[0].getData();
				updateWidgetEnablements();
			}
		});
		
		if(openProjects.isEmpty()) {
			setErrorMessage(Messages.ProjectSettingsWizardPage_noOpenProjects);
		}
		
		
		if((initialProject == null || !initialProject.isOpen()) && !openProjects.isEmpty()) {
			initialProject = openProjects.get(0).getProject();
		}
			
		if(initialProject != null) {
			String initialProjectName = initialProject.getName();
			for(int i = 0; i < openProjects.size(); i++) {
				ICProject tableProject = openProjects.get(i);
				if(tableProject.getElementName().equals(initialProjectName)) {
					projectTable.select(i);
					configViewer.setInput(getConfigurations(tableProject));
					configViewer.refresh();
					configTable.select(0);
					selectedProject = tableProject;
					selectedConfiguration = (ICConfigurationDescription)configTable.getSelection()[0].getData();
					break;
				}
			}
		}
	}
	
	
	private static List<ICConfigurationDescription> getConfigurations(ICProject project) {
		// get a read-only project description, no need to waste memory
		ICProjectDescription description = CCorePlugin.getDefault().getProjectDescription(project.getProject(), false);
		return Arrays.asList(description.getConfigurations());
	}
	
	
	private static List<ICProject> getAllOpenCProjects() {
        List<ICProject> projects = new ArrayList<ICProject>();
		try {
	        for(ICProject project : CoreModel.getDefault().getCModel().getCProjects()) {
	            if(project.getProject().isOpen()) {
	            	projects.add(project);
				}
	        }
		} catch(CModelException e) {
			CUIPlugin.log(e);
		}
		return projects;
	}
	
	
	protected void createSettingsSelectionGroup(Composite parent) {
		Composite settingsSelectionGroup = new Composite(parent, SWT.NONE);
		settingsSelectionGroup.setLayout(new GridLayout());
		settingsSelectionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		settingsSelectionGroup.setFont(parent.getFont());
	
		Label label = new Label(settingsSelectionGroup, SWT.NONE);
		label.setText(strategy.getMessage(MessageType.SETTINGS));
		
		Table table = new Table(settingsSelectionGroup, SWT.CHECK | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		settingsViewer = new CheckboxTableViewer(table);
		settingsViewer.setContentProvider(new ListContentProvider());
		
		settingsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateWidgetEnablements();
			}
		});
		
		LabelProvider settingsProcessorLabelProvider = new LabelProvider() {
			@Override public Image getImage(Object element) {
				return ((ISettingsProcessor)element).getIcon();
			}
			@Override public String getText(Object element) {
				return ((ISettingsProcessor)element).getDisplayName();
			}
		};
		
		settingsViewer.setLabelProvider(settingsProcessorLabelProvider); 
		settingsViewer.setInput(processors);
		settingsViewer.setAllChecked(true);
		
		
		Composite buttonComposite = new Composite(settingsSelectionGroup, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
        layout.marginHeight= layout.marginWidth= 0;
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData());
        
		Button selectButton = new Button(buttonComposite, SWT.PUSH);
		selectButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectButton.setText(Messages.ProjectSettingsWizardPage_selectAll);
		
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				settingsViewer.setAllChecked(true);
				updateWidgetEnablements();
			}
		});
		
		Button deselectButton = new Button(buttonComposite, SWT.PUSH);
		deselectButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectButton.setText(Messages.ProjectSettingsWizardPage_deselectAll);
		
		deselectButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				settingsViewer.setAllChecked(false);
				updateWidgetEnablements();
			}
		});
	}
	
	
	protected void createFileSelectionGroup(Composite parent) {
		Composite fileSelectionGroup = new Composite(parent, SWT.NONE);
		fileSelectionGroup.setLayout(new GridLayout(2, false));
		fileSelectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSelectionGroup.setFont(parent.getFont());
		
		Label label = new Label(fileSelectionGroup, SWT.NONE);
		label.setText(strategy.getMessage(MessageType.FILE));
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		
		filePathText = new Text(fileSelectionGroup, SWT.SINGLE | SWT.BORDER);
		filePathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateWidgetEnablements();
				strategy.fileSelected(ProjectSettingsWizardPage.this);
			} 
		});
		
		Button browseButton = new Button(fileSelectionGroup, SWT.PUSH);
		browseButton.setText(Messages.ProjectSettingsWizardPage_browse);
		browseButton.setLayoutData(new GridData());
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				int type = (strategy instanceof ProjectSettingsImportStrategy) ? SWT.OPEN : SWT.SAVE;
				FileDialog fileDialog = new FileDialog(getShell(), type);
				fileDialog.setFilterExtensions(new String[] {"*." + FILENAME_EXTENSION}); //$NON-NLS-1$
				String filePath = fileDialog.open();
				if(filePath != null)
					filePathText.setText(filePath);
			}
		});
	}

	


//	protected void createCheckboxSelectionGroup(Composite parent) {
//		Composite checkboxSelectGroup = new Composite(parent, SWT.NONE);
//		checkboxSelectGroup.setLayout(new GridLayout());
//		checkboxSelectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		checkboxSelectGroup.setFont(parent.getFont());
//		
//		final Button checkBox = new Button(checkboxSelectGroup, SWT.CHECK);
//		checkBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		checkBox.setText(strategy.getMessage(CHECKBOX));
//		
//		checkBox.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				strategy.handleCheckboxClick(ProjectSettingsWizardPage.this, checkBox.getSelection());
//			}
//		});
//	}
	

}

