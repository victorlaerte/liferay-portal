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

package com.liferay.forms.apio.internal.architect.provider;

import com.liferay.apio.architect.provider.Provider;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormContextProviderHelper;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderingContext;
import com.liferay.forms.apio.internal.architect.FormContext;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.ServerErrorException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Victor Oliveira
 */
@Component(immediate = true)
public class FormContextProvider implements Provider<FormContext> {

	@Override
	public FormContext createContext(HttpServletRequest httpServletRequest) {
		try {
			String languageId = LanguageUtil.getLanguageId(httpServletRequest);

			Locale locale = LocaleUtil.fromLanguageId(languageId);

			String portletNamespace = ParamUtil.getString(
				httpServletRequest, "portletNamespace");

			String serializedFormContext = ParamUtil.getString(
				httpServletRequest, "serializedFormContext");

			DDMFormRenderingContext ddmFormRenderingContext =
				createDDMFormRenderingContext(
					httpServletRequest, locale, portletNamespace);

			List<Object> ddmFormPagesTemplateContext =
				_ddmFormContextProviderHelper.createDDMFormPagesTemplateContext(
					ddmFormRenderingContext, serializedFormContext, languageId);

			return new FormContext(
				languageId, portletNamespace, ddmFormPagesTemplateContext);

		}
		catch (Exception e) {
			throw new ServerErrorException(500, e);
		}
	}

	protected DDMFormRenderingContext createDDMFormRenderingContext(
		HttpServletRequest request, Locale locale, String portletNamespace) {

		DDMFormRenderingContext ddmFormRenderingContext =
			new DDMFormRenderingContext();

		ddmFormRenderingContext.setHttpServletRequest(request);
		ddmFormRenderingContext.setLocale(locale);
		ddmFormRenderingContext.setPortletNamespace(portletNamespace);

		return ddmFormRenderingContext;
	}

	@Reference
	private DDMFormContextProviderHelper _ddmFormContextProviderHelper;

}