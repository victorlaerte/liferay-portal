/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.form.internal.dto.v1_0.util;

import static com.liferay.portal.vulcan.util.TransformUtil.transform;
import static com.liferay.portal.vulcan.util.TransformUtil.transformToArray;

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldType;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;
import com.liferay.dynamic.data.mapping.model.DDMFormRule;
import com.liferay.dynamic.data.mapping.model.DDMFormSuccessPageSettings;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.headless.form.dto.v1_0.Column;
import com.liferay.headless.form.dto.v1_0.Field;
import com.liferay.headless.form.dto.v1_0.FormPage;
import com.liferay.headless.form.dto.v1_0.FormStructure;
import com.liferay.headless.form.dto.v1_0.Grid;
import com.liferay.headless.form.dto.v1_0.Option;
import com.liferay.headless.form.dto.v1_0.Row;
import com.liferay.headless.form.dto.v1_0.SuccessPage;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Javier Gamarra
 */
public class StructureUtil {

	public static FormStructure toFormStructure(
			DDMStructure ddmStructure, Portal portal,
			UserLocalService userLocalService, Locale locale)
		throws PortalException {

		DDMForm ddmForm = ddmStructure.getDDMForm();

		DDMFormSuccessPageSettings ddmFormSuccessPageSettings =
			ddmForm.getDDMFormSuccessPageSettings();

		DDMFormLayout ddmFormLayout = ddmStructure.getDDMFormLayout();

		return new FormStructure() {
			{
				availableLanguages = LocaleUtil.toW3cLanguageIds(
					ddmStructure.getAvailableLanguageIds());
				contentSpaceId = ddmStructure.getGroupId();
				creator = CreatorUtil.toCreator(
					portal,
					userLocalService.getUserById(ddmStructure.getUserId()));
				dateCreated = ddmStructure.getCreateDate();
				dateModified = ddmStructure.getModifiedDate();
				description = ddmStructure.getDescription(locale);
				formPages = transformToArray(
					ddmFormLayout.getDDMFormLayoutPages(),
					ddmFormLayoutPage -> _toFormPage(
						ddmFormLayoutPage, ddmStructure, locale),
					FormPage.class);
				id = ddmStructure.getStructureId();
				name = ddmStructure.getName(locale);
				successPage =
					_getSuccessPage(ddmFormSuccessPageSettings, locale);
			}
		};
	}

	private static SuccessPage _getSuccessPage(
		DDMFormSuccessPageSettings ddmFormSuccessPageSettings, Locale locale) {

		if (ddmFormSuccessPageSettings.isEnabled()) {

			return new SuccessPage() {
				{
					description = _toString(
						ddmFormSuccessPageSettings.getBody(), locale);
					headline = _toString(
						ddmFormSuccessPageSettings.getTitle(), locale);
				}
			};
		}

		return null;
	}

	private static List<String> _getFieldNames(
		DDMFormLayoutPage ddmFormLayoutPage, DDMStructure ddmStructure) {

		return Stream.of(
			ddmFormLayoutPage.getDDMFormLayoutRows()
		).flatMap(
			Collection::stream
		).map(
			DDMFormLayoutRow::getDDMFormLayoutColumns
		).flatMap(
			Collection::stream
		).map(
			DDMFormLayoutColumn::getDDMFormFieldNames
		).map(
			formFieldNames -> _getNestedFieldNames(formFieldNames, ddmStructure)
		).flatMap(
			Collection::stream
		).collect(
			Collectors.toList()
		);
	}

	private static List<String> _getNestedFieldNames(
		List<String> ddmFormFieldNames, DDMStructure ddmStructure) {

		List<DDMFormField> ddmFormFields = ddmStructure.getDDMFormFields(true);

		Stream<DDMFormField> ddmFormFieldStream = ddmFormFields.stream();

		return ddmFormFieldStream.filter(
			formField -> ddmFormFieldNames.contains(formField.getName())
		).map(
			formField -> formField.getNestedDDMFormFields(
			).stream(
			).map(
				DDMFormField::getName
			).collect(
				Collectors.toList()
			)
		).map(
			fieldNames -> _getNestedFieldNames(fieldNames, ddmStructure)
		).peek(
			fieldNames -> fieldNames.addAll(ddmFormFieldNames)
		).flatMap(
			Collection::stream
		).collect(
			Collectors.toList()
		);
	}

	private static Boolean _hasFormRulesFunction(DDMFormField ddmFormField) {
		DDMForm ddmForm = ddmFormField.getDDMForm();

		List<DDMFormRule> ddmFormRules = ddmForm.getDDMFormRules();

		return ddmFormRules.stream(
		).map(
			DDMFormRule::getCondition
		).anyMatch(
			ruleCondition -> ruleCondition.contains(ddmFormField.getName())
		);
	}

	private static String _toDataType(DDMFormField ddmFormField) {
		String type = ddmFormField.getType();

		if ("date".equals(type)) {
			return "date";
		}
		if (DDMFormFieldType.DOCUMENT_LIBRARY.equals(type)) {
			return "document";
		}
		else if (DDMFormFieldType.JOURNAL_ARTICLE.equals(type)) {
			return "structuredContent";
		}
		else if (DDMFormFieldType.LINK_TO_PAGE.equals(type)) {
			return "url";
		}
		else if ("paragraph".equals(type)) {
			return "paragraph";
		}
		else if (DDMFormFieldType.RADIO.equals(type)) {
			return "string";
		}

		return ddmFormField.getDataType();
	}

	private static Field _toField(DDMFormField ddmFormField, Locale locale) {
		return new Field() {
			{
				grid = new Grid() {
					{
						columns = transform(
							_toLocalizedValueMapEntry(ddmFormField, "columns"),
							entry -> new Column() {
								{
									label = _toString(entry.getValue(), locale);
									value = entry.getKey();
								}
							},
							Column.class);

						rows = transform(
							_toLocalizedValueMapEntry(ddmFormField, "rows"),
							entry -> new Row() {
								{
									label = _toString(entry.getValue(), locale);
									value = entry.getKey();
								}
							},
							Row.class);
					}
				};
				hasFormRules = _hasFormRulesFunction(ddmFormField);
				immutable = ddmFormField.isTransient();
				label = _toString(ddmFormField.getLabel(), locale);
				localizable = ddmFormField.isLocalizable();
				multiple = ddmFormField.isMultiple();
				name = ddmFormField.getName();
				predefinedValue = _toString(
					ddmFormField.getPredefinedValue(), locale);
				repeatable = ddmFormField.isRepeatable();
				required = ddmFormField.isRequired();
				showLabel = ddmFormField.isShowLabel();
				text = _getText(ddmFormField, locale);

				setDataType(_toDataType(ddmFormField));
				setInputControl(_toInputControl(ddmFormField));
				setLabel(_toString(ddmFormField.getLabel(), locale));
				setLocalizable(ddmFormField.isLocalizable());
				setMultiple(ddmFormField.isMultiple());
				setName(ddmFormField.getName());
				setOptions(
					() -> Optional.ofNullable(
						ddmFormField.getDDMFormFieldOptions()
					).map(
						DDMFormFieldOptions::getOptions
					).map(
						Map::entrySet
					).map(
						Set::stream
					).orElseGet(
						Stream::empty
					).map(
						entry -> new Option() {
							{
								setLabel(_toString(entry.getValue(), locale));
								setValue(entry.getKey());
							}
						}
					).toArray(
						Option[]::new
					));
			}
		};
	}

	private static String _getText(DDMFormField ddmFormField, Locale locale) {
		Object textProperty = ddmFormField.getProperty("text");

		if (textProperty instanceof LocalizedValue) {
			return _toString((LocalizedValue) textProperty, locale);
		}

		return null;
	}
	private static FormPage _toFormPage(
		DDMFormLayoutPage ddmFormLayoutPage, DDMStructure ddmStructure,
		Locale locale) {

		List<String> fieldNames = _getFieldNames(
			ddmFormLayoutPage, ddmStructure);

		DDMFormField[] ddmFormFields = ddmStructure.getDDMFormFields(
			true
		).stream(
		).filter(
			ddmFormField -> fieldNames.contains(ddmFormField.getName())
		).toArray(
			DDMFormField[]::new
		);

		return new FormPage() {
			{
				headline = _toString(ddmFormLayoutPage.getTitle(), locale);
				fields = transform(
					ddmFormFields,
					ddmFormField -> _toField(ddmFormField, locale),
					Field.class);
				text = _toString(ddmFormLayoutPage.getDescription(), locale);
			}
		};
	}

	private static String _toInputControl(DDMFormField ddmFormField) {
		String type = ddmFormField.getType();

		if (DDMFormFieldType.CHECKBOX.equals(type) ||
			DDMFormFieldType.RADIO.equals(type) ||
			DDMFormFieldType.SELECT.equals(type) ||
			DDMFormFieldType.TEXT.equals(type) ||
			DDMFormFieldType.TEXT_AREA.equals(type)) {

			return type;
		}

		return null;
	}

	private static Map.Entry<String, LocalizedValue>[]
		_toLocalizedValueMapEntry(DDMFormField ddmFormField, String element) {

		Object property = ddmFormField.getProperty(element);

		if (property != null) {
			DDMFormFieldOptions ddmFormFieldOptions =
				(DDMFormFieldOptions) property;

			Map<String, LocalizedValue> options = ddmFormFieldOptions.getOptions();

			Set<Map.Entry<String, LocalizedValue>> entries = options.entrySet();

			return entries.toArray(new Map.Entry[entries.size()]);
		}

		return new Map.Entry[0];
	}

	private static String _toString(
		LocalizedValue localizedValue, Locale locale) {

		if (localizedValue == null) {
			return null;
		}

		return localizedValue.getString(locale);
	}

}