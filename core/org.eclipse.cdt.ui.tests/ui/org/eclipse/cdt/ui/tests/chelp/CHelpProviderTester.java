/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.tests.chelp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.text.CHelpBookDescriptor;
import org.eclipse.cdt.internal.ui.text.CHelpSettings;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.ICHelpResourceDescriptor;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;

/**
 * 
 */
public class CHelpProviderTester{
	private static final String KEY_PROVIDER_ID = "providerID";
	private static final String KEY_REQUESTED_NAME = "requestedName";
	private static final String KEY_BOOK_TITLE = "bookTitle";
	private static final String KEY_BOOK_TYPE = "bookType";
	
	private Properties fProperties;
	private static CHelpProviderTester fDefaultInstance = null;
	
	private CHelpProviderTester(){
	}
	
	public static CHelpProviderTester getDefault(){
		if(fDefaultInstance == null)
			fDefaultInstance = new CHelpProviderTester();
		return fDefaultInstance;
	}
	
	private class CHelpBook implements ICHelpBook{
		private int fCHelpType;
		private String fTitle;
		
		public CHelpBook(String providerID, int type){
			fCHelpType = type;
			fTitle = generateBookTitle(providerID,type);
		}
			
		public String getTitle(){
			return fTitle;
		}
			
		public int getCHelpType(){
			return fCHelpType;
		}
	}
	
	private class CHelpResourceDescriptor implements ICHelpResourceDescriptor{
		ICHelpBook fBook;
		String fString;
		String fLabel;
		String fHref;
		IHelpResource fResources[];
		
		public CHelpResourceDescriptor(ICHelpBook helpBook, String string, String providerID){
			fBook = helpBook;
			fString = string;
			fHref = string + helpBook.getTitle() + ".html";
			fLabel = generateHelpString(helpBook, string, providerID);
			fResources = new IHelpResource[1];
			fResources[0] = new IHelpResource(){
				public String getHref(){
					return fHref;
				}
				
				public String getLabel(){
					return fLabel;
				}				
			};
		}
	
		public ICHelpBook getCHelpBook(){
			return fBook;
		}
		
		public IHelpResource[] getHelpResources(){
			return fResources;
		}
	}

	private class FunctionSummary implements IFunctionSummary {

        private String fName = "Name";
        private String fReturnType = "ReturnType";
        private String fPrototype = "Prototype";
        private String fSummary = "Summary";
        private String fSynopsis = "Synopsis";
        private class RequiredInclude implements IRequiredInclude {
        	private String include;
        	
        	public RequiredInclude (String file) {
        		include = file;
        	}
        	
        	public String getIncludeName() {
        		return include;
        	}
        	
        	public boolean isStandard() {
        		return true;
        	}
        }
        
        public FunctionSummary(ICHelpBook helpBook, String string, String providerID){
        	fName = string;
        	fSummary = generateHelpString(helpBook, string, providerID);
        }

        public class FunctionPrototypeSummary implements IFunctionPrototypeSummary {
            public String getName()             { return fName; }
            public String getReturnType()       { return fReturnType; }
            public String getArguments()        { return fPrototype; }
            public String getPrototypeString(boolean namefirst) {
                if (true == namefirst) {
                    return fName + " (" + fPrototype + ") " + fReturnType;
                }
                else {
                    return fReturnType + " " + fName + " (" + fPrototype + ")";
                }
            }
        }

        public String getName()                         { return fName; }
        public String getNamespace()                    { return "dummy namespace"; }
        public String getDescription()                  { return fSummary; }
        public IFunctionPrototypeSummary getPrototype() { return new FunctionPrototypeSummary(); }
        
        public IRequiredInclude[] getIncludes() {
        	return (IRequiredInclude[])null; 
        }
        
    }

	private static String generateHelpString(ICHelpBook helpBook, String name, String providerID){
		Properties props = new Properties();
		props.setProperty(KEY_PROVIDER_ID, providerID);
		props.setProperty(KEY_REQUESTED_NAME, name);
		props.setProperty(KEY_BOOK_TITLE, helpBook.getTitle());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try{
			props.store(outputStream,null);
		}
		catch(Exception e){
		}
		return outputStream.toString();
	}
	
	private static String generateBookTitle(String providerID, int bookType){
		Properties props = new Properties();
		props.setProperty(KEY_PROVIDER_ID, providerID);
		props.setProperty(KEY_BOOK_TYPE, String.valueOf(bookType));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try{
			props.store(outputStream,null);
		}
		catch(Exception e){
		}
		return outputStream.toString();
	}

	private CHelpProviderTester(String string) throws IOException{
		fProperties = new Properties();
		ByteArrayInputStream stream = new ByteArrayInputStream(string.getBytes());
		
		try{
			fProperties.load(stream);
		}catch(IOException e){
			//TODO: handle
			throw e;
		}
	}
	
	private String getValueByKey(String key){
		String val = fProperties.getProperty(key);
		if(val == null)
			val = new String();
		return val;
	}
	
	private String getHelpProviderID(){
		return getValueByKey(KEY_PROVIDER_ID);
	}

	private String getRequestedName(){
		return getValueByKey(KEY_REQUESTED_NAME);
	}

	private String getBookTitle(){
		return getValueByKey(KEY_BOOK_TITLE);
	}
	
	public boolean onlyTestInfoProvidersAvailable(){
		IConfigurationElement configElements[] = Platform.getExtensionRegistry().getConfigurationElementsFor(CUIPlugin.PLUGIN_ID, CHelpSettings.CONTRIBUTION_EXTENSION);
		int numExts = 0;
		for(int i = 0; i < configElements.length; i++){
			String id = configElements[i].getAttribute("id");
			if(!id.startsWith(CHelpTest.TEST_EXTENSION_ID_PREFIX))
				return false;
		}
		return true;
	}

	public ICHelpResourceDescriptor[] generateHelpResources(ICHelpBook[] helpBooks, String name, String providerID){
		ICHelpResourceDescriptor des[] = new ICHelpResourceDescriptor[helpBooks.length];
		for(int i = 0; i < helpBooks.length; i++){
			des[i] = new CHelpResourceDescriptor(helpBooks[i],name,providerID);
		}
		return des;
	}
	
	public IFunctionSummary generateFunctionInfo(ICHelpBook[] helpBooks, String name, String providerID){
		if(helpBooks.length == 0)
			return null;
		return new FunctionSummary(helpBooks[0],name,providerID);
	}
	
	public IFunctionSummary[] generateMatchingFunctions(ICHelpBook[] helpBooks, String prefix, String providerID){
		IFunctionSummary sum[] = new IFunctionSummary[helpBooks.length];
		for(int i = 0; i < helpBooks.length; i++){
			sum[i] = new FunctionSummary(helpBooks[i],prefix,providerID);
		}
		return sum;
	}
	
	public ICHelpBook[] generateCHelpBooks(final String providerID){
		ICHelpBook books[] = new ICHelpBook[3];
		books[0] = new CHelpBook(providerID,ICHelpBook.HELP_TYPE_C);
		books[1] = new CHelpBook(providerID,ICHelpBook.HELP_TYPE_CPP);
		books[2] = new CHelpBook(providerID,ICHelpBook.HELP_TYPE_ASM);
		return books;
	}
	
	private void checkResponse(CHelpProviderTester data[], ICHelpInvocationContext context, String name, boolean allBooksResponded){
		CHelpBookDescriptor bookDes[] = CHelpProviderManager.getDefault().getCHelpBookDescriptors(context);
		for(int i = 0; i < data.length; i++){
			CHelpProviderTester tester = data[i];
			Assert.assertTrue("the name passed to CHelpProvider (" + tester.getRequestedName() + ") differs prom tha name passed to manager (" + name + ")",name.equals(tester.getRequestedName()));
			String bookTitle = tester.getBookTitle();
			int j = 0;
			for(; j < bookDes.length; j++){
				if(bookTitle.equals(bookDes[j].getCHelpBook().getTitle())){
					Assert.assertTrue("provider was requested for help in disabled book",bookDes[j].isEnabled());
					break;
				}
			}
			Assert.assertFalse("provider was requested for help in non-existent book",j == bookDes.length);
		}
		
		if(allBooksResponded){
			for(int i = 0; i < bookDes.length; i++){
				if(bookDes[i].isEnabled()){
					String bookTitle = bookDes[i].getCHelpBook().getTitle();
					int j = 0;
					for(; j < data.length; j++){
						if(bookTitle.equals(data[j].getBookTitle()))
							break;
					}
					Assert.assertFalse("provider was not requested for help in enabled book",j == bookDes.length);
				}
			}
		}
	}

	public void checkHelpResources(ICHelpResourceDescriptor helpDescriptors[], ICHelpInvocationContext context, String name){
		if(helpDescriptors == null || helpDescriptors.length == 0)
			return;
		List dataList = new ArrayList(helpDescriptors.length);
		for(int i = 0; i < helpDescriptors.length; i++){
			try{
				dataList.add(new CHelpProviderTester(helpDescriptors[i].getHelpResources()[0].getLabel()));
			}catch(IOException e){
				Assert.fail("checkHelpResources failed to instantiate CHelpProviderTester, IOException occured: " + e.getMessage());
			}
		}
		if(dataList.size() > 0)
			checkResponse((CHelpProviderTester[])dataList.toArray(new CHelpProviderTester[dataList.size()]), context, name, true);
	}

	public void checkMatchingFunctions(IFunctionSummary summaries[], ICHelpInvocationContext context, String name){
		if(summaries == null || summaries.length == 0)
			return;
		List dataList = new ArrayList(summaries.length);
		for(int i = 0; i < summaries.length; i++){
			try{
				dataList.add(new CHelpProviderTester(summaries[i].getDescription()));
			}catch(IOException e){
				Assert.fail("checkMatchingFunctions failed to instantiate CHelpProviderTester, IOException occured: " + e.getMessage());
			}
		}
		if(dataList.size() > 0)
			checkResponse((CHelpProviderTester[])dataList.toArray(new CHelpProviderTester[dataList.size()]), context, name, true);
	}
	
	public void checkFunctionInfo(IFunctionSummary summary, ICHelpInvocationContext context, String name){
		if(summary == null)
			return;
		CHelpProviderTester data[] = new CHelpProviderTester[1];
		try{
			data[0] = new CHelpProviderTester(summary.getDescription());
			checkResponse(data, context, name, false);
		}catch(IOException e){
			Assert.fail("checkFunctionInfo failed to instantiate CHelpProviderTester, IOException occured: " + e.getMessage());
		}
		
	}
}
