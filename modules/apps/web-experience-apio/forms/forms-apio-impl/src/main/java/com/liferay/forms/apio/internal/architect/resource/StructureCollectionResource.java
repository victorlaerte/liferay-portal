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
import com.liferay.apio.architect.resource.ItemResource;
import com.liferay.apio.architect.routes.CollectionRoutes;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.forms.apio.architect.identifier.StructureIdentifier;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

import javax.ws.rs.ServerErrorException;

/**
 * Provides the information necessary to expose Structure resources through
 * a web API. The resources are mapped from the internal model {@code
 * DDMStructure}.
 * @author Paulo Cruz
 * @review
 */
@Component(immediate = true)
public class StructureCollectionResource implements
	ItemResource<DDMStructure, Long, StructureIdentifier> {

	@Override
	public String getName() {
		return "structures";
	}

	@Override
	public ItemRoutes<DDMStructure, Long> itemRoutes(
		ItemRoutes.Builder<DDMStructure, Long> builder) {

		return builder.addGetter(
			this::_getStructure
		).build();
	}

	@Override
	public Representor<DDMStructure, Long> representor(
		Representor.Builder<DDMStructure, Long> builder) {

		return builder.types(
			"Structure"
		).identifier(
			DDMStructure::getStructureId
		).addDate(
			"createDate", DDMStructure::getCreateDate
		).addDate(
			"modifiedDate", DDMStructure::getCreateDate
		).addDate(
			"lastPublishDate", DDMStructure::getLastPublishDate
		).addNumber(
			"companyId", DDMStructure::getCompanyId
		).addNumber(
			"groupId", DDMStructure::getGroupId
		).addNumber(
			"userId", DDMStructure::getUserId
		).addString(
			"userName", DDMStructure::getUserName
		).addNumber(
			"versionUserId", DDMStructure::getVersionUserId
		).addString(
			"versionUserName", DDMStructure::getVersionUserName
		).addString(
			"version", DDMStructure::getVersion
		).addNumber(
			"parentStructureId", DDMStructure::getParentStructureId
		).addNumber(
			"classNameId", DDMStructure::getClassNameId
		).addString(
			"structureKey", DDMStructure::getStructureKey
		).addString(
			"storageType", DDMStructure::getStorageType
		).addNumber(
			"type", DDMStructure::getType
		).addLocalizedString(
			"description",
			(ddmStructure, language) -> ddmStructure.getDescription(
				language.getPreferredLocale())
		).addLocalizedString(
			"name",
			(ddmStructure, language) -> ddmStructure.getName(
				language.getPreferredLocale())
		).addString(
			"definition", DDMStructure::getDefinition
		).build();
	}

	private DDMStructure _getStructure(Long structureId) {
		try {
			return _ddmStructureLocalService.getStructure(structureId);
		}
		catch(PortalException pe) {
			throw new ServerErrorException(500, pe);
		}
	}

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

}
