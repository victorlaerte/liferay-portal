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
 * @author Victor Oliveira
 */
public class FormContextForm {

	/**
	 * Builds a {@code Form} that generates {@code FormContextForm}
	 * depending on the HTTP body.
	 *
	 * @param  formBuilder the {@code Form} builder
	 * @return a context form instance
	 */
	public static Form<FormContextForm> buildForm(
		Form.Builder<FormContextForm> formBuilder) {

		String title = "The form instance context form";

		String description =
			"This form context can be used to evaluate a form instance";

		return formBuilder.title(
			__ -> title
		).description(
			__ -> description
		).constructor(
			FormContextForm::new
		).addRequiredString(
			"languageId", FormContextForm::_setLanguageId
		).addRequiredString(
			"fieldValues", FormContextForm::_setFieldValues
		).build();
	}

	public String getFieldValues() {
		return _fieldValues;
	}

	public String getLanguageId() {
		return _languageId;
	}

	private void _setFieldValues(String fieldValues) {
		_fieldValues = fieldValues;
	}

	private void _setLanguageId(String languageId) {
		_languageId = languageId;
	}

	private String _fieldValues;
	private String _languageId;

}