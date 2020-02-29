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
package org.yx.bean;

import java.lang.annotation.Annotation;

import org.yx.annotation.Box;
import org.yx.common.AopExcutor;
import org.yx.db.exec.DBTransaction;
import org.yx.exception.SumkException;

public class AopExcutorFactory {
	public static AopExcutor create(Integer key) {
		Annotation[] annotations = AopMetaHolder.get(key);
		if (annotations != null && annotations.length > 0) {
			for (Annotation a : annotations) {
				if (Box.class.isInstance(a)) {
					Box b = (Box) a;
					return new AopExcutor(new DBTransaction(b.value(), b.dbType(), b.transaction()));
				}
			}
		}
		throw new SumkException(-3451435, "不支持aop");
	}
}
