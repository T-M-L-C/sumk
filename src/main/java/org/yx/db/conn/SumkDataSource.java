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
package org.yx.db.conn;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.yx.db.DBType;
import org.yx.exception.SumkException;

public class SumkDataSource implements DataSource {
	private final String name;
	private final DBType type;
	private final DataSource proxy;
	private boolean enable = true;

	public SumkDataSource(String name, DBType type, DataSource ds) {
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
		this.proxy = Objects.requireNonNull(ds);
	}

	public String getName() {
		return name;
	}

	public DBType getType() {
		return type;
	}

	@Override
	public SumkConnection getConnection() throws SQLException {
		return new SumkConnection(proxy.getConnection(), this);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return proxy.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		proxy.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		proxy.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return proxy.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return proxy.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface == this.getClass()) {
			return iface.cast(this);
		}
		if (iface.isInstance(proxy)) {
			return iface.cast(proxy);
		}
		throw new SumkException(-234345, this.getClass().getSimpleName() + " does not wrap " + iface.getName());
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface == this.getClass() || iface.isInstance(proxy);
	}

	@Override
	public String toString() {
		return new StringBuilder("ds[").append(name).append("-")
				.append(type.name().startsWith("READ") ? "READ" : type.name()).append("]").toString();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return new SumkConnection(proxy.getConnection(username, password), this);
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

}
