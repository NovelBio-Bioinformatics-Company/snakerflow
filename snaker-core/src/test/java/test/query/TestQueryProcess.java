package test.query;

import org.junit.Test;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.model.PageModel;

import test.TestSnakerBase;

/**
 * 流程定义查询测试
 * @author yuqs
 * @since 1.0
 */
public class TestQueryProcess extends TestSnakerBase {
	@Test
	public void test() {
		System.out.println(engine.process().getProcesss(null));
		System.out.println(engine.process().getProcesss(new PageModel().bePageable(), 
				new QueryFilter().setName("subprocess1")));
		System.out.println(engine.process().getProcessByVersion("subprocess1", 0));
		System.out.println(engine.process().getProcessByName("subprocess1"));
	}
}
