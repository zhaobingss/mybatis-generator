#!/bin/bash

cd $(dirname $0)


JARS=$(ls *.jar)

CLASSPATH=.
for jar in $JARS;do
  CLASSPATH=$CLASSPATH:$jar
done

echo $CLASSPATH

java -classpath $CLASSPATH org.mybatis.generator.api.ShellRunner -configfile generatorConfig.xml