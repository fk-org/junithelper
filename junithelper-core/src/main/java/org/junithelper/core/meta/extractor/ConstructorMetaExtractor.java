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
import java.util.regex.Pattern;

import org.junithelper.core.config.Configulation;
import org.junithelper.core.constant.RegExp;
import org.junithelper.core.constant.StringValue;
import org.junithelper.core.meta.AccessModifier;
import org.junithelper.core.meta.ArgTypeMeta;
import org.junithelper.core.meta.ClassMeta;
import org.junithelper.core.meta.ConstructorMeta;
import org.junithelper.core.parser.convert.TypeNameConverter;
import org.junithelper.core.parser.detect.AccessModifierDetector;

public class ConstructorMetaExtractor {

	private AccessModifierDetector accessModifierDetector = new AccessModifierDetector();

	private Configulation config;
	private ClassMeta classMeta;
	@SuppressWarnings("unused")
	private String sourceCodeString;

	public ConstructorMetaExtractor(Configulation config) {
		this.config = config;
	}

	public ConstructorMetaExtractor initialize(String sourceCodeString) {
		initialize(null, sourceCodeString);
		return this;
	}

	public ConstructorMetaExtractor initialize(ClassMeta classMeta,
			String sourceCodeString) {
		this.sourceCodeString = sourceCodeString;
		if (classMeta == null) {
			this.classMeta = new ClassMetaExtractor(config)
					.extract(sourceCodeString);
		} else {
			this.classMeta = classMeta;
		}
		return this;
	}

	public List<ConstructorMeta> extract(String sourceCodeString) {
		List<ConstructorMeta> dest = new ArrayList<ConstructorMeta>();
		sourceCodeString = sourceCodeString
				.replaceAll("\\s+?" + StringValue.Comma, StringValue.Comma)
				.replaceAll(StringValue.Comma + "\\s+?", StringValue.Comma)
				.replaceAll("<\\s+?", "<").replaceAll("\\s+?>", ">");
		Matcher matcherGrouping = RegExp.PatternObject.MethodSignatureArea
				.matcher(sourceCodeString);
		while (matcherGrouping.find()) {
			ConstructorMeta meta = new ConstructorMeta();
			String methodSignatureArea = matcherGrouping.group(0)
					.replaceAll(StringValue.CarriageReturn, StringValue.Empty)
					.replaceAll(StringValue.LineFeed, StringValue.Space);
			// -----------------
			// access modifier
			meta.accessModifier = getAccessModifier(methodSignatureArea);
			// -----------------
			// method signature area without access modifier
			String methodSignatureAreaWithoutAccessModifier = trimAccessModifierFromMethodSignatureArea(methodSignatureArea);
			String matchesConstructors = RegExp.Anything_ZeroOrMore_Min
					+ RegExp.WhiteSpace.Consecutive_ZeroOrMore_Max
					+ classMeta.name + "\\(([^\\)]*?)\\)"
					+ RegExp.WhiteSpace.Consecutive_ZeroOrMore_Max
					+ "(throws\\s.+)*.*?"
					+ RegExp.WhiteSpace.Consecutive_ZeroOrMore_Max + "\\{.*";
			if (!methodSignatureAreaWithoutAccessModifier
					.matches(matchesConstructors)) {
				continue;
			}
			// -----------------
			// is constructor
			String constructorString = StringValue.Space
					+ methodSignatureAreaWithoutAccessModifier;
			String groupConstructor = RegExp.WhiteSpace.Consecutive_ZeroOrMore_Max
					+ classMeta.name
					+ "\\(([^\\)]*?)\\)"
					+ RegExp.WhiteSpace.Consecutive_ZeroOrMore_Max
					+ "(throws .+)*.*?"
					+ RegExp.WhiteSpace.Consecutive_ZeroOrMore_Max + "\\{.*";
			Matcher constructorMatcher = Pattern.compile(groupConstructor)
					.matcher(constructorString);
			if (!constructorMatcher.find()) {
				continue;
			}
			// -----------------
			// args
			String args = constructorMatcher.group(1);
			// generics
			String[] tmpArr = args.split(StringValue.Comma);
			int tmpArrLen = tmpArr.length;
			List<String> tmpArrList = new ArrayList<String>();
			String buf = StringValue.Empty;
			for (int i = 0; i < tmpArrLen; i++) {
				String element = tmpArr[i].trim();
				// ex. List<String>
				if (element.matches(".+?<.+?>.+")) {
					tmpArrList.add(element);
					continue;
				}
				// ex. Map<String
				if (element.matches(".+?<.+")) {
					buf += element;
					continue;
				}
				// ex. (Map<String,) Object>
				if (element.matches(".+?>.+")) {
					String result = buf + StringValue.Comma + element;
					tmpArrList.add(result);
					buf = StringValue.Empty;
					continue;
				}
				if (!buf.equals(StringValue.Empty)) {
					buf += StringValue.Comma + element;
					continue;
				}
				tmpArrList.add(element);
			}
			String[] argArr = tmpArrList.toArray(new String[0]);
			int argArrLen = argArr.length;
			for (int i = 0; i < argArrLen; i++) {
				ArgTypeMeta argTypeMeta = new ArgTypeMeta();
				String argTypeFull = argArr[i];
				Matcher toGenericsMatcher = Pattern.compile(
						RegExp.Generics_Group).matcher(argTypeFull);
				while (toGenericsMatcher.find()) {
					String[] generics = toGenericsMatcher.group()
							.replaceAll("<", StringValue.Empty)
							.replaceAll(">", StringValue.Empty)
							.split(StringValue.Comma);
					// convert to java.lang.Object if self
					// class is included
					for (String generic : generics) {
						generic = new TypeNameConverter(config)
								.toCompilableType(classMeta.packageName,
										generic, classMeta.importedList);
						argTypeMeta.generics.add(generic);
					}
				}
				String argTypeName = argTypeFull
						.replaceAll(RegExp.Generics, StringValue.Empty)
						.replaceAll("final ", StringValue.Empty).split("\\s+")[0]
						.trim();
				if (argTypeName != null && !argTypeName.equals("")) {
					argTypeMeta.name = new TypeNameConverter(config)
							.toCompilableType(classMeta.packageName,
									argTypeName, argTypeMeta.generics,
									classMeta.importedList);
					argTypeMeta.nameInMethodName = new TypeNameConverter(config)
							.toAvailableInMethodName(argTypeMeta.name);
					meta.argTypes.add(argTypeMeta);
					Matcher nameMatcher = RegExp.PatternObject.MethodArg_Group
							.matcher(argTypeFull);
					if (nameMatcher.find()) {
						meta.argNames.add(nameMatcher.group(1));
					} else {
						meta.argNames.add("constructorArg" + i);
					}
				}
			}
			dest.add(meta);
		}
		if (dest.size() == 0) {
			ConstructorMeta defaultConstructorMeta = new ConstructorMeta();
			dest.add(defaultConstructorMeta);
		}
		return dest;
	}

	AccessModifier getAccessModifier(String methodSignatureArea) {
		if (accessModifierDetector.isPublic(methodSignatureArea)) {
			return AccessModifier.Public;
		} else if (accessModifierDetector.isProtected(methodSignatureArea)) {
			return AccessModifier.Protected;
		} else if (accessModifierDetector.isPackageLocal(methodSignatureArea)) {
			return AccessModifier.PackageLocal;
		} else if (accessModifierDetector.isPrivate(methodSignatureArea)) {
			return AccessModifier.Private;
		} else {
			return AccessModifier.Public;
		}
	}

	String trimAccessModifierFromMethodSignatureArea(String methodSignatureArea) {
		String regExpForAccessModifier_public = AccessModifierDetector.RegExp.Prefix
				+ "public" + "\\s+";
		String regExpForAccessModifier_protected = AccessModifierDetector.RegExp.Prefix
				+ "protected" + "\\s+";
		String methodSignatureAreaWithoutAccessModifier = methodSignatureArea
				.replaceAll(StringValue.Tab, StringValue.Space)
				.replaceAll(regExpForAccessModifier_public, StringValue.Space)
				.replaceAll(regExpForAccessModifier_protected,
						StringValue.Space)
				.replaceAll("\\sfinal\\s", StringValue.Space);
		return methodSignatureAreaWithoutAccessModifier;
	}

}