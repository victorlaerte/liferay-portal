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

package com.liferay.forms.apio.internal.architect.form;

import com.liferay.apio.architect.form.Form;

/**
 * @author Paulo Cruz
 */
public class FormInstanceRecordUpdaterForm implements FormInstanceRecordForm {

	/**
	 * Builds a {@code Form} that generates
	 * {@code FormInstanceRecordUpdaterForm} depending on the HTTP body.
	 *
	 * @param  formBuilder the {@code Form} builder
	 * @return a form instance record form
	 */
	public static Form<FormInstanceRecordUpdaterForm> buildForm(
		Form.Builder<FormInstanceRecordUpdaterForm> formBuilder) {

		String title = "The form instance record form";

		String description =
			"This form can be used to update a form instance record";

		return formBuilder.title(
			__ -> title
		).description(
			__ -> description
		).constructor(
			FormInstanceRecordUpdaterForm::new
		).addRequiredString(
			"fieldValues", FormInstanceRecordUpdaterForm::_setFieldValues
		).addRequiredLong(
			"formInstanceRecordId",
			FormInstanceRecordUpdaterForm::_setFormInstanceRecordId
		).addRequiredString(
			"formInstanceRecordVersion",
			FormInstanceRecordUpdaterForm::_setFormInstanceRecordVersion
		).addRequiredString(
			"formInstanceVersion", FormInstanceRecordUpdaterForm::_setFormInstanceVersion
		).build();
	}

	public String getFieldValues() {
		return _fieldValues;
	}

	public long getFormInstanceRecordId() {
		return _formInstanceRecordId;
	}

	public String getFormInstanceRecordVersion() {
		return _formInstanceRecordVersion;
	}

	public String getFormInstanceVersion() {
		return _formInstanceVersion;
	}

	private void _setFieldValues(String formValues) {
		_fieldValues = formValues;
	}

	private void _setFormInstanceRecordId(long formInstanceRecordId) {
		_formInstanceRecordId = formInstanceRecordId;
	}

	private void _setFormInstanceRecordVersion(String formInstanceRecordVersion) {
		_formInstanceRecordVersion = formInstanceRecordVersion;
	}

	private void _setFormInstanceVersion(String formInstanceVersion) {
		_formInstanceVersion = formInstanceVersion;
	}

	private String _fieldValues;
	private long _formInstanceRecordId;
	private String _formInstanceRecordVersion;
	private String _formInstanceVersion;

}