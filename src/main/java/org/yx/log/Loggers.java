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
package org.yx.log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.yx.conf.AppInfo;

public class Loggers {
	public static final String ROOT = "root";
	private LogLevel DEFAULT_LEVEL = LogLevel.INFO;
	private Map<String, LogLevel> levelMap = new HashMap<>();

	private Map<String, SumkLogger> map = new ConcurrentHashMap<>();
	private static Loggers[] INSTS = new Loggers[0];
	private final String name;

	private Loggers(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Loggers [name=" + name + "]";
	}

	static {

		AppInfo.addObserver(info -> {
			try {
				String temp = AppInfo.get("sumk.log.level", "info");
				String[] levelStrs = temp.replace('，', ',').split(",");
				Map<String, LogLevel> newLevels = new HashMap<>();
				for (String levelStr : levelStrs) {
					levelStr = levelStr.trim();
					if (levelStr.isEmpty()) {
						continue;
					}
					String[] levelNamePair = levelStr.split(":");
					switch (levelNamePair.length) {
					case 1:
						newLevels.put(ROOT, LogLevel.valueOf(levelNamePair[0].trim().toUpperCase()));
						break;
					case 2:
						newLevels.put(levelNamePair[0].trim(), LogLevel.valueOf(levelNamePair[1].trim().toUpperCase()));
						break;
					default:
						System.err.println(levelStr + " is not valid name:level format");
					}
				}
				for (Loggers logger : INSTS) {
					logger.resetLevel(newLevels);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static synchronized Loggers create(String name) {
		Loggers l = new Loggers(name);
		Loggers[] rss = new Loggers[INSTS.length + 1];
		System.arraycopy(INSTS, 0, rss, 0, INSTS.length);
		rss[rss.length - 1] = l;
		INSTS = rss;
		System.out.println("loggers insts:" + Arrays.toString(INSTS));
		return l;
	}

	private LogLevel level(String logName) {
		int index = 0;
		while (logName.length() > 0) {
			LogLevel level = levelMap.get(logName);
			if (level != null) {
				return level;
			}
			index = logName.lastIndexOf(".");
			if (index < 1) {
				break;
			}
			logName = logName.substring(0, index);
		}
		return DEFAULT_LEVEL;
	}

	public void setDefaultLevel(LogLevel level) {
		if (level != null) {
			DEFAULT_LEVEL = level;
		}
	}

	public SumkLogger get(String name) {
		return map.get(name);
	}

	public SumkLogger putIfAbsent(String name, SumkLogger log) {
		return map.putIfAbsent(name, log);
	}

	public LogLevel getLevel(SumkLogger log) {
		String name = log.getName();
		if (name == null || name.isEmpty()) {
			return DEFAULT_LEVEL;
		}
		return level(name);
	}

	public synchronized void resetLevel(Map<String, LogLevel> newLevels) {
		LogLevel defaultLevel = newLevels.remove(ROOT);
		if (defaultLevel != null) {
			DEFAULT_LEVEL = defaultLevel;
		}
		levelMap = newLevels;
	}
}
