/* 
 * Copyright 2009-2010 junithelper.org. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */
package org.junithelper.core.meta.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.junithelper.core.config.Configulation;
import org.junithelper.core.constant.RegExp;
import org.junithelper.core.filter.TrimFilterUtil;
import org.junithelper.core.meta.ClassMeta;
import org.junithelper.core.meta.ConstructorMeta;
import org.junithelper.core.meta.MethodMeta;

public class ClassMetaExtractor {

	@SuppressWarnings("unused")
	private Configulation config;
	private ConstructorMetaExtractor constructorMetaExtractor;
	private MethodMetaExtractor methodMetaExtractor;
	private ImportedListExtractor importedListExtractor;

	public ClassMetaExtractor(Configulation config) {
		this.config = config;
		constructorMetaExtractor = new ConstructorMetaExtractor(config);
		methodMetaExtractor = new MethodMetaExtractor(config);
		importedListExtractor = new ImportedListExtractor(config);
	}

	public ClassMeta extract(String sourceCodeString) {

		ClassMeta meta = new ClassMeta();

		String modifiedSourceCodeString = TrimFilterUtil
				.doAllFilters(sourceCodeString);

		// -----------------
		// package name
		Matcher matcherGroupingPackageName = RegExp.PatternObject.Pacakge_Group
				.matcher(modifiedSourceCodeString);
		if (matcherGroupingPackageName.find()) {
			meta.packageName = matcherGroupingPackageName.group(1);
		}
		String outOfBrace = modifiedSourceCodeString.split("\\{")[0];
		String[] splittedBySpace = outOfBrace.split("\\s+");
		if (!outOfBrace.matches(".*\\s+class\\s+.*")
				&& !outOfBrace.matches(".*\\s+enum\\s+.*")) {
			meta.isAbstract = true;
		} else {
			for (String each : splittedBySpace) {
				if (each.equals("abstract") || each.equals("interface")
						|| each.equals("@interface")) {
					meta.isAbstract = true;
					break;
				}
			}
		}
		// -----------------
		// class name
		meta.name = splittedBySpace[splittedBySpace.length - 1];
		for (int i = 0; i < splittedBySpace.length; i++) {
			if (splittedBySpace[i].equals("extends")
					|| splittedBySpace[i].equals("implements")) {
				meta.name = splittedBySpace[i - 1];
				break;
			}
		}
		// -----------------
		// imported list
		meta.importedList = importedListExtractor
				.extract(modifiedSourceCodeString);
		// -----------------
		// constructors
		meta.constructors = constructorMetaExtractor.initialize(meta,
				modifiedSourceCodeString).extract(modifiedSourceCodeString);
		// -----------------
		// methods
		meta.methods = methodMetaExtractor.initialize(meta,
				modifiedSourceCodeString).extract(modifiedSourceCodeString);

		// check duplicated variable name
		if (meta.constructors.size() > 0) {
			// constructor arg name is "target"
			for (ConstructorMeta cons : meta.constructors) {
				int len = cons.argNames.size();
				for (int i = 0; i < len; i++) {
					if (cons.argNames.get(i).equals("target")) {
						cons.argNames.set(i, "target_");
					}
				}
			}
			// duplicated to constructor arg names
			ConstructorMeta constructor = meta.constructors.get(0);
			for (MethodMeta method : meta.methods) {
				int len = method.argNames.size();
				for (int i = 0; i < len; i++) {
					String targetArgName = method.argNames.get(i);
					List<String> methodArgNames = new ArrayList<String>();
					for (String methodArgName : method.argNames) {
						if (!targetArgName.equals(methodArgName)) {
							methodArgNames.add(methodArgName);
						}
					}
					method.argNames.set(
							i,
							renameIfDuplicatedToConstructorArgNames(
									targetArgName, constructor.argNames,
									methodArgNames));
				}
			}
		}

		return meta;

	}

	String renameIfDuplicatedToConstructorArgNames(String argName,
			List<String> constructorArgs, List<String> methodArgs) {
		if (argName == null) {
			return null;
		}
		if (argName.equals("target")) {
			return renameIfDuplicatedToConstructorArgNames(argName + "_",
					constructorArgs, methodArgs);
		}
		for (String consArg : constructorArgs) {
			if (argName.equals(consArg)) {
				return renameIfDuplicatedToConstructorArgNames(argName + "_",
						constructorArgs, methodArgs);
			}
		}
		for (String methodArg : methodArgs) {
			if (argName.equals(methodArg)) {
				return renameIfDuplicatedToConstructorArgNames(argName + "_",
						constructorArgs, methodArgs);
			}
		}
		return argName;
	}
}
