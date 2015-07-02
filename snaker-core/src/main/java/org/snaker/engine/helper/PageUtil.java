package org.snaker.engine.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.snaker.engine.model.Datagrid;
import org.snaker.engine.model.PageModel;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.alibaba.fastjson.JSONObject;

/**
 * 将后台结果集封装成前台easyui的工具类.
 * @author novelbio
 *
 */
public class PageUtil {

	/**
	 * 把普通分页结果转换成easyUI需要的形式
	 * 
	 * @param lsParams 记录集合
	 * @return
	 */
	public static <T> Object changeToEasyuiPage(Collection<T> lsParams) {
		return changeToEasyuiPage(lsParams.size(), lsParams);
	}

	/**
	 * 把普通分页结果转换成easyUI需要的形式
	 * 
	 * @param page mongoDB返回的分页结果
	 * @return
	 */
	public static <T> Object changeToEasyuiPage(Page<T> page) {
		return changeToEasyuiPage(page, null);
	}
	
	/**
	 * 把普通分页结果转换成easyUI需要的形式
	 * 
	 * @param page mongoDB返回的分页结果
	 * @param clazz viewModel 对应的class
	 * @return
	 */
	public static <T, K> Object changeToEasyuiPage(Page<T> page, Class<K> clazz) {
		return changeToEasyuiPage(page.getTotalElements(), page.getContent(), clazz);
	}
	
	/**
	 * 把普通分页结果转换成easyUI需要的形式
	 * 
	 * @param Collection 分页结果集
	 * @param clazz viewModel 对应的class
	 * @return
	 */
	public static <T, K> Object changeToEasyuiPage(Collection<T> collection, Class<K> clazz) {
		long total = collection == null ? 0l : collection.size();
		return changeToEasyuiPage(total, collection, clazz);
	}

	/**
	 * 把普通分页结果转换成easyUI需要的形式.构造前台easyui的datagrid所需数据的json对象.  
	 * <br/><b>需注意:分页需要在调用该方法之前实现好</b>
	 * 
	 * @param total 		数据总条数
	 * @param dataList	分页结果数据.会以json形式输出到页面
	 * @return
	 */
	public static <T> Object changeToEasyuiPage(long total, Collection<T> lsParams) {
		return changeToEasyuiPage(total, lsParams, null);
	}
	
	/**
	 * 按照Query和PageModel 查出符合条件的结果,并按分页要求返回json结果.
	 * @param pageModel		分页模型
	 * @param mongoTemplate	数据库操作类
	 * @param query			查询模型
	 * @param queryClazz		数据库查询操作返回的结果类型类.
	 * @return
	 */
	public static <T, K> Object changeToEasyuiPage(PageModel pageModel, MongoTemplate mongoTemplate, Query query, Class<T> queryClazz) {
		return changeToEasyuiPage(pageModel, mongoTemplate, query, queryClazz, null);
	}
	
	/**
	 * 按照Query和PageModel 查出符合条件的结果,并按分页要求返回json结果.
	 * @param pageModel		分页模型
	 * @param mongoTemplate	数据库操作类
	 * @param query			查询模型
	 * @param queryClazz		数据库查询操作返回的结果类型类.
	 * @param viewClazz		要转换的view对象类.<b>如和queryClazz类相同,可以直接写null.</b>
	 * @return
	 */
	public static <T, K> Object changeToEasyuiPage(PageModel pageModel, MongoTemplate mongoTemplate, Query query, Class<T> queryClazz, Class<K> viewClazz) {
		long total = mongoTemplate.count(query, queryClazz);
		List<T> lsData = mongoTemplate.find(query.with(pageModel.bePageable()), queryClazz);
		
		return changeToEasyuiPage(total, lsData, viewClazz);
	}

	/**
	 * 把普通分页结果转换成easyUI需要的形式
	 * @param total		数据总条数(不是lsParams的数据条数)
	 * @param lsParams	当前返回的具体数据
	 * @param viewClazz	需要转换成的viewClazz类.<b>如不需要转换,可直接写null</b>
	 * @return
	 */
	public static <T, K> Object changeToEasyuiPage(long total, Collection<T> lsParams, Class<K> viewClazz) {
		Datagrid datagrid = null;
		if (lsParams == null) {
			datagrid = new Datagrid(0, new ArrayList<>());
		} else if(viewClazz != null){
			List<K> lsK = new ArrayList<>();
			for (Object object : lsParams) {
				K newObject = null;
				try {
					newObject = viewClazz.newInstance();
					BeanUtils.copyProperties(object, newObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
				lsK.add(newObject);
			}
			datagrid = new Datagrid(total, lsK);
		} else {
			datagrid = new Datagrid(total, lsParams);
		}
		return JSONObject.toJSON(datagrid);
	}

	/**
	 * 把普通分页结果转换成easyUI需要的形式
	 * 
	 * @param page mongoDB返回的分页结果
	 * @param clazz viewModel 对就的class
	 * @return
	 */
	public static <T, K> Object copyToView(Collection<T> data, Class<K> clazz) {
		List<K> lsK = new ArrayList<>();
		for (Object object : data) {
			K newObject = null;
			try {
				newObject = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			BeanUtils.copyProperties(object, newObject);
			lsK.add(newObject);
		}
		return JSONObject.toJSON(lsK);
	}

	/**
	 * 直接传入要分页的集合、当前页数、每页显示条数，假分页
	 * 
	 * @param params 要分页的集合
	 * @param pageModel 分页对象
	 * @return
	 */
	public static <T> Object pageList(List<T> params, PageModel pageModel) {
		return pageList(params, pageModel, null);
	}

	/**
	 * 直接传入要分页的集合、当前页数、每页显示条数，假分页
	 * 
	 * @param params 		要分页的集合
	 * @param pageModel 	分页对象
	 * @param clazz view类
	 * @return
	 */
	public static <T, K> Object pageList(List<T> params, PageModel pageModel, Class<K> clazz) {
		Datagrid datagrid = null;
		if (params == null || pageModel == null) {
			datagrid = new Datagrid(0, null);
			return JSONObject.toJSON(datagrid);
		}
		// 当前页
		int intPage = (pageModel.getPage() == 0 ? 1 : pageModel.getPage());
		// 每页显示条数
		int number = (pageModel.getRows() == 0 ? 10 : pageModel.getRows());
		// 每页的开始记录 第一页为1 第二页为number +1
		int start = (intPage - 1) * number;

		int leave = params.size() - start;
		List<T> params1 = new ArrayList<T>();
		if (leave < number) {
			params1 = params.subList(start, start + leave);
		} else {
			params1 = params.subList(start, start + number);
		}
		if (clazz != null) {
			List<K> lsK = new ArrayList<>();
			for (Object object : params1) {
				K newObject = null;
				try {
					newObject = clazz.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				BeanUtils.copyProperties(object, newObject);
				lsK.add(newObject);
			}
			datagrid = new Datagrid(params.size(), lsK);
		} else {
			datagrid = new Datagrid(params.size(), params1);
		}
		return JSONObject.toJSON(datagrid);
	}
	

}
