/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.plus.sql.gen;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.AggregatePath;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.Table;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import com.navercorp.spring.data.plus.sql.gen.annotation.GeneratedTable;
import com.navercorp.spring.data.plus.sql.gen.column.TbColumn;
import com.navercorp.spring.data.plus.sql.gen.column.TbInfo;
import com.navercorp.spring.jdbc.plus.commons.annotations.SqlFunction;

/**
 * The type Spring data table generator.
 *
 * @author Myeonghyeon Lee
 */
@SupportedAnnotationTypes(value = "org.springframework.data.relational.core.mapping.Table")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SpringDataTableGenerator extends AbstractProcessor {
	private static final String ENTITY_TYPE_FIELD_NAME = "ENTITY_TYPE";
	private static final String TABLE_INFO_FIELD_NAME = "___tbInfo";
	private final Set<String> generatedTypes = new HashSet<>();

	private static String convertTableTypeName(String simpleName) {
		return "Tb" + simpleName;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}

		if (annotations.size() == 0) {
			return true;
		}

		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Table.class);
		for (Element element : elements) {
			this.generateType(element);
		}

		return true;
	}

	private void generateType(Element element) {
		if (this.generatedTypes.contains(element.asType().toString())) {
			return;
		}

		if (element.getAnnotation(GeneratedTable.class) != null) {
			return;
		}

		TypeSpec typeSpec = this.createTypeSpec(element);

		String packageName = this.processingEnv.getElementUtils()
			.getPackageOf(element)
			.getQualifiedName()
			.toString();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

		String fileName = packageName + "." + typeSpec.name;
		this.createSourceFile(this.processingEnv.getFiler(), fileName, javaFile);

		this.generatedTypes.add(element.asType().toString());
	}

	/**
	 * Create type spec.
	 *
	 * @param element the element
	 * @return the type spec
	 */
	protected TypeSpec createTypeSpec(Element element) {
		List<? extends Element> fieldElements = element.getEnclosedElements();
		List<FieldSpec> fieldSpecs = new ArrayList<>();

		fieldSpecs.add(FieldSpec.builder(ClassName.get(Class.class), ENTITY_TYPE_FIELD_NAME)
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
			.initializer("$N.class", element.asType().toString())
			.build());

		fieldSpecs.add(FieldSpec.builder(ClassName.get(TbInfo.class), TABLE_INFO_FIELD_NAME)
			.addModifiers(Modifier.PRIVATE, Modifier.TRANSIENT, Modifier.FINAL)
			.addAnnotation(AnnotationSpec.builder(Transient.class).build())
			.build());

		for (Element fieldElement : fieldElements) {
			if (!fieldElement.getKind().isField()) {
				continue;
			}

			if (fieldElement.getAnnotation(Transient.class) != null) {
				continue;
			}

			TypeName type = this.determineFieldTypeName(fieldElement);
			String fieldName = fieldElement.getSimpleName().toString();
			fieldSpecs.add(FieldSpec.builder(type, fieldName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addAnnotations(this.createAnnotationSpecs(fieldElement))
				.build());
		}

		List<AnnotationSpec> annotationSpecs = new ArrayList<>();
		annotationSpecs.add(AnnotationSpec.builder(GeneratedTable.class)
			.addMember("value", "$S", element.asType().toString())
			.build());
		Table table = element.getAnnotation(Table.class);
		if (table != null) {
			annotationSpecs.add(AnnotationSpec.get(table));
		}

		List<MethodSpec> methods = this.createConstructors(fieldSpecs);
		methods.add(MethodSpec.methodBuilder("path")
			.addModifiers(Modifier.PUBLIC)
			.returns(ClassName.get(String.class))
			.addStatement("return this.$N.path()", TABLE_INFO_FIELD_NAME)
			.build());
		methods.add(MethodSpec.methodBuilder("table")
			.addModifiers(Modifier.PUBLIC)
			.returns(ClassName.get(String.class))
			.addStatement("return this.$N.table()", TABLE_INFO_FIELD_NAME)
			.build());
		methods.add(MethodSpec.methodBuilder("alias")
			.addModifiers(Modifier.PUBLIC)
			.returns(ClassName.get(String.class))
			.addStatement("return this.$N.alias()", TABLE_INFO_FIELD_NAME)
			.build());

		String fileName = convertTableTypeName(element.getSimpleName().toString());
		return TypeSpec.classBuilder(fileName)
			.addAnnotations(annotationSpecs)
			.addModifiers(Modifier.PUBLIC)
			.addMethods(methods)
			.addFields(fieldSpecs)
			.build();
	}

	/**
	 * Create constructors list.
	 *
	 * @param fieldSpecs the field specs
	 * @return the list
	 */
	protected List<MethodSpec> createConstructors(List<FieldSpec> fieldSpecs) {
		List<MethodSpec> methodSpecs = new ArrayList<>();

		MethodSpec.Builder allArgsConstructorBuilder = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		for (FieldSpec fieldSpec : fieldSpecs) {
			if (fieldSpec.modifiers.contains(Modifier.STATIC)) {
				continue;
			}

			allArgsConstructorBuilder.addParameter(fieldSpec.type, fieldSpec.name);
			allArgsConstructorBuilder.addStatement("this.$N = $N", fieldSpec.name, fieldSpec.name);
		}
		methodSpecs.add(allArgsConstructorBuilder.build());

		MethodSpec.Builder transformConstructorBuilder = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addParameter(ClassName.get(RelationalMappingContext.class), "relationalMappingContext")
			.addStatement(
				"org.springframework.data.relational.core.mapping.RelationalPersistentEntity<?> "
					+ "entity = relationalMappingContext.getRequiredPersistentEntity($N)",
				ENTITY_TYPE_FIELD_NAME)
			.addStatement(
				"org.springframework.data.relational.core.mapping.AggregatePath "
					+ "aggregatePath = "
					+ "relationalMappingContext.getAggregatePath(entity)");

		for (FieldSpec fieldSpec : fieldSpecs) {
			if (fieldSpec.modifiers.contains(Modifier.STATIC)) {
				continue;
			}

			if (fieldSpec.type.toString().equals(TbInfo.class.getName())) {
				transformConstructorBuilder.addStatement(
					"this.$N = com.navercorp.spring.data.plus.sql.gen.column.TbInfo.create("
						+ "aggregatePath)", TABLE_INFO_FIELD_NAME);
				continue;
			}

			String fieldName = fieldSpec.name;
			transformConstructorBuilder.addStatement(
				"org.springframework.data.relational.core.mapping.AggregatePath "
					+ "$NPropertyPath = "
					+ "aggregatePath.append(entity.getRequiredPersistentProperty($S))",
				fieldName, fieldName);
			if (fieldSpec.type.toString().equals(TbColumn.class.getName())) {
				transformConstructorBuilder.addStatement(
					"this.$N = com.navercorp.spring.data.plus.sql.gen.column.TbColumn.create("
						+ "$NPropertyPath)", fieldName, fieldName);
			} else {
				transformConstructorBuilder.addStatement(
					"this.$N = new $N($NPropertyPath)",
					fieldName, fieldSpec.type.toString(), fieldName);
			}
		}
		methodSpecs.add(transformConstructorBuilder.build());

		MethodSpec.Builder pathConstructorBuilder = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addParameter(ClassName.get(AggregatePath.class), "aggregatePath")
			.addStatement(
				"org.springframework.data.relational.core.mapping.RelationalPersistentEntity<?> "
					+ "entity = aggregatePath.getLeafEntity()");

		for (FieldSpec fieldSpec : fieldSpecs) {
			if (fieldSpec.modifiers.contains(Modifier.STATIC)) {
				continue;
			}

			if (fieldSpec.type.toString().equals(TbInfo.class.getName())) {
				pathConstructorBuilder.addStatement(
					"this.$N = com.navercorp.spring.data.plus.sql.gen.column.TbInfo"
						+ ".create(aggregatePath)",
					TABLE_INFO_FIELD_NAME);
				continue;
			}

			String fieldName = fieldSpec.name;
			pathConstructorBuilder.addStatement(
				"org.springframework.data.relational.core.mapping.AggregatePath "
					+ "$NPropertyPath = "
					+ "aggregatePath.append(entity.getRequiredPersistentProperty($S))",
				fieldName, fieldName);
			if (fieldSpec.type.toString().equals(TbColumn.class.getName())) {
				pathConstructorBuilder.addStatement(
					"this.$N = com.navercorp.spring.data.plus.sql.gen.column.TbColumn.create("
						+ "$NPropertyPath)", fieldName, fieldName);
			} else {
				pathConstructorBuilder.addStatement(
					"this.$N = new $N($NPropertyPath)",
					fieldName, fieldSpec.type.toString(), fieldName);
			}
		}
		methodSpecs.add(pathConstructorBuilder.build());

		MethodSpec defaultConstructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addStatement(
				"this(new org.springframework.data.relational.core.mapping.RelationalMappingContext())")
			.build();
		methodSpecs.add(defaultConstructor);

		return methodSpecs;
	}

	/**
	 * Determine field type name type name.
	 *
	 * @param fieldElement the field element
	 * @return the type name
	 */
	protected TypeName determineFieldTypeName(Element fieldElement) {
		TypeName type = ClassName.get(TbColumn.class);
		TypeName typeName = ClassName.get(fieldElement.asType());

		if (typeName instanceof ParameterizedTypeName parameterizedTypeName) {
			if (parameterizedTypeName.typeArguments.size() == 1) {    // List, Set
				ClassName componentName = (ClassName)parameterizedTypeName.typeArguments.get(0);
				type = ClassName.get(
					componentName.packageName(),
					convertTableTypeName(componentName.simpleName()));
			} else if (parameterizedTypeName.typeArguments.size() == 2) {    // Collection
				ClassName componentName = (ClassName)parameterizedTypeName.typeArguments.get(1);
				type = ClassName.get(
					componentName.packageName(),
					convertTableTypeName(componentName.simpleName()));
			}
		} else if (typeName instanceof ArrayTypeName arrayTypeName) {
			ClassName componentName = (ClassName)arrayTypeName.componentType;
			type = ClassName.get(
				componentName.packageName(),
				convertTableTypeName(componentName.simpleName()));
		} else if (fieldElement.getAnnotation(Embedded.class) != null
			|| fieldElement.getAnnotation(Embedded.Empty.class) != null
			|| fieldElement.getAnnotation(Embedded.Nullable.class) != null
		) {
			ClassName className = (ClassName)typeName;
			type = ClassName.get(
				className.packageName(),
				convertTableTypeName(className.simpleName()));
			Element embeddedElement = this.processingEnv.getElementUtils()
				.getTypeElement(fieldElement.asType().toString());
			this.generateType(embeddedElement);
		} else if (this.processingEnv.getTypeUtils()
			.asElement(fieldElement.asType())
			.getAnnotation(Table.class) != null
		) {
			ClassName className = (ClassName)typeName;
			type = ClassName.get(className.packageName(), convertTableTypeName(className.simpleName()));
		}

		return type;
	}

	/**
	 * Create annotation specs list.
	 *
	 * @param element the element
	 * @return the list
	 */
	protected List<AnnotationSpec> createAnnotationSpecs(Element element) {
		List<AnnotationSpec> annotationSpecs = new ArrayList<>();
		Id id = element.getAnnotation(Id.class);
		if (id != null) {
			annotationSpecs.add(AnnotationSpec.get(id));
		}
		Column column = element.getAnnotation(Column.class);
		if (column != null) {
			annotationSpecs.add(AnnotationSpec.get(column));
		}
		Embedded embedded = element.getAnnotation(Embedded.class);
		if (embedded != null) {
			annotationSpecs.add(AnnotationSpec.get(embedded));
		}
		Embedded.Nullable embeddedNullable = element.getAnnotation(Embedded.Nullable.class);
		if (embeddedNullable != null) {
			annotationSpecs.add(AnnotationSpec.get(embeddedNullable));
		}
		Embedded.Empty embeddedEmpty = element.getAnnotation(Embedded.Empty.class);
		if (embeddedEmpty != null) {
			annotationSpecs.add(AnnotationSpec.get(embeddedEmpty));
		}
		MappedCollection mappedCollection = element.getAnnotation(MappedCollection.class);
		if (mappedCollection != null) {
			annotationSpecs.add(AnnotationSpec.get(mappedCollection));
		}
		SqlFunction sqlFunction = element.getAnnotation(SqlFunction.class);
		if (sqlFunction != null) {
			annotationSpecs.add(AnnotationSpec.get(sqlFunction));
		}

		return annotationSpecs;
	}

	private void createSourceFile(Filer filer, String sourcePath, JavaFile javaFile) {
		JavaFileObject jfo = null;
		try {
			jfo = filer.createSourceFile(sourcePath);
			jfo.delete();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Can not create sourcePath. path: " + sourcePath, e);
		}

		try (Writer writer = jfo.openWriter()) {
			javaFile.writeTo(writer);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Can not write file. sourcePath: " + sourcePath, e);
		}
	}
}
