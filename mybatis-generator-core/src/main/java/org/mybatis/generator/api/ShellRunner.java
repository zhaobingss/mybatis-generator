/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.api;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.logging.LogFactory;

/**
 * This class allows the code generator to be run from the command line.
 * @author Jeff Butler
 */
public class ShellRunner {

    /** 配置文件路径 */
    private static final String CONFIG_FILE = "-configfile";
    /** 是否覆盖java文件 */
    private static final String OVERWRITE = "-overwrite";
    /** 指定contextIDs*/
    private static final String CONTEXT_IDS = "-contextids";
    /** 指定数据库表名*/
    private static final String TABLES = "-tables";
    /** 是否启用冗余回调*/
    private static final String VERBOSE = "-verbose";
    /** 是否强制输出java日志*/
    private static final String FORCE_JAVA_LOGGING = "-forceJavaLogging";
    /** 帮助命令？*/
    private static final String HELP_1 = "-?";
    /** 帮助命令h*/
    private static final String HELP_2 = "-h";

    public static void main(String[] args) {

        if (args.length == 0) {
            usage();
            System.exit(0);
            return;
        }

        Map<String, String> arguments = parseCommandLine(args);
        if (arguments.containsKey(HELP_1) || arguments.containsKey(HELP_2)) {
            usage();
            System.exit(0);
            return;
        }

        if (!arguments.containsKey(CONFIG_FILE)) {
            writeLine(getString("RuntimeError.0"));
            return;
        }

        String configfile = arguments.get(CONFIG_FILE);
        File configurationFile = new File(configfile);
        if (!configurationFile.exists()) {
            writeLine(getString("RuntimeError.1", configfile));
            return;
        }

        Set<String> fullyqualifiedTables = new HashSet<>();
        if (arguments.containsKey(TABLES)) {
            StringTokenizer st = new StringTokenizer(arguments.get(TABLES), ",");
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    fullyqualifiedTables.add(s);
                }
            }
        }

        Set<String> contexts = new HashSet<>();
        if (arguments.containsKey(CONTEXT_IDS)) {
            StringTokenizer st = new StringTokenizer(arguments.get(CONTEXT_IDS), ",");
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (s.length() > 0) {
                    contexts.add(s);
                }
            }
        }

        // 生成代码过程中的警告信息
        List<String> warnings = new ArrayList<>();
        try {
            // 解析配置文件
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(configurationFile);

            // 全局配置添加到context
            List<Context> list = config.getContexts();
            if (list != null && !list.isEmpty()){
                for (Context context : list){
                    Set<Map.Entry<Object, Object>> set = Configuration.properties.entrySet();
                    if (!set.isEmpty()){
                        Iterator<Map.Entry<Object, Object>> iter = set.iterator();
                        while (iter.hasNext()){
                            Map.Entry<Object, Object> entry = iter.next();
                            context.addProperty((String) entry.getKey(), (String) entry.getValue());
                        }
                    }
                }
            }

            // 判断是否合并或者是覆盖java文件
            boolean isJavaOverwrite = "true".equals(Configuration.properties.getProperty("isJavaOverwrite"));
            boolean isJavaMerge = "true".equals(Configuration.properties.getProperty("isJavaMerge"));
            DefaultShellCallback shellCallback = new DefaultShellCallback(isJavaOverwrite, isJavaMerge);

            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
            ProgressCallback progressCallback = arguments.containsKey(VERBOSE) ? new VerboseProgressCallback() : null;
            myBatisGenerator.generate(progressCallback, contexts, fullyqualifiedTables);

        } catch (XMLParserException e) {
            writeLine(getString("Progress.3"));
            writeLine();
            for (String error : e.getErrors()) {
                writeLine(error);
            }
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (InvalidConfigurationException e) {
            writeLine(getString("Progress.16"));
            for (String error : e.getErrors()) {
                writeLine(error);
            }
            return;
        } catch (InterruptedException e) {

        }

        for (String warning : warnings) {
            writeLine(warning);
        }

        if (warnings.size() == 0) {
            writeLine(getString("Progress.4"));
        } else {
            writeLine();
            writeLine(getString("Progress.5"));
        }
    }

    private static void usage() {
        String lines = getString("Usage.Lines");
        int intLines = Integer.parseInt(lines);
        for (int i = 0; i < intLines; i++) {
            String key = "Usage." + i;
            writeLine(getString(key));
        }
    }

    private static void writeLine(String message) {
        System.out.println(message);
    }

    private static void writeLine() {
        System.out.println();
    }

    private static Map<String, String> parseCommandLine(String[] args) {
        List<String> errors = new ArrayList<String>();
        Map<String, String> arguments = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            if (CONFIG_FILE.equalsIgnoreCase(args[i])) {
                if ((i + 1) < args.length) {
                    arguments.put(CONFIG_FILE, args[i + 1]);
                } else {
                    errors.add(getString("RuntimeError.19", CONFIG_FILE));
                }
                i++;
            } else if (OVERWRITE.equalsIgnoreCase(args[i])) {
                arguments.put(OVERWRITE, "Y");
            } else if (VERBOSE.equalsIgnoreCase(args[i])) {
                arguments.put(VERBOSE, "Y");
            } else if (HELP_1.equalsIgnoreCase(args[i])) {
                arguments.put(HELP_1, "Y");
            } else if (HELP_2.equalsIgnoreCase(args[i])) {
                arguments.put(HELP_1, "Y");
            } else if (FORCE_JAVA_LOGGING.equalsIgnoreCase(args[i])) {
                LogFactory.forceJavaLogging();
            } else if (CONTEXT_IDS.equalsIgnoreCase(args[i])) {
                if ((i + 1) < args.length) {
                    arguments.put(CONTEXT_IDS, args[i + 1]);
                } else {
                    errors.add(getString("RuntimeError.19", CONTEXT_IDS));
                }
                i++;
            } else if (TABLES.equalsIgnoreCase(args[i])) {
                if ((i + 1) < args.length) {
                    arguments.put(TABLES, args[i + 1]);
                } else {
                    errors.add(getString("RuntimeError.19", TABLES));
                }
                i++;
            } else {
                errors.add(getString("RuntimeError.20", args[i]));
            }
        }

        if (!errors.isEmpty()) {
            for (String error : errors) {
                writeLine(error);
            }

            System.exit(-1);
        }

        return arguments;
    }
}
