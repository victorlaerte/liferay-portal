/*
 * *
 *  * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *  *
 *  * This library is free software; you can redistribute it and/or modify it under
 *  * the terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *
 */

package com.liferay.forms.apio.internal.architect.form;

import com.liferay.apio.architect.form.Form;

public class FormInstanceRecordForm {

	/**
	 * Builds a {@code Form} that generates {@code FormInstanceRecordForm}
	 * depending on the HTTP body.
	 *
	 * @param  formBuilder the {@code Form} builder
	 * @return a form instance record form
	 * @review
	 */
	public static Form<FormInstanceRecordForm> buildForm(
		Form.Builder<FormInstanceRecordForm> formBuilder) {
		return formBuilder.title(
			__ -> "The form instance record form"
		).description(
			__ -> "This form can be used to create or update a " +
				  "form instance record"
		).constructor(
			FormInstanceRecordForm::new
		).addRequiredString(
			"fieldValues", FormInstanceRecordForm::_setFieldValues
		).addRequiredLong(
			"formInstanceRecordId",
			FormInstanceRecordForm::_setFormInstanceRecordId
		).addRequiredLong(
			"formInstanceRecordVersion",
			FormInstanceRecordForm::_setFormInstanceRecordVersion
		).build();
	}

	public String getFieldValues() {
		return _fieldValues;
	}

	private void _setFieldValues(String formValues) {
		_fieldValues = formValues;
	}

	public long getFormInstanceRecordId() {
		return _formInstanceRecordId;
	}

	private void _setFormInstanceRecordId(long formInstanceRecordId) {
		_formInstanceRecordId = formInstanceRecordId;
	}

	public long getFormInstanceRecordVersion() {
		return _formInstanceRecordVersion;
	}

	private void _setFormInstanceRecordVersion(long formInstanceRecordVersion) {
		_formInstanceRecordVersion = formInstanceRecordVersion;
	}

	private String _fieldValues;
	private long _formInstanceRecordId;
	private long _formInstanceRecordVersion;
}
