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

package com.liferay.media.object.apio.internal.form;

import com.liferay.apio.architect.file.BinaryFile;
import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.form.Form.Builder;

/**
 * @author Javier Gamarra
 */
public class MediaObjectCreatorForm {

	public static Form<MediaObjectCreatorForm> buildForm(
		Builder<MediaObjectCreatorForm> formBuilder) {

		return formBuilder.title(
			__ -> "The media object creator form"
		).description(
			__ -> "This form can be used to create a media object"
		).constructor(
			MediaObjectCreatorForm::new
		).addOptionalString(
			"changeLog", MediaObjectCreatorForm::setChangelog
		).addOptionalString(
			"description", MediaObjectCreatorForm::setDescription
		).addRequiredFile(
			"binaryFile", MediaObjectCreatorForm::setBinaryFile
		).addRequiredLong(
			"repositoryId", MediaObjectCreatorForm::setRepositoryId
		).addRequiredString(
			"sourceFileName", MediaObjectCreatorForm::setSourceFileName
		).addRequiredString(
			"title", MediaObjectCreatorForm::setTitle
		).build();
	}

	public BinaryFile getBinaryFile() {
		return _binaryFile;
	}

	public String getChangelog() {
		return _changelog;
	}

	public String getDescription() {
		return _description;
	}

	public long getRepositoryId() {
		return _repositoryId;
	}

	public String getSourceFileName() {
		return _sourceFileName;
	}

	public String getTitle() {
		return _title;
	}

	public void setBinaryFile(BinaryFile binaryFile) {
		_binaryFile = binaryFile;
	}

	public void setChangelog(String changelog) {
		_changelog = changelog;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void setRepositoryId(long repositoryId) {
		_repositoryId = repositoryId;
	}

	public void setSourceFileName(String sourceFileName) {
		_sourceFileName = sourceFileName;
	}

	public void setTitle(String title) {
		_title = title;
	}

	private BinaryFile _binaryFile;
	private String _changelog;
	private String _description;
	private long _repositoryId;
	private String _sourceFileName;
	private String _title;

}