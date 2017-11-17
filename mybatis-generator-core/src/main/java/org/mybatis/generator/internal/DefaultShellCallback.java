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
package org.mybatis.generator.internal;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.exception.ShellException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * The Class DefaultShellCallback.
 *
 * @author Jeff Butler
 */
public class DefaultShellCallback implements ShellCallback {

    /** The overwrite. */
    private boolean overwrite;
    /** merge java file */
    private boolean isJavaMerge;

    /**
     * Instantiates a new default shell callback.
     *
     * @param overwrite
     *            the overwrite
     */
    public DefaultShellCallback(boolean overwrite, boolean isJavaMerge) {
        super();
        this.overwrite = overwrite;
        this.isJavaMerge = isJavaMerge;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#getDirectory(java.lang.String, java.lang.String)
     */
    @Override
    public File getDirectory(String targetProject, String targetPackage)
            throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a sub directory, but in package
        // format (with dots instead of slashes). The sub directory will be
        // created
        // if it does not already exist

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            throw new ShellException(getString("Warning.9", //$NON-NLS-1$
                    targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", //$NON-NLS-1$
                        directory.getAbsolutePath()));
            }
        }

        return directory;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#refreshProject(java.lang.String)
     */
    @Override
    public void refreshProject(String project) {
        // nothing to do in the default shell callback
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isMergeSupported()
     */
    @Override
    public boolean isMergeSupported() {
        return isJavaMerge;// 注意这个的修改
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isOverwriteEnabled()
     */
    @Override
    public boolean isOverwriteEnabled() {
        return overwrite;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#mergeJavaFile(java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public String mergeJavaFile(String newFileSource,
            File existingFile, String[] javadocTags, String fileEncoding)
            throws ShellException {
        try {
        	String source = getNewJavaFile(newFileSource, existingFile);
        	return source;
		} catch (FileNotFoundException e) {
			System.out.println("merge java file exception:"+e.getMessage());
			throw new UnsupportedOperationException(e);
		}
    }
    
    public String getNewJavaFile(String newFileSource, File existingFile) throws FileNotFoundException {  
        CompilationUnit newCompilationUnit = JavaParser.parse(newFileSource);  
        CompilationUnit existingCompilationUnit = JavaParser.parse(existingFile);  
        return mergerFile(newCompilationUnit,existingCompilationUnit);  
    }  
    
    public String mergerFile(CompilationUnit newCompilationUnit,CompilationUnit oldCompilationUnit){  
    	String lineSeparator = System.getProperty("line.separator");
        
        //截取Class  
        TypeDeclaration<?> newType = newCompilationUnit.getTypes().get(0);
        TypeDeclaration<?> oldType = oldCompilationUnit.getTypes().get(0); 
        
        if (newType.getName().toString().contains("Example")){
        	return newCompilationUnit.toString();
        }
        
        // 设置包名为新的包名
    	StringBuilder sb = new StringBuilder(newCompilationUnit.getPackageDeclaration().get().toString());  
  
        //合并imports
        NodeList<ImportDeclaration> newImports = newCompilationUnit.getImports();  
        NodeList<ImportDeclaration> oldImports = oldCompilationUnit.getImports();  
        NodeList<ImportDeclaration> cmpImports = new NodeList<ImportDeclaration>(newImports);
        if (oldImports.size() > 0){
        	for (ImportDeclaration oldImp : oldImports){
            	boolean isRepeat = false;
            	if (newImports.size() > 0){
            		for (ImportDeclaration newImp : newImports){
                    	if (oldImp.toString().equals(newImp.toString())){
                    		isRepeat = true;
                    	}
                    }
            	}
            	if (!isRepeat){
            		int idx = oldImports.indexOf(oldImp);
            		cmpImports.add(idx, oldImp);
            	}
            }
        }
        if (cmpImports.size() > 0){
        	for (ImportDeclaration imports : cmpImports) {  
                sb.append(imports.toString());  
            } 
        }
        
        sb.append(lineSeparator);
        
        // 设置类名
        String className = newType.toString().substring(0, newType.toString().indexOf("{")+1);  
        sb.append(className);  
        sb.append(lineSeparator);
        
        //合并fields（根据注解）
        List<FieldDeclaration> newFields = newType.getFields();
        List<FieldDeclaration> oldFields = oldType.getFields();  
        List<FieldDeclaration> cmpFields = new ArrayList<FieldDeclaration>(newFields);
        if (oldFields.size() > 0){
        	for (FieldDeclaration oldField : oldFields){
        		if (!oldField.toString().contains(MergeConstants.NEW_ELEMENT_TAG)){
        			int index = oldFields.indexOf(oldField);
            		cmpFields.add(index, oldField);
        		}
            }
        }
        if (cmpFields.size() > 0){
        	for (FieldDeclaration field : cmpFields){
        		String [] strs = field.toString().split(lineSeparator);
        		for (String str : strs){
        			sb.append("\t"+str+lineSeparator); 
        		}
                sb.append(lineSeparator);  
            }
        }
        sb.append(lineSeparator);
  
        //合并methods  
        List<MethodDeclaration> newMethods = newType.getMethods();
        List<MethodDeclaration> oldMethods = oldType.getMethods();  
        List<MethodDeclaration> cmpMethods = new ArrayList<MethodDeclaration>(newMethods);
        if (oldMethods.size() > 0){
        	for (MethodDeclaration oldMethod : oldMethods){
        		if (!oldMethod.toString().contains(MergeConstants.NEW_ELEMENT_TAG)){
        			int index = oldMethods.indexOf(oldMethod);
        			cmpMethods.add(index, oldMethod);
        		}
        	}
        }
        if (cmpMethods.size() > 0){
        	for (MethodDeclaration method : cmpMethods){
        		String [] strs = method.toString().split(lineSeparator);
        		for (String str : strs){
        			sb.append("\t"+str+lineSeparator); 
        		}
                sb.append(lineSeparator);
            }
        }
  
        return sb.append("}").toString();  
    }
    
}
