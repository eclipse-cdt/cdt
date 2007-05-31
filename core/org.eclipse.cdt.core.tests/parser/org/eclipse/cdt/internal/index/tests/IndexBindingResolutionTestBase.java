/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.pdom.tests.PDOMPrettyPrinter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/**
 * When the PDOM is used to avoid parsing work (i.e. an AST is obtained which
 * is backed by the PDOM), it must be possible to resolve which binding a name
 * in the AST is referring to. If the binding is not defined in the AST fragment
 * then it is assumed to have come from a file which is already indexed.
 * 
 * This class is for testing the process by which bindings are looked up in
 * the PDOM purely from AST information (i.e. without a real binding from the DOM)
 */
public abstract class IndexBindingResolutionTestBase extends BaseTestCase {
	private static final boolean DEBUG= false;
	protected ITestStrategy strategy;
	
	public void setStrategy(ITestStrategy strategy) {
		this.strategy = strategy;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		strategy.setUp();
	}
	
	protected void tearDown() throws Exception {
		strategy.tearDown();
		super.tearDown();
	}
	
	protected IASTName[] findNames(String section, int len) {
		// get the language from the language manager
		ILanguage language = null;
		ICProject cproject = strategy.getCProject();
		IASTTranslationUnit ast = strategy.getAst();
		try {
			IProject project = cproject.getProject();
			ICConfigurationDescription configuration = CoreModel.getDefault().getProjectDescription(project, false).getActiveConfiguration();
			language = LanguageManager.getInstance().getLanguageForFile(strategy.getAst().getFilePath(), project, configuration);
		} catch (CoreException e) {
			fail("Unexpected exception while getting language for file.");
		}
		
		
		assertNotNull("No language for file " + ast.getFilePath().toString(), language);
		
		return language.getSelectedNames(ast, strategy.getTestData()[1].indexOf(section), len);
	}

	
	protected IBinding getBindingFromASTName(String section, int len) {
		return getBindingFromASTName(section, len, false);
	}
	
	protected IBinding getBindingFromASTName(String section, int len, boolean matchLength) {
		IASTName[] names= findNames(section, len);
		if(matchLength) {
			List lnames= new ArrayList(Arrays.asList(names));
			for(ListIterator li= lnames.listIterator(); li.hasNext(); ) {
				IASTName name= (IASTName) li.next();
				if(name.getRawSignature().length()!=len) {
					li.remove();
				}
			}
			names= (IASTName[]) lnames.toArray(new IASTName[lnames.size()]);
		}
		assertEquals("<>1 name found for \""+section+"\"", 1, names.length);
		IBinding binding = names[0].resolveBinding();
		assertNotNull("No binding for "+names[0].getRawSignature(), binding);
		assertFalse("Binding is a ProblemBinding for name "+names[0].getRawSignature(), IProblemBinding.class.isAssignableFrom(names[0].resolveBinding().getClass()));
		return names[0].resolveBinding();
	}

	protected IBinding getProblemFromASTName(String section, int len) {
		// get the language from the language manager
		ILanguage language = null;
		ICProject cproject = strategy.getCProject();
		IASTTranslationUnit ast = strategy.getAst();
		try {
			IProject project = cproject.getProject();
			ICConfigurationDescription configuration = CoreModel.getDefault().getProjectDescription(project, false).getActiveConfiguration();
			language = LanguageManager.getInstance().getLanguageForFile(ast.getFilePath(), project, configuration);
		} catch (CoreException e) {
			fail("Unexpected exception while getting language for file.");
		}
		
		assertNotNull("No language for file " + ast.getFilePath().toString(), language);
		
		IASTName[] names= language.getSelectedNames(ast, strategy.getTestData()[1].indexOf(section), len);
		assertEquals("<>1 name found for \""+section+"\"", 1, names.length);
		IBinding binding = names[0].resolveBinding();
		assertNotNull("No binding for "+names[0].getRawSignature(), binding);
		assertTrue("Binding is not a ProblemBinding for name "+names[0].getRawSignature(), IProblemBinding.class.isAssignableFrom(names[0].resolveBinding().getClass()));
		return names[0].resolveBinding();
	}
	
	protected static void assertQNEquals(String expectedQN, IBinding b) {
		try {
			assertInstance(b, IBinding.class);
			if(b instanceof ICPPBinding) {
				assertEquals(expectedQN, CPPVisitor.renderQualifiedName(((ICPPBinding)b).getQualifiedName()));
			} else {
				assertEquals(expectedQN, b.getName());
			}
		} catch(DOMException de) {
			fail(de.getMessage());
		}
	}

	protected IType getVariableType(IBinding binding) throws DOMException {
		assertTrue(binding instanceof IVariable);
		return ((IVariable)binding).getType();
	}

	protected IType getPtrType(IBinding binding) throws DOMException {
		// assert binding is a variable
		IVariable v = (IVariable) binding;
		IPointerType ptr = (IPointerType) v.getType();
		return ptr.getType();
	}

	protected void assertParamType(int index, Class type, IType function) throws DOMException {
		// assert function is IFunctionType
		IFunctionType ft = (IFunctionType) function;
		assertTrue(type.isInstance((ft.getParameterTypes()[index])));
	}

	protected void assertCompositeTypeParam(int index, int compositeTypeKey, IType function, String qn) throws DOMException {
		// assert function is IFunctionType
		IFunctionType ft = (IFunctionType) function;
		assertTrue(ICPPClassType.class.isInstance((ft.getParameterTypes()[index])));
		assertEquals(compositeTypeKey, ((ICPPClassType)ft.getParameterTypes()[index]).getKey());
		assertEquals(qn, CPPVisitor.renderQualifiedName(((ICPPClassType)ft.getParameterTypes()[index]).getQualifiedName()));
	}

	protected static void assertInstance(Object o, Class c) {
		assertNotNull(o);
		assertTrue("Expected "+c.getName()+" but got "+o.getClass().getName(), c.isInstance(o));
	}
	
	protected String readTaggedComment(final String tag) throws IOException {
		return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "parser", getClass(), tag);
	}

	protected IIndex getIndex() {
		return strategy.getIndex();
	}

	interface ITestStrategy {
		IIndex getIndex();
		void setUp() throws Exception;
		void tearDown() throws Exception;
		public IASTTranslationUnit getAst();
		public StringBuffer[] getTestData();
		public ICProject getCProject();
		public boolean isCompositeIndex();
	}
	
	protected static void assertVariable(IBinding b, String qn, Class expType, String expTypeQN) {
		try {
			assertInstance(b, IVariable.class);
			IVariable variable = (IVariable) b;
			assertQNEquals(qn, variable);
			assertInstance(variable.getType(), expType);
			if(expTypeQN!=null) {
				IType type= variable.getType();
				assertInstance(type, IBinding.class);
				assertQNEquals(expTypeQN, (IBinding) type);
			}
		} catch(DOMException de) {
			fail(de.getMessage());
		}
	}
	
	protected static void assertTypeContainer(IType conType, String expQN, Class containerType, Class expContainedType, String expContainedTypeQN) {
		try {
			assertInstance(conType, ITypeContainer.class);
			assertInstance(conType, containerType);
			IType containedType= ((ITypeContainer)conType).getType();
			assertInstance(containedType, expContainedType);
			if(expContainedTypeQN!=null) {
				assertInstance(containedType, IBinding.class);
				assertQNEquals(expContainedTypeQN, (IBinding) containedType);
			}
		} catch(DOMException de) {
			fail(de.getMessage());
		}
	}

	class SinglePDOMTestStrategy implements ITestStrategy {
		private IIndex index;
		private ICProject cproject;
		private StringBuffer[] testData;
		private IASTTranslationUnit ast;
		private boolean cpp;

		public SinglePDOMTestStrategy(boolean cpp) {
			this.cpp = cpp;
		}

		public ICProject getCProject() {
			return cproject;
		}
		
		public StringBuffer[] getTestData() {
			return testData;
		}

		public IASTTranslationUnit getAst() {
			return ast;
		}

		public void setUp() throws Exception {
			cproject = cpp ? CProjectHelper.createCCProject(getName()+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) 
					: CProjectHelper.createCProject(getName()+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			Bundle b = CTestPlugin.getDefault().getBundle();
			testData = TestSourceReader.getContentsForTest(b, "parser", IndexBindingResolutionTestBase.this.getClass(), getName(), 2);

			IFile file = TestSourceReader.createFile(cproject.getProject(), new Path("header.h"), testData[0].toString());
			CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));

			IFile cppfile= TestSourceReader.createFile(cproject.getProject(), new Path("references.c" + (cpp ? "pp" : "")), testData[1].toString());
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
			
			if(DEBUG) {
				((PDOM)CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
			}

			index= CCorePlugin.getIndexManager().getIndex(cproject);

			index.acquireReadLock();
			ast = TestSourceReader.createIndexBasedAST(index, cproject, cppfile);
		}

		public void tearDown() throws Exception {
			if(index != null) {
				index.releaseReadLock();
			}
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}

		public IIndex getIndex() {
			return index;
		}
		
		public boolean isCompositeIndex() {
			return false;
		}
	}

	class ReferencedProject implements ITestStrategy {
		private IIndex index;
		private ICProject cproject, referenced;
		private StringBuffer[] testData;
		private IASTTranslationUnit ast;
		private boolean cpp;

		public ReferencedProject(boolean cpp) {
			this.cpp = cpp;
		}

		public ICProject getCProject() {
			return cproject;
		}
		
		public void tearDown() throws Exception {
			if(index != null) {
				index.releaseReadLock();
			}
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
			if (referenced != null) {
				referenced.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}

		public void setUp() throws Exception {
			cproject= cpp ? CProjectHelper.createCCProject("OnlineContent"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER)
					: CProjectHelper.createCProject("OnlineContent"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			Bundle b= CTestPlugin.getDefault().getBundle();
			testData= TestSourceReader.getContentsForTest(b, "parser", IndexBindingResolutionTestBase.this.getClass(), getName(), 2);
			referenced = createReferencedContent();

			IFile references= TestSourceReader.createFile(cproject.getProject(), new Path("refs.c" + (cpp ? "pp" : "")), testData[1].toString());

			IProject[] refs = new IProject[] {referenced.getProject()};
			IProjectDescription pd = cproject.getProject().getDescription();
			pd.setReferencedProjects(refs);
			cproject.getProject().setDescription(pd, new NullProgressMonitor());

			IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
			IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
			CCorePlugin.getIndexManager().reindex(cproject);
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
			
			if(DEBUG) {
				System.out.println("Online: "+getName());
			 	((PDOM)CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
			}

			index= CCorePlugin.getIndexManager().getIndex(cproject);
			index.acquireReadLock();
			ast= TestSourceReader.createIndexBasedAST(index, cproject, references);
		}

		protected ICProject createReferencedContent() throws CoreException {
			ICProject referenced = cpp ? CProjectHelper.createCCProject("ReferencedContent"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER)
					: CProjectHelper.createCProject("ReferencedContent"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			String content = testData[0].toString();
			IFile file = TestSourceReader.createFile(referenced.getProject(), new Path("header.h"), content);
			
			IndexerPreferences.set(referenced.getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, "true");
			IndexerPreferences.set(referenced.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
			CCorePlugin.getIndexManager().reindex(referenced);
			
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
			
			if(DEBUG) {
				System.out.println("Referenced: "+getName());
				((PDOM)CCoreInternals.getPDOMManager().getPDOM(referenced)).accept(new PDOMPrettyPrinter());
			}
			
			return referenced;
		}

		public IASTTranslationUnit getAst() {
			return ast;
		}

		public IIndex getIndex() {
			return index;
		}

		public StringBuffer[] getTestData() {
			return testData;
		}
		
		public boolean isCompositeIndex() {
			return true;
		}
	}

	/**
	 * When a test is failing only for the strategy where the test data is split over
	 * multiple index fragements, we artificially fail the single fragment strategy also.
	 * This is not ideal, but as both strategies behaviour are typically the same, is 
	 * quite rare.
	 */
	protected void fakeFailForSingle() {
		if(getName().startsWith("_") && strategy instanceof SinglePDOMTestStrategy) {
			fail("Artificially failing - see IndexBindingResolutionTestBase.fakeFailForSingle()");
		}
	}
}