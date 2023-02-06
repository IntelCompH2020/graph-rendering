package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.data.query.Query;
import gr.cite.tools.fieldset.BaseFieldSet;

public class GremlinLookup extends Lookup {
	private Paging page;
	private Ordering order;
	private Lookup.Header metadata;
	private BaseFieldSet project;

	public GremlinLookup() {
	}

	public Paging getPage() {
		return this.page;
	}

	public void setPage(Paging page) {
		this.page = page;
	}

	public Ordering getOrder() {
		return this.order;
	}

	public void setOrder(Ordering order) {
		this.order = order;
	}

	public Lookup.Header getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Lookup.Header metadata) {
		this.metadata = metadata;
	}

	public BaseFieldSet getProject() {
		return this.project;
	}

	public void setProject(BaseFieldSet project) {
		this.project = project;
	}

	protected void enrichCommon(Query query) {
		if (this.page != null) {
			query.setPage(this.page);
		}

		if (this.order != null && this.order.getItems() != null && this.order.getItems().size() > 0) {
			query.setOrder(this.order);
		}
	}

	public static class Header {
		public Boolean countAll;

		public Header() {
		}

		public Boolean getCountAll() {
			return this.countAll;
		}

		public void setCountAll(Boolean countAll) {
			this.countAll = countAll;
		}
	}
}
