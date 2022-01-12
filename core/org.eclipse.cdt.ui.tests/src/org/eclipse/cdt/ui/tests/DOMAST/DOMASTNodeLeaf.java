/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * @author dsteffle
 */
public class DOMASTNodeLeaf implements IAdaptable {
	private static final String INTERNAL = "internal"; //$NON-NLS-1$
	private static final String VARIABLE_SIZED_ = "* "; //$NON-NLS-1$
	private static final String VOLATILE_ = "volatile "; //$NON-NLS-1$
	private static final String STATIC_ = "static "; //$NON-NLS-1$
	private static final String RESTRICT_ = "restrict "; //$NON-NLS-1$
	private static final String CONST_ = "const "; //$NON-NLS-1$
	private static final String DASH = "-"; //$NON-NLS-1$
	private static final String FILE_SEPARATOR = "\\"; //$NON-NLS-1$
	public static final String BLANK_STRING = ""; //$NON-NLS-1$
	private static final String IGCCAST_PREFIX = "IGCCAST"; //$NON-NLS-1$
	private static final String IGNUAST_PREFIX = "IGNUAST"; //$NON-NLS-1$
	private static final String IGPPAST_PREFIX = "IGPPAST"; //$NON-NLS-1$
	private static final String ICPPAST_PREFIX = "ICPPAST"; //$NON-NLS-1$
	private static final String ICAST_PREFIX = "ICAST"; //$NON-NLS-1$
	private static final String IAST_PREFIX = "IAST"; //$NON-NLS-1$
	private static final String START_OF_LIST = ": "; //$NON-NLS-1$
	private static final String LIST_SEPARATOR = ", "; //$NON-NLS-1$
	private static final String PERIOD = "."; //$NON-NLS-1$
	private IASTNode node = null;
	private DOMASTNodeParent parent;

	// used for applying filters to the tree, since it is lazily populated
	// all parents of the desired tree object to display need to have a flag as well
	private int filterFlag = 0;
	private static Set<String> ignoreInterfaces = new HashSet<>();
	public static final int FLAG_PROBLEM = 1 << 0;
	public static final int FLAG_PREPROCESSOR = 1 << 1;
	public static final int FLAG_INCLUDE_STATEMENTS = 1 << 2;
	static {
		ignoreInterfaces.addAll(
				Arrays.asList(new String[] { "IASTCompletionContext", "ICPPASTCompletionContext", "IASTNode" }));
	}

	public DOMASTNodeLeaf(IASTNode node) {
		this.node = node;
	}

	public IASTNode getNode() {
		return node;
	}

	public void setParent(DOMASTNodeParent parent) {
		this.parent = parent;
	}

	public DOMASTNodeParent getParent() {
		return parent;
	}

	private boolean hasProperPrefix(String string) {
		if (string.startsWith(IAST_PREFIX) || string.startsWith(ICAST_PREFIX) || string.startsWith(ICPPAST_PREFIX)
				|| string.startsWith(IGPPAST_PREFIX) || string.startsWith(IGNUAST_PREFIX)
				|| string.startsWith(IGCCAST_PREFIX)) {
			if (!ignoreInterfaces.contains(string)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		if (node == null)
			return BLANK_STRING;
		StringBuilder buffer = new StringBuilder();
		List<Class<?>> search = new LinkedList<>();
		boolean done = false;
		boolean needComma = false;

		for (search.add(node.getClass()); !search.isEmpty();) {
			Class<?> clazz = search.remove(0);
			if (clazz.isInterface()) {
				if (clazz.getPackage().toString().indexOf(INTERNAL) < 0) {
					String interfaceName = clazz.getName();
					interfaceName = interfaceName.substring(interfaceName.lastIndexOf(PERIOD) + 1);
					if (hasProperPrefix(interfaceName)) {
						if (needComma)
							buffer.append(LIST_SEPARATOR);
						buffer.append(interfaceName);
						needComma = true;
						done = true;
					}
				}
			}
			if (!done) {
				search.addAll(Arrays.asList(clazz.getInterfaces()));
				final Class<?> superclass = clazz.getSuperclass();
				if (superclass != null)
					search.add(superclass);
			}
		}

		if (node instanceof IASTProblemHolder) {
			buffer.append(START_OF_LIST);
			buffer.append(((IASTProblemHolder) node).getProblem().getMessageWithLocation());
		} else if (node instanceof IASTSimpleDeclaration) {
			String name = null;
			IASTDeclarator[] decltors = ((IASTSimpleDeclaration) node).getDeclarators();

			if (decltors.length > 0) {
				buffer.append(START_OF_LIST);
				for (int i = 0; i < decltors.length; i++) {
					name = getDeclaratorName(decltors[i]);
					buffer.append(name);

					if (i + 1 < decltors.length)
						buffer.append(LIST_SEPARATOR);
				}
			}
			return buffer.toString();
		} else if (node instanceof IASTFunctionDefinition) {
			String name = getDeclaratorName(((IASTFunctionDefinition) node).getDeclarator());
			if (name != null) {
				buffer.append(START_OF_LIST);
				buffer.append(name);
			}
			return buffer.toString();
		} else if (node instanceof IASTName) {
			buffer.append(START_OF_LIST);
			buffer.append(node);
			return buffer.toString();
		} else if (node instanceof IASTTranslationUnit) {
			String fileName = ((IASTTranslationUnit) node).getFilePath();
			int lastSlash = fileName.lastIndexOf(FILE_SEPARATOR);

			if (lastSlash > 0) {
				buffer.append(START_OF_LIST);
				buffer.append(fileName.substring(lastSlash + 1)); // TODO make path relative to project, i.e. /projectName/path/file.c
			}

			return buffer.toString();
		} else if (node instanceof IASTDeclSpecifier) {
			buffer.append(START_OF_LIST);
			buffer.append(getSignature((IASTDeclSpecifier) node));
			return buffer.toString();
		} else if (node instanceof IASTPreprocessorIncludeStatement) {
			String path = ((IASTPreprocessorIncludeStatement) node).getPath();
			int lastSlash = path.lastIndexOf(FILE_SEPARATOR) + 1;
			buffer.append(START_OF_LIST);
			buffer.append(path.substring(lastSlash));
		} else if (node instanceof IASTPreprocessorObjectStyleMacroDefinition) {
			String name = ((IASTPreprocessorObjectStyleMacroDefinition) node).getName().toString();
			if (name != null) {
				buffer.append(START_OF_LIST);
				buffer.append(name);
			}
		} else if (node instanceof IASTLiteralExpression) {
			buffer.append(START_OF_LIST);
			buffer.append(node.toString());
		} else if (node instanceof IASTCastExpression) {
			buffer.append(START_OF_LIST);
			buffer.append(ASTStringUtil.getCastOperatorString((IASTCastExpression) node));
		} else if (node instanceof IASTUnaryExpression) {
			buffer.append(START_OF_LIST);
			buffer.append(ASTStringUtil.getUnaryOperatorString((IASTUnaryExpression) node));
		} else if (node instanceof IASTBinaryExpression) {
			buffer.append(START_OF_LIST);
			buffer.append(ASTStringUtil.getBinaryOperatorString((IASTBinaryExpression) node));
		} else if (node instanceof ICASTDesignator) {
			if (node instanceof ICASTArrayDesignator
					&& ((ICASTArrayDesignator) node).getSubscriptExpression() != null) {
				buffer.append(START_OF_LIST);
				buffer.append(((ICASTArrayDesignator) node).getSubscriptExpression());
			} else if (node instanceof ICASTFieldDesignator && ((ICASTFieldDesignator) node).getName() != null) {
				buffer.append(START_OF_LIST);
				buffer.append(((ICASTFieldDesignator) node).getName());
			} else if (node instanceof IGCCASTArrayRangeDesignator
					&& ((IGCCASTArrayRangeDesignator) node).getRangeCeiling() != null
					&& ((IGCCASTArrayRangeDesignator) node).getRangeFloor() != null) {
				buffer.append(START_OF_LIST);
				buffer.append(((IGCCASTArrayRangeDesignator) node).getRangeCeiling());
				buffer.append(DASH);
				buffer.append(((IGCCASTArrayRangeDesignator) node).getRangeFloor());
			}
		} else if (node instanceof IASTArrayModifier) {
			boolean started = false;
			if (node instanceof ICASTArrayModifier) {
				started = true;
				buffer.append(START_OF_LIST);
				if (((ICASTArrayModifier) node).isConst())
					buffer.append(CONST_);
				if (((ICASTArrayModifier) node).isRestrict())
					buffer.append(RESTRICT_);
				if (((ICASTArrayModifier) node).isStatic())
					buffer.append(STATIC_);
				if (((ICASTArrayModifier) node).isVolatile())
					buffer.append(VOLATILE_);
				if (((ICASTArrayModifier) node).isVariableSized())
					buffer.append(VARIABLE_SIZED_);
			}

			IASTExpression constantExpression = ((IASTArrayModifier) node).getConstantExpression();
			if (constantExpression != null && constantExpression instanceof IASTIdExpression) {
				if (!started)
					buffer.append(START_OF_LIST);
				buffer.append(((IASTIdExpression) constantExpression).getName().toString());
			}
		} else if (node instanceof IASTPointer) {
			boolean started = false;

			if (((IASTPointer) node).isConst()) {
				if (!started) {
					started = true;
					buffer.append(START_OF_LIST);
				}
				buffer.append(CONST_);
			}

			if (((IASTPointer) node).isVolatile()) {
				if (!started) {
					started = true;
					buffer.append(START_OF_LIST);
				}
				buffer.append(VOLATILE_);
			}

			if (((IASTPointer) node).isRestrict()) {
				if (!started) {
					started = true;
					buffer.append(START_OF_LIST);
				}
				buffer.append(RESTRICT_);
			}

		}

		return buffer.toString();
	}

	private String getSignature(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof IASTCompositeTypeSpecifier) {
			StringBuilder buf = new StringBuilder();
			IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) declSpec;
			switch (comp.getKey()) {
			case IASTCompositeTypeSpecifier.k_struct:
				buf.append(Keywords.cSTRUCT);
				break;
			case IASTCompositeTypeSpecifier.k_union:
				buf.append(Keywords.cUNION);
				break;
			default:
				buf.append(Keywords.cCLASS);
				break;
			}
			buf.append(' ');
			buf.append(comp.getName().toString());
			return buf.toString();
		} else if (declSpec instanceof IASTEnumerationSpecifier) {
			StringBuilder buf = new StringBuilder();
			IASTEnumerationSpecifier comp = (IASTEnumerationSpecifier) declSpec;
			buf.append(Keywords.cENUM);
			buf.append(' ');
			buf.append(comp.getName().toString());
			return buf.toString();
		}
		String intermed = declSpec.getRawSignature();
		return intermed.replaceAll("\\s+", " ");
	}

	private String getDeclaratorName(IASTDeclarator decltor) {
		String name = BLANK_STRING;
		while (decltor != null && decltor.getName() != null && decltor.getName().toString() == null) {
			decltor = decltor.getNestedDeclarator();
		}
		if (decltor != null && decltor.getName() != null) {
			name = decltor.getName().toString();
		}
		return name;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key == IPropertySource.class)
			return new ASTPropertySource(getNode());

		return null;
	}

	public String getFilename() {
		if (node == null)
			return BLANK_STRING;
		IASTFileLocation f = node.getFileLocation();
		if (f == null)
			return BLANK_STRING;
		return f.getFileName();
	}

	public int getOffset() {
		IASTFileLocation f = node.getFileLocation();
		if (f == null)
			return 0;
		return f.getNodeOffset();
	}

	public int getLength() {
		IASTFileLocation f = node.getFileLocation();
		if (f == null)
			return 0;
		return f.getNodeLength();
	}

	public void setFiltersFlag(int flag) {
		filterFlag |= flag;

		if (parent != null) {
			parent.setFiltersFlag(flag);
		}
	}

	public int getFiltersFlag() {
		return filterFlag;
	}

	public int relativeNodePosition(IASTNode n) {
		ASTNode astNode = (ASTNode) n;
		ASTNode thisNode = (ASTNode) getNode();
		if (thisNode.getOffset() > astNode.getOffset())
			return -1;
		if ((thisNode.getOffset() + thisNode.getLength()) < (astNode.getOffset() + astNode.getLength()))
			return 1;
		return 0;
	}

	private static class ASTPropertySource implements IPropertySource {
		private static final String DOMEXCEPTION_THIS_METHOD_ISN_T_SUPPORTED_BY_THIS_OBJECT = "DOMException - this method isn't supported by this Object"; //$NON-NLS-1$
		private static final String FLUSH_CACHE_METHOD_NAME = "flushCache"; //$NON-NLS-1$
		private static final IPropertyDescriptor[] BLANK_DESCRIPTORS = new IPropertyDescriptor[0];
		private static final String OPEN_PAREN = " ("; //$NON-NLS-1$
		private static final String CLOSE_PAREN = ")"; //$NON-NLS-1$
		private static final String L_BRACKET_STRING = "["; //$NON-NLS-1$
		private static final String R_BRACKET_STRING = "]"; //$NON-NLS-1$
		private static final String CLONE_METHOD_NAME = "clone"; //$NON-NLS-1$
		private static final String NO_ELEMENT_STRING = "[0]"; //$NON-NLS-1$
		private static final String SEMI = ";"; //$NON-NLS-1$
		private static final String GETTYPE_METHOD_NAME = "getType"; //$NON-NLS-1$
		private static final String EXCEPTION_ON = " on "; //$NON-NLS-1$
		private static final String NULL_STRING = "null"; //$NON-NLS-1$
		private static final String OBJECT_SEPARATOR = ", "; //$NON-NLS-1$
		private static final String COLON_SEPARATOR = ": "; //$NON-NLS-1$
		private static final String IBINDING_TAG = "IBinding: "; //$NON-NLS-1$
		private static final String EMPTY_PARAMETER = "()"; //$NON-NLS-1$
		private static final String NODE_PREFIX = "Node: "; //$NON-NLS-1$
		private static final String BINDING_PREFIX = "Binding: "; //$NON-NLS-1$
		private static final int DEFAULT_DESCRIPTOR_SIZE = 4;
		IASTNode node = null;

		public ASTPropertySource(IASTNode node) {
			this.node = node;
		}

		@Override
		public Object getEditableValue() {
			return null;
		}

		@Override
		public IPropertyDescriptor[] getPropertyDescriptors() {
			if (node instanceof IASTTranslationUnit) // skip the properties for the TU (too expensive)
				return BLANK_DESCRIPTORS;

			IPropertyDescriptor[] descriptors = new IPropertyDescriptor[DEFAULT_DESCRIPTOR_SIZE];

			if (node instanceof IASTName) {
				IPropertyDescriptor[] desc = getPropertyDescriptors(((IASTName) node).resolveBinding());
				if (desc != null)
					for (IPropertyDescriptor element : desc)
						descriptors = ArrayUtil.append(IPropertyDescriptor.class, descriptors, element);
				desc = getPropertyDescriptors(node);
				if (desc != null)
					for (IPropertyDescriptor element : desc)
						descriptors = ArrayUtil.append(IPropertyDescriptor.class, descriptors, element);

			} else {
				IPropertyDescriptor[] desc = getPropertyDescriptors(node);
				if (desc != null)
					for (IPropertyDescriptor element : desc)
						descriptors = ArrayUtil.append(IPropertyDescriptor.class, descriptors, element);
			}

			return ArrayUtil.trim(IPropertyDescriptor.class, descriptors);
		}

		private IPropertyDescriptor[] getPropertyDescriptors(Object obj) {
			IPropertyDescriptor[] desc = new IPropertyDescriptor[DEFAULT_DESCRIPTOR_SIZE];
			if (obj == null)
				return BLANK_DESCRIPTORS;
			Class<?> objClass = obj.getClass();
			Class<?>[] interfaces = objClass.getInterfaces();

			for (Class<?> interface1 : interfaces) {
				Method[] methods = interface1.getMethods();
				for (int j = 0; j < methods.length; j++) {
					// multiple interfaces can have the same method, so don't duplicate that method in the property view
					// which causes an ArrayIndexOutOfBoundsException elsewhere
					if (alreadyEncountered(methods[j], desc))
						continue;

					if (methods[j].getParameterTypes().length > 0 || (!shouldInvokeMethod(methods[j].getName())))
						continue; // only do getters, that aren't in the bad list (like clone())

					TextPropertyDescriptor text = null;
					if (obj instanceof IBinding)
						text = new TextPropertyDescriptor(BINDING_PREFIX + methods[j].getName(),
								methods[j].getName() + EMPTY_PARAMETER);
					else
						text = new TextPropertyDescriptor(NODE_PREFIX + methods[j].getName(),
								methods[j].getName() + EMPTY_PARAMETER);

					if (obj instanceof IBinding)
						text.setCategory(
								IBINDING_TAG
										+ ((IASTName) node).resolveBinding().getClass().getName()
												.substring(((IASTName) node).resolveBinding().getClass().getName()
														.lastIndexOf(PERIOD) + 1)
										+ COLON_SEPARATOR + getValueString(((IASTName) node).resolveBinding()));
					else
						text.setCategory(objClass.getName().substring(objClass.getName().lastIndexOf(PERIOD) + 1)
								+ COLON_SEPARATOR + getValueString(node));
					desc = ArrayUtil.append(IPropertyDescriptor.class, desc, text);
				}
			}

			return ArrayUtil.trim(IPropertyDescriptor.class, desc);
		}

		private boolean alreadyEncountered(Method method, IPropertyDescriptor[] desc) {
			StringBuilder name = null;
			for (IPropertyDescriptor element : desc) {
				if (element == null) // reached the end of the array
					break;

				name = new StringBuilder();
				name.append(method.getName());
				name.append(EMPTY_PARAMETER);
				if (name.toString().equals(element.getDisplayName()))
					return true;
			}

			return false;
		}

		@Override
		public Object getPropertyValue(Object id) {
			if (!(id instanceof String))
				return BLANK_STRING;

			Class<?> nodeClass = node.getClass();

			String value = BLANK_STRING;

			try {
				Object result = null;
				if (node instanceof IASTName && ((String) id).startsWith(BINDING_PREFIX)) {
					String methodName = id.toString();
					methodName = methodName.replaceAll(BINDING_PREFIX, BLANK_STRING);
					Method method = ((IASTName) node).resolveBinding().getClass().getMethod(methodName, new Class[0]); // only going to be getter methods...
					result = method.invoke(((IASTName) node).resolveBinding());
				} else {
					String methodName = id.toString();
					methodName = methodName.replaceAll(NODE_PREFIX, BLANK_STRING);
					Method method = nodeClass.getMethod(methodName, new Class[0]); // only going to be getter methods...
					method.setAccessible(true);
					result = method.invoke(node);
				}

				if (result == null) {
					value = NULL_STRING;
				} else if (result.getClass().isArray()) { // if it's an array
					if (result.getClass().getComponentType().equals(char.class)) // array of char
						value = String.valueOf((char[]) result);
					else if (result.getClass().isPrimitive()) {
						value = trimObjectToString(result.toString());
					} else
						value = getValueString((Object[]) result);
				} else {
					value = getValueString(result);
				}
			} catch (Exception e) {
				if (e instanceof InvocationTargetException) {
					if (((InvocationTargetException) e).getTargetException() instanceof DOMException)
						return DOMEXCEPTION_THIS_METHOD_ISN_T_SUPPORTED_BY_THIS_OBJECT;

					e.printStackTrace(); // display all exceptions to developers
					return trimObjectToString(((InvocationTargetException) e).getTargetException().toString())
							+ EXCEPTION_ON
							+ ((InvocationTargetException) e).getTargetException().getStackTrace()[0].toString();
				}

				return e.toString();
			}

			return value;
		}

		private String trimObjectToString(String str) {
			return str.substring(str.lastIndexOf(PERIOD) + 1);
		}

		private String getValueString(Object obj) {
			if (obj == null)
				return NULL_STRING;

			StringBuilder buffer = new StringBuilder();

			if (obj.getClass().isPrimitive()) {
				buffer.append(trimObjectToString(obj.toString()));
			} else if (obj instanceof ASTNodeProperty) {
				buffer.append(((ASTNodeProperty) obj).getName());
			} else if (obj instanceof IASTProblemHolder) {
				IASTProblemHolder ph = (IASTProblemHolder) obj;
				IASTProblem problem = ph.getProblem();
				buffer.append(problem.getMessage());
			} else if (obj instanceof IASTName) {
				final String toString = ((IASTName) obj).toString();
				if (toString != null) {
					buffer.append(trimObjectToString(toString));
					buffer.append(COLON_SEPARATOR);
					buffer.append(getType(((IASTName) obj).resolveBinding()));
				}
			} else if (obj instanceof IType) {
				buffer.append(getType(obj));
			} else if (obj instanceof IBinding) {
				buffer.append(((IBinding) obj).getName());
				buffer.append(COLON_SEPARATOR);
				buffer.append(getType(obj));
			} else if (obj instanceof IASTExpression) {
				buffer.append(ASTStringUtil.getExpressionString((IASTExpression) obj));
			} else if (obj instanceof IASTNode) {
				String utilString = DOMAST.getNodeSignature((IASTNode) obj);
				if (utilString != null && !utilString.equals(BLANK_STRING)) {
					buffer.append(trimObjectToString(obj.toString()));
					buffer.append(COLON_SEPARATOR);
					buffer.append(utilString);
				} else
					buffer.append(trimObjectToString(obj.toString()));
			} else
				buffer.append(obj.toString());

			if (obj instanceof IBinding) {
				buffer.append(OPEN_PAREN);
				buffer.append(Integer.toHexString(obj.hashCode()));
				buffer.append(CLOSE_PAREN);
			}

			return buffer.toString();
		}

		private String getType(Object obj) {
			if (obj == null)
				return NULL_STRING;

			if (obj instanceof IType)
				return ASTTypeUtil.getType((IType) obj);

			Method[] methods = obj.getClass().getMethods();
			boolean hasGetType = false;

			int i = 0;
			for (; i < methods.length; i++) {
				if (methods[i].getName().equals(GETTYPE_METHOD_NAME)) {
					hasGetType = true;
					break;
				}
			}

			if (hasGetType) {
				try {
					Object result = methods[i].invoke(obj);

					if (result instanceof IType) {
						return ASTTypeUtil.getType((IType) result);
					}
				} catch (Exception e) {
					if (e instanceof InvocationTargetException) {
						if (((InvocationTargetException) e).getTargetException() instanceof DOMException)
							return DOMEXCEPTION_THIS_METHOD_ISN_T_SUPPORTED_BY_THIS_OBJECT;

						e.printStackTrace(); // display all exceptions to developers
						return trimObjectToString(((InvocationTargetException) e).getTargetException().toString())
								+ EXCEPTION_ON
								+ ((InvocationTargetException) e).getTargetException().getStackTrace()[0].toString();
					}

					return e.toString();
				}
			}

			return BLANK_STRING; // if there is no type
		}

		private String getValueString(Object[] objs) {
			if (objs.length == 0)
				return trimObjectToString(objs.getClass().getName()).replaceAll(SEMI, BLANK_STRING) + NO_ELEMENT_STRING;

			StringBuilder buffer = new StringBuilder();
			buffer.append(L_BRACKET_STRING);
			for (int i = 0; i < objs.length; i++) {
				buffer.append(getValueString(objs[i]));
				if (i < objs.length - 1)
					buffer.append(OBJECT_SEPARATOR);
			}
			buffer.append(R_BRACKET_STRING);

			return buffer.toString();
		}

		// used to determine if a getter method should be invoked or not, there may be a list of them in the future...
		private boolean shouldInvokeMethod(String method) {
			if (method.equals(CLONE_METHOD_NAME))
				return false;
			if (method.equals(FLUSH_CACHE_METHOD_NAME))
				return false;
			if (method.startsWith("set"))
				return false;

			return true;
		}

		@Override
		public boolean isPropertySet(Object id) {
			return false;
		}

		@Override
		public void resetPropertyValue(Object id) {
		}

		@Override
		public void setPropertyValue(Object id, Object value) {
		}
	}

}
