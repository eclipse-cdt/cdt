/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.lang.reflect.Field;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Common base class for AST2 and index tests.
 */
public class SemanticTestBase extends BaseTestCase {
	public SemanticTestBase() {
		super();
	}

	public SemanticTestBase(String name) {
		super(name);
	}

	protected static class CommonCTypes {
		public static IType pointerToVoid = pointerTo(CBasicType.VOID);
		public static IType pointerToConstVoid = pointerTo(constOf(CBasicType.VOID));
		public static IType pointerToInt = pointerTo(CBasicType.INT);
		public static IType pointerToConstInt = pointerTo(constOf(CBasicType.INT));
		public static IType pointerToVolatileInt = pointerTo(volatileOf(CBasicType.INT));
		public static IType pointerToConstVolatileInt = pointerTo(constVolatileOf(CBasicType.INT));

		private static IType pointerTo(IType type) {
			return new CPointerType(type, 0);
		}

		private static IType constOf(IType type) {
			return new CQualifierType(type, true, false, false);
		}

		private static IType volatileOf(IType type) {
			return new CQualifierType(type, false, true, false);
		}

		private static IType constVolatileOf(IType type) {
			return new CQualifierType(type, true, true, false);
		}
	}

	protected static void assertSameType(IType expected, IType actual) {
		assertNotNull(expected);
		assertNotNull(actual);
		assertTrue("Expected same types, but the types were: '" + ASTTypeUtil.getType(expected, false) + "' and '"
				+ ASTTypeUtil.getType(actual, false) + "'", expected.isSameType(actual));
	}

	protected static void assertType(IVariable variable, IType expectedType) {
		assertSameType(expectedType, variable.getType());
	}

	protected static SizeAndAlignment getSizeAndAlignment(IType type, IASTNode lookupPoint) {
		try {
			CPPSemantics.pushLookupPoint(lookupPoint);
			return SizeofCalculator.getSizeAndAlignment(type);
		} finally {
			CPPSemantics.popLookupPoint();
		}
	}

	protected class BindingAssertionHelper {
		protected String contents;
		protected IASTTranslationUnit tu;

		public BindingAssertionHelper(String contents, IASTTranslationUnit tu) {
			this.contents = contents;
			this.tu = tu;
		}

		public IASTTranslationUnit getTranslationUnit() {
			return tu;
		}

		public IProblemBinding assertProblem(String section, int len) {
			if (len <= 0)
				len = section.length() + len;
			IBinding binding = binding(section, len);
			assertTrue("Non-ProblemBinding for name: " + section.substring(0, len), binding instanceof IProblemBinding);
			return (IProblemBinding) binding;
		}

		public IProblemBinding assertProblem(String context, int len, int problemId) {
			IProblemBinding problemBinding = assertProblem(context, len);
			assertEquals(problemId, problemBinding.getID());
			return problemBinding;
		}

		public IProblemBinding assertProblem(String context, String name) {
			IBinding binding = binding(context, name);
			assertTrue("Non-ProblemBinding for name: " + name, binding instanceof IProblemBinding);
			return (IProblemBinding) binding;
		}

		public IProblemBinding assertProblem(String context, String name, int problemId) {
			IProblemBinding problemBinding = assertProblem(context, name);
			assertEquals(problemId, problemBinding.getID());
			return problemBinding;
		}

		public <T extends IBinding> T assertNonProblem(String section, int len) {
			if (len <= 0)
				len = section.length() + len;
			IBinding binding = binding(section, len);
			if (binding instanceof IProblemBinding) {
				IProblemBinding problem = (IProblemBinding) binding;
				fail("ProblemBinding for name: " + section.substring(0, len) + " (" + renderProblemID(problem.getID())
						+ ")");
			}
			if (binding == null) {
				fail("Null binding resolved for name: " + section.substring(0, len));
			}
			return (T) binding;
		}

		private int getIdentifierOffset(String str) {
			for (int i = 0; i < str.length(); ++i) {
				if (Character.isJavaIdentifierPart(str.charAt(i)))
					return i;
			}
			fail("Didn't find identifier in \"" + str + "\"");
			return -1;
		}

		private int getIdentifierLength(String str, int offset) {
			int i;
			for (i = offset; i < str.length() && Character.isJavaIdentifierPart(str.charAt(i)); ++i) {
			}
			return i;
		}

		public IProblemBinding assertProblemOnFirstIdentifier(String section) {
			int offset = getIdentifierOffset(section);
			String identifier = section.substring(offset, getIdentifierLength(section, offset));
			return assertProblem(section, identifier);
		}

		public IProblemBinding assertProblemOnFirstIdentifier(String section, int problemId) {
			IProblemBinding problemBinding = assertProblemOnFirstIdentifier(section);
			assertEquals(problemId, problemBinding.getID());
			return problemBinding;
		}

		public <T extends IBinding> T assertNonProblemOnFirstIdentifier(String section, Class... cs) {
			int offset = getIdentifierOffset(section);
			String identifier = section.substring(offset, getIdentifierLength(section, offset));
			return assertNonProblem(section, identifier, cs);
		}

		public void assertNoName(String section, int len) {
			IASTName name = findName(section, len);
			if (name != null) {
				String selection = section.substring(0, len);
				fail("Found unexpected \"" + selection + "\": " + name.resolveBinding());
			}
		}

		/**
		 * Asserts that there is exactly one name at the given location and that
		 * it resolves to the given type of binding.
		 */
		public IASTImplicitName assertImplicitName(String section, int len, Class<?> bindingClass) {
			IASTName name = findImplicitName(section, len);
			final String selection = section.substring(0, len);
			assertNotNull("Did not find \"" + selection + "\"", name);

			assertInstance(name, IASTImplicitName.class);
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) name.getParent();
			IASTImplicitName[] implicits = owner.getImplicitNames();
			assertNotNull(implicits);

			if (implicits.length > 1) {
				boolean found = false;
				for (IASTImplicitName n : implicits) {
					if (((ASTNode) n).getOffset() == ((ASTNode) name).getOffset()) {
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
			assertNull("found name \"" + selection + "\"", name);
		}

		public IASTImplicitName[] getImplicitNames(String section) {
			return getImplicitNames(section, section.length());
		}

		public IASTImplicitName[] getImplicitNames(String section, int len) {
			IASTName name = findImplicitName(section, len);
			IASTImplicitNameOwner owner = (IASTImplicitNameOwner) name.getParent();
			IASTImplicitName[] implicits = owner.getImplicitNames();
			return implicits;
		}

		public IASTImplicitDestructorName[] getImplicitDestructorNames(String section) {
			return getImplicitDestructorNames(section, section.length());
		}

		public IASTImplicitDestructorName[] getImplicitDestructorNames(String section, int len) {
			final int offset = contents.indexOf(section);
			assertTrue(offset >= 0);
			IASTNodeSelector selector = tu.getNodeSelector(null);
			IASTNode enclosingNode = selector.findEnclosingNode(offset, len);
			if (!(enclosingNode instanceof IASTImplicitDestructorNameOwner))
				return IASTImplicitDestructorName.EMPTY_NAME_ARRAY;
			return ((IASTImplicitDestructorNameOwner) enclosingNode).getImplicitDestructorNames();
		}

		public IASTName findName(String section, int len) {
			final int offset = contents.indexOf(section);
			assertTrue("Section \"" + section + "\" not found", offset >= 0);
			IASTNodeSelector selector = tu.getNodeSelector(null);
			return selector.findName(offset, len);
		}

		public IASTName findName(String context, String name) {
			if (context == null) {
				context = contents;
			}
			int offset = contents.indexOf(context);
			assertTrue("Context \"" + context + "\" not found", offset >= 0);
			int nameOffset = context.indexOf(name);
			assertTrue("Name \"" + name + "\" not found", nameOffset >= 0);
			IASTNodeSelector selector = tu.getNodeSelector(null);
			return selector.findName(offset + nameOffset, name.length());
		}

		public IASTName findName(String name) {
			return findName(contents, name);
		}

		public IASTImplicitName findImplicitName(String section, int len) {
			final int offset = contents.indexOf(section);
			assertTrue(offset >= 0);
			IASTNodeSelector selector = tu.getNodeSelector(null);
			return selector.findImplicitName(offset, len);
		}

		public <T extends IASTNode> T assertNode(String context, String nodeText, Class... cs) {
			if (context == null) {
				context = contents;
			}
			int offset = contents.indexOf(context);
			assertTrue("Context \"" + context + "\" not found", offset >= 0);
			int nodeOffset = context.indexOf(nodeText);
			assertTrue("Node \"" + nodeText + "\" not found", nodeOffset >= 0);
			IASTNodeSelector selector = tu.getNodeSelector(null);
			IASTNode node = selector.findNode(offset + nodeOffset, nodeText.length());
			return assertType(node, cs);
		}

		public <T extends IASTNode> T assertNode(String nodeText, Class... cs) {
			return assertNode(contents, nodeText, cs);
		}

		private String renderProblemID(int i) {
			try {
				for (Field field : IProblemBinding.class.getDeclaredFields()) {
					if (field.getName().startsWith("SEMANTIC_")) {
						if (field.getType() == int.class) {
							Integer ci = (Integer) field.get(null);
							if (ci.intValue() == i) {
								return field.getName();
							}
						}
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			return "Unknown problem ID";
		}

		public <T extends IBinding> T assertNonProblem(String section, int len, Class... cs) {
			if (len <= 0)
				len += section.length();
			IBinding binding = binding(section, len);
			assertTrue("ProblemBinding for name: " + section.substring(0, len), !(binding instanceof IProblemBinding));
			return assertType(binding, cs);
		}

		public <T extends IBinding> T assertNonProblem(String section, Class... cs) {
			return assertNonProblem(section, section.length(), cs);
		}

		public <T extends IBinding> T assertNonProblem(String context, String name, Class... cs) {
			IBinding binding = binding(context, name);
			assertTrue("ProblemBinding for name: " + name, !(binding instanceof IProblemBinding));
			return assertType(binding, cs);
		}

		public void assertVariableType(String variableName, IType expectedType) {
			IVariable var = assertNonProblem(variableName);
			assertSameType(expectedType, var.getType());
		}

		public void assertVariableTypeProblem(String variableName) {
			IVariable var = assertNonProblem(variableName);
			assertInstance(var.getType(), IProblemType.class);
		}

		public void assertVariableValue(String variableName, long expectedValue) {
			IVariable var = assertNonProblem(variableName);
			BaseTestCase.assertVariableValue(var, expectedValue);
		}

		public <T, U extends T> U assertType(T obj, Class... cs) {
			for (Class c : cs) {
				assertInstance(obj, c);
			}
			return (U) obj;
		}

		private IBinding binding(String section, int len) {
			IASTName astName = findName(section, len);
			final String selection = section.substring(0, len);
			assertNotNull("No AST name for \"" + selection + "\"", astName);
			assertEquals(selection, astName.getRawSignature());

			IBinding binding = astName.resolveBinding();
			assertNotNull("No binding for " + astName.getRawSignature(), binding);

			return astName.resolveBinding();
		}

		private IBinding binding(String context, String name) {
			IASTName astName = findName(context, name);
			assertNotNull("No AST name for \"" + name + "\"", astName);
			assertEquals(name, astName.getRawSignature());

			IBinding binding = astName.resolveBinding();
			assertNotNull("No binding for " + astName.getRawSignature(), binding);

			return astName.resolveBinding();
		}
	}
}
