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
import javax.servlet.http.HttpServletResponse;

import org.yx.annotation.Bean;
import org.yx.annotation.http.SumkServlet;
import org.yx.http.handler.HttpActionNode;
import org.yx.http.handler.HttpHandlerChain;
import org.yx.http.handler.WebContext;

@Bean
@SumkServlet(value = { "/rest/*" }, loadOnStartup = 1, appKey = "rest")
public class RestServer extends AbstractHttpServer {

	private static final long serialVersionUID = 7437235491L;

	@Override
	protected void handle(String act, HttpActionNode info, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		if (info.upload != null) {
			log.error(act + " type error.It is not uploader");
			return;
		}

		WebContext wc = new WebContext(act, info, req, resp);
		HttpHandlerChain.inst.handle(wc);
	}
}
