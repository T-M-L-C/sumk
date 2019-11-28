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
package org.yx.sumk.batis;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.managed.ManagedTransaction;
import org.yx.bean.IOC;
import org.yx.common.SumkLogs;
import org.yx.conf.AppInfo;
import org.yx.conf.LocalMultiResourceLoaderSupplier;
import org.yx.conf.MultiResourceLoader;
import org.yx.exception.SumkException;
import org.yx.log.Log;
import org.yx.util.Asserts;

public class SqlSessionFactory {

	private static Map<String, SqlSessionFactory> factoryMap = new ConcurrentHashMap<>();

	private Configuration configuration;
	private String db;

	static SqlSessionFactory create(String dbName) throws Exception {
		SqlSessionFactory sessionFactory = new SqlSessionFactory();
		sessionFactory.db = dbName;
		List<ConfigurationFactory> confFactorys = IOC.getBeans(ConfigurationFactory.class);
		if (confFactorys != null && confFactorys.size() > 0) {
			for (ConfigurationFactory f : confFactorys) {
				Configuration conf = f.create(dbName);
				if (conf != null) {
					sessionFactory.configuration = conf;
					return sessionFactory.sqlParse();
				}
			}
		}
		sessionFactory.configuration = new Configuration();
		return sessionFactory.sqlParse();
	}

	private static Supplier<MultiResourceLoader> resourceLoader = new LocalMultiResourceLoaderSupplier(
			AppInfo.get("sumk.db.mybatis.path", AppInfo.CLASSPATH_URL_PREFIX + "batis"));

	public static void resourceLoader(Supplier<MultiResourceLoader> resourceLoader) {
		SqlSessionFactory.resourceLoader = resourceLoader;
	}

	public static Supplier<MultiResourceLoader> resourceLoader() {
		return resourceLoader;
	}

	public static SqlSessionFactory get(String dbName) {
		try {
			Asserts.hasText(dbName, "db name can not be empty");
			dbName = dbName.trim();
			SqlSessionFactory factory = factoryMap.get(dbName);
			if (factory != null) {
				return factory;
			}
			synchronized (SqlSessionFactory.class) {
				factory = factoryMap.get(dbName);
				if (factory != null) {
					return factory;
				}
				factory = SqlSessionFactory.create(dbName);
				factoryMap.put(dbName, factory);
			}
			return factory;
		} catch (Exception e) {
			Log.printStack(SumkLogs.SQL_ERROR, e);
			SumkException.throwException(100234325, "create factory failed");
			return null;
		}
	}

	void destroy() {

	}

	public static void reload(String dbName) throws Exception {
		Asserts.hasText(dbName, "db name can not be empty");
		dbName = dbName.trim();
		SqlSessionFactory factory = factoryMap.get(dbName);
		if (factory == null) {
			return;
		}
		factory = SqlSessionFactory.create(dbName);
		SqlSessionFactory old = factoryMap.put(dbName, factory);
		old.destroy();
	}

	public SqlSession session(Connection conn) {

		Transaction transaction = new ManagedTransaction(conn, false);
		SimpleExecutor excutor = new SimpleExecutor(configuration, transaction);
		return new DefaultSqlSession(configuration, excutor);
	}

	SqlSessionFactory sqlParse() throws Exception {
		Map<String, byte[]> sqls = resourceLoader.get().openResources(db);
		Set<Map.Entry<String, byte[]>> entries = sqls.entrySet();
		for (Map.Entry<String, byte[]> entry : entries) {
			byte[] bs = entry.getValue();
			XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(new ByteArrayInputStream(bs), configuration,
					entry.getKey(), configuration.getSqlFragments());
			xmlMapperBuilder.parse();
		}
		return this;
	}

}
