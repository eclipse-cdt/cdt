package org.eclipse.cdt.qt.internal.core;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.qt.core.QtKeywords;

/**
 * Extracts required information from FunctionCallExpressions that call
 * QObject::connect. The parts that are significant are the bindings to the
 * signal/slot functions (given in the 2nd and 4th parameters). The binding is
 * found by looking for matching signal/slot tagged functions in the type
 * specified as the 1st/3rd parameters. See the example in
 * {@link QtSignalSlotReferenceName}.
 *
 * @see QtSignalSlotReferenceName
 */
@SuppressWarnings("restriction")
public class QtConnectFunctionCall {
	private final IASTFunctionCallExpression fncall;

	// NOTE: This expression allows embedded line terminators (?s) for cases where the code looks like:
	// QObject::connect( &a, SIGNAL(
	//					sig1(
	//						int
	//					), ...
	// The two patterns are nearly identical.  The difference is because the first is for matching SIGNAL/
	// SLOT expansions.  The second is for matching the argument to that expansion.
	private static final Pattern Regex_SignalSlotExpansion = Pattern.compile("(?s)((?:SIGNAL)|(?:SLOT))\\s*\\(\\s*(.*?)\\s*\\)\\s*");
	private static final Pattern Regex_FunctionCall = Pattern.compile("(?s)\\s*(.*)\\s*\\(\\s*(.*?)\\s*\\)\\s*");

	public QtConnectFunctionCall(IASTFunctionCallExpression fncall) {
		this.fncall = fncall;
	}

	public Collection<IASTNode> getChildren() {

		IASTInitializerClause[] args = fncall.getArguments();
		if (args.length != 4)
			return null;

		return CollectionUtils.merge(
					getChildren(fncall, getBaseType(args[0]), args[1]),
					getChildren(fncall, getBaseType(args[2]), args[3]));
	}

	private static boolean matches(String expansionArgs, IBinding candidate) {
		if (!(candidate instanceof ICPPFunction))
			return false;

		ICPPFunction function = (ICPPFunction) candidate;

		Matcher m_expansion = Regex_FunctionCall.matcher(expansionArgs);
		if (!m_expansion.matches())
			return false;

		String expansionFunctionName = m_expansion.group(1);
		if (!expansionFunctionName.equals(function.getName()))
			return false;

		Matcher m_function = Regex_FunctionCall.matcher(function.getType().toString());
		if (!m_function.matches())
			return false;

		String functionParams = m_function.group(2);
		String expansionParams = m_expansion.group(2);

    	// TODO This should follow the Qt code for stripping space characters and shifting
    	//      const qualification.  For now it does the simpler job of just stripping the
    	//      whitespace.

		String typeStr_noWS = functionParams.replaceAll("\\s", "");
		String expansionParams_noWS = expansionParams.replaceAll("\\s", "");

		return typeStr_noWS.equals(expansionParams_noWS);
	}

	private static ICPPClassType getBaseType(IASTInitializerClause init) {

		if (!(init instanceof ICPPASTInitializerClause))
			return null;

		ICPPASTInitializerClause cppInit = (ICPPASTInitializerClause) init;
		ICPPEvaluation eval = cppInit.getEvaluation();
		if (eval == null)
			return null;

		IType type = eval.getTypeOrFunctionSet(cppInit);
		while (type instanceof IPointerType)
			type = ((IPointerType) type).getType();

		return type instanceof ICPPClassType ? (ICPPClassType) type : null;
	}

	private Collection<IASTNode> getChildren(IASTNode parent, ICPPClassType cls, IASTNode arg) {
		if (cls == null)
			return null;

		String raw = arg.getRawSignature();
		Matcher m = Regex_SignalSlotExpansion.matcher(raw);
		if (!m.matches())
			return null;

		String macroName = m.group(1);
		boolean findSignals = QtKeywords.SIGNAL.equals(macroName);
		boolean findSlots = QtKeywords.SLOT.equals(macroName);

		// Get the argument to the SIGNAL/SLOT macro and the offset/length of that argument within the
		// complete function argument.  E.g., with this argument to QObject::connect
		//      SIGNAL( signal(int) )
		// the values are
		//		expansionArgs:   "signal(int)"
		//		expansionOffset: 8
		//		expansionLength: 11
		String expansionArgs = m.group(2);
		int expansionOffset = m.start(2);
		int expansionLength = m.end(2) - expansionOffset;

		Set<IBinding> bindings = new LinkedHashSet<IBinding>();
		for (ICPPMethod method : cls.getMethods())
			if (QtSignalSlotTagger.isQtSignalOrSlot(method, findSignals, findSlots)
			 && matches(expansionArgs, method))
				bindings.add(method);

		List<IASTNode> children = new LinkedList<IASTNode>();
		for (IBinding binding : bindings)
			children.add(new QtSignalSlotReferenceName(parent, arg, expansionArgs, expansionOffset, expansionLength, binding));
		return children;
	}
}
