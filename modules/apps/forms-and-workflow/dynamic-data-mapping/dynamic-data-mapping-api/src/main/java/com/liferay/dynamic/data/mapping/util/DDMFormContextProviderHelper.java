package com.liferay.dynamic.data.mapping.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Victor Oliveira
 */
public interface DDMFormContextProviderHelper {

	public List<Object> createDDMFormPagesTemplateContext(
		HttpServletRequest request, HttpServletResponse response)
		throws Exception;
}
