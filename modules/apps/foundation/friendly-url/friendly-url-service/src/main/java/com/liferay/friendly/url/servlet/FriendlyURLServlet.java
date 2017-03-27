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

package com.liferay.friendly.url.servlet;

import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.NoSuchLayoutException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutFriendlyURL;
import com.liferay.portal.kernel.model.LayoutFriendlyURLComposite;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutFriendlyURLLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.servlet.PortalMessages;
import com.liferay.portal.kernel.servlet.ServletContextPool;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PortalInstances;

import java.io.IOException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Wing Shun Chan
 * @author Jorge Ferrer
 * @author Shuyang Zhou
 */
public class FriendlyURLServlet extends HttpServlet {

	public Redirect getRedirect(HttpServletRequest request, String path)
		throws PortalException {

		if (path.length() <= 1) {
			return new Redirect();
		}

		// Group friendly URL

		String friendlyURL = path;

		int pos = path.indexOf(CharPool.SLASH, 1);

		if (pos != -1) {
			friendlyURL = path.substring(0, pos);
		}

		long companyId = PortalInstances.getCompanyId(request);

		Group group = groupLocalService.fetchFriendlyURLGroup(
			companyId, friendlyURL);

		if (group == null) {
			String screenName = friendlyURL.substring(1);

			if (_user || !Validator.isNumber(screenName)) {
				User user = userLocalService.fetchUserByScreenName(
					companyId, screenName);

				if (user != null) {
					group = user.getGroup();
				}
				else if (_log.isWarnEnabled()) {
					_log.warn("No user exists with friendly URL " + screenName);
				}
			}
			else {
				long groupId = GetterUtil.getLong(screenName);

				group = groupLocalService.fetchGroup(groupId);

				if (group == null) {
					if (_log.isDebugEnabled()) {
						_log.debug(
							"No group exists with friendly URL " + groupId +
								". Try fetching by screen name instead.");
					}

					User user = userLocalService.fetchUserByScreenName(
						companyId, screenName);

					if (user != null) {
						group = user.getGroup();
					}
					else if (_log.isWarnEnabled()) {
						_log.warn(
							"No user or group exists with friendly URL " +
								groupId);
					}
				}
			}
		}

		if (group == null) {
			StringBundler sb = new StringBundler(5);

			sb.append("{companyId=");
			sb.append(companyId);
			sb.append(", friendlyURL=");
			sb.append(friendlyURL);
			sb.append("}");

			throw new NoSuchGroupException(sb.toString());
		}

		// Layout friendly URL

		friendlyURL = null;

		if ((pos != -1) && ((pos + 1) != path.length())) {
			friendlyURL = path.substring(pos);
		}
		else {
			request.setAttribute(
				WebKeys.REDIRECT_TO_DEFAULT_LAYOUT, Boolean.TRUE);
		}

		Map<String, Object> requestContext = new HashMap<>();

		requestContext.put("request", request);

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			serviceContext = ServiceContextFactory.getInstance(request);

			ServiceContextThreadLocal.pushServiceContext(serviceContext);
		}

		Map<String, String[]> params = request.getParameterMap();

		try {
			LayoutFriendlyURLComposite layoutFriendlyURLComposite =
				portal.getLayoutFriendlyURLComposite(
					group.getGroupId(), _private, friendlyURL, params,
					requestContext);

			Layout layout = layoutFriendlyURLComposite.getLayout();

			request.setAttribute(WebKeys.LAYOUT, layout);

			Locale locale = portal.getLocale(request);

			String layoutFriendlyURLCompositeFriendlyURL =
				layoutFriendlyURLComposite.getFriendlyURL();

			if (Validator.isNull(layoutFriendlyURLCompositeFriendlyURL)) {
				layoutFriendlyURLCompositeFriendlyURL = layout.getFriendlyURL(
					locale);
			}

			pos = layoutFriendlyURLCompositeFriendlyURL.indexOf(
				Portal.FRIENDLY_URL_SEPARATOR);

			if (pos != 0) {
				if (pos != -1) {
					layoutFriendlyURLCompositeFriendlyURL =
						layoutFriendlyURLCompositeFriendlyURL.substring(0, pos);
				}

				String i18nLanguageId = (String)request.getAttribute(
					WebKeys.I18N_LANGUAGE_ID);

				if ((Validator.isNotNull(i18nLanguageId) &&
					 !LanguageUtil.isAvailableLocale(
						 group.getGroupId(), i18nLanguageId)) ||
					!StringUtil.equalsIgnoreCase(
						layoutFriendlyURLCompositeFriendlyURL,
						layout.getFriendlyURL(locale))) {

					Locale originalLocale = setAlternativeLayoutFriendlyURL(
						request, layout, layoutFriendlyURLCompositeFriendlyURL);

					String redirect = portal.getLocalizedFriendlyURL(
						request, layout, locale, originalLocale);

					Boolean forcePermanentRedirect = Boolean.TRUE;

					if (Validator.isNull(i18nLanguageId)) {
						forcePermanentRedirect = Boolean.FALSE;
					}

					return new Redirect(
						redirect, Boolean.TRUE, forcePermanentRedirect);
				}
			}
		}
		catch (NoSuchLayoutException nsle) {
			List<Layout> layouts = layoutLocalService.getLayouts(
				group.getGroupId(), _private,
				LayoutConstants.DEFAULT_PARENT_LAYOUT_ID);

			for (Layout layout : layouts) {
				if (layout.matches(request, friendlyURL)) {
					String redirect = portal.getLayoutActualURL(
						layout, Portal.PATH_MAIN);

					return new Redirect(redirect);
				}
			}

			throw nsle;
		}

		String actualURL = portal.getActualURL(
			group.getGroupId(), _private, Portal.PATH_MAIN, friendlyURL, params,
			requestContext);

		return new Redirect(actualURL);
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		_private = GetterUtil.getBoolean(
			servletConfig.getInitParameter("servlet.init.private"));

		String proxyPath = portal.getPathProxy();

		_user = GetterUtil.getBoolean(
			servletConfig.getInitParameter("servlet.init.user"));

		if (_private) {
			if (_user) {
				_friendlyURLPathPrefix = portal.getPathFriendlyURLPrivateUser();
			}
			else {
				_friendlyURLPathPrefix =
					portal.getPathFriendlyURLPrivateGroup();
			}
		}
		else {
			_friendlyURLPathPrefix = portal.getPathFriendlyURLPublic();
		}

		_pathInfoOffset = _friendlyURLPathPrefix.length() - proxyPath.length();
	}

	@Override
	public void service(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		// Do not set the entire full main path. See LEP-456.

		String pathInfo = getPathInfo(request);

		Redirect redirect = null;

		try {
			redirect = getRedirect(request, pathInfo);

			if (request.getAttribute(WebKeys.LAST_PATH) == null) {
				request.setAttribute(
					WebKeys.LAST_PATH, getLastPath(request, pathInfo));
			}
		}
		catch (PortalException pe) {
			if (_log.isWarnEnabled()) {
				_log.warn(pe);
			}

			if (pe instanceof NoSuchGroupException ||
				pe instanceof NoSuchLayoutException) {

				portal.sendError(
					HttpServletResponse.SC_NOT_FOUND, pe, request, response);

				return;
			}
		}

		if (redirect == null) {
			redirect = new Redirect();
		}

		if (_log.isDebugEnabled()) {
			_log.debug("Redirect " + redirect.getPath());
		}

		if (redirect.isValidForward()) {
			ServletContext servletContext = getServletContext();

			RequestDispatcher requestDispatcher =
				servletContext.getRequestDispatcher(redirect.getPath());

			if (requestDispatcher != null) {
				requestDispatcher.forward(request, response);
			}
		}
		else {
			if (redirect.isPermanent()) {
				response.setHeader("Location", redirect.getPath());
				response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			}
			else {
				response.sendRedirect(redirect.getPath());
			}
		}
	}

	public static class Redirect {

		public Redirect() {
			this(Portal.PATH_MAIN);
		}

		public Redirect(String path) {
			this(path, false, false);
		}

		public Redirect(String path, boolean force, boolean permanent) {
			_path = path;
			_force = force;
			_permanent = permanent;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Redirect)) {
				return false;
			}

			Redirect redirect = (Redirect)obj;

			if (Objects.equals(getPath(), redirect.getPath()) &&
				(isForce() == redirect.isForce()) &&
				(isPermanent() == redirect.isPermanent())) {

				return true;
			}
			else {
				return false;
			}
		}

		public String getPath() {
			if (Validator.isNull(_path)) {
				return Portal.PATH_MAIN;
			}

			return _path;
		}

		@Override
		public int hashCode() {
			int hash = HashUtil.hash(0, _path);

			hash = HashUtil.hash(hash, _force);
			hash = HashUtil.hash(hash, _permanent);

			return hash;
		}

		public boolean isForce() {
			return _force;
		}

		public boolean isPermanent() {
			return _permanent;
		}

		public boolean isValidForward() {
			String path = getPath();

			if (path.charAt(0) != CharPool.SLASH) {
				return false;
			}

			if (isForce()) {
				return false;
			}

			return true;
		}

		private final boolean _force;
		private final String _path;
		private final boolean _permanent;

	}

	@Activate
	@Modified
	protected void activate(final Map<String, Object> properties) {
		ServletConfig servletConfig = new ServletConfig() {

			@Override
			public String getInitParameter(String name) {
				String value = GetterUtil.getString(properties.get(name), null);

				return value;
			}

			@Override
			public Enumeration<String> getInitParameterNames() {
				Set<String> keys = properties.keySet();

				Vector<String> keyNames = new Vector<>(keys);

				return keyNames.elements();
			}

			@Override
			public ServletContext getServletContext() {
				return ServletContextPool.get(portal.getServletContextName());
			}

			@Override
			public String getServletName() {
				String servletName = GetterUtil.getString(
					properties.get("osgi.http.whiteboard.servlet.name"));

				return servletName;
			}

		};

		try {
			init(servletConfig);
		}
		catch (ServletException se) {
			_log.error("Unable to initialize friendly URL servlet", se);
		}
	}

	/**
	 * @deprecated As of 1.0.0, with no direct replacement
	 */
	@Deprecated
	protected String getFriendlyURL(String pathInfo) {
		String friendlyURL = _friendlyURLPathPrefix;

		if (Validator.isNotNull(pathInfo)) {
			friendlyURL = friendlyURL.concat(pathInfo);
		}

		return friendlyURL;
	}

	protected LastPath getLastPath(
		HttpServletRequest request, String pathInfo) {

		String lifecycle = ParamUtil.getString(request, "p_p_lifecycle");

		if (lifecycle.equals("1")) {
			return new LastPath(_friendlyURLPathPrefix, pathInfo);
		}
		else {
			return new LastPath(
				_friendlyURLPathPrefix, pathInfo,
				HttpUtil.parameterMapToString(request.getParameterMap()));
		}
	}

	protected String getPathInfo(HttpServletRequest request) {
		String requestURI = request.getRequestURI();

		int pos = requestURI.indexOf(Portal.JSESSIONID);

		if (pos == -1) {
			return requestURI.substring(_pathInfoOffset);
		}

		return requestURI.substring(_pathInfoOffset, pos);
	}

	/**
	 * @deprecated As of 1.0.0, with no direct replacement
	 */
	@Deprecated
	protected Object[] getRedirect(
			HttpServletRequest request, String path, String mainPath,
			Map<String, String[]> params)
		throws Exception {

		Redirect redirect = getRedirect(request, path);

		return new Object[] {redirect.getPath(), redirect.isForce()};
	}

	protected Locale setAlternativeLayoutFriendlyURL(
		HttpServletRequest request, Layout layout, String friendlyURL) {

		List<LayoutFriendlyURL> layoutFriendlyURLs =
			layoutFriendlyURLLocalService.getLayoutFriendlyURLs(
				layout.getPlid(), friendlyURL, 0, 1);

		if (layoutFriendlyURLs.isEmpty()) {
			return null;
		}

		LayoutFriendlyURL layoutFriendlyURL = layoutFriendlyURLs.get(0);

		Locale locale = LocaleUtil.fromLanguageId(
			layoutFriendlyURL.getLanguageId());

		String alternativeLayoutFriendlyURL = portal.getLocalizedFriendlyURL(
			request, layout, locale, locale);

		SessionMessages.add(
			request, "alternativeLayoutFriendlyURL",
			alternativeLayoutFriendlyURL);

		PortalMessages.add(
			request, PortalMessages.KEY_JSP_PATH,
			"/html/common/themes/layout_friendly_url_redirect.jsp");

		return locale;
	}

	@Reference
	protected GroupLocalService groupLocalService;

	@Reference
	protected LayoutFriendlyURLLocalService layoutFriendlyURLLocalService;

	@Reference
	protected LayoutLocalService layoutLocalService;

	@Reference
	protected Portal portal;

	@Reference
	protected UserLocalService userLocalService;

	private static final Log _log = LogFactoryUtil.getLog(
		FriendlyURLServlet.class);

	private String _friendlyURLPathPrefix;
	private int _pathInfoOffset;
	private boolean _private;
	private boolean _user;

}