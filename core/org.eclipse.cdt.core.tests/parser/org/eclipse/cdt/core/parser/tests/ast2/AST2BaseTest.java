/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ASTComparer;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

/**
 * @author aniefer
 */
public class AST2BaseTest extends BaseTestCase {
	public final static String TEST_CODE = "<testcode>";
    protected static final IParserLogService NULL_LOG = new NullLogService();
	protected static boolean sValidateCopy;

    public AST2BaseTest() {
    	super();
    }
    
    public AST2BaseTest(String name) {
    	super(name);
    }
    
    @Override
	protected void setUp() throws Exception {
    	sValidateCopy= true;
		super.setUp();
	}

	protected IASTTranslationUnit parse(String code, ParserLanguage lang) throws ParserException {
    	return parse(code, lang, false, true);
    }
    
    protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions) throws ParserException {
    	return parse(code, lang, useGNUExtensions, true);
    }

    /**
     * @param string
     * @param c
     * @return
     * @throws ParserException
     */
    protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
    		boolean expectNoProblems) throws ParserException {
    	return parse(code, lang, useGNUExtensions, expectNoProblems, false);
    }
    
    protected IASTTranslationUnit parse(String code, ParserLanguage lang, boolean useGNUExtensions,
    		boolean expectNoProblems, boolean skipTrivialInitializers) throws ParserException {
		IScanner scanner = createScanner(FileContent.create(TEST_CODE, code.toCharArray()), lang, ParserMode.COMPLETE_PARSE, 
        		new ScannerInfo());
        configureScanner(scanner);
        AbstractGNUSourceCodeParser parser = null;
        if (lang == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration config = null;
            if (useGNUExtensions)
            	config = new GPPParserExtensionConfiguration();
            else
            	config = new ANSICPPParserExtensionConfiguration();
            parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG,config, null);
        } else {
            ICParserExtensionConfiguration config = null;

            if (useGNUExtensions)
            	config = new GCCParserExtensionConfiguration();
            else
            	config = new ANSICParserExtensionConfiguration();
            
            parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config, null);
        }
        if (skipTrivialInitializers)
        	parser.setSkipTrivialExpressionsInAggregateInitializers(true);
        
        IASTTranslationUnit tu = parser.parse();
        assertTrue(tu.isFrozen());
        if (sValidateCopy)
        	validateCopy(tu);

        if (parser.encounteredError() && expectNoProblems)
            throw new ParserException("FAILURE"); //$NON-NLS-1$
         
        if (lang == ParserLanguage.C && expectNoProblems) {
        	assertEquals(CVisitor.getProblems(tu).length, 0);
        	assertEquals(tu.getPreprocessorProblems().length, 0);
        } else if (lang == ParserLanguage.CPP && expectNoProblems) {
        	assertEquals(CPPVisitor.getProblems(tu).length, 0);
        	assertEquals(0, tu.getPreprocessorProblems().length);
        }
        if (expectNoProblems)
            assertEquals(0, tu.getPreprocessorProblems().length);
        
        return tu;
    }

	protected void configureScanner(IScanner scanner) {
	}

	public static IScanner createScanner(FileContent codeReader, ParserLanguage lang, ParserMode mode,
			IScannerInfo scannerInfo) {
		IScannerExtensionConfiguration configuration = null;
        if (lang == ParserLanguage.C)
            configuration= GCCScannerExtensionConfiguration.getInstance();
        else
            configuration= GPPScannerExtensionConfiguration.getInstance();
        IScanner scanner;
        scanner= new CPreprocessor(codeReader, scannerInfo, lang, NULL_LOG, configuration, 
        		IncludeFileContentProvider.getSavedFilesProvider());
		return scanner;
	}

    /**
     * @param string
     */
    protected void validateSimplePostfixInitializerExpressionC(String code) throws ParserException {
        ICASTTypeIdInitializerExpression e = (ICASTTypeIdInitializerExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
        assertNotNull(e);
        assertNotNull(e.getTypeId());
        assertNotNull(e.getInitializer());
    }

    /**
     * @param string
     * @throws ParserException
     */
    protected void validateSimpleUnaryTypeIdExpression(String code, int op) throws ParserException {
        IASTCastExpression e = (IASTCastExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
        assertNotNull(e);
        assertEquals(e.getOperator(), op);
        assertNotNull(e.getTypeId());
        IASTIdExpression x = (IASTIdExpression) e.getOperand();
        assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
    }

    /**
     * @param code
     * @param op
     * @throws ParserException
     */
    protected void validateSimpleTypeIdExpressionC(String code, int op) throws ParserException {
        IASTTypeIdExpression e = (IASTTypeIdExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
        assertNotNull(e);
        assertEquals(e.getOperator(), op);
        assertNotNull(e.getTypeId());
    }

    /**
     * @param string
     * @param op_prefixIncr
     * @throws ParserException
     */
    protected void validateSimpleUnaryExpressionC(String code, int operator) throws ParserException {
        IASTUnaryExpression e = (IASTUnaryExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
        assertNotNull(e);
        assertEquals(e.getOperator(), operator);
        IASTIdExpression x = (IASTIdExpression) e.getOperand();
        assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
    }

    /**
     * @param code 
     * @throws ParserException
     */
    protected void validateConditionalExpressionC(String code) throws ParserException {
        IASTConditionalExpression e = (IASTConditionalExpression) getExpressionFromStatementInCode(code, ParserLanguage.C);
        assertNotNull(e);
        IASTIdExpression x = (IASTIdExpression) e.getLogicalConditionExpression();
        assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
        IASTIdExpression y = (IASTIdExpression) e.getPositiveResultExpression();
        assertEquals(y.getName().toString(), "y"); //$NON-NLS-1$
        IASTIdExpression x2 = (IASTIdExpression) e.getNegativeResultExpression();
        assertEquals(x.getName().toString(), x2.getName().toString());
    }

    /**
     * @param operand
     * @throws ParserException
     */
    protected void validateSimpleBinaryExpressionC(String code, int operand) throws ParserException {
        IASTBinaryExpression e = (IASTBinaryExpression) getExpressionFromStatementInCode(code, ParserLanguage.C); 
        assertNotNull(e);
        assertEquals(e.getOperator(), operand);
        IASTIdExpression x = (IASTIdExpression) e.getOperand1();
        assertEquals(x.getName().toString(), "x"); //$NON-NLS-1$
        IASTIdExpression y = (IASTIdExpression) e.getOperand2();
        assertEquals(y.getName().toString(), "y"); //$NON-NLS-1$
    }

    protected IASTExpression getExpressionFromStatementInCode(String code, ParserLanguage language) throws ParserException {
        StringBuffer buffer = new StringBuffer("void f() { "); //$NON-NLS-1$
        buffer.append("int x, y;\n"); //$NON-NLS-1$
        buffer.append(code);
        buffer.append(";\n}"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), language);
        IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
        IASTCompoundStatement cs = (IASTCompoundStatement) f.getBody();
        IASTExpressionStatement s = (IASTExpressionStatement) cs.getStatements()[1];
        return s.getExpression();
    }

    protected <T extends IASTNode> T validateCopy(T tu) {
		IASTNode copy = tu.copy();
		assertFalse(copy.isFrozen());
		ASTComparer.assertCopy(tu, copy);
		return (T) copy;
	}
	
    
    static protected class CNameCollector extends CASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        @Override
		public int visit(IASTName name) {
            nameList.add(name);
            return PROCESS_CONTINUE;
        }
        public IASTName getName(int idx) {
            if (idx < 0 || idx >= nameList.size())
                return null;
            return (IASTName) nameList.get(idx);
        }
        public int size() { return nameList.size(); } 
    }

    protected void assertInstances(CNameCollector collector, IBinding binding, int num) throws Exception {
        int count = 0;
        
        if (binding == null) assertTrue(false);
        
        for (int i = 0; i < collector.size(); i++) {
            if (collector.getName(i).resolveBinding() == binding)
                count++;
        }
        
        assertEquals(count, num);
    }

    static protected class CPPNameCollector extends CPPASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List<IASTName> nameList = new ArrayList<IASTName>();
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
        public int size() { return nameList.size(); }
        
        public void dump() {
        	for (int i=0; i<size(); i++) {
        		IASTName name= getName(i);
        		String parent= name.getParent() != null ? name.getParent().getRawSignature() : "";
        		System.out.println(i+": #"+name.getRawSignature()+"# "+parent);
        	}
        }
    }

    protected void assertInstances(CPPNameCollector collector, IBinding binding, int num) throws Exception {
        int count = 0;
        for (int i = 0; i < collector.size(); i++)
            if (collector.getName(i).resolveBinding() == binding)
                count++;
        
        assertEquals(num, count);
    }

	protected void isExpressionStringEqual(IASTInitializerClause exp, String str) {
		String expressionString = ASTSignatureUtil.getExpressionString((IASTExpression) exp);
		assertEquals(str, expressionString);
	}

	protected void isExpressionStringEqual(IASTExpression exp, String str) {
		String expressionString = ASTSignatureUtil.getExpressionString(exp);
		assertEquals(str, expressionString);
	}
	
	protected void isParameterSignatureEqual(IASTDeclarator decltor, String str) {
		assertEquals(str, ASTSignatureUtil.getParameterSignature(decltor));
	}
	
	protected void isSignatureEqual(IASTDeclarator decltor, String str) {
		assertEquals(str, ASTSignatureUtil.getSignature(decltor));
	}
	
	protected void isSignatureEqual(IASTDeclSpecifier declSpec, String str) {
		assertEquals(str, ASTSignatureUtil.getSignature(declSpec));
	}
	
	protected void isSignatureEqual(IASTTypeId typeId, String str) {
		assertEquals(str, ASTSignatureUtil.getSignature(typeId));
	}
	
	protected void isTypeEqual(IASTDeclarator decltor, String str) {
		assertEquals(str, ASTTypeUtil.getType(decltor));
	}
	
	protected void isTypeEqual(IASTTypeId typeId, String str) {
		assertEquals(str, ASTTypeUtil.getType(typeId));
	}
	
	protected void isTypeEqual(IType type, String str) {
		assertEquals(str, ASTTypeUtil.getType(type));
	}
	
	protected void isParameterTypeEqual(IFunctionType fType, String str) {
		assertEquals(str, ASTTypeUtil.getParameterTypeString(fType));
	}
	
	static protected class CNameResolver extends CASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		public List nameList = new ArrayList();
		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return (IASTName) nameList.get(idx);
		}
		public int size() { return nameList.size(); } 
	}
	
	static protected class CPPNameResolver extends CPPASTVisitor {
		{
			shouldVisitNames = true;
		}
		public int numProblemBindings=0;
		public int numNullBindings=0;
		public List nameList = new ArrayList();
		@Override
		public int visit(IASTName name) {
			nameList.add(name);
			IBinding binding = name.resolveBinding();
			if (binding instanceof IProblemBinding)
				numProblemBindings++;
			if (binding == null)
				numNullBindings++;
			return PROCESS_CONTINUE;
		}
		public IASTName getName(int idx) {
			if (idx < 0 || idx >= nameList.size())
				return null;
			return (IASTName) nameList.get(idx);
		}
		public int size() { return nameList.size(); } 
	}
	
	protected String getAboveComment() throws IOException {
		return getContents(1)[0].toString();
	}
	
	protected StringBuffer[] getContents(int sections) throws IOException {
		CTestPlugin plugin = CTestPlugin.getDefault();
		if (plugin == null)
			throw new AssertionFailedError("This test must be run as a JUnit plugin test");
		return TestSourceReader.getContentsForTest(plugin.getBundle(), "parser", getClass(), getName(), sections);
	}

	protected static <T> T assertInstance(Object o, Class<T> clazz, Class... cs) {
		assertNotNull("Expected object of "+clazz.getName()+" but got a null value", o);
		assertTrue("Expected "+clazz.getName()+" but got "+o.getClass().getName(), clazz.isInstance(o));
		for (Class c : cs) {
			assertNotNull("Expected object of "+c.getName()+" but got a null value", o);
			assertTrue("Expected "+c.getName()+" but got "+o.getClass().getName(), c.isInstance(o));
		}
		return clazz.cast(o);
	}
	
	protected static void assertField(IBinding binding, String fieldName, String ownerName) {
    	assertInstance(binding, IField.class);
    	assertEquals(fieldName, binding.getName());
    	ICompositeType struct = ((IField) binding).getCompositeTypeOwner();
    	assertEquals(ownerName, struct.getName());
    }
	
	protected class BindingAssertionHelper {
		protected IASTTranslationUnit tu;
		protected String contents;
		protected boolean isCPP;
    	
    	public BindingAssertionHelper(String contents, boolean isCPP) throws ParserException {
    		this.contents= contents;
    		this.isCPP= isCPP;
    		this.tu= parse(contents, isCPP ? ParserLanguage.CPP : ParserLanguage.C, true, false);
		}
    	
    	public IASTTranslationUnit getTranslationUnit() {
    		return tu;
    	}
    	
    	public IProblemBinding assertProblem(String section, int len) {
    		if (len <= 0)
    			len= section.length()+len;
    		IBinding binding= binding(section, len);
    		assertTrue("Non-ProblemBinding for name: " + section.substring(0, len),
    				binding instanceof IProblemBinding);
    		return (IProblemBinding) binding;
    	}
    	
    	public <T extends IBinding> T assertNonProblem(String section, int len) {
    		if (len <= 0)
    			len= section.length()+len;
    		IBinding binding= binding(section, len);
    		if (binding instanceof IProblemBinding) {
    			IProblemBinding problem= (IProblemBinding) binding;
    			fail("ProblemBinding for name: " + section.substring(0, len) + " (" + renderProblemID(problem.getID())+")"); 
    		}
    		if (binding == null) {
    			fail("Null binding resolved for name: " + section.substring(0, len));
    		}
    		return (T) binding;
    	}

    	public void assertNoName(String section, int len) {
			IASTName name= findName(section, len);
			if (name != null) {
				String selection = section.substring(0, len);
				fail("Found unexpected \""+selection+"\": " + name.resolveBinding());
			}
    	}

    	/**
    	 * Asserts that there is exactly one name at the given location and that
    	 * it resolves to the given type of binding.
    	 */
    	public IASTImplicitName assertImplicitName(String section, int len, Class<?> bindingClass) {
    		IASTName name = findImplicitName(section, len);
    		final String selection = section.substring(0, len);
			assertNotNull("did not find \""+selection+"\"", name);
			
			assertInstance(name, IASTImplicitName.class);
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) name.getParent();
			IASTImplicitName[] implicits = owner.getImplicitNames();
			assertNotNull(implicits);
			
			if (implicits.length > 1) {
				boolean found = false;
				for (IASTImplicitName n : implicits) {
					if (((ASTNode) n).getOffset() == ((ASTNode)name).getOffset()) {
						assertFalse(found);
						found = true;
					}
				}
				assertTrue(found);
			}
			
    		assertEquals(selection, name.getRawSignature());
    		IBinding binding = name.resolveBinding();
    		assertNotNull(binding);
    		assertInstance(binding, bindingClass);
    		return (IASTImplicitName) name;
    	}
    	
    	public void assertNoImplicitName(String section, int len) {
    		IASTName name = findImplicitName(section, len);
    		final String selection = section.substring(0, len);
    		assertNull("found name \""+selection+"\"", name);
    	}
    	
    	public IASTImplicitName[] getImplicitNames(String section, int len) {
    		IASTName name = findImplicitName(section, len);
    		IASTImplicitNameOwner owner = (IASTImplicitNameOwner) name.getParent();
			IASTImplicitName[] implicits = owner.getImplicitNames();
			return implicits;
    	}
    	
    	public IASTName findName(String section, int len) {
    		final int offset = contents.indexOf(section);
    		assertTrue(offset >= 0);
    		IASTNodeSelector selector = tu.getNodeSelector(null);
    		return selector.findName(offset, len);
    	}
 
    	public IASTName findImplicitName(String section, int len) {
    		final int offset = contents.indexOf(section);
    		assertTrue(offset >= 0);
    		IASTNodeSelector selector = tu.getNodeSelector(null);
    		return selector.findImplicitName(offset, len);
    	}

    	private String renderProblemID(int i) {
    		try {
    			for (Field field : IProblemBinding.class.getDeclaredFields()) {
    				if (field.getName().startsWith("SEMANTIC_")) {
    					if (field.getType() == int.class) {
    						Integer ci= (Integer) field.get(null);
    						if (ci.intValue() == i) {
    							return field.getName();
    						}
    					}
    				}
    			}
    		} catch(IllegalAccessException iae) {
    			throw new RuntimeException(iae);
    		}
    		return "Unknown problem ID";
    	}
    	
    	public <T extends IBinding> T assertNonProblem(String section, int len, Class<T> type, Class... cs) {
    		IBinding binding= binding(section, len);
    		assertTrue("ProblemBinding for name: " + section.substring(0, len),
    				!(binding instanceof IProblemBinding));
    		assertInstance(binding, type);
    		for (Class c : cs) {
    			assertInstance(binding, c);
    		}
    		return type.cast(binding);
    	}
    	
    	private IBinding binding(String section, int len) {
    		IASTName name = findName(section, len);
    		final String selection = section.substring(0, len);
			assertNotNull("did not find \""+selection+"\"", name);
    		assertEquals(selection, name.getRawSignature());
    			
    		IBinding binding = name.resolveBinding();
    		assertNotNull("No binding for "+name.getRawSignature(), binding);
    		
    		return name.resolveBinding();
    	}
    }

	final protected IASTTranslationUnit parseAndCheckBindings(String code, ParserLanguage lang) throws Exception {
		return parseAndCheckBindings(code, lang, false);
	}

	final protected IASTTranslationUnit parseAndCheckBindings(String code, ParserLanguage lang, boolean useGnuExtensions) throws Exception {
		return parseAndCheckBindings(code, lang, useGnuExtensions, false);
	}
	
	final protected IASTTranslationUnit parseAndCheckBindings(String code, ParserLanguage lang, boolean useGnuExtensions,
			boolean skipTrivialInitializers) throws Exception {
		IASTTranslationUnit tu = parse(code, lang, useGnuExtensions, true, skipTrivialInitializers); 
		CNameCollector col = new CNameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
		return tu;
	}

	final protected void assertNoProblemBindings(CNameCollector col) {
		Iterator i = col.nameList.iterator();
		while (i.hasNext()) {
			IASTName n = (IASTName) i.next();
			assertFalse(n.resolveBinding() instanceof IProblemBinding);
		}
	}

	final protected void assertProblemBindings(CNameCollector col, int count) {
		Iterator i = col.nameList.iterator();
		int sum = 0;
		while (i.hasNext()) {
			IASTName n = (IASTName) i.next();
			if (n.getBinding() instanceof IProblemBinding)
				++sum;
		}
		assertEquals(count, sum);
	}

	final protected <T extends IASTDeclaration> T getDeclaration(IASTTranslationUnit tu, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls= tu.getDeclarations();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTDeclaration> T getDeclaration(ICPPASTNamespaceDefinition ns, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls= ns.getDeclarations();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTDeclaration> T getDeclaration(ICPPASTLinkageSpecification ls, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls= ls.getDeclarations();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTDeclaration> T getDeclaration(IASTCompositeTypeSpecifier ct, int i_decl) {
		Class<T> tclass;
		IASTDeclaration[] decls= ct.getMembers();
		assertTrue(decls.length > i_decl);
		return (T) decls[i_decl];
	}

	final protected <T extends IASTCompositeTypeSpecifier> T getCompositeType(IASTTranslationUnit tu, int i_decl) {
		IASTSimpleDeclaration sdecl= getDeclaration(tu, i_decl);
		return (T) sdecl.getDeclSpecifier();
	}

	final protected <T extends IASTStatement> T getStatement(IASTFunctionDefinition fdef, int i_stmt) {
		return getStatement((IASTCompoundStatement) fdef.getBody(), i_stmt);
	}

	final protected <T extends IASTStatement> T getStatement(IASTCompoundStatement compound, int i_stmt) {
		IASTStatement[] stmts= compound.getStatements();
		assertTrue(stmts.length > i_stmt);
		return (T) stmts[i_stmt];
	}

	final protected <T extends IASTExpression> T getExpressionOfStatement(IASTFunctionDefinition fdef, int i) {
		IASTStatement stmt= getStatement(fdef, i);
		assertInstance(stmt, IASTExpressionStatement.class);
		return (T) ((IASTExpressionStatement) stmt).getExpression();
	}
}
