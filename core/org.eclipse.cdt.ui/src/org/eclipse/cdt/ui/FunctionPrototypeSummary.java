package org.eclipse.cdt.ui;

/**
 * This class is a helper class which takes care of implementing some of the 
 * function prototype parsing and stripping.
 */
public class FunctionPrototypeSummary implements IFunctionSummary.IFunctionPrototypeSummary {
	String fname;
	String freturn;
	String farguments;
		
	/**
	 * Creates a prototype which matches the format
	 * returntype function(arguments)
	 * @param properProto
	 */
	public FunctionPrototypeSummary(String proto) {
		int leftbracket = proto.indexOf('(');
		int rightbracket = proto.lastIndexOf(')');
		farguments = proto.substring(leftbracket + 1, rightbracket);
			
		int nameend = leftbracket - 1;
		while(proto.charAt(nameend) == ' ') {
			nameend--;
		}
		int namestart = nameend;
		while(namestart > 0 && proto.charAt(namestart) != ' ') {
			namestart--;
		}
		fname = proto.substring(namestart, nameend + 1).trim();
			
		if(namestart == 0) {
			freturn = "void";
		} else {
			freturn = proto.substring(0, namestart).trim();
		}
	}

	public String getName() {
		return fname;
	}

	public String getReturnType() {
		return freturn;
	}
		
	public String getArguments() {
		return farguments;
	}
		
	public String getPrototypeString(boolean namefirst) {
		StringBuffer buffer = new StringBuffer();
		if(!namefirst) {
			buffer.append(getArguments());
			buffer.append(" ");
		}
		buffer.append(getName());
		buffer.append("(");
		if(getArguments() != null) {
			buffer.append(getArguments());
		}
		buffer.append(")");
		if(namefirst) {
			buffer.append(" ");
			buffer.append(getReturnType());
		}
		return buffer.toString();
	}
}
