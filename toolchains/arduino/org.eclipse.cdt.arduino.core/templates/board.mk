ifeq ($(OS),Windows_NT)
SHELL = $(ComSpec)
RMDIR = rmdir /s /q
mymkdir = if not exist "$1" mkdir "$1"
else
RMDIR = rm -fr
mymkdir = mkdir -p $1
endif

PROJECT_OBJS = \
<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
	${build_path}/project/${cpp?groups[1]}.cpp.o \
</#if>
</#list>

PLATFORM_OBJS = \
<#list platform_srcs as file>
<#assign cpp = file?matches("${platform_path}/(.*)\\.cpp")>
<#if cpp>
	${build_path}/platform/${cpp?groups[1]}.cpp.o \
</#if>
<#assign c = file?matches("${platform_path}/(.*)\\.c")>
<#if c>
	${build_path}/platform/${c?groups[1]}.c.o \
</#if>
<#assign S = file?matches("${platform_path}/(.*)\\.S")>
<#if S>
	${build_path}/platform/${S?groups[1]}.S.o \
</#if>
</#list>

LIBRARIES_OBJS = \
<#list libraries_srcs as file>
<#assign cpp = file?matches("${libraries_path}/(.*?)/.*?/(.*)\\.cpp")>
<#if !cpp>
<#assign cpp = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.cpp")>
</#if>
<#if cpp>
	${build_path}/libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.o \
</#if>
<#assign c = file?matches("${libraries_path}/(.*?)/.*?/(.*)\\.c")>
<#if !c>
<#assign c = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.c")>
</#if>
<#if c>
	${build_path}/libraries/${c?groups[1]}/${c?groups[2]}.c.o \
</#if>
</#list>

TARGETS = \
<#if recipe_objcopy_hex_pattern??>
	${build_path}/${project_name}.hex \
</#if>
<#if recipe_objcopy_epp_pattern??>
	${build_path}/${project_name}.eep \
</#if>
<#if recipe_objcopy_bin_pattern??>
	${build_path}/${project_name}.bin \
</#if>

all: $(TARGETS)

<#if recipe_objcopy_hex_pattern??>
${build_path}/${project_name}.hex: ${build_path}/${project_name}.elf
	${recipe_objcopy_hex_pattern}

</#if>
<#if recipe_objcopy_epp_pattern??>
${build_path}/${project_name}.eep: ${build_path}/${project_name}.elf
	${recipe_objcopy_eep_pattern}

</#if>
<#if recipe_objcopy_bin_pattern??>
${build_path}/${project_name}.bin: ${build_path}/${project_name}.elf
	${recipe_objcopy_bin_pattern}

</#if>
${build_path}/${project_name}.elf: $(PROJECT_OBJS) $(LIBRARIES_OBJS) ${build_path}/core.a
	${recipe_c_combine_pattern}

${build_path}/core.a:	$(PLATFORM_OBJS)

clean:
	$(RMDIR) ${build_path}

size:
	${recipe_size_pattern}

<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
${build_path}/project/${cpp?groups[1]}.cpp.o: ../${file} ${build_path}/project/${cpp?groups[1]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}

${build_path}/project/${cpp?groups[1]}.cpp.d: ;

-include ${build_path}/project/${cpp?groups[1]}.cpp.d 

</#if>
</#list>

<#list platform_srcs as file>
<#assign cpp = file?matches("${platform_path}/(.*)\\.cpp")>
<#if cpp>
${build_path}/platform/${cpp?groups[1]}.cpp.o: ${file} ${build_path}/platform/${cpp?groups[1]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}
	${recipe_ar_pattern}

${build_path}/platform/${cpp?groups[1]}.cpp.d: ;

-include ${build_path}/platform/${cpp?groups[1]}.cpp.d

</#if>
<#assign c = file?matches("${platform_path}/(.*)\\.c")>
<#if c>
${build_path}/platform/${c?groups[1]}.c.o: ${file} ${build_path}/platform/${c?groups[1]}.c.d
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern}
	${recipe_ar_pattern}
	
${build_path}/platform/${c?groups[1]}.c.d: ;

-include ${build_path}/platform/${c?groups[1]}.c.d

</#if>
<#assign S = file?matches("${platform_path}/(.*)\\.S")>
<#if S>
${build_path}/platform/${S?groups[1]}.S.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_S_o_pattern}
	${recipe_ar_pattern}

</#if>
</#list>

<#list libraries_srcs as file>
<#assign cpp = file?matches("${libraries_path}/(.*?)/.*?/(.*)\\.cpp")>
<#if !cpp>
<#assign cpp = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.cpp")>
</#if>
<#if cpp>
${build_path}/libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.o: ${file} ${build_path}/libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.d
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}

${build_path}/libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.d: ;

-include ${build_path}/libraries/${cpp?groups[1]}/${cpp?groups[2]}.cpp.d

</#if>
<#assign c = file?matches("${libraries_path}/(.*?)/.*?/(.*)\\.c")>
<#if !c>
<#assign c = file?matches("${platform_path}/libraries/(.*?)/(.*)\\.c")>
</#if>
<#if c>
${build_path}/libraries/${c?groups[1]}/${c?groups[2]}.c.o: ${file} ${build_path}/libraries/${c?groups[1]}/${c?groups[2]}.c.d
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern}

${build_path}/libraries/${c?groups[1]}/${c?groups[2]}.c.d: ;

-include ${build_path}/libraries/${c?groups[1]}/${c?groups[2]}.c.d

</#if>
</#list>