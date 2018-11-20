/**********************************************************************
 * Copyright (c) 2004, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.tests.chelp;

import static org.eclipse.cdt.ui.tests.chelp.CHelpTest.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.junit.Assert;

/**
 *
 */
public class CHelpProviderTester {
	private static final String KEY_PROVIDER_ID = "providerID";
	private static final String KEY_REQUESTED_NAME = "requestedName";
	private static final String KEY_BOOK_TITLE = "bookTitle";
	private static final String KEY_BOOK_TYPE = "bookType";

	private Properties fProperties;
	private static CHelpProviderTester fDefaultInstance;

	private CHelpProviderTester() {
	}

	public static CHelpProviderTester getDefault() {
		if (fDefaultInstance == null)
			fDefaultInstance = new CHelpProviderTester();
		return fDefaultInstance;
	}

	private class CHelpBook implements ICHelpBook {
		private int fCHelpType;
		private String fTitle;
		private List<IFunctionSummary> fFunctions = new ArrayList<>();

		public CHelpBook(String providerID, int type) {
			fCHelpType = type;
			fTitle = generateBookTitle(providerID, type);
			if (fCHelpType == HELP_TYPE_C) {
				fFunctions.add(new FunctionSummary(this, "setvbuf", providerID));
				fFunctions.add(new FunctionSummary(this, "wait", providerID));
			}
		}

		@Override
		public String getTitle() {
			return fTitle;
		}

		@Override
		public int getCHelpType() {
			return fCHelpType;
		}

		public List<IFunctionSummary> getMatchingFunctions(String prefix) {
			List<IFunctionSummary> result = new ArrayList<>();
			for (IFunctionSummary function : fFunctions) {
				if (function.getName().startsWith(prefix)) {
					result.add(function);
				}
			}
			return result;
		}
	}

	private class CHelpResourceDescriptor implements ICHelpResourceDescriptor {
		ICHelpBook fBook;
		String fString;
		String fLabel;
		String fHref;
		IHelpResource fResources[];

		public CHelpResourceDescriptor(ICHelpBook helpBook, String string, String providerID) {
			fBook = helpBook;
			fString = string;
			fHref = string + helpBook.getTitle() + ".html";
			fLabel = generateHelpString(helpBook, string, providerID);
			fResources = new IHelpResource[1];
			fResources[0] = new IHelpResource() {
				@Override
				public String getHref() {
					return fHref;
				}

				@Override
				public String getLabel() {
					return fLabel;
				}
			};
		}

		@Override
		public ICHelpBook getCHelpBook() {
			return fBook;
		}

		@Override
		public IHelpResource[] getHelpResources() {
			return fResources;
		}
	}

	private class FunctionSummary implements IFunctionSummary {
		private String fName = "Name";
		private String fReturnType = "ReturnType";
		private String fPrototype = "Prototype";
		private String fSummary = "Summary";
		private String fSynopsis = "Synopsis";
		private IRequiredInclude[] incs = new IRequiredInclude[] { new RequiredInclude("dummy.h") };

		private class RequiredInclude implements IRequiredInclude {
			private String include;

			public RequiredInclude(String file) {
				include = file;
			}

			@Override
			public String getIncludeName() {
				return include;
			}

			@Override
			public boolean isStandard() {
				return true;
			}
		}

		public FunctionSummary(ICHelpBook helpBook, String string, String providerID) {
			fName = string;
			fSummary = generateHelpString(helpBook, string, providerID);
		}

		public class FunctionPrototypeSummary implements IFunctionPrototypeSummary {
			@Override
			public String getName() {
				return fName;
			}

			@Override
			public String getReturnType() {
				return fReturnType;
			}

			@Override
			public String getArguments() {
				return fPrototype;
			}

			@Override
			public String getPrototypeString(boolean namefirst) {
				if (true == namefirst) {
					return fName + " (" + fPrototype + ") " + fReturnType;
				} else {
					return fReturnType + " " + fName + " (" + fPrototype + ")";
				}
			}
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public String getNamespace() {
			return "dummy namespace";
		}

		@Override
		public String getDescription() {
			return fSummary;
		}

		@Override
		public IFunctionPrototypeSummary getPrototype() {
			return new FunctionPrototypeSummary();
		}

		@Override
		public IRequiredInclude[] getIncludes() {
			return incs;
		}
	}

	private static String generateHelpString(ICHelpBook helpBook, String name, String providerID) {
		Properties props = new Properties();
		props.setProperty(KEY_PROVIDER_ID, providerID);
		props.setProperty(KEY_REQUESTED_NAME, name);
		props.setProperty(KEY_BOOK_TITLE, helpBook.getTitle());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			props.store(outputStream, null);
		} catch (IOException e) {
			fail(e);
		}
		return outputStream.toString();
	}

	private static String generateBookTitle(String providerID, int bookType) {
		Properties props = new Properties();
		props.setProperty(KEY_PROVIDER_ID, providerID);
		props.setProperty(KEY_BOOK_TYPE, String.valueOf(bookType));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			props.store(outputStream, null);
		} catch (IOException e) {
			fail(e);
		}
		return outputStream.toString();
	}

	private CHelpProviderTester(String string) {
		fProperties = new Properties();
		ByteArrayInputStream stream = new ByteArrayInputStream(string.getBytes());
		try {
			fProperties.load(stream);
		} catch (IOException e) {
			fail(e);
		}
	}

	private String getValueByKey(String key) {
		String val = fProperties.getProperty(key);
		if (val == null)
			val = ""; //$NON-NLS-1$
		return val;
	}

	private String getHelpProviderID() {
		return getValueByKey(KEY_PROVIDER_ID);
	}

	private String getRequestedName() {
		return getValueByKey(KEY_REQUESTED_NAME);
	}

	private String getBookTitle() {
		return getValueByKey(KEY_BOOK_TITLE);
	}

	public boolean onlyTestInfoProvidersAvailable() {
		IConfigurationElement configElements[] = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CUIPlugin.PLUGIN_ID, CHelpSettings.CONTRIBUTION_EXTENSION);
		int numExts = 0;
		for (int i = 0; i < configElements.length; i++) {
			String id = configElements[i].getAttribute("id");
			if (!id.startsWith(CHelpTest.TEST_EXTENSION_ID_PREFIX))
				return false;
		}
		return true;
	}

	public ICHelpResourceDescriptor[] generateHelpResources(ICHelpBook[] helpBooks, String name, String providerID) {
		ICHelpResourceDescriptor des[] = new ICHelpResourceDescriptor[helpBooks.length];
		for (int i = 0; i < helpBooks.length; i++) {
			des[i] = new CHelpResourceDescriptor(helpBooks[i], name, providerID);
		}
		return des;
	}

	public IFunctionSummary generateFunctionInfo(ICHelpBook[] helpBooks, String name, String providerID) {
		if (helpBooks.length == 0)
			return null;
		return new FunctionSummary(helpBooks[0], name, providerID);
	}

	public IFunctionSummary[] generateMatchingFunctions(ICHelpBook[] helpBooks, String prefix, String providerID) {
		ArrayList<IFunctionSummary> lst = new ArrayList<>();
		for (ICHelpBook helpBook : helpBooks) {
			if (helpBook instanceof CHelpBook) {
				lst.addAll(((CHelpBook) helpBook).getMatchingFunctions(prefix));
			}
		}
		return lst.toArray(new IFunctionSummary[lst.size()]);
	}

	public ICHelpBook[] generateCHelpBooks(final String providerID) {
		ICHelpBook books[] = new ICHelpBook[3];
		books[0] = new CHelpBook(providerID, ICHelpBook.HELP_TYPE_C);
		books[1] = new CHelpBook(providerID, ICHelpBook.HELP_TYPE_CPP);
		books[2] = new CHelpBook(providerID, ICHelpBook.HELP_TYPE_ASM);
		return books;
	}

	private void checkResponse(CHelpProviderTester data[], ICHelpInvocationContext context, String name,
			boolean allBooksResponded) {
		CHelpBookDescriptor bookDes[] = CHelpProviderManager.getDefault().getCHelpBookDescriptors(context);
		for (int i = 0; i < data.length; i++) {
			CHelpProviderTester tester = data[i];
			Assert.assertTrue(
					"the name passed to CHelpProvider (" + tester.getRequestedName()
							+ ") differs prom tha name passed to manager (" + name + ")",
					name.equals(tester.getRequestedName()));
			String bookTitle = tester.getBookTitle();
			int j = 0;
			for (; j < bookDes.length; j++) {
				if (bookTitle.equals(bookDes[j].getCHelpBook().getTitle())) {
					Assert.assertTrue("provider was requested for help in disabled book", bookDes[j].isEnabled());
					break;
				}
			}
			Assert.assertFalse("provider was requested for help in non-existent book", j == bookDes.length);
		}

		if (allBooksResponded) {
			for (int i = 0; i < bookDes.length; i++) {
				if (bookDes[i].isEnabled()) {
					String bookTitle = bookDes[i].getCHelpBook().getTitle();
					int j = 0;
					for (; j < data.length; j++) {
						if (bookTitle.equals(data[j].getBookTitle()))
							break;
					}
					Assert.assertFalse("provider was not requested for help in enabled book", j == bookDes.length);
				}
			}
		}
	}

	public void checkHelpResources(ICHelpResourceDescriptor helpDescriptors[], ICHelpInvocationContext context,
			String name) {
		if (helpDescriptors == null || helpDescriptors.length == 0)
			return;
		List<CHelpProviderTester> dataList = new ArrayList<>(helpDescriptors.length);
		for (int i = 0; i < helpDescriptors.length; i++) {
			dataList.add(new CHelpProviderTester(helpDescriptors[i].getHelpResources()[0].getLabel()));
		}
		if (!dataList.isEmpty())
			checkResponse(dataList.toArray(new CHelpProviderTester[dataList.size()]), context, name, true);
	}

	public void checkMatchingFunctions(IFunctionSummary summaries[], ICHelpInvocationContext context, String name) {
		if (summaries == null || summaries.length == 0)
			return;
		List<CHelpProviderTester> dataList = new ArrayList<>(summaries.length);
		for (int i = 0; i < summaries.length; i++) {
			dataList.add(new CHelpProviderTester(summaries[i].getDescription()));
		}
		if (!dataList.isEmpty())
			checkResponse(dataList.toArray(new CHelpProviderTester[dataList.size()]), context, name, true);
	}

	public void checkFunctionInfo(IFunctionSummary summary, ICHelpInvocationContext context, String name) {
		if (summary == null)
			return;
		CHelpProviderTester data[] = new CHelpProviderTester[1];
		data[0] = new CHelpProviderTester(summary.getDescription());
		checkResponse(data, context, name, false);
	}
}
