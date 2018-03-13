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

package com.liferay.dynamic.data.mapping.service;

import aQute.bnd.annotation.ProviderType;

import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.jsonwebservice.JSONWebService;
import com.liferay.portal.kernel.security.access.control.AccessControlled;
import com.liferay.portal.kernel.service.BaseService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.spring.osgi.OSGiBeanProperties;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides the remote service interface for DDMFormInstance. Methods of this
 * service are expected to have security checks based on the propagated JAAS
 * credentials because this service can be accessed remotely.
 *
 * @author Brian Wing Shun Chan
 * @see DDMFormInstanceServiceUtil
 * @see com.liferay.dynamic.data.mapping.service.base.DDMFormInstanceServiceBaseImpl
 * @see com.liferay.dynamic.data.mapping.service.impl.DDMFormInstanceServiceImpl
 * @generated
 */
@AccessControlled
@JSONWebService
@OSGiBeanProperties(property =  {
	"json.web.service.context.name=ddm", "json.web.service.context.path=DDMFormInstance"}, service = DDMFormInstanceService.class)
@ProviderType
@Transactional(isolation = Isolation.PORTAL, rollbackFor =  {
	PortalException.class, SystemException.class})
public interface DDMFormInstanceService extends BaseService {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link DDMFormInstanceServiceUtil} to access the ddm form instance remote service. Add custom service methods to {@link com.liferay.dynamic.data.mapping.service.impl.DDMFormInstanceServiceImpl} and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public DDMFormInstance addFormInstance(long groupId, long ddmStructureId,
		Map<Locale, java.lang.String> nameMap,
		Map<Locale, java.lang.String> descriptionMap,
		DDMFormValues settingsDDMFormValues, ServiceContext serviceContext)
		throws PortalException;

	public void deleteFormInstance(long ddmFormInstanceId)
		throws PortalException;

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public DDMFormInstance fetchFormInstance(long ddmFormInstanceId)
		throws PortalException;

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public DDMFormInstance getFormInstance(long ddmFormInstanceId)
		throws PortalException;

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<DDMFormInstance> getFormInstances(long[] groupIds);

	/**
	* Returns the OSGi service identifier.
	*
	* @return the OSGi service identifier
	*/
	public java.lang.String getOSGiServiceIdentifier();

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<DDMFormInstance> search(long companyId, long groupId,
		java.lang.String keywords, int start, int end,
		OrderByComparator<DDMFormInstance> orderByComparator);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<DDMFormInstance> search(long companyId, long groupId,
		java.lang.String[] names, java.lang.String[] descriptions,
		boolean andOperator, int start, int end,
		OrderByComparator<DDMFormInstance> orderByComparator);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int searchCount(long companyId, long groupId,
		java.lang.String keywords);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int searchCount(long companyId, long groupId,
		java.lang.String[] names, java.lang.String[] descriptions,
		boolean andOperator);

	/**
	* Updates the the record set's settings.
	*
	* @param formInstanceId the primary key of the form instance
	* @param settingsDDMFormValues the record set's settings. For more
	information see <code>DDMFormValues</code> in the
	<code>dynamic.data.mapping.api</code> module.
	* @return the record set
	* @throws PortalException if a portal exception occurred
	*/
	public DDMFormInstance updateFormInstance(long formInstanceId,
		DDMFormValues settingsDDMFormValues) throws PortalException;

	public DDMFormInstance updateFormInstance(long ddmFormInstanceId,
		long ddmStructureId, Map<Locale, java.lang.String> nameMap,
		Map<Locale, java.lang.String> descriptionMap,
		DDMFormValues settingsDDMFormValues, ServiceContext serviceContext)
		throws PortalException;
}