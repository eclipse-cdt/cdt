package org.eclipse.cdt.core.parser.tests.ast2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;

import java.io.IOException;
import java.nio.file.Paths;

public class IsAssignableTest extends AST2CPPTestWithGccExtensions {

	private static final String TEMPLATE_FILE_PATH = "resources/keywords/__is_assignable.keyword";
	private static final String CONTENT_TO_REPLACE = "is_assignable<param_1, param_2>";
	private static String TEMPLATE_FILE_CONTENT;

	private String code;

	static {
		try {
			TEMPLATE_FILE_CONTENT = new String(readAllBytes(Paths.get(TEMPLATE_FILE_PATH)), UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String createCodeFromTemplate() throws IOException {
		return TEMPLATE_FILE_CONTENT.replace(CONTENT_TO_REPLACE, getAboveComment());
	}

	public IsAssignableTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		code = createCodeFromTemplate();
	}

	// is_assignable<int&, int>
	public void test_intRef_int() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<int&, const int>
	public void test_intRef_constInt() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<int&, const int&>
	public void test_intRef_constIntRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<int&, int&>
	public void test_intRef_intRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<int, int>
	public void test_int_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<int, const int>
	public void test_int_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<int, int&>
	public void test_int_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<int, const int&>
	public void test_int_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int, int>
	public void test_constInt_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int, const int>
	public void test_constInt_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int, int&>
	public void test_constInt_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int, const int&>
	public void test_constInt_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int&, int>
	public void test_constIntRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int&, const int>
	public void test_constIntRef_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int&, int&>
	public void test_constIntRef_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<const int&, const int&>
	public void test_constIntRef_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	// is_assignable<Empty&, Empty>
	public void test_structRef_struct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty&, const Empty>
	public void test_structRef_constStruct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty&, Empty&>
	public void test_structRef_structRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty&, const Empty&>
	public void test_structRef_constStructRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty, Empty>
	public void test_struct_struct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty, const Empty>
	public void test_struct_constStruct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty, Empty&>
	public void test_struct_structRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<Empty, const Empty&>
	public void test_struct_constStructRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B&, B>
	public void test_ComplicatedStructRef_ComplicatedStruct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B&, const B>
	public void test_ComplicatedStructRef_constComplicatedStruct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B&, B&>
	public void test_ComplicatedStructRef_ComplicatedStructRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B&, const B&>
	public void test_ComplicatedStructRef_constComplicatedStructRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B, B>
	public void test_ComplicatedStruct_ComplicatedStruct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B, const B>
	public void test_ComplicatedStruct_constComplicatedStruct() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B, B&>
	public void test_ComplicatedStruct_ComplicatedStructRef() throws Exception {
		parseAndCheckBindings(code);
	}

	// is_assignable<B, const B&>
	public void test_ComplicatedSstruct_constComplicatedStructRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, bool>
	public void test_boolRef_bool() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, const bool>
	public void test_boolRef_constBool() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, bool&>
	public void test_boolRef_boolRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, const bool&>
	public void test_boolRef_consBoolRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool, bool>
	public void test_bool_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<bool, const bool>
	public void test_bool_constBool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<bool, bool&>
	public void test_bool_boolRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<bool, const bool&>
	public void test_bool_constBoolRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t&, nullptr_t>
	public void test_nullRef_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<nullptr_t&, const nullptr_t>
	public void test_nullRef_constNull() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<nullptr_t&, nullptr_t&>
	public void test_nullRef_NullRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<nullptr_t&, const nullptr_t&>
	public void test_nullRef_constNullRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<nullptr_t, nullptr_t>
	public void test_null_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, const nullptr_t>
	public void test_null_constNull() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, nullptr_t&>
	public void test_null_nullRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, const nullptr_t&>
	public void test_null_constNullRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, E>
	public void test_enumRef_enum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<E&, const E>
	public void test_enumRef_constEnum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<E&, E&>
	public void test_enumRef_enumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<E&, const E&>
	public void test_enumRef_constEnumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, E>
	public void test_intRef_enum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, const E>
	public void test_intRef_constEnum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, E&>
	public void test_intRef_enumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, const E&>
	public void test_intRef_constEnumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<E&, int>
	public void test_enumRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, const int>
	public void test_enumRef_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, int&>
	public void test_enumRef_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, const int&>
	public void test_enumRef_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, E2>
	public void test_enumRef_otherEnum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, const E2>
	public void test_enumRef_constOtherEnum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, E2&>
	public void test_enumRef_otherEnumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, const E2&>
	public void test_enumRef_constOtherEnumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E, E>
	public void test_enumRef_enum_enum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E, const E>
	public void test_enum_constEnum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E, E&>
	public void test_enum_enumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E, const E&>
	public void test_enum_constEnumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE&, SE>
	public void test_enumClassRef_enumClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<SE&, const SE>
	public void test_enumClassRef_constEnumClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<SE&, SE&>
	public void test_enumClassRef_enumClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<SE&, const SE&>
	public void test_enumClassRef_constEnumClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, SE>
	public void test_intRef_enumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, const SE>
	public void test_intRef_constEnumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, SE&>
	public void test_intRef_enumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, const SE&>
	public void test_intRef_constEnumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE&, int>
	public void test_enumClassRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE&, const int>
	public void test_enumClassRef_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE&, int&>
	public void test_enumClassRef_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE&, const int&>
	public void test_enumClassRef_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE, SE>
	public void test_enumClass_enumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE, const SE>
	public void test_enumClass_constEnumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE, SE&>
	public void test_enumClass_enumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE, const SE&>
	public void test_enumClass_constEnumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<AnyAssign&, int>
	public void test_structWithTemplateRef_int() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, nullptr_t>
	public void test_structWithTemplateRef_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, E>
	public void test_structWithTemplateRef_enum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, SE>
	public void test_structWithTemplateRef_enumClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, Empty>
	public void test_structWithTemplateRef_empty() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, U>
	public void test_structWithTemplateRef_unionWithInnerEmpty() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, int&>
	public void test_structWithTemplateRef_intRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, E&>
	public void test_structWithTemplateRef_enumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, SE&>
	public void test_structWithTemplateRef_enumClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, Empty&>
	public void test_structWithTemplateRef_emptyRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, U&>
	public void test_structWithTemplateRef_unionWithInnerEmptyRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, AnyAssign>
	public void test_structWithTemplateRef_structWithTemplate() throws Exception {
		parseAndCheckBindings(code);
	}

	//MISSING: probably working.
	//is_assignable<AnyAssign&, std::initializer_list<int>>

	//is_assignable<AnyAssign&, int[1]>
	public void test_structWithTemplateRef_intArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, nullptr_t[1]>
	public void test_structWithTemplateRef_nullArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, E[1]>
	public void test_structWithTemplateRef_enumArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, SE[1]>
	public void test_structWithTemplateRef_enumClassArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, int(&)[1]>
	public void test_structWithTemplateRef_intRefArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, nullptr_t(&)[1]>
	public void test_structWithTemplateRef_nullRefArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, E(&)[1]>
	public void test_structWithTemplateRef_enuRefArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AnyAssign&, SE(&)[1]>
	public void test_structWithTemplateRef_enumClassRefArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, E>
	public void test_boolRef_enum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, SE>
	public void test_boolRef_enumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<bool&, void*>
	public void test_boolRef_voidPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, int B::*>
	public void test_boolRef_structPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<bool&, nullptr_t>
	public void test_boolRef_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void*&, nullptr_t>
	public void test_voidPtr_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int*&, nullptr_t>
	public void test_intPtr_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int B::*&, nullptr_t>
	public void test_intPtrScoped_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<nullptr_t&, bool>
	public void test_null_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void*&, bool>
	public void test_voidPtr_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<E&, bool>
	public void test_enumRef_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<SE&, bool>
	public void test_enumClassRef_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const AnyAssign&, int>
	public void test_constStructWithTemplateRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<AnyAssign&, void>
	public void test_structWithTemplateRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, int>
	public void test_void_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const void, int>
	public void test_constVoid_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int, void>
	public void test_int_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int, const void>
	public void test_int_constVoid() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int, void>
	public void test_constInt_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int, const void>
	public void test_constInt_constVoid() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, void>
	public void test_intRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, const void>
	public void test_intRef_constVoid() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int&, void>
	public void test_constIntRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int&, const void>
	public void test_constIntRef_constVoid() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, void>
	public void test_void_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const void, void>
	public void test_constVoid_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const void, const void>
	public void test_constVoid_constVoid() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[1], int[1]>
	public void test_intArray_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[1], int[1]>
	public void test_intArrayRef_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[1], int(&)[1]>
	public void test_intArrayRef_intArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[2], int[1]>
	public void test_intArray_intArrayDifferentSize() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[2], int[1]>
	public void test_intArrayRef_intArrayDifferentSize() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[2], int(&)[1]>
	public void test_intArray_intArrayRefDifferentSize() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[1], void>
	public void test_intArray_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[1], void>
	public void test_intArrayRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, int[1]>
	public void test_void_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, int(&)[1]>
	public void test_void_intArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[], int[]>
	public void test_zeroSizeIntArray_zeroSizeIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[], int[]>
	public void test_zeroSizeIntArrayRef_zeroSizeIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[], int(&)[]>
	public void test_zeroSizeIntArrayRef_zeroSizeIntArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[1], int[]>
	public void test_intArray_zeroSizeIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[1], int[]>
	public void test_intArrayRef_zeroSizeIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[1], int(&)[]>
	public void test_intArrayRef_zeroSizeIntArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[], int[1]>
	public void test_zeroSizeIntArray_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[], int[1]>
	public void test_zeroSizeIntArrayRef_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[], int(&)[1]>
	public void test_zeroSizeIntArrayRef_intArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[], void>
	public void test_zeroSizeIntArray_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int(&)[], void>
	public void test_zeroSizeIntArrayRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, int[]>
	public void test_void_zeroSizeIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, int(&)[]>
	public void test_void_zeroSizeIntArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, DelCopyAssign>
	public void test_copyConstructClassRef_copyConstructClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelCopyAssign&, const DelCopyAssign>
	public void testCopyConstructClassRef_constCopyConstructClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, DelCopyAssign&>
	public void testCopyConstructClassRef_CopyConstructClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, const DelCopyAssign&>
	public void testCopyConstructClassRef_constCopyConstructClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, void>
	public void testCopyConstructClassRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, void()>
	public void testCopyConstructClassRef_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, void(&)()>
	public void testCopyConstructClassRef_voidRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelCopyAssign&, int>
	public void testCopyConstructClassRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, DelAnyAssign&&>
	public void test_delAnyAssignRef_DelAnyAssignRref() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelAnyAssign&, const DelAnyAssign&>
	public void test_delAnyAssignRef_constDelAnyAssignRref() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelAnyAssign, DelAnyAssign&&>
	public void test_delAnyAssign_DelAnyAssignRref() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelAnyAssign, const DelAnyAssign&>
	public void test_delAnyAssign_constDelAnyAssignRref() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<const DelAnyAssign&, DelAnyAssign&&>
	public void test_constDelAnyAssignRef_constDelAnyAssignRref() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const DelAnyAssign&, const DelAnyAssign&>
	public void test_constDelAnyAssignRef_constDelAnyAssignRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const DelAnyAssign, DelAnyAssign&&>
	public void test_constDelAnyAssign_constDelAnyAssignRref() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const DelAnyAssign, const DelAnyAssign&>
	public void test_constDelAnyAssign_constDelAnyAssignRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int>
	public void test_constDelAnyAssignRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int&>
	public void test_constDelAnyAssignRef_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, const int&>
	public void test_constDelAnyAssignRef_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, void>
	public void test_constDelAnyAssignRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, void()>
	public void test_constDelAnyAssignRef_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, void() const>
	public void test_constDelAnyAssignRef_constVoidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, void(&)()>
	public void test_constDelAnyAssignRef_voidRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, void(&&)()>
	public void test_constDelAnyAssignRef_voidRref() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, nullptr_t>
	public void test_constDelAnyAssignRef_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, nullptr_t&>
	public void test_constDelAnyAssignRef_nullRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, bool>
	public void test_constDelAnyAssignRef_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, bool&>
	public void test_constDelAnyAssignRef_boolRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, E>
	public void test_constDelAnyAssignRef_enum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, E&>
	public void test_constDelAnyAssignRef_enumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, SE>
	public void test_constDelAnyAssignRef_enumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, SE&>
	public void test_constDelAnyAssignRef_enumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, Empty>
	public void test_constDelAnyAssignRef_empty() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, Empty&>
	public void test_constDelAnyAssignRef_emtpyRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, B>
	public void test_constDelAnyAssignRef_B() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, B&>
	public void test_constDelAnyAssignRef_BRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, U>
	public void test_constDelAnyAssignRef_union() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, U&>
	public void test_constDelAnyAssignRef_unionRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, void*>
	public void test_constDelAnyAssignRef_voidPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int*>
	public void test_constDelAnyAssignRef_intPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, B*>
	public void test_constDelAnyAssignRef_BPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, D*>
	public void test_constDelAnyAssignRef_superPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int B::*>
	public void test_constDelAnyAssignRef_innerPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int D::*>
	public void test_constDelAnyAssignRef_innerSuperPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int[]>
	public void test_constDelAnyAssignRef_zeroSizedIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int[1]>
	public void test_constDelAnyAssignRef_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int(&)[]>
	public void test_constDelAnyAssignRef_zeroSizedIntArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelAnyAssign&, int(&)[1]>
	public void test_constDelAnyAssignRef_intArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(), void>
	public void test_void_voidFtk() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, void()>
	public void test_void_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(), void()>
	public void test_voidFkt_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(&)(), void>
	public void test_voidRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, void(&)()>
	public void test_void_voidRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(&)(), void(&)()>
	public void test_voidRef_voidRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(&)(), void()>
	public void test_voidRef_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(), void(&)()>
	public void test_voidFkt_voidRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, ImplicitTo<int>>
	public void test_intRef_implicitToInt() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, ExplicitTo<int>>
	public void test_intRef_explicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int, ImplicitTo<int>>
	public void test_int_implicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int, ExplicitTo<int>>
	public void test_int_explicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int, ImplicitTo<int>>
	public void test_constInt_implicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int, ExplicitTo<int>>
	public void test_constInt_explicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int&, ImplicitTo<int>>
	public void test_constIntRef_implicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int&, ExplicitTo<int>>
	public void test_constIntRef_explicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelImplicitTo<int>&, DelImplicitTo<int>>
	public void test_delImplicitToRef_delImplicitTo() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelImplicitTo<int>, DelImplicitTo<int>>
	public void test_delImplicitTo_delImplicitTo() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, DelImplicitTo<int>>
	public void test_intRef_delImplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int, DelImplicitTo<int>>
	public void test_int_delImplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int&, DelImplicitTo<int>>
	public void test_constIntRef_delImplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int, DelImplicitTo<int>>
	public void test_constInt_delImplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int&, DelExplicitTo<int>>
	public void test_intRef_delExplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int, DelExplicitTo<int>>
	public void test_int_delExplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int&, DelExplicitTo<int>>
	public void test_constIntRef_delExplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const int, DelExplicitTo<int>>
	public void test_constInt_delExplicitTo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<B*&, B*>
	public void test_BPtrRef_BPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<B*&, D*>
	public void test_BPtrRef_superPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<const B*&, D*>
	public void test_constBPtrRef_superPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<const B*&, const D*>
	public void test_constBPtrRef_constSuperPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<B*&, B*&>
	public void test_BPtrRef_BPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<B*&, D*&>
	public void test_BPtrRef_superPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<const B*&, B*&>
	public void test_constBPtrRef_BPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<const B*&, D*&>
	public void test_constBPtrRef_superPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<B* const&, B*&>
	public void test_constBPtrRef_BPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<B* const&, D*&>
	public void test_BPtrConstRef_superPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<D*&, B*>
	public void test_superPtrRef_BPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<D*&, B*&>
	public void test_superPtrRef_BPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO&, MO>
	public void test_moRef_mo() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<MO&, MO&&>
	public void test_moRef_moRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<MO, MO>
	public void test_mo_mo() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<MO, MO&&>
	public void test_mo_moRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<const MO&, MO>
	public void test_constMoRef_mo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const MO&, MO&&>
	public void test_constMoRef_moRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO&, const MO&&>
	public void test_moRef_constMoRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO&, MO&>
	public void test_moRef_moRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO&, const MO&>
	public void test_moRef_constMoRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const MO, MO>
	public void test_constMo_mo() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const MO, MO&&>
	public void test_constMo_moRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO, const MO&&>
	public void test_constMo_constMoRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO, MO&>
	public void test_mo_moRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<MO, const MO&>
	public void test_mo_constMoRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<NontrivialUnion&, NontrivialUnion>
	public void test_nonTrivialUnionRef_nonTrivialUnion() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<NontrivialUnion&, NontrivialUnion&&>
	public void test_nonTrivialUnionRef_nonTrivialUnionRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<NontrivialUnion&, NontrivialUnion&>
	public void test_nonTrivialUnionRef_nonTrivialUnionRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<NontrivialUnion&, const NontrivialUnion&>
	public void test_nonTrivialUnionRef_constNonTrivialUnionRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<NontrivialUnion&, const NontrivialUnion&&>
	public void test_nonTrivialUnionRef_constNonTrivialUnionRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<Abstract&, Abstract>
	public void test_abstractRef_abstract() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&, Abstract&&>
	public void test_abstractRef_abstractRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&, Abstract&>
	public void test_abstractRef_abstractRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&, const Abstract&>
	public void test_abstractRef_constAbstractRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&, const Abstract&&>
	public void test_abstractRef_constAbstractRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&&, Abstract>
	public void test_abstractRValueRef_abstract() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&&, Abstract&&>
	public void test_abstractRValueRef_abstractRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&&, Abstract&>
	public void test_abstractRValueRef_abstractRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&&, const Abstract&>
	public void test_abstractRValueRef_constAbstractRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Abstract&&, const Abstract&&>
	public void test_abstractRValueRef_constAbstractRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&, AbstractDelDtor>
	public void test_abstractDelDetorRef_AbstractDelDtor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&, AbstractDelDtor&&>
	public void test_abstractDelDetorRef_AbstractDelDtorRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&, AbstractDelDtor&>
	public void test_abstractDelDetorRef_AbstractDelDtorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&, const AbstractDelDtor&>
	public void test_abstractDelDetorRef_constAbstractDelDtorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&, const AbstractDelDtor&&>
	public void test_abstractDelDetorRef_constAbstractDelDtorRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&&, AbstractDelDtor>
	public void test_abstractDelDetorRValueRef_AbstractDelDtor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&&, AbstractDelDtor&&>
	public void test_abstractDelDetorRValueRef_AbstractDelDtorRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&&, AbstractDelDtor&>
	public void test_abstractDelDetorRValueRef_AbstractDelDtorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&&, const AbstractDelDtor&>
	public void test_abstractDelDetorRValueRef_constAbstractDelDtorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<AbstractDelDtor&&, const AbstractDelDtor&&>
	public void test_abstractDelDetorRValueRef_constAbstractDelDtorRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&, DelDef>
	public void test_delDefRef_delDef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&, DelDef&&>
	public void test_delDefRef_delDefRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&, DelDef&>
	public void test_delDefRef_delDefRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&, const DelDef&>
	public void test_delDefRef_constDelDefRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&, const DelDef&&>
	public void test_delDefRef_constDelDefRValueef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&&, DelDef>
	public void test_delDefRValueRef_delDef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&&, DelDef&&>
	public void test_delDefRValueRef_delDefRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&&, DelDef&>
	public void test_delDefRValueRef_delDefRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&&, const DelDef&>
	public void test_delDefRValueRef_constDelDefRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelDef&&, const DelDef&&>
	public void test_delDefRValueRef_constDelDefRValueRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, Ellipsis>
	public void test_ellipsisConstructorRef_ellipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const Ellipsis>
	public void test_ellipsisConstructorRef_constEllipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, Ellipsis&>
	public void test_ellipsisConstructorRef_ellipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const Ellipsis&>
	public void test_ellipsisConstructorRef_constEllipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis, Ellipsis>
	public void test_ellipsisConstructor_ellipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis, const Ellipsis>
	public void test_ellipsisConstructor_constEllipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis, Ellipsis&>
	public void test_ellipsisConstructor_ellipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis, const Ellipsis&>
	public void test_ellipsisConstructor_constEllipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, void>
	public void test_ellipsisConstructorRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<Ellipsis&, int>
	public void test_ellipsisConstructorRef_int() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const int>
	public void test_ellipsisConstructorRef_constInt() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, int&>
	public void test_ellipsisConstructorRef_intRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const int&>
	public void test_ellipsisConstructorRef_constIntRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, Empty>
	public void test_ellipsisConstructorRef_empty() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const Empty>
	public void test_ellipsisConstructorRef_constEmpty() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, Empty&>
	public void test_ellipsisConstructorRef_emptyRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const Empty&>
	public void test_ellipsisConstructorRef_constEmptyRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, E>
	public void test_ellipsisConstructorRef_enum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const E>
	public void test_ellipsisConstructorRef_constEnum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, E&>
	public void test_ellipsisConstructorRef_enumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const E&>
	public void test_ellipsisConstructorRef_constEnumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, SE>
	public void test_ellipsisConstructorRef_enumClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const SE>
	public void test_ellipsisConstructorRef_constEnumClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, SE&>
	public void test_ellipsisConstructorRef_enumClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const SE&>
	public void test_ellipsisConstructorRef_constEnumClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, bool>
	public void test_ellipsisConstructorRef_bool() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const bool>
	public void test_ellipsisConstructorRef_constBool() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, bool&>
	public void test_ellipsisConstructorRef_boolRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const bool&>
	public void test_ellipsisConstructorRef_constBoolRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, nullptr_t>
	public void test_ellipsisConstructorRef_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const nullptr_t>
	public void test_ellipsisConstructorRef_constNull() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, nullptr_t&>
	public void test_ellipsisConstructorRef_nullRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const nullptr_t&>
	public void test_ellipsisConstructorRef_constNullRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, void*>
	public void test_ellipsisConstructorRef_voidPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const void*>
	public void test_ellipsisConstructorRef_constVoidPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, void*&>
	public void test_ellipsisConstructorRef_voidPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, const void*&>
	public void test_ellipsisConstructorRef_constVoidPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, void()>
	public void test_ellipsisConstructorRef_voidFkt() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Ellipsis&, void(&)()>
	public void test_ellipsisConstructorRef_voidFktRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis&, DelEllipsis>
	public void test_deletedEllipsisConstructorRef_deletedEllipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis&, const DelEllipsis>
	public void test_deletedEllipsisConstructorRef_constDeletedEllipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis&, DelEllipsis&>
	public void test_deletedEllipsisConstructorRef_deletedEllipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis&, const DelEllipsis&>
	public void test_deletedEllipsisConstructorRef_constDeletedEllipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis, DelEllipsis>
	public void test_deletedEllipsisConstructor_deletedEllipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis, const DelEllipsis>
	public void test_deletedEllipsisConstructor_constDeletedEllipsisConstructor() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis, DelEllipsis&>
	public void test_deletedEllipsisConstructor_deletedEllipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis, const DelEllipsis&>
	public void test_deletedEllipsisConstructor_constDeletedEllipsisConstructorRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<DelEllipsis&, void>
	public void test_deletedEllipsisConstructorRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, int>
	public void test_deletedEllipsisConstructorRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const int>
	public void test_deletedEllipsisConstructorRef_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, int&>
	public void test_deletedEllipsisConstructorRef_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const int&>
	public void test_deletedEllipsisConstructorRef_constIntRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, Empty>
	public void test_deletedEllipsisConstructorRef_empty() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const Empty>
	public void test_deletedEllipsisConstructorRef_constEmpty() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, Empty&>
	public void test_deletedEllipsisConstructorRef_emptyRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const Empty&>
	public void test_deletedEllipsisConstructorRef_constEmptyRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, E>
	public void test_deletedEllipsisConstructorRef_enum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const E>
	public void test_deletedEllipsisConstructorRef_constEnum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, E&>
	public void test_deletedEllipsisConstructorRef_enumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const E&>
	public void test_deletedEllipsisConstructorRef_constEnumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, SE>
	public void test_deletedEllipsisConstructorRef_enumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const SE>
	public void test_deletedEllipsisConstructorRef_constEnumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, SE&>
	public void test_deletedEllipsisConstructorRef_enumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const SE&>
	public void test_deletedEllipsisConstructorRef_constEnumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, bool>
	public void test_deletedEllipsisConstructorRef_bool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const bool>
	public void test_deletedEllipsisConstructorRef_constBool() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, bool&>
	public void test_deletedEllipsisConstructorRef_boolRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const bool&>
	public void test_deletedEllipsisConstructorRef_constBoolRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, nullptr_t>
	public void test_deletedEllipsisConstructorRef_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const nullptr_t>
	public void test_deletedEllipsisConstructorRef_constNull() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, nullptr_t&>
	public void test_deletedEllipsisConstructorRef_nullRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const nullptr_t&>
	public void test_deletedEllipsisConstructorRef_constNullRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, void*>
	public void test_deletedEllipsisConstructorRef_voidPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const void*>
	public void test_deletedEllipsisConstructorRef_constVoidPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, void*&>
	public void test_deletedEllipsisConstructorRef_voidPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, const void*&>
	public void test_deletedEllipsisConstructorRef_constVoidPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, void()>
	public void test_deletedEllipsisConstructorRef_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelEllipsis&, void(&)()>
	public void test_deletedEllipsisConstructorRef_voidFktRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<FromArgs<int>&, int>
	public void test_fromIntRef_int() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<FromArgs<int>&, const int>
	public void test_fromIntRef_constInt() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<FromArgs<int>&, ImplicitTo<int>>
	public void test_fromIntRef_implicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<FromArgs<int>&, ImplicitTo<const int>>
	public void test_fromIntRef_implicitToConstInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<FromArgs<int>&, ExplicitTo<int>>
	public void test_fromIntRef_explicitToInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<FromArgs<int>&, ExplicitTo<const int>>
	public void test_fromIntRef_explicitToConstInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelFromArgs<int>&, int>
	public void test_delFromIntRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<DelFromArgs<int>&, const int>
	public void test_delFromIntRef_constInt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(*&)(), ImplicitTo<void(*)()>>
	public void test_voidFktRef_implicitToVoidFktRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<void(*&)(), ExplicitTo<void(*)()>>
	public void test_voidFktRef_explicitToVoidFktRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UAssignAll&, UAssignAll>
	public void test_unionAssignAllRef_unionAssignAll() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, const UAssignAll>
	public void test_unionAssignAllRef_constUnionAssignAll() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, UAssignAll&>
	public void test_unionAssignAllRef_unionAssignAllRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, const UAssignAll&>
	public void test_unionAssignAllRef_constUnionAssignAllRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll, UAssignAll>
	public void test_unionAssignAll_unionAssignAll() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll, const UAssignAll>
	public void test_unionAssignAll_constUnionAssignAll() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll, UAssignAll&>
	public void test_unionAssignAll_unionAssignAllRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll, const UAssignAll&>
	public void test_unionAssignAll_constUnionAssignAllRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void>
	public void test_unionAssignAllRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const UAssignAll&, void>
	public void test_constUnionAssignAllRef_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const UAssignAll&, UAssignAll>
	public void test_constUnionAssignAllRef_unionAssignAll() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const UAssignAll&, const UAssignAll>
	public void test_constUnionAssignAllRef_constUnassignAll() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const UAssignAll&, UAssignAll&>
	public void test_unionAssignAllRef_unionAnassignAllRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const UAssignAll&, const UAssignAll&>
	public void test_unionAssignAllRef_constUnionAnassignAllRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UAssignAll&, void() const>
	public void test_unionAssignAllRef_constVoid() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UAssignAll&, void() &>
	public void test_unionAssignAllRef_voidRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UAssignAll&, void() const volatile &&>
	public void test_unionAssignAllRef_voidRValueRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UAssignAll&, int>
	public void test_unionAssignAllRef_int() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int&>
	public void test_unionAssignAllRef_intRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, E>
	public void test_unionAssignAllRef_enum() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, E&>
	public void test_unionAssignAllRef_enumRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, SE>
	public void test_unionAssignAllRef_enumClass() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, SE&>
	public void test_unionAssignAllRef_enumClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, double>
	public void test_unionAssignAllRef_double() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, double&>
	public void test_unionAssignAllRef_doubleRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, Empty>
	public void test_unionAssignAllRef_empty() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, Empty&>
	public void test_unionAssignAllRef_emptyRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, B>
	public void test_unionAssignAllRef_b() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, B&>
	public void test_unionAssignAllRef_bRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, U>
	public void test_unionAssignAllRef_union() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, U&>
	public void test_unionAssignAllRef_unionRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, nullptr_t>
	public void test_unionAssignAllRef_null() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, nullptr_t&>
	public void test_unionAssignAllRef_nullRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void()>
	public void test_unionAssignAllRef_voidFkt() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void(&)()>
	public void test_unionAssignAllRef_voidFktRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void(*)()>
	public void test_unionAssignAllRef_voidFktPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void(*&)()>
	public void test_unionAssignAllRef_voidFktPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int*>
	public void test_unionAssignAllRef_intPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int*&>
	public void test_unionAssignAllRef_intPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void*>
	public void test_unionAssignAllRef_voidPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, void*&>
	public void test_unionAssignAllRef_voidPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, const int*>
	public void test_unionAssignAllRef_constIntPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, const int*&>
	public void test_unionAssignAllRef_constIntPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, const void*>
	public void test_unionAssignAllRef_constVoidPtr() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, const void*&>
	public void test_unionAssignAllRef_constVoidPtrRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int[1]>
	public void test_unionAssignAllRef_intArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int(&)[1]>
	public void test_unionAssignAllRef_intArrayRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int[]>
	public void test_unionAssignAllRef_emptyIntArray() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UAssignAll&, int(&)[]>
	public void test_unionAssignAllRef_emptyIntArrayRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<UDelAssignAll&, int>
	public void test_unionDelAssignAllRef_int() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int&>
	public void test_unionDelAssignAllRef_intRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, E>
	public void test_unionDelAssignAllRef_enum() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, E&>
	public void test_unionDelAssignAllRef_enumRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, SE>
	public void test_unionDelAssignAllRef_enumClass() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, SE&>
	public void test_unionDelAssignAllRef_enumClassRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, double>
	public void test_unionDelAssignAllRef_double() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, double&>
	public void test_unionDelAssignAllRef_doubleRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, Empty>
	public void test_unionDelAssignAllRef_empty() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, Empty&>
	public void test_unionDelAssignAllRef_emptyRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, B>
	public void test_unionDelAssignAllRef_complicatedStruct() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, B&>
	public void test_unionDelAssignAllRef_complicatedStructRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, U>
	public void test_unionDelAssignAllRef_union() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, U&>
	public void test_unionDelAssignAllRef_unionRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, nullptr_t>
	public void test_unionDelAssignAllRef_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, std::nullptr_t&>
	public void test_unionDelAssignAllRef_nullRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void()>
	public void test_unionDelAssignAllRef_voidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void(&)()>
	public void test_unionDelAssignAllRef_voidFktRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void()  const>
	public void test_unionDelAssignAllRef_constVoidFkt() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void(*)()>
	public void test_unionDelAssignAllRef_voidFktPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void(*&)()>
	public void test_unionDelAssignAllRef_voidFktPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int*>
	public void test_unionDelAssignAllRef_intPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int*&>
	public void test_unionDelAssignAllRef_intPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void*>
	public void test_unionDelAssignAllRef_voidPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, void*&>
	public void test_unionDelAssignAllRef_voidPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, const int*>
	public void test_unionDelAssignAllRef_constIntPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, const int*&>
	public void test_unionDelAssignAllRef_constIntPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, const void*>
	public void test_unionDelAssignAllRef_constVoidPtr() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, const void*&>
	public void test_unionDelAssignAllRef_constVoidPtrRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int[1]>
	public void test_unionDelAssignAllRef_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int(&)[1]>
	public void test_unionDelAssignAllRef_intArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int[]>
	public void test_unionDelAssignAllRef_emptyIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<UDelAssignAll&, int(&)[]>
	public void test_unionDelAssignAllRef_emptyIntArrayRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(&)(), nullptr_t>
	public void test_voidFktPtr_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, void(&)()>
	public void test_null_voidFktRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void(&)(), int[]>
	public void test_voidFktRef_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[], void(&)()>
	public void test_intArray_voidFktRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[], nullptr_t>
	public void test_emptyIntArray_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, int[]>
	public void test_null_emptyIntArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<int[1], nullptr_t>
	public void test_intArray_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, int[1]>
	public void test_null_intArray() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<void, std::nullptr_t>
	public void test_void_null() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<nullptr_t, void>
	public void test_null_void() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const D&, B&>
	public void test_subclassRef_classRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<const B&, B&>
	public void test_classRef_classRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}

	//is_assignable<B&, const D&>
	public void test_subClassRef_constSuperClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<B&, const B&>
	public void test_classRef_constClassRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<int&, const double&>
	public void test_intRef_constDoubleRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<B&, const F&>
	public void test_subClassRef_constSuperClassRecursiveRef() throws Exception {
		parseAndCheckBindings(code);
	}

	//is_assignable<Empty&, const F&>
	public void test_EmptyRef_constSuperClassRecursiveRef() throws Exception {
		parseAndCheckBindingsHasErrors(code);
	}
}
