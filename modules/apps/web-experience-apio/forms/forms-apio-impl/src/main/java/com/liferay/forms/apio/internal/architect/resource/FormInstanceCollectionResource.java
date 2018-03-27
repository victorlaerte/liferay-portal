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

import com.liferay.apio.architect.pagination.PageItems;
import com.liferay.apio.architect.pagination.Pagination;
import com.liferay.apio.architect.representor.Representor;
import com.liferay.apio.architect.resource.NestedCollectionResource;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormContextProviderHelper;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderingContext;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.forms.apio.architect.identifier.FormInstanceIdentifier;
import com.liferay.forms.apio.architect.identifier.StructureIdentifier;
import com.liferay.forms.apio.internal.architect.FormContext;
import com.liferay.forms.apio.internal.architect.form.FormContextForm;
import com.liferay.portal.apio.architect.context.auth.MockPermissions;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.site.apio.architect.identifier.WebSiteIdentifier;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides the information necessary to expose FormInstance resources through
 * a web API. The resources are mapped from the internal model {@code
 * DDMFormInstance}.
 * @author Victor Oliveira
 */
@Component(immediate = true)
public class FormInstanceCollectionResource
	implements NestedCollectionResource<DDMFormInstance, Long,
		FormInstanceIdentifier, Long, WebSiteIdentifier> {

	@Override
	public NestedCollectionRoutes<DDMFormInstance, Long> collectionRoutes(
		NestedCollectionRoutes.Builder<DDMFormInstance, Long> builder) {

		return builder.addGetter(
			this::_getPageItems, Company.class
		).build();
	}

	@Override
	public String getName() {
		return "form-instance";
	}

	@Override
	public ItemRoutes<DDMFormInstance, Long> itemRoutes(
		ItemRoutes.Builder<DDMFormInstance, Long> builder) {

		return builder.addGetter(
			this::_getFormInstance
		).addUpdater(
			this::_evaluateContext, MockPermissions::validPermission, FormContextForm::buildForm
		).build();
	}

	@Override
	public Representor<DDMFormInstance, Long> representor(
		Representor.Builder<DDMFormInstance, Long> builder) {

		return builder.types(
			"FormInstance"
		).identifier(
			DDMFormInstance::getFormInstanceId
		).addBidirectionalModel(
			"webSite", "form-instance", WebSiteIdentifier.class,
			DDMFormInstance::getGroupId
		).addDate(
			"createDate", DDMFormInstance::getCreateDate
		).addDate(
			"modifiedDate", DDMFormInstance::getModifiedDate
		).addDate(
			"lastPublishDate", DDMFormInstance::getLastPublishDate
		).addLocalizedString(
			"description",
			(ddmFormInstance, language) -> ddmFormInstance.getDescription(
				language.getPreferredLocale())
		).addLocalizedString(
			"name",
			(ddmFormInstance, language) -> ddmFormInstance.getName(
				language.getPreferredLocale())
		).addString(
			"userName", DDMFormInstance::getUserName
		).addString(
			"settings", DDMFormInstance::getSettings
		).addString(
			"versionUserName", DDMFormInstance::getVersionUserName
		).addString(
			"version", DDMFormInstance::getVersion
		).addLinkedModel(
			"structure", StructureIdentifier.class,
			DDMFormInstance::getStructureId
		).build();
	}

	private DDMFormInstance _evaluateContext(
		Long formInstanceId, FormContextForm formContextForm) {

		try {

			System.out.println(formContextForm.getSerializedFormContext());

			DDMFormRenderingContext ddmFormRenderingContext =
				new DDMFormRenderingContext();

			ddmFormRenderingContext.setLocale(LocaleUtil.fromLanguageId(formContextForm.getLanguageId()));
			ddmFormRenderingContext.setPortletNamespace(formContextForm.getPortletNamespace());

			List<Object> ddmFormPagesTemplateContext =
				_ddmFormContextProviderHelper.createDDMFormPagesTemplateContext(
					ddmFormRenderingContext, formContextForm.getPortletNamespace(), formContextForm.getPortletNamespace());

			return _ddmFormInstanceService.getFormInstance(formInstanceId);
		}
		catch (Exception pe) {
			throw  new ServerErrorException(500, pe);
		}
	}

	private DDMFormInstance _getFormInstance(Long formInstanceId) {
		try {
			return _ddmFormInstanceService.getFormInstance(formInstanceId);
		}
		catch (PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	private PageItems<DDMFormInstance> _getPageItems(
		Pagination pagination, Long groupId, Company company) {

		List<DDMFormInstance> ddmFormInstances =
			_ddmFormInstanceService.getFormInstances(
				company.getCompanyId(), groupId, pagination.getStartPosition(),
				pagination.getEndPosition());

		int count = _ddmFormInstanceService.getFormInstancesCount(
			company.getCompanyId(), groupId);

		return new PageItems<>(ddmFormInstances, count);
	}

	@Reference
	private DDMFormInstanceService _ddmFormInstanceService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private DDMFormContextProviderHelper _ddmFormContextProviderHelper;

}