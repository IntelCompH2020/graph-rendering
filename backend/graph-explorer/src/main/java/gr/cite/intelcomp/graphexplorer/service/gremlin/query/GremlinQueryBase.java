package gr.cite.intelcomp.graphexplorer.service.gremlin.query;

import gr.cite.intelcomp.graphexplorer.common.enums.CompareType;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.common.GremlinFactory;
import gr.cite.tools.data.query.*;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.*;
import java.util.stream.Collectors;

public abstract class GremlinQueryBase<D, X, Y> implements Query {

	private final GremlinFactory gremlinFactory;
	protected  final ConventionService conventionService;
	protected Ordering order;
	protected Paging page;

	public GremlinQueryBase(GremlinFactory gremlinFactory, ConventionService conventionService) {
		this.gremlinFactory = gremlinFactory;
		this.conventionService = conventionService;
	}

	public Ordering getOrder() {
		return this.order;
	}

	public void setOrder(Ordering order) {
		this.order = order;
	}

	public Paging getPage() {
		return this.page;
	}

	public void setPage(Paging page) {
		this.page = page;
	}

	protected abstract Boolean isFalseQuery();
	protected abstract GraphTraversal<X, Y> getGraphTraversal(GremlinFactory gremlinFactory);
	protected abstract String[] getLabels();

	protected abstract GraphTraversal<X, Y> applyFilters();

	protected GraphTraversal<X, Y> applyAuthZ() {
		return null;
	}

	protected abstract String fieldNameOf(FieldResolver item);
	protected abstract FieldSet fullDataFieldSet();
	protected abstract GraphTraversal<?, ?> fieldProjection(FieldResolver item, FieldSet nestedFields);

	protected abstract List<D> convert(List<Map<String, Object>> items, FieldSet projection, Set<String> columns);

	protected <X1, Y1> GraphTraversal<X1, Y1> applyOrdering(GraphTraversal<X1, Y1>  query) {
		if (this.getOrder() == null || this.getOrder().isEmpty()) return query;

		boolean hasStartOrder = false;
		for (String item : this.getOrder().getItems()) {
			OrderingFieldResolver resolver = new OrderingFieldResolver(item);
			String fieldName = this.fieldNameOf(resolver);
			if (fieldName == null || fieldName.isEmpty()) continue;
			if (!hasStartOrder) {
				query.order();
				hasStartOrder = true;
			}
			if (resolver.isAscending()) query.by(fieldName, org.apache.tinkerpop.gremlin.process.traversal.Order.asc);
			else query.by(fieldName, org.apache.tinkerpop.gremlin.process.traversal.Order.desc);
		}
		return query;
	}

	protected  <X1, Y1> GraphTraversal<X1, Y1> applyPaging(GraphTraversal<X1, Y1>  query) {
		if (this.getPage() != null && !this.getPage().isEmpty()) {
			int low = 0;
			if (this.getPage().getOffset() > 0) {
				low = this.getPage().getOffset();
			}

			int max = Integer.MAX_VALUE;
			if (this.getPage().getSize() > 0) {
				max = this.getPage().getSize();
			}
			query.range(low, max);
		}
		return query;
	}

	public List<D> collect() {
		return this.collectAs(this.fullDataFieldSet());
	}

	public List<Map<String, Object>> collectProjected(FieldSet projection) {
		if (this.isFalseQuery() || projection == null || projection.isEmpty()) return new ArrayList<>();
		GraphTraversal<X, Y> query = this.getGraphTraversal(this.gremlinFactory).hasLabel(P.within(this.getLabels()));

		GraphTraversal<?, ?> filters = this.applyFilters();
		GraphTraversal<?, ?> authZFilter = this.applyAuthZ();
		query = query.hasLabel(P.within(this.getLabels()));
		this.combineFilters(query, filters, authZFilter);
		query = this.applyOrdering(query);
		query = this.applyPaging(query);
		Map<String, GraphTraversal<?, ?>>  graphTraversalMap = this.buildProjection(projection);
		if (graphTraversalMap.isEmpty()) return new ArrayList<>();
		return this.applyProjection(query, graphTraversalMap).toList();
	}

	public List<D> collectAs(FieldSet projection) {
		List<Map<String, Object>> datas = this.collectProjected(projection);
		if (datas.isEmpty()) return new ArrayList<>();

		Set<String>  projectionFields = this.buildProjectionFields(projection);
		List<D> results = this.convert(datas, projection, projectionFields);

		return results != null ? results : new ArrayList<>();
	}

	public D first() {
		return this.firstAs(this.fullDataFieldSet());
	}

	public D firstAs(FieldSet projection) {
		Map<String, Object> data = this.firstProjected(projection);
		if (data == null) return null;

		List<D> results = this.convert(List.of(data), projection, this.buildProjectionFields(projection));

		return results != null || results.isEmpty() ? results.get(0) : null;
	}

	public Map<String, Object> firstProjected(FieldSet projection) {
		if (this.isFalseQuery() || projection == null || projection.isEmpty()) return null;
		GraphTraversal<X, Y> query = this.getGraphTraversal(this.gremlinFactory).hasLabel(P.within(this.getLabels()));

		GraphTraversal<?, ?> filters = this.applyFilters();
		GraphTraversal<?, ?> authZFilter = this.applyAuthZ();
		query = query.hasLabel(P.within(this.getLabels()));
		this.combineFilters(query, filters, authZFilter);
		query = this.applyOrdering(query);
		query = this.applyPaging(query);
		Map<String, GraphTraversal<?, ?>>  graphTraversalMap = this.buildProjection(projection);
		if (graphTraversalMap.isEmpty()) return null;
		return this.applyProjection(query, graphTraversalMap).next();
	}

	public Long count() {
		if (this.isFalseQuery()) return 0L;
		GraphTraversal<X, Y> query = this.getGraphTraversal(this.gremlinFactory);

		GraphTraversal<?, ?> filters = this.applyFilters();
		GraphTraversal<?, ?> authZFilter = this.applyAuthZ();
		query = query.hasLabel(P.within(this.getLabels()));
		this.combineFilters(query, filters, authZFilter);
		return query.count().next();
	}

	private <X1, Y1> GraphTraversal<X1, Map<String, Object>> applyProjection(GraphTraversal<X1, Y1> query,  Map<String, GraphTraversal<?, ?>> graphTraversalMap) {
		List<String> projected = graphTraversalMap.keySet().stream().collect(Collectors.toList());
		GraphTraversal<X1, Map<String, Object>> projectedQuery = null;
		if (projected.size() == 1){
			projectedQuery = query.project(projected.get(0));
		} else {
			projectedQuery = query.project(projected.get(0), projected.subList(1, projected.size()).toArray(new String[projected.size() - 1]));
		}
		for (String projectionPath: projected) {
			projectedQuery.by(graphTraversalMap.get(projectionPath));
		}
		return projectedQuery;

	}

	protected Map<String, GraphTraversal<?, ?>>  buildProjection(FieldSet projection) {
		List<String> projected = new ArrayList<>();
		Map<String, GraphTraversal<?, ?>> traversalHashMap = new HashMap<>();
		for (String item : projection.getFields()) {
			FieldResolver resolver = new FieldResolver(item);
			String fieldName = this.fieldNameOf(resolver);
			if (fieldName == null || fieldName.isEmpty()) continue;
			FieldSet nestedFields = projection.extractPrefixed(this.conventionService.asPrefix(resolver.getPrefix()));
			GraphTraversal<?, ?>  traversal =  this.fieldProjection(resolver, nestedFields);
			if (traversal == null) continue;;

			if (projected.contains(fieldName)) continue;
			else projected.add(fieldName);
			projected.add(fieldName);
			traversalHashMap.put(fieldName, traversal);
		}
		return traversalHashMap;
	}

	protected Set<String>  buildProjectionFields(FieldSet projection) {
		Set<String> projected = new HashSet<>();
		for (String item : projection.getFields()) {
			FieldResolver resolver = new FieldResolver(item);
			String fieldName = this.fieldNameOf(resolver);
			if (fieldName == null || fieldName.isEmpty()) continue;

			if (projected.contains(fieldName)) continue;
			else projected.add(fieldName);
			projected.add(fieldName);
		}
		return projected;
	}

	protected <D1, X1, Y1> void applySubQuery(GremlinQueryBase<D1, X1, Y1> subQuery, GraphTraversal<?, ?> subCriteria) {
		GraphTraversal<X1, Y1> filters = subQuery.applyFilters();
		GraphTraversal<X1, Y1> authZFilter = subQuery.applyAuthZ();
		subCriteria = subCriteria.hasLabel(P.within(subQuery.getLabels()));
		this.combineFilters(subCriteria, filters, authZFilter);
	}

	protected <D1, X1, Y1> GraphTraversal<?, Map<String, Object>> buildSelectSubQuery(GremlinQueryBase<D1, X1, Y1> subQuery, FieldSet projection, GraphTraversal<?, ?> subCriteria) {
		GraphTraversal<X1, Y1> filters = subQuery.applyFilters();
		GraphTraversal<X1, Y1> authZFilter = subQuery.applyAuthZ();
		subCriteria = subCriteria.hasLabel(P.within(subQuery.getLabels()));
		this.combineFilters(subCriteria, filters, authZFilter);
		Map<String, GraphTraversal<?, ?>>  graphTraversalMap = subQuery.buildProjection(projection);
		if (graphTraversalMap.isEmpty()) throw new RuntimeException("projection not set");
		return subQuery.applyProjection(subCriteria, graphTraversalMap);
	}


	protected <X> Boolean isEmpty(Collection<X> collection) {
		return collection == null ? false : collection.size() == 0;
	}

	protected <D1, X1, Y1> Boolean isFalseQuery(GremlinQueryBase<D1, X1, Y1> subQuery) {
		return subQuery == null ? false : subQuery.isFalseQuery();
	}

	public static <T> T convertSafe(Map<String, Object> map, Set<String> availableAliases, String alias, Class<T> clazz) {
		if (!availableAliases.contains(alias)) return null;
		Object o;
		try {
			o = map.get(alias);
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (o == null) return null;
		try {
			return clazz.cast(o);
		} catch (ClassCastException e) {
			return null;
		}

	}

	protected  <D1, X1, Y1> List<D1> convertNested(Map<String, Object> rawData, FieldSet projection, Set<String> columns, GremlinQueryBase<D1, X1, Y1> query, String field) {
		List<D1> items = null;
		if (columns.stream().anyMatch(x -> x.startsWith(field))) {
			ArrayList<Map<String, Object>> innerRawDataList = (ArrayList<Map<String, Object>>)rawData.get(field);
			Set<String> innerColumns = query.buildProjectionFields(projection);
			if (innerRawDataList != null && innerColumns != null && !innerColumns.isEmpty()) {
				items = query.convert(innerRawDataList, projection, innerColumns);
			}
		}
		return items;
	}

	protected <D1, X1, Y1> D1 convertInnerObject(Map<String, Object> rawData, FieldSet projection, Set<String> columns, GremlinQueryBase<D1, X1, Y1> query, String field) {
		if (columns.stream().anyMatch(x -> x.startsWith(field))) {
			Map<String, Object> innerRawData =(Map<String, Object>)rawData.get(field);
			Set<String> innerColumns = query.buildProjectionFields(projection);
			if (innerRawData != null && innerColumns != null && !innerColumns.isEmpty()) {
				List<D1> results = query.convert(List.of(innerRawData), projection, innerColumns);
				return results != null || results.isEmpty() ? results.get(0) : null;
			}
		}
		return null;
	}

	protected Boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	protected void combineFilters(GraphTraversal<?, ?> context, GraphTraversal<?, ?> filters, GraphTraversal<?, ?> authZFilter) {
		if (filters != null || authZFilter != null) {
			if (filters == null && authZFilter != null) {
				context.filter(authZFilter);
			} else if (filters != null && authZFilter == null) {
				context.filter(filters);
			} else {
				context.filter(__.and(authZFilter, filters));
			}

		}
	}

	protected  <V> P<V> getCompare(CompareType compareType, V value){
		switch (compareType) {
			case EQUAL: return P.eq(value);
			case NOT_EQUAL: return P.neq(value);
			case GREATER: return P.gt(value);
			case GREATER_EQUAL: return P.gte(value);
			case LESS: return P.lt(value);
			case LESS_EQUAL: return P.lte(value);
			default:
				throw new MyApplicationException("invalid type " + compareType);
		}
	}

}
