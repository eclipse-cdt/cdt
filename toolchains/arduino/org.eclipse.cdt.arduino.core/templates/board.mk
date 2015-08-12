OUTPUT_DIR = ${boardId}

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
	$(OUTPUT_DIR)/project/${cpp?groups[1]}.o \
</#if>
</#list>

PLATFORM_OBJS = \
<#list platform_srcs as file>
<#assign cpp = file?matches("${platform_path}/(.*)\\.cpp")>
<#if cpp>
	$(OUTPUT_DIR)/platform/${cpp?groups[1]}.o \
</#if>
<#assign c = file?matches("${platform_path}/(.*)\\.c")>
<#if c>
	$(OUTPUT_DIR)/platform/${c?groups[1]}.o \
</#if>
</#list>

all: $(OUTPUT_DIR)/${project_name}.hex $(OUTPUT_DIR)/${project_name}.eep

$(OUTPUT_DIR)/${project_name}.hex: $(OUTPUT_DIR)/${project_name}.elf
	${recipe_objcopy_hex_pattern}

$(OUTPUT_DIR)/${project_name}.eep: $(OUTPUT_DIR)/${project_name}.elf
	${recipe_objcopy_eep_pattern}

$(OUTPUT_DIR)/${project_name}.elf: $(PROJECT_OBJS) $(OUTPUT_DIR)/libc.a
	${recipe_c_combine_pattern}

$(OUTPUT_DIR)/libc.a:	$(PLATFORM_OBJS)

clean:
	$(RMDIR) $(OUTPUT_DIR)

<#list project_srcs as file>
<#assign cpp = file?matches("(.*)\\.cpp")>
<#if cpp>
$(OUTPUT_DIR)/project/${cpp?groups[1]}.o: ../${file}
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}

</#if>
</#list>

<#list platform_srcs as file>
<#assign cpp = file?matches("${platform_path}/(.*)\\.cpp")>
<#if cpp>
$(OUTPUT_DIR)/platform/${cpp?groups[1]}.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_cpp_o_pattern}
	${recipe_ar_pattern}

</#if>
<#assign c = file?matches("${platform_path}/(.*)\\.c")>
<#if c>
$(OUTPUT_DIR)/platform/${c?groups[1]}.o: ${file}
	@$(call mymkdir,$(dir $@))
	${recipe_c_o_pattern}
	${recipe_ar_pattern}

</#if>
</#list>
