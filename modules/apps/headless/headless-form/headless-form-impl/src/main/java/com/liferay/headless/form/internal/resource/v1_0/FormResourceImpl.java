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

import static com.liferay.headless.form.internal.dto.v1_0.util.StructureUtil.toFormStructure;

import com.liferay.document.library.util.DLURLHelper;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.headless.form.dto.v1_0.Form;
import com.liferay.headless.form.dto.v1_0.FormDocument;
import com.liferay.headless.form.internal.dto.v1_0.util.CreatorUtil;
import com.liferay.headless.form.internal.dto.v1_0.util.FormDocumentUtil;
import com.liferay.headless.form.internal.helper.UploadFileHelper;
import com.liferay.headless.form.resource.v1_0.FormResource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/form.properties",
	scope = ServiceScope.PROTOTYPE, service = FormResource.class
)
public class FormResourceImpl extends BaseFormResourceImpl {

	@Override
	public Page<Form> getContentSpaceFormsPage(
			Long contentSpaceId, Pagination pagination)
		throws Exception {

		return Page.of(
			transform(
				_ddmFormInstanceService.getFormInstances(
					contextCompany.getCompanyId(), contentSpaceId,
					pagination.getStartPosition(), pagination.getEndPosition()),
				this::_toForm),
			pagination,
			_ddmFormInstanceService.getFormInstancesCount(
				contextCompany.getCompanyId(), contentSpaceId));
	}

	@Override
	public Form getForm(Long formId) throws Exception {
		return _toForm(_ddmFormInstanceService.getFormInstance(formId));
	}

	@Override
	public FormDocument postFormUploadFile(
			Long formId, MultipartBody multipartBody)
		throws Exception {

		DDMFormInstance ddmFormInstance =
			_ddmFormInstanceService.getFormInstance(formId);

		return FormDocumentUtil.toDocument(
			_uploadFileHelper.uploadFile(ddmFormInstance, multipartBody),
			_dlurlHelper);
	}

	private Form _toForm(DDMFormInstance formInstance) throws PortalException {
		if (formInstance == null) {
			return null;
		}

		return new Form() {
			{
				availableLanguages = LocaleUtil.toW3cLanguageIds(
					formInstance.getAvailableLanguageIds());
				creator = CreatorUtil.toCreator(
					_portal,
					_userLocalService.getUser(formInstance.getUserId()));
				dateCreated = formInstance.getCreateDate();
				dateModified = formInstance.getModifiedDate();
				datePublished = formInstance.getLastPublishDate();
				defaultLanguage = formInstance.getDefaultLanguageId();
				description = formInstance.getDescription(
					contextAcceptLanguage.getPreferredLocale());
				id = formInstance.getFormInstanceId();
				name = formInstance.getName(
					contextAcceptLanguage.getPreferredLocale());
				structure = toFormStructure(
					formInstance.getStructure(), _portal, _userLocalService,
					contextAcceptLanguage.getPreferredLocale());
			}
		};
	}

	@Reference
	private DDMFormInstanceService _ddmFormInstanceService;

	@Reference
	private DLURLHelper _dlurlHelper;

	@Reference
	private Portal _portal;

	@Reference
	private UploadFileHelper _uploadFileHelper;

	@Reference
	private UserLocalService _userLocalService;

}