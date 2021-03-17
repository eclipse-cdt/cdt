package org.eclipse.cdt.core.parser.tests.ast2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;

import java.io.IOException;
import java.nio.file.Paths;

public class AutoRangeTest extends AST2CPPTestBase {

	private static final String HEADER_PATH = "resources/ast2/AutoRangeTestHeader.hpp";
	private static String HEADER_CONTENT;

	static {
		try {
			HEADER_CONTENT = new String(readAllBytes(Paths.get(HEADER_PATH)), UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public AutoRangeTest(String name) {
		super(name);
	}

	//	int main()
	//	{
	//	  for (auto &&prxyyl : sw) {
	//	    prxyyl.doSth();
	//	  }
	//	  return 0;
	//	}
	public void testAutoReferenceType_loop() throws Exception {
		parseAndCheckBindings(HEADER_CONTENT + getAboveComment());
	}

	//	int main()
	//	{
	//	  auto && foo = *begin(sw);
	//	  foo.doSth();
	//
	//	  return 0;
	//	}
	public void testAutoReferenceType_autoVariableCall() throws Exception {
		parseAndCheckBindings(HEADER_CONTENT + getAboveComment());
	}

	//	int main()
	//	{
	//	  auto && fo = begin(sw).operator*();
	//	  fo.doSth();
	//
	//	  return 0;
	//	}
	public void testAutoReferenceType_operator() throws Exception {
		parseAndCheckBindings(HEADER_CONTENT + getAboveComment());
	}
}
