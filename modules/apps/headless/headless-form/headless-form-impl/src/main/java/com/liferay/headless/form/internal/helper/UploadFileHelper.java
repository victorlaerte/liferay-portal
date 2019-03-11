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

package com.liferay.headless.form.internal.helper;

import com.google.gson.Gson;

import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.headless.form.dto.v1_0.MediaForm;
import com.liferay.headless.form.internal.model.FileEntryValue;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.BadRequestException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Javier Gamarra
 * @author Paulo Cruz
 */
@Component(immediate = true, service = UploadFileHelper.class)
public class UploadFileHelper {

	public void linkFiles(
		List<DDMFormField> ddmFormFields,
		List<DDMFormFieldValue> ddmFormFieldValues) {

		Stream<DDMFormField> ddmFormFieldsStream = ddmFormFields.stream();

		ddmFormFieldsStream.filter(
			formField -> Objects.equals(formField.getType(), "document_library")
		).map(
			field -> _findField(field.getName(), ddmFormFieldValues)
		).forEach(
			optional -> {
				if (optional.isPresent()) {
					try {
						DDMFormFieldValue ddmFormFieldValue = optional.get();

						_setFileEntryAsFormFieldValue(ddmFormFieldValue);
					}
					catch (Exception e) {
						throw new BadRequestException(e);
					}
				}
			}
		);
	}

	public FileEntry uploadFile(
			DDMFormInstance ddmFormInstance, MultipartBody multipartBody)
		throws IOException, PortalException {

		MediaForm mediaForm = multipartBody.getValueAsInstance(
			"mediaForm", MediaForm.class);

		Long folderIdOptional = mediaForm.getFolderId();

		BinaryFile binaryFile = multipartBody.getBinaryFile("file");

		return _dlAppService.addFileEntry(
			ddmFormInstance.getGroupId(), folderIdOptional,
			binaryFile.getFileName(), binaryFile.getContentType(),
			mediaForm.getTitle(), mediaForm.getDescription(), null,
			binaryFile.getInputStream(), binaryFile.getSize(),
			new ServiceContext());
	}

	private Value _calculateDDMFormFieldValue(
		String jsonValue, DDMFormField ddmFormField) {

		if (ddmFormField.isLocalizable()) {
			LocalizedValue localizedValue = new LocalizedValue();

			localizedValue.addString(
				localizedValue.getDefaultLocale(), jsonValue);

			return localizedValue;
		}

		return new UnlocalizedValue(jsonValue);
	}

	private Long _extractFileEntryId(DDMFormFieldValue ddmFormFieldValue) {
		Value value = ddmFormFieldValue.getValue();

		return Stream.of(
			value.getValues()
		).map(
			Map::values
		).flatMap(
			Collection::stream
		).findFirst(
		).map(
			fileEntryUrl -> fileEntryUrl.substring(
				fileEntryUrl.lastIndexOf("/") + 1)
		).map(
			Long::valueOf
		).orElse(
			null
		);
	}

	private Optional<DDMFormFieldValue> _findField(
		String formFieldName, List<DDMFormFieldValue> ddmFormFieldValues) {

		Stream<DDMFormFieldValue> ddmFormFieldValuesStream =
			ddmFormFieldValues.stream();

		return ddmFormFieldValuesStream.filter(
			value -> formFieldName.equals(value.getName())
		).findFirst();
	}

	private void _setFileEntryAsFormFieldValue(
			DDMFormFieldValue ddmFormFieldValue)
		throws PortalException {

		Gson gson = new Gson();

		FileEntry fileEntry = _dlAppService.getFileEntry(
			_extractFileEntryId(ddmFormFieldValue));

		String json = gson.toJson(
			new FileEntryValue(
				fileEntry.getFileEntryId(), fileEntry.getGroupId(),
				fileEntry.getTitle(), fileEntry.getMimeType(),
				fileEntry.getUuid(), fileEntry.getVersion()));

		ddmFormFieldValue.setValue(
			_calculateDDMFormFieldValue(
				json, ddmFormFieldValue.getDDMFormField()));
	}

	@Reference
	private DLAppService _dlAppService;

}