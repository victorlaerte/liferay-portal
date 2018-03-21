package com.liferay.forms.apio.internal.architect.provider;

import com.liferay.apio.architect.provider.Provider;
import com.liferay.dynamic.data.mapping.util.DDMFormContextProviderHelper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Victor Oliveira
 */
@Component(immediate = true)
public class DDMFormContentProvider implements Provider<List<Object>> {

	@Override
	public List<Object> createContext(HttpServletRequest httpServletRequest) {
		try {
			return _ddmFormContextProviderHelper.createDDMFormPagesTemplateContext(httpServletRequest, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Reference
	private DDMFormContextProviderHelper _ddmFormContextProviderHelper;
}
