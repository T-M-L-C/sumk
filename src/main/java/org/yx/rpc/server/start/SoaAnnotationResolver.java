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
package org.yx.rpc.server.start;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.yx.annotation.rpc.Soa;
import org.yx.annotation.rpc.SoaClass;
import org.yx.asm.ArgPojos;
import org.yx.asm.AsmUtils;
import org.yx.asm.MethodParamInfo;
import org.yx.bean.IOC;
import org.yx.bean.Loader;
import org.yx.common.matcher.BooleanMatcher;
import org.yx.common.matcher.Matchers;
import org.yx.conf.AppInfo;
import org.yx.exception.SimpleSumkException;
import org.yx.exception.SumkException;
import org.yx.log.Log;
import org.yx.log.Logs;
import org.yx.rpc.RpcActionNode;
import org.yx.rpc.RpcActions;
import org.yx.util.StringUtil;
import org.yx.validate.ParamFactory;

public class SoaAnnotationResolver {
	private SoaClassResolver soaClassResolver;
	private SoaNameResolver nameResolver;
	private Predicate<String> exclude = BooleanMatcher.FALSE;

	public SoaAnnotationResolver() {
		nameResolver = Loader.newInstanceFromAppKey("sumk.rpc.name.resolver");
		if (nameResolver == null) {
			nameResolver = new SoaNameResolverImpl();
		}

		soaClassResolver = Loader.newInstanceFromAppKey("sumk.rpc.name.soaclass.resolver");
		if (soaClassResolver == null) {
			soaClassResolver = new SoaClassResolverImpl();
		}

		String patterns = AppInfo.get("sumk.rpc.server.exclude", null);
		if (patterns != null) {
			this.exclude = Matchers.createWildcardMatcher(patterns, 1);
			Logs.rpc().debug("soa server exclude:{}", this.exclude);
		}
	}

	private void parseSoaClass(Map<Method, String> map, Class<?> targetClass, SoaClass sc, Class<?> refer)
			throws NoSuchMethodException, SecurityException {
		String pre = this.soaClassResolver.solvePrefix(targetClass, refer);
		if (pre == null) {
			return;
		}
		Method[] methods = refer.getMethods();
		for (Method m : methods) {
			Method methodInTarget = targetClass.getMethod(m.getName(), m.getParameterTypes());
			if (!m.getReturnType().isAssignableFrom(methodInTarget.getReturnType())) {
				SumkException.throwException(234324, targetClass.getName() + "." + methodInTarget.getName() + "的返回值类型是"
						+ methodInTarget.getReturnType().getName() + ",期待的类型是" + m.getReturnType().getName());
			}
			map.put(methodInTarget, pre);
		}
	}

	private void parseSoa(Map<Method, String> map, Class<?> clz) {
		Method[] methods = clz.getMethods();
		for (final Method m : methods) {
			if (AsmUtils.isFilted(m.getName())) {
				continue;
			}
			if (m.getAnnotation(Soa.class) == null || map.containsKey(m)) {
				continue;
			}
			if (AsmUtils.notPublicOnly(m.getModifiers())) {
				Log.get("sumk.asm").error("$$$ {}.{} has bad modifiers, maybe static or private", clz.getName(),
						m.getName());
				continue;
			}
			map.putIfAbsent(m, "");
		}
	}

	public void resolve(Object bean) throws Exception {

		Class<?> clz = IOC.getTargetClassOfBean(bean);
		if (this.exclude.test(clz.getName())) {
			return;
		}
		Map<Method, String> map = new HashMap<>();
		Class<?> refer = null;
		SoaClass sc = clz.getAnnotation(SoaClass.class);
		if (sc != null) {
			refer = this.soaClassResolver.getRefer(clz, sc);
			this.parseSoaClass(map, clz, sc, refer);
		}

		if (sc == null || refer != clz) {
			this.parseSoa(map, clz);
		}
		if (map.isEmpty()) {
			return;
		}

		List<MethodParamInfo> mpInfos = AsmUtils.buildMethodInfos(new ArrayList<>(map.keySet()));
		for (MethodParamInfo info : mpInfos) {
			Method m = info.getMethod();
			String prefix = map.get(m);
			Soa act = m.getAnnotation(Soa.class);
			List<String> soaNames = StringUtil.isEmpty(prefix) ? nameResolver.solve(clz, m, act)
					: Collections.singletonList(prefix + m.getName());
			if (soaNames == null || soaNames.isEmpty()) {
				continue;
			}

			int toplimit = act != null && act.toplimit() > 0 ? act.toplimit()
					: AppInfo.getInt("sumk.rpc.thread.priority.default", 100000);
			boolean publish = act != null ? act.publish() : true;
			RpcActionNode node = new RpcActionNode(bean, m, ArgPojos.create(info), info.getArgNames(),
					ParamFactory.create(m), toplimit, publish);

			for (String soaName : soaNames) {
				if (soaName == null || soaName.isEmpty()) {
					continue;
				}
				if (RpcActions.getActionNode(soaName) != null) {
					RpcActionNode node0 = RpcActions.getActionNode(soaName);
					Logs.rpc().error(soaName + " already existed -- {}.{},{}.{}", node0.getDeclaringClass().getName(),
							node0.getMethodName(), m.getDeclaringClass().getName(), m.getName());
					throw new SimpleSumkException(1242436, soaName + " already existed");
				}
				RpcActions.putActNode(soaName, node);
			}
		}
	}

}
