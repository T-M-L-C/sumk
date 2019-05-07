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
package org.yx.http.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.yx.annotation.Bean;
import org.yx.annotation.http.SumkServlet;
import org.yx.common.GsonHelper;
import org.yx.conf.AppInfo;
import org.yx.http.HttpActionHolder;
import org.yx.http.kit.InnerHttpUtil;
import org.yx.rpc.RpcActionHolder;
import org.yx.util.StringUtil;
import org.yx.util.secury.MD5Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Bean
@SumkServlet(value = { "/_sumk_acts" }, loadOnStartup = -1)
public class ActInfomation extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		InnerHttpUtil.noCache(resp);
		resp.setContentType("text/html;charset=UTF-8");
		String md5 = AppInfo.get("sumk.acts.md5", "61C72B1CE5858D83C90BA7B5B1096697");
		String sign = req.getParameter("sign");
		String mode = req.getParameter("mode");
		try {
			if (sign == null || !md5.equals(MD5Utils.encrypt(sign.getBytes())) || StringUtil.isEmpty(mode)) {
				return;
			}
		} catch (Exception e) {
		}
		GsonBuilder builder = GsonHelper.builder("sumk.acts");
		boolean pretty = false;
		if ("1".equals(req.getParameter("pretty"))) {
			builder.setPrettyPrinting();
			pretty = true;
		}
		Gson gson = builder.create();
		if (mode.equals("http")) {
			List<Map<String, Object>> list = HttpActionHolder.infos();
			write(resp, gson.toJson(list), pretty);
			return;
		}
		if (mode.equals("rpc")) {
			List<Map<String, Object>> list = RpcActionHolder.infos();
			write(resp, gson.toJson(list), pretty);
			return;
		}

	}

	private void write(HttpServletResponse resp, String msg, boolean pretty) throws IOException {
		if (pretty) {
			msg = msg.replace("\n", "<br/>").replace(" ", "&nbsp;");
		}
		resp.getWriter().write(msg);
	}
}
