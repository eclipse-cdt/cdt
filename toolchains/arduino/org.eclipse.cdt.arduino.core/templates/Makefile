ifeq ($(OS),Windows_NT)
SHELL = cmd
RMDIR = rmdir /s /q
RM = del /q
mymkdir = if not exist "$1" mkdir "$1"
else
RMDIR = rm -fr
RM = rm -f
mymkdir = mkdir -p $1
endif

PROJECT_OBJS = \
<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
	project/${cpp?groups[1]}.cpp.o \
</#if>
</#list>

PLATFORM_CORE_OBJS = \
<#list platform_core_srcs as file>
<#assign cpp = file?matches("${platform_core_path}/(.*)\\.cpp")>
<#if cpp>
	core/${cpp?groups[1]}.cpp.o \
</#if>
<#assign c = file?matches("${platform_core_path}/(.*)\\.c")>
<#if c>
	core/${c?groups[1]}.c.o \
</#if>
<#assign S = file?matches("${platform_core_path}/(.*)\\.S")>
<#if S>
	core/${S?groups[1]}.S.o \
</#if>
</#list>

PLATFORM_VARIANT_OBJS = \
<#list platform_variant_srcs as file>
<#assign cpp = file?matches("${platform_variant_path}/(.*)\\.cpp")>
<#if cpp>
	variant/${cpp?groups[1]}.cpp.o \
</#if>
<#assign c = file?matches("${platform_variant_path}/(.*)\\.c")>
<#if c>
	variant/${c?groups[1]}.c.o \
</#if>
<#assign S = file?matches("${platform_variant_path}/(.*)\\.S")>
<#if S>
	variant/${S?groups[1]}.S.o \
</#if>
</#list>

LIBRARIES_OBJS = \
<#list libraries_srcs as file>
<#assign cpp = file?matches("${libraries_path}/(.*?)/(.*)\\.cpp")>
<#if !cpp>
<#assign cpp = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.cpp")>
</#if>
<#if cpp>
	libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.o \
</#if>
<#assign c = file?matches("${libraries_path}/(.*?)/(.*)\\.c")>
<#if !c>
<#assign c = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.c")>
</#if>
<#if c>
	libraries/${c?groups[1]}/${c?groups[2]}.c.o \
</#if>
<#assign S = file?matches("${libraries_path}/(.*?)/(.*)\\.S")>
<#if !S>
<#assign S = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.S")>
</#if>
<#if S>
	libraries/${S?groups[1]}/${S?groups[2]}.S.o \
</#if>
</#list>

TARGETS = \
<#if recipe_objcopy_hex_pattern??>
	${project_name}.hex \
</#if>
<#if recipe_objcopy_eep_pattern??>
	${project_name}.eep \
</#if>
<#if recipe_objcopy_bin_pattern??>
	${project_name}.bin \
</#if>

all: $(TARGETS)

<#if recipe_objcopy_hex_pattern??>
${project_name}.hex: ${project_name}.elf
	${recipe_objcopy_hex_pattern}

</#if>
<#if recipe_objcopy_eep_pattern??>
${project_name}.eep: ${project_name}.elf
	${recipe_objcopy_eep_pattern}

</#if>
<#if recipe_objcopy_bin_pattern??>
${project_name}.bin: ${project_name}.elf
	${recipe_objcopy_bin_pattern}

</#if>
${project_name}.elf: $(PROJECT_OBJS) $(LIBRARIES_OBJS) core.a
	${recipe_c_combine_pattern}

core.a:	$(PLATFORM_CORE_OBJS) $(PLATFORM_VARIANT_OBJS)

clean:
	-$(RMDIR) project core variant libraries
	-$(RM) *.hex *.eep *.bin *.elf *.a *.ar *.d

size:
	${recipe_size_pattern}

<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
project/${cpp?groups[1]}.cpp.o: ../../${file} project/${cpp?groups[1]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}

project/${cpp?groups[1]}.cpp.d: ;

-include project/${cpp?groups[1]}.cpp.d 

</#if>
</#list>

<#list platform_core_srcs as file>
<#assign cpp = file?matches("${platform_core_path}/(.*)\\.cpp")>
<#if cpp>
core/${cpp?groups[1]}.cpp.o: ${file} core/${cpp?groups[1]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern_plat}
	${recipe_ar_pattern}

core/${cpp?groups[1]}.cpp.d: ;

-include core/${cpp?groups[1]}.cpp.d

</#if>
<#assign c = file?matches("${platform_core_path}/(.*)\\.c")>
<#if c>
core/${c?groups[1]}.c.o: ${file} core/${c?groups[1]}.c.d
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern_plat}
	${recipe_ar_pattern}
	
core/${c?groups[1]}.c.d: ;

-include core/${c?groups[1]}.c.d

</#if>
<#assign S = file?matches("${platform_core_path}/(.*)\\.S")>
<#if S>
core/${S?groups[1]}.S.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_S_o_pattern_plat}
	${recipe_ar_pattern}

</#if>
</#list>

<#list platform_variant_srcs as file>
<#assign cpp = file?matches("${platform_variant_path}/(.*)\\.cpp")>
<#if cpp>
variant/${cpp?groups[1]}.cpp.o: ${file} variant/${cpp?groups[1]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern_plat}
	${recipe_ar_pattern}

variant/${cpp?groups[1]}.cpp.d: ;

-include variant/${cpp?groups[1]}.cpp.d

</#if>
<#assign c = file?matches("${platform_variant_path}/(.*)\\.c")>
<#if c>
variant/${c?groups[1]}.c.o: ${file} variant/${c?groups[1]}.c.d
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern_plat}
	${recipe_ar_pattern}
	
variant/${c?groups[1]}.c.d: ;

-include variant/${c?groups[1]}.c.d

</#if>
<#assign S = file?matches("${platform_variant_path}/(.*)\\.S")>
<#if S>
variant/${S?groups[1]}.S.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_S_o_pattern_plat}
	${recipe_ar_pattern}

</#if>
</#list>

<#list libraries_srcs as file>
<#assign cpp = file?matches("${libraries_path}/(.*?)/(.*)\\.cpp")>
<#if !cpp>
<#assign cpp = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.cpp")>
</#if>
<#if cpp>
libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.o: ${file} libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}

libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.d: ;

-include libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.d

</#if>
<#assign c = file?matches("${libraries_path}/(.*?)/(.*)\\.c")>
<#if !c>
<#assign c = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.c")>
</#if>
<#if c>
libraries/${c?groups[1]}/${c?groups[2]}.c.o: ${file} libraries/${c?groups[1]}/${c?groups[2]}.c.d
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern}

libraries/${c?groups[1]}/${c?groups[2]}.c.d: ;

-include libraries/${c?groups[1]}/${c?groups[2]}.c.d

</#if>
<#assign S = file?matches("${libraries_path}/(.*?)/(.*)\\.S")>
<#if !S>
<#assign S = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.S")>
</#if>
<#if S>
libraries/${S?groups[1]}/${S?groups[2]}.S.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_S_o_pattern}

</#if>
</#list>