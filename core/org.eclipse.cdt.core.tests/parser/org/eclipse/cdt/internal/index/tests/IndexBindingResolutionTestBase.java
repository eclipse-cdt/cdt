/*******************************************************************************
 * Copyright (c) 2006, 2014 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
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
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.pdom.tests.PDOMPrettyPrinter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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
 * the PDOM purely from AST information (i.e. without a real binding from the DOM).
 */
public abstract class IndexBindingResolutionTestBase extends BaseTestCase {
	private static final boolean DEBUG= false;
	private static final String END_OF_ADDED_CODE_MARKER = "/*END_OF_ADDED_CODE*/";
	protected ITestStrategy strategy;

	public void setStrategy(ITestStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		strategy.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		strategy.tearDown();
		super.tearDown();
	}

	protected IASTName findName(String section, int len, boolean preferImplicitName) {
		if (len <= 0)
			len += section.length();

		for (int i = 0; i < strategy.getAstCount(); i++) {
			IASTTranslationUnit ast = strategy.getAst(i);
			final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
			String source = ast.getRawSignature();
			// Skip the automatically added code.
			int offset = source.indexOf(END_OF_ADDED_CODE_MARKER);
			if (offset >= 0) {
				offset += END_OF_ADDED_CODE_MARKER.length();
				if (source.charAt(offset) == '\n')
					offset++;
			} else {
				offset = 0;
			}
			offset = source.indexOf(section, offset);
			if (offset >= 0) {
				IASTName name= null;
				if (!preferImplicitName)
					name= nodeSelector.findName(offset, len);
				if (name == null)
					name= nodeSelector.findImplicitName(offset, len);
				return name;
			}
		}

		return null;
	}
	
	protected IASTName findName(String section, int len) {
		return findName(section, len, false);
	}
	
	protected IASTName findImplicitName(String section, int len) {
		return findName(section, len, true);
	}

	/**
	 * Attempts to get an IBinding from the initial specified number of characters
	 * from the specified code fragment. Fails the test if
	 * <ul>
	 *  <li> There is not a unique name with the specified criteria
	 *  <li> The binding associated with the name is null or a problem binding
     *  <li> The binding is not an instance of the specified class
	 * </ul>
	 * @param section the code fragment to search for in the AST. The first occurrence of an identical section is used.
	 * @param len the length of the specified section to use as a name. This can also be useful for distinguishing between
	 * template names, and template ids.
	 * @param clazz an expected class type or interface that the binding should extend/implement
	 * @return the associated name's binding
	 */
	protected <T> T getBindingFromASTName(String section, int len, Class<T> clazz, Class... cs) {
		if (len <= 0)
			len += section.length();

		IASTName name= findName(section, len);
		assertNotNull("Name not found for \"" + section + "\"", name);
		assertEquals(section.substring(0, len), name.getRawSignature());

		IBinding binding = name.resolveBinding();
		assertNotNull("No binding for " + name.getRawSignature(), binding);
		assertFalse("Binding is a ProblemBinding for name \"" + name.getRawSignature() + "\"", IProblemBinding.class.isAssignableFrom(name.resolveBinding().getClass()));
		assertInstance(binding, clazz, cs);
		return clazz.cast(binding);
	}
	
	/**
	 * Attempts to get an IBinding attached to an implicit name from the initial specified 
	 * number of characters from the specified code fragment. Fails the test if
	 * <ul>
	 *  <li> There is not a unique implicit name with the specified criteria
	 *  <li> The binding associated with the implicit name is null or a problem binding
     *  <li> The binding is not an instance of the specified class
	 * </ul>
	 * @param section the code fragment to search for in the AST. The first occurrence of an identical section is used.
	 * @param len the length of the specified section to use as a name
	 * @param clazz an expected class type or interface that the binding should extend/implement
	 * @return the associated implicit name's binding
	 */
	protected <T> T getBindingFromImplicitASTName(String section, int len, Class<T> clazz, Class... cs) {
		if (len <= 0)
			len += section.length();

		IASTName name= findImplicitName(section, len);
		assertNotNull("Name not found for \"" + section + "\"", name);
		assertEquals(section.substring(0, len), name.getRawSignature());

		IBinding binding = name.resolveBinding();
		assertNotNull("No binding for " + name.getRawSignature(), binding);
		assertFalse("Binding is a ProblemBinding for name \"" + name.getRawSignature() + "\"", IProblemBinding.class.isAssignableFrom(name.resolveBinding().getClass()));
		assertInstance(binding, clazz, cs);
		return clazz.cast(binding);
	}

	/*
	 * @see IndexBindingResolutionTestBase#getBindingFromASTName(String, int, Class<T>, Class...)
	 */
	protected <T extends IBinding> T getBindingFromASTName(String section, int len) {
		if (len <= 0)
			len += section.length();

		IASTName name= findName(section, len);
		assertNotNull("Name not found for \"" + section + "\"", name);
		assertEquals(section.substring(0, len), name.getRawSignature());

		IBinding binding = name.resolveBinding();
		assertNotNull("No binding for " + name.getRawSignature(), binding);
		assertFalse("Binding is a ProblemBinding for name \"" + name.getRawSignature() + "\"", IProblemBinding.class.isAssignableFrom(name.resolveBinding().getClass()));
		return (T) binding;
	}

	protected <T extends IBinding> T getBindingFromFirstIdentifier(String section) {
		return getBindingFromASTName(section, getIdentifierLength(section));
	}

	protected <T extends IBinding> T getBindingFromFirstIdentifier(String section, Class<T> clazz, Class... cs) {
		return getBindingFromASTName(section, getIdentifierLength(section), clazz, cs);
	}

	/*
	 * @see IndexBindingResolutionTestBase#getBindingFromImplicitASTName(String, int, Class<T>, Class ...)
	 */
	protected <T extends IBinding> T getBindingFromImplicitASTName(String section, int len) {
		if (len <= 0)
			len += section.length();

		IASTName name= findImplicitName(section, len);
		assertNotNull("Name not found for \"" + section + "\"", name);
		assertEquals(section.substring(0, len), name.getRawSignature());

		IBinding binding = name.resolveBinding();
		assertNotNull("No binding for " + name.getRawSignature(), binding);
		assertFalse("Binding is a ProblemBinding for name \"" + name.getRawSignature() + "\"",
				IProblemBinding.class.isAssignableFrom(name.resolveBinding().getClass()));
		return (T) binding;
	}

	/**
	 * Attempts to verify that the resolved binding for a name is a problem binding.
	 * @param section the code fragment to search for in the AST. The first occurrence of an identical section is used.
	 * @param len the length of the specified section to use as a name
	 * @return the associated name's binding
	 */
	protected IBinding getProblemFromASTName(String section, int len) {
		IASTName name= findName(section, len);
		assertNotNull("Name not found for \"" + section + "\"", name);
		assertEquals(section.substring(0, len), name.getRawSignature());

		IBinding binding = name.resolveBinding();
		assertNotNull("No binding for " + name.getRawSignature(), binding);
		assertTrue("Binding is not a ProblemBinding for name \"" + name.getRawSignature() + "\"",
				IProblemBinding.class.isAssignableFrom(name.resolveBinding().getClass()));
		return name.resolveBinding();
	}

	protected IBinding getProblemFromFirstIdentifier(String section) {
		return getProblemFromASTName(section, getIdentifierLength(section));
	}

	protected static void assertQNEquals(String expectedQN, IBinding b) {
		assertInstance(b, IBinding.class);
		if (b instanceof ICPPBinding) {
			assertEquals(expectedQN, ASTTypeUtil.getQualifiedName((ICPPBinding) b));
		} else {
			assertEquals(expectedQN, b.getName());
		}
	}

	protected IType getVariableType(IBinding binding) throws DOMException {
		assertTrue(binding instanceof IVariable);
		return ((IVariable) binding).getType();
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
		assertEquals(compositeTypeKey, ((ICPPClassType) ft.getParameterTypes()[index]).getKey());
		assertEquals(qn, ASTTypeUtil.getQualifiedName((ICPPClassType) ft.getParameterTypes()[index]));
	}

	protected static <T> T assertInstance(Object o, Class<T> clazz, Class ... cs) {
		assertNotNull("Expected " + clazz.getName() + " but got null", o);
		assertTrue("Expected " + clazz.getName() + " but got " + o.getClass().getName(), clazz.isInstance(o));
		for (Class c : cs) {
			assertTrue("Expected " + clazz.getName() + " but got " + o.getClass().getName(), c.isInstance(o));
		}
		return clazz.cast(o);
	}

	protected String readTaggedComment(final String tag) throws IOException {
		return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "parser", getClass(), tag);
	}

	protected IIndex getIndex() {
		return strategy.getIndex();
	}

	protected static void assertVariable(IBinding b, String qn, Class expType, String expTypeQN) {
		assertInstance(b, IVariable.class);
		IVariable variable = (IVariable) b;
		assertQNEquals(qn, variable);
		assertInstance(variable.getType(), expType);
		if (expTypeQN != null) {
			IType type= variable.getType();
			assertInstance(type, IBinding.class);
			assertQNEquals(expTypeQN, (IBinding) type);
		}
	}

	protected static void assertTypeContainer(IType conType, String expQN, Class containerType, Class expContainedType, String expContainedTypeQN) {
		assertInstance(conType, ITypeContainer.class);
		assertInstance(conType, containerType);
		IType containedType= ((ITypeContainer) conType).getType();
		assertInstance(containedType, expContainedType);
		if (expContainedTypeQN != null) {
			assertInstance(containedType, IBinding.class);
			assertQNEquals(expContainedTypeQN, (IBinding) containedType);
		}
	}

	protected final void checkBindings() {
		for (int i = 0; i < strategy.getAstCount(); i++) {
			checkBindings(strategy.getAst(i));
		}
	}

	protected final void checkBindings(IASTTranslationUnit ast) {
		NameCollector col = new NameCollector();
		ast.accept(col);
		for (IASTName n : col.nameList) {
			assertFalse("ProblemBinding for " + n.getRawSignature(), n.resolveBinding() instanceof IProblemBinding);
		}
	}

	protected int getIdentifierLength(String str) {
		int i;
		for (i = 0; i < str.length() && Character.isJavaIdentifierPart(str.charAt(i)); ++i) {
		}
		return i;
	}

	static protected class NameCollector extends ASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List<IASTName> nameList = new ArrayList<>();

        @Override
		public int visit(IASTName name) {
            nameList.add(name);
            return PROCESS_CONTINUE;
        }

        public IASTName getName(int idx) {
            if (idx < 0 || idx >= nameList.size())
                return null;
            return nameList.get(idx);
        }

        public int size() {
        	return nameList.size();
        }
    }

	protected interface ITestStrategy {
		IIndex getIndex();
		void setUp() throws Exception;
		void tearDown() throws Exception;
		public int getAstCount();
		public IASTTranslationUnit getAst(int index);
		public StringBuilder getAstSource(int index);
		public StringBuilder[] getTestData();
		public ICProject getCProject();
		public boolean isCompositeIndex();
	}

	class SinglePDOMTestFirstASTStrategy implements ITestStrategy {
		private IIndex index;
		private ICProject cproject;
		private StringBuilder[] testData;
		private IASTTranslationUnit ast;
		private final boolean cpp;

		public SinglePDOMTestFirstASTStrategy(boolean cpp) {
			this.cpp = cpp;
		}

		@Override
		public ICProject getCProject() {
			return cproject;
		}

		@Override
		public StringBuilder[] getTestData() {
			return testData;
		}

		@Override
		public int getAstCount() {
			return 1;
		}

		@Override
		public IASTTranslationUnit getAst(int index) {
			if (index != 0)
				throw new IllegalArgumentException();
			return ast;
		}

		@Override
		public StringBuilder getAstSource(int index) {
			if (index != 0)
				throw new IllegalArgumentException();
			return testData[1];
		}

		@Override
		public void setUp() throws Exception {
			cproject = cpp ?
					CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) :
					CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			Bundle b = CTestPlugin.getDefault().getBundle();
			testData = TestSourceReader.getContentsForTest(b, "parser", IndexBindingResolutionTestBase.this.getClass(), getName(), 2);

			if (testData.length < 2)
				fail("Insufficient test data");
			testData[1].insert(0, "#include \"header.h\" " + END_OF_ADDED_CODE_MARKER + "\n");

			IFile file = TestSourceReader.createFile(cproject.getProject(), new Path("header.h"), testData[0].toString());
			CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
	        waitForIndexer(cproject);

			if (DEBUG) {
				System.out.println("Project PDOM: " + getName());
				((PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
			}

			index= CCorePlugin.getIndexManager().getIndex(cproject);

			index.acquireReadLock();
			IFile cppfile= TestSourceReader.createFile(cproject.getProject(), new Path("references.c" + (cpp ? "pp" : "")), testData[1].toString());
			ast = TestSourceReader.createIndexBasedAST(index, cproject, cppfile);
		}

		@Override
		public void tearDown() throws Exception {
			if (index != null) {
				index.releaseReadLock();
			}
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}

		@Override
		public IIndex getIndex() {
			return index;
		}

		@Override
		public boolean isCompositeIndex() {
			return false;
		}
	}

	class SinglePDOMTestStrategy implements ITestStrategy {
		private IIndex index;
		private ICProject cproject;
		private StringBuilder[] testData;
		private IASTTranslationUnit ast;
		private final boolean cpp;

		public SinglePDOMTestStrategy(boolean cpp) {
			this.cpp = cpp;
		}

		@Override
		public ICProject getCProject() {
			return cproject;
		}

		@Override
		public StringBuilder[] getTestData() {
			return testData;
		}

		@Override
		public int getAstCount() {
			return 1;
		}

		@Override
		public IASTTranslationUnit getAst(int index) {
			if (index != 0)
				throw new IllegalArgumentException();
			return ast;
		}

		@Override
		public StringBuilder getAstSource(int index) {
			if (index != 0)
				throw new IllegalArgumentException();
			return testData[1];
		}

		@Override
		public void setUp() throws Exception {
			cproject = cpp ?
					CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) :
					CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			Bundle b = CTestPlugin.getDefault().getBundle();
			testData = TestSourceReader.getContentsForTest(b, "parser", IndexBindingResolutionTestBase.this.getClass(), getName(), 2);

			if (testData.length < 2)
				fail("Insufficient test data");
			testData[1].insert(0, "#include \"header.h\" " + END_OF_ADDED_CODE_MARKER + "\n");

			IFile file = TestSourceReader.createFile(cproject.getProject(), new Path("header.h"), testData[0].toString());
			CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
	        waitForIndexer(cproject);

			IFile cppfile= TestSourceReader.createFile(cproject.getProject(), new Path("references.c" + (cpp ? "pp" : "")), testData[1].toString());
	        waitForIndexer(cproject);

			if (DEBUG) {
				System.out.println("Project PDOM: " + getName());
				((PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
			}

			index= CCorePlugin.getIndexManager().getIndex(cproject);

			index.acquireReadLock();
			ast = TestSourceReader.createIndexBasedAST(index, cproject, cppfile);
		}

		@Override
		public void tearDown() throws Exception {
			if (index != null) {
				index.releaseReadLock();
			}
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}

		@Override
		public IIndex getIndex() {
			return index;
		}

		@Override
		public boolean isCompositeIndex() {
			return false;
		}
	}

	/**
	 * This strategy allows tests to create an arbitrary number of header and source files
	 * and to obtain ASTs of any subset of the created files.
	 *
	 * The first line of each comment section preceding the test contains the name of the file
	 * to put the contents of the section to. To request the AST of a file, put an asterisk after
	 * the file name.
	 *
	 * If the same file name is repeated more than once, the file will be created and then updated
	 * with the new contents. It is guaranteed that the indexer will run for the original and then
	 * for the updated file contents.
	 */
	protected class SinglePDOMTestNamedFilesStrategy implements ITestStrategy {
		private IIndex index;
		private ICProject cproject;
		private StringBuilder[] testData;
		private final List<StringBuilder> astSources;
		private final List<IASTTranslationUnit> asts;
		private final boolean cpp;

		public SinglePDOMTestNamedFilesStrategy(boolean cpp) {
			this.cpp = cpp;
			astSources = new ArrayList<>();
			asts = new ArrayList<>();
		}

		@Override
		public ICProject getCProject() {
			return cproject;
		}

		@Override
		public StringBuilder[] getTestData() {
			return testData;
		}

		@Override
		public int getAstCount() {
			return asts.size();
		}

		@Override
		public IASTTranslationUnit getAst(int index) {
			return asts.get(index);
		}

		@Override
		public StringBuilder getAstSource(int index) {
			return astSources.get(index);
		}

		@Override
		public void setUp() throws Exception {
			cproject = cpp ?
					CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) :
					CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			Bundle b = CTestPlugin.getDefault().getBundle();
			testData = TestSourceReader.getContentsForTest(b, "parser", IndexBindingResolutionTestBase.this.getClass(), getName(), 0);

			List<IFile> astFiles = new ArrayList<>();
			for (int i = 0; i < testData.length;) {
				Set<String> createdFiles = new HashSet<>();
				for (int j = i; j < testData.length; j++, i++) {
					StringBuilder contents = testData[j];
					int endOfLine = contents.indexOf("\n");
					if (endOfLine >= 0) {
						endOfLine++;
					} else {
						endOfLine = contents.length();
					}
					String filename = contents.substring(0, endOfLine).trim();
					boolean astRequested = filename.endsWith("*");
					if (astRequested) {
						filename = filename.substring(0, filename.length() - 1).trim();
					}
					if (!createdFiles.add(filename)) {
						// The file has already been encountered since the project was indexed.
						// Wait for the indexer before updating the file.
						break;
					}
					contents.delete(0, endOfLine);  // Remove first line from the file contents.
					IFile file = TestSourceReader.createFile(cproject.getProject(), new Path(filename), contents.toString());
					if (astRequested || (j == testData.length - 1 && astFiles.isEmpty())) {
						int pos = astFiles.indexOf(file);
						if (pos < 0) {
							astFiles.add(file);
							astSources.add(contents);
						} else {
							astSources.set(pos, contents);
						}
					}
				}
				CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		        waitForIndexer(cproject);
			}

			if (DEBUG) {
				System.out.println("Project PDOM: " + getName());
				((PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
			}

			index= CCorePlugin.getIndexManager().getIndex(cproject);

			index.acquireReadLock();
			for (IFile file : astFiles) {
				asts.add(TestSourceReader.createIndexBasedAST(index, cproject, file));
			}
		}

		@Override
		public void tearDown() throws Exception {
			if (index != null) {
				index.releaseReadLock();
			}
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}

		@Override
		public IIndex getIndex() {
			return index;
		}

		@Override
		public boolean isCompositeIndex() {
			return false;
		}
	}

	class ReferencedProject implements ITestStrategy {
		private IIndex index;
		private ICProject cproject, referenced;
		private StringBuilder[] testData;
		private IASTTranslationUnit ast;
		private final boolean cpp;

		public ReferencedProject(boolean cpp) {
			this.cpp = cpp;
		}

		@Override
		public ICProject getCProject() {
			return cproject;
		}

		@Override
		public void tearDown() throws Exception {
			if (index != null) {
				index.releaseReadLock();
			}
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
			if (referenced != null) {
				referenced.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}

		@Override
		public void setUp() throws Exception {
			cproject= cpp ?
					CProjectHelper.createCCProject("OnlineContent"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) :
					CProjectHelper.createCProject("OnlineContent"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			Bundle b= CTestPlugin.getDefault().getBundle();
			testData= TestSourceReader.getContentsForTest(b, "parser", IndexBindingResolutionTestBase.this.getClass(), getName(), 2);
			if (testData.length < 2)
				fail("Insufficient test data");
			testData[1].insert(0, "#include \"header.h\" " + END_OF_ADDED_CODE_MARKER + "\n");

			referenced = createReferencedContent();

			TestScannerProvider.sIncludes= new String[] {referenced.getProject().getLocation().toOSString()};
			IFile references= TestSourceReader.createFile(cproject.getProject(), new Path("refs.c" + (cpp ? "pp" : "")), testData[1].toString());

			IProject[] refs = new IProject[] {referenced.getProject()};
			IProjectDescription pd = cproject.getProject().getDescription();
			pd.setReferencedProjects(refs);
			cproject.getProject().setDescription(pd, new NullProgressMonitor());

			IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
			CCorePlugin.getIndexManager().reindex(cproject);
			waitForIndexer(cproject);

			if (DEBUG) {
				System.out.println("Online: " + getName());
			 	((PDOM) CCoreInternals.getPDOMManager().getPDOM(cproject)).accept(new PDOMPrettyPrinter());
			}

			index= CCorePlugin.getIndexManager().getIndex(cproject, IIndexManager.ADD_DEPENDENCIES);
			index.acquireReadLock();
			ast= TestSourceReader.createIndexBasedAST(index, cproject, references);
		}

		private ICProject createReferencedContent() throws Exception {
			ICProject referenced = cpp ?
					CProjectHelper.createCCProject("ReferencedContent" + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) :
					CProjectHelper.createCProject("ReferencedContent" + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
			String content = testData[0].toString();
			IFile file = TestSourceReader.createFile(referenced.getProject(), new Path("header.h"), content);

			IndexerPreferences.set(referenced.getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
			CCorePlugin.getIndexManager().reindex(referenced);

			waitForIndexer(referenced);

			if (DEBUG) {
				System.out.println("Referenced: " + getName());
				((PDOM) CCoreInternals.getPDOMManager().getPDOM(referenced)).accept(new PDOMPrettyPrinter());
			}

			return referenced;
		}

		@Override
		public int getAstCount() {
			return 1;
		}

		@Override
		public IASTTranslationUnit getAst(int index) {
			if (index != 0)
				throw new IllegalArgumentException();
			return ast;
		}

		@Override
		public StringBuilder getAstSource(int index) {
			if (index != 0)
				throw new IllegalArgumentException();
			return testData[1];
		}

		@Override
		public IIndex getIndex() {
			return index;
		}

		@Override
		public StringBuilder[] getTestData() {
			return testData;
		}

		@Override
		public boolean isCompositeIndex() {
			return true;
		}
	}

	/**
	 * When a test is failing only for the strategy where the test data is split over
	 * multiple index fragments, we artificially fail the single fragment strategy also.
	 * This is not ideal, but as both strategies behavior are typically the same, is
	 * quite rare.
	 */
	protected void fakeFailForSingle() {
		if (getName().startsWith("_") && strategy instanceof SinglePDOMTestStrategy) {
			fail("Artificially failing - see IndexBindingResolutionTestBase.fakeFailForSingle()");
		}
	}

	/**
	 * When a test is failing only for the strategy where the test data is not split over
	 * multiple index fragments, we artificially fail the single fragment strategy also.
	 * This is not ideal, but as both strategies behavior are typically the same, is
	 * quite rare.
	 */
	protected void fakeFailForMultiProject() {
		if (getName().startsWith("_") && strategy instanceof ReferencedProject) {
			fail("Artificially failing - see IndexBindingResolutionTestBase.fakeFailForReferenced()");
		}
	}
	
	protected static void assertSameType(IType first, IType second){
		assertNotNull(first);
		assertNotNull(second);
		assertTrue("Expected types to be the same, but first was: '" + first.toString() + "' and second was: '" + second + "'", first.isSameType(second));
	}
}
