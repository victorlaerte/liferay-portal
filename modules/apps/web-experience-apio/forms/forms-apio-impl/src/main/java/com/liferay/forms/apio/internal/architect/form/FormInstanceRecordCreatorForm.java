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
public class FormInstanceRecordCreatorForm implements FormInstanceRecordForm {

	/**
	 * Builds a {@code Form} that generates
	 * {@code FormInstanceRecordCreatorForm} depending on the HTTP body.
	 *
	 * @param  formBuilder the {@code Form} builder
	 * @return a form instance record form
	 */
	public static Form<FormInstanceRecordCreatorForm> buildForm(
		Form.Builder<FormInstanceRecordCreatorForm> formBuilder) {

		String title = "The form instance record form";

		String description =
			"This form can be used to create a form instance record";

		return formBuilder.title(
			__ -> title
		).description(
			__ -> description
		).constructor(
			FormInstanceRecordCreatorForm::new
		).addRequiredString(
			"fieldValues", FormInstanceRecordCreatorForm::_setFieldValues
		).addRequiredBoolean(
			"isDraft", FormInstanceRecordCreatorForm::_setDraft
		).build();
	}

	public String getFieldValues() {
		return _fieldValues;
	}

	public boolean isDraft() {
		return _draft;
	}

	private void _setDraft(boolean draft) {
		_draft = draft;
	}

	private void _setFieldValues(String formValues) {
		_fieldValues = formValues;
	}

	private boolean _draft;
	private String _fieldValues;

}