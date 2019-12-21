/**
 * Copyright (C) 2016 - 2030 youtongluan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yx.http;

import javax.servlet.http.HttpServletRequest;

public interface ActParser {

	String parse(HttpServletRequest req);

	static final ActParser pathActParser = req -> {
		String path = req.getPathInfo();
		if (path == null) {
			return null;
		}
		while (path.endsWith("/") && path.length() > 0) {
			path = path.substring(0, path.length() - 1);
		}
		while (path.startsWith("/") && path.length() > 0) {
			path = path.substring(1);
		}
		return path.replace('/', '.');
	};

	static final ActParser paramterActParser = req -> {
		return req.getParameter("act");
	};
}
