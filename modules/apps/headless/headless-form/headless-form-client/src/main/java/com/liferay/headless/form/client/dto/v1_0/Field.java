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

package com.liferay.headless.form.client.dto.v1_0;

import com.liferay.headless.form.client.function.UnsafeSupplier;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class Field {

	public Boolean getAutocomplete() {
		return autocomplete;
	}

	public void setAutocomplete(Boolean autocomplete) {
		this.autocomplete = autocomplete;
	}

	public void setAutocomplete(
		UnsafeSupplier<Boolean, Exception> autocompleteUnsafeSupplier) {

		try {
			autocomplete = autocompleteUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean autocomplete;

	public String getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public void setDataSourceType(
		UnsafeSupplier<String, Exception> dataSourceTypeUnsafeSupplier) {

		try {
			dataSourceType = dataSourceTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String dataSourceType;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public void setDataType(
		UnsafeSupplier<String, Exception> dataTypeUnsafeSupplier) {

		try {
			dataType = dataTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String dataType;

	public String getDisplayStyle() {
		return displayStyle;
	}

	public void setDisplayStyle(String displayStyle) {
		this.displayStyle = displayStyle;
	}

	public void setDisplayStyle(
		UnsafeSupplier<String, Exception> displayStyleUnsafeSupplier) {

		try {
			displayStyle = displayStyleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String displayStyle;

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public void setGrid(UnsafeSupplier<Grid, Exception> gridUnsafeSupplier) {
		try {
			grid = gridUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Grid grid;

	public Boolean getHasFormRules() {
		return hasFormRules;
	}

	public void setHasFormRules(Boolean hasFormRules) {
		this.hasFormRules = hasFormRules;
	}

	public void setHasFormRules(
		UnsafeSupplier<Boolean, Exception> hasFormRulesUnsafeSupplier) {

		try {
			hasFormRules = hasFormRulesUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean hasFormRules;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	public Boolean getImmutable() {
		return immutable;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
	}

	public void setImmutable(
		UnsafeSupplier<Boolean, Exception> immutableUnsafeSupplier) {

		try {
			immutable = immutableUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean immutable;

	public Boolean getInline() {
		return inline;
	}

	public void setInline(Boolean inline) {
		this.inline = inline;
	}

	public void setInline(
		UnsafeSupplier<Boolean, Exception> inlineUnsafeSupplier) {

		try {
			inline = inlineUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean inline;

	public String getInputControl() {
		return inputControl;
	}

	public void setInputControl(String inputControl) {
		this.inputControl = inputControl;
	}

	public void setInputControl(
		UnsafeSupplier<String, Exception> inputControlUnsafeSupplier) {

		try {
			inputControl = inputControlUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String inputControl;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLabel(
		UnsafeSupplier<String, Exception> labelUnsafeSupplier) {

		try {
			label = labelUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String label;

	public Boolean getLocalizable() {
		return localizable;
	}

	public void setLocalizable(Boolean localizable) {
		this.localizable = localizable;
	}

	public void setLocalizable(
		UnsafeSupplier<Boolean, Exception> localizableUnsafeSupplier) {

		try {
			localizable = localizableUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean localizable;

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public void setMultiple(
		UnsafeSupplier<Boolean, Exception> multipleUnsafeSupplier) {

		try {
			multiple = multipleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean multiple;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName(UnsafeSupplier<String, Exception> nameUnsafeSupplier) {
		try {
			name = nameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String name;

	public Option[] getOptions() {
		return options;
	}

	public void setOptions(Option[] options) {
		this.options = options;
	}

	public void setOptions(
		UnsafeSupplier<Option[], Exception> optionsUnsafeSupplier) {

		try {
			options = optionsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Option[] options;

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public void setPlaceholder(
		UnsafeSupplier<String, Exception> placeholderUnsafeSupplier) {

		try {
			placeholder = placeholderUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String placeholder;

	public String getPredefinedValue() {
		return predefinedValue;
	}

	public void setPredefinedValue(String predefinedValue) {
		this.predefinedValue = predefinedValue;
	}

	public void setPredefinedValue(
		UnsafeSupplier<String, Exception> predefinedValueUnsafeSupplier) {

		try {
			predefinedValue = predefinedValueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String predefinedValue;

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setReadOnly(
		UnsafeSupplier<Boolean, Exception> readOnlyUnsafeSupplier) {

		try {
			readOnly = readOnlyUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean readOnly;

	public Boolean getRepeatable() {
		return repeatable;
	}

	public void setRepeatable(Boolean repeatable) {
		this.repeatable = repeatable;
	}

	public void setRepeatable(
		UnsafeSupplier<Boolean, Exception> repeatableUnsafeSupplier) {

		try {
			repeatable = repeatableUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean repeatable;

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public void setRequired(
		UnsafeSupplier<Boolean, Exception> requiredUnsafeSupplier) {

		try {
			required = requiredUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean required;

	public Boolean getShowAsSwitcher() {
		return showAsSwitcher;
	}

	public void setShowAsSwitcher(Boolean showAsSwitcher) {
		this.showAsSwitcher = showAsSwitcher;
	}

	public void setShowAsSwitcher(
		UnsafeSupplier<Boolean, Exception> showAsSwitcherUnsafeSupplier) {

		try {
			showAsSwitcher = showAsSwitcherUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean showAsSwitcher;

	public Boolean getShowLabel() {
		return showLabel;
	}

	public void setShowLabel(Boolean showLabel) {
		this.showLabel = showLabel;
	}

	public void setShowLabel(
		UnsafeSupplier<Boolean, Exception> showLabelUnsafeSupplier) {

		try {
			showLabel = showLabelUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Boolean showLabel;

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setStyle(
		UnsafeSupplier<String, Exception> styleUnsafeSupplier) {

		try {
			style = styleUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String style;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setText(UnsafeSupplier<String, Exception> textUnsafeSupplier) {
		try {
			text = textUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String text;

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public void setTooltip(
		UnsafeSupplier<String, Exception> tooltipUnsafeSupplier) {

		try {
			tooltip = tooltipUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String tooltip;

	public Validation getValidation() {
		return validation;
	}

	public void setValidation(Validation validation) {
		this.validation = validation;
	}

	public void setValidation(
		UnsafeSupplier<Validation, Exception> validationUnsafeSupplier) {

		try {
			validation = validationUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Validation validation;

}