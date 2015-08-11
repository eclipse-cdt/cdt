OUTPUT_DIR = ${boardId}

ifeq ($(OS),Windows_NT)
RMDIR = rmdir /s /q
mymkdir = if not exist "$(call fixpath,$1)" mkdir $(call fixpath,$1)
else
RMDIR = rm -fr
mymkdir = mkdir -p $1
endif

SOURCES = \
<#list sources as file>
	../src/${file} \
</#list>

all:
	@$(call mymkdir,$(OUTPUT_DIR))
	echo hello from template

clean:
	$(RMDIR) $(OUTPUT_DIR)
