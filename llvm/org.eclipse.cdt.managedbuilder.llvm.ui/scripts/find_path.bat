@echo OFF

for %%D in (c,d,e) do (    
    If exist %%D:\ (        
        for /f "delims=" %%f in ('dir /b /s %%D:\%1 2^> NUL') do (
            @echo %1 found: %%~dpf
        )
    )
)