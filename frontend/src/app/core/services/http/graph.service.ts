import { Injectable } from '@angular/core';
import { InstallationConfigurationService } from '@common/installation-configuration/installation-configuration.service';
import { BaseHttpService } from '@common/base/base-http.service';
import { Observable, throwError } from 'rxjs';
import { Graph, GraphEdge, GraphNode} from '@app/core/model/graph/graph.model';
import { GraphData } from '@app/core/model/graph/graph-data.mode';
import { GraphInfoLookup } from '@app/core/model/graph/graph-info.lookup';
import { GraphInfo } from '@app/core/model/graph/graph-info.model';
import { GraphDataLookup } from '@app/core/query/graph-data.lookup';
import { GraphLookup } from '@app/core/query/graph.lookup';
import { catchError } from 'rxjs/operators';
import { QueryResult } from '@common/model/query-result';
import { nameof } from 'ts-simple-nameof';

@Injectable()
export class GraphService {
	private get apiBase(): string {
		return `${this.installationConfiguration.appServiceAddress}api/graph`;
	}

	constructor(
		private installationConfiguration: InstallationConfigurationService,
		private http: BaseHttpService
	) {}

	public query(q: GraphLookup): Observable<QueryResult<Graph>> {
		const url = `${this.apiBase}/query`;
		return this.http
			.post<QueryResult<Graph>>(url, q)
			.pipe(catchError((error: any) => throwError(error)));
	}

	public queryData(q: GraphDataLookup): Observable<GraphData> {
		const url = `${this.apiBase}/data/query`;
		return this.http
			.post<GraphData>(url, q)
			.pipe(catchError((error: any) => throwError(error)));
	}

	public getGraphInfo( query: GraphInfoLookup, options?: Object ): Observable<GraphInfo> {
		const url = `${this.apiBase}/data/get-info`;
		return this.http.post<GraphInfo>(url, query, options);
	}




	// LOOKUPS

	public static  DefaultGraphListingLookup(params?: {}): GraphLookup{
		const lookup = new GraphLookup();

		lookup.project = {
			fields: [
				nameof<Graph>(x => x.id),

				nameof<Graph>(x => x.name),
				nameof<Graph>(x => x.description),

				nameof<Graph>(x => x.graphEdges) ,
				nameof<Graph>(x => x.graphEdges) + "." + nameof<GraphEdge>(x => x.id),
				nameof<Graph>(x => x.graphEdges) + "." + nameof<GraphEdge>(x => x.edge) + '.id',

				nameof<Graph>(x => x.graphNodes),
				nameof<Graph>(x => x.graphNodes) + "." + nameof<GraphNode>(x => x.node)+ '.id',
			]
		};
		lookup.order = { items: [nameof<Graph>(x => x.name)] };
		lookup.page = { offset: 0, size: 10 };
		return lookup;
	}
}
