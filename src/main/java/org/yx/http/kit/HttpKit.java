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
package org.yx.http.kit;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.yx.common.ActStatis;

public interface HttpKit {

	String getType(HttpServletRequest req);

	Charset charset(HttpServletRequest req);

	void error(HttpServletResponse resp, int code, String errorMsg, Charset charset) throws IOException;

	void error(HttpServletResponse resp, int httpStatus, int code, String errorMsg, Charset charset) throws IOException;

	void noCache(HttpServletResponse resp);

	void act(String act, long time, boolean isSuccess);

	ActStatis actStatis();

}