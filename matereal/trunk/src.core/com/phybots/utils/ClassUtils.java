/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package com.phybots.utils;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.phybots.entity.Robot;


public class ClassUtils {

	public static void main(String[] args) {

		System.out.println("List of loaded Phybots classes:");
		for (String className : getClassNames("jp.digitalmuseum")) {
			System.out.println(className);
		}
		System.out.println();

		System.out.println("List of available Robot classes:");
		for (Class<?> classObject : getClasses("jp.digitalmuseum")) {
			if (Robot.class.isAssignableFrom(classObject) &&
					!Modifier.isAbstract(classObject.getModifiers())) {
				System.out.println(classObject.getCanonicalName());
			}
		}
	}

	public static List<String> getClassNames() {
		return getClassNames(null);
	}

	public static List<String> getClassNames(String rootPackageName) {
		List<String> classNames = new ArrayList<String>();

		String classpathString = System.getProperties().getProperty(
				"java.class.path");
		if (classpathString != null && classpathString.length() > 0) {
			Stack<File> classpaths = new Stack<File>();
			Stack<String> packages = new Stack<String>();
			for (String classpath : classpathString.split(File.pathSeparator)) {
				classpaths.push(new File(classpath.trim()));
				packages.push(null);
			}
			while (!classpaths.isEmpty()) {
				File classpath = classpaths.pop();
				String packageName = packages.pop();
				if (classpath.exists()) {

					if (classpath.isDirectory()) {
						for (File file : classpath.listFiles()) {
							StringBuilder sb = new StringBuilder();
							if (packageName != null) {
								sb.append(packageName);
								sb.append(".");
							}
							String fileName = file.getName();

							if (file.isDirectory()) {
								classpaths.add(file);
								sb.append(fileName);
								packages.add(sb.toString());
							}

							else if (file.isFile() &&
									endsWith(fileName, ".class")) {
								sb.append(fileName.substring(
										0,
										fileName.length() - ".class".length()));

								String className = sb.toString();
								if (rootPackageName == null ||
										className.startsWith(rootPackageName)) {
									classNames.add(className);
								}
							}
						}
					}

					else if (packageName == null &&
							classpath.isFile() &&
							endsWith(classpath.getName(), ".jar")) {
						try {
							ZipFile zipFile = new ZipFile(classpath);
							Enumeration<? extends ZipEntry> enumeration =
									zipFile.entries();

							while (enumeration.hasMoreElements()) {
								ZipEntry entry = enumeration.nextElement();
								if (!entry.isDirectory()) {
									String path = entry.getName();
									if (!endsWith(path, ".class")) {
										continue;
									}

									String className = path.substring(
											0,
											path.length() - ".class".length());
									className = className.replaceAll("/", ".");

									if (rootPackageName == null ||
											className.startsWith(rootPackageName)) {
										classNames.add(className);
									}
								}
							}
						} catch (Exception e) {
							// Do nothing.
						}
					}
				}
			}
		}
		return classNames;
	}

	public static List<Class<?>> getClasses() {
		return getClasses(null);
	}

	public static List<Class<?>> getClasses(String packageName) {
		List<Class<?>> classObjects = new ArrayList<Class<?>>();

		for (String className : getClassNames(packageName)) {
			if (className.indexOf("$") < 0) {
				try {
					classObjects.add(Class.forName(className));
				} catch (Exception e) {
					// Do nothing.
				} catch (Error e) {
					// Do nothing.
				}
			}
		}
		return classObjects;
	}

	private static boolean endsWith(String filePath, String extension) {
		return filePath.toLowerCase().endsWith(extension);
	}
}
