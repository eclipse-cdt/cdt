ifeq ($(OS),Windows_NT)
RMDIR = rmdir /s /q
mymkdir = if not exist "$(call fixpath,$1)" mkdir $(call fixpath,$1)
else
RMDIR = rm -fr
mymkdir = mkdir -p $1
endif

PROJECT_OBJS = \
<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
	${build_path}/project/${cpp?groups[1]}.o \
</#if>
</#list>

PLATFORM_OBJS = \
<#list platform_srcs as file>
<#assign cpp = file?matches("${platform_path}/(.*)\\.cpp")>
<#if cpp>
	${build_path}/platform/${cpp?groups[1]}.o \
</#if>
<#assign c = file?matches("${platform_path}/(.*)\\.c")>
<#if c>
	${build_path}/platform/${c?groups[1]}.o \
</#if>
</#list>

all: ${build_path}/${project_name}.hex ${build_path}/${project_name}.eep

${build_path}/${project_name}.hex: ${build_path}/${project_name}.elf
	${recipe_objcopy_hex_pattern}

${build_path}/${project_name}.eep: ${build_path}/${project_name}.elf
	${recipe_objcopy_eep_pattern}

${build_path}/${project_name}.elf: $(PROJECT_OBJS) ${build_path}/libc.a
	${recipe_c_combine_pattern}

${build_path}/libc.a:	$(PLATFORM_OBJS)

clean:
	$(RMDIR) ${build_path}

size:
	${recipe_size_pattern}

<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
${build_path}/project/${cpp?groups[1]}.o: ../${file}
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}

</#if>
</#list>

<#list platform_srcs as file>
<#assign cpp = file?matches("${platform_path}/(.*)\\.cpp")>
<#if cpp>
${build_path}/platform/${cpp?groups[1]}.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}
	${recipe_ar_pattern}

</#if>
<#assign c = file?matches("${platform_path}/(.*)\\.c")>
<#if c>
${build_path}/platform/${c?groups[1]}.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern}
	${recipe_ar_pattern}

</#if>
</#list>
