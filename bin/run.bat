@echo off & setlocal enabledelayedexpansion

set LIB_JARS=.
for %%i in (*) do set LIB_JARS=!LIB_JARS!;%%i

rem java -Xms64m -Xmx1024m -XX:MaxPermSize=64M -classpath %LIB_JARS% org.mybatis.generator.api.ShellRunner -configfile generatorConfig.xml -overwrite
java -Xms64m -Xmx1024m -XX:MaxPermSize=64M -classpath %LIB_JARS% org.mybatis.generator.api.ShellRunner -configfile generatorConfig.xml

pause
