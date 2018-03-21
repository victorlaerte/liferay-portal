package com.liferay.dynamic.data.mapping.form.renderer;

import com.liferay.dynamic.data.mapping.form.evaluator.DDMFormEvaluator;
import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldTypeServicesTracker;
import com.liferay.dynamic.data.mapping.form.renderer.internal.DDMFormPagesTemplateContextFactory;
import com.liferay.dynamic.data.mapping.form.renderer.internal.servlet.DDMFormTemplateContextProcessor;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.util.DDMFormContextProviderHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

/**
 * @author Victor Oliveira
 */
@Component(
	immediate = true, service = DDMFormContextProviderHelper.class
)
public class DDMFormContextProviderHelperImpl implements DDMFormContextProviderHelper {

	protected DDMFormRenderingContext createDDMFormRenderingContext(
		HttpServletRequest request, HttpServletResponse response, Locale locale,
		String portletNamespace) {

		DDMFormRenderingContext ddmFormRenderingContext =
			new DDMFormRenderingContext();

		ddmFormRenderingContext.setHttpServletRequest(request);
		ddmFormRenderingContext.setHttpServletResponse(response);
		ddmFormRenderingContext.setLocale(locale);
		ddmFormRenderingContext.setPortletNamespace(portletNamespace);

		return ddmFormRenderingContext;
	}

	protected DDMFormPagesTemplateContextFactory getDdmFormPagesTemplateContext(
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		String portletNamespace = ParamUtil.getString(
			request, "portletNamespace");

		String languageId = LanguageUtil.getLanguageId(request);

		Locale locale = LocaleUtil.fromLanguageId(languageId);

		DDMFormRenderingContext ddmFormRenderingContext =
			createDDMFormRenderingContext(
				request, response, locale, portletNamespace);

		DDMFormTemplateContextProcessor ddmFormTemplateContextProcessor =
			createDDMFormTemplateContextProcessor(request);

		DDMFormValues ddmFormValues =
			ddmFormTemplateContextProcessor.getDDMFormValues();

		ddmFormRenderingContext.setDDMFormValues(ddmFormValues);

		ddmFormRenderingContext.setGroupId(
			ddmFormTemplateContextProcessor.getGroupId());

		_prepareThreadLocal(locale);

		DDMForm ddmForm = ddmFormTemplateContextProcessor.getDDMForm();

		DDMFormLayout ddmFormLayout =
			ddmFormTemplateContextProcessor.getDDMFormLayout();

		return new DDMFormPagesTemplateContextFactory(
			ddmForm, ddmFormLayout, ddmFormRenderingContext);
	}

	public List<Object> createDDMFormPagesTemplateContext(
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		DDMFormPagesTemplateContextFactory
			ddmFormPagesTemplateContextFactory =
			getDdmFormPagesTemplateContext(request, response);

		ddmFormPagesTemplateContextFactory.setDDMFormEvaluator(
			_ddmFormEvaluator);
		ddmFormPagesTemplateContextFactory.
			setDDMFormFieldTypeServicesTracker(
				_ddmFormFieldTypeServicesTracker);
		ddmFormPagesTemplateContextFactory.setJSONFactory(_jsonFactory);

		return ddmFormPagesTemplateContextFactory.create();
	}

	private static void _prepareThreadLocal(Locale locale)
		throws Exception, PortalException {

		LocaleThreadLocal.setThemeDisplayLocale(locale);
	}

	protected DDMFormTemplateContextProcessor
		createDDMFormTemplateContextProcessor(HttpServletRequest request)
	throws Exception {

		String serializedFormContext = ParamUtil.getString(
			request, "serializedFormContext");

		JSONObject jsonObject = _jsonFactory.createJSONObject(
			serializedFormContext);

		return new DDMFormTemplateContextProcessor(
			jsonObject, ParamUtil.getString(request, "languageId"));
	}

	@Reference
	private DDMFormEvaluator _ddmFormEvaluator;

	@Reference
	private DDMFormFieldTypeServicesTracker _ddmFormFieldTypeServicesTracker;

	@Reference
	private JSONFactory _jsonFactory;

}
