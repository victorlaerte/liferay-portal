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

package com.liferay.forms.apio.internal.architect.resource;

import com.liferay.apio.architect.language.Language;
import com.liferay.apio.architect.pagination.PageItems;
import com.liferay.apio.architect.pagination.Pagination;
import com.liferay.apio.architect.representor.Representor;
import com.liferay.apio.architect.resource.NestedCollectionResource;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.forms.apio.architect.identifier.FormInstanceIdentifier;
import com.liferay.forms.apio.architect.identifier.FormInstanceRecordIdentifier;
import com.liferay.forms.apio.internal.architect.FormInstanceRecordServiceContext;
import com.liferay.forms.apio.internal.architect.form.FormInstanceRecordForm;
import com.liferay.forms.apio.internal.architect.helper.FormInstanceRecordResourceHelper;
import com.liferay.portal.apio.architect.context.auth.MockPermissions;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
			this::_addFormInstanceRecord, Language.class,
			FormInstanceRecordServiceContext.class,
			MockPermissions::validPermission,
			FormInstanceRecordForm::buildForm
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
			this::_updateFormInstanceRecord, Language.class,
			FormInstanceRecordServiceContext.class,
			MockPermissions::validPermission,
			FormInstanceRecordForm::buildForm
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
			"form-instance", "form-instance-record",
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
			"fieldValues", FormInstanceRecordResourceHelper::getFieldValuesJSON
		).build();
	}

	private DDMFormInstanceRecord _addFormInstanceRecord(
		Long formInstanceId,
		FormInstanceRecordForm formInstanceRecordForm,
		Language language,
		FormInstanceRecordServiceContext formInstanceRecordServiceContext) {

		try {
			DDMFormInstance ddmFormInstance =
				_ddmFormInstanceService.getFormInstance(formInstanceId);

			DDMStructure ddmStructure = ddmFormInstance.getStructure();

			DDMFormValues ddmFormValues =
				FormInstanceRecordResourceHelper.getDDMFormValues(
					formInstanceRecordForm.getFieldValues(),
					ddmStructure.getDDMForm(), language);

			ServiceContext serviceContext =
				formInstanceRecordServiceContext.getServiceContext();

			if(formInstanceRecordForm.isDraft()) {
				_setServiceContextAsDraft(serviceContext);
			}

			return _ddmFormInstanceRecordService.addFormInstanceRecord(
				ddmFormInstance.getGroupId(),
				ddmFormInstance.getFormInstanceId(), ddmFormValues,
				formInstanceRecordServiceContext.getServiceContext());
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private DDMFormInstanceRecord _getFormInstanceRecord(
		Long formInstanceRecordId) {

		try {
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
					formInstanceId, WorkflowConstants.STATUS_ANY,
					pagination.getStartPosition(), pagination.getEndPosition(),
					null);

			int count =
				_ddmFormInstanceRecordService.getFormInstanceRecordsCount(
					formInstanceId);

			return new PageItems<>(ddmFormInstances, count);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private void _setServiceContextAsDraft(ServiceContext serviceContext) {
		serviceContext.setAttribute("status", WorkflowConstants.STATUS_DRAFT);
		serviceContext.setAttribute("validateDDMFormValues", Boolean.FALSE);
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);
	}

	private DDMFormInstanceRecord _updateFormInstanceRecord(
		Long formInstanceRecordId,
		FormInstanceRecordForm formInstanceRecordForm,
		Language language,
		FormInstanceRecordServiceContext formInstanceRecordServiceContext) {

		try {
			DDMFormInstanceRecord ddmFormInstanceRecord =
				_getFormInstanceRecord(formInstanceRecordId);

			DDMFormInstance ddmFormInstance =
				ddmFormInstanceRecord.getFormInstance();

			DDMStructure ddmStructure = ddmFormInstance.getStructure();

			DDMFormValues ddmFormValues =
				FormInstanceRecordResourceHelper.getDDMFormValues(
					formInstanceRecordForm.getFieldValues(),
					ddmStructure.getDDMForm(), language);

			ServiceContext serviceContext =
				formInstanceRecordServiceContext.getServiceContext();

			if(formInstanceRecordForm.isDraft()) {
				_setServiceContextAsDraft(serviceContext);
			}

			return _ddmFormInstanceRecordService.updateFormInstanceRecord(
				formInstanceRecordId, false, ddmFormValues, serviceContext);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Reference
	private DDMFormInstanceRecordService _ddmFormInstanceRecordService;

	@Reference
	private DDMFormInstanceService _ddmFormInstanceService;

}