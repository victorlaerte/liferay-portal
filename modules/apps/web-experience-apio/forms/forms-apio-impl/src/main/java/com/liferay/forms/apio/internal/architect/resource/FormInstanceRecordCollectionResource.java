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

package com.liferay.forms.apio.internal.architect.resource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liferay.apio.architect.credentials.Credentials;
import com.liferay.apio.architect.language.Language;
import com.liferay.apio.architect.pagination.PageItems;
import com.liferay.apio.architect.pagination.Pagination;
import com.liferay.apio.architect.representor.Representor;
import com.liferay.apio.architect.resource.NestedCollectionResource;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.forms.apio.architect.identifier.FormInstanceIdentifier;
import com.liferay.forms.apio.architect.identifier.FormInstanceRecordIdentifier;
import com.liferay.forms.apio.internal.architect.FormFieldValue;
import com.liferay.forms.apio.internal.architect.form.FormInstanceRecordForm;
import com.liferay.portal.apio.architect.context.auth.MockPermissions;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.ServerErrorException;
import javax.xml.ws.Service;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the information necessary to expose FormInstanceRecord resources
 * through a web API. The resources are mapped from the internal model {@code
 * DDMFormInstanceRecord}.
 * @author Paulo Cruz
 */
@Component(immediate = true)
public class FormInstanceRecordCollectionResource
	implements NestedCollectionResource<DDMFormInstanceRecord, Long,
		FormInstanceRecordIdentifier, Long, FormInstanceIdentifier> {

	@Override
	public NestedCollectionRoutes<DDMFormInstanceRecord, Long> collectionRoutes(
		NestedCollectionRoutes.Builder<DDMFormInstanceRecord, Long> builder) {

		return builder.addGetter(
			this::_getPageItems
		).addCreator(
			this::_addFormInstanceRecord, ServiceContext.class,
			MockPermissions::validPermission, FormInstanceRecordForm::buildForm
		).build();
	}

	@Override
	public String getName() {
		return "form-instance-record";
	}

	@Override
	public ItemRoutes<DDMFormInstanceRecord, Long> itemRoutes(
		ItemRoutes.Builder<DDMFormInstanceRecord, Long> builder) {

		return builder.addGetter(
			this::_getFormInstanceRecord
		).addUpdater(
			this::_updateFormInstanceRecord, ServiceContext.class,
			this::_validateUpdatePermission, FormInstanceRecordForm::buildForm
		).build();
	}

	@Override
	public Representor<DDMFormInstanceRecord, Long> representor(
		Representor.Builder<DDMFormInstanceRecord, Long> builder) {

		return builder.types(
			"FormInstanceRecord"
		).identifier(
			DDMFormInstanceRecord::getFormInstanceRecordId
		).addBidirectionalModel(
			"form-instance",
			"form-instance-record",
			FormInstanceIdentifier.class,
			DDMFormInstanceRecord::getFormInstanceId
		).addDate(
			"createDate", DDMFormInstanceRecord::getCreateDate
		).addDate(
			"modifiedDate", DDMFormInstanceRecord::getModifiedDate
		).addDate(
			"lastPublishDate", DDMFormInstanceRecord::getLastPublishDate
		).addNumber(
			"companyId", DDMFormInstanceRecord::getCompanyId
		).addNumber(
			"groupId", DDMFormInstanceRecord::getGroupId
		).addNumber(
			"userId", DDMFormInstanceRecord::getUserId
		).addNumber(
			"versionUserId", DDMFormInstanceRecord::getVersionUserId
		).addString(
			"userName", DDMFormInstanceRecord::getUserName
		).addString(
			"versionUserName", DDMFormInstanceRecord::getVersionUserName
		).addString(
			"version", DDMFormInstanceRecord::getVersion
		).addLocalizedString(
			"fieldValues", this::_getFieldValues
		).build();
	}

	private DDMFormInstanceRecord _getFormInstanceRecord(
		Long formInstanceRecordId) {

		try {
			DDMFormInstanceRecord record =
				_ddmFormInstanceRecordService.getFormInstanceRecord(
					formInstanceRecordId);

			return _ddmFormInstanceRecordService.getFormInstanceRecord(
				formInstanceRecordId);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private PageItems<DDMFormInstanceRecord> _getPageItems(
		Pagination pagination, Long formInstanceId) {

		try {
			List<DDMFormInstanceRecord> ddmFormInstances =
				_ddmFormInstanceRecordService.getFormInstanceRecords(
					formInstanceId);

			int count =
				_ddmFormInstanceRecordService.getFormInstanceRecordsCount(
					formInstanceId);

			return new PageItems<>(ddmFormInstances, count);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private String _getFieldValues(DDMFormInstanceRecord ddmFormInstanceRecord,
								   Language language) {
		try {
			Gson gson = new Gson();

			List<DDMFormFieldValue> ddmFormFieldValues =
				ddmFormInstanceRecord.getDDMFormValues().getDDMFormFieldValues();

			List<FormFieldValue> formFieldValues = new ArrayList<>();

			for (DDMFormFieldValue ddmFormFieldValue : ddmFormFieldValues) {
				String instanceId = ddmFormFieldValue.getInstanceId();
				String name = ddmFormFieldValue.getName();
				String value = ddmFormFieldValue.getValue()
					.getString(language.getPreferredLocale());

				formFieldValues.add(new FormFieldValue(instanceId, name, value));
			}

			return gson.toJson(formFieldValues);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private DDMFormInstanceRecord _addFormInstanceRecord(
		Long formInstanceId, FormInstanceRecordForm formInstanceRecordForm,
		ServiceContext serviceContext) {

		try {
			DDMFormInstance ddmFormInstance =
				_ddmFormInstanceService.getFormInstance(formInstanceId);

			DDMFormValues ddmFormValues =
				_getDDMFormValues(formInstanceRecordForm,
					ddmFormInstance.getStructure().getDDMForm());

			return _ddmFormInstanceRecordService.addFormInstanceRecord(
				ddmFormInstance.getGroupId(), ddmFormInstance.getFormInstanceId(),
				ddmFormValues, serviceContext);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private DDMFormInstanceRecord _updateFormInstanceRecord(
		Long formInstanceId, FormInstanceRecordForm formInstanceRecordForm,
		ServiceContext serviceContext) {

		try {
			DDMFormInstance ddmFormInstance =
				_ddmFormInstanceService.getFormInstance(formInstanceId);

			DDMFormValues ddmFormValues =
				_getDDMFormValues(formInstanceRecordForm,
					ddmFormInstance.getStructure().getDDMForm());

			// TODO Major version check
			return _ddmFormInstanceRecordService.updateFormInstanceRecord(
				formInstanceRecordForm.getFormInstanceRecordId(), true,
				ddmFormValues, null);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private DDMFormValues _getDDMFormValues(
		FormInstanceRecordForm formInstanceRecordForm, DDMForm ddmForm) {

		Gson gson = new Gson();
		Type listType = new TypeToken<ArrayList<FormFieldValue>>(){}.getType();

		ArrayList<FormFieldValue> formFieldValues =
			gson.fromJson(formInstanceRecordForm.getFieldValues(), listType);

		DDMFormValues ddmFormValues = new DDMFormValues(ddmForm);

		for (FormFieldValue formFieldValue : formFieldValues) {
			DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();
			ddmFormFieldValue.setInstanceId(formFieldValue.getInstanceId());
			ddmFormFieldValue.setName(formFieldValue.getName());
			ddmFormFieldValue.setValue(
				new UnlocalizedValue(formFieldValue.getValue()));

			ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
		}

		return ddmFormValues;
	}

	private Boolean _validateUpdatePermission(
		Credentials credentials, Long formInstanceRecordId) {

		try {
			User currentUser = _userService.getCurrentUser();

			DDMFormInstanceRecord ddmFormInstanceRecord =
				_ddmFormInstanceRecordService.getFormInstanceRecord(
					formInstanceRecordId.longValue());

			// TODO Review rules to this permission checker
			return currentUser.getUserId() == ddmFormInstanceRecord.getUserId();
		}
		catch(PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Reference
	private DDMFormInstanceService _ddmFormInstanceService;

	@Reference
	private DDMFormInstanceRecordService _ddmFormInstanceRecordService;

	@Reference
	private UserService _userService;


}