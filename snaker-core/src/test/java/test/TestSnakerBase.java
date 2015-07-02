/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import org.snaker.engine.IMgmtProcess;
import org.snaker.engine.IMgmtQuery;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.core.SnakerEngineImpl;
import org.snaker.engine.helper.SpringFactoryService;

/**
 * 测试辅助基类，提供execute的递归方法及SnakerEngine实例
 * @author yuqs
 * @since 1.0
 */
public class TestSnakerBase {
	protected static String processId;
	protected static SnakerEngine engine = getEngine();
	protected IMgmtProcess processService = engine.process();
	protected IMgmtQuery queryService = engine.query();
	protected static SnakerEngine getEngine() {
//		return new Configuration().buildSnakerEngine();
		
		return SpringFactoryService.getBean(SnakerEngineImpl.class);
	}
}
