/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.dwarf;

/**
 * DWARF constant.
 */
public class DwarfConstants {

	/* Tags. */

	public final static int DW_TAG_array_type = 0x01;
	public final static int DW_TAG_class_type = 0x02;
	public final static int DW_TAG_entry_point = 0x03;
	public final static int DW_TAG_enumeration_type = 0x04;
	public final static int DW_TAG_formal_parameter = 0x05;
	public final static int DW_TAG_imported_declaration = 0x08;
	public final static int DW_TAG_label = 0x0a;
	public final static int DW_TAG_lexical_block = 0x0b;
	public final static int DW_TAG_member = 0x0d;
	public final static int DW_TAG_pointer_type = 0x0f;
	public final static int DW_TAG_reference_type = 0x10;
	public final static int DW_TAG_compile_unit = 0x11;
	public final static int DW_TAG_string_type = 0x12;
	public final static int DW_TAG_structure_type = 0x13;
	public final static int DW_TAG_subroutine_type = 0x15;
	public final static int DW_TAG_typedef = 0x16;
	public final static int DW_TAG_union_type = 0x17;
	public final static int DW_TAG_unspecified_parameters = 0x18;
	public final static int DW_TAG_variant = 0x19;
	public final static int DW_TAG_common_block = 0x1a;
	public final static int DW_TAG_common_inclusion = 0x1b;
	public final static int DW_TAG_inheritance = 0x1c;
	public final static int DW_TAG_inlined_subroutine = 0x1d;
	public final static int DW_TAG_module = 0x1e;
	public final static int DW_TAG_ptr_to_member_type = 0x1f;
	public final static int DW_TAG_set_type = 0x20;
	public final static int DW_TAG_subrange_type = 0x21;
	public final static int DW_TAG_with_stmt = 0x22;
	public final static int DW_TAG_access_declaration = 0x23;
	public final static int DW_TAG_base_type = 0x24;
	public final static int DW_TAG_catch_block = 0x25;
	public final static int DW_TAG_const_type = 0x26;
	public final static int DW_TAG_constant = 0x27;
	public final static int DW_TAG_enumerator = 0x28;
	public final static int DW_TAG_file_type = 0x29;
	public final static int DW_TAG_friend = 0x2a;
	public final static int DW_TAG_namelist = 0x2b;
	public final static int DW_TAG_namelist_item = 0x2c;
	public final static int DW_TAG_packed_type = 0x2d;
	public final static int DW_TAG_subprogram = 0x2e;
	public final static int DW_TAG_template_type_param = 0x2f;
	public final static int DW_TAG_template_value_param = 0x30;
	public final static int DW_TAG_thrown_type = 0x31;
	public final static int DW_TAG_try_block = 0x32;
	public final static int DW_TAG_variant_part = 0x33;
	public final static int DW_TAG_variable = 0x34;
	public final static int DW_TAG_volatile_type = 0x35;
	public final static int DW_TAG_lo_user = 0x4080;
	public final static int DW_TAG_MIPS_loop = 0x4081;
	public final static int DW_TAG_format_label = 0x4101;
	public final static int DW_TAG_function_template = 0x4102;
	public final static int DW_TAG_class_template = 0x4103;
	public final static int DW_TAG_hi_user = 0xffff;

	/* Children determination encodings. */
	public final static int DW_CHILDREN_no = 0;
	public final static int DW_CHILDREN_yes = 1;

	/* DWARF attributes encodings. */
	public final static int DW_AT_sibling = 0x01;
	public final static int DW_AT_location = 0x02;
	public final static int DW_AT_name = 0x03;
	public final static int DW_AT_ordering = 0x09;
	public final static int DW_AT_subscr_data = 0x0a;
	public final static int DW_AT_byte_size = 0x0b;
	public final static int DW_AT_bit_offset = 0x0c;
	public final static int DW_AT_bit_size = 0x0d;
	public final static int DW_AT_element_list = 0x0f;
	public final static int DW_AT_stmt_list = 0x10;
	public final static int DW_AT_low_pc = 0x11;
	public final static int DW_AT_high_pc = 0x12;
	public final static int DW_AT_language = 0x13;
	public final static int DW_AT_member = 0x14;
	public final static int DW_AT_discr = 0x15;
	public final static int DW_AT_discr_value = 0x16;
	public final static int DW_AT_visibility = 0x17;
	public final static int DW_AT_import = 0x18;
	public final static int DW_AT_string_length = 0x19;
	public final static int DW_AT_common_reference = 0x1a;
	public final static int DW_AT_comp_dir = 0x1b;
	public final static int DW_AT_const_value = 0x1c;
	public final static int DW_AT_containing_type = 0x1d;
	public final static int DW_AT_default_value = 0x1e;
	public final static int DW_AT_inline = 0x20;
	public final static int DW_AT_is_optional = 0x21;
	public final static int DW_AT_lower_bound = 0x22;
	public final static int DW_AT_producer = 0x25;
	public final static int DW_AT_prototyped = 0x27;
	public final static int DW_AT_return_addr = 0x2a;
	public final static int DW_AT_start_scope = 0x2c;
	public final static int DW_AT_stride_size = 0x2e;
	public final static int DW_AT_upper_bound = 0x2f;
	public final static int DW_AT_abstract_origin = 0x31;
	public final static int DW_AT_accessibility = 0x32;
	public final static int DW_AT_address_class = 0x33;
	public final static int DW_AT_artificial = 0x34;
	public final static int DW_AT_base_types = 0x35;
	public final static int DW_AT_calling_convention = 0x36;
	public final static int DW_AT_count = 0x37;
	public final static int DW_AT_data_member_location = 0x38;
	public final static int DW_AT_decl_column = 0x39;
	public final static int DW_AT_decl_file = 0x3a;
	public final static int DW_AT_decl_line = 0x3b;
	public final static int DW_AT_declaration = 0x3c;
	public final static int DW_AT_discr_list = 0x3d;
	public final static int DW_AT_encoding = 0x3e;
	public final static int DW_AT_external = 0x3f;
	public final static int DW_AT_frame_base = 0x40;
	public final static int DW_AT_friend = 0x41;
	public final static int DW_AT_identifier_case = 0x42;
	public final static int DW_AT_macro_info = 0x43;
	public final static int DW_AT_namelist_items = 0x44;
	public final static int DW_AT_priority = 0x45;
	public final static int DW_AT_segment = 0x46;
	public final static int DW_AT_specification = 0x47;
	public final static int DW_AT_static_link = 0x48;
	public final static int DW_AT_type = 0x49;
	public final static int DW_AT_use_location = 0x4a;
	public final static int DW_AT_variable_parameter = 0x4b;
	public final static int DW_AT_virtuality = 0x4c;
	public final static int DW_AT_vtable_elem_location = 0x4d;
	public final static int DW_AT_lo_user = 0x2000;
	public final static int DW_AT_MIPS_fde = 0x2001;
	public final static int DW_AT_MIPS_loop_begin = 0x2002;
	public final static int DW_AT_MIPS_tail_loop_begin = 0x2003;
	public final static int DW_AT_MIPS_epilog_begin = 0x2004;
	public final static int DW_AT_MIPS_loop_unroll_factor = 0x2005;
	public final static int DW_AT_MIPS_software_pipeline_depth = 0x2006;
	public final static int DW_AT_MIPS_linkage_name = 0x2007;
	public final static int DW_AT_MIPS_stride = 0x2008;
	public final static int DW_AT_MIPS_abstract_name = 0x2009;
	public final static int DW_AT_MIPS_clone_origin = 0x200a;
	public final static int DW_AT_MIPS_has_inlines = 0x200b;
	public final static int DW_AT_MIPS_stride_byte = 0x200c;
	public final static int DW_AT_MIPS_stride_elem = 0x200d;
	public final static int DW_AT_MIPS_ptr_dopetype = 0x200e;
	public final static int DW_AT_MIPS_allocatable_dopetype = 0x200f;
	public final static int DW_AT_MIPS_assumed_shape_dopetype = 0x2010;
	public final static int DW_AT_MIPS_assumed_size = 0x2011;
	public final static int DW_AT_sf_names = 0x2101;
	public final static int DW_AT_src_info = 0x2102;
	public final static int DW_AT_mac_info = 0x2103;
	public final static int DW_AT_src_coords = 0x2104;
	public final static int DW_AT_body_begin = 0x2105;
	public final static int DW_AT_body_end = 0x2106;
	public final static int DW_AT_hi_user = 0x3fff;

	/* DWARF form encodings. */
	public final static int DW_FORM_addr = 0x01;
	public final static int DW_FORM_block2 = 0x03;
	public final static int DW_FORM_block4 = 0x04;
	public final static int DW_FORM_data2 = 0x05;
	public final static int DW_FORM_data4 = 0x06;
	public final static int DW_FORM_data8 = 0x07;
	public final static int DW_FORM_string = 0x08;
	public final static int DW_FORM_block = 0x09;
	public final static int DW_FORM_block1 = 0x0a;
	public final static int DW_FORM_data1 = 0x0b;
	public final static int DW_FORM_flag = 0x0c;
	public final static int DW_FORM_sdata = 0x0d;
	public final static int DW_FORM_strp = 0x0e;
	public final static int DW_FORM_udata = 0x0f;
	public final static int DW_FORM_ref_addr = 0x10;
	public final static int DW_FORM_ref1 = 0x11;
	public final static int DW_FORM_ref2 = 0x12;
	public final static int DW_FORM_ref4 = 0x13;
	public final static int DW_FORM_ref8 = 0x14;
	public final static int DW_FORM_ref_udata = 0x15;
	public final static int DW_FORM_indirect = 0x16;

	/* DWARF location operation encodings. */
	public final static int DW_OP_addr = 0x03; /* Constant address. */
	public final static int DW_OP_deref = 0x06;
	public final static int DW_OP_const1u = 0x08; /* Unsigned 1-byte constant. */
	public final static int DW_OP_const1s = 0x09; /* Signed 1-byte constant. */
	public final static int DW_OP_const2u = 0x0a; /* Unsigned 2-byte constant. */
	public final static int DW_OP_const2s = 0x0b; /* Signed 2-byte constant. */
	public final static int DW_OP_const4u = 0x0c; /* Unsigned 4-byte constant. */
	public final static int DW_OP_const4s = 0x0d; /* Signed 4-byte constant. */
	public final static int DW_OP_const8u = 0x0e; /* Unsigned 8-byte constant. */
	public final static int DW_OP_const8s = 0x0f; /* Signed 8-byte constant. */
	public final static int DW_OP_constu = 0x10; /* Unsigned LEB128 constant. */
	public final static int DW_OP_consts = 0x11; /* Signed LEB128 constant. */
	public final static int DW_OP_dup = 0x12;
	public final static int DW_OP_drop = 0x13;
	public final static int DW_OP_over = 0x14;
	public final static int DW_OP_pick = 0x15; /* 1-byte stack index. */
	public final static int DW_OP_swap = 0x16;
	public final static int DW_OP_rot = 0x17;
	public final static int DW_OP_xderef = 0x18;
	public final static int DW_OP_abs = 0x19;
	public final static int DW_OP_and = 0x1a;
	public final static int DW_OP_div = 0x1b;
	public final static int DW_OP_minus = 0x1c;
	public final static int DW_OP_mod = 0x1d;
	public final static int DW_OP_mul = 0x1e;
	public final static int DW_OP_neg = 0x1f;
	public final static int DW_OP_not = 0x20;
	public final static int DW_OP_or = 0x21;
	public final static int DW_OP_plus = 0x22;
	public final static int DW_OP_plus_uconst = 0x23; /* Unsigned LEB128 addend. */
	public final static int DW_OP_shl = 0x24;
	public final static int DW_OP_shr = 0x25;
	public final static int DW_OP_shra = 0x26;
	public final static int DW_OP_xor = 0x27;
	public final static int DW_OP_bra = 0x28; /* Signed 2-byte constant. */
	public final static int DW_OP_eq = 0x29;
	public final static int DW_OP_ge = 0x2a;
	public final static int DW_OP_gt = 0x2b;
	public final static int DW_OP_le = 0x2c;
	public final static int DW_OP_lt = 0x2d;
	public final static int DW_OP_ne = 0x2e;
	public final static int DW_OP_skip = 0x2f; /* Signed 2-byte constant. */
	public final static int DW_OP_lit0 = 0x30; /* Literal 0. */
	public final static int DW_OP_lit1 = 0x31; /* Literal 1. */
	public final static int DW_OP_lit2 = 0x32; /* Literal 2. */
	public final static int DW_OP_lit3 = 0x33; /* Literal 3. */
	public final static int DW_OP_lit4 = 0x34; /* Literal 4. */
	public final static int DW_OP_lit5 = 0x35; /* Literal 5. */
	public final static int DW_OP_lit6 = 0x36; /* Literal 6. */
	public final static int DW_OP_lit7 = 0x37; /* Literal 7. */
	public final static int DW_OP_lit8 = 0x38; /* Literal 8. */
	public final static int DW_OP_lit9 = 0x39; /* Literal 9. */
	public final static int DW_OP_lit10 = 0x3a; /* Literal 10. */
	public final static int DW_OP_lit11 = 0x3b; /* Literal 11. */
	public final static int DW_OP_lit12 = 0x3c; /* Literal 12. */
	public final static int DW_OP_lit13 = 0x3d; /* Literal 13. */
	public final static int DW_OP_lit14 = 0x3e; /* Literal 14. */
	public final static int DW_OP_lit15 = 0x3f; /* Literal 15. */
	public final static int DW_OP_lit16 = 0x40; /* Literal 16. */
	public final static int DW_OP_lit17 = 0x41; /* Literal 17. */
	public final static int DW_OP_lit18 = 0x42; /* Literal 18. */
	public final static int DW_OP_lit19 = 0x43; /* Literal 19. */
	public final static int DW_OP_lit20 = 0x44; /* Literal 20. */
	public final static int DW_OP_lit21 = 0x45; /* Literal 21. */
	public final static int DW_OP_lit22 = 0x46; /* Literal 22. */
	public final static int DW_OP_lit23 = 0x47; /* Literal 23. */
	public final static int DW_OP_lit24 = 0x48; /* Literal 24. */
	public final static int DW_OP_lit25 = 0x49; /* Literal 25. */
	public final static int DW_OP_lit26 = 0x4a; /* Literal 26. */
	public final static int DW_OP_lit27 = 0x4b; /* Literal 27. */
	public final static int DW_OP_lit28 = 0x4c; /* Literal 28. */
	public final static int DW_OP_lit29 = 0x4d; /* Literal 29. */
	public final static int DW_OP_lit30 = 0x4e; /* Literal 30. */
	public final static int DW_OP_lit31 = 0x4f; /* Literal 31. */
	public final static int DW_OP_reg0 = 0x50; /* Register 0. */
	public final static int DW_OP_reg1 = 0x51; /* Register 1. */
	public final static int DW_OP_reg2 = 0x52; /* Register 2. */
	public final static int DW_OP_reg3 = 0x53; /* Register 3. */
	public final static int DW_OP_reg4 = 0x54; /* Register 4. */
	public final static int DW_OP_reg5 = 0x55; /* Register 5. */
	public final static int DW_OP_reg6 = 0x56; /* Register 6. */
	public final static int DW_OP_reg7 = 0x57; /* Register 7. */
	public final static int DW_OP_reg8 = 0x58; /* Register 8. */
	public final static int DW_OP_reg9 = 0x59; /* Register 9. */
	public final static int DW_OP_reg10 = 0x5a; /* Register 10. */
	public final static int DW_OP_reg11 = 0x5b; /* Register 11. */
	public final static int DW_OP_reg12 = 0x5c; /* Register 12. */
	public final static int DW_OP_reg13 = 0x5d; /* Register 13. */
	public final static int DW_OP_reg14 = 0x5e; /* Register 14. */
	public final static int DW_OP_reg15 = 0x5f; /* Register 15. */
	public final static int DW_OP_reg16 = 0x60; /* Register 16. */
	public final static int DW_OP_reg17 = 0x61; /* Register 17. */
	public final static int DW_OP_reg18 = 0x62; /* Register 18. */
	public final static int DW_OP_reg19 = 0x63; /* Register 19. */
	public final static int DW_OP_reg20 = 0x64; /* Register 20. */
	public final static int DW_OP_reg21 = 0x65; /* Register 21. */
	public final static int DW_OP_reg22 = 0x66; /* Register 22. */
	public final static int DW_OP_reg23 = 0x67; /* Register 24. */
	public final static int DW_OP_reg24 = 0x68; /* Register 24. */
	public final static int DW_OP_reg25 = 0x69; /* Register 25. */
	public final static int DW_OP_reg26 = 0x6a; /* Register 26. */
	public final static int DW_OP_reg27 = 0x6b; /* Register 27. */
	public final static int DW_OP_reg28 = 0x6c; /* Register 28. */
	public final static int DW_OP_reg29 = 0x6d; /* Register 29. */
	public final static int DW_OP_reg30 = 0x6e; /* Register 30. */
	public final static int DW_OP_reg31 = 0x6f; /* Register 31. */
	public final static int DW_OP_breg0 = 0x70; /* Base register 0. */
	public final static int DW_OP_breg1 = 0x71; /* Base register 1. */
	public final static int DW_OP_breg2 = 0x72; /* Base register 2. */
	public final static int DW_OP_breg3 = 0x73; /* Base register 3. */
	public final static int DW_OP_breg4 = 0x74; /* Base register 4. */
	public final static int DW_OP_breg5 = 0x75; /* Base register 5. */
	public final static int DW_OP_breg6 = 0x76; /* Base register 6. */
	public final static int DW_OP_breg7 = 0x77; /* Base register 7. */
	public final static int DW_OP_breg8 = 0x78; /* Base register 8. */
	public final static int DW_OP_breg9 = 0x79; /* Base register 9. */
	public final static int DW_OP_breg10 = 0x7a; /* Base register 10. */
	public final static int DW_OP_breg11 = 0x7b; /* Base register 11. */
	public final static int DW_OP_breg12 = 0x7c; /* Base register 12. */
	public final static int DW_OP_breg13 = 0x7d; /* Base register 13. */
	public final static int DW_OP_breg14 = 0x7e; /* Base register 14. */
	public final static int DW_OP_breg15 = 0x7f; /* Base register 15. */
	public final static int DW_OP_breg16 = 0x80; /* Base register 16. */
	public final static int DW_OP_breg17 = 0x81; /* Base register 17. */
	public final static int DW_OP_breg18 = 0x82; /* Base register 18. */
	public final static int DW_OP_breg19 = 0x83; /* Base register 19. */
	public final static int DW_OP_breg20 = 0x84; /* Base register 20. */
	public final static int DW_OP_breg21 = 0x85; /* Base register 21. */
	public final static int DW_OP_breg22 = 0x86; /* Base register 22. */
	public final static int DW_OP_breg23 = 0x87; /* Base register 23. */
	public final static int DW_OP_breg24 = 0x88; /* Base register 24. */
	public final static int DW_OP_breg25 = 0x89; /* Base register 25. */
	public final static int DW_OP_breg26 = 0x8a; /* Base register 26. */
	public final static int DW_OP_breg27 = 0x8b; /* Base register 27. */
	public final static int DW_OP_breg28 = 0x8c; /* Base register 28. */
	public final static int DW_OP_breg29 = 0x8d; /* Base register 29. */
	public final static int DW_OP_breg30 = 0x8e; /* Base register 30. */
	public final static int DW_OP_breg31 = 0x8f; /* Base register 31. */
	public final static int DW_OP_regx = 0x90; /* Unsigned LEB128 register. */
	public final static int DW_OP_fbreg = 0x91; /* Signed LEB128 register. */
	public final static int DW_OP_bregx = 0x92; /* ULEB128 register followed by SLEB128 off. */
	public final static int DW_OP_piece = 0x93; /* ULEB128 size of piece addressed. */
	public final static int DW_OP_deref_size = 0x94; /* 1-byte size of data retrieved. */
	public final static int DW_OP_xderef_size = 0x95; /* 1-byte size of data retrieved. */
	public final static int DW_OP_nop = 0x96;
	public final static int DW_OP_push_object_address = 0x97;
	public final static int DW_OP_call2 = 0x98;
	public final static int DW_OP_call4 = 0x99;
	public final static int DW_OP_call_ref = 0x9a;

	public final static int DW_OP_lo_user = 0xe0; /* Implementation-defined range start. */
	public final static int DW_OP_hi_user = 0xff; /* Implementation-defined range end. */

	/* DWARF base type encodings. */
	public final static int DW_ATE_void = 0x0;
	public final static int DW_ATE_address = 0x1;
	public final static int DW_ATE_boolean = 0x2;
	public final static int DW_ATE_complex_float = 0x3;
	public final static int DW_ATE_float = 0x4;
	public final static int DW_ATE_signed = 0x5;
	public final static int DW_ATE_signed_char = 0x6;
	public final static int DW_ATE_unsigned = 0x7;
	public final static int DW_ATE_unsigned_char = 0x8;

	public final static int DW_ATE_lo_user = 0x80;
	public final static int DW_ATE_hi_user = 0xff;

	/* DWARF accessibility encodings. */
	public final static int DW_ACCESS_public = 1;
	public final static int DW_ACCESS_protected = 2;
	public final static int DW_ACCESS_private = 3;

	/* DWARF visibility encodings. */
	public final static int DW_VIS_local = 1;
	public final static int DW_VIS_exported = 2;
	public final static int DW_VIS_qualified = 3;

	/* DWARF virtuality encodings. */
	public final static int DW_VIRTUALITY_none = 0;
	public final static int DW_VIRTUALITY_virtual = 1;
	public final static int DW_VIRTUALITY_pure_virtual = 2;

	/* DWARF language encodings. */
	public final static int DW_LANG_C89 = 0x0001;
	public final static int DW_LANG_C = 0x0002;
	public final static int DW_LANG_Ada83 = 0x0003;
	public final static int DW_LANG_C_plus_plus = 0x0004;
	public final static int DW_LANG_Cobol74 = 0x0005;
	public final static int DW_LANG_Cobol85 = 0x0006;
	public final static int DW_LANG_Fortran77 = 0x0007;
	public final static int DW_LANG_Fortran90 = 0x0008;
	public final static int DW_LANG_Pascal83 = 0x0009;
	public final static int DW_LANG_Modula2 = 0x000a;
	public final static int DW_LANG_Java = 0x000b;
	public final static int DW_LANG_C99 = 0x000c;
	public final static int DW_LANG_Ada95 = 0x000d;
	public final static int DW_LANG_Fortran95 = 0x000e;
	public final static int DW_LANG_PL1 = 0x000f;
	public final static int DW_LANG_lo_user = 0x8000;
	public final static int DW_LANG_Mips_Assembler = 0x8001;
	public final static int DW_LANG_hi_user = 0xffff;

	/* DWARF identifier case encodings. */
	public final static int DW_ID_case_sensitive = 0;
	public final static int DW_ID_up_case = 1;
	public final static int DW_ID_down_case = 2;
	public final static int DW_ID_case_insensitive = 3;

	/* DWARF calling conventions encodings. */
	public final static int DW_CC_normal = 0x1;
	public final static int DW_CC_program = 0x2;
	public final static int DW_CC_nocall = 0x3;
	public final static int DW_CC_lo_user = 0x40;
	public final static int DW_CC_hi_user = 0xff;

	/* DWARF inline encodings. */
	public final static int DW_INL_not_inlined = 0;
	public final static int DW_INL_inlined = 1;
	public final static int DW_INL_declared_not_inlined = 2;
	public final static int DW_INL_declared_inlined = 3;

	/* DWARF ordering encodings. */
	public final static int DW_ORD_row_major = 0;
	public final static int DW_ORD_col_major = 1;

	/* DWARF discriminant descriptor encodings. */
	public final static int DW_DSC_label = 0;
	public final static int DW_DSC_range = 1;

	/* DWARF standard opcode encodings. */
	public final static int DW_LNS_copy = 1;
	public final static int DW_LNS_advance_pc = 2;
	public final static int DW_LNS_advance_line = 3;
	public final static int DW_LNS_set_file = 4;
	public final static int DW_LNS_set_column = 5;
	public final static int DW_LNS_negate_stmt = 6;
	public final static int DW_LNS_set_basic_block = 7;
	public final static int DW_LNS_const_add_pc = 8;
	public final static int DW_LNS_fixed_advance_pc = 9;
	public final static int DW_LNS_set_prologue_end = 10;
	public final static int DW_LNS_set_epilog_begin = 11;

	/* DWARF extended opcide encodings. */
	public final static int DW_LNE_end_sequence = 1;
	public final static int DW_LNE_set_address = 2;
	public final static int DW_LNE_define_file = 3;

	/* DWARF macinfo type encodings. */
	public final static int DW_MACINFO_define = 1;
	public final static int DW_MACINFO_undef = 2;
	public final static int DW_MACINFO_start_file = 3;
	public final static int DW_MACINFO_end_file = 4;
	public final static int DW_MACINFO_vendor_ext = 255;

	/* DWARF call frame instruction encodings. */
	public final static int DW_CFA_advance_loc = 0x40;
	public final static int DW_CFA_offset = 0x80;
	public final static int DW_CFA_restore = 0xc0;
	public final static int DW_CFA_extended = 0;

	public final static int DW_CFA_nop = 0x00;
	public final static int DW_CFA_set_loc = 0x01;
	public final static int DW_CFA_advance_loc1 = 0x02;
	public final static int DW_CFA_advance_loc2 = 0x03;
	public final static int DW_CFA_advance_loc4 = 0x04;
	public final static int DW_CFA_offset_extended = 0x05;
	public final static int DW_CFA_restore_extended = 0x06;
	public final static int DW_CFA_undefined = 0x07;
	public final static int DW_CFA_same_value = 0x08;
	public final static int DW_CFA_register = 0x09;
	public final static int DW_CFA_remember_state = 0x0a;
	public final static int DW_CFA_restore_state = 0x0b;
	public final static int DW_CFA_def_cfa = 0x0c;
	public final static int DW_CFA_def_cfa_register = 0x0d;
	public final static int DW_CFA_def_cfa_offset = 0x0e;
	public final static int DW_CFA_low_user = 0x1c;
	public final static int DW_CFA_MIPS_advance_loc8 = 0x1d;
	public final static int DW_CFA_GNU_window_save = 0x2d;
	public final static int DW_CFA_GNU_args_size = 0x2e;
	public final static int DW_CFA_high_user = 0x3f;

}
