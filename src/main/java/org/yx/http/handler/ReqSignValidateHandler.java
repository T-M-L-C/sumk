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

import org.yx.annotation.http.Web;
import org.yx.conf.AppInfo;
import org.yx.exception.HttpException;
import org.yx.http.HttpCiphers;
import org.yx.log.Log;
import org.yx.util.StringUtil;

public class ReqSignValidateHandler implements HttpHandler {

	private byte[] salt;

	public ReqSignValidateHandler() {

		String saltStr = AppInfo.get("sumk.sign.salt");
		if (StringUtil.isNotEmpty(saltStr)) {
			salt = saltStr.getBytes();
		}
	}

	@Override
	public boolean accept(Web web) {
		return web.sign();
	}

	@Override
	public boolean handle(WebContext ctx) throws Exception {
		String sign = ctx.sign();
		if (StringUtil.isEmpty(sign)) {
			HttpException.throwException(this.getClass(), "签名不能为空");
		}
		byte[] bs = ctx.getDataInByteArray();
		if (bs == null) {
			return false;
		}
		if (salt != null) {
			byte[] temp = new byte[bs.length + salt.length];
			System.arraycopy(bs, 0, temp, 0, bs.length);
			System.arraycopy(salt, 0, temp, bs.length, salt.length);
			bs = temp;
		}
		String sign1 = HttpCiphers.getSigner().sign(bs, ctx.httpRequest());
		if (!sign.equals(sign1)) {
			Log.get("sumk.http").debug("client sign:{},computed is:{}", sign, sign1);
			HttpException.throwException(this.getClass(), "签名验证错误");
		}
		return false;
	}

}
