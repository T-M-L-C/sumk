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

import org.yx.annotation.Bean;
import org.yx.http.handler.HttpHandler;
import org.yx.listener.ClassLoaderFactorysBean;

@Bean
public class RestHandlerFactorysBean extends ClassLoaderFactorysBean<HttpHandler> {

	public RestHandlerFactorysBean() {
		super("org.yx.http.handler", "http.intf", "org.yx.http.handler");
	}

	@Override
	public Class<HttpHandler> acceptClass() {
		return HttpHandler.class;
	}

}
