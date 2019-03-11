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

package com.liferay.headless.form.internal.resource.v1_0;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelper;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.headless.form.dto.v1_0.FieldValue;
import com.liferay.headless.form.dto.v1_0.FormRecord;
import com.liferay.headless.form.dto.v1_0.FormRecordForm;
import com.liferay.headless.form.internal.dto.v1_0.util.CreatorUtil;
import com.liferay.headless.form.internal.dto.v1_0.util.FormDocumentUtil;
import com.liferay.headless.form.internal.helper.FetchLatestRecordHelper;
import com.liferay.headless.form.internal.helper.UploadFileHelper;
import com.liferay.headless.form.internal.model.FormFieldValue;
import com.liferay.headless.form.resource.v1_0.FormRecordResource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.Context;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/form-record.properties",
	scope = ServiceScope.PROTOTYPE, service = FormRecordResource.class
)
public class FormRecordResourceImpl extends BaseFormRecordResourceImpl {

	@Override
	public FormRecord getFormFetchLatestDraft(Long formId) throws Exception {
		DDMFormInstance ddmFormInstance =
			_ddmFormInstanceService.getFormInstance(formId);

		return _toFormRecord(
			_fetchLatestRecordVersionHelper.fetchLatestDraftRecord(
				ddmFormInstance, _user));
	}

	@Override
	public Page<FormRecord> getFormFormRecordsPage(
			Long formId, Pagination pagination)
		throws Exception {

		return Page.of(
			transform(
				_ddmFormInstanceRecordService.getFormInstanceRecords(
					formId, WorkflowConstants.STATUS_ANY,
					pagination.getStartPosition(), pagination.getEndPosition(),
					null),
				this::_toFormRecord),
			pagination,
			_ddmFormInstanceRecordService.getFormInstanceRecordsCount(formId));
	}

	@Override
	public FormRecord getFormRecord(Long formRecordId) throws Exception {
		return _toFormRecord(
			_ddmFormInstanceRecordService.getFormInstanceRecord(formRecordId));
	}

	@Override
	public FormRecord postFormFormRecord(
			Long formId, FormRecordForm formRecordForm)
		throws Exception {

		DDMFormInstance ddmFormInstance =
			_ddmFormInstanceService.getFormInstance(formId);

		DDMStructure ddmStructure = ddmFormInstance.getStructure();

		DDMForm ddmForm = ddmStructure.getDDMForm();

		DDMFormValues ddmFormValues = _getDDMFormValues(
			formRecordForm.getFieldValues(), ddmForm,
			contextAcceptLanguage.getPreferredLocale());

		_uploadFileHelper.linkFiles(
			ddmForm.getDDMFormFields(), ddmFormValues.getDDMFormFieldValues());

		ServiceContext formServiceContext = _getServiceContext(
			formRecordForm.getDraft());

		return _toFormRecord(
			_ddmFormInstanceRecordService.addFormInstanceRecord(
				ddmFormInstance.getGroupId(),
				ddmFormInstance.getFormInstanceId(), ddmFormValues,
				formServiceContext));
	}

	@Override
	public FormRecord putFormRecord(
			Long formRecordId, FormRecordForm formRecordForm)
		throws Exception {

		DDMFormInstanceRecord ddmFormInstanceRecord =
			_ddmFormInstanceRecordService.getFormInstanceRecord(formRecordId);

		DDMFormInstance ddmFormInstance =
			ddmFormInstanceRecord.getFormInstance();

		DDMStructure ddmStructure = ddmFormInstance.getStructure();

		DDMForm ddmForm = ddmStructure.getDDMForm();

		DDMFormValues ddmFormValues = _getDDMFormValues(
			formRecordForm.getFieldValues(), ddmForm,
			contextAcceptLanguage.getPreferredLocale());

		_uploadFileHelper.linkFiles(
			ddmForm.getDDMFormFields(), ddmFormValues.getDDMFormFieldValues());

		ServiceContext formServiceContext = _getServiceContext(
			formRecordForm.getDraft());

		return _toFormRecord(
			_ddmFormInstanceRecordService.updateFormInstanceRecord(
				formRecordId, false, ddmFormValues, formServiceContext));
	}

	private DDMFormValues _getDDMFormValues(
		String fieldValues, DDMForm ddmForm, Locale locale) {

		DDMFormValues ddmFormValues = new DDMFormValues(ddmForm);

		ddmFormValues.addAvailableLocale(locale);
		ddmFormValues.setDefaultLocale(locale);

		FormFieldValueListToken formFieldValueListToken =
			new FormFieldValueListToken();

		Map<String, DDMFormField> ddmFormFieldsMap =
			ddmForm.getDDMFormFieldsMap(true);

		Type listType = formFieldValueListToken.getType();

		Gson gson = new Gson();

		List<FormFieldValue> formFieldValues = gson.fromJson(
			fieldValues, listType);

		for (FormFieldValue formFieldValue : formFieldValues) {
			DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();

			ddmFormFieldValue.setName(formFieldValue.name);

			DDMFormField ddmFormField = ddmFormFieldsMap.get(
				formFieldValue.name);

			Value value = _EMPTY_VALUE;

			if (ddmFormField != null) {
				value = Optional.ofNullable(
					formFieldValue.value
				).map(
					this::_toString
				).map(
					stringValue -> _getValue(stringValue, ddmFormField, locale)
				).orElse(
					_EMPTY_VALUE
				);
			}

			_setFieldValue(value, ddmFormValues, ddmFormFieldValue);
		}

		return ddmFormValues;
	}

	private FileEntry _getFileEntryId(String value, DLAppService dlAppService)
		throws PortalException {

		if (!_isJSONObject(value) || !value.contains("uuid")) {
			return null;
		}

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(value);

		String uuid = jsonObject.getString("uuid");
		long groupId = jsonObject.getLong("groupId");

		return dlAppService.getFileEntryByUuidAndGroupId(uuid, groupId);
	}

	private String _getLocalizedString(Value localizedValue) {
		return localizedValue.getString(
			contextAcceptLanguage.getPreferredLocale());
	}

	private ServiceContext _getServiceContext(boolean draft)
		throws PortalException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			DDMFormInstanceRecord.class.getName(), _httpServletRequest);

		if (draft) {
			serviceContext.setAttribute(
				"status", WorkflowConstants.STATUS_DRAFT);
			serviceContext.setAttribute("validateDDMFormValues", Boolean.FALSE);
			serviceContext.setWorkflowAction(
				WorkflowConstants.ACTION_SAVE_DRAFT);
		}
		else {
			serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		}

		return serviceContext;
	}

	private Value _getValue(
		String stringValue, DDMFormField ddmFormField, Locale locale) {

		Value value = null;

		if (ddmFormField.isLocalizable()) {
			value = new LocalizedValue();

			value.addString(locale, stringValue);
		}
		else {
			value = new UnlocalizedValue(stringValue);
		}

		return value;
	}

	private boolean _isJSONObject(String json) throws JSONException {
		if (json.startsWith("{") &&
			(JSONFactoryUtil.createJSONObject(json) != null)) {

			return true;
		}

		return false;
	}

	private void _setFieldValue(
		Value value, DDMFormValues ddmFormValues,
		DDMFormFieldValue ddmFormFieldValue) {

		ddmFormFieldValue.setValue(value);

		ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
	}

	private FieldValue _toFieldValues(DDMFormFieldValue ddmFormFieldValue)
		throws Exception {

		String value = _getLocalizedString(ddmFormFieldValue.getValue());

		FileEntry fileEntry = _getFileEntryId(value, _dlAppService);

		return new FieldValue() {
			{
				if (fileEntry != null) {
					document = FormDocumentUtil.toDocument(
						fileEntry, _dlurlHelper);
					documentId = fileEntry.getFileEntryId();
				}

				name = ddmFormFieldValue.getName();
				value = _getLocalizedString(ddmFormFieldValue.getValue());
			}
		};
	}

	private FormRecord _toFormRecord(
			DDMFormInstanceRecord ddmFormInstanceRecord)
		throws PortalException {

		DDMFormValues ddmFormValues = ddmFormInstanceRecord.getDDMFormValues();

		return new FormRecord() {
			{
				creator = CreatorUtil.toCreator(
					_portal,
					_userLocalService.getUser(
						ddmFormInstanceRecord.getUserId()));
				draft =
					ddmFormInstanceRecord.getStatus() ==
						WorkflowConstants.STATUS_DRAFT;
				dateCreated = ddmFormInstanceRecord.getCreateDate();
				dateModified = ddmFormInstanceRecord.getModifiedDate();
				datePublished = ddmFormInstanceRecord.getLastPublishDate();
				fieldValues = transformToArray(
					ddmFormValues.getDDMFormFieldValues(),
					FormRecordResourceImpl.this::_toFieldValues,
					FieldValue.class);
				id = ddmFormInstanceRecord.getFormInstanceRecordId();
			}
		};
	}

	private String _toString(JsonElement jsonElement) {
		if (jsonElement instanceof JsonPrimitive) {
			JsonPrimitive jsonPrimitive = (JsonPrimitive)jsonElement;

			if (!jsonPrimitive.isJsonNull()) {
				return jsonPrimitive.getAsString();
			}
		}

		return jsonElement.toString();
	}

	private static final Value _EMPTY_VALUE = new UnlocalizedValue(
		(String)null);

	@Reference
	private DDMFormInstanceRecordService _ddmFormInstanceRecordService;

	@Reference
	private DDMFormInstanceService _ddmFormInstanceService;

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private DLURLHelper _dlurlHelper;

	@Reference
	private FetchLatestRecordHelper _fetchLatestRecordVersionHelper;

	@Context
	private HttpServletRequest _httpServletRequest;

	@Reference
	private Portal _portal;

	@Reference
	private UploadFileHelper _uploadFileHelper;

	@Context
	private User _user;

	@Reference
	private UserLocalService _userLocalService;

	private class FormFieldValueListToken
		extends TypeToken<ArrayList<FormFieldValue>> {
	}

}