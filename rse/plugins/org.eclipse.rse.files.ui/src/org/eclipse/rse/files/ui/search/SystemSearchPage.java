/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.actions.SystemSelectRemoteFolderAction;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.search.SystemSearchUtil;
import org.eclipse.rse.services.search.HostSearchResultSet;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.search.SystemSearchUI;
import org.eclipse.rse.ui.view.search.SystemSearchViewPart;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * This class provides the universal search page in the Search.
 */
public class SystemSearchPage extends DialogPage implements ISearchPage {
	
	public static final String SYSTEM_SEARCH_PAGE_ID = "org.eclipse.rse.files.ui.search.searchPage";
	
	// search utility
	private SystemSearchUtil util;
	
	// search page container
	private ISearchPageContainer container;
	
	// flag to indicate whether showing the dialog for the first time
	private boolean isFirstShow = true;
	
	// search string controls
	private Label stringLabel;
	private Combo stringCombo;
	private Button caseButton;
	private Label stringHintLabel;
	private Button stringRegexButton;
	private boolean initialCaseSensitive;
	private boolean initialStringRegex;
	
	// file name controls
	private Label fileNameLabel;
	private Combo fileNameCombo;
	private Button fileNameBrowseButton;
	private Label fileNameHintLabel;
	private Button fileNameRegexButton;
	private boolean initialFileNameRegex;
	
	// file name editor
	private FileNameEditor fileNameEditor;
	
	// folder name controls
	private Label folderNameLabel;
	private Combo folderNameCombo;
	private Button folderNameBrowseButton;
	
	// folder name editor
	private FolderNameEditor folderNameEditor;
	
	// advanced controls
	private Button searchArchivesButton;
	private Button searchSubfoldersButton;
	private boolean initialSearchArchives;
	private boolean initialSearchSubfolders;
	
	// constants for storing configuration
	private static final String REMOTE_SEARCH_PAGE_NAME = "RemoteSearchPage";
	private static final String STORE_CONFIG_CASE_SENSITIVE = "caseSensitive";
	private static final String STORE_CONFIG_STRING_REGEX = "stringRegex";
	private static final String STORE_CONFIG_FILENAME_REGEX = "fileNameRegex";
	private static final String STORE_CONFIG_INCLUDE_ARCHIVES = "includeArchives";
	private static final String STORE_CONFIG_INCLUDE_SUBFOLDERS = "includeSubfolders";	
	
	private static final String STORE_CONFIG_DATA_SIZE = "dataSize";
	
	// constants for storing data
	private static final String STORE_DATA_PREFIX = REMOTE_SEARCH_PAGE_NAME + ".data";
	private static final String STORE_DATA_SEARCH_STRING = "searchString";
	private static final String STORE_DATA_CASE_SENSITIVE = "caseSensitive";
	private static final String STORE_DATA_STRING_REGEX = "stringRegex";
	private static final String STORE_DATA_FILE_NAMES = "fileNames";
	private static final String STORE_DATA_FILE_NAME_REGEX = "fileNameRegex";
	private static final String STORE_DATA_PROFILE_NAME = "profileName";
	private static final String STORE_DATA_CONNECTION_NAME = "connectionName";
	private static final String STORE_DATA_FOLDER_NAME = "folderName";
	private static final String STORE_DATA_INCLUDE_ARCHIVES = "includeArchives";
	private static final String STORE_DATA_INCLUDE_SUBFOLDERS = "includeSubfolders";
	
	// a list to hold previous searche data
	private List previousSearchData = new ArrayList();
	
	// maximum size of data list
	private static final int MAX_DATA_SIZE = 20;
	
	// inner class to hold search data
	private class RemoteSearchData {
		
		private String searchString;
		private boolean caseSensitive;
		private boolean stringRegex;
		private String fileNames;
		private boolean fileNameRegex;
		private String profileName;
		private String connectionName;
		private String folderName;
		private boolean includeArchives;
		private boolean includeSubfolders;
		
		/**
		 * Constructor for search data.
		 * @param searchString the search string.
		 * @param caseSensitive <code>true</code> if case sensitive, <code>false</code> otherwise.
		 * @param stringRegex <code>true</code> if search string is a regular expression, <code>false</code> otherwise.
		 * @param fileNames set of file names.
		 * @param fileNameRegex <code>true</code> if the file name is a regular expression, <code>false</code> otherwise.
		 * @param profileName the profile name.
		 * @param connectionName the connection name.
		 * @param folderName the folder name.
		 * @param includeArchives <code>true</code> if archives should also be searched, <code>false</code> otherwise.
		 * @param includeSubfolders <code>true</code> if subfolders should also be searched, <code>false</code> otherwise.
		 */
		private RemoteSearchData(String searchString, boolean caseSensitive, boolean stringRegex, 
								 String fileNames, boolean fileNameRegex, String profileName, String connectionName,
								 String folderName, boolean includeArchives, boolean includeSubfolders) {
			this.searchString = searchString;
			this.caseSensitive = caseSensitive;
			this.stringRegex = stringRegex;
			this.fileNames = fileNames;
			this.fileNameRegex = fileNameRegex;
			this.profileName = profileName;
			this.connectionName = connectionName;
			this.folderName = folderName;
			this.includeArchives = includeArchives;
			this.includeSubfolders = includeSubfolders;
		}
	}
	
	// abstract inner class for handling a combo and browse button combination
	abstract private class CommonEditor extends SelectionAdapter implements DisposeListener {
		
		protected Combo nameCombo;
		protected Button browseButton;
		
		/**
		 * Constructor for editor.
		 * @param nameCombo the combo.
		 * @param browseButton the browse button.
		 */
		private CommonEditor(Combo nameCombo, Button browseButton) {
			this.nameCombo = nameCombo;
			this.browseButton = browseButton;
			
			// add dispose listeners to both the combo and the button
			// we make them null when they are disposed for cleanup to occur
			nameCombo.addDisposeListener(this);
			browseButton.addDisposeListener(this);
			
			// add selection listener to the browse button
			browseButton.addSelectionListener(this);
		}
		
		/**
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {
			
			if (e.widget == nameCombo) {
				nameCombo = null;
			}
			else if (e.widget == browseButton) {
				browseButton = null;
			}
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			
			if (e.widget == browseButton) {
				handleBrowseSelected();
			}
		}
		
		protected abstract void handleBrowseSelected();
	}
	
	// inner class to handle file name editing
	private class FileNameEditor extends CommonEditor {
		
		/**
		 * Constructor for folder name editor.
		 * @param fileCombo the file combo.
		 * @param browseButton the file types browse button.
		 */
		private FileNameEditor(Combo fileCombo, Button browseButton) {
			super(fileCombo, browseButton);
		}
		
		/**
		 * @see org.eclipse.rse.files.ui.search.SystemSearchPage.CommonEditor#handleBrowseSelected()
		 */
		protected void handleBrowseSelected() {
			
			// get the shell from the page
			Shell shell = SystemSearchPage.this.getShell();
			
			// create select file types action that opens the select file types dialog 
			SystemSearchSelectFileTypesAction action = new SystemSearchSelectFileTypesAction(shell);
			
			// get current types list from the combo
			List prevTypes = util.typesStringToList(getFileNamesText());
			
			// set the preselection of the select file types dialog to the current list of types in the combo
			action.setTypes(prevTypes);
			
			// run the action
			action.run();
			
			// if the user did not cancel from the dialog
			if (!action.wasCancelled()) {
				
				// get the types the user chose in the dialog 
				List newTypes = action.getTypes();
				
				// set the combo to have the new types
				setFileNamesText(util.typesListToString(newTypes));
			}
		}
		
		/**
		 * Sets the file names text.
		 * @param fileNames the file names string.
		 */
		private void setFileNamesText(String fileNames) {
			nameCombo.setText(fileNames);
		}
		
		/**
		 * Gets the text in the file names combo.
		 * @return the file names text.
		 */
		private String getFileNamesText() {
			return nameCombo.getText();
		}
	}
	
	// inner class to handle folder name editing
	private class FolderNameEditor extends CommonEditor {
		
		// holds info for each item in the folder combo
		private class Info {
			private String profileName;
			private String connectionName;
			private String folderPath;
			
			private Info(String profileName, String connectionName, String folderPath) {
				this.profileName = profileName;
				this.connectionName = connectionName;
				this.folderPath = folderPath;
			}
		}
		
		// the current info
		// TODO: needs to go when we show profile name in the folder combo
		private Info currInfo;
		
		/**
		 * Constructor for folder name editor.
		 * @param folderCombo the folder name combo.
		 * @param browseButton the folder browse button.
		 */
		private FolderNameEditor(Combo folderCombo, Button browseButton) {
			super(folderCombo, browseButton);
			
			// add selection listener to the folder combo
			folderCombo.addSelectionListener(this);
		}
		
		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {

			// if the selected widget is the browse button, let the super class handle it
			if (e.widget == browseButton) {
				super.widgetSelected(e);
			}
			// otherwise if the selected widget is the folder combo, then get the selection index
			// and get the data associated with that index
			// TODO: needs to go when we show profile name in the folder combo
			else if (e.widget == nameCombo){
				int selectionIndex = nameCombo.getSelectionIndex();
				currInfo = getInfo(selectionIndex);
			}
			else {
				super.widgetSelected(e);
			}
		}

		/**
		 * @see org.eclipse.rse.files.ui.search.SystemSearchPage.CommonEditor#handleBrowseSelected()
		 */
		protected void handleBrowseSelected() {
			
			// get the shell from the page
			Shell shell = SystemSearchPage.this.getShell();
			
			// create select folder action which opens the select folder dialog 
			SystemSelectRemoteFolderAction selectFolderAction = new SystemSelectRemoteFolderAction(shell);
			selectFolderAction.setShowNewConnectionPrompt(true);
			selectFolderAction.setShowPropertySheet(true, false);
			selectFolderAction.setNeedsProgressMonitor(true);
			selectFolderAction.setMultipleSelectionMode(false);
			
			// run the action
			selectFolderAction.run();
			
			// if the user did not cancel out of the dialog
			if (!selectFolderAction.wasCancelled()) {
				// store remote path
				IRemoteFile remoteFile = selectFolderAction.getSelectedFolder();
				String folderPath = remoteFile.getAbsolutePath();
				
				// store connection and profile
				IHost conn = selectFolderAction.getSelectedConnection();
				String profileName = conn.getSystemProfileName();
				String connectionName = conn.getAliasName();
				
				setFolderText(profileName, connectionName, folderPath);
			}
		}
		
		/**
		 * Sets the folder name combo text. Also sets the profile name, connection name and folder path.
		 * @param profileName the profile name.
		 */
		private void setFolderText(String profileName, String connectionName, String folderPath) {
			
			String text = getFormattedText(profileName, connectionName, folderPath);
			
			// get existing items in the combo
			String[] currItems = nameCombo.getItems();
			int selectionIndex = -1;
			boolean matchFound = false;
			
			// see if one of the entries match
			for (int i = 0; i < currItems.length; i++) {
				
				if (currItems[i].equals(text)) {
					selectionIndex = i;
					matchFound = true;
					break;
				}
			}
			
			// if no matches found, we add our text to the end of the list of items in the combo and select it
			// note that we can't simply call setText() because this is a read-only combo
			if (!matchFound) {
				int oldLength = currItems.length;
				String[] newItems = new String[oldLength + 1];
				System.arraycopy(currItems, 0, newItems, 0, currItems.length);
				newItems[oldLength] = text;
				nameCombo.setItems(newItems);
				selectionIndex = oldLength;
				
				// TODO: needs to go when we show profile name in the folder combo
				addData(selectionIndex, profileName, connectionName, folderPath);
			}
			
			// select from the current index
			nameCombo.select(selectionIndex);
			
			currInfo = getInfo(selectionIndex);
		}
		
		/**
		 * Convert to text.
		 * @return the string representation.
		 */
		private String getFormattedText(String profileName, String connectionName, String folderPath) {
			// TODO: prefix with profile name after checking preference ??
			// then we will no longer need to store data in the combo, which is a very inelegant solution
			// we can simply parse whatever comes out of getText() and get profile name, connection name
			// and folder path
			return connectionName + ":" + folderPath;
		}
		
		/**
		 * Add data.
		 * @param index the index for which this data applies.
		 * @param profileName the profile name.
		 * @param connectionName the connection name.
		 * @param folderPath the folder path.
		 */
		private void addData(int index, String profileName, String connectionName, String folderPath) {
			Info info = new Info(profileName, connectionName, folderPath);
			nameCombo.setData(String.valueOf(index), info);	
		}
		
		/**
		 * Gets the info for a given index.
		 * @param index the index.
		 * @return the info for the index.
		 */
		private Info getInfo(int index) {
			return (Info)(nameCombo.getData(String.valueOf(index)));
		}
		
		/**
		 * Returns the profile name.
		 * @return the profile name, or <code>null</code> if none specified.
		 */
		private String getProfileName() {
			return currInfo.profileName;
		}
		
		/**
		 * Returns the connection name.
		 * @return the connection name, or <code>null</code> if none specified.
		 */
		private String getConnectionName() {
			return currInfo.connectionName;
		}
		
		/**
		 * Returns the folder path.
		 * @return the folder path, or <code>null</code> if none specified.
		 */
		private String getFolderPath() {
			return currInfo.folderPath;
		}
	}
	
	/**
	 * This contructor instantiates a remote search page.
	 */
	public SystemSearchPage() {
		super();
		this.util = SystemSearchUtil.getInstance();
	}
	
	/**
	 * Gets the search data from current state of the dialog. If the search string matches a previous
	 * search data search string, then that data is deleted and replaced with the new data.
	 * @return the search data corresponding to the current state of the dialog.
	 */
	private RemoteSearchData getSearchData() {
		RemoteSearchData data = null;
		String searchString = stringCombo.getText();
		
		int i = previousSearchData.size() - 1;
		
		boolean matchFound = false;
		
		// go through stored search data, and see if one has the search string
		// matching our current search string
		while (i >= 0) {
			data = (RemoteSearchData)previousSearchData.get(i);
			
			if (searchString.equals(data.searchString)) {
				matchFound = true;
				break;
			}
			
			i--;
		}
		
		// if a match has been found, modify its properties
		if (matchFound) {
			data.searchString = searchString;
			data.caseSensitive = caseButton.getSelection();
			data.stringRegex = stringRegexButton.getSelection();
			data.fileNames = getFileNames();
			data.fileNameRegex = fileNameRegexButton.getSelection();
			data.profileName = getProfileName();
			data.connectionName = getConnectionName();
			data.folderName = getFolderName();
			data.includeArchives = searchArchivesButton.getSelection();
			data.includeSubfolders = searchSubfoldersButton.getSelection();
			
			// remove data from the list because it will be later added
			// we want to basically move the data to the end of the list
			previousSearchData.remove(data);
		}
		// otherwise create new data
		else {
			data = new RemoteSearchData(searchString, caseButton.getSelection(), stringRegexButton.getSelection(),
					getFileNames(), fileNameRegexButton.getSelection(), getProfileName(), getConnectionName(),
					getFolderName(), searchArchivesButton.getSelection(), searchSubfoldersButton.getSelection());
		}
		
		// if the data size is the maximum size allowable,
		// remove the first entry from the list
		if (previousSearchData.size() == MAX_DATA_SIZE) {
			previousSearchData.remove(0);
		}
		
		// now add the data to the data list
		previousSearchData.add(data);
		
		return data;
	}
	
	/**
	 * Gets the file names string.
	 * @return the file names string.
	 */
	private String getFileNames() {
		return fileNameEditor.getFileNamesText();
	}
	
	/**
	 * Gets the profile name from the folder selection.
	 * @return the profile name.
	 */
	private String getProfileName() {
		return folderNameEditor.getProfileName();
	}
	
	/**
	 * Gets the connection name from the folder selection.
	 * @return the connection name.
	 */
	private String getConnectionName() {
		return folderNameEditor.getConnectionName();
	}
	
	/**
	 * Gets the folder name from the folder selection.
	 * @return the folder name.
	 */
	private String getFolderName() {
		return folderNameEditor.getFolderPath();
	}
	
	/**
	 * @see org.eclipse.search.ui.ISearchPage#performAction()
	 */
	public boolean performAction() {
		
		// if the string is a regex, check that the regular expression is valid
		// if not show error and return false
		if (stringRegexButton.getSelection()) {
			String searchString = stringCombo.getText();
			
			if (searchString != null && searchString.length() != 0) {
				
				if (!util.isValidRegex(searchString)) {
					SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_REMOTE_SEARCH_INVALID_REGEX);
					message.makeSubstitution(searchString);
					SystemMessageDialog.displayErrorMessage(getShell(), message);
					stringCombo.setFocus();
					return false;
				}
			}
		}
		
		// if the file name is a regex, check that the regular expression is valid
		// if not show error and return false
		if (fileNameRegexButton.getSelection()) {
			String fileNameString = fileNameEditor.getFileNamesText();
			
			if (fileNameString != null && fileNameString.length() != 0) {
				
				if (!util.isValidRegex(fileNameString)) {
					SystemMessage message = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_REMOTE_SEARCH_INVALID_REGEX);
					message.makeSubstitution(fileNameString);
					SystemMessageDialog.displayErrorMessage(getShell(), message);
					fileNameCombo.setFocus();
					return false;
				}
			}
		}
		
		// get the data corresponding to the current dialog state
		RemoteSearchData data = getSearchData();
		
		SystemSearchString searchString = new SystemSearchString(data.searchString, data.caseSensitive, data.stringRegex, data.fileNames, data.fileNameRegex, data.includeArchives, data.includeSubfolders);
		
		IRemoteFileSubSystem subsys = getRemoteFileSubSystem(data.profileName, data.connectionName);
		
		if (subsys == null) {
			return false;
		}
		
		IRemoteFile remoteFile;
		
		try {
			remoteFile = subsys.getRemoteFileObject(data.folderName);
		}
		catch (SystemMessageException e) {
			// TODO: show error
			SystemBasePlugin.logError("Error occured trying to get remote file object", e);
			return false;
		}
		

		
	
		// create a search result set to contain all the results
		IHostSearchResultSet set = new HostSearchResultSet();
	
		if (subsys instanceof FileServiceSubSystem)
		{
	
			
			// set the name
			String name = remoteFile.getAbsolutePath() + " - " + searchString.getFileNamesString() + "(" + searchString.getTextString() + ")";
			set.setName(name);
			
			FileServiceSubSystem ss = (FileServiceSubSystem)subsys;
			IHostSearchResultConfiguration config = ss.createSearchConfiguration(set, remoteFile, searchString);
			
			// show results in remote search view
			showInView(set);
			
			// kick off the search
			ss.search(config);
			
			// save the configuration
			writeConfiguration();
			
			// finally save the data
			writeData();
		}
	
		

		
		return true;
	}
	
	/**
	 * Show the search results in the remote search view.
	 * @param resultSet the search result set.
	 */
	private void showInView(IHostSearchResultSet resultSet) 
	{
		SystemSearchUI searchUI = SystemSearchUI.getInstance();
		SystemSearchViewPart searchPart = searchUI.activateSearchResultView();
		searchPart.addSearchResult((IAdaptable)resultSet);
	}
	


	/**
	 * @see org.eclipse.search.ui.ISearchPage#setContainer(org.eclipse.search.ui.ISearchPageContainer)
	 */
	public void setContainer(ISearchPageContainer container) {
		this.container = container;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		
		// if we are to become visible
		if (visible) {
			
			// if it is the very first time, then set the items for the string combo and file name combo
			// and initialize from selection
			if (isFirstShow) {
				isFirstShow = false;
				
				// read in the data
				readData();
				
				// we set items and text here, rather than during createControl() to prevent page from resizing
				setPreviousSearchStrings();
				setPreviousFileNames();
				setPreviousFolderNames();
				initialize();
			}
			
			// set focus to the string combo every time we become visible
			stringCombo.setFocus();
			
			// indicate whether search is allowed
			container.setPerformActionEnabled(isValid()); 
		}
		
		super.setVisible(visible);
	}
	
	/**
	 * Obtains the search strings from previous search data and adds them to the search string combo.
	 */
	private void setPreviousSearchStrings() {
		int size = previousSearchData.size();
		
		// array to hold previous search strings
		String[] searchStrings = new String[size];
		
		for (int i = 0; i < size; i++) {
			searchStrings[i] = ((RemoteSearchData)previousSearchData.get(size - 1 - i)).searchString;
		}
		
		stringCombo.setItems(searchStrings);
	}
	
	/**
	 * Obtains the file name strings from previous search data and adds them to the file name combo.
	 */
	private void setPreviousFileNames() {
		int size = previousSearchData.size();
		
		// list to hold type strings
		List typesStringList = new ArrayList(size);
		
		// go through the search data starting from the end
		for (int i = size - 1; i >= 0; i--) {
			RemoteSearchData data = (RemoteSearchData)previousSearchData.get(i);
			
			// now get the types string representing the list of types in the data
			String typeString = data.fileNames;
			
			// check if we already have this types string in the types string list
			// if not, add it
			if (!typesStringList.contains(typeString)) {
				typesStringList.add(typeString);
			}
		}
		
		String[] typesStringArray = (String[])typesStringList.toArray(new String[typesStringList.size()]);
		
		fileNameCombo.setItems(typesStringArray);
	}
	
	/**
	 * Obtains the folder name strings from previous search data and adds them to the folder name combo.
	 */
	private void setPreviousFolderNames() {
		int size = previousSearchData.size();
		
		// list to hold folder names
		List folderNamesList = new ArrayList(size);
		
		int j = 0;
		
		// go through the search data starting from the end
		for (int i = size - 1; i >= 0; i--) {
			RemoteSearchData data = (RemoteSearchData)previousSearchData.get(i);
			
			// get the string from the folder name editor
			String folderNameString = folderNameEditor.getFormattedText(data.profileName, data.connectionName, data.folderName);
			
			if (!folderNamesList.contains(folderNameString)) {
				folderNamesList.add(folderNameString);
				
				// add combo data
				// TODO: needs to go when we show profile name in the folder name combo
				folderNameEditor.addData(j, data.profileName, data.connectionName, data.folderName);
				j++;
			}
		}
		
		String[] folderNamesArray = (String[])folderNamesList.toArray(new String[folderNamesList.size()]);
		
		folderNameCombo.setItems(folderNamesArray);
	}
	
	/**
	 * Initializes the dialog.
	 */
	private void initialize() {
		
		ISelection selection = container.getSelection();
		
		String text = null;
		String fileName = null;
		String profileName = null;
		String connectionName = null;
		String folderName = null;
		
		// if selection is not empty, we handle structured selection or text selection
		if (selection != null && !selection.isEmpty()) {
		
			// if it is a structured selection
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				// check if it is a remote file
				if (obj instanceof IRemoteFile) {
					
					IRemoteFile remoteFile = (IRemoteFile)obj;
					boolean supportsArchiveManagement = remoteFile.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement();
					
					// if it's a file, but not an archive, get the file name, connection info, and parent folder name
					if (remoteFile.isFile() && (!remoteFile.isArchive() && !supportsArchiveManagement)) {
						fileName = remoteFile.getName();
						IHost conn = remoteFile.getSystemConnection();
						profileName = conn.getSystemProfileName();
						connectionName = conn.getAliasName();
						folderName = remoteFile.getParentPath();
					}
					// otherwise if it's a folder or an arvhive, get the connection info and the name
					else if (remoteFile.isDirectory() || (remoteFile.isArchive() && supportsArchiveManagement)) {
						IHost conn = remoteFile.getSystemConnection();
						profileName = conn.getSystemProfileName();
						connectionName = conn.getAliasName();
						folderName = remoteFile.getAbsolutePath();						
					}
				}
			}
			// otherwise, if it is a text selection
			else if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection)selection;
				text = textSelection.getText();
			}
		}
		
		// if text is null, then make it "*"
		if (text == null || text.equals("")) {
			text = "*";
		}
		// otherwise, make sure to escape the special characters so that we actually search for the
		// string with the special characters
		else {
			text = insertEscapeChar(text);
		}
		
		stringCombo.setText(text);
		
		// if the file name is still null, set it to "*"
		if (fileName == null) {
			fileName = "*";
		}
		
		// set the file name
		fileNameEditor.setFileNamesText(fileName);
		
		// set the remote folder combo properties if profile name, connection name and folder path are
		// not null
		if (profileName != null && connectionName != null && folderName != null) {
			folderNameEditor.setFolderText(profileName, connectionName, folderName);
		}
	}
	
	/**
	 * Inserts escape character '\' for literals '*', '?' and '\\'.
	 * @param text the text.
	 * @return the text with the escape character inserted as needed, or "" if the given text is <code>null</code>,
	 *         or an error occurs. 
	 */
	private String insertEscapeChar(String text) {
		
		if (text == null || text.equals("")) {
			return "";
		}
		
		StringBuffer sbIn = new StringBuffer(text);
		BufferedReader reader = new BufferedReader(new StringReader(text));
		int lengthOfFirstLine = 0;
		
		try {
			lengthOfFirstLine = reader.readLine().length();
		}
		catch (IOException ex) {
			return "";
		}
		
		StringBuffer sbOut = new StringBuffer(lengthOfFirstLine + 5);
		int i = 0;
		
		while (i < lengthOfFirstLine) {
			char ch = sbIn.charAt(i);
			
			if (ch == '*' || ch == '?' || ch == '\\') {
				sbOut.append("\\");
			}
			
			sbOut.append(ch);
			
			i = i + 1;
		}
		
		return sbOut.toString();
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		// compute horizontal and vertical units
		initializeDialogUnits(parent);
		
		// read configuration
		readConfiguration();
		
		// main composite
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout(3, false);
		mainLayout.horizontalSpacing = 10;
		main.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gd.horizontalSpan = 3;
		main.setLayoutData(gd);
		
		// create controls for search string
		createSearchStringControls(main);
		
		// create controls for file name
		createFileNameControls(main);
		
		// create controls for folder
		createFolderControls(main);
		
		// create advanced options controls
		createAdvancedControls(main);
			
		// set mnemonics
		SystemWidgetHelpers.setMnemonics(parent);
		
		// set the top level control
		setControl(main);
		
		// set help
		SystemWidgetHelpers.setHelp(main, RSEUIPlugin.HELPPREFIX + "rsdi0000");
	}
	
	/**
	 * Creates controls for users to specify the search string.
	 * @param comp the parent composite.
	 */
	private void createSearchStringControls(Composite comp) {

		// label introducing string combo		
		stringLabel = new Label(comp, SWT.LEFT);
		stringLabel.setText(FileResources.RESID_SEARCH_STRING_LABEL_LABEL);
		stringLabel.setToolTipText(FileResources.RESID_SEARCH_STRING_LABEL_TOOLTIP);
		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 3;
		stringLabel.setLayoutData(gd);

		// string combo
		stringCombo = new Combo(comp, SWT.SINGLE | SWT.BORDER);
		stringCombo.setToolTipText(FileResources.RESID_SEARCH_STRING_COMBO_TOOLTIP);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		stringCombo.setLayoutData(gd);
		
		stringCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				container.setPerformActionEnabled(isValid());
			}
		});
		
		stringCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleStringComboSelected();
			}
		});
		
		// checkbox for case sensitive
		caseButton = new Button(comp, SWT.CHECK);
		caseButton.setText(FileResources.RESID_SEARCH_CASE_BUTTON_LABEL);
		caseButton.setToolTipText(FileResources.RESID_SEARCH_CASE_BUTTON_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		caseButton.setLayoutData(gd);
		caseButton.setSelection(initialCaseSensitive);

		// label explaining special characters
		stringHintLabel = new Label(comp, SWT.LEFT);
		stringHintLabel.setText(FileResources.RESID_SEARCH_STRING_HINT_LABEL);
		stringHintLabel.setToolTipText(FileResources.RESID_SEARCH_STRING_HINT_TOOLTIP);
		
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		stringHintLabel.setLayoutData(gd);

		// checkbox for regex
		stringRegexButton = new Button(comp, SWT.CHECK);
		stringRegexButton.setText(FileResources.RESID_SEARCH_STRING_REGEX_LABEL);
		stringRegexButton.setToolTipText(FileResources.RESID_SEARCH_STRING_REGEX_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		stringRegexButton.setLayoutData(gd);
		stringRegexButton.setSelection(initialStringRegex);
		stringHintLabel.setVisible(!stringRegexButton.getSelection());
		
		stringRegexButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stringHintLabel.setVisible(!stringRegexButton.getSelection());
			}
		});
	}
	
	/**
	 * Handles selection of an item from the string combo. Sets the dialog state for the 
	 * selection data corrsponding to the search string selection.
	 */
	private void handleStringComboSelected() {
		
		if (stringCombo.getSelectionIndex() < 0) {
			return;
		}
		
		int index = previousSearchData.size() - 1 - stringCombo.getSelectionIndex();
		
		RemoteSearchData data = (RemoteSearchData)previousSearchData.get(index);
		
		if (data == null || !stringCombo.getText().equals(data.searchString)) {
			return;
		}
		
		// set search string properties
		stringCombo.setText(data.searchString);
		
		// set case sensitive
		caseButton.setSelection(data.caseSensitive);
		
		// set string regex
		stringHintLabel.setVisible(!data.stringRegex);
		stringRegexButton.setSelection(data.stringRegex);
		
		// set types list
		fileNameEditor.setFileNamesText(data.fileNames);
		
		// set file name regex
		fileNameHintLabel.setVisible(!data.fileNameRegex);
		fileNameRegexButton.setSelection(data.fileNameRegex);
		
		// set the remote folder properties
		folderNameEditor.setFolderText(data.profileName, data.connectionName, data.folderName);
		
		// set search archive
		searchArchivesButton.setSelection(data.includeArchives);
		
		// set search subfolders
		searchSubfoldersButton.setSelection(data.includeSubfolders);
	}
	
	/**
	 * Gets the remote file subsystem for the given profile name and connection name.
	 * @return the remote file subsystem, or <code>null</code> if the profile or connection does not exist.
	 */
	private IRemoteFileSubSystem getRemoteFileSubSystem(String profName, String connName) {
		
		if (profName == null || connName == null) {
			return null;
		}
		else {
			ISystemRegistry reg = RSEUIPlugin.getTheSystemRegistry();
			ISystemProfile profile = reg.getSystemProfile(profName);
			
			if (profile == null) {
				return null;
			}
			
			IHost conn = reg.getHost(profile, connName);
			
			if (conn == null) {
				return null;
			}
			
			return RemoteFileUtility.getFileSubSystem(conn);
		}
	}
	
	/**
	 * Creates controls to specify file name.
	 * @param comp the parent composite.
	 */
	private void createFileNameControls(Composite comp) {
		
		// label introducing file name combo	
		fileNameLabel = new Label(comp, SWT.LEFT);
		fileNameLabel.setText(FileResources.RESID_SEARCH_FILENAME_LABEL_LABEL);
		fileNameLabel.setToolTipText(FileResources.RESID_SEARCH_FILENAME_LABEL_TOOLTIP);
		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 3;
		fileNameLabel.setLayoutData(gd);
		
		// file name combo
		fileNameCombo = new Combo(comp, SWT.SINGLE | SWT.BORDER);
		fileNameCombo.setToolTipText(FileResources.RESID_SEARCH_FILENAME_COMBO_TOOLTIP);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fileNameCombo.setLayoutData(gd);
		
		fileNameCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				container.setPerformActionEnabled(isValid());
			}
		});
		
		// browse types button
		fileNameBrowseButton = new Button(comp, SWT.PUSH);
		fileNameBrowseButton.setText(FileResources.RESID_SEARCH_FILENAME_BROWSE_LABEL);
		fileNameBrowseButton.setToolTipText(FileResources.RESID_SEARCH_FILENAME_BROWSE_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fileNameBrowseButton.setLayoutData(gd);
		
		// create the file name editor
		fileNameEditor = new FileNameEditor(fileNameCombo, fileNameBrowseButton);
		
		// label explaining special characters
		fileNameHintLabel = new Label(comp, SWT.LEFT);
		fileNameHintLabel.setText(FileResources.RESID_SEARCH_FILENAME_HINT_LABEL);
		fileNameHintLabel.setToolTipText(FileResources.RESID_SEARCH_FILENAME_HINT_TOOLTIP);
		
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fileNameHintLabel.setLayoutData(gd);
		
		// checkbox for regex
		fileNameRegexButton = new Button(comp, SWT.CHECK);
		fileNameRegexButton.setText(FileResources.RESID_SEARCH_FILENAME_REGEX_LABEL);
		fileNameRegexButton.setToolTipText(FileResources.RESID_SEARCH_FILENAME_REGEX_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fileNameRegexButton.setLayoutData(gd);
		fileNameRegexButton.setSelection(initialFileNameRegex);
		fileNameHintLabel.setVisible(!fileNameRegexButton.getSelection());
		
		fileNameRegexButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileNameHintLabel.setVisible(!fileNameRegexButton.getSelection());
			}
		});
	}
	
	/**
	 * Creates controls to specify folder name.
	 */
	private void createFolderControls(Composite comp) {
		
		// label introducing folder name combo	
		folderNameLabel = new Label(comp, SWT.LEFT);
		folderNameLabel.setText(FileResources.RESID_SEARCH_FOLDERNAME_LABEL_LABEL);
		folderNameLabel.setToolTipText(FileResources.RESID_SEARCH_FOLDERNAME_LABEL_TOOLTIP);
		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 3;
		folderNameLabel.setLayoutData(gd);
		
		// folder name combo
		folderNameCombo = new Combo(comp, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		folderNameCombo.setToolTipText(FileResources.RESID_SEARCH_FOLDERNAME_COMBO_TOOLTIP);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		folderNameCombo.setLayoutData(gd);
		
		folderNameCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				container.setPerformActionEnabled(isValid());
			}
		});
		
		// browse types button
		folderNameBrowseButton = new Button(comp, SWT.PUSH);
		folderNameBrowseButton.setText(FileResources.RESID_SEARCH_FOLDERNAME_BROWSE_LABEL);
		folderNameBrowseButton.setToolTipText(FileResources.RESID_SEARCH_FOLDERNAME_BROWSE_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		folderNameBrowseButton.setLayoutData(gd);
		
		// create the folder name editor
		folderNameEditor = new FolderNameEditor(folderNameCombo, folderNameBrowseButton);
	}
	
	/**
	 * Creates controls for advanced options.
	 */
	private void createAdvancedControls(Composite comp) {
		
		// dummy label	
		Label dummyLabel = new Label(comp, SWT.LEFT);
		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 3;
		dummyLabel.setLayoutData(gd);
		
		// checkbox for search in archive files
		searchArchivesButton = new Button(comp, SWT.CHECK);
		searchArchivesButton.setText(FileResources.RESID_SEARCH_INCLUDE_ARCHIVES_LABEL);
		searchArchivesButton.setToolTipText(FileResources.RESID_SEARCH_INCLUDE_ARCHIVES_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 1;
		searchArchivesButton.setLayoutData(gd);
		
		searchArchivesButton.setSelection(initialSearchArchives);
		
		// checkbox for search in subfolders
		searchSubfoldersButton = new Button(comp, SWT.CHECK);
		searchSubfoldersButton.setText(FileResources.RESID_SEARCH_INCLUDE_SUBFOLDERS_LABEL);
		searchSubfoldersButton.setToolTipText(FileResources.RESID_SEARCH_INCLUDE_SUBFOLDERS_TOOLTIP);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 20;
		searchSubfoldersButton.setLayoutData(gd);
		
		searchSubfoldersButton.setSelection(initialSearchSubfolders);
	}
	
	/**
	 * Returns whether the search string, file name and folder name are all valid.
	 * @return <code>true</code> if all are valid, <code>false</code> otherwise.
	 */
	private boolean isValid() {
		
		// note that we check folder name first for performance reasons (it is most likely to be
		// invalid since we do not allow empty folder name)
		return isFolderNameValid() && isSearchStringValid() && isFileNameValid();
		
		// TODO: error messages!! But are they even visible in the search dialog?
	}
	
	/**
	 * Returns whether search string is valid. Note that search string is valid if empty (we assume empty
	 * search string is the same as "*");
	 * @return <code>true</code> if search string is not <code>null</code>, <code>false</code> otherwise.
	 */
	private boolean isSearchStringValid() {
		
		String searchString = stringCombo.getText();
		
		if (searchString == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Returns whether file name is valid. Note that file name is valid if empty (we assume empty file
	 * name is the same as "*");
	 * @return <code>true</code> if file name is not <code>null</code>, <code>false</code> otherwise.
	 */
	private boolean isFileNameValid() {
		
		String fileNameString = fileNameCombo.getText();
		
		if (fileNameString == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Returns whether folder name is valid.
	 * @return <code>true</code> if folder name is not <code>null</code> or empty, <code>false</code> otherwise.
	 */
	private boolean isFolderNameValid() {
		
		String folderName = folderNameCombo.getText();
		
		if (folderName == null || folderName.trim().length() == 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Get plugin dialog settings.
	 * @return the dialog settings of the plugin.
	 */
	private IDialogSettings getPluginDialogSettings() {
		return RSEUIPlugin.getDefault().getDialogSettings();
	}
		
	/**
	 * Returns the dialog settings for the remote search page.
	 * @return the dialog settings used to store search page configuration.
	 */
	private IDialogSettings getConfigDialogSettings() {
		IDialogSettings settings = getPluginDialogSettings();
		IDialogSettings dialogSettings = settings.getSection(REMOTE_SEARCH_PAGE_NAME);
		
		if (dialogSettings == null) {
			dialogSettings = settings.addNewSection(REMOTE_SEARCH_PAGE_NAME);
		}
			
		return dialogSettings;
	}
	
	/**
	 * Retrieves the configuration from the dialog settings.
	 */
	private void readConfiguration() {
		IDialogSettings s = getConfigDialogSettings();
		initialCaseSensitive = s.getBoolean(STORE_CONFIG_CASE_SENSITIVE);
		initialStringRegex = s.getBoolean(STORE_CONFIG_STRING_REGEX);
		initialFileNameRegex = s.getBoolean(STORE_CONFIG_FILENAME_REGEX);
		initialSearchArchives = s.getBoolean(STORE_CONFIG_INCLUDE_ARCHIVES);
		initialSearchSubfolders = s.getBoolean(STORE_CONFIG_INCLUDE_SUBFOLDERS);
	}
	
	/**
	 * Stores the current configuration to the dialog settings.
	 */
	private void writeConfiguration() {
		IDialogSettings s = getConfigDialogSettings();
		s.put(STORE_CONFIG_CASE_SENSITIVE, caseButton.getSelection());
		s.put(STORE_CONFIG_STRING_REGEX, stringRegexButton.getSelection());
		s.put(STORE_CONFIG_FILENAME_REGEX, fileNameRegexButton.getSelection());
		s.put(STORE_CONFIG_INCLUDE_ARCHIVES, searchArchivesButton.getSelection());
		s.put(STORE_CONFIG_INCLUDE_SUBFOLDERS, searchSubfoldersButton.getSelection());
	}
	
	/**
	 * Reads the data.
	 */
	private void readData() {
		
		// store the size
		int size = 0;
		
		try {
			size = getConfigDialogSettings().getInt(STORE_CONFIG_DATA_SIZE);
		}
		catch (NumberFormatException e) {
			// if a number format exception occurs, it doesn't mean there is an error
			// it simply means this is the first time the dialog is opened
			size = 0;
		}
		
		if (size > 0) {
			// take the minimum of stored size and the maximum size allowable
			// this is unnecessary, since the stored size should never be
			// more than the maximum size, but we check just in case
			size = Math.min(size, MAX_DATA_SIZE);
			previousSearchData = new ArrayList(size);
		}
		
		for (int i = 0; i < size; i++) {
 			IDialogSettings dataSection = getPluginDialogSettings().getSection(STORE_DATA_PREFIX + i);
 			String searchString = dataSection.get(STORE_DATA_SEARCH_STRING);
 			boolean caseSensitive = dataSection.getBoolean(STORE_DATA_CASE_SENSITIVE);
 			boolean stringRegex = dataSection.getBoolean(STORE_DATA_STRING_REGEX);
 			
 			String fileNamesString = dataSection.get(STORE_DATA_FILE_NAMES);
 			boolean fileNameRegex = dataSection.getBoolean(STORE_DATA_FILE_NAME_REGEX);
 			
 			String profileName = dataSection.get(STORE_DATA_PROFILE_NAME);
 			String connectionName = dataSection.get(STORE_DATA_CONNECTION_NAME);
 			String folderName = dataSection.get(STORE_DATA_FOLDER_NAME);
 			
 			boolean searchArchives = dataSection.getBoolean(STORE_DATA_INCLUDE_ARCHIVES);
 			boolean searchSubfolders = dataSection.getBoolean(STORE_DATA_INCLUDE_SUBFOLDERS);
 			
 			RemoteSearchData data = new RemoteSearchData(searchString, caseSensitive, stringRegex, fileNamesString, fileNameRegex, profileName, connectionName, folderName, searchArchives, searchSubfolders);
 			previousSearchData.add(data);
		}
	}
	
	/**
	 * Writes the data.
	 */
	private void writeData() {
		
		// get the size of data
		int size = previousSearchData.size();
		
		// take the minimum of the data size and the maximum size allowable
		// this is unnecessary, since the data size should never be
		// more than the maximum size allowable, but we check just in case
		size = Math.min(size, MAX_DATA_SIZE);
		
		// store the size
		getConfigDialogSettings().put(STORE_CONFIG_DATA_SIZE, size);
		
		IDialogSettings pluginSettings = getPluginDialogSettings();
		
		for (int i = 0; i < size; i++) {
			IDialogSettings dataSection = pluginSettings.getSection(STORE_DATA_PREFIX + i);
			
			if (dataSection == null) {
				dataSection = pluginSettings.addNewSection(STORE_DATA_PREFIX + i);
			}
			
			RemoteSearchData data = (RemoteSearchData)previousSearchData.get(i);
			
			dataSection.put(STORE_DATA_SEARCH_STRING, data.searchString);
			dataSection.put(STORE_DATA_CASE_SENSITIVE, data.caseSensitive);
			dataSection.put(STORE_DATA_STRING_REGEX, data.stringRegex);
			
			dataSection.put(STORE_DATA_FILE_NAMES, data.fileNames);
			dataSection.put(STORE_DATA_FILE_NAME_REGEX, data.fileNameRegex);
			
			dataSection.put(STORE_DATA_PROFILE_NAME, data.profileName);
			dataSection.put(STORE_DATA_CONNECTION_NAME, data.connectionName);
			dataSection.put(STORE_DATA_FOLDER_NAME, data.folderName);
			
			dataSection.put(STORE_DATA_INCLUDE_ARCHIVES, data.includeArchives);
			dataSection.put(STORE_DATA_INCLUDE_SUBFOLDERS, data.includeSubfolders);
		}
	}
}