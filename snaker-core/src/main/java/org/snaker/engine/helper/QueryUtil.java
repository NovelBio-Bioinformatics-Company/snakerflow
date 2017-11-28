package org.snaker.engine.helper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * 拼接MongodbTemplate查询使用的query对象的工具类
 * 
 * @author novelbio
 */
public class QueryUtil {

	private static final String EMPTY = "";
	private static final String SIGN = "_";
	private static final String RANGE = "range" + SIGN;
	private static final String RANGE_BEGIN = "begin" + SIGN;
	private static final String RANGE_END = "end" + SIGN;
	private static final String IN = "in" + SIGN;
	private static final String EQ = "eq" + SIGN;
	private static final String LIKE = "like" + SIGN;

	private Query query;

	/**
	 * 获取QueryUtil实例
	 * 
	 * @return
	 */
	public static QueryUtil instance() {
		return new QueryUtil();
	}

	/**
	 * 获得组合的query对象
	 * 
	 * @return 最终的query结果
	 */
	public Query build() {
		return this.query;
	}

	/**
	 * 根据输入参数bean填充QueryUtil内部query对象<br>
	 * bean中的属性名格式有:
	 * <ul>
	 * <li><b>{propertyName} </b>: 添加{propertyName}属性%like%查询约束</li>
	 * <li><b>like_{propertyName} </b>: 添加{propertyName}属性%like%查询约束</li>
	 * <li><b>eq_{propertyName} </b>: 添加{propertyName}属性相等查询约束</li>
	 * <li><b>in_{propertyName} </b>: 添加{propertyName}属性in查询约束</li>
	 * <li><b>begin_{propertyName}</b>: 添加{propertyName}属性起始查询约束</li>
	 * <li><b>end_{propertyName}</b>: 添加{propertyName}属性结束查询约束</li>
	 * </ul>
	 * <br>
	 * 说明:
	 * <ul>
	 * <li>bean的属性的值不为空时添加约束</li>
	 * <li>相等查询约束里如果属性的值是空和空字符串，不添加约束</li>
	 * </ul>
	 * 
	 * @param bean
	 *            填充使用的对象
	 * @return QueryUtil对象
	 */
	public QueryUtil fillQueryBean(Object bean) {

		/*
		 * 查询表单属性名:查询类型类型
		 */
		HashMap<String, String> mapLogicPropertyName2PropertyName = new HashMap<>();
		/*
		 * 实际的属性名称：值
		 */
		HashMap<String, Object> mapLogicKey2Value = new HashMap<>();
		initParam(bean, mapLogicPropertyName2PropertyName, mapLogicKey2Value);
		for (Map.Entry<String, String> entry : mapLogicPropertyName2PropertyName.entrySet()) {
			String logicPropertyName = entry.getKey();
			String propertyName = entry.getValue();
			int signIndex = StringUtils.indexOf(logicPropertyName, SIGN);
			String prefixName = StringUtils.substring(logicPropertyName, 0, signIndex + 1);
			switch (prefixName) {
			case RANGE:
				Object startValue = mapLogicKey2Value.get(RANGE_BEGIN + logicPropertyName);
				Object endValue = mapLogicKey2Value.get(RANGE_END + logicPropertyName);
				range(propertyName, startValue, endValue);
				break;
			case IN:
				Object inValue = mapLogicKey2Value.get(logicPropertyName);
				in(propertyName, (Collection<?>) inValue);
				break;
			case EQ:
				Object eqValue = mapLogicKey2Value.get(logicPropertyName);
				is(propertyName, eqValue);
				break;
			case LIKE:
				Object likeValue = mapLogicKey2Value.get(logicPropertyName);
				like(propertyName, (String) likeValue);
				break;
			case EMPTY:
				Object likeValue2 = mapLogicKey2Value.get(logicPropertyName);
				if (likeValue2 instanceof String) {
					is(propertyName, (String) likeValue2);
				} else if (likeValue2 instanceof Integer) {
					is(propertyName, likeValue2);
				} else if (likeValue2 instanceof HashSet) {
					in(propertyName, (HashSet<?>) likeValue2);
				}
				break;
			}
		}
		return this;
	}

	/**
	 * 添加{propertyName}的<b>=</b>查询约束<br>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            查询的约束值
	 * @return 当前QueryUtil对象
	 */
	public QueryUtil is(String propertyName, Object value) {

		// 如果查询对象的值是String类，并且是空字符串时,什么也不做
		if ((value instanceof String) && StringUtils.isBlank((String) value)) {
			return this;
		} else {
			query.addCriteria(Criteria.where(propertyName).is(value));
		}
		return this;
	}

	/**
	 * 添加{propertyName}的<b>in</b>查询约束<br>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param likeValue2
	 *            查询的约束值
	 * @return 当前QueryUtil对象
	 */
	public QueryUtil in(String propertyName, Object likeValue2) {

		query.addCriteria(Criteria.where(propertyName).in(likeValue2));
		return this;
	}
	
	/**
	 * 添加{propertyName}的<b>in</b>查询约束<br>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param likeValue2
	 *            查询的约束值
	 * @return 当前QueryUtil对象
	 */
	public QueryUtil in(String propertyName, Collection<?> likeValue2) {
		query.addCriteria(Criteria.where(propertyName).in(likeValue2));
		return this;
	}

	/**
	 * 添加{propertyName}的<b>%like%</b>查询约束<br>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            查询的约束值
	 * @return 当前QueryUtil对象
	 */
	public QueryUtil like(String propertyName, String value) {

		Pattern pattern = Pattern.compile("^.*" + value + ".*$", Pattern.CASE_INSENSITIVE);
		query.addCriteria(Criteria.where(propertyName).regex(pattern));
		return this;
	}

	/**
	 * 添加{propertyName}的起始查询约束和结束查询约束<br>
	 * 
	 * @param propertyName
	 *            属性名
	 * @param beginValue
	 *            起始查询约束值
	 * @param endValue
	 *            结束查询约束值
	 * @return 当前QueryUtil对象
	 */
	public QueryUtil range(String propertyName, Object beginValue, Object endValue) {
		String condition = getConditonValue(new Boolean[] { beginValue != null, endValue != null });
		switch (condition) {
		case "11":
			query.addCriteria(Criteria.where(propertyName).gte(beginValue).lte(endValue));
			break;
		case "10":
			query.addCriteria(Criteria.where(propertyName).gte(beginValue));
			break;
		case "01":
			query.addCriteria(Criteria.where(propertyName).lte(endValue));
			break;
		}
		return this;
	}

	private QueryUtil() {
		this.query = new Query();
	}

	private void initParam(Object bean, HashMap<String, String> mapLogicPropertyName2PropertyName, HashMap<String, Object> mapLogicKey2Value) {

		try {
			Field[] fields = bean.getClass().getDeclaredFields();
			for (Field field : fields) {
				//add by fans.fan 增加注解判断.必须是添加了QueryCondation注解的,才是查询条件.
				if (!field.isAnnotationPresent(QueryCondation.class)) {
					continue;
				}
				//end by fans.fan 
				String name = field.getName();
				field.setAccessible(true);
				Object value = field.get(bean);
				if (null == value || (value instanceof String && StringUtils.isBlank((String) value))) {
					continue;
				}
				if (StringUtils.startsWith(name, RANGE_BEGIN) || StringUtils.startsWith(name, RANGE_END)) {
					String prefix = StringUtils.startsWith(name, RANGE_BEGIN) ? RANGE_BEGIN : RANGE_END;
					String propertyName = name.substring(prefix.length());
					String rangeName = RANGE + propertyName;
					mapLogicPropertyName2PropertyName.put(rangeName, propertyName);
					mapLogicKey2Value.put(prefix + rangeName, value);
				} else if (StringUtils.startsWith(name, EQ)) {
					String propertyName = name.substring(EQ.length());
					mapLogicPropertyName2PropertyName.put(name, propertyName);
					mapLogicKey2Value.put(name, value);
				} else if (StringUtils.startsWith(name, LIKE)) {
					String propertyName = name.substring(LIKE.length());
					mapLogicPropertyName2PropertyName.put(name, propertyName);
					mapLogicKey2Value.put(name, value);
				} else if (StringUtils.startsWith(name, IN)) {
					String propertyName = name.substring(IN.length());
					mapLogicPropertyName2PropertyName.put(name, propertyName);
					mapLogicKey2Value.put(name, value);
				} else {
					mapLogicPropertyName2PropertyName.put(name, name);
					mapLogicKey2Value.put(name, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getConditonValue(Boolean[] conditon) {

		StringBuilder sb = new StringBuilder();
		for (Boolean b : conditon) {
			sb = b ? sb.append("1") : sb.append("0");
		}
		return sb.toString();
	}

}
