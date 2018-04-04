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

package com.liferay.forms.apio.internal.architect;

import java.util.List;

/**
 * @author Victor Oliveira
 */
public class FormContext {

	public FormContext(
		String languageId, String portletNamespace,
		List<Object> formPagesTemplateContext) {

		_languageId = languageId;
		_portletNamespace = portletNamespace;
		_formPagesTemplateContext = formPagesTemplateContext;
	}

	public List<Object> getFormPagesTemplateContext() {
		return _formPagesTemplateContext;
	}

	public String getLanguageId() {
		return _languageId;
	}

	public String getPortletNamespace() {
		return _portletNamespace;
	}

	private final List<Object> _formPagesTemplateContext;
	private final String _languageId;
	private final String _portletNamespace;

}