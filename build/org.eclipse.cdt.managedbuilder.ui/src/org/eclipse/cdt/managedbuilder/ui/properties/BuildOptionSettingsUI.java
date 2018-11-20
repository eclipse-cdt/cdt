/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * ARM Ltd. - basic tooltip support
 * Miwako Tokugawa (Intel Corporation) - Fixed-location tooltip support
 * Baltasar Belyavsky (Texas Instruments) - custom field-editor support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.settings.model.MultiItemsHolder;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOption.ITreeOption;
import org.eclipse.cdt.managedbuilder.core.IOption.ITreeRoot;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.MultiResourceInfo;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Option settings page in project properties Build Settings under Tool Settings tab.
 */
public class BuildOptionSettingsUI extends AbstractToolSettingUI {
	private Map<String, FieldEditor> fieldsMap = new HashMap<>();
	private IOptionCategory category;
	private IHoldsOptions optionHolder;
	/** Option Holders involved */
	private IHoldsOptions[] ohs;
	/** The index of the current IHoldsOptions in ohs */
	private int curr = -1;
	private Map<String, CustomFieldEditorDescriptor> customFieldEditorDescriptorIndex;
	private Map<FieldEditor, Composite> fieldEditorsToParentMap = new HashMap<>();
	/** True if the user selected "Display tool option tips at a fixed location" in Preferences */
	private boolean displayFixedTip;
	/** type of mouse action the displayFixedTip responds to.
	 ** currently set to Enter rather than Hover since the former seems more responsive **/
	private final static int selectAction = SWT.MouseEnter;

	private final class TreeBrowseFieldEditor extends StringButtonFieldEditor {
		private final String nameStr;
		private final IOption option;
		private String contextId;

		private TreeBrowseFieldEditor(String name, String labelText, Composite parent, String nameStr, IOption option,
				String contextId) {
			super(name, labelText, parent);
			this.nameStr = nameStr;
			this.option = option;
			this.contextId = contextId;
		}

		@Override
		protected String changePressed() {
			ITreeRoot treeRoot;
			try {
				treeRoot = option.getTreeRoot();
				TreeSelectionDialog dlg = new TreeSelectionDialog(getShell(), treeRoot, nameStr, contextId);
				String name = getStringValue();
				if (name != null) {
					String treeId = option.getId(name);
					if (treeId != null) {
						ITreeOption node = treeRoot.findNode(treeId);
						if (node != null) {
							dlg.setSelection(node);
						}
					}
				}

				if (dlg.open() == Window.OK) {
					ITreeOption selected = dlg.getSelection();
					return selected.getName();
				}
			} catch (BuildException e) {
			}
			return null;
		}
	}

	private class TipInfo {
		private String name;
		private String tip;

		public TipInfo(String name, String tip) {
			this.name = name;
			this.tip = tip;
		}

		protected String getName() {
			return name;
		}

		protected String getTip() {
			return tip;
		}
	}

	public BuildOptionSettingsUI(AbstractCBuildPropertyTab page, IResourceInfo info, IHoldsOptions optionHolder,
			IOptionCategory _category) {
		this(page, info, optionHolder, _category, false);
	}

	/**
	 * @param page - parent page
	 * @param info - resource info
	 * @param optionHolder - option holder (i.e. tool)
	 * @param cat - option category
	 * @param displayFixedTip - {@code true} if tooltips for the option are
	 *    displayed at fixed area on the bottom of the dialog or
	 *    {@code false} as a regular tooltip hover
	 *
	 * @since 7.0
	 */
	public BuildOptionSettingsUI(AbstractCBuildPropertyTab page, IResourceInfo info, IHoldsOptions optionHolder,
			IOptionCategory cat, boolean displayFixedTip) {
		super(info);
		this.category = cat;
		this.displayFixedTip = displayFixedTip;
		this.optionHolder = optionHolder;
		buildPropPage = page;
		if (info instanceof MultiItemsHolder) {
			MultiResourceInfo mri = (MultiResourceInfo) info;
			IResourceInfo[] ris = (IResourceInfo[]) mri.getItems();
			String id = category.getId();

			/*
			 * Collect together all the IHoldsOptions (ITools & IToolChains)
			 * from the MultiResourceInfo's set of selected configs
			 * which contain the option category and accept the input type
			 * of this option holder.
			 */
			ArrayList<IHoldsOptions> lst = new ArrayList<>();
			if (optionHolder instanceof ITool) {
				String ext = ((ITool) optionHolder).getDefaultInputExtension();
				for (int i = 0; i < ris.length; i++) {
					ITool[] ts = ris[i].getTools();
					for (int j = 0; j < ts.length; j++) {
						IOptionCategory op = ts[j].getOptionCategory(id);
						if (op != null) {
							if (ext.equals(ts[j].getDefaultInputExtension())) {
								lst.add(ts[j]);
							}
						}
					}
				}
			} else if (optionHolder instanceof IToolChain) {
				for (int i = 0; i < ris.length; i++) {
					IToolChain tc = ris[i].getParent().getToolChain();
					IOptionCategory op = tc.getOptionCategory(id);
					if (op != null)
						lst.add(tc);
				}
			}

			ohs = lst.toArray(new IHoldsOptions[lst.size()]);
			for (int i = 0; i < ohs.length; i++) {
				if (ohs[i].equals(optionHolder)) {
					curr = i;
					break;
				}
			}
		} else {
			ohs = null;
			curr = 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#computeSize()
	 */
	@Override
	public Point computeSize() {
		return super.computeSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#Editors()
	 */
	@Override
	protected void createFieldEditors() {
		// true if the user selected "Display tool option tips at a fixed location" in Preferences AND
		// and we are displaying the tool tip box on this page because one or more option has non-empty tool tip.
		boolean pageHasToolTipBox = isToolTipBoxNeeded();

		// Get the preference store for the build settings
		super.createFieldEditors();
		// Iterate over the options in the category and create a field editor
		// for each
		Object[][] options = category.getOptions(fInfo, optionHolder);

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];

			// check to see if the option has an applicability calculator
			IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();
			IBuildObject config = fInfo;

			if (applicabilityCalculator == null || applicabilityCalculator.isOptionVisible(config, holder, opt)) {

				String optId = getToolSettingsPrefStore().getOptionId(opt);
				final String nameStr = opt.getName();
				String tipStr = opt.getToolTip();
				String contextId = opt.getContextId();

				if (pageHasToolTipBox && (tipStr == null || tipStr.trim().length() == 0)) {
					tipStr = Messages.BuildOptionSettingsUI_0;
				}

				try {
					// Figure out which type the option is and add a proper field
					// editor for it
					Composite fieldEditorParent = getFieldEditorParent();
					FieldEditor fieldEditor = null;

					String customFieldEditorId = opt.getFieldEditorId();
					if (customFieldEditorId != null) {
						fieldEditor = createCustomFieldEditor(customFieldEditorId);
						if (fieldEditor != null) {
							ICustomBuildOptionEditor customFieldEditor = (ICustomBuildOptionEditor) fieldEditor;
							if (customFieldEditor.init(opt, opt.getFieldEditorExtraArgument(), optId,
									fieldEditorParent)) {
								Control[] toolTipSources = customFieldEditor.getToolTipSources();
								if (toolTipSources != null) {
									for (Control control : toolTipSources) {
										if (pageHasToolTipBox) {
											control.setData(new TipInfo(nameStr, tipStr));
											control.addListener(selectAction, tipSetListener);
										} else {
											control.setToolTipText(tipStr);
										}
									}
								}
							} else {
								fieldEditor = null;
							}
						}
					}

					if (fieldEditor == null) {
						switch (opt.getValueType()) {
						case IOption.STRING: {
							StringFieldEditor stringField;

							// If browsing is set, use a field editor that has a
							// browse button of the appropriate type.
							switch (opt.getBrowseType()) {
							case IOption.BROWSE_DIR: {
								stringField = new DirectoryFieldEditor(optId, nameStr, fieldEditorParent);
								if (opt.getBrowseFilterPath() != null) {
									try {
										String filterPath = ManagedBuildManager.getBuildMacroProvider().resolveValue(
												opt.getBrowseFilterPath(), null, null,
												IBuildMacroProvider.CONTEXT_OPTION, opt.getOptionContextData(holder));
										((DirectoryFieldEditor) stringField).setFilterPath(new File(filterPath));
									} catch (BuildMacroException bmx) {
										ManagedBuilderUIPlugin.log(bmx);
									}
								}
							}
								break;

							case IOption.BROWSE_FILE: {
								stringField = new FileFieldEditor(optId, nameStr, fieldEditorParent) {
									/**
									 * Do not perform validity check on the file name due to losing focus,
									 * see http://bugs.eclipse.org/289448
									 */
									@Override
									protected boolean checkState() {
										clearErrorMessage();
										return true;
									}
								};
								if (opt.getBrowseFilterPath() != null) {
									try {
										String filterPath = ManagedBuildManager.getBuildMacroProvider().resolveValue(
												opt.getBrowseFilterPath(), null, null,
												IBuildMacroProvider.CONTEXT_OPTION, opt.getOptionContextData(holder));
										((FileFieldEditor) stringField).setFilterPath(new File(filterPath));
									} catch (BuildMacroException bmx) {
										ManagedBuilderUIPlugin.log(bmx);
									}
								}
								((FileFieldEditor) stringField).setFileExtensions(opt.getBrowseFilterExtensions());
							}
								break;

							case IOption.BROWSE_NONE: {
								final StringFieldEditorM local = new StringFieldEditorM(optId, nameStr,
										fieldEditorParent);
								stringField = local;
								local.getTextControl().addModifyListener(new ModifyListener() {
									@Override
									public void modifyText(ModifyEvent e) {
										local.valueChanged();
									}
								});
							}
								break;

							default: {
								throw new BuildException(null);
							}
							}
							Label label = stringField.getLabelControl(fieldEditorParent);
							Text text = stringField.getTextControl(fieldEditorParent);
							if (pageHasToolTipBox) {
								label.setData(new TipInfo(nameStr, tipStr));
								label.addListener(selectAction, tipSetListener);
								text.setData(new TipInfo(nameStr, tipStr));
								text.addListener(selectAction, tipSetListener);
							} else {
								label.setToolTipText(tipStr);
								text.setToolTipText(tipStr);
							}
							if (!contextId.equals(AbstractPage.EMPTY_STR)) {
								PlatformUI.getWorkbench().getHelpSystem().setHelp(text, contextId);
							}
							fieldEditor = stringField;
						}
							break;

						case IOption.BOOLEAN: {
							fieldEditor = new TriStateBooleanFieldEditor(optId, nameStr, tipStr, fieldEditorParent,
									contextId, ohs, curr);
							// tipStr is handled in TriStateBooleanFieldEditor constructor
						}
							break;

						case IOption.ENUMERATED: {
							String selId = opt.getSelectedEnum();
							String sel = opt.getEnumName(selId);

							// Get all applicable values for this enumerated Option, But display
							// only the enumerated values that are valid (static set of enumerated values defined
							// in the plugin.xml file) in the UI Combobox. This refrains the user from selecting an
							// invalid value and avoids issuing an error message.
							String[] enumNames = opt.getApplicableValues();
							Vector<String> enumValidList = new Vector<>();
							for (int i = 0; i < enumNames.length; ++i) {
								if (opt.getValueHandler().isEnumValueAppropriate(config, opt.getOptionHolder(), opt,
										opt.getValueHandlerExtraArgument(), enumNames[i])) {
									enumValidList.add(enumNames[i]);
								}
							}
							String[] enumValidNames = new String[enumValidList.size()];
							enumValidList.copyInto(enumValidNames);

							// if (displayFixedTip==false), tooltip was already set in BuildOptionComboFieldEditor constructor.
							String tooltipHoverStr = displayFixedTip ? null : tipStr;
							fieldEditor = new BuildOptionComboFieldEditor(optId, nameStr, tooltipHoverStr, contextId,
									enumValidNames, sel, fieldEditorParent);

							if (pageHasToolTipBox) {
								Combo combo = ((BuildOptionComboFieldEditor) fieldEditor).getComboControl();
								Label label = fieldEditor.getLabelControl(fieldEditorParent);
								combo.setData(new TipInfo(nameStr, tipStr));
								combo.addListener(selectAction, tipSetListener);
								label.setData(new TipInfo(nameStr, tipStr));
								label.addListener(selectAction, tipSetListener);
							}
						}
							break;

						case IOption.TREE:
							fieldEditor = new TreeBrowseFieldEditor(optId, nameStr, fieldEditorParent, nameStr, opt,
									contextId);
							((StringButtonFieldEditor) fieldEditor).setChangeButtonText("..."); //$NON-NLS-1$

							if (pageHasToolTipBox) {
								Text text = ((StringButtonFieldEditor) fieldEditor).getTextControl(fieldEditorParent);
								Label label = fieldEditor.getLabelControl(fieldEditorParent);
								text.setData(new TipInfo(nameStr, tipStr));
								text.addListener(selectAction, tipSetListener);
								label.setData(new TipInfo(nameStr, tipStr));
								label.addListener(selectAction, tipSetListener);
							}
							break;

						case IOption.INCLUDE_PATH:
						case IOption.STRING_LIST:
						case IOption.PREPROCESSOR_SYMBOLS:
						case IOption.LIBRARIES:
						case IOption.OBJECTS:
						case IOption.INCLUDE_FILES:
						case IOption.LIBRARY_PATHS:
						case IOption.LIBRARY_FILES:
						case IOption.MACRO_FILES:
						case IOption.UNDEF_INCLUDE_PATH:
						case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
						case IOption.UNDEF_INCLUDE_FILES:
						case IOption.UNDEF_LIBRARY_PATHS:
						case IOption.UNDEF_LIBRARY_FILES:
						case IOption.UNDEF_MACRO_FILES: {
							// if (displayFixedTip==false), tooltip was already set in FileListControlFieldEditor constructor.
							String tooltipHoverStr = displayFixedTip ? null : tipStr;
							fieldEditor = new FileListControlFieldEditor(optId, nameStr, tooltipHoverStr, contextId,
									fieldEditorParent, opt.getBrowseType());
							if (opt.getBrowseFilterPath() != null) {
								try {
									String filterPath = ManagedBuildManager.getBuildMacroProvider().resolveValue(
											opt.getBrowseFilterPath(), null, null, IBuildMacroProvider.CONTEXT_OPTION,
											opt.getOptionContextData(holder));
									((FileListControlFieldEditor) fieldEditor).setFilterPath(filterPath);
								} catch (BuildMacroException bmx) {
									ManagedBuilderUIPlugin.log(bmx);
								}
							}
							((FileListControlFieldEditor) fieldEditor)
									.setFilterExtensions(opt.getBrowseFilterExtensions());

							if (pageHasToolTipBox) {
								Label label = fieldEditor.getLabelControl(fieldEditorParent);
								label.setData(new TipInfo(nameStr, tipStr));
								label.addListener(selectAction, tipSetListener);
							}
						}
							break;

						default:
							throw new BuildException(null);
						}
					}

					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, fieldEditorParent);

					addField(fieldEditor);
					fieldsMap.put(optId, fieldEditor);
					fieldEditorsToParentMap.put(fieldEditor, fieldEditorParent);

				} catch (BuildException e) {
				}
			}
		}
	}

	/**
	 * Instantiates the custom-field editor registered under the given id.
	 */
	private FieldEditor createCustomFieldEditor(String customFieldEditorId) {
		if (this.customFieldEditorDescriptorIndex == null) {
			loadCustomFieldEditorDescriptors();
		}

		CustomFieldEditorDescriptor editorDescriptor = this.customFieldEditorDescriptorIndex.get(customFieldEditorId);
		if (editorDescriptor != null) {
			return editorDescriptor.createEditor();
		}

		return null;
	}

	/**
	 * Holds all the information necessary to instantiate a custom field-editor.
	 * Also acts as a factory - instantiates and returns a non-initialized field-editor.
	 */
	private class CustomFieldEditorDescriptor {
		private final IConfigurationElement element;

		public CustomFieldEditorDescriptor(IConfigurationElement providerElement) {
			this.element = providerElement;
		}

		FieldEditor createEditor() {
			try {
				Object editor = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (editor instanceof FieldEditor && editor instanceof ICustomBuildOptionEditor) {
					return (FieldEditor) editor;
				}
			} catch (Exception x) {
				ManagedBuilderUIPlugin.log(x);
			}

			return null;
		}
	}

	/**
	 * Loads all the registered custom field-editor descriptors.
	 * Synchronization is not necessary as this would always be invoked on the UI thread.
	 */
	private void loadCustomFieldEditorDescriptors() {
		if (this.customFieldEditorDescriptorIndex != null)
			return;

		this.customFieldEditorDescriptorIndex = new HashMap<>();

		IExtensionPoint ep = Platform.getExtensionRegistry()
				.getExtensionPoint(ManagedBuilderUIPlugin.getUniqueIdentifier() + ".buildDefinitionsUI"); //$NON-NLS-1$

		for (IExtension e : ep.getExtensions()) {
			for (IConfigurationElement providerElement : e.getConfigurationElements()) {
				String editorId = providerElement.getAttribute("id"); //$NON-NLS-1$

				this.customFieldEditorDescriptorIndex.put(editorId, new CustomFieldEditorDescriptor(providerElement));
			}
		}
	}

	/**
	 * Answers <code>true</code> if the settings page has been created for the
	 * option category specified in the argument.
	 *
	 * @see org.eclipse.cdt.managedbuilder.ui.properties.AbstractToolSettingUI#isFor(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean isFor(Object holder, Object cat) {
		if (holder instanceof IHoldsOptions && cat != null && cat instanceof IOptionCategory) {
			if (holder == this.optionHolder && cat.equals(this.category))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		// Write the field editor contents out to the preference store
		boolean ok = super.performOk();
		// Write the preference store values back to the build model

		Object[][] clonedOptions;
		//		IResourceConfiguration realRcCfg = null;
		IConfiguration realCfg = null;
		IBuildObject handler = null;

		realCfg = buildPropPage.getCfg(); //.getRealConfig(clonedConfig);
		if (realCfg == null)
			return false;
		handler = realCfg;
		clonedOptions = category.getOptions(fInfo, optionHolder);

		for (int i = 0; i < clonedOptions.length; i++) {
			IHoldsOptions clonedHolder = (IHoldsOptions) clonedOptions[i][0];
			if (clonedHolder == null)
				break; //  The array may not be full
			IOption clonedOption = (IOption) clonedOptions[i][1];

			IHoldsOptions realHolder = clonedHolder; // buildPropPage.getRealHoldsOptions(clonedHolder);
			IOption realOption = clonedOption; // buildPropPage.getRealOption(clonedOption, clonedHolder);
			if (realOption == null)
				continue;

			try {
				// Transfer value from preference store to options
				IOption setOption = null;
				switch (clonedOption.getValueType()) {
				case IOption.BOOLEAN:
					boolean boolVal = clonedOption.getBooleanValue();
					setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, boolVal);
					// Reset the preference store since the Id may have changed
					//						if (setOption != option) {
					//							getToolSettingsPrefStore().setValue(setOption.getId(), boolVal);
					//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
					//							fe.setPreferenceName(setOption.getId());
					//						}
					break;
				case IOption.ENUMERATED:
				case IOption.TREE:
					String enumVal = clonedOption.getStringValue();
					String enumId = clonedOption.getId(enumVal);
					setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption,
							(enumId != null && enumId.length() > 0) ? enumId : enumVal);
					// Reset the preference store since the Id may have changed
					//						if (setOption != option) {
					//							getToolSettingsPrefStore().setValue(setOption.getId(), enumVal);
					//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
					//							fe.setPreferenceName(setOption.getId());
					//					}
					break;
				case IOption.STRING:
					String strVal = clonedOption.getStringValue();
					setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, strVal);
					// Reset the preference store since the Id may have changed
					//						if (setOption != option) {
					//							getToolSettingsPrefStore().setValue(setOption.getId(), strVal);
					//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
					//							fe.setPreferenceName(setOption.getId());
					//						}
					break;
				case IOption.STRING_LIST:
				case IOption.INCLUDE_PATH:
				case IOption.PREPROCESSOR_SYMBOLS:
				case IOption.LIBRARIES:
				case IOption.OBJECTS:
				case IOption.INCLUDE_FILES:
				case IOption.LIBRARY_PATHS:
				case IOption.LIBRARY_FILES:
				case IOption.MACRO_FILES:
				case IOption.UNDEF_INCLUDE_PATH:
				case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
				case IOption.UNDEF_INCLUDE_FILES:
				case IOption.UNDEF_LIBRARY_PATHS:
				case IOption.UNDEF_LIBRARY_FILES:
				case IOption.UNDEF_MACRO_FILES:
					@SuppressWarnings("unchecked")
					String[] listVal = ((List<String>) clonedOption.getValue()).toArray(new String[0]);
					setOption = ManagedBuildManager.setOption(realCfg, realHolder, realOption, listVal);

					// Reset the preference store since the Id may have changed
					//						if (setOption != option) {
					//							getToolSettingsPrefStore().setValue(setOption.getId(), listStr);
					//							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
					//							fe.setPreferenceName(setOption.getId());
					//						}
					break;
				default:
					break;
				}

				// Call an MBS CallBack function to inform that Settings related to Apply/OK button
				// press have been applied.
				if (setOption == null)
					setOption = realOption;

				if (setOption.getValueHandler().handleValue(handler, setOption.getOptionHolder(), setOption,
						setOption.getValueHandlerExtraArgument(), IManagedOptionValueHandler.EVENT_APPLY)) {
					// TODO : Event is handled successfully and returned true.
					// May need to do something here say log a message.
				} else {
					// Event handling Failed.
				}

			} catch (BuildException e) {
			} catch (ClassCastException e) {
			}

		}
		return ok;
	}

	/**
	 * Update field editors in this page when the page is loaded.
	 */
	@Override
	public void updateFields() {
		Object[][] options = category.getOptions(fInfo, optionHolder);
		// some option has changed on this page... update enabled/disabled state for all options

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];
			String prefName = getToolSettingsPrefStore().getOptionId(opt);

			// is the option on this page?
			if (fieldsMap.containsKey(prefName)) {
				FieldEditor fieldEditor = fieldsMap.get(prefName);
				try {
					if (opt.getValueType() == IOption.ENUMERATED) {
						updateEnumList(fieldEditor, opt, holder, fInfo);
					}
				} catch (BuildException be) {
				}

				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();
				if (applicabilityCalculator != null) {
					Composite parent = fieldEditorsToParentMap.get(fieldEditor);
					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, parent);
				}
			}
		}

		Collection<FieldEditor> fieldsList = fieldsMap.values();
		for (FieldEditor editor : fieldsList) {
			if (editor instanceof TriStateBooleanFieldEditor)
				((TriStateBooleanFieldEditor) editor).set3(true);
			editor.load();
		}
	}

	private void setFieldEditorEnablement(IHoldsOptions holder, IOption option,
			IOptionApplicability optionApplicability, FieldEditor fieldEditor, Composite parent) {
		if (optionApplicability == null)
			return;

		// if the option is not enabled then disable it
		IBuildObject config = fInfo;
		if (!optionApplicability.isOptionEnabled(config, holder, option)) {
			fieldEditor.setEnabled(false, parent);
		} else {
			fieldEditor.setEnabled(true, parent);
		}
	}

	private boolean hasStr(String tipStr) {
		return (tipStr != null && tipStr.trim().length() > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);

		Object source = event.getSource();
		IOption changedOption = null;
		IHoldsOptions changedHolder = null;
		String id = null;

		if (source instanceof FieldEditor) {
			FieldEditor fe = (FieldEditor) source;

			if (fe instanceof TriStateBooleanFieldEditor)
				((TriStateBooleanFieldEditor) fe).set3(false);

			id = fe.getPreferenceName();

			Object[] option = this.getToolSettingsPrefStore().getOption(id);

			if (option == null) {
				int n = id.lastIndexOf('.');
				if (n > 0) {
					id = id.substring(0, n);
					option = getToolSettingsPrefStore().getOption(id);
				}
			}

			if (option != null) {
				changedOption = (IOption) option[1];
				changedHolder = (IHoldsOptions) option[0];
				try {
					switch (changedOption.getValueType()) {
					case IOption.STRING:
						if (fe instanceof StringFieldEditor) {
							String val = ((StringFieldEditor) fe).getStringValue();
							ManagedBuildManager.setOption(fInfo, changedHolder, changedOption, val);
						}
						break;
					case IOption.BOOLEAN:
						if (fe instanceof BooleanFieldEditor) {
							boolean val = ((BooleanFieldEditor) fe).getBooleanValue();
							ManagedBuildManager.setOption(fInfo, changedHolder, changedOption, val);
						}
						break;
					case IOption.ENUMERATED:
						if (fe instanceof BuildOptionComboFieldEditor) {
							String name = ((BuildOptionComboFieldEditor) fe).getSelection();
							String enumId = changedOption.getEnumeratedId(name);
							ManagedBuildManager.setOption(fInfo, changedHolder, changedOption,
									(enumId != null && enumId.length() > 0) ? enumId : name);

						}
						break;
					case IOption.TREE:
						if (fe instanceof TreeBrowseFieldEditor) {
							String name = ((TreeBrowseFieldEditor) fe).getStringValue();
							String treeId = changedOption.getId(name);
							ManagedBuildManager.setOption(fInfo, changedHolder, changedOption,
									(treeId != null && treeId.length() > 0) ? treeId : name);

						}
						break;
					case IOption.INCLUDE_PATH:
					case IOption.STRING_LIST:
					case IOption.PREPROCESSOR_SYMBOLS:
					case IOption.LIBRARIES:
					case IOption.OBJECTS:
					case IOption.INCLUDE_FILES:
					case IOption.LIBRARY_PATHS:
					case IOption.LIBRARY_FILES:
					case IOption.MACRO_FILES:
					case IOption.UNDEF_INCLUDE_PATH:
					case IOption.UNDEF_PREPROCESSOR_SYMBOLS:
					case IOption.UNDEF_INCLUDE_FILES:
					case IOption.UNDEF_LIBRARY_PATHS:
					case IOption.UNDEF_LIBRARY_FILES:
					case IOption.UNDEF_MACRO_FILES:
						if (fe instanceof FileListControlFieldEditor) {
							String val[] = ((FileListControlFieldEditor) fe).getStringListValue();
							ManagedBuildManager.setOption(fInfo, changedHolder, changedOption, val);
						}
						break;
					default:
						break;
					}
				} catch (BuildException e) {
				}
			}
		}

		Object[][] options = category.getOptions(fInfo, optionHolder);

		// some option has changed on this page... update enabled/disabled state for all options

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];
			String optId = getToolSettingsPrefStore().getOptionId(opt);

			// is the option on this page?
			if (fieldsMap.containsKey(optId)) {
				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();

				FieldEditor fieldEditor = fieldsMap.get(optId);
				try {
					if (opt.getValueType() == IOption.ENUMERATED) {
						// the item list of this enumerated option may have changed, update it
						updateEnumList(fieldEditor, opt, holder, fInfo);
					}
				} catch (BuildException be) {
				}

				if (applicabilityCalculator != null) {
					Composite parent = fieldEditorsToParentMap.get(fieldEditor);
					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, parent);
				}
			}
		}

		Collection<FieldEditor> xxx = fieldsMap.values();
		for (FieldEditor editor : xxx) {
			if (id == null || !id.equals(editor.getPreferenceName()))
				editor.load();
		}
	}

	@Override
	public void setValues() {
		updateFields();
	}

	/**
	 * @param optionHolder - option holder such as {@link ITool}
	 * @param category - option category
	 *
	 * @return true if the page needs to have the tool tip box.
	 *
	 * @since 7.0
	 */
	protected boolean needToolTipBox(IHoldsOptions optionHolder, IOptionCategory category) {
		if (optionHolder instanceof ITool) { // option category page
			Object[][] options = category.getOptions(fInfo, optionHolder);
			for (int index = 0; index < options.length; ++index) {
				IHoldsOptions holder = (IHoldsOptions) options[index][0];
				if (holder == null)
					break; //  The array may not be full
				IOption opt = (IOption) options[index][1];
				String tipStr = opt.getToolTip();

				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();
				IBuildObject config = fInfo;

				if (applicabilityCalculator == null || applicabilityCalculator.isOptionVisible(config, holder, opt)) {
					if (hasStr(tipStr)) {
						return true; // an option with a tip string was found.
					}
				}
			}
		}
		// A tool option summary page does not list individual options
		// so never should have the box
		return false;
	}

	/**
	 * The items shown in an enumerated option may depend on other option values.
	 * Whenever an option changes, check and update the valid enum values in
	 * the combo fieldeditor.
	 *
	 * See also https://bugs.eclipse.org/bugs/show_bug.cgi?id=154053
	 *
	 * @param fieldEditor enumerated combo fieldeditor
	 * @param opt         enumerated option type to update
	 * @param holder      the option holder
	 * @param config      project or resource info
	 * @throws BuildException
	 */
	protected void updateEnumList(FieldEditor fieldEditor, IOption opt, IHoldsOptions holder, IResourceInfo config)
			throws BuildException {
		// Get all applicable values for this enumerated Option, and filter out
		// the disable values
		String[] enumNames = opt.getApplicableValues();

		// get the currently selected enum value, the updated enum list may not contain
		// it, in that case a new value has to be selected
		String selectedEnum = opt.getSelectedEnum();
		String selectedEnumName = opt.getEnumName(selectedEnum);

		// get the default value for this enumerated option
		String defaultEnumId = (String) opt.getDefaultValue();
		String defaultEnumName = opt.getEnumName(defaultEnumId);

		boolean selectNewEnum = true;
		boolean selectDefault = false;

		Vector<String> enumValidList = new Vector<>();
		for (int i = 0; i < enumNames.length; ++i) {
			if (opt.getValueHandler().isEnumValueAppropriate(config, opt.getOptionHolder(), opt,
					opt.getValueHandlerExtraArgument(), enumNames[i])) {
				if (selectedEnumName.equals(enumNames[i])) {
					// the currently selected enum is part of the new item list, no need to select a new value.
					selectNewEnum = false;
				}
				if (defaultEnumName.equals(enumNames[i])) {
					// the default enum value is part of new item list
					selectDefault = true;
				}
				enumValidList.add(enumNames[i]);
			}
		}
		String[] enumValidNames = new String[enumValidList.size()];
		enumValidList.copyInto(enumValidNames);

		if (selectNewEnum) {
			// apparently the currently selected enum value is not part anymore of the enum list
			// select a new value.
			String selection = null;
			if (selectDefault) {
				// the default enum value is part of the item list, use it
				selection = (String) opt.getDefaultValue();
			} else if (enumValidNames.length > 0) {
				// select the first item in the item list
				selection = opt.getEnumeratedId(enumValidNames[0]);
			}
			ManagedBuildManager.setOption(config, holder, opt, selection);
		}
		((BuildOptionComboFieldEditor) fieldEditor).setOptions(enumValidNames);
		fieldEditor.load();
	}

	private final Listener tipSetListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			Object data = event.widget.getData();
			if (data != null && buildPropPage != null) {
				TipInfo obj = (TipInfo) data;
				((ToolSettingsTab) buildPropPage).updateTipText(obj.getName(), obj.getTip());
			}
		}
	};

	/**
	 *
	 *
	 *
	 */
	class TriStateBooleanFieldEditor extends BooleanFieldEditor {
		protected Button button = null;
		protected IHoldsOptions[] holders = null;
		private boolean enable3 = true;
		protected int current = 0;

		public TriStateBooleanFieldEditor(String name, String labelText, String tooltip, Composite parent,
				String contextId, IHoldsOptions[] ho, int curr) {
			super(name, labelText, parent);
			holders = ho;
			current = curr;
			button = getChangeControl(parent);
			if (displayFixedTip && isToolTipBoxNeeded()) {
				button.setData(new TipInfo(labelText, tooltip));
				button.addListener(selectAction, tipSetListener);
			} else {
				button.setToolTipText(tooltip);
			}
			if (!contextId.equals(AbstractPage.EMPTY_STR)) {
				PlatformUI.getWorkbench().getHelpSystem().setHelp(button, contextId);
			}

		}

		@Override
		protected void valueChanged(boolean oldValue, boolean newValue) {
			button.setGrayed(false);
			super.valueChanged(!newValue, newValue);
		}

		@Override
		protected void doLoad() {
			if (enable3 && holders != null && button != null) {
				String id = getPreferenceName();
				IOption op = holders[current].getOptionById(id);
				if (op != null) {
					if (op.getSuperClass() != null)
						id = op.getSuperClass().getId();
					int[] vals = new int[2];
					for (int i = 0; i < holders.length; i++) {
						op = holders[i].getOptionBySuperClassId(id);
						try {
							if (op != null)
								vals[op.getBooleanValue() ? 1 : 0]++;
						} catch (BuildException e) {
						}
					}
					boolean value = false;
					boolean gray = false;
					if (vals[1] > 0) {
						value = true;
						if (vals[0] > 0)
							gray = true;
					}
					button.setGrayed(gray);
					button.setSelection(value);
					return;
				}
			}
			super.doLoad(); // default case
		}

		void set3(boolean state) {
			enable3 = state;
		}
	}

	/**
	 * @since 8.2
	 */
	public static class TreeSelectionDialog extends TitleAreaDialog {
		private final ITreeRoot treeRoot;
		private ITreeOption selected;
		private final String name;
		private String contextId;
		private String baseMessage = ""; //$NON-NLS-1$
		private TreeViewer viewer;

		public TreeSelectionDialog(Shell parentShell, ITreeRoot root, String name, String contextId) {
			super(parentShell);
			treeRoot = root;
			setShellStyle(getShellStyle() | SWT.RESIZE);
			if (root.getIcon() != null) {
				Image img = createImage(root.getIcon());
				if (img != null) {
					setTitleImage(img);
				}
			}
			this.name = name;
			this.contextId = contextId;
			if (contextId != null && contextId.length() > 0) {
				setHelpAvailable(true);
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			if (contextId != null && contextId.length() > 0) {
				PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextId);
			}

			Composite control = new Composite(parent, SWT.NULL);
			GridData gd = new GridData(GridData.FILL_BOTH);
			GridLayout topLayout = new GridLayout();
			topLayout.numColumns = 1;
			control.setLayout(topLayout);
			control.setLayoutData(gd);

			PatternFilter filter = new PatternFilter();
			filter.setIncludeLeadingWildcard(true);
			FilteredTree tree = new FilteredTree(control, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter,
					true);
			viewer = tree.getViewer();
			viewer.setContentProvider(new ITreeContentProvider() {

				@Override
				public void dispose() {
				}

				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				@Override
				public Object[] getElements(Object inputElement) {
					return getChildren(inputElement);
				}

				@Override
				public Object[] getChildren(Object parentElement) {
					if (parentElement instanceof ITreeOption) {
						ITreeOption[] children = ((ITreeOption) parentElement).getChildren();

						// Not entirely sure whether this method is allowed to return null,
						// but let's play safe.
						if (children == null)
							return null;

						List<ITreeOption> childrenList = new ArrayList<>(Arrays.asList(children));

						// Check if any of the children has empty name
						List<ITreeOption> toRemove = null;
						for (ITreeOption child : children) {
							if (child.getName() == null || child.getName().trim().length() == 0) {
								if (toRemove == null) {
									toRemove = new ArrayList<>();
								}
								toRemove.add(child);
							}
						}
						if (toRemove != null) {
							childrenList.removeAll(toRemove);
						}

						// Sort the children.
						Collections.sort(childrenList, new Comparator<ITreeOption>() {
							@Override
							public int compare(ITreeOption arg0, ITreeOption arg1) {
								if (arg0.getOrder() == arg1.getOrder()) {
									return arg0.getName().compareToIgnoreCase(arg1.getName());
								} else {
									return arg0.getOrder() - arg1.getOrder();
								}
							}
						});

						return childrenList.toArray(new ITreeOption[0]);
					}
					return null;
				}

				@Override
				public Object getParent(Object element) {
					if (element instanceof ITreeOption) {
						return ((ITreeOption) element).getParent();
					}
					return null;
				}

				@Override
				public boolean hasChildren(Object element) {
					Object[] children = getChildren(element);
					return children != null && children.length > 0;
				}

			});

			viewer.setLabelProvider(new LabelProvider() {

				@Override
				public String getText(Object element) {
					if (element instanceof ITreeOption) {
						return ((ITreeOption) element).getName();
					}
					return super.getText(element);
				}

				@Override
				public Image getImage(Object element) {
					if (element instanceof ITreeOption) {
						String icon = ((ITreeOption) element).getIcon();
						return createImage(icon);
					}
					return super.getImage(element);
				}
			});

			viewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection selection = event.getSelection();
					if (selection instanceof IStructuredSelection) {
						Object selectedObj = ((IStructuredSelection) selection).getFirstElement();
						if (selectedObj instanceof ITreeOption) {
							selected = (ITreeOption) selectedObj;

							updateOKButton(selected);

							// Adjust Message
							String description = selected.getDescription();
							if (description == null) {
								ITreeOption node = selected;
								description = ""; //$NON-NLS-1$
								String sep = ": "; //$NON-NLS-1$
								while (node != null && node.getParent() != null) { // ignore root
									description = sep + node.getName() + description;
									node = node.getParent();
								}
								description = description.substring(sep.length()); // remove the first separator.
							}
							setMessage(baseMessage + description);
						}
					}
				}
			});

			viewer.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					ISelection selection = event.getSelection();
					if (!selection.isEmpty() && selection instanceof IStructuredSelection
							&& ((IStructuredSelection) selection).size() == 1) {
						Object selectedNode = ((IStructuredSelection) selection).getFirstElement();
						if (selectedNode instanceof ITreeOption) {
							if (updateOKButton((ITreeOption) selectedNode)) {
								TreeSelectionDialog.this.okPressed();
							} else { // if doubleclick is not on selectable item, expand/collapse
								viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
							}
						}
					}
				}
			});

			viewer.setInput(treeRoot);

			String msg = "Select " + name; //$NON-NLS-1$
			getShell().setText(msg);
			setTitle(msg);
			if (treeRoot.getDescription() != null) {
				baseMessage = treeRoot.getDescription();
				setMessage(baseMessage);
				baseMessage += "\nCurrent Selection: "; //$NON-NLS-1$
			} else {
				setMessage(msg);
			}

			return control;
		}

		@Override
		public void create() {
			super.create();
			// Need to update selection after the dialog has been created
			// so that we trigger the listener and correctly update the label
			// and buttons
			setUISelection();
		}

		public ITreeOption getSelection() {
			return selected;
		}

		/**
		 * @since 8.2
		 */
		public void setSelection(ITreeOption option) {
			if (treeRoot == getRoot(option)) { // only work in the same tree
				selected = option;
				setUISelection();
			}
		}

		private void setUISelection() {
			if (viewer != null && selected != null)
				viewer.setSelection(new StructuredSelection(selected), true);
		}

		private static ITreeRoot getRoot(ITreeOption option) {
			if (option != null) {
				ITreeOption parent = option.getParent();
				while (parent != null) {
					option = parent;
					parent = option.getParent();
				}
				return option instanceof ITreeRoot ? (ITreeRoot) option : null;
			}
			return null;
		}

		private Image createImage(String icon) {
			if (icon != null) {
				URL url = null;
				try {
					url = FileLocator.find(new URL(icon));
				} catch (Exception e) {
				}
				if (url != null) {
					ImageDescriptor desc = ImageDescriptor.createFromURL(url);
					return desc.createImage();
				}
			}
			return null;
		}

		private boolean updateOKButton(ITreeOption selection) {
			// Check if Valid selection (only allow selecting leaf nodes)
			if (treeRoot.isSelectLeafsOnly()) {
				boolean enableOK = !selection.isContainer();
				getButton(IDialogConstants.OK_ID).setEnabled(enableOK);
				return enableOK;
			}
			return false;
		}
	}
}
